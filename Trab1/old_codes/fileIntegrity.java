package dataServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class fileIntegrity {
	
	private static final String CAMINHO_DIRETORIO = "src/data";
	private static final String NOME_ARQUIVO1 = "data1.txt";
	private static final String NOME_ARQUIVO2 = "data2.txt";
	
	public static synchronized void checkConsistency (final String NOME_ARQUIVO) {
		Path diretorioPath = Paths.get(CAMINHO_DIRETORIO);
		Path arquivoPath1 = diretorioPath.resolve(NOME_ARQUIVO1);
		Path arquivoPath2 = diretorioPath.resolve(NOME_ARQUIVO2);
		
		if (NOME_ARQUIVO.equals(NOME_ARQUIVO1)) {
	        // Lê todas as linhas atuais do arquivo (se o arquivo não existir, retorna uma lista vazia)
	        try {
				List<String> linhas = Files.exists(arquivoPath1) 
				    ? Files.readAllLines(arquivoPath1) 
				    : new java.util.ArrayList<>();
				
		        // Escreve todas as linhas de volta no arquivo2 (sobrescrevendo os dados)
		        Files.write(arquivoPath2, linhas);
		        
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else if (NOME_ARQUIVO.equals(NOME_ARQUIVO2)) {
	        // Lê todas as linhas atuais do arquivo (se o arquivo não existir, retorna uma lista vazia)
	        try {
				List<String> linhas = Files.exists(arquivoPath2) 
				    ? Files.readAllLines(arquivoPath2) 
				    : new java.util.ArrayList<>();
				
		        // Escreve todas as linhas de volta no arquivo2 (sobrescrevendo os dados)
		        Files.write(arquivoPath1, linhas);
		        
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			System.out.println("Erro na consistência de arquivo");
		}
		
//        // Lê todas as linhas atuais do arquivo (se o arquivo não existir, retorna uma lista vazia)
//        try {
//			List<String> linhas = Files.exists(arquivoPath1) 
//			    ? Files.readAllLines(arquivoPath1) 
//			    : new java.util.ArrayList<>();
//			
//	        // Escreve todas as linhas de volta no arquivo2 (sobrescrevendo os dados)
//	        Files.write(arquivoPath2, linhas);
//	        
//		} catch (IOException e) {
//			e.printStackTrace();
//		}		
	}
}
