package server;

import java.io.*;
import java.net.*;

import balancer.Balanceador;

public class OldServer2 {

    public static void iniciar(int port) throws IOException {
    	int porta = port;
    	int portaServer1;

        try (ServerSocket servidor = new ServerSocket(porta)) {
            System.out.println("Servidor ouvindo na porta " + porta);

            while (true) {
            	portaServer1 = Balanceador.requestServer();
            	final int portaServer = portaServer1;
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
                        
                        // Alterações;
                        
                        try (
                                Socket conexaoServer = new Socket("127.0.0.1", portaServer);
                                PrintWriter saidaServer = new PrintWriter(
                                    conexaoServer.getOutputStream(), true
                                );
                                BufferedReader entradaServer = new BufferedReader(
                                    new InputStreamReader(
                                    	conexaoServer.getInputStream()
                                    )
                                )
                            ) {
                                // Envia uma linha de texto ao servidor
                                saidaServer.println(mensagem);

                                // Aguarda a resposta do servidor
                                String respostaServer = entradaServer.readLine();

                                if (respostaServer != null) {
                                    System.out.println("Servidor>> " + respostaServer);
                                } else {
                                    System.out.println(
                                        "O servidor encerrou a conexão sem resposta."
                                    );
                                }

                            } catch (IOException e) {
                                System.err.println(
                                    "Erro na comunicação: " + e.getMessage()
                                );
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
