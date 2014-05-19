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
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import com.sun.org.apache.xerces.internal.impl.xpath.regex.ParseException;

import de.htwg_konstanz.ebus.framework.wholesaler.api.boa.ProductBOA;
import de.htwg_konstanz.ebus.framework.wholesaler.api.security.Security;
import de.htwg_konstanz.ebus.wholesaler.demo.IAction;
import de.htwg_konstanz.ebus.wholesaler.demo.LoginBean;
import de.htwg_konstanz.ebus.wholesaler.demo.util.Constants;

/**
* The LoginAction processes an authentication request.<p>
* The real work of the authentication process is done by the {@link LoginBean}.  
*
* @author tdi
*/
public class ImportAction implements IAction  {
	
	public static final String ACTION_SHOW_PRODUCT_LIST = "showProductList";
	public static final String PARAM_LOGIN_BEAN = "loginBean";
	private static final String PARAM_PRODUCT_LIST = "productList";
	private static final String UPLOAD_DIRECTORY = "upload";
	
	public ImportAction()
	{
		super();
	}

   /**
   * The execute method is automatically called by the dispatching sequence of the {@link ControllerServlet}. 
   * 
   * @param request the HttpServletRequest-Object provided by the servlet engine
   * @param response the HttpServletResponse-Object provided by the servlet engine
   * @param errorList a Stringlist for possible error messages occured in the corresponding action
   * @return the redirection URL
   */
	public String execute(HttpServletRequest request, HttpServletResponse response, ArrayList<String> errorList)
	{
		
		// Check that we have a file upload request
		boolean isMultipart = ServletFileUpload.isMultipartContent(request);
		if(isMultipart)
		{
		
		if (!ServletFileUpload.isMultipartContent(request)) {
			
			
			System.out.println("LEER");
			
		}
		
		
		
		// configures upload settings
        DiskFileItemFactory factory = new DiskFileItemFactory();

		// Configure a repository (to ensure a secure temp location is used)
		ServletContext servletContext = request.getSession().getServletContext();
		File repository = (File) servletContext.getAttribute("javax.servlet.context.tempdir");
		factory.setRepository(repository);

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);
		
	
		
		// Parse the request
			try {
				List<FileItem> items = upload.parseRequest(request);
				 if (items != null && items.size() > 0) {
		                // iterates over form's fields
					 for (FileItem item : items) {
		               
						 
						 /*
						// Process a regular form field
						 if (item.isFormField()) {
						     String name = item.getFieldName();
						     String value = item.getString();
						     
						 }
						 
						 
						 if (!item.isFormField()) {
							 String fieldName = item.getFieldName();
							    String fileName = item.getName();
							    String contentType = item.getContentType();
							    boolean isInMemory = item.isInMemory();
							    long sizeInBytes = item.getSize();
		                       
		                         }*/
		                        
							    InputStream iSt = item.getInputStream();
							    new ImportDOM(iSt);
		            		 	System.out.println("I'm here");
		                    
					 }
				 }
				 else{
					 PrintWriter writer =response.getWriter();
					 writer.write("Keine Datei gewählt!");
					 writer.flush();
					 writer.close();
				 }
				 
				 
				 
				 request.setAttribute("message","Upload has been done successfully!");
				 
			} 
			catch (Exception ex) {
	            request.setAttribute("message",
	                    "There was an error: " + ex.getMessage());
	            System.out.println("error: "+ ex.getMessage());
			}
			
			
		}
			
			 
	    	
		
		
		
		// get the login bean from the session
		LoginBean loginBean = (LoginBean)request.getSession(true).getAttribute(PARAM_LOGIN_BEAN);
		
		// ensure that the user is logged in
		if (loginBean != null && loginBean.isLoggedIn())
		{
			// ensure that the user is allowed to execute this action (authorization)
			// at this time the authorization is not fully implemented.
			// -> use the "Security.RESOURCE_ALL" constant which includes all resources.
			if (Security.getInstance().isUserAllowed(loginBean.getUser(), Security.RESOURCE_ALL, Security.ACTION_READ))
			{
				// find all available products and put it to the session
				List<?> productList = ProductBOA.getInstance().findAll();
				request.getSession(true).setAttribute(PARAM_PRODUCT_LIST, productList);					
			    
				// redirect to the import page
				return "import.jsp";
				
			    
			}
			else
			{
				// authorization failed -> show error message
				errorList.add("You are not allowed to perform this action!");
				
				// redirect to the welcome page
				return "welcome.jsp";
			}
		}
		else
			// redirect to the login page
			return "login.jsp";				
	}

   /**
   * Each action itself decides if it is responsible to process the corrensponding request or not.
   * This means that the {@link ControllerServlet} will ask each action by calling this method if it
   * is able to process the incoming action request, or not.
   * 
   * @param actionName the name of the incoming action which should be processed
   * @return true if the action is responsible, else false
   */
	public boolean accepts(String actionName)
	{
		return actionName.equalsIgnoreCase(Constants.ACTION_IMPORT);
	}
}
