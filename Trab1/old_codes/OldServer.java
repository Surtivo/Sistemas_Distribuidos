package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class OldServer {

    public void iniciar(int porta) {
//        ObjectOutputStream saida;
//        ObjectInputStream entrada;
//        boolean sair = false;
//        String mensagem = "";

        try (// criando um socket para ouvir na porta e com uma fila de tamanho 10
		ServerSocket servidor = new ServerSocket(porta, 10)) {
            Socket conexao;
            while (true) {
                System.out.println("Ouvindo na porta: " + porta);
                
                //ficarah bloqueado aqui ate' alguem cliente se conectar
                conexao = servidor.accept();

                System.out.println("Conexao estabelecida com: " + conexao.getInetAddress().getHostAddress() + " " + conexao.getPort());
                
                Thread clienteThread = new Thread(new AtendenteCliente(conexao));
                clienteThread.start();
//                sair = true;
            }

        } catch (Exception e) {
            System.err.println("Erro: " + e.toString());
        }
    }
    
    private static class AtendenteCliente implements Runnable {
        private Socket conexao;

        public AtendenteCliente(Socket conexao) {
            this.conexao = conexao;
        }

        @Override
        public void run() {
            try (
                ObjectOutputStream saida = new ObjectOutputStream(conexao.getOutputStream());
                ObjectInputStream entrada = new ObjectInputStream(conexao.getInputStream())
            ) {
                saida.flush();
                // Enviando mensagem inicial ao cliente
                saida.writeObject("Conexao estabelecida com sucesso...");

                String mensagem;
                int IntVal;
                
                do {
                    mensagem = (String) entrada.readObject();                    
                    System.out.println("Cliente [" + conexao.getInetAddress().getHostAddress() + " " + conexao.getPort() + "] >> " + mensagem);
                    IntVal = Integer.parseInt(mensagem);
                    
                    if (IntVal == 0){
                    	System.out.println("Conexao encerrada pelo cliente: " + conexao.getInetAddress().getHostAddress());
                    }
                    else if (Evaluate(IntVal)) {
                    	saida.writeObject(String.valueOf(true));
                    }else {
                    	saida.writeObject(String.valueOf(false));
                    }
                    
                    saida.flush();
                } while (!mensagem.equals("0"));
                
            } catch (Exception e) {
                System.err.println("Erro na conexao com o cliente: " + e.toString());
            } finally {
                try {
                    System.out.println("Conexao encerrada pelo cliente");
                    conexao.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public static boolean Evaluate (int number) {
    	if(number%2 == 0)
    		return true;
    	
    	return false;
    }

    public static void main(String[] args) {
        int porta = -1;

        //verificando se foi informado 1 argumento de linha de comando
        if (args.length < 1) {
            System.err.println("Uso: java tcp.Servidor <porta>");
            System.exit(1);
        }

        try { // para garantir que somente inteiros serao atribuidos a porta

            porta = Integer.parseInt(args[0]);

        } catch (Exception e) {
            System.err.println("Erro: " + e.toString());
            System.exit(1);
        }

        if (porta < 1024) {
            System.err.println("A porta deve ser maior que 1024.");
            System.exit(1);
        }

        OldServer s = new OldServer();
        s.iniciar(porta);
    }
}