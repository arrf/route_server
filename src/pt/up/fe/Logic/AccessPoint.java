package pt.up.fe.Logic;

public class AccessPoint {

	private String ssid;
	private String bssid;
	private int channel;
	private double latitude_center;
	private double longitude_center;
	private double latitude_start;
	private double longitude_start;
	private double latitude_end;
	private double longitude_end;
	private double radius;
	
	private double latitude_handoff;
	private double longitude_handoff;

	public AccessPoint(String ssid, String bssid, int channel) {
		this.ssid = ssid;
		this.bssid = bssid;
		this.channel = channel;
	}
	
	public void setCenter(GeoPoint p, double rad)
	{
		this.latitude_center = p.latitude;
		this.longitude_center = p.longitude;
		this.radius = rad;
	}
	
	public void setStart(GeoPoint p)
	{
		this.latitude_start = p.latitude;
		this.longitude_start = p.longitude;
	}
	
	public void setEnd(GeoPoint p)
	{
		this.latitude_end = p.latitude;
		this.longitude_end = p.longitude;
	}
	
	public void setHandoff(GeoPoint p)
	{
		this.latitude_handoff = p.latitude;
		this.longitude_handoff = p.longitude;
	}

	public String getSSSID() {
		return ssid;
	}

	public String getBSSSID() {
		return bssid;
	}

	public int getChannel() {
		return channel;
	}

	public GeoPoint getCenter() {
		return new GeoPoint(latitude_center, longitude_center);
	}
	
	public GeoPoint getStart() {
		return new GeoPoint(latitude_start, longitude_start);
	}
	
	public GeoPoint getEnd() {
		return new GeoPoint(latitude_end, longitude_end);
	}

	public double getRadius() {
		return radius;
	}
	
	public GeoPoint getHandoff(){
		return new GeoPoint(latitude_handoff, longitude_handoff);
	}
	
	/*public double getLatitudeStart()
	{
		return latitude_start;
	}
	
	public double getLongitudeStart()
	{
		return longitude_start;
	}*/
}
