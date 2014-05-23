package de.htwg_konstanz.ebus.wholesaler.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

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

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.SupplierBOA;

public class ImportDOM {

	org.w3c.dom.Document dom;

	public ImportDOM(InputStream xml) {
		try {
			// Parsing of a XML-Document through Java
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			// configure Parsing Process with Factory
			dbf.setValidating(true);
			dbf.setExpandEntityReferences(true);
			// ignore Whitespaces
			dbf.setIgnoringElementContentWhitespace(true);
			dbf.setNamespaceAware(false);
			dbf.setIgnoringComments(true);

			DocumentBuilder db = dbf.newDocumentBuilder();
			dbf.setIgnoringElementContentWhitespace(true);

			org.w3c.dom.Document document = db.parse(xml);

			if (this.validate(document)) {
				if (getSupplier(document) != null) {
					writeSupplierNameToDatabase(getSupplier(document));

				} else {
					System.out.println("Supplier not found in Database");
				}
			} else {
				System.out.println("Document not valide");
			}

			this.dom = document;

		} catch (ParserConfigurationException e) {
			System.out.println("ParserConfigurationException");
			e.printStackTrace();
		} catch (SAXException e) {
			System.out.println("SaxException");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
		}
	}

	// Validieren wsdl validiert
	// DOM Element Childs ...
	// Datenbank verkn�pfung

	public boolean validate(org.w3c.dom.Document document) {

		// Create a SchemaFactory capable of understanding W3C schemas

		System.out.println("TEST");
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try {
			schema = factory.newSchema(new File(
					"C:\\Temp\\bmecat_new_catalog_1_2_simple_without_NS.xsd"));

			// Create a Validator object, which can be used to validate
			// an instance document.
			Validator validator = schema.newValidator();
			// Validate the DOM tree
			validator.validate(new DOMSource(document));
			System.out.println("TRUE!");
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}

	// checken ob optional oder nicht bei xml
	public BOSupplier getSupplier(org.w3c.dom.Document document) {

		// test if Supplier is in Database
		NodeList supplier = document.getElementsByTagName("SUPPLIER_NAME");
		System.out.println(supplier.item(0).hasChildNodes());
		String companyname;
		BOSupplier endsupplier = null;

		if (supplier.getLength() >= 1) {

			companyname = supplier.item(0).getFirstChild().getNodeValue();

		}

		List<BOSupplier> suppliers = SupplierBOA.getInstance().findAll();
		for (BOSupplier supp : suppliers) {

			if (endsupplier.getCompanyname().equals(suppliers)) {

				endsupplier = supp;
			}
		}
		return endsupplier;
	}

	public void writeSupplierNameToDatabase(BOSupplier sup) {

		SupplierBOA supboa = SupplierBOA.getInstance();
		supboa.saveOrUpdate(sup);

	}

	public void writeArticleDetailsToDatabase(org.w3c.dom.Document document) {
		ProductBOA pboa = ProductBOA.getInstance();

		NodeList articles = document.getElementsByTagName("ARTICLE");

		for (int i = 0; i <= articles.getLength(); i++) {

			NodeList children = articles.item(i).getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node node = children.item(j);
				if (node.getNodeName().equals("SUPPLIER_AID")) {

				}
				if (node.getNodeName().equals("ARTICLE_DETAILS")) {

				}
			}
		}
	}

	public void getArticleDetails(org.w3c.dom.Document document) {

	}

	public void uploadFile() {

	}

}