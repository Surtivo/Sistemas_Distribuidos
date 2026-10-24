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

public class dataserver {
    public static void main(String [] args) throws InterruptedException{
        DataServer4 dataserver = new DataServer4(5001, true, "data1.txt");
        try {
			dataserver.iniciar();
		} catch (IOException e) {
			e.printStackTrace();
		}      
    }
}