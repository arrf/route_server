package pt.up.fe.Logic;

import java.awt.geom.Line2D;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class Route {

	private String routeXml;
	private ArrayList<AccessPoint> apList = new ArrayList<AccessPoint>();
	private ArrayList<AccessPoint> apListHandoff = new ArrayList<AccessPoint>();

	public Route(String route) {
		routeXml = route;
		apList = decodeRoute(getAPDocument());
		apListHandoff = calculateHandoff();
	}

	//generate a Document to parse the xml file with the apList information
	private Document getAPDocument() {
		DocumentBuilder builder;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(
					routeXml)));
			return doc;
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	//decode the xml to generate a list of access points
	private ArrayList<AccessPoint> decodeRoute(Document doc) {
		NodeList nl1, nl2;
		ArrayList<AccessPoint> list = new ArrayList<AccessPoint>();
		nl1 = doc.getElementsByTagName("access_point");
		if (nl1.getLength() > 0) {
			for (int i = 0; i < nl1.getLength(); i++) {
				Node node1 = nl1.item(i);

				if (node1.getNodeType() == Node.ELEMENT_NODE) {
					Element eElement = (Element) node1;

					String ssid = eElement.getElementsByTagName("ssid").item(0)
							.getTextContent();
					String bssid = eElement.getElementsByTagName("bssid")
							.item(0).getTextContent();
					int channel = Integer.parseInt(eElement
							.getElementsByTagName("channel").item(0)
							.getTextContent());
					double lat_c = Double.parseDouble(eElement
							.getElementsByTagName("lat_center").item(0)
							.getTextContent());
					double lng_c = Double.parseDouble(eElement
							.getElementsByTagName("lng_center").item(0)
							.getTextContent());
					double radius = Double.parseDouble(eElement
							.getElementsByTagName("radius").item(0)
							.getTextContent());
					double lat_s = Double.parseDouble(eElement
							.getElementsByTagName("lat_start").item(0)
							.getTextContent());
					double lng_s = Double.parseDouble(eElement
							.getElementsByTagName("lng_start").item(0)
							.getTextContent());
					double lat_e = Double.parseDouble(eElement
							.getElementsByTagName("lat_end").item(0)
							.getTextContent());
					double lng_e = Double.parseDouble(eElement
							.getElementsByTagName("lng_end").item(0)
							.getTextContent());
					AccessPoint ap = new AccessPoint(ssid, bssid, channel);
					ap.setCenter(new GeoPoint(lat_c, lng_c), radius);
					ap.setStart(new GeoPoint(lat_s, lng_s));
					ap.setEnd(new GeoPoint(lat_e, lng_e));
					list.add(ap);
				}
			}
		}
		return list;
	}

	
	//generate a new list to return to Android app with the points for the handoff
	public ArrayList<AccessPoint> calculateHandoff()
	{
		AccessPoint firstAp = apList.get(0);
		firstAp.setHandoff(firstAp.getStart());
		ArrayList<AccessPoint> apListHandoffs = new ArrayList<AccessPoint>();
		apListHandoffs.add(firstAp);
		
		for(int i=1; i<apList.size(); i++)
		{
			AccessPoint prevAP = apList.get(i-1);
			AccessPoint nextAP = apList.get(i);
			if(needHandoffCalculation(prevAP.getStart(), prevAP.getEnd(), nextAP.getStart())==true)
			{
				GeoPoint handoffPoint = calculateHandoffPoint(prevAP.getCenter(), nextAP.getCenter(), nextAP.getStart(), prevAP.getEnd());
				if(calculateDistance(handoffPoint, nextAP.getCenter()) >= nextAP.getRadius())
					nextAP.setHandoff(nextAP.getStart());
				else if(calculateDistance(handoffPoint, prevAP.getCenter()) >= prevAP.getRadius())
					nextAP.setHandoff(prevAP.getEnd());	
				else
					nextAP.setHandoff(handoffPoint);
			}
			else
			{
				nextAP.setHandoff(nextAP.getStart());
			}
			apListHandoffs.add(nextAP);
		}
		return apListHandoffs;
	}
	
	//calculate the midpoint between the centers of the access points
	private GeoPoint calculateMidpoint(GeoPoint gp1, GeoPoint gp2) //a linear approximation could be used
	{
		double dLon = Math.toRadians(gp2.longitude - gp1.longitude);
		
	    double lat1 = Math.toRadians(gp1.latitude);
	    double lat2 = Math.toRadians(gp2.latitude);
	    double lon1 = Math.toRadians(gp1.longitude);

	    double Bx = Math.cos(lat2) * Math.cos(dLon);
	    double By = Math.cos(lat2) * Math.sin(dLon);
	    double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
	    double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);

	    return new GeoPoint(Math.toDegrees(lat3), Math.toDegrees(lon3));	
	}
	
	//calculate the intersection between the line of g1_end/gp2_start and the perpendicular of gp1_center/gp2_center
	private GeoPoint calculateHandoffPoint(GeoPoint gp1_center, GeoPoint gp2_center, GeoPoint gp2_start, GeoPoint gp1_end)
	{
		/*double m_center = (Math.toRadians(gp2_center.longitude) - Math.toRadians(gp1_center.longitude))/(Math.toRadians(gp2_center.latitude) - Math.toRadians(gp1_center.latitude));
		double m_perpendicular = -1/m_center;
		double m_se = (Math.toRadians(gp1_end.longitude) - Math.toRadians(gp2_start.longitude))/(Math.toRadians(gp1_end.latitude) - Math.toRadians(gp2_start.latitude));
		GeoPoint midPoint = calculateMidpoint(gp1_center, gp2_center);
		
		double x0 = Math.toRadians(gp2_start.latitude);
		double y0 = Math.toRadians(gp2_start.longitude);
		double x1 = Math.toRadians(midPoint.latitude);
		double y1 = Math.toRadians(midPoint.longitude);
		double lat = (-(m_se*x0) + y0 + m_perpendicular*x1 - y1)/(m_perpendicular - m_se);
		double lng = m_perpendicular*lat - m_perpendicular*x1 + y1;
		
		return new GeoPoint(Math.toDegrees(lat), Math.toDegrees(lng));*/
		
		double bearing1 = calculateBearing(gp1_center, gp2_center);
		double bearing2 = calculateBearing(gp2_start, gp1_end);
		double bearing_p = bearing1 + 90;
		GeoPoint midPoint = calculateMidpoint(gp1_center, gp2_center);
		
		double lat1 = Math.toRadians(midPoint.latitude), lon1 = Math.toRadians(midPoint.longitude);
		double  lat2 = Math.toRadians(gp2_start.latitude), lon2 = Math.toRadians(gp2_start.longitude);
		double  brng13 = Math.toRadians(bearing_p), brng23 = Math.toRadians(bearing2);
		double  dLat = lat2-lat1, dLon = lon2-lon1;
		  
		double dist12 = 2*Math.asin( Math.sqrt( Math.sin(dLat/2)*Math.sin(dLat/2) + 
		    Math.cos(lat1)*Math.cos(lat2)*Math.sin(dLon/2)*Math.sin(dLon/2) ) );
		  if (dist12 == 0) return null;
		  
		  // initial/final bearings between points
		 double brngA = Math.acos( ( Math.sin(lat2) - Math.sin(lat1)*Math.cos(dist12) ) / 
		    ( Math.sin(dist12)*Math.cos(lat1) ) );
		 // if (isNaN(brngA)) brngA = 0;  // protect against rounding
		 double brngB = Math.acos( ( Math.sin(lat1) - Math.sin(lat2)*Math.cos(dist12) ) / 
		    ( Math.sin(dist12)*Math.cos(lat2) ) );
		  
		 double brng12, brng21;
		  if (Math.sin(lon2-lon1) > 0) {
		    brng12 = brngA;
		    brng21 = 2*Math.PI - brngB;
		  } else {
		    brng12 = 2*Math.PI - brngA;
		    brng21 = brngB;
		  }
		  
		  double alpha1 = (brng13 - brng12 + Math.PI) % (2*Math.PI) - Math.PI;  // angle 2-1-3
		  double alpha2 = (brng21 - brng23 + Math.PI) % (2*Math.PI) - Math.PI;  // angle 1-2-3
		  
		  //if (Math.sin(alpha1)==0 && Math.sin(alpha2)==0) return null;  // infinite intersections
		  //if (Math.sin(alpha1)*Math.sin(alpha2) < 0) return null;       // ambiguous intersection
		  
		  //alpha1 = Math.abs(alpha1);
		  //alpha2 = Math.abs(alpha2);
		  // ... Ed Williams takes abs of alpha1/alpha2, but seems to break calculation?
		  
		  double alpha3 = Math.acos( -Math.cos(alpha1)*Math.cos(alpha2) + 
		                       Math.sin(alpha1)*Math.sin(alpha2)*Math.cos(dist12) );
		  double dist13 = Math.atan2( Math.sin(dist12)*Math.sin(alpha1)*Math.sin(alpha2), 
		                       Math.cos(alpha2)+Math.cos(alpha1)*Math.cos(alpha3) );
		  double lat3 = Math.asin( Math.sin(lat1)*Math.cos(dist13) + 
		                    Math.cos(lat1)*Math.sin(dist13)*Math.cos(brng13) );
		  double dLon13 = Math.atan2( Math.sin(brng13)*Math.sin(dist13)*Math.cos(lat1), 
		                       Math.cos(dist13)-Math.sin(lat1)*Math.sin(lat3) );
		  double lon3 = lon1+dLon13;
		  lon3 = (lon3+3*Math.PI) % (2*Math.PI) - Math.PI;  // normalise to -180..+180¼
		  
		  return new GeoPoint(Math.toDegrees(lat3), Math.toDegrees(lon3));
		
	}
	
	
	private double calculateBearing(GeoPoint gs, GeoPoint ge)
	{
		 double lat1 = Math.toRadians(gs.latitude), lat2 = Math.toRadians(ge.latitude);
		 double dLon = (Math.toRadians(ge.longitude)-Math.toRadians(gs.longitude));

		 double y = Math.sin(dLon) * Math.cos(lat2);
		 double x = Math.cos(lat1)*Math.sin(lat2) -
		          Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		 double brng = Math.atan2(y, x);
		  
		  return (Math.toDegrees(brng)+360) % 360;
	}
	
	//calculate distances to see if there is need to handoff or not
	private boolean needHandoffCalculation(GeoPoint gp1_start, GeoPoint gp1_end, GeoPoint gp2_start)
	{
		if(calculateDistance(gp1_start, gp1_end) > calculateDistance(gp1_start, gp2_start))
			return true;
		else
			return false;
	}
	
	//distance between two coordinates
	private double calculateDistance(GeoPoint gp1, GeoPoint gp2)
	{
		double R = 6371000; // Radius of the Earth (m)
		double dLat = Math.toRadians(gp2.latitude-gp1.latitude);
		double dLon = Math.toRadians(gp2.longitude-gp1.longitude);
		double lat1 = Math.toRadians(gp1.latitude);
		double lat2 = Math.toRadians(gp2.latitude);

		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		        Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		
		return R * c;
	}
	
	public ArrayList<AccessPoint> getApList(){
		return apList;
	}
	
	
	public ArrayList<AccessPoint> getApListHandoff()
	{
		return apListHandoff;
	}
	
	public String getApListHandoffXml()
	{
		String ans="<aplist>";
		
		for(int i=0; i<apListHandoff.size(); i++)
		{
			ans = ans + "<access_point>";
			ans = ans + "<ssid>" + apListHandoff.get(i).getSSSID() + "</ssid>";
			ans = ans + "<bssid>" + apListHandoff.get(i).getBSSSID() + "</bssid>";
			ans = ans + "<channel>" + apListHandoff.get(i).getChannel() + "</channel>";
			ans = ans + "<lat_center>" + apListHandoff.get(i).getCenter().latitude + "</lat_center>";
			ans = ans + "<lng_center>" + apListHandoff.get(i).getCenter().longitude + "</lng_center>";
			ans = ans + "<radius>" + apListHandoff.get(i).getRadius() + "</radius>";
			ans = ans + "<lat_start>" + apListHandoff.get(i).getStart().latitude + "</lat_start>";
			ans = ans + "<lng_start>" + apListHandoff.get(i).getStart().longitude + "</lng_start>";
			ans = ans + "<lat_end>" + apListHandoff.get(i).getEnd().latitude + "</lat_end>";
			ans = ans + "<lng_end>" + apListHandoff.get(i).getEnd().longitude + "</lng_end>";
			ans = ans + "<lat_handoff>" + apListHandoff.get(i).getHandoff().latitude + "</lat_handoff>";
			ans = ans + "<lng_handoff>" + apListHandoff.get(i).getHandoff().longitude + "</lng_handoff>";
			ans = ans + "</access_point>";
		}
		ans = ans + "</aplist>";
		return ans;
	}
	

}
