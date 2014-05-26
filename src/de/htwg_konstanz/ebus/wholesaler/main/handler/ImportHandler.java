package de.htwg_konstanz.ebus.wholesaler.main.handler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.w3c.dom.Document;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOPurchasePrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSalesPrice;
import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOSupplier;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.PriceBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.wholesaler.main.dom.DomInteractor;

public class ImportHandler {

	public static final String ADDED_SALES_PRICES = "added sales prices";
	public static final String ADDED_PURCHASE_PRICES = "added purchase prices";
	public static final String ADDED_PRODUCTS = "added products";
	public static final String DELETED_SALES_PRICES = "deleted sales prices";
	public static final String DELETED_PURCASE_PRICES = "deleted purcase prices";
	public static final String DELETED_PRODUCTS = "deleted products";

	private final List<String> errorList;
	private final Map<String, AtomicInteger> updateFeedback;

	public ImportHandler(List<String> errorList,
			Map<String, AtomicInteger> updateFeedback) {
		super();
		this.errorList = errorList;
		this.updateFeedback = updateFeedback;
	}

	private void initFeedbackMap() {
		updateFeedback.put(DELETED_PRODUCTS, new AtomicInteger(0));
		updateFeedback.put(DELETED_PURCASE_PRICES, new AtomicInteger(0));
		updateFeedback.put(DELETED_SALES_PRICES, new AtomicInteger(0));
		updateFeedback.put(ADDED_PRODUCTS, new AtomicInteger(0));
		updateFeedback.put(ADDED_PURCHASE_PRICES, new AtomicInteger(0));
		updateFeedback.put(ADDED_SALES_PRICES, new AtomicInteger(0));
	}

	private boolean error() {
		return !errorList.isEmpty();
	}

	private InputStream getFileStream(HttpServletRequest request) {

		// configures upload settings
		DiskFileItemFactory factory = new DiskFileItemFactory();

		// Configure a repository (to ensure a secure temp location is used)
		ServletContext servletContext = request.getSession()
				.getServletContext();
		File repository = (File) servletContext
				.getAttribute("javax.servlet.context.tempdir");
		factory.setRepository(repository);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		InputStream iSt = null;
		try {
			List<FileItem> items = upload.parseRequest(request);
			if (items != null && items.size() > 0) {
				initFeedbackMap();
				// iterates over form's fields
				for (FileItem item : items) {
					// parse them in input stream
					iSt = item.getInputStream();
					break;
				}
			}

		} catch (FileUploadException | IOException ex) {
			errorList.add("File couldn't be uploaded");
		}
		return iSt;
	}

	public void process(HttpServletRequest request, HttpServletResponse response) {
		initFeedbackMap();
		InputStream xmlDocument = getFileStream(request);
		if (error())
			return;
		Document dom = DomInteractor.createDomFromXml(xmlDocument, errorList);
		if (error())
			return;
		DomInteractor.validateXml(dom, errorList);
		BOSupplier supplier = null;
		if (error())
			return;
		supplier = DomInteractor.containsValideSupplier(dom, errorList);
		if (error())
			return;
		deleteAllSupplierProductsFromDb(supplier);
		if (error())
			return;
		DomInteractor.writeDomToDb(dom, errorList, updateFeedback);
		if (error())
			return;
		request.getSession(true).setAttribute("updateFeedback", updateFeedback);
	}

	private void deleteAllSupplierProductsFromDb(BOSupplier supplier) {
		ProductBOA boa = ProductBOA.getInstance();
		List<BOProduct> allProducts = boa.findAll();
		for (BOProduct boProduct : allProducts) {
			if (boProduct.getSupplier().equals(supplier)) {
				for (BOSalesPrice price : boProduct.getSalesPrices()) {
					updateFeedback.get(DELETED_SALES_PRICES).incrementAndGet();
					PriceBOA.getInstance().deleteSalesPrice(price);
				}
				for (BOPurchasePrice pprice : boProduct.getPurchasePrices()) {
					updateFeedback.get(DELETED_PURCASE_PRICES)
							.incrementAndGet();
					PriceBOA.getInstance().deletePurchasePrice(pprice);
				}
				updateFeedback.get(DELETED_PRODUCTS).incrementAndGet();
				boa.delete(boProduct);
			}
		}
	}
}
