package de.htwg_konstanz.ebus.wholesaler.main;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOCountry;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOCurrency;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOOrderItemPurchase;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOPurchasePrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.CountryBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.CurrencyBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.OrderItemBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.PriceBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.SupplierBOA;

final class DomRequester {
	private final Document dom;

	DomRequester(Document dom) {
		this.dom = dom;
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
		// TODO generate BOSalesPrice
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
					BOPurchasePrice price = new BOPurchasePrice();
					price.setCountry(country);
					price.setTaxrate(new BigDecimal(values.get("TAX")));
					price.setPricetype(articlePriceType);
					price.setAmount(new BigDecimal(values.get("PRICE_AMOUNT")));
					price.setProduct(product);

					// TODO store product before price? -> look it up in the
					// JavaDocs.
					// return a list of BOPurchasePrice and call the method
					// below after
					// saving the product
					PriceBOA.getInstance().saveOrUpdate(price);
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
				// TODO check if OrderItemNumber is really = Contentunit
				orderip.setOrderItemNumber(new Integer(nodeValue));
				break;
			case "NO_CU_PER_OU":
				// TODO: unused!!!
				// check if no_cu_per_ou for BOOrderItemPurchase is
				// OrderItemNumber
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
