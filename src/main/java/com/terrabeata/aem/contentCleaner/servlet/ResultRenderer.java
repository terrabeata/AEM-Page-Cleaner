package com.terrabeata.aem.contentCleaner.servlet;

import java.io.IOException;
import java.net.URLEncoder;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adobe.daycare.contentCleaner.servlet.CleanUpServlet.CleanUpResult;
import com.sun.mail.iap.Argument;


public class ResultRenderer {

	private static final Logger log = LoggerFactory.getLogger(ResultRenderer.class);
	
	private static final String RESOURCE_LIST_PAGES = "daycare/page-content/components/list";
	private static final String RESOURCE_FAULT = "daycare/page-content/components/fault";
	private static final String RESOURCE_SUCCESS = "daycare/page-content/components/success";
	
	private static final String LIST_SELECTORS = "";
	private static final String SUCCESS_SELECTORS = "success";
	private static final String FAULT_SELECTORS = "fault";
	
	private static final String REQUEST_RESOURCE = "/apps/daycare/page-content/cleanup";
	
	private static final String CONTENT_TYPE = "text/html; charset=utf-8";

	public void renderChangeRootResult(SlingHttpServletRequest request, 
            SlingHttpServletResponse response) throws IOException {
    	String rootPath = request.getParameter("rootPath");
    	if (rootPath == null || rootPath == "/" || rootPath == "/content") {
    		rootPath = "";
    	}
    	rootPath = URLEncoder.encode(rootPath, "utf-8");
    	String redirectPath = REQUEST_RESOURCE + ".html"+"?rootPath=" + rootPath;
		response.sendRedirect(redirectPath);		
	}
	
	/**
	 * 
	 * Rendering method for the form that lists the Page nodes with 
	 * nt:unstructured content nodes
	 */
	public void renderForm(SlingHttpServletRequest request, 
            SlingHttpServletResponse response, String message)
           		 throws IOException
    {
		log.info("[renderForm]");
		RequestDispatcherOptions options = new RequestDispatcherOptions();
		options.setForceResourceType(RESOURCE_LIST_PAGES);
		options.setReplaceSelectors(LIST_SELECTORS);
		
		RequestDispatcher rd = request.getRequestDispatcher(REQUEST_RESOURCE, options);
		
		response.setContentType(CONTENT_TYPE);
		

		
		log.info("[renderForm] include JSP");
		try {
			rd.include(request, response);
		} catch (ServletException e){
			response.getWriter().write("ServletException: " + e.getMessage());
		}
		
		log.info("[renderForm] complete");
	}

	/**
	 * Method that calls HTML renderers for the success page.
	 */
	public void renderConvertSuccess(SlingHttpServletRequest request, 
            SlingHttpServletResponse response, CleanUpResult result)
           		 throws IOException
	{
		
		log.info("[renderConvertSuccess]");
		request.setAttribute("backupDirectory", result.getBackupDirectory());
		request.setAttribute("message", result.getMessage());
		request.setAttribute("original", result.getCleanedPage());
		RequestDispatcherOptions options = new RequestDispatcherOptions();
        options.setForceResourceType(RESOURCE_SUCCESS);
        options.setAddSelectors(SUCCESS_SELECTORS);
        response.setContentType(CONTENT_TYPE);
        
        RequestDispatcher rd = request.getRequestDispatcher(REQUEST_RESOURCE, options);
        
		log.info("[renderConvertSuccess] include resource type render");
		try {
			rd.forward(request, response);
		} catch (ServletException e){
			response.getWriter().write("ServletException: " + e.getMessage());
		}
			
	}
	
	/**
	 * Method that calls HTML renderers for the revert page.
	 */
	public void renderRevertChange(SlingHttpServletRequest request, 
            SlingHttpServletResponse response, CleanUpResult result)
           		 throws IOException
	{
		
		log.info("renderRevertChange::");
		
//		RequestDispatcherOptions options = new RequestDispatcherOptions();
//        options.setForceResourceType("page-content-cleanup/cleanup/revert");
//        options.setAddSelectors("revert");
//        response.setContentType("text/html; charset=utf-8");
//        
//        RequestDispatcher rd = request.getRequestDispatcher("/apps/page-content-cleanup/servlet", options);
//        
//		log.info("renderRevertChange:: include resource type render");
//        rd.include(request, response);
			
	}
	
	/**
	 * Method that calls HTML renderers for the commit page.
	 */
	public void renderCommitChange(SlingHttpServletRequest request, 
            SlingHttpServletResponse response, CleanUpResult result)
           		 throws IOException
	{
		
		log.info("renderCommitChange::");
		
		response.sendRedirect("/apps/page-content-cleanup.html");
			
	}


	/**
	 * Method that calls HTML renderers for the success page.
	 */
	public void renderConvertFault(SlingHttpServletRequest request, 
            SlingHttpServletResponse response, CleanUpResult result)
           		 throws IOException
	{
		
		if (result == null) {
			log.warn("[renderConvertFault] result is empty");
			return;
		}
		
		request.setAttribute("backupDirectory", result.getBackupDirectory());
		request.setAttribute("message", result.getMessage());
		request.setAttribute("original", result.getCleanedPage());
		RequestDispatcherOptions options = new RequestDispatcherOptions();
        options.setForceResourceType(RESOURCE_FAULT);
        options.setAddSelectors(FAULT_SELECTORS);
        response.setContentType(CONTENT_TYPE);

        request.setAttribute("error-message", result.getMessage());
        
        RequestDispatcher rd = request.getRequestDispatcher(REQUEST_RESOURCE, options);
        
		try {
			rd.include(request, response);
		} catch (ServletException e){
			response.getWriter().write("ServletException: " + e.getMessage());
		}
	}
	



	

}
