
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Random;

public class Client {

    private static final String ENDERECO = "127.0.0.1";
    private static final int PORTA_BALANCEADOR = 7000;
    private static final String DIRETORIO = "src/logs";

    public void iniciar(String idCliente)
            throws InterruptedException, IOException {

        Random gerador = new Random();

        Path diretorioLogs = Paths.get(DIRETORIO);
        Files.createDirectories(diretorioLogs);

        Path arquivoLog = diretorioLogs.resolve("cliente-" + idCliente + ".txt");

        System.out.println("Cliente " + idCliente + " iniciado.");

        while (!Thread.currentThread().isInterrupted()) {

            int numero = gerador.nextInt(1000) + 1;

            // Registra o valor gerado antes de enviá-lo.
            Files.writeString(
                arquivoLog,
                numero + System.lineSeparator(),
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );

            boolean concluido = false;

            while (!concluido && !Thread.currentThread().isInterrupted()) {
                try (
                    Socket conexao = new Socket(ENDERECO, PORTA_BALANCEADOR);
                    PrintWriter saida =
                        new PrintWriter(
                            conexao.getOutputStream(), true
                        );
                    BufferedReader entrada =
                        new BufferedReader(
                            new InputStreamReader(
                                conexao.getInputStream()
                            )
                        )
                ) {
                    conexao.setSoTimeout(15000);

                    saida.println(numero);

                    String resposta = entrada.readLine();

                    if (resposta != null && !resposta.startsWith("ERROR|")) {
                        System.out.println("Cliente " + idCliente + " enviou " + numero + " | Resposta: " + resposta);
                        concluido = true;

                    } else {
                        System.err.println("Resposta inválida: " + resposta);
                    }

                } catch (IOException e) {
                    System.err.println("Falha ao enviar " + numero + ". Nova tentativa em 500 ms. "+ e.getMessage());
                    Thread.sleep(500);
                }
            }

            int intervalo = gerador.nextInt(151) + 50;
            Thread.sleep(intervalo);
        }

        System.out.println("Cliente " + idCliente + " encerrado.");
    }

    public static void main(String[] args) throws Exception {
        String id = args.length > 0 ? args[0] : "1";
        new Client().iniciar(id);
    }
}
