/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pt.up.fe.Communication;

import java.net.*;
import java.util.*;

import org.w3c.dom.Document;

import pt.up.fe.Logic.ClientInfo;
import pt.up.fe.Logic.GMapV2Direction;
import pt.up.fe.Logic.GeoPoint;
import pt.up.fe.Logic.Route;

public class ServerProtocol{
	
    public String processInput(String s, Socket cs)
    {
        String[] res = s.split("#");
        String ret = "";
        GMapV2Direction gm = new GMapV2Direction();
        ArrayList<String> ssids = new ArrayList<String>();;
        ArrayList<GeoPoint> route = null;
        int maxRadius;
        
        if(res[0].equals("0"))
        {
            Document doc = gm.getDocument(Double.parseDouble(res[1]), Double.parseDouble(res[2]), Double.parseDouble(res[3]), Double.parseDouble(res[4]), res[5]);
            route = gm.getDirection(doc);
            maxRadius = Integer.parseInt(res[6]);
            for(int i=7; i<res.length; i++)
        		ssids.add(res[i]);
            ClientInfo ci = new ClientInfo(ssids, route, maxRadius);
            String apList = ci.getApList();
            if(apList.equals("error"))
            	ret = "1#0";
            else
            {
            	Route r = new Route(ci.getApList());
            	ret = "1#" + r.getApListHandoffXml();
            }
        }
        else if (res[0].equals("1"))
        {
        	Document doc = gm.getDocument(Double.parseDouble(res[1]), Double.parseDouble(res[2]), res[3], res[4]);
        	route = gm.getDirection(doc);
        	maxRadius = Integer.parseInt(res[5]);
        	for(int i=6; i<res.length; i++)
        		ssids.add(res[i]);
        	ClientInfo ci = new ClientInfo(ssids, route, maxRadius);
        	String apList = ci.getApList();
            if(apList.equals("error"))
            	ret = "1#0";
            else
            {
            	Route r = new Route(ci.getApList());
            	ret = "1#" + r.getApListHandoffXml();
            }
        }
        else if(res[0].equals("2"))
        {
            Document doc = gm.getDocument(Double.parseDouble(res[1]), Double.parseDouble(res[2]), Double.parseDouble(res[3]), Double.parseDouble(res[4]), res[5]);
            route = gm.getDirection(doc);        
            ret = "2";
            if(route.size()==0)
                ret = "2#0";
            else
            {
                for(int i=0; i<route.size(); i++)
            	   ret = ret + "#" + route.get(i).latitude + "#" + route.get(i).longitude;
            }
        }
        else if (res[0].equals("3"))
        {
        	Document doc = gm.getDocument(Double.parseDouble(res[1]), Double.parseDouble(res[2]), res[3], res[4]);
        	route = gm.getDirection(doc);
        	ret = "2";
            if(route.size()==0)
                ret = "2#0";
            else
            {
                for(int i=0; i<route.size(); i++)
                    ret = ret + "#" + route.get(i).latitude + "#" + route.get(i).longitude;
            } 
        }
        return ret;
    }


    
}
