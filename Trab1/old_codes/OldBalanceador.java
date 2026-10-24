package balancer;

import java.util.Random;

public class OldBalanceador {
	public static int requestPort() {
		
		// Balanceador no momento está aleatório. Quero deixar em round robin depois
		
		int[] portas = {666, 616, 1610};
		
    	Random gerador1 = new Random();
    	int PortIndex = gerador1.nextInt(3);
    	
    	return portas[PortIndex];
	}
	
	public static int requestServer() {
		
		// Balanceador aleatório para servidores de dados.
		
		int[] portas = {5001, 5002};
		
    	Random gerador2 = new Random();
    	int PortIndex = gerador2.nextInt(2);
    	
    	return portas[PortIndex];
	}
}
