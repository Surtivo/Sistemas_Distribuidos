package client;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Random;

import balancer.Balanceador;

public class Cliente{

    public void iniciar(String endereco) {
        ObjectOutputStream saida;
        ObjectInputStream entrada;
        Socket conexao;
        String mensagem = "";
        int porta;
        
        while (true) {
        	porta = Balanceador.requestPort();
        	try {
			    conexao = new Socket(endereco, porta);
			    System.out.println("Conectado ao servidor " + endereco + ", na porta: " + porta);
//			    System.out.println("Digite: FIM para encerrar a conexao");

			    // ligando as conexoes de saida e de entrada
			    saida = new ObjectOutputStream(conexao.getOutputStream());
			    saida.flush();
			    entrada = new ObjectInputStream(conexao.getInputStream());

			    //lendo a mensagem enviada pelo servidor
			    mensagem = (String) entrada.readObject();
			    System.out.println("Servidor>> "+mensagem);

//			    do {
		    	Random gerador = new Random();
		    	int numeroInt = gerador.nextInt(1000) + 1;
		    	
//		    	System.out.print("..: ");
		        mensagem = String.valueOf(numeroInt);
		        saida.writeObject(mensagem);
		        saida.flush();
		        
		        //lendo a mensagem enviada pelo servidor
		        mensagem = (String) entrada.readObject();
		        System.out.println("Servidor>> "+mensagem + "\n");
		        
		        int sleepTime = gerador.nextInt(151) + 50;
		        Thread.sleep(sleepTime);
			        
//			    } while (!mensagem.equals("0"));

			    saida.close();
			    entrada.close();
			    conexao.close();

			} catch (Exception e) {
			    System.err.println("erro: " + e.toString());
			}
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Uso: java tcp.Cliente <endereco-IP>");
            System.exit(1);
        }

        Cliente c = new Cliente();
        c.iniciar(args[0]);
    }
}