package pt.up.fe.Communication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;


public class AppMain {

    public static void main(String[] args)
    {
        System.out.println("Server running...");
		
        Server sv = new Server();
        sv.start();      
    }
       
}
