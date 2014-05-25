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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;

import de.htwg_konstanz.ebus.framework.wholesaler.api.bo.BOProduct;
import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.security.Security;
import de.htwg_konstanz.ebus.wholesaler.demo.ControllerServlet;
import de.htwg_konstanz.ebus.wholesaler.demo.IAction;
import de.htwg_konstanz.ebus.wholesaler.demo.LoginBean;
import de.htwg_konstanz.ebus.wholesaler.demo.util.Constants;
import de.htwg_konstanz.ebus.wholesaler.main.DomInteractor;

/**
 * The LoginAction processes an authentication request.
 * <p>
 * The real work of the authentication process is done by the {@link LoginBean}.
 * 
 * @author tdi
 */
public class ExportAction implements IAction {

	private List<String> errorList;

	public ExportAction() {
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
				this.errorList = errorList;
				doExport(request, response);
			} else {
				// authorization failed -> show error message
				errorList.add("You are not allowed to perform this action!");
			}
			return errorList.isEmpty() ? "export.jsp" : "welcome.jsp";
		} else
			// redirect to the login page
			return "login.jsp";
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
		return actionName.equalsIgnoreCase(Constants.ACTION_EXPORT_XML)
				|| actionName.equalsIgnoreCase(Constants.ACTION_EXPORT_XHTML);
	}

	private void doExport(HttpServletRequest request,
			HttpServletResponse response) {
		if (request.getParameter("type") != null)
			// only called by page loading -> no action
			return;

		String action = request.getParameter("action");
		String match = request.getParameter("match");

		List<BOProduct> productList = filterProductList(request, match);
		if (productList.isEmpty()) {
			// no products remaining after filtering -> error
			errorList.add("No matching products found for pattern: " + match);
			return;
		}
		Document dom = DomInteractor.createDomFromData(productList, errorList);
		DomInteractor.validateXml(dom, errorList);
		if (errorList.isEmpty()) {
			switch (action) {
			case Constants.ACTION_EXPORT_XML:
				// TODO write file and provide it
				// http://www.journaldev.com/1964/servlet-upload-file-and-download-file-example
				TransformerFactory tranFactory = TransformerFactory
						.newInstance();
				Transformer aTransformer;
				File f = null;
				String fileName = "ExportProduktkatalog.xml";
				try {
					aTransformer = tranFactory.newTransformer();
					Source src = new DOMSource(dom);
					f = new File(fileName);
					Result dest = new StreamResult(f);
					aTransformer.transform(src, dest);
				} catch (TransformerConfigurationException e) {
					errorList.add("Error while configure Transformer");
					e.printStackTrace();
				} catch (TransformerException e) {
					errorList.add("Error while transforming DOMSource to File");
					e.printStackTrace();
				}
				ServletContext ctx = request.getSession().getServletContext();
				InputStream fis;
				try {
					fis = new FileInputStream(f);
					String mimeType = ctx.getMimeType(f.getAbsolutePath());
					response.setContentType(mimeType != null ? mimeType
							: "application/octet-stream");
					response.setContentLength((int) f.length());
					response.setHeader("Content-Disposition",
							"attachment; filename=\"" + fileName + "\"");
					ServletOutputStream os = response.getOutputStream();

					byte[] bufferData = new byte[1024];
					int read = 0;

					while ((read = fis.read(bufferData)) != -1) {
						os.write(bufferData, 0, read);
					}
					os.flush();
					os.close();
					fis.close();
					PrintWriter out = response.getWriter();
					out.write("<a href=\"UploadDownloadFileServlet?fileName="
							+ f.getName() + "\">Download " + f.getName()
							+ "</a>");
					System.out
							.println("File downloaded at client successfully");
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				break;
			case Constants.ACTION_EXPORT_XHTML:
				// TODO convert to XHTML-DOM, validate, write file and provide
				// it
				break;
			}
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
