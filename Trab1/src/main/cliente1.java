package main;

import java.io.IOException;

import client.Client;

public class cliente1 {
    public static void main(String [] args) throws InterruptedException{
        Client client1 = new Client();
        try {
			client1.iniciar("1");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}      
    }
}