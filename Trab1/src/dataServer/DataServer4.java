
package dataServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class DataServer4 {

    private static final String ENDERECO = "127.0.0.1";
    private static final int PORTA_PRIMARIO = 5001;
    private static final int PORTA_REPLICA = 5002;
    private static final String DIRETORIO = "src/data";

    private final int porta;
    private final boolean primario;
    private final String nomeArquivo;

    private final Random gerador = new Random();

    public DataServer4(int porta, boolean primario, String nomeArquivo) {
        this.porta = porta;
        this.primario = primario;
        this.nomeArquivo = nomeArquivo;
    }

    private Path caminhoArquivo() throws IOException {
        Path diretorio = Paths.get(DIRETORIO);
        Files.createDirectories(diretorio);

        return diretorio.resolve(nomeArquivo);
    }

    public void iniciar() throws IOException {
        try (ServerSocket servidor = new ServerSocket(porta)) {

            System.out.println("Servidor de dados " + (primario ? "PRIMÁRIO" : "RÉPLICA") + " iniciado na porta " + porta);

            while (true) {
                Socket cliente = servidor.accept();

                Thread worker = new Thread(
                    () -> processarCliente(cliente)
                );

                worker.start();
            }
        }
    }

    private void processarCliente(Socket cliente) {
        try (
            Socket conexao = cliente;
            BufferedReader entrada =
                new BufferedReader(
                    new InputStreamReader(
                        conexao.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                );
            PrintWriter saida =
                new PrintWriter(
                    conexao.getOutputStream(), true
                )
        ) {
            conexao.setSoTimeout(15000);

            String mensagem = entrada.readLine();

            if (mensagem == null) {
                return;
            }

            if (mensagem.startsWith("REPLICATE|")) {
                if (primario) {
                    saida.println("ERROR|O primário não aceita REPLICATE.");
                    return;
                }

                String numero = mensagem.substring("REPLICATE|".length());

                synchronized (this) {
                    inserirSeAusente(numero);
                }

                saida.println("ACK|" + numero);
                return;
            }

            if (mensagem.startsWith("DELETE|")) {
                if (primario) {
                    saida.println("ERROR|DELETE só é aceito pela réplica.");
                    return;
                }

                String numero = mensagem.substring(
                    "DELETE|".length()
                );

                synchronized (this) {
                    removerNumero(numero);
                }

                saida.println("ACK|" + numero);
                return;
            }

            if (!mensagem.matches("\\d{1,4}")) {
                saida.println("ERROR|Mensagem inválida.");
                return;
            }

            int numero = Integer.parseInt(mensagem);

            if (numero < 1 || numero > 1000) {
                saida.println("ERROR|Número fora do intervalo 1..1000.");
                return;
            }

            String resposta;

            if (primario) {
                resposta = processarNoPrimario(
                    String.valueOf(numero)
                );
            } else {
                // A réplica não realiza inserções normais.
                // Encaminha a solicitação ao primário.
                resposta = encaminharAoPrimario(
                    String.valueOf(numero)
                );
            }

            saida.println(resposta);

        } catch (IOException | NumberFormatException e) {
            System.err.println(
                "Erro no servidor de dados " + porta
                + ": " + e.getMessage()
            );
        }
    }

    private String processarNoPrimario(String numero) {
        synchronized (DataServer4.class) {
            try {
                // Simula latência do processamento de dados.
                Thread.sleep(gerador.nextInt(201) + 100);

                Path arquivo = caminhoArquivo();
                List<String> linhas = lerLinhas(arquivo);

                if (linhas.contains(numero)) {
                    return "EXISTS|" + numero;
                }

                // Primeiro, grava na réplica e aguarda ACK.
                String respostaReplica = enviarParaReplica("REPLICATE|" + numero);

                if (!("ACK|" + numero).equals(respostaReplica)) {
                    return "ERROR|Réplica não confirmou a inserção.";
                }

                // Só depois da confirmação da réplica grava localmente.
                try {
                    inserirSeAusente(numero);
                } catch (IOException e) {
                    // Compensa a inserção da réplica se a escrita
                    // local falhar.
                    try {
                        enviarParaReplica("DELETE|" + numero);
                    } catch (IOException compensacao) {
                        System.err.println("ATENÇÃO: falha na compensação. "+ "Os arquivos podem divergir.");
                    }

                    return "ERROR|Falha ao gravar no primário.";
                }

                System.out.println("Número " + numero + " inserido e replicado.");

                return "INSERTED|" + numero;

            } catch (IOException e) {
                return "ERROR|Falha na operação: "
                    + e.getMessage();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return "ERROR|Operação interrompida.";
            }
        }
    }

    private String encaminharAoPrimario(String mensagem) throws IOException {

        try (
            Socket conexao = new Socket(ENDERECO, PORTA_PRIMARIO);
            PrintWriter saida =
                new PrintWriter(
                    conexao.getOutputStream(), true
                );
            BufferedReader entrada =
                new BufferedReader(
                    new InputStreamReader(
                        conexao.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )
        ) {
            conexao.setSoTimeout(15000);
            saida.println(mensagem);

            String resposta = entrada.readLine();

            if (resposta == null) {
                throw new IOException(
                    "Primário encerrou a conexão sem resposta."
                );
            }
            
            System.out.println("Encaminhando requisição ao primário: " + mensagem + " | Resposta: " + resposta);

            return resposta;
        }
    }

    private String enviarParaReplica(String mensagem)
            throws IOException {

        try (
            Socket conexao = new Socket(ENDERECO, PORTA_REPLICA);
            PrintWriter saida =
                new PrintWriter(
                    conexao.getOutputStream(), true
                );
            BufferedReader entrada =
                new BufferedReader(
                    new InputStreamReader(
                        conexao.getInputStream(),
                        StandardCharsets.UTF_8
                    )
                )
        ) {
            conexao.setSoTimeout(15000);
            saida.println(mensagem);

            String resposta = entrada.readLine();

            if (resposta == null) {
                throw new IOException(
                    "Réplica encerrou a conexão sem resposta."
                );
            }

            return resposta;
        }
    }

    private List<String> lerLinhas(Path arquivo)
            throws IOException {

        if (!Files.exists(arquivo)) {
            return new ArrayList<>();
        }

        return new ArrayList<>(
            Files.readAllLines(arquivo, StandardCharsets.UTF_8)
        );
    }

    private void inserirSeAusente(String numero)
            throws IOException {

        Path arquivo = caminhoArquivo();
        List<String> linhas = lerLinhas(arquivo);

        if (!linhas.contains(numero)) {
            linhas.add(numero);

            linhas.sort(
                Comparator.comparingInt(Integer::parseInt)
            );

            Files.write(
                arquivo,
                linhas,
                StandardCharsets.UTF_8
            );
        }
    }

    private void removerNumero(String numero)
            throws IOException {

        Path arquivo = caminhoArquivo();
        List<String> linhas = lerLinhas(arquivo);

        linhas.removeIf(numero::equals);

        linhas.sort(
            Comparator.comparingInt(Integer::parseInt)
        );

        Files.write(
            arquivo,
            linhas,
            StandardCharsets.UTF_8
        );
    }

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0
            ? Integer.parseInt(args[0])
            : PORTA_PRIMARIO;

        boolean primario = args.length <= 1
            || Boolean.parseBoolean(args[1]);

        String arquivo = args.length > 2
            ? args[2]
            : (primario ? "data1.txt" : "data2.txt");

        new DataServer4(porta, primario, arquivo).iniciar();
    }
}
