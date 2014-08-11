package pt.up.fe.Communication;

import java.net.*;
import java.io.*;


public class ServerThread extends Thread {
    
    private Socket socket = null;

    public ServerThread(Socket socket) {
    	super("ServerThread");
    	this.socket = socket;
    }
 
    
    public void run() {
        try {
            
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
 
            String inputLine, outputLine;
            ServerProtocol sp = new ServerProtocol();                       
            System.out.println("\nFROM: " + socket.getInetAddress().toString());
            while ((inputLine = in.readLine()) != null) 
            {
            	System.out.print("INPUT - " + inputLine);
                outputLine = sp.processInput(inputLine, socket);
                System.out.println("OUTPUT - " + outputLine);
                out.println(outputLine);
                //if (outputLine.equals("bye") )
                    break;
            }
            out.close();
            in.close();
            socket.close();
 
            } 
            catch (IOException e) {
                System.err.println("ServerThread: falha a ler ou escrever no socket: " + e);
               // System.exit(1);
            }
    }
    
}