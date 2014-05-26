/* LoginAction.java
 ***********************************************************************************
 * 15.03.2007 ** tdi
 * - created
 *
 ***********************************************************************************
 * Copyright 2007 HTWG Konstanz
 * 
 * Prof. Dr.-Ing. Juergen Waesch
 * Dipl. -Inf. (FH) Thomas Dietrich
 * Fakultaet Informatik - Department of Computer Science
 * E-Business Technologien 
 * 
 * Hochschule Konstanz Technik, Wirtschaft und Gestaltung
 * University of Applied Sciences
 * Brauneggerstrasse 55
 * D-78462 Konstanz
 * 
 * E-Mail: juergen.waesch(at)htwg-konstanz.de
 ************************************************************************************/
package de.htwg_konstanz.ebus.wholesaler.action;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
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
import de.htwg_konstanz.ebus.framework.wholesaler.api.security.Security;
import de.htwg_konstanz.ebus.wholesaler.demo.ControllerServlet;
import de.htwg_konstanz.ebus.wholesaler.demo.IAction;
import de.htwg_konstanz.ebus.wholesaler.demo.LoginBean;
import de.htwg_konstanz.ebus.wholesaler.demo.util.Constants;
import de.htwg_konstanz.ebus.wholesaler.main.dom.DomInteractor;

/**
 * The LoginAction processes an authentication request.
 * <p>
 * The real work of the authentication process is done by the {@link LoginBean}.
 * 
 * @author tdi
 */
public class ImportAction implements IAction {

	public static final String ADDED_SALES_PRICES = "added sales prices";
	public static final String ADDED_PURCHASE_PRICES = "added purchase prices";
	public static final String ADDED_PRODUCTS = "added products";
	public static final String DELETED_SALES_PRICES = "deleted sales prices";
	public static final String DELETED_PURCASE_PRICES = "deleted purcase prices";
	public static final String DELETED_PRODUCTS = "deleted products";
	private List<String> errorList = null;
	private final Map<String, AtomicInteger> updateFeedback = new HashMap<>();

	public ImportAction() {
		super();
	}

	/**
	 * The execute method is automatically called by the dispatching sequence of
	 * the {@link ControllerServlet}.
	 * 
	 * @param request
	 *            the HttpServletRequest-Object provided by the servlet engine
	 * @param response
	 *            the HttpServletResponse-Object provided by the servlet engine
	 * @param errorList
	 *            a Stringlist for possible error messages occured in the
	 *            corresponding action
	 * @return the redirection URL
	 */
	public String execute(HttpServletRequest request,
			HttpServletResponse response, ArrayList<String> errorList) {
		request.getSession(true).setAttribute("updateFeedback", updateFeedback);
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if (isMultipart) {

			this.errorList = errorList;

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
			try {
				List<FileItem> items = upload.parseRequest(request);
				if (items != null && items.size() > 0) {
					initFeedbackMap();
					// iterates over form's fields
					for (FileItem item : items) {
						// parse them in input stream
						InputStream iSt = item.getInputStream();
						// Creates ImportDOM and with the xml Stream as a
						// parameter
						handleImport(iSt);

					}
				}

			} catch (FileUploadException | IOException ex) {

				errorList.add("File couldn't be uploaded");
			}
			return "import.jsp";

		}

		// get the login bean from the session
		LoginBean loginBean = (LoginBean) request.getSession(true)
				.getAttribute(Constants.PARAM_LOGIN_BEAN);

		// ensure that the user is logged in
		if (loginBean != null && loginBean.isLoggedIn()) {
			// ensure that the user is allowed to execute this action
			// (authorization)
			// at this time the authorization is not fully implemented.
			// -> use the "Security.RESOURCE_ALL" constant which includes all
			// resources.
			if (Security.getInstance().isUserAllowed(loginBean.getUser(),
					Security.RESOURCE_ALL, Security.ACTION_READ)) {

				// redirect to the import page
				return "import.jsp";

			} else {
				// authorization failed -> show error message
				errorList.add("You are not allowed to perform this action!");

				// redirect to the welcome page
				return "welcome.jsp";
			}
		} else
			// redirect to the login page
			return "login.jsp";
	}

	private void initFeedbackMap() {
		updateFeedback.put(DELETED_PRODUCTS, new AtomicInteger(0));
		updateFeedback.put(DELETED_PURCASE_PRICES, new AtomicInteger(0));
		updateFeedback.put(DELETED_SALES_PRICES, new AtomicInteger(0));
		updateFeedback.put(ADDED_PRODUCTS, new AtomicInteger(0));
		updateFeedback.put(ADDED_PURCHASE_PRICES, new AtomicInteger(0));
		updateFeedback.put(ADDED_SALES_PRICES, new AtomicInteger(0));
	}

	/**
	 * Each action itself decides if it is responsible to process the
	 * corrensponding request or not. This means that the
	 * {@link ControllerServlet} will ask each action by calling this method if
	 * it is able to process the incoming action request, or not.
	 * 
	 * @param actionName
	 *            the name of the incoming action which should be processed
	 * @return true if the action is responsible, else false
	 */
	public boolean accepts(String actionName) {
		return actionName.equalsIgnoreCase(Constants.ACTION_IMPORT);
	}

	private boolean noError() {
		return errorList.isEmpty();
	}

	private void handleImport(InputStream xmlDocument) {
		Document dom = DomInteractor.createDomFromXml(xmlDocument, errorList);
		if (noError())
			DomInteractor.validateXml(dom, errorList);
		BOSupplier supplier = null;
		if (noError())
			supplier = DomInteractor.containsValideSupplier(dom, errorList);
		if (noError())
			deleteAllSupplierProductsFromDb(supplier);
		if (noError())
			DomInteractor.writeDomToDb(dom, errorList, updateFeedback);
	}

	private void deleteAllSupplierProductsFromDb(BOSupplier supplier) {

		// TODO Feedback
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