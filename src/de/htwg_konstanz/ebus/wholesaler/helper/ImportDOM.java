package de.htwg_konstanz.ebus.wholesaler.helper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOCountry;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOPurchasePrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.PriceBOA;
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
					writeArticleDetailsToDatabase(document);

				} else {
					System.out.println("Supplier not found in Database");
				}
			} else {
				System.out.println("Document not valid");
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

	public boolean validate(org.w3c.dom.Document document) {

		// Create a SchemaFactory capable of understanding W3C schemas

		System.out.println("TEST");
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema;
		try {
			schema = factory.newSchema(new File(
					"C:\\TEMP\\bmecat_new_catalog_1_2_simple_without_NS.xsd"));

			// Create a Validator object, which can be used to validate
			// an instance document.
			Validator validator = schema.newValidator();
			// Validate the DOM tree
			validator.validate(new DOMSource(document));
			System.out.println("TRUE!");
			return true;
		} catch (IOException e) {
			System.out.println("IOException in validate");
			e.printStackTrace();
			return false;
		} catch (SAXException e) {
			System.out.println("SAXException in validate");
			e.printStackTrace();
			return false;
		}

	}

	// checken ob optional oder nicht bei xml
	public BOSupplier getSupplier(org.w3c.dom.Document document) {

		// test if Supplier is in Database
		NodeList supplier = document.getElementsByTagName("SUPPLIER_NAME");
		System.out.println(supplier.item(0).hasChildNodes());
		String companyname = null;
		BOSupplier endsupplier = new BOSupplier();

		if (supplier.getLength() >= 1) {

			companyname = supplier.item(0).getFirstChild().getNodeValue();
			endsupplier.setCompanyname(companyname);
		}

		List<BOSupplier> suppliers = SupplierBOA.getInstance().findAll();
		for (BOSupplier supp : suppliers) {

			if (endsupplier.getCompanyname().equals(suppliers)) {

				endsupplier = supp;
				System.out.println("supplier" + endsupplier);
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
		BOProduct product = new BOProduct();

		NodeList articles = document.getElementsByTagName("ARTICLE");

		for (int i = 0; i <= articles.getLength(); i++) {

			NodeList children = articles.item(i).getChildNodes();
			for (int j = 0; j < children.getLength(); j++) {
				Node node = children.item(j);
				if (node.getNodeName().equals("SUPPLIER_AID")) {
					String sAID = node.getFirstChild().getNodeValue();
					System.out.println("foundSAID:"
							+ product.getSupplier().getSupplierNumber()
									.equals(sAID));
					// TODO Write Supplier AID to Database
				}
				if (node.getNodeName().equals("ARTICLE_DETAILS")) {
					String ean = node.getFirstChild().getNodeValue();
					// String descrshort=node.;
					String descrlong = node.getLastChild().getNodeValue();
					System.out.println(ean);
					System.out.println(descrlong);
					// product.setLongDescription(descrlong);
					// product.setShortDescription(shortDescription)
				}
				if (node.getNodeName().equals("ARTICLE_ORDER_DETAILS")) {
					String orderunit = node.getFirstChild().getNodeValue();
					System.out.println("orderunit");
					// ORDER_UNIT --> BOOderItemPurchase.setOrderUnit
					// CONTENT_UNIT -->
					// BOOderItemPurchase.setProductDescription??
					// NO_CU_PER_OU --> BOOderItemPurchase.setUnitPrice

				}
				if (node.getNodeName().equals("ARTICLE_PRICE_DETAILS")) {

					getArticlePriceDetails(node);

				}

			}
			pboa.saveOrUpdate(product);
		}
	}

	public void getArticlePriceDetails(Node node) {

		Node articleprice = node.getFirstChild();
		NamedNodeMap attrpricelists = articleprice.getAttributes();
		Node attrpricelist = attrpricelists.item(0);
		String typeOfPrice = attrpricelist.getFirstChild().getNodeValue();
		String amount = null;
		String currency = null;
		String taxrate = null;
		String country = null;

		for (int a = 0; a < articleprice.getChildNodes().getLength(); a++) {

			Node subnode = articleprice.getChildNodes().item(a);
			if (subnode.getNodeName().equals("PRICE_AMOUNT")) {
				amount = subnode.getFirstChild().getNodeValue();
			}
			if (subnode.getNodeName().equals("PRICE_CURRENCY")) {
				currency = subnode.getFirstChild().getNodeValue();
			}
			if (subnode.getNodeName().equals("TAX")) {
				taxrate = subnode.getFirstChild().getNodeValue();
			}
			// TODO mehr als ein Territory ??
			if (subnode.getNodeName().equals("TERRITORY")) {
				country = subnode.getFirstChild().getNodeValue();
			}
			// convert String to BigDecimal to make it possible to create a
			// BOPurchasePrice with the parameters amount and tax
			BigDecimal tax = new BigDecimal(taxrate);
			BigDecimal priceamount = new BigDecimal(amount);

			// Create an countryobject to set a country for the price
			BOCountry countryobj = new BOCountry();
			countryobj.setIsocode(country);

			// create an Object BOPurchaseprice
			BOPurchasePrice price = new BOPurchasePrice(priceamount, tax,
					typeOfPrice);

			// set a Country for the price
			price.setCountry(countryobj);

			// TODO set a Product for the Price

			// set a Currency for the Price

			// writes PricetoDatabase
			PriceBOA priceboa = PriceBOA.getInstance();
			priceboa.saveOrUpdatePurchasePrice(price);

		}
	}

	public void uploadFile() {

	}

}