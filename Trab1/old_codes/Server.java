package server;

import java.io.*;
import java.net.*;

public class Server {

    public static void iniciar(int port) throws IOException {
        int porta = port;

        try (ServerSocket servidor = new ServerSocket(porta)) {
            System.out.println("Servidor ouvindo na porta " + porta);

            while (true) {
                // Aguarda uma nova conexão TCP
                Socket cliente = servidor.accept();
                
                System.out.println("Conexao estabelecida com: " + cliente.getInetAddress().getHostAddress() + " " + cliente.getPort());

                // Cria uma thread para processar essa conexão
                Thread worker = new Thread(() -> {
                    try (
                        Socket conexao = cliente;
                        BufferedReader entrada = new BufferedReader(
                            new InputStreamReader(
                                conexao.getInputStream()
                            )
                        );
                        PrintWriter saida = new PrintWriter(
                            conexao.getOutputStream(), true
                        )
                    ) {
                        String mensagem = entrada.readLine();
                        int numero = Integer.parseInt(mensagem);

                        // Primeiro: calcular a primalidade
                        boolean primo = numero >= 2;

                        for (int i = 2; i * i <= numero && primo; i++) {
                            if (numero % i == 0) {
                                primo = false;
                            }
                        }
                        
                        // Aqui entraria a comunicação com
                        // um servidor de dados via outro socket.
                        System.out.println("Número " + numero + " é primo? " + primo);

                        saida.println("Primalidade: " + primo);

                    }	catch (IOException | NumberFormatException e) {
                    		System.err.println("Erro no worker: " + e.getMessage());
                    	}
                });

                worker.start();
            }
        }
    }
}
