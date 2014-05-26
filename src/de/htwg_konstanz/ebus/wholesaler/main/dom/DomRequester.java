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
import de.htwg_konstanz.ebus.framework.wholesaler.vo.Country;
import de.htwg_konstanz.ebus.wholesaler.main.handler.ImportHandler;

final class DomRequester {
	private static final String SALE_FACTOR = "1.5";
	private final Document dom;
	private final Map<String, AtomicInteger> updateFeedback;

	DomRequester(Document dom) {
		this(dom, new HashMap<String, AtomicInteger>());
	}

	DomRequester(Document dom, Map<String, AtomicInteger> updateFeedback) {
		this.dom = dom;
		this.updateFeedback = updateFeedback;
	}

	BOSupplier getSupplierFromName() {
		NodeList supplier = dom.getElementsByTagName("SUPPLIER_NAME");
		String companyName = supplier.item(0).getFirstChild().getNodeValue();
		List<BOSupplier> suppliers = SupplierBOA.getInstance()
				.findByCompanyName(companyName);
		return suppliers.isEmpty() ? new BOSupplier() : suppliers.get(0);
	}

	List<BOProduct> getAllProducts(BOSupplier supplier) {
		ArrayList<BOProduct> products = new ArrayList<BOProduct>();
		Element root = dom.getDocumentElement();
		NodeList articles = root.getElementsByTagName("ARTICLE");

		for (int i = 0; i <= articles.getLength(); i++) {
			// NodeList articleData = articles.item(i).getChildNodes();
			BOProduct product = new BOProduct();

			Element article = (Element) articles.item(i);

			NodeList shortDescrList = article
					.getElementsByTagName("DESCRIPTION_SHORT");
			product.setShortDescription(shortDescrList.item(0).getFirstChild()
					.getNodeValue());
			if (article.getElementsByTagName("DESCRIPTION_LONG").getLength() > 0) {
				NodeList longDescrList = article
						.getElementsByTagName("DESCRIPTION_LONG");
				product.setLongDescription(longDescrList.item(0)
						.getFirstChild().getNodeValue());
			}
			if (article.getElementsByTagName("MANUFACTURER_NAME").getLength() > 0) {
				NodeList mname = article
						.getElementsByTagName("MANUFACTURER_NAME");
				product.setManufacturer(mname.item(0).getFirstChild()
						.getNodeValue());
			}
			// TODO brauchen wir es so oder haben wir es anders ?
			NodeList sAIDList = article.getElementsByTagName("SUPPLIER_AID");
			product.setOrderNumberCustomer(sAIDList.item(0).getFirstChild()
					.getNodeValue());
			product.setSupplier(supplier);
			product.setOrderNumberSupplier(sAIDList.item(0).getFirstChild()
					.getNodeValue());

			// TODO hier der Tutor die Produkte wenn vorhaden, sollen wir das
			// bei uns auch ändern?
			// ??

			updateFeedback.get(ImportHandler.ADDED_PRODUCTS).incrementAndGet();
			ProductBOA.getInstance().saveOrUpdate(product);

			NodeList articlePrices = article
					.getElementsByTagName("ARTICLE_PRICE");
			getArticlePriceDetails(product, articlePrices);

			products.add(product);

		}

		return products;
	}

	private void getArticlePriceDetails(BOProduct product,
			NodeList articlePrices) {

		BOSalesPrice salesprice = new BOSalesPrice();
		BOPurchasePrice purchaseprice = new BOPurchasePrice();

		// Get Price Amount and get if exist Pirce_Type and Tax otherwise set
		// Default like the other parameters (Counter, LowerBoundScaledPrice)
		// Ugly: SaveOrUpdate-Method overrides multiple prices for the same
		// Product...
		for (int i = 0; i < articlePrices.getLength(); i++) {
			Element articlePrice = (Element) articlePrices.item(i);
			NodeList articlePriceAmountList = articlePrice
					.getElementsByTagName("PRICE_AMOUNT");
			String priceType;
			if (articlePrice.getAttribute("price_type") != null)
				priceType = articlePrice.getAttribute("price_type");
			else
				priceType = "net_list";
			BigDecimal pAmount = BigDecimal.valueOf(Double
					.valueOf(articlePriceAmountList.item(0).getFirstChild()
							.getNodeValue()));
			BigDecimal tax;
			if (articlePrice.getElementsByTagName("TAX").getLength() > 0) {
				NodeList taxes = articlePrice.getElementsByTagName("TAX");
				Double taxDouble = Double.valueOf(taxes.item(0).getFirstChild()
						.getNodeValue());
				tax = BigDecimal.valueOf(taxDouble);
			} else {
				tax = BigDecimal.valueOf(Double.valueOf(0.1900));
			}
			salesprice.setProduct(product);
			// The Profit margin is twice as high
			salesprice.setAmount(pAmount.multiply(new BigDecimal(SALE_FACTOR)));
			salesprice.setPricetype(priceType);
			salesprice.setTaxrate(tax);
			salesprice.setCountry(new BOCountry(new Country("DE")));
			salesprice.setLowerboundScaledprice(1);
			purchaseprice.setProduct(product);
			purchaseprice.setAmount(pAmount);
			purchaseprice.setPricetype(priceType);
			purchaseprice.setTaxrate(tax);
			purchaseprice.setCountry(new BOCountry(new Country("DE")));
			purchaseprice.setLowerboundScaledprice(1);
			PriceBOA.getInstance().saveOrUpdateSalesPrice(salesprice);
			PriceBOA.getInstance().saveOrUpdatePurchasePrice(purchaseprice);
		}
	}

}
