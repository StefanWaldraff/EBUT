package de.htwg_konstanz.ebus.wholesaler.main.dom;

import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSalesPrice;

/**
 * writes Data to XML Document
 * */
public class DomProcessor {

	private final Document document;

	/**
	 * Class Constructor
	 * 
	 * @param Document
	 * */
	DomProcessor(Document dom) {

		this.document = dom;

	}

	/**
	 * Creates a new element "T_NEW_CATALOG" and appends the product catalog to
	 * the document.
	 * 
	 * @param root
	 *            the actually element you like to append the product catalog
	 * 
	 * @param productList
	 *            a {@link BOProduct} list with all products to export
	 */

	void addProductCatalog(Element root, List<BOProduct> productList) {
		Element catalog = document.createElement("T_NEW_CATALOG");
		for (BOProduct product : productList) {
			addArticle(catalog, product);
		}
		root.appendChild(catalog);
	}

	/**
	 * Creates an element "ARTICLE" for the product catalog and calls other
	 * methods to create the article in the catalog.
	 * 
	 * @param catalog
	 *            the element where to append all the products to export
	 * 
	 * @param product
	 *            a {@link BOProduct} product to add to the catalog
	 * 
	 */
	private void addArticle(Element catalog, BOProduct product) {

		Element article = document.createElement("ARTICLE");
		addSupplierAID(product, article);
		addArticleDetails(product, article);
		addArticleOrderDetails(article);
		addArticlePriceDetails(product, article);
		catalog.appendChild(article);
	}

	/**
	 * Creates and appends the element "SUPPLIER_AID" for the product catalog.
	 * 
	 * @param product
	 *            a {@link BOProduct} product to add to the catalog
	 * @param article
	 *            the element where to append the supplierAID
	 */

	private void addSupplierAID(BOProduct product, Element article) {

		Element supplierAID = document.createElement("SUPPLIER_AID");
		supplierAID.appendChild(document.createTextNode((product
				.getOrderNumberSupplier())));
		article.appendChild(supplierAID);
	}

	/**
	 * Creates and appends an element "ARTICLE_DETAILS" and his childs
	 * "DESCRIPTION_SHORT" and "DESCRIPTION_LONG" to the document.
	 * 
	 * @param product
	 *            a {@link BOProduct} product to add to the catalog
	 * 
	 * @param article
	 *            the article element where to append the details
	 */
	private void addArticleDetails(BOProduct product, Element article) {
		Element articleDetails = document.createElement("ARTICLE_DETAILS");
		Element descrShort = document.createElement("DESCRIPTION_SHORT");
		descrShort.appendChild(document.createTextNode(product
				.getShortDescription()));
		articleDetails.appendChild(descrShort);
		Element descrLong = document.createElement("DESCRIPTION_LONG");
		descrLong.appendChild(document.createTextNode(product
				.getLongDescription()));
		articleDetails.appendChild(descrLong);
		article.appendChild(articleDetails);
	}

	/**
	 * Creates and appends a new element "HEADER" to the document. Also calls
	 * methds to append a product catalog and a supplier.
	 * 
	 * @param root
	 *            the element in the document were to append the header
	 */

	void addHeader(Element root) {
		// Creates a new element "HEADER"
		Element header = document.createElement("HEADER");
		addCatalog(header);
		addSupplier(header);
		// appends header to the document
		root.appendChild(header);
	}

	/**
	 * Creates and appends an element "CATALOG" and his childs "LANGUAGE",
	 * "CATALOG_ID", "CATALOG_VERSION" and "CATALOG_NAME".
	 * 
	 * @param header
	 *            the header part of the document were to append the catalog
	 *            details
	 * 
	 */
	private void addCatalog(Element header) {

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

	/**
	 * Creates and appends an element "ARTICLE_ORDER_DETAILS" and his childs
	 * "ORDER_UNIT", "CONTENT_UNIT" and "NO_CU_PER_OU" to the document.
	 * 
	 * @param article
	 *            the article element where to append the details
	 */
	private void addArticleOrderDetails(Element article) {

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

	/**
	 * Creates and appends an element "ARTICLE_PRICE_DETAILS" and for each price
	 * a child "ARTICLE_PRICE" and for each article price a child
	 * "PRICE_AMOUNT", "PRICE_CURRENCY", "TAX" and "TERRITORY" in the document.
	 * 
	 * @param product
	 *            a {@link BOProduct} product to add to the catalog
	 * 
	 * @param article
	 *            the article element where to append the details
	 */
	private void addArticlePriceDetails(BOProduct product, Element article) {

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

	/**
	 * Creates and appends an element "SUPPLIER" and his child "SUPPLIER_NAME".
	 * In this method the supplier name is set.
	 * 
	 * @param header
	 *            the header part of the document were to append the catalog
	 *            details
	 */

	private void addSupplier(Element header) {

		Element supplier = document.createElement("SUPPLIER");
		Element supplierName = document.createElement("SUPPLIER_NAME");
		supplierName.appendChild(document.createTextNode("Firmenname"));
		supplier.appendChild(supplierName);
		header.appendChild(supplier);
	}

}
