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

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import de.htwg_konstanz.ebus.framework.wholesaler.api.security.Security;
import de.htwg_konstanz.ebus.wholesaler.demo.ControllerServlet;
import de.htwg_konstanz.ebus.wholesaler.demo.IAction;
import de.htwg_konstanz.ebus.wholesaler.demo.LoginBean;
import de.htwg_konstanz.ebus.wholesaler.demo.util.Constants;
import de.htwg_konstanz.ebus.wholesaler.main.handler.ExportHandler;

/**
 * The LoginAction processes an authentication request.
 * <p>
 * The real work of the authentication process is done by the {@link LoginBean}.
 * 
 * @author tdi
 */
public class ExportAction implements IAction {

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
		request.getSession(true).setAttribute("filesUploaded",
				new ArrayList<String>());
		// ensure that the user is logged in
		if (loginBean != null && loginBean.isLoggedIn()) {
			// ensure that the user is allowed to execute this action
			// (authorization)
			// at this time the authorization is not fully implemented.
			// -> use the "Security.RESOURCE_ALL" constant which includes all
			// resources.
			if (Security.getInstance().isUserAllowed(loginBean.getUser(),
					Security.RESOURCE_ALL, Security.ACTION_READ)) {
				new ExportHandler(errorList).process(request, response);
				return "export.jsp";
			} else {
				// authorization failed -> show error message
				errorList.add("You are not allowed to perform this action!");
				return "welcome.jsp";
			}

		} else {
			// redirect to the login page
			return "login.jsp";
		}
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

}
