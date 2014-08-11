package pt.up.fe.Communication;

import java.net.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends Thread{
    
    static private boolean shouldIBeRunning = false;
    static private ServerThread serverThread;
    private static final int SERVER_PORT = 6001;

    @Override
    public void run(){
        
        shouldIBeRunning = true;
        
        ServerSocket serverSocket = null;
        boolean listening = true;
                             
        try {
            serverSocket = new ServerSocket(SERVER_PORT);
            System.out.println("IP: " + serverSocket.getInetAddress().toString() + " PORT: " + SERVER_PORT);
        } 
        catch (IOException e) {
            System.err.println("Server: não foi possível escutar a porta" + SERVER_PORT + ":" + e);
            System.exit(-1);
        }
        System.out.println("Waiting for clients...");
        while (listening && shouldIBeRunning) {
            try {
            serverThread = new ServerThread(serverSocket.accept());
            serverThread.start();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
        try {
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println("*** Server actually shutting down!");
    }
    
    public void terminateServer()
    {
        shouldIBeRunning = false;
    }
}