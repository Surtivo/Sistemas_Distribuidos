package dataServer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ServidorNumeros {

    private static final String CAMINHO_DIRETORIO = "src/data";
    private static final String NOME_ARQUIVO = "numeros.txt";

    public static synchronized void processarNumero(int novoNumero) {
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

            // Converte as linhas para uma lista de inteiros
            List<Integer> numeros = linhas.stream()
                    .filter(s -> !s.trim().isEmpty())
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            // Verifica se o número já existe na lista
            if (numeros.contains(novoNumero)) {
                System.out.println("O número " + novoNumero + " já existe no arquivo. Nenhuma alteração feita.");
                return;
            }

            // Adiciona o novo número e ordena a lista em ordem crescente
            numeros.add(novoNumero);
            Collections.sort(numeros);

            // Converte todos os números de volta para linhas de texto
            List<String> novasLinhas = numeros.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList());

            // Escreve todas as linhas de volta no arquivo (sobrescrevendo com os dados ordenados)
            Files.write(arquivoPath, novasLinhas);

            System.out.println("Número " + novoNumero + " adicionado e ordenado com sucesso!");

        } catch (IOException e) {
            System.err.println("Erro ao processar o arquivo: " + e.getMessage());
        } catch (NumberFormatException e) {
            System.err.println("Erro: O arquivo contém dados que não são números válidos.");
        }
    }

    public static void main(String[] args) {
        // Simulando requisições chegando ao servidor em momentos diferentes
        processarNumero(50);
        processarNumero(10);
        processarNumero(30);
        processarNumero(30); // Tentativa de duplicado
        processarNumero(5);
    }
}