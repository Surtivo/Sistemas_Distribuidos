package main;

import java.io.IOException;

import client.Client;

public class cliente2 {
    public static void main(String [] args) throws InterruptedException{
        Client client2 = new Client();
        try {
			client2.iniciar("2");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
    }
}