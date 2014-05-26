package de.htwg_konstanz.ebus.wholesaler.main.handler;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.wholesaler.demo.util.Constants;
import de.htwg_konstanz.ebus.wholesaler.main.dom.DomInteractor;

public class ExportHandler {
	private final List<String> errorList;

	public ExportHandler(List<String> errorList) {
		this.errorList = errorList;
	}

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
				// TODO transform XML DOM to XHTML DOM
				InputStream iST = null;
				// TODO InputStream aus File C:\\TEMP\\XMLtoXHMTLSchema.xslt
				dom = DomInteractor.createDomFromXml(iST, errorList);

				file = DomInteractor.createFileFromDom(dom, "xhtml", errorList);
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
