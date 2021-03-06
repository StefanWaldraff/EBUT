package de.htwg_konstanz.ebus.wholesaler.main.dom;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;

/** Dom Interactor controlls writing to Database. */
public final class DomInteractor {

	/**
	 * get XSD File
	 * */
	private static final File XSD_FILE = new File(
			"C:\\TEMP\\bmecat_new_catalog_1_2_simple_without_NS.xsd");

	org.w3c.dom.Document dom;

	/**
	 * prevents instantiation via reflection
	 */
	private DomInteractor() {
		throw new AssertionError("May not be called!");
	}

	/**
	 * create a Document from File.
	 * 
	 * @param Inputstream
	 *            has uploaded Document
	 * @param errorList
	 *            checking Methods for errors
	 */
	public static Document createDomFromXml(InputStream xmlDocument,
			List<String> errorList) {
		// Parsing of a XML-Document through Java
		DocumentBuilderFactory dbf = setupDocumentBuilderFactory();

		try {
			DocumentBuilder db = dbf.newDocumentBuilder();

			Document document = db.parse(xmlDocument);
			return document;
		} catch (ParserConfigurationException e) {
			errorList.add("Parser configuration is wrong");
		} catch (SAXException e) {
			errorList.add("The document is not well-formed xml");
		} catch (IOException e) {
			errorList.add("Could not read document");
		}
		return null;
	}

	/**
	 * Configurate DocumentBuilderFactory.
	 */
	private static DocumentBuilderFactory setupDocumentBuilderFactory()
			throws FactoryConfigurationError {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		// configure Parsing Process with Factory
		dbf.setValidating(false);
		dbf.setExpandEntityReferences(true);
		// ignore white spaces
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setNamespaceAware(false);
		dbf.setIgnoringComments(true);
		dbf.setIgnoringElementContentWhitespace(true);
		return dbf;
	}

	/**
	 * checks if File has valid and wellformed XML in it.
	 * 
	 * @param Document
	 *            which we created from File
	 * @param errorList
	 */
	public static void validateXml(Document document, List<String> errorList) {
		// Create a SchemaFactory capable of understanding W3C schemas
		SchemaFactory factory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		try {
			Schema schema = factory.newSchema(XSD_FILE);
			// Create a Validator object, which can be used to validate
			// an instance document.
			Validator validator = schema.newValidator();
			// Validate the DOM tree
			validator.validate(new DOMSource(document));
		} catch (SAXException e) {
			errorList.add("The XML file is not valide!");
		} catch (IOException e) {
			errorList.add("Could not read dom");
		}
	}

	/**
	 * checks if File has supplier of the File in Database.
	 * 
	 * @param Document
	 *            which we created from File
	 * @param errorList
	 */
	public static BOSupplier containsValideSupplier(Document dom,
			List<String> errorList) {
		BOSupplier supplier = new DomRequester(dom).getSupplierFromName();
		if (supplier.getCompanyname() == null) {
			errorList.add("Supplier not found in database.");
			return null;
		}

		return supplier;
	}

	/**
	 * calls all DomRequester to write Dom to Database
	 * 
	 * @param Document
	 *            which we created from File
	 * @param errorList
	 */
	public static void writeDomToDb(Document dom, List<String> errorList,
			Map<String, AtomicInteger> updateFeedback) {
		DomRequester domInterpreter = new DomRequester(dom, updateFeedback);
		BOSupplier supplier = domInterpreter.getSupplierFromName();
		domInterpreter.getAllProducts(supplier);
	}

	/**
	 * calls DOMProcessor to create DOMDocument out of Database.
	 * 
	 * @param products
	 *            List of {@BOProduct} BOProducts
	 * @param errorList
	 */
	public static Document createDomFromData(List<BOProduct> products,
			List<String> errorList) {

		Document document = null;

		try {
			document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();

			DomProcessor domProcessor = new DomProcessor(document);
			Element root = document.createElement("BMECAT");

			root.setAttribute("version", "1.2");
			root.setAttribute("xmlns:xsi",
					"http://www.w3.org/2001/XMLSchema-instance");
			domProcessor.addHeader(root);
			domProcessor.addProductCatalog(root, products);

			document.appendChild(root);
		} catch (ParserConfigurationException e) {
			errorList.add("Error configure Parser");
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			errorList.add("Error while configurate Factory");
			e.printStackTrace();
		}

		return document;
	}

	/**
	 * Transform File from Document via Transformer
	 * 
	 * @param Document
	 *            which we created from File
	 * @param fileExtension
	 *            if we do transformation with xhtml or xml
	 * @param errorlist
	 * 
	 */
	public static File createFileFromDom(Document dom, String fileExtension,
			List<String> errorList) {
		TransformerFactory tranFactory = TransformerFactory.newInstance();
		Transformer aTransformer;
		File file = null;

		// create a unique filename
		String fileName = generateUniqueFileName(fileExtension);

		try {
			aTransformer = tranFactory.newTransformer();
			Source src = new DOMSource(dom);
			file = new File(fileName);
			Result dest = new StreamResult(file);
			aTransformer.transform(src, dest);
		} catch (TransformerConfigurationException e) {
			errorList.add("Error while configure Transformer");
		} catch (TransformerException e) {
			errorList.add("Error while transforming DOMSource to File");
		}

		return file;
	}

	private static String generateUniqueFileName(String fileExtension) {
		String fileName = (new BigInteger(130, new SecureRandom()))
				.toString(32) + "." + fileExtension;
		return fileName;
	}

	public static File transformDomToXhtmlFile(Document dom,
			List<String> errorList) {
		// open xslt script
		StreamSource xsltScript = new StreamSource(new File(
				"C:\\TEMP\\Xhtml.xslt"));
		String targetFileName = generateUniqueFileName("html");

		try {

			FileOutputStream target = new FileOutputStream(targetFileName);
			Transformer transformer = TransformerFactory.newInstance()
					.newTransformer(xsltScript);
			// transforms the dom to xhtml file
			transformer.transform(new DOMSource(dom), new StreamResult(target));

			target.flush();
			target.close();
		} catch (TransformerConfigurationException
				| TransformerFactoryConfigurationError e) {
			errorList.add("Could not open xslt script.");
		} catch (TransformerException e) {
			errorList.add("Could not transform to xhtml file");
		} catch (FileNotFoundException e) {
			errorList.add("Could not open target file.");
		} catch (IOException e) {
			errorList.add("Could not close FileStream.");
		}
		return new File(targetFileName);
	}

}