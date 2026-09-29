package app.astrosoft.test;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import app.astrosoft.beans.BirthData;
import app.astrosoft.beans.Place;
import app.astrosoft.consts.Sex;
import app.astrosoft.export.XMLHelper;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

public class BirthDataXmlTest {

    @Test
    public void testBirthDataSerialization() throws Exception {
        // Create a place in a different timezone (PST) to ensure local JVM timezone doesn't shift it
        Place originalPlace = new Place("Bellevue", "Washington", "United States", 47.6, -122.2, "America/Los_Angeles");
        
        // Create birth data: July 15 1998, 21:51:49
        BirthData originalBd = new BirthData("Test Person", Sex.Male, 15, 7, 1998, 21, 51, 49, originalPlace);
        
        // 1. Serialize to XML Document
        Document doc = XMLHelper.createDOM();
        Element root = originalBd.toXMLElement(doc);
        doc.appendChild(root);
        
        // Generate XML string to verify string outputs directly
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        String xmlString = writer.getBuffer().toString();
        
        // Verify XML string contains exact unshifted date and time
        Assert.assertTrue("XML should contain exact date string: " + xmlString, xmlString.contains("Jul 15 1998"));
        Assert.assertTrue("XML should contain exact time string: " + xmlString, xmlString.contains("09:51:49 PM"));
        Assert.assertTrue("XML should contain exact timezone: " + xmlString, xmlString.contains("America/Los_Angeles"));
        Assert.assertTrue("XML should contain State: " + xmlString, xmlString.contains("Washington"));
        Assert.assertTrue("XML should contain Country: " + xmlString, xmlString.contains("United States"));
        
        // 2. Deserialize from XML Document
        BirthData parsedBd = BirthData.valueOfXMLNode(root);
        
        // 3. Verify all fields match exactly
        Assert.assertEquals("Name should match", "Test Person", parsedBd.name());
        Assert.assertEquals("Sex should match", Sex.Male, parsedBd.sex());
        
        // Verify Date components
        Assert.assertEquals("Year should match", 1998, parsedBd.year());
        Assert.assertEquals("Month should match", 7, parsedBd.month());
        Assert.assertEquals("Date should match", 15, parsedBd.date());
        
        // Verify Time components
        Assert.assertEquals("Hour should match", 21, parsedBd.hour());
        Assert.assertEquals("Minutes should match", 51, parsedBd.minutes());
        
        // Verify Place components
        Place parsedPlace = parsedBd.getBirthPlace();
        Assert.assertEquals("City should match", "Bellevue", parsedPlace.city());
        Assert.assertEquals("State should match", "Washington", parsedPlace.state());
        Assert.assertEquals("Country should match", "United States", parsedPlace.country());
        Assert.assertEquals("Latitude should match", 47.6, parsedPlace.latitude(), 0.001);
        Assert.assertEquals("Longitude should match", -122.2, parsedPlace.longitude(), 0.001);
        Assert.assertEquals("Timezone ID should match", "America/Los_Angeles", parsedPlace.astrosoftTimeZone().id());
    }
}
