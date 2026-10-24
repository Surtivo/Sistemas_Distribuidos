
package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;

import balancer.Balanceador;

public class OldClient {

    public void iniciar(String endereco) throws InterruptedException {
        String mensagem;
        int porta;

        do {
        	porta = Balanceador.requestPort();
            
	    	Random gerador = new Random();
	    	int numeroInt = gerador.nextInt(1000) + 1;
	    	mensagem = String.valueOf(numeroInt);

            if (mensagem.equalsIgnoreCase("FIM") || mensagem.equals("0")) {
                break;
            }
            
            System.out.println(
                    "Conectando ao servidor " + endereco +
                    ", na porta: " + porta
                );

//            System.out.println(
//                "Digite 0 ou FIM para encerrar."
//            );

            // Uma conexão por requisição
            try (
                Socket conexao = new Socket(endereco, porta);
                PrintWriter saida = new PrintWriter(
                    conexao.getOutputStream(), true
                );
                BufferedReader entrada = new BufferedReader(
                    new InputStreamReader(
                        conexao.getInputStream()
                    )
                )
            ) {
                // Envia uma linha de texto ao servidor
                saida.println(mensagem);

                // Aguarda a resposta do worker
                String resposta = entrada.readLine();

                if (resposta != null) {
                    System.out.println("Servidor>> " + resposta);
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
	        
	        int sleepTime = gerador.nextInt(151) + 50;
	        Thread.sleep(sleepTime);

        } while (true);

        System.out.println("Cliente encerrado.");

    }

//    public static void main(String[] args) throws NumberFormatException, InterruptedException {
//        if (args.length < 2) {
//            System.err.println(
//                "Uso: java client.Client <endereco-IP>"
//            );
//            System.exit(1);
//        }
//
//        Client c = new Client();
//        c.iniciar(args[0]);
//    }
}
