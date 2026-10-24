package main;

import java.io.IOException;

import server.Server2;


public class server1 {
    public static void main(String [] args){
    	try {
			Server2.iniciar(666);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}