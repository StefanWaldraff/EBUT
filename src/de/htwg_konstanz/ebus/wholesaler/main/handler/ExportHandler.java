package de.htwg_konstanz.ebus.wholesaler.main.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.wholesaler.demo.util.Constants;
import de.htwg_konstanz.ebus.wholesaler.main.dom.DomInteractor;

/** Controls Export from Database to XMLFile an XHTML. */
public class ExportHandler {
	private final List<String> errorList;

	/**
	 * Constructor of ExportHandler
	 * 
	 * @param errorList
	 * */
	public ExportHandler(List<String> errorList) {
		this.errorList = errorList;
	}

	/**
	 * calls methods for exporting XML file or XHTML File.
	 * 
	 * @param request
	 * 
	 * @param response
	 */

	public void process(HttpServletRequest request, HttpServletResponse response) {
		if (request.getParameter("type") == null)
			// only called by page loading -> no action
			return;

		String action = request.getParameter("action");
		String match = request.getParameter("match");

		List<BOProduct> productList = filterProductList(request, match);
		if (productList.isEmpty()) {
			// no products remaining after filtering -> error
			errorList.add("No matching products found for pattern: " + match);
		}
		Document dom = null;
		if (errorList.isEmpty())
			dom = DomInteractor.createDomFromData(productList, errorList);
		if (errorList.isEmpty())
			DomInteractor.validateXml(dom, errorList);
		File file = null;
		if (errorList.isEmpty()) {
			switch (action) {
			case Constants.ACTION_EXPORT_XML:
				file = DomInteractor.createFileFromDom(dom, "xml", errorList);
				break;
			case Constants.ACTION_EXPORT_XHTML:
				file = DomInteractor.transformDomToXhtmlFile(dom, errorList);
				break;
			}
		}
		if (errorList.isEmpty()) {
			@SuppressWarnings("unchecked")
			ArrayList<String> urls = (ArrayList<String>) request.getSession(
					true).getAttribute("filesUploaded");
			String filePath = file.getAbsolutePath();
			urls.add(filePath);
		}
	}

	/**
	 * exporting only parts from productcatalog to
	 * 
	 * @param request
	 * @param toMatch
	 *            String of what should be matched
	 */
	private List<BOProduct> filterProductList(HttpServletRequest request,
			String toMatch) {
		List<BOProduct> productList = ProductBOA.getInstance().findAll();
		if (toMatch == null || toMatch.equals(""))
			// match is empty, nothing to filter
			return productList;

		// convert to lower case
		toMatch = toMatch.toLowerCase();
		List<BOProduct> filteredList = new ArrayList<BOProduct>();
		for (BOProduct p : productList) {
			String shortDescription = p.getShortDescription();
			if (shortDescription != null
					&& shortDescription.toLowerCase().contains(toMatch))
				filteredList.add(p);
		}
		return filteredList;
	}
}
