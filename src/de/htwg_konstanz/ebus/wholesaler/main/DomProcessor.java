package de.htwg_konstanz.ebus.wholesaler.main;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSalesPrice;

public class DomProcessor {

	private final Document document;

	DomProcessor(Document dom) {

		this.document = dom;

	}

	void appendProductCatalog(Element root, List<BOProduct> productList) {
		// creates a new element "T_NEW_CATALOG"
		Element catalog = document.createElement("T_NEW_CATALOG");

		for (BOProduct product : productList) {
			// calls appendArticle Method
			appendArticle(catalog, product);
		}
		// appends the product catalog to the document
		root.appendChild(catalog);
	}

	private void appendArticle(Element catalog, BOProduct product) {
		// Creates an element "ARTICLE" for the product catalog
		Element article = document.createElement("ARTICLE");
		// calls Method to get SupplierAID as a ChildNode
		appendSupplierAID(product, article);
		// calls Method to get ArticleDetails as a ChildNode
		appendArticleDetails(product, article);
		// calls Method to get ArticleOrderDetails as a ChildNode
		appendArticleOrderDetails(article);
		// calls Method to get ArticlePriceDetails as a ChildNode
		appendArticlePriceDetails(product, article);
		// appends the articles to the product catalog
		catalog.appendChild(article);
	}

	private void appendSupplierAID(BOProduct product, Element article) {
		// create an element SUPPLIER_AID
		Element supplierAID = document.createElement("SUPPLIER_AID");
		// read SupplierAID out of Database and append it to the TextNode of
		// supplierAID
		supplierAID.appendChild(document.createTextNode((product
				.getOrderNumberSupplier())));
		// appends supplierAID to articles
		article.appendChild(supplierAID);
	}

	private void appendArticleDetails(BOProduct product, Element article) {
		// creates an Element ARTICLE_DETAILS
		Element articleDetails = document.createElement("ARTICLE_DETAILS");
		// creates an Element DESCRIPTION_SHORT
		Element descShort = document.createElement("DESCRIPTION_SHORT");
		// read shortDescription out of Database and append it to the TextNode
		// of descShort
		descShort.appendChild(document.createTextNode(product
				.getShortDescription()));
		// appends shortDescription to articlesDetails
		articleDetails.appendChild(descShort);
		// read longDescription out of Database and append it to the TextNode
		// of descLong
		Element descLong = document.createElement("DESCRIPTION_LONG");
		descLong.appendChild(document.createTextNode(product
				.getLongDescription()));
		// appends longDescription to articlesDetails
		articleDetails.appendChild(descLong);
		// appends articleDetails to articles
		article.appendChild(articleDetails);
	}

	void appendHeader(Element root) {
		// Creates a new element "HEADER"
		Element header = document.createElement("HEADER");
		appendCatalog(header);
		appendSupplier(header);
		// appends header to the document
		root.appendChild(header);
	}

	private void appendCatalog(Element header) {
		// Creates and appends an element "CATALOG" and his childs "LANGUAGE",
		// "CATALOG_ID", "CATALOG_VERSION" and "CATALOG_NAME"

		Element catalog = document.createElement("CATALOG");
		Element language = document.createElement("LANGUAGE");
		language.appendChild(document.createTextNode("eng"));
		catalog.appendChild(language);

		Element catID = document.createElement("CATALOG_ID");
		catID.appendChild(document.createTextNode("CAT 12"));
		catalog.appendChild(catID);

		Element catVersion = document.createElement("CATALOG_VERSION");
		catVersion.appendChild(document.createTextNode("1.0"));
		catalog.appendChild(catVersion);

		Element catName = document.createElement("CATALOG_NAME");
		catName.appendChild(document.createTextNode("Export Catalog"));
		catalog.appendChild(catName);

		header.appendChild(catalog);
	}

	private void appendArticleOrderDetails(Element article) {
		/*
		 * Creates and appends an element "ARTICLE_ORDER_DETAILS" and his childs
		 * "ORDER_UNIT", "CONTENT_UNIT" and "NO_CU_PER_OU" to the document.
		 */

		// TODO nach Steffen Hardcodiert --> nochmal rausfinden
		Element orderDetails = document.createElement("ARTICLE_ORDER_DETAILS");

		Element orderUnit = document.createElement("ORDER_UNIT");
		orderUnit.appendChild(document.createTextNode("PK"));
		orderDetails.appendChild(orderUnit);

		Element contentUnit = document.createElement("CONTENT_UNIT");
		contentUnit.appendChild(document.createTextNode("C62"));
		orderDetails.appendChild(contentUnit);

		Element noCuPerOu = document.createElement("NO_CU_PER_OU");
		noCuPerOu.appendChild(document.createTextNode("10"));
		orderDetails.appendChild(noCuPerOu);

		article.appendChild(orderDetails);
	}

	private void appendArticlePriceDetails(BOProduct product, Element article) {
		/*
		 * Creates and appends an element "ARTICLE_PRICE_DETAILS" and for each
		 * price a child "ARTICLE_PRICE" and for each article price a child
		 * "PRICE_AMOUNT", "PRICE_CURRENCY", "TAX" and "TERRITORY" in the
		 * document.
		 */
		Element priceDetails = document.createElement("ARTICLE_PRICE_DETAILS");

		List<BOSalesPrice> salesPrices = product.getSalesPrices();
		for (BOSalesPrice salesPrice : salesPrices) {
			Element price = document.createElement("ARTICLE_PRICE");
			price.setAttribute("price_type", salesPrice.getPricetype());

			Element amount = document.createElement("PRICE_AMOUNT");
			amount.appendChild(document.createTextNode(salesPrice.getAmount()
					.toString()));
			price.appendChild(amount);

			Element currency = document.createElement("PRICE_CURRENCY");
			currency.appendChild(document.createTextNode(salesPrice
					.getCountry().getCurrency().getCode()));
			price.appendChild(currency);

			Element tax = document.createElement("TAX");
			tax.appendChild(document.createTextNode(salesPrice.getTaxrate()
					.toString()));
			price.appendChild(tax);

			Element territory = document.createElement("TERRITORY");
			territory.appendChild(document.createTextNode(salesPrice
					.getCountry().getIsocode()));
			price.appendChild(territory);

			priceDetails.appendChild(price);
		}
		article.appendChild(priceDetails);
	}

	private void appendSupplier(Element header) {
		/*
		 * Creates and appends an element "SUPPLIER" and his child
		 * "SUPPLIER_NAME". In this method the supplier name is set.
		 */
		Element supplier = document.createElement("SUPPLIER");
		Element supplierName = document.createElement("SUPPLIER_NAME");
		supplierName.appendChild(document.createTextNode("Firmenname"));
		supplier.appendChild(supplierName);
		header.appendChild(supplier);
	}

}
