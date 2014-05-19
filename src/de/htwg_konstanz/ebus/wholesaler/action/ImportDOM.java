package de.htwg_konstanz.ebus.wholesaler.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import java.lang.Object;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;

public class ImportDOM {
	
	org.w3c.dom.Document dom;
	
	public ImportDOM(InputStream xml){
		try {
		//Parsing of a XML-Document through Java
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// configure Parsing Process with Factory 
		dbf.setValidating(true);
		dbf.setExpandEntityReferences(true);
		//ignore Whitespaces
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setNamespaceAware(false);
		//dbf.setSchema(null);
		dbf.setIgnoringComments(true);
		//dbf.setXIncludeAware(false);
		
		// Create a SchemaFactory capable of understanding W3C schemas
		SchemaFactory factory = SchemaFactory.
		newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = factory.newSchema(new File("mySchema.xsd"));
		// Create a Validator object, which can be used to validate
		// an instance document.
		Validator validator = schema.newValidator();
		
		
		DocumentBuilder db = dbf.newDocumentBuilder();
		//db.isIgnoringElementContentWhitespace();
		//TODO !!!!!db.setErrorHandler(org.xml.sax.helpers.DefaultHandler);
		db.setEntityResolver(null);
		org.w3c.dom.Document document = db.parse(xml);
		// Validate the DOM tree
		validator.validate(new DOMSource(document));
			this.dom=document;
	
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}
	
	
	//Validieren wsdl validiert
	//DOM Element Childs ... 
	//Datenbank verknüpfung
	
	public void writeToDatabase(){
		
	
		
		
	}
	
	
	public static void doSomething(Node node) {
	    // do something with the current node instead of System.out
	    System.out.println(node.getNodeName());

	    NodeList nodeList = node.getChildNodes();
	    for (int i = 0; i < nodeList.getLength(); i++) {
	        Node currentNode = nodeList.item(i);
	        if (currentNode.getNodeType() == Node.ELEMENT_NODE) {
	            //calls this method for all the children which is Element
	            doSomething(currentNode);
	        }
	    }
	
	
	
	
	}
	
}