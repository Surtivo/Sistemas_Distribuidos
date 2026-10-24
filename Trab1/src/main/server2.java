package main;

import java.io.IOException;
import server.Server2;

public class server2 {
    public static void main(String [] args){
    	try {
			Server2.iniciar(616);
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}