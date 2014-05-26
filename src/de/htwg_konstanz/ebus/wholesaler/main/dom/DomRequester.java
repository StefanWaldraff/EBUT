package de.htwg_konstanz.ebus.wholesaler.main.dom;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOCountry;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOCurrency;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOOrderItemPurchase;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOPurchasePrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSalesPrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.CountryBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.CurrencyBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.OrderItemBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.PriceBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.SupplierBOA;
import de.htwg_konstanz.ebus.wholesaler.action.ImportAction;

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

		NodeList articles = dom.getElementsByTagName("ARTICLE");

		for (int i = 0; i <= articles.getLength(); i++) {
			NodeList articleData = articles.item(i).getChildNodes();
			BOProduct product = new BOProduct();
			product.setSupplier(supplier);
			getArticleData(articleData, product);
			products.add(product);
		}

		return products;
	}

	private void getArticleData(NodeList articleData, BOProduct product) {
		for (int j = 0; j < articleData.getLength(); j++) {
			Node node = articleData.item(j);
			switch (node.getNodeName()) {
			case "SUPPLIER_AID":
				product.setOrderNumberSupplier(node.getNodeValue());
				break;
			case "ARTICLE_DETAILS":
				getArticleDetails(articleData, product, node);
				break;
			case "ARTICLE_ORDER_DETAILS":
				getArticleOrderDetails(node);
				// TODO Connection to Product??
				updateFeedback.get(ImportAction.ADDED_PRODUCTS)
						.incrementAndGet();
				ProductBOA.getInstance().saveOrUpdate(product);
				break;
			case "ARTICLE_PRICE_DETAILS":
				NodeList articlePriceDetails = node.getChildNodes();
				getArticlePriceDetails(product, articlePriceDetails);
				break;
			}
		}
	}

	private void getArticlePriceDetails(BOProduct product,
			NodeList articlePriceDetails) {
		for (int i = 0; i < articlePriceDetails.getLength(); i++) {
			Node articlePrice = articlePriceDetails.item(i);
			// get price_type attribute
			String articlePriceType = articlePrice.getAttributes().item(0)
					.getNodeValue();
			NodeList articlePriceData = articlePrice.getChildNodes();

			HashMap<String, String> values = new HashMap<>();
			for (int k = 0; k < articlePriceData.getLength(); k++) {
				Node dataNode = articlePriceData.item(k);
				if (!dataNode.getNodeName().equals("TERRITORY"))
					values.put(dataNode.getNodeName(), dataNode.getNodeValue());
				else {
					BOCurrency currency = CurrencyBOA.getInstance()
							.findCurrency(values.get("PRICE_CURRENCY"));
					if (currency == null) {
						currency = new BOCurrency();
						currency.setCode(values.get("PRICE_CURRENCY"));
						CurrencyBOA.getInstance().saveOrUpdate(currency);
					}
					BOCountry country = CountryBOA.getInstance().findCountry(
							values.get("TERRITORY"));
					if (country == null) {
						country = new BOCountry();
						country.setCurrency(currency);
						country.setIsocode(values.get("TERRITORY"));
						CountryBOA.getInstance().saveOrUpdate(country);
					}
					// set Country, Taxrate and Pricetype for Purchaseprice
					BOPurchasePrice purchaseprice = new BOPurchasePrice();
					purchaseprice.setCountry(country);
					purchaseprice.setTaxrate(new BigDecimal(values.get("TAX")));
					purchaseprice.setPricetype(articlePriceType);
					purchaseprice.setAmount(new BigDecimal(values
							.get("PRICE_AMOUNT")));
					purchaseprice.setProduct(product);

					// set Country, Taxrate and Pricetype for SalesPrice
					BOSalesPrice salesprice = new BOSalesPrice();
					salesprice.setCountry(country);
					salesprice.setTaxrate(new BigDecimal(values.get("TAX")));
					salesprice.setPricetype(articlePriceType);

					// calculate Salesamount, which has an amount 1.5 times of
					// Purchaseamount
					BigDecimal purchaseamount = new BigDecimal(
							(values.get("PRICE_AMOUNT")));
					BigDecimal factor = new BigDecimal(SALE_FACTOR);
					MathContext mc = new MathContext(4); // 4 precision
					BigDecimal salesamount = purchaseamount
							.multiply(factor, mc);
					salesprice.setAmount(salesamount);
					salesprice.setProduct(product);

					// saving Purchase and Salesprice in Database
					updateFeedback.get(ImportAction.ADDED_PURCHASE_PRICES)
							.incrementAndGet();
					PriceBOA.getInstance().saveOrUpdate(purchaseprice);
					updateFeedback.get(ImportAction.ADDED_SALES_PRICES)
							.incrementAndGet();
					PriceBOA.getInstance().saveOrUpdate(salesprice);
				}
			}
		}
	}

	private void getArticleOrderDetails(Node node) {
		BOOrderItemPurchase orderip = new BOOrderItemPurchase();
		NodeList articleOrderDetails = node.getChildNodes();
		for (int a = 0; a < articleOrderDetails.getLength(); a++) {
			String nodeValue = articleOrderDetails.item(a).getNodeValue();
			switch (articleOrderDetails.item(a).getNodeName()) {
			case "ORDER_UNIT":
				orderip.setOrderUnit(nodeValue);
				break;
			case "CONTENT_UNIT":
				// TODO check if Contentunit is needed was not last semesters
				orderip.setOrderItemNumber(new Integer(nodeValue));
				break;
			case "NO_CU_PER_OU":
				// TODO: unused!!!
				// check if no_cu_per_ou for BOOrderItemPurchase is
				// is needed was not the last semesters
				break;
			}
		}
		// Create OrderItemBOA and save BOOrderItemPurchase
		OrderItemBOA.getInstance().saveOrUpdate(orderip);
	}

	private void getArticleDetails(NodeList articleData, BOProduct product,
			Node node) {
		NodeList articleDetails = node.getChildNodes();
		for (int k = 0; k < articleDetails.getLength(); k++) {
			Node detailNode = articleData.item(k);
			switch (detailNode.getNodeName()) {
			case "DESCRIPTION_SHORT":
				product.setShortDescription(detailNode.getFirstChild()
						.getNodeValue());
				break;
			case "DESCRIPTION_LONG":
				product.setLongDescription(detailNode.getFirstChild()
						.getNodeValue());
				break;
			}
		}
	}
}
