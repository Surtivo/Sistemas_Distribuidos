package main;

import java.io.IOException;

import server.Server2;

public class server3 {
    public static void main(String [] args){
    	try {
			Server2.iniciar(1610);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}