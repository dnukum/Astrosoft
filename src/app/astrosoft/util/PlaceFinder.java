/**
 * PlaceFinder.java
 * Created On 2006, Mar 9, 2006 1:49:36 PM
 * @author E. Rajasekar
 */

package app.astrosoft.util;

import java.util.ArrayList;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import app.astrosoft.beans.Place;
import java.net.URL;
import java.net.HttpURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import app.astrosoft.beans.Place.Location;
import app.astrosoft.beans.Place.LocationType;


public class PlaceFinder {

	private static final String NAME_NODE = "name";
	private static final String LATITUDE_NODE = "latitude";
	private static final String LOGITUDE_NODE = "longitude";
	private static final String DIR_ATTRIB = "dir";
	
	private static final String XML_SOURCE = "/resources/LatitudeLongitudes.xml";
	private static XPath xpath = XPathFactory.newInstance().newXPath();
	
	
	private static List<Place> searchOpenMeteo(String place) {
		List<Place> list = new ArrayList<Place>();
		try {
			String urlStr = "https://geocoding-api.open-meteo.com/v1/search?name=" + URLEncoder.encode(place, "UTF-8") + "&count=5&language=en&format=json";
			URL url = new URL(urlStr);
			HttpURLConnection con = (HttpURLConnection) url.openConnection();
			con.setRequestMethod("GET");
			con.setConnectTimeout(4000);
			con.setReadTimeout(4000);
			
			if (con.getResponseCode() == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				StringBuilder response = new StringBuilder();
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					response.append(inputLine);
				}
				in.close();
				
				String json = response.toString();
				Matcher m = Pattern.compile("\\{([^\\{\\}]+)\\}").matcher(json);
				while (m.find()) {
					String obj = m.group(1);
					String name = extractJsonValue(obj, "name");
					String latStr = extractJsonValue(obj, "latitude");
					String lonStr = extractJsonValue(obj, "longitude");
					String tz = extractJsonValue(obj, "timezone");
					String country = extractJsonValue(obj, "country");
					String state = extractJsonValue(obj, "admin1");
					
					if (name != null && latStr != null && lonStr != null && tz != null) {
					    if (state == null) state = "";
						double lat = Double.parseDouble(latStr);
						double lon = Double.parseDouble(lonStr);
						
						Location latLoc = new Location(lat, LocationType.Latitude);
						Location lonLoc = new Location(lon, LocationType.Longitude);
						
						list.add(new Place(name, state, country, latLoc, lonLoc, tz));
					}
				}
			}
		} catch (Exception e) {
			System.err.println("Failed to fetch location from Open-Meteo: " + e.getMessage());
		}
		return list;
	}
	
	private static String extractJsonValue(String jsonObj, String key) {
		Matcher mString = Pattern.compile("\"" + key + "\":\"([^\"]+)\"").matcher(jsonObj);
		if (mString.find()) {
			return mString.group(1);
		}
		Matcher mNumber = Pattern.compile("\"" + key + "\":([\\-\\d\\.]+)").matcher(jsonObj);
		if (mNumber.find()) {
			return mNumber.group(1);
		}
		return null;
	}

	public static List<Place> findPlace(String place){

		
		List<Place> placeList = new ArrayList<Place>();
		
		String expression = "//world/country/state/city[name='" + place + "']";
		InputSource inputSource = new InputSource(PlaceFinder.class.getResourceAsStream(XML_SOURCE));
		Node node = null;
		try {
			node = (Node) xpath.evaluate(expression, inputSource, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			e.printStackTrace();
		}
		
		if (node != null){
		
			String city = "";
			String state = "";
			String country = "";
			String latitude = "0:0";
			String longitude = "0:0";
			char longDir = 'n';
			char latDir = 'e';
			String timeZoneId = null;
			
			NodeList cityChilds = node.getChildNodes();
			
			for(int i = 0; i < cityChilds.getLength(); i++){
				Node child = cityChilds.item(i);
				
				if (child.getNodeName().equals(NAME_NODE)){
					city = child.getTextContent();
				}else if (child.getNodeName().equals(LOGITUDE_NODE)){
					longitude = child.getTextContent();
					longDir = child.getAttributes().getNamedItem(DIR_ATTRIB).getNodeValue().charAt(0);
				}if (child.getNodeName().equals(LATITUDE_NODE)){
					latitude = child.getTextContent();
					latDir = child.getAttributes().getNamedItem(DIR_ATTRIB).getNodeValue().charAt(0);
				}
			}
			Node stateNode = node.getParentNode();
			state = stateNode.getChildNodes().item(1).getTextContent(); 
			Node countryNode = stateNode.getParentNode();
			country = countryNode.getChildNodes().item(1).getTextContent();
			timeZoneId = countryNode.getChildNodes().item(3).getTextContent();
			Place p = new Place(city, state, country, latitude, latDir, longitude, longDir, timeZoneId);
			placeList.add(p);
			//placeList.add(Place.getDefault());
			//placeList.add(p);
			//placeList.add(p);
		}
		
		
		if (placeList.isEmpty()) {
		    placeList = searchOpenMeteo(place);
		}
		
		return placeList;

	}
	
	public static void main(String[] args) throws XPathExpressionException {
		
		System.out.println(findPlace("Chennai"));
	}
}
