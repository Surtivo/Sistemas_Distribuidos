package main;

import java.io.IOException;

import dataServer.DataServer4;

//public class dataserver {
//    public static void main(String [] args) throws InterruptedException{
//        DataServer2 dataserver = new DataServer2();
//        try {
//			dataserver.iniciar();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}      
//    }
//}

public class dataserver2 {
    public static void main(String [] args) throws InterruptedException{
        DataServer4 dataserver2 = new DataServer4(5002, false, "data2.txt");
        try {
			dataserver2.iniciar();
		} catch (IOException e) {
			e.printStackTrace();
		}      
    }
}