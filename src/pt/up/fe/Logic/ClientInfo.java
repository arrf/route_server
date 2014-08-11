package pt.up.fe.Logic;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;



public class ClientInfo {

	private ArrayList<String> ssids;
	private ArrayList<GeoPoint> route;
	private int maxRadius;
	
	public ClientInfo(ArrayList<String> ssids, ArrayList<GeoPoint> route, int max)
	{
		this.ssids = ssids;
		this.route = route;
		maxRadius = max;
	}
	
	public String getApList()
	{
		String ansXml="";
		BufferedReader br = null;
		FileInputStream file = null;
		
		String path="[";
		String ssidsParam="[";
		
		for(int i=0; i<route.size(); i++)
		{
			path = path + "\"" + route.get(i).longitude + " " + route.get(i).latitude + "\"";
			if(i!=route.size()-1)
				path = path + ", ";
		}
		path = path + "]";
		
		/*path = "[\"-8.596592 41.178026\", \"-8.595267 41.177952\", \"-8.595187 41.177544\"]";*/
		
		for(int i=0; i<ssids.size(); i++)
		{
			ssidsParam = ssidsParam + "\"" + ssids.get(i) + "\"";
			if(i!=ssids.size()-1)
				ssidsParam = ssidsParam + ",";
		}
		ssidsParam = ssidsParam + "]";
		
		System.out.println("PARAM PATH = " + path);
		System.out.println("PARAM SSIDS = " + ssidsParam);
			
		URL url;
		try {
			url = new URL("http://cloud.futurecities.up.pt/~boris/index.php?action=AP/getAPList");
			
			Map<String,Object> params = new LinkedHashMap<String, Object>();
			params.put("params[path]", path);
			params.put("params[returnMode]", 1);
			params.put("params[maxRadius]", maxRadius);
	        params.put("params[sesID]", 1000);
	        params.put("params[clientID]", 1);
	        params.put("params[secToken]", "pas2");
	        params.put("params[ssids]", ssidsParam);

	        StringBuilder postData = new StringBuilder();
	        for (Map.Entry<String,Object> param : params.entrySet()) {
	            if (postData.length() != 0) postData.append('&');
	            postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
	            postData.append('=');
	            postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
	        }
	        byte[] postDataBytes = postData.toString().getBytes("UTF-8");
	        
	        System.out.println("QUery: " + path);

	        HttpURLConnection conn = (HttpURLConnection)url.openConnection();
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
	        conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
	        conn.setDoOutput(true);
	        conn.getOutputStream().write(postDataBytes);

	        /*String response = "";
	        Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
	        for (int c; (c = in.read()) >= 0; ){
	        	System.out.print((char)c);
	        	response = response + String.valueOf(c);
	        }*/
	        
	        BufferedReader in = new BufferedReader(
			        new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			//print result
			System.out.println(response.toString());     
	        return createApListXml(response.toString());  
	        
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
		return "";
		
		

	}
	
	public String createApListXml(String apList)
	{
		String ans="<aplist>";
		
		String apListNew = apList.substring(14); //remove {"getaplist":"
		if(apListNew.contains("null") || apList.length()==16)
			return "error";
		String ap[] = apListNew.split(";");
		System.out.println("NEW:" + apListNew);
		System.out.println(ap.length + "APs");
		for(int i=0; i<ap.length-1; i++)
		{
			String parts[] = ap[i].split(",");
			ans = ans + "<access_point>";
			ans = ans + "<ssid>" + parts[0] + "</ssid>";
			ans = ans + "<bssid>" + parts[1] + "</bssid>";
			ans = ans + "<channel>" + parts[2].substring(0, 4) + "</channel>";
			
			if(Double.parseDouble(parts[3]) < Double.parseDouble(parts[4]))
			{
				ans = ans + "<lng_center>" + parts[3] + "</lng_center>";
				ans = ans + "<lat_center>" + parts[4] + "</lat_center>";
			}
			else
			{
				ans = ans + "<lng_center>" + parts[4] + "</lng_center>";
				ans = ans + "<lat_center>" + parts[3] + "</lat_center>";
			}
			ans = ans + "<radius>" + parts[9] + "</radius>";
			ans = ans + "<lng_start>" + parts[5] + "</lng_start>";
			ans = ans + "<lat_start>" + parts[6] + "</lat_start>";
			ans = ans + "<lng_end>" + parts[7] + "</lng_end>";
			ans = ans + "<lat_end>" + parts[8] + "</lat_end>";
			ans = ans + "</access_point>";
		}
		
		ans = ans + "</aplist>";
		return ans;
	}
}
