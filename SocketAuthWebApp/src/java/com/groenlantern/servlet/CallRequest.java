package com.groenlantern.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.Socket;
import java.util.Enumeration;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
@WebServlet("/CallRequestServlet")
public class CallRequest extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");

        //Get Parameter Values for request
        String serverIP = "";
        String port = "";
        String eventType = "";
        String UserPin = "";
        String deviceID = "";
        String deviceSerial = "";
        String version = "";
        String transactionType = "";

        //process request parameters
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();

            //Skip any null values parameters
            if (paramName == null) {
                continue;
            }
            if (request.getParameterValues(paramName) == null
                    || request.getParameterValues(paramName).length < 1) {
                continue;
            }

            //Save parameter value to matching variable
            if (paramName.trim().equals("serverName")) {
                serverIP = request.getParameterValues(paramName)[0];
            }
            if (paramName.trim().equals("portNo")) {
                port = request.getParameterValues(paramName)[0];
            }
            if (paramName.trim().equals("eventType")) {
                eventType = request.getParameterValues(paramName)[0];
            }
            if (paramName.trim().equals("userPin")) {
                UserPin = request.getParameterValues(paramName)[0];
            }
            if (paramName.trim().equals("deviceID")) {
                deviceID = request.getParameterValues(paramName)[0];
            }
            if (paramName.trim().equals("deviceSerial")) {
                deviceSerial = request.getParameterValues(paramName)[0];
            }
            if (paramName.trim().equals("deviceVersion")) {
                version = request.getParameterValues(paramName)[0];
            }
            if (paramName.trim().equals("tranType")) {
                transactionType = request.getParameterValues(paramName)[0];
            }
        }

        //Print auth request parameters
        //String for socket request
        String xmlSocketAuthString = buildXMLAuthString(eventType, UserPin,
                deviceID, deviceSerial,
                version, transactionType, false);

        //String to print out to console
        String xmlSocketAuthStringHuman = buildXMLAuthString(eventType, UserPin,
                deviceID, deviceSerial,
                version, transactionType, true);

        //Print XML request
        System.out.println("Socket Server : " + serverIP + ":" + port);
        System.out.println("Socket XML String Formatted :\n" + xmlSocketAuthStringHuman);

        //Process request
        String responseMessage = getRequestResponse(serverIP, Integer.parseInt(port), xmlSocketAuthString);

        //Response Display
        //System.out.println("Socket Response : " + responseMessage);
        String formattedResponse = responseMessage;
        try {
            formattedResponse = outputXMLFormatter(responseMessage, null, true);
            System.out.println("Socket Response Formatted : \n" + formattedResponse);
        } catch (Exception ex) {
            formattedResponse = responseMessage;
            System.out.println("Formatting Exception 3003 : " + responseMessage + " : " + ex.getMessage());
        }

        //Send response back to requesting page
        try (PrintWriter out = response.getWriter()) {
            out.println(formattedResponse);
        }
    }

    /**
     * Send server request and return response
     *
     * @param ipAddress
     * @param portNo
     * @param authRequestXML
     * @return
     */
    private static String getRequestResponse(String ipAddress, int portNo, String authRequestXML) {

        try (
                //Connect to server socket    
                Socket echoSocket = new Socket(ipAddress, portNo);
                //setup socket writer    
                PrintWriter out = new PrintWriter(echoSocket.getOutputStream(), true);
                BufferedReader in = new BufferedReader(new InputStreamReader(echoSocket.getInputStream()));) {

            //Send request
            out.println(authRequestXML);

            //Get result message from server and return 
            String resultString = in.readLine();

            return resultString;
        } catch (Exception e) {
            System.err.println("Server Socket Exception 4002 : " + e.getMessage());
            return "Server Connection Error";
        }

    }

    /**
     * Build String for Auth Request
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
     * Format XML String for output
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

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }

}
