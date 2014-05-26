//package de.htwg_konstanz.ebus.wholesaler.main;
//
//import java.io.File;
//import java.io.IOException;
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import javax.xml.parsers.DocumentBuilder;
//import javax.xml.parsers.DocumentBuilderFactory;
//import javax.xml.parsers.ParserConfigurationException;
//
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
//import org.w3c.dom.NodeList;
//import org.xml.sax.SAXException;
//
//import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOCountry;
//import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
//import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOPurchasePrice;
//import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSalesPrice;
//import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;
//import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.PriceBOA;
//import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
//import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.SupplierBOA;
//import de.htwg_konstanz.ebus.framework.wholesaler.api.boa._BaseBOA;
//import de.htwg_konstanz.ebus.framework.wholesaler.vo.Country;
//
//public class DomRequesterNew {
//	/**
//	 * Constructor
//	 */
//	public DomRequesterNew() {
//		super();
//	}
//
//	/**
//	 * saves a document to the database
//	 * 
//	 * @param dom
//	 *            the document which will be saved
//	 * @return map, containing error messages, new and updated product lists
//	 */
//	public Map<String, List<String>> insertIntoDatabase(File xmlfile) {
//		Document dom = null;
//		try {
//			// Document Builder Factory initialization
//			DocumentBuilderFactory factory = DocumentBuilderFactory
//					.newInstance();
//			System.out.println("Factory successful created");
//			// Create Document Builder
//			DocumentBuilder builder = factory.newDocumentBuilder();
//			System.out.println("Document Builder created");
//			// Start Parsing
//			dom = builder.parse(xmlfile);
//			System.out.println("Dom successfully parsed");
//			System.out.println("XML File exists? answer: " + xmlfile.exists());
//		} catch (SAXException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ParserConfigurationException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		// map with updated and new products
//		// start initialize Result Map
//		HashMap<String, List<String>> resultMap = initResultMap();
//		System.out.println("Result Map created");
//		// lists for the output
//		ArrayList<String> updatedProducts = new ArrayList<String>();
//		ArrayList<String> newProducts = new ArrayList<String>();
//		// get dom root element
//		Element root = dom.getDocumentElement();
//		System.out.println("Root: " + root.getNodeValue());
//
//		// Supplier
//		// get supplier from dom root
//		BOSupplier supplier = getSupplier(root);
//
//		if (supplier == null) {
//			resultMap.get(Errors.ERRORS).add("No Valid Supplier found!");
//			return resultMap;
//		}
//
//		// TODO bis hier hin besser unseres
//
//		// routine to find all articles
//		NodeList articles = root.getElementsByTagName("ARTICLE");
//		for (int i = 0; i < articles.getLength(); i++) {
//			Element article = (Element) articles.item(i);
//			// get the supported elements by tag name
//			BOProduct product = new BOProduct();
//
//			NodeList shortDescrList = article
//					.getElementsByTagName("DESCRIPTION_SHORT");
//			product.setShortDescription(shortDescrList.item(0).getFirstChild()
//					.getNodeValue());
//			if (article.getElementsByTagName("DESCRIPTION_LONG").getLength() > 0) {
//				NodeList longDescrList = article
//						.getElementsByTagName("DESCRIPTION_LONG");
//				product.setLongDescription(longDescrList.item(0)
//						.getFirstChild().getNodeValue());
//			}
//			if (article.getElementsByTagName("MANUFACTURER_NAME").getLength() > 0) {
//				NodeList mname = article
//						.getElementsByTagName("MANUFACTURER_NAME");
//				product.setManufacturer(mname.item(0).getFirstChild()
//						.getNodeValue());
//			}
//
//			// TODO haben schon get SupplierAID
//			NodeList aidList = article.getElementsByTagName("SUPPLIER_AID");
//			product.setOrderNumberCustomer(aidList.item(0).getFirstChild()
//					.getNodeValue());
//			product.setSupplier(supplier);
//			product.setOrderNumberSupplier(aidList.item(0).getFirstChild()
//					.getNodeValue());
//
//			ProductBOA pb = ProductBOA.getInstance();
//			// find all products
//			List<BOProduct> listeBOProduct = pb.findAll();
//
//			// TODO unseres besser
//			boolean neu = true;
//			if (listeBOProduct != null) {
//				for (BOProduct iterator : listeBOProduct) {
//					if (iterator.getOrderNumberCustomer().equals(
//							product.getOrderNumberCustomer())) {
//						pb.delete(iterator);
//						neu = false;
//					}
//				}
//			}
//			// if there are new products
//			if (neu) {
//				// save it to db
//				pb.saveOrUpdate(product);
//				// calculate the sales price with 100% gain
//				insertPrices(article, product);
//				// add short description
//				newProducts.add(product.getShortDescription());
//			} else {
//				// if the product still exists --> update the product on db and
//				// calculate the sales price
//				ProductBOA.getInstance().delete(product);
//				_BaseBOA.getInstance().commit();
//				pb.saveOrUpdate(product);
//				insertPrices(article, product);
//				updatedProducts.add(product.getShortDescription());
//			}
//		}
//		resultMap.put(Errors.NEW_PRODUCTS, newProducts);
//		resultMap.put(Errors.UPDATED_PRODUCTS, updatedProducts);
//		// return the resulat map with errors, new products and updated products
//		return resultMap;
//	}
//
//	/**
//	 * searches the specified supplier out of the database
//	 * 
//	 * @param root
//	 *            node of the document
//	 * @return supplier, or null if none was found
//	 */
//	private BOSupplier getSupplier(final Element root) {
//		BOSupplier supplier = null;
//		System.out.println("NodeList Suppliers");
//		NodeList suppliers = root.getElementsByTagName("SUPPLIER_NAME");
//
//		System.out.println("NodeList: " + suppliers.item(0));
//		Element supplierElement = (Element) suppliers.item(0);
//		String supplierName = supplierElement.getFirstChild().getNodeValue();
//		System.out.println("Supplier Name: " + supplierName);
//		SupplierBOA sboa = SupplierBOA.getInstance();
//
//		List<BOSupplier> listeBOSupplier = sboa.findByCompanyName(supplierName);
//		if (!listeBOSupplier.isEmpty()) {
//			supplier = listeBOSupplier.get(0);
//		}
//		return supplier;
//	}
//
//	/**
//	 * initializes the resulting map
//	 * 
//	 * @return initialized map
//	 */
//	private HashMap<String, List<String>> initResultMap() {
//		HashMap<String, List<String>> result = new HashMap<String, List<String>>();
//		result.put(Errors.ERRORS, new ArrayList<String>());
//		result.put(Errors.NOT_VALID, new ArrayList<String>());
//		result.put(Errors.NOT_WELLFORMED, new ArrayList<String>());
//		result.put(Errors.UPDATED_PRODUCTS, new ArrayList<String>());
//		result.put(Errors.NEW_PRODUCTS, new ArrayList<String>());
//		return result;
//	}
//
//	private void insertPrices(Element article, BOProduct bp) {
//		NodeList articlePrices = article.getElementsByTagName("ARTICLE_PRICE");
//		BOSalesPrice salesprice = new BOSalesPrice();
//		BOPurchasePrice purchaseprice = new BOPurchasePrice();
//
//		// Get Price Amount and get if exist Pirce_Type and Tax otherwise set
//		// Default like the other parameters (Counter, LowerBoundScaledPrice)
//		// Ugly: SaveOrUpdate-Method overrides multiple prices for the same
//		// Product...
//		for (int i = 0; i < articlePrices.getLength(); i++) {
//			Element articlePrice = (Element) articlePrices.item(i);
//			NodeList articlePriceAmountList = articlePrice
//					.getElementsByTagName("PRICE_AMOUNT");
//			String priceType;
//			if (articlePrice.getAttribute("price_type") != null)
//				priceType = articlePrice.getAttribute("price_type");
//			else
//				priceType = "net_list";
//			BigDecimal pAmount = BigDecimal.valueOf(Double
//					.valueOf(articlePriceAmountList.item(0).getFirstChild()
//							.getNodeValue()));
//			BigDecimal tax;
//			if (articlePrice.getElementsByTagName("TAX").getLength() > 0) {
//				NodeList taxes = articlePrice.getElementsByTagName("TAX");
//				Double taxDouble = Double.valueOf(taxes.item(0).getFirstChild()
//						.getNodeValue());
//				tax = BigDecimal.valueOf(taxDouble);
//			} else {
//				tax = BigDecimal.valueOf(Double.valueOf(0.1900));
//			}
//			salesprice.setProduct(bp);
//			// The Profit margin is twice as high
//			salesprice.setAmount(pAmount.multiply(new BigDecimal(1.5)));
//			salesprice.setPricetype(priceType);
//			salesprice.setTaxrate(tax);
//			salesprice.setCountry(new BOCountry(new Country("DE")));
//			salesprice.setLowerboundScaledprice(1);
//			purchaseprice.setProduct(bp);
//			purchaseprice.setAmount(pAmount);
//			purchaseprice.setPricetype(priceType);
//			purchaseprice.setTaxrate(tax);
//			purchaseprice.setCountry(new BOCountry(new Country("DE")));
//			purchaseprice.setLowerboundScaledprice(1);
//			PriceBOA.getInstance().saveOrUpdateSalesPrice(salesprice);
//			PriceBOA.getInstance().saveOrUpdatePurchasePrice(purchaseprice);
//		}
//	}
// }