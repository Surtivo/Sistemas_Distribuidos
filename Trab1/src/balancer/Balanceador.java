
package balancer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class Balanceador {

    private static final int PORTA_BALANCEADOR = 7000;
    private static final String ENDERECO_SERVIDORES = "127.0.0.1";

    private static final int[] PORTAS_SERVIDORES = {666, 616, 1610};

    private static final AtomicInteger PROXIMO_SERVIDOR = new AtomicInteger(0);

    // Round Robin para escolher a próxima porta do servidor de aplicação.

    private static int requestPort() {
        int indice = Math.floorMod(
            PROXIMO_SERVIDOR.getAndIncrement(),
            PORTAS_SERVIDORES.length
        );

        return PORTAS_SERVIDORES[indice];
    }

    public void iniciar() throws IOException {
        try (ServerSocket servidor =
                 new ServerSocket(PORTA_BALANCEADOR)) {

            System.out.println(
                "Balanceador iniciado na porta "
                + PORTA_BALANCEADOR
            );

            while (true) {
                Socket cliente = servidor.accept();

                Thread worker = new Thread(
                    () -> encaminhar(cliente)
                );

                worker.start();
            }
        }
    }

    private void encaminhar(Socket cliente) {
        int portaDestino = requestPort();

        try (
            Socket conexaoCliente = cliente;
            BufferedReader entradaCliente =
                new BufferedReader(
                    new InputStreamReader(
                        conexaoCliente.getInputStream()
                    )
                );
            PrintWriter saidaCliente =
                new PrintWriter(
                    conexaoCliente.getOutputStream(), true
                )
        ) {
            String mensagem = entradaCliente.readLine();

            if (mensagem == null) {
                return;
            }

            System.out.println(
                "Encaminhando requisição para a porta "
                + portaDestino + ": " + mensagem
            );

            try (
                Socket servidorApp = new Socket(
                    ENDERECO_SERVIDORES, portaDestino
                );
                PrintWriter saidaApp =
                    new PrintWriter(
                        servidorApp.getOutputStream(), true
                    );
                BufferedReader entradaApp =
                    new BufferedReader(
                        new InputStreamReader(
                            servidorApp.getInputStream()
                        )
                    )
            ) {
                servidorApp.setSoTimeout(10000);

                saidaApp.println(mensagem);

                String resposta = entradaApp.readLine();

                if (resposta == null) {
                    saidaCliente.println("ERROR|Servidor de aplicação encerrou a conexão sem resposta.");
                } else {
                    saidaCliente.println(resposta);
                }
            }

        } catch (IOException e) {
            System.err.println(
                "Erro no encaminhamento: " + e.getMessage()
            );
        }
    }

    public static void main(String[] args) throws IOException {
        new Balanceador().iniciar();
    }
}
