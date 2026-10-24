
package main;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

// Arquivo principal do sistema. Ele inicia todos os componentes em um só terminal.

public class Sistema {

    private static final List<Process> processos =
        new ArrayList<>();

    private static final String HOST = "127.0.0.1";

    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(
            new Thread(Sistema::encerrarProcessos)
        );

        try {
            String java = System.getProperty("java.home")
                + File.separator + "bin" + File.separator
                + (System.getProperty("os.name")
                    .toLowerCase().contains("win")
                    ? "java.exe" : "java");

            String classpath = "bin";

            // 1. Servidores de dados
            iniciarProcesso(java, classpath,
                "main.dataserver");
            aguardarPorta(5001);

            iniciarProcesso(java, classpath,
                "main.dataserver2");
            aguardarPorta(5002);

            // 2. Servidores de aplicação
            iniciarProcesso(java, classpath,
                "main.server1");
            aguardarPorta(666);

            iniciarProcesso(java, classpath,
                "main.server2");
            aguardarPorta(616);

            iniciarProcesso(java, classpath,
                "main.server3");
            aguardarPorta(1610);

            // 3. Balanceador
            iniciarProcesso(java, classpath,
                "balancer.Balanceador");
            aguardarPorta(7000);

            // 4. Clientes
            iniciarProcesso(java, classpath,
                "main.cliente1");

            iniciarProcesso(java, classpath,
                "main.cliente2");

            System.out.println(
                "Todos os componentes foram iniciados."
            );

            // Mantém o programa principal vivo.
            while (true) {
                Thread.sleep(1000);
            }

        } catch (Exception e) {
            System.err.println(
                "Erro ao iniciar o sistema: "
                + e.getMessage()
            );

            encerrarProcessos();
        }
    }

    private static void iniciarProcesso(
            String java,
            String classpath,
            String classe) throws IOException {

        System.out.println(
            "Iniciando " + classe + "..."
        );

        Process processo = new ProcessBuilder(
            java,
            "-cp",
            classpath,
            classe
        )
            .inheritIO()
            .start();

        processos.add(processo);
    }

    private static void aguardarPorta(int porta)
            throws InterruptedException, IOException {

        long limite = System.currentTimeMillis() + 10000;

        while (System.currentTimeMillis() < limite) {

            try (Socket socket = new Socket()) {
                socket.connect(
                    new InetSocketAddress(HOST, porta),
                    300
                );

                System.out.println(
                    "Porta " + porta + " disponível."
                );
                return;

            } catch (IOException e) {
                Thread.sleep(200);
            }
        }

        throw new IOException(
            "Tempo esgotado aguardando a porta " + porta
        );
    }

    private static void encerrarProcessos() {
        System.out.println(
            "Encerrando os componentes..."
        );

        for (Process processo : processos) {
            processo.destroy();
        }

        for (Process processo : processos) {
            try {
                if (!processo.waitFor(
                        2, java.util.concurrent.TimeUnit.SECONDS)) {
                    processo.destroyForcibly();
                }
            } catch (InterruptedException e) {
                processo.destroyForcibly();
                Thread.currentThread().interrupt();
            }
        }
    }
}
