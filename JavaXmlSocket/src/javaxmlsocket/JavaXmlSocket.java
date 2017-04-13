package javaxmlsocket;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Jean-Pierre Erasmus
 */
public class JavaXmlSocket {

    /**
     *
     * @param args
     */
    public static void main(String[] args) {
        //Get Socket connection string
        String xmlSocketAuthString = buildXMLAuthString("Authentication",
                "12345",
                "12345",
                "ABCDE",
                "ABCDE",
                "Users",
                false);

        String xmlSocketAuthStringHuman = buildXMLAuthString("Authentication",
                "12345",
                "12345",
                "ABCDE",
                "ABCDE",
                "Users",
                true);
        //Print String 
        System.out.println("Socket XML String :\n" + xmlSocketAuthString);
        System.out.println("Socket XML String Formatted :\n" + xmlSocketAuthStringHuman);

        //Create socket connection and print request response
        try (
                Socket echoSocket = new Socket("196.37.22.179", 9011);
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));) {

            //Send request
            out.println(xmlSocketAuthString);
            
            //Get Response            
            String resultString = in.readLine();
            
            //Display respone
            System.out.println("Result String: " + resultString);
            System.out.println(outputXMLFormatter(resultString, null, true));
            
            //Write response
            writeReadme(outputXMLFormatter(resultString, null, true));
        } catch (Exception e) {
            System.err.println("Exception " + e.getMessage());
            System.exit(1);

        }

    }

    /**
     *
     * @param responseToWrite
     */
    private static void writeReadme(String responseToWrite) {         
        
        //Create new Readme file with response
        try (FileWriter filewriter = new FileWriter("README.txt",false); 
             BufferedWriter buffwriter = new BufferedWriter(filewriter); ) {
            
            //Output Response String to output
            buffwriter.write(responseToWrite);
                        
        } catch (IOException e) {
            System.out.println("Readme.txt Write Exception " + e.getMessage());            
        }        
    }

    /**
     *
     * @return
     */
    private static String buildXMLAuthString(String eventType,
            String UserPin,
            String deviceID,
            String deviceSerial,
            String version,
            String transactionType,
            boolean indentOutput) {
        String xmlOutput = "";

        DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder xmlBuilder;

        try {
            //Create XML Document
            xmlBuilder = xmlFactory.newDocumentBuilder();
            Document xmlDoc = xmlBuilder.newDocument();

            //Create request node
            Element mainRootElement = xmlDoc.createElement("request");
            xmlDoc.appendChild(mainRootElement);
            //Create event type node
            Element eventTypeElement = xmlDoc.createElement("EventType");
            eventTypeElement.appendChild(xmlDoc.createTextNode(eventType));
            mainRootElement.appendChild(eventTypeElement);

            //Create event Node
            Element eventNode = xmlDoc.createElement("event");

            //Event Detail nodes
            //User Pin
            Element userPinNode = xmlDoc.createElement("UserPin");
            userPinNode.appendChild(xmlDoc.createTextNode(UserPin));
            eventNode.appendChild(userPinNode);
            //Device ID
            Element deviceIDNode = xmlDoc.createElement("DeviceId");
            deviceIDNode.appendChild(xmlDoc.createTextNode(deviceID));
            eventNode.appendChild(deviceIDNode);
            //Device Serial
            Element deviceSerialNode = xmlDoc.createElement("DeviceSer");
            deviceSerialNode.appendChild(xmlDoc.createTextNode(deviceSerial));
            eventNode.appendChild(deviceSerialNode);
            //Device Version
            Element deviceVersionNode = xmlDoc.createElement("DeviceVer");
            deviceVersionNode.appendChild(xmlDoc.createTextNode(version));
            eventNode.appendChild(deviceVersionNode);
            //Transaction Type
            Element tranTypeNode = xmlDoc.createElement("TransType");
            tranTypeNode.appendChild(xmlDoc.createTextNode(transactionType));
            eventNode.appendChild(tranTypeNode);

            //Add event to request
            mainRootElement.appendChild(eventNode);

            // output XML to String 
            return outputXMLFormatter(null, xmlDoc, indentOutput);

        } catch (ParserConfigurationException | DOMException | SAXException | IOException | TransformerException e) {
            System.out.println("buildXMLAuthString() Exception : 1001 : " + e.getMessage());
        }

        return xmlOutput;
    }

    /**
     *
     * @param xmlString
     * @param xmlInDocument
     * @param indentXML
     * @return
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    private static String outputXMLFormatter(String xmlString,
            Document xmlInDocument,
            boolean indentXML) throws SAXException, IOException, ParserConfigurationException, TransformerConfigurationException, TransformerException {
        String formattedXMLOutput = "";

        //Cater for Strings or Documents
        if (xmlString != null) {
            //Create XML Document from string
            DocumentBuilderFactory docfactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuiler = docfactory.newDocumentBuilder();
            InputSource inSource = new InputSource(new StringReader(xmlString));
            xmlInDocument = docBuiler.parse(inSource);
        } else if (xmlInDocument != null) {
            //Process the input document
        } else {
            //No XML to formated
            return "";
        }

        //Use Transformer to format the XML Document. 
        //output XML to String 
        Transformer transformer = TransformerFactory.newInstance().newTransformer();

        //Set indentation
        if (indentXML) { //dependant on human readability requirements
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        } else {
            transformer.setOutputProperty(OutputKeys.INDENT, "no");
        }

        //Create formatted String as output
        StreamResult result = new StreamResult(new StringWriter());
        DOMSource source = new DOMSource(xmlInDocument);
        transformer.transform(source, result);

        //Return formatted XML String 
        return result.getWriter().toString();

    }

}
