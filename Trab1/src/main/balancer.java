package main;

import java.io.IOException;

import balancer.Balanceador;

public class balancer {
    public static void main(String [] args) throws InterruptedException{
    	Balanceador balanceador = new Balanceador();
        	try {
				balanceador.iniciar();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    }
}
