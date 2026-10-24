
package server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Random;

public class Server2 {

    private static final String ENDERECO_DADOS = "127.0.0.1";

    private static final int[] PORTAS_DADOS = {5001, 5002};

    // Escolhe aleatoriamente um servidor de dados para cada requisição.
    private static int escolherServidorDeDados() {
		
    	Random gerador = new Random();
    	int PortIndex = gerador.nextInt(2);

        return PORTAS_DADOS[PortIndex];
    }

    public static void iniciar(int porta) throws IOException {
        try (ServerSocket servidor = new ServerSocket(porta)) {

            System.out.println("Servidor de aplicação iniciado na porta "+ porta);

            while (true) {
                Socket cliente = servidor.accept();

                Thread worker = new Thread(
                    () -> processarRequisicao(cliente)
                );

                worker.start();
            }
        }
    }

    private static void processarRequisicao(Socket cliente) {
        try (
            Socket conexaoCliente = cliente;
            BufferedReader entrada =
                new BufferedReader(
                    new InputStreamReader(
                        conexaoCliente.getInputStream()
                    )
                );
            PrintWriter saida =
                new PrintWriter(
                    conexaoCliente.getOutputStream(), true
                )
        ) {
            String mensagem = entrada.readLine();

            if (mensagem == null) {
                return;
            }

            int numero;

            try {
                numero = Integer.parseInt(mensagem);

                if (numero < 1 || numero > 1000) {
                    saida.println(
                        "ERROR|Número fora do intervalo 1..1000."
                    );
                    return;
                }

            } catch (NumberFormatException e) {
                saida.println("ERROR|Número inválido.");
                return;
            }

            boolean primo = ehPrimo(numero);
            int portaDados = escolherServidorDeDados();
            String respostaDados;

            try (
                Socket conexaoDados = new Socket(
                    ENDERECO_DADOS, portaDados
                );
                PrintWriter saidaDados =
                    new PrintWriter(
                        conexaoDados.getOutputStream(), true
                    );
                BufferedReader entradaDados =
                    new BufferedReader(
                        new InputStreamReader(
                            conexaoDados.getInputStream()
                        )
                    )
            ) {
                conexaoDados.setSoTimeout(15000);
                saidaDados.println(numero);
                respostaDados = entradaDados.readLine();

                if (respostaDados == null) {
                    saida.println(
                        "ERROR|Servidor de dados não respondeu."
                    );
                    return;
                }

            } catch (IOException e) {
                System.err.println("Falha no servidor de dados: " + e.getMessage());
                saida.println("ERROR|Falha na operação de dados.");
                return;
            }

            if (respostaDados.startsWith("ERROR|")) {
                saida.println(respostaDados);
                return;
            }

            System.out.println("Número " + numero + " | Primo: " + primo + " | Dados: " + respostaDados);
            saida.println("Número=" + numero + "; Primo=" + primo + "; Dados=" + respostaDados);

        } catch (IOException e) {
            System.err.println("Erro no worker: " + e.getMessage());
        }
    }

    private static boolean ehPrimo(int numero) {
        if (numero < 2) {
            return false;
        }

        for (int i = 2; i * i <= numero; i++) {
            if (numero % i == 0) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) throws IOException {
        int porta = args.length > 0
            ? Integer.parseInt(args[0])
            : 666;

        iniciar(porta);
    }
}
