package de.htwg_konstanz.ebus.wholesaler.main.dom;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOCountry;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOPurchasePrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSalesPrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.PriceBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.SupplierBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa._BaseBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.vo.Country;
import de.htwg_konstanz.ebus.wholesaler.main.handler.ImportHandler;

/**
 * The Class DomRequester has methods to transform uploaded xml in database
 */
final class DomRequester {
	private final Document dom;
	private final Map<String, AtomicInteger> updateFeedback;

	/**
	 * Constructor with one parameter. Calls second Constructor
	 * 
	 */
	DomRequester(Document dom) {
		this(dom, new HashMap<String, AtomicInteger>());
	}

	/**
	 * Constructor class DOMREquester.
	 * */
	DomRequester(Document dom, Map<String, AtomicInteger> updateFeedback) {
		this.dom = dom;
		this.updateFeedback = updateFeedback;
	}

	/**
	 * Reads supplier out of Document.
	 */
	BOSupplier getSupplierFromName() {
		NodeList supplier = dom
				.getElementsByTagName(XmlConstants.SUPPLIER_NAME);
		String companyName = supplier.item(0).getFirstChild().getNodeValue();
		List<BOSupplier> suppliers = SupplierBOA.getInstance()
				.findByCompanyName(companyName);
		return suppliers.isEmpty() ? new BOSupplier() : suppliers.get(0);
	}

	/**
	 * iterates over all items of the product and save them in Database.
	 * 
	 */
	List<BOProduct> getAllProducts(BOSupplier supplier) {
		ArrayList<BOProduct> products = new ArrayList<BOProduct>();
		Element root = dom.getDocumentElement();
		NodeList articles = root.getElementsByTagName(XmlConstants.ARTICLE);

		for (int i = 0; i < articles.getLength(); i++) {

			BOProduct product = new BOProduct();

			Element article = (Element) articles.item(i);

			NodeList shortDescrList = article
					.getElementsByTagName(XmlConstants.DESCRIPTION_SHORT);
			product.setShortDescription(shortDescrList.item(0).getFirstChild()
					.getNodeValue());
			if (article.getElementsByTagName(XmlConstants.DESCRIPTION_LONG)
					.getLength() > 0) {
				NodeList longDescrList = article
						.getElementsByTagName(XmlConstants.DESCRIPTION_LONG);
				product.setLongDescription(longDescrList.item(0)
						.getFirstChild().getNodeValue());
			}
			if (article.getElementsByTagName(XmlConstants.MANUFACTURER_NAME)
					.getLength() > 0) {
				NodeList mname = article
						.getElementsByTagName(XmlConstants.MANUFACTURER_NAME);
				product.setManufacturer(mname.item(0).getFirstChild()
						.getNodeValue());
			}

			NodeList sAIDList = article
					.getElementsByTagName(XmlConstants.SUPPLIER_AID);
			product.setOrderNumberCustomer(sAIDList.item(0).getFirstChild()
					.getNodeValue());
			product.setSupplier(supplier);
			product.setOrderNumberSupplier(sAIDList.item(0).getFirstChild()
					.getNodeValue());

			// check for conflicts when reimporting
			BOProduct productInDb = ProductBOA
					.getInstance()
					.findByOrderNumberCustomer(product.getOrderNumberCustomer());
			if (productInDb != null) {
				// already exists in db
				for (BOSalesPrice price : productInDb.getSalesPrices()) {
					// remove all sales prices for product
					updateFeedback.get(ImportHandler.DELETED_SALES_PRICES)
							.incrementAndGet();
					PriceBOA.getInstance().delete(price);
				}
				for (BOPurchasePrice price : productInDb.getPurchasePrices()) {
					// remove all purchase prices for product
					updateFeedback.get(ImportHandler.DELETED_PURCASE_PRICES)
							.incrementAndGet();
					PriceBOA.getInstance().delete(price);
				}
				updateFeedback.get(ImportHandler.DELETED_PRODUCTS)
						.incrementAndGet();
				ProductBOA.getInstance().delete(productInDb);
				// store changes in db
				_BaseBOA.getInstance().commit();
			}

			updateFeedback.get(ImportHandler.ADDED_PRODUCTS).incrementAndGet();
			ProductBOA.getInstance().saveOrUpdate(product);

			NodeList articlePrices = article
					.getElementsByTagName(XmlConstants.ARTICLE_PRICE);
			getArticlePriceDetails(product, articlePrices);

			// store changes in db
			_BaseBOA.getInstance().commit();
			products.add(product);

		}

		return products;
	}

	/**
	 * Read Items of BOSalesprices and BOPurchaseprices and write them to
	 * Database.
	 */

	private void getArticlePriceDetails(BOProduct product,
			NodeList articlePrices) {

		BOSalesPrice salesprice = new BOSalesPrice();
		BOPurchasePrice purchaseprice = new BOPurchasePrice();

		// Checks if priceType and tax exists, otherwise set Default

		for (int i = 0; i < articlePrices.getLength(); i++) {
			Element articlePrice = (Element) articlePrices.item(i);
			NodeList articlePriceAmountList = articlePrice
					.getElementsByTagName(XmlConstants.PRICE_AMOUNT);
			String priceType;
			if (articlePrice.getAttribute(XmlConstants.PRICE_TYPE) != null) {
				priceType = articlePrice.getAttribute(XmlConstants.PRICE_TYPE);
			} else {
				priceType = XmlConstants.NET_LIST;
			}

			BigDecimal pAmount = BigDecimal.valueOf(Double
					.valueOf(articlePriceAmountList.item(0).getFirstChild()
							.getNodeValue()));
			BigDecimal tax;
			if (articlePrice.getElementsByTagName(XmlConstants.TAX).getLength() > 0) {
				NodeList taxes = articlePrice
						.getElementsByTagName(XmlConstants.TAX);
				Double taxDouble = Double.valueOf(taxes.item(0).getFirstChild()
						.getNodeValue());
				tax = BigDecimal.valueOf(taxDouble);
			} else {
				tax = BigDecimal.valueOf(Double.valueOf(0.1900));
			}
			salesprice.setProduct(product);
			// sale products 1.5 as high as bought
			salesprice.setAmount(pAmount.multiply(new BigDecimal(
					XmlConstants.SALE_FACTOR)));
			salesprice.setPricetype(priceType);
			salesprice.setTaxrate(tax);
			salesprice.setCountry(new BOCountry(new Country(XmlConstants.DE)));
			salesprice.setLowerboundScaledprice(1);
			purchaseprice.setProduct(product);
			purchaseprice.setAmount(pAmount);
			purchaseprice.setPricetype(priceType);
			purchaseprice.setTaxrate(tax);
			purchaseprice
					.setCountry(new BOCountry(new Country(XmlConstants.DE)));
			purchaseprice.setLowerboundScaledprice(1);

			// store sales price
			updateFeedback.get(ImportHandler.ADDED_SALES_PRICES)
					.incrementAndGet();
			PriceBOA.getInstance().saveOrUpdateSalesPrice(salesprice);

			// store purchase price
			updateFeedback.get(ImportHandler.ADDED_PURCHASE_PRICES)
					.incrementAndGet();
			PriceBOA.getInstance().saveOrUpdatePurchasePrice(purchaseprice);
		}
	}

}
