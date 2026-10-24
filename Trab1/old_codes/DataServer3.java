package dataServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DataServer3 {

    private static final String CAMINHO_DIRETORIO = "src/data";
    
    public void iniciar(String fileName, int porta) throws IOException {    	 
        final String NOME_ARQUIVO = fileName;

        try (ServerSocket servidor = new ServerSocket(porta)) {
            System.out.println("Servidor ouvindo na porta " + porta);

            while (true) {
                // Aguarda uma nova conexão TCP
                Socket cliente = servidor.accept();
                
                System.out.println("Conexao estabelecida com: " + cliente.getInetAddress().getHostAddress() + " " + cliente.getPort());
                
                
                BufferedReader entrada = new BufferedReader(
                        new InputStreamReader(
                            cliente.getInputStream()
                        )
                    );
                
                String mensagem = entrada.readLine();
                
                processarNumero(mensagem, cliente, NOME_ARQUIVO);
            }
        }
    }

    public static synchronized void processarNumero(String novoNumero, Socket cliente,  final String NOME_ARQUIVO) {
    	
    	Random gerador = new Random();
    	int sleepTime = gerador.nextInt(201) + 100;
        try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
        try {
            Path diretorioPath = Paths.get(CAMINHO_DIRETORIO);
            if (!Files.exists(diretorioPath)) {
                Files.createDirectories(diretorioPath);
            }

            Path arquivoPath = diretorioPath.resolve(NOME_ARQUIVO);

            // Lê todas as linhas atuais do arquivo (se o arquivo não existir, retorna uma lista vazia)
            List<String> linhas = Files.exists(arquivoPath) 
                ? Files.readAllLines(arquivoPath) 
                : new java.util.ArrayList<>();
            
            PrintWriter saida = new PrintWriter(
                    cliente.getOutputStream(), true
                );

            // Verifica se o número já existe na lista
            if (linhas.contains(novoNumero)) {
                System.out.println("O número " + novoNumero + " já existe no arquivo. Nenhuma alteração feita.");
                // Envia uma linha de texto ao servidor
                saida.println("Número já existe no arquivo.");          
                return;
            }

            // Adiciona o novo número e ordena a lista em ordem crescente
            linhas.add(novoNumero);
            Collections.sort(linhas);

            // Escreve todas as linhas de volta no arquivo (sobrescrevendo com os dados ordenados)
            Files.write(arquivoPath, linhas);
              
            System.out.println("Número " + novoNumero + " adicionado e ordenado com sucesso!");
            saida.println("Número adicionado e ordenado com sucesso!");

        } catch (IOException e) {
            System.err.println("Erro ao processar o arquivo: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erro: O arquivo contém dados que não são números válidos.");
        }
    }

//    public static void main(String[] args) {
//        // Simulando requisições chegando ao servidor em momentos diferentes
//        processarNumero(50);
//        processarNumero(10);
//        processarNumero(30);
//        processarNumero(30); // Tentativa de duplicado
//        processarNumero(5);
//    }
}