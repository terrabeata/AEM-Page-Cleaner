package com.terrabeata.aem.contentCleaner.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.servlet.ServletException;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestPathInfo;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.apache.sling.auth.core.AuthenticationSupport;
import org.apache.sling.jcr.api.SlingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.search.Predicate;
import com.day.cq.search.PredicateGroup;
import com.day.cq.search.Query;
import com.day.cq.search.QueryBuilder;
import com.day.cq.search.result.Hit;
import com.day.cq.search.result.SearchResult;


@Properties({
	@Property(name="service.description", value="Clean-up Servlet"),
	@Property(name="service.vendor", value="Adobe Support")
})
@Component(metatype = false, immediate = true)
@SlingServlet(
		methods={"GET","POST"},
		generateComponent = false,
		resourceTypes={"daycare/pageClean"}
	)
public class CleanUpServlet extends SlingAllMethodsServlet {
	
	public static final String PAGE_CONTENT_PRIMARY_TYPE = "cq:PageContent";

	public static final String PAGE_COPY_DIRECTORY = 
			                                   "/var/page-content-cleanup/";
	
	public static final String SEARCH_PARENT_TYPE = "cq:Page";

	public static final String INVALID_CONTENT_TYPE = 
			                                   JcrConstants.NT_UNSTRUCTURED;
	
	public static final String VALID_CONTENT_TYPE = "cq:PageContent";
	
	public static final String DEFAULT_ROOT_PATH = "/content";
	
	public static final String PROPERTIES_NOT_COPIED = "propertiesNotCopied";
	
	public static final String CHILDREN_NOT_COPIED = "childNodesNotCopied";

	private static final long serialVersionUID = -7279957921233392800L;
	
	private static final Logger log = 
			LoggerFactory.getLogger(CleanUpServlet.class);
	

	@Reference
	private QueryBuilder queryBuilder;
	
	@Reference
	private SlingRepository repository;

	private ResultRenderer renderer;
	
	public CleanUpServlet() {
		renderer = new ResultRenderer();
	}
	
	
	//==========================================================================
	// GET is used by the page used for entry into the utility - the listing
	// of pages with contents nodes of nt:unstructured. After this process is 
	// done through POST so parameters are not part of the url.
	//
	// Page rendering is done using the resource type of 
	// /apps/pennwell42953
	// Uses the selector for rendering:
	//    list-form
	//==========================================================================
	
	@Override
    protected void doGet(SlingHttpServletRequest request, 
    		       SlingHttpServletResponse response) 
    		       throws ServletException, IOException {
		
		log.debug("[doGet]");
		
//		RequestPathInfo requestPathInfo = request.getRequestPathInfo();
		
		RequestPathInfo requestPathInfo = request.getRequestPathInfo();
		String selectors = requestPathInfo.getSelectorString();
		
	    final ResourceResolver resolver = 
	    		(ResourceResolver) request.getAttribute(
	    				AuthenticationSupport.REQUEST_ATTRIBUTE_RESOLVER);
	    final Session session = 
	    		(resolver != null) ? resolver.adaptTo(Session.class) : null;
	    
	    String rootPath = request.getParameter("rootPath");
	    if (rootPath == null || rootPath.length() == 0) 
	    	rootPath = DEFAULT_ROOT_PATH;
	    
	    request.setAttribute("rootPath", rootPath);
	    
	   

	    if (rootPath == "" || rootPath == "/" || rootPath == "/content") {
	    	renderer.renderForm(request, response, "Provide a root path");
	    	return;
	    }
	    
	    PredicateGroup searchPred = new PredicateGroup("pages");
	    Predicate typePred = new Predicate("nodeType", "type");
	    typePred.set("type", SEARCH_PARENT_TYPE);
	    searchPred.add(typePred);
	    Predicate pathPred = new Predicate("rootPath", "path");
	    pathPred.set("path", rootPath);
	    searchPred.add(pathPred);
	    
	    
	    Query query = this.queryBuilder.createQuery(searchPred, session);
	    query.setHitsPerPage(0);
	    query.setStart(0);
	    SearchResult result = query.getResult();
	    List<Hit> hits = result.getHits();
	    ArrayList<String> paths = new ArrayList<String>();
	    
	    for (Hit hit : hits) {
	  		try {
		    	Resource resultResource = hit.getResource();
		    	
		    	
		    	
		    	Resource contentResource = resolver.resolve(resultResource.getPath() + "/" + JcrConstants.JCR_CONTENT);
		    	
		    	if (contentResource != null) {
		    		ValueMap contentProperties = 
		    				contentResource.adaptTo(ValueMap.class);
		    	    
		    	    String primaryType = 
		    	    		contentProperties.get("jcr:primaryType").toString();
		    	    
		    		if (primaryType.equals(INVALID_CONTENT_TYPE)) {
		    			paths.add(hit.getPath());
		    		}
		    	}
			} catch (Exception e)
			{
				log.warn("Exception occurred while accessing page: {}: {}", e.getClass().getName(), e.getMessage());
			}
		}
	    
	    request.setAttribute("hit-paths", paths);

		renderer.renderForm(request, response, null);
		
    }
	
	//==========================================================================
	// POST is used for all processing after the entry page.
	//
	// Page rendering is done using the resource types:
	//     /apps/pennwell42953/cleanup/success
	//     /apps/pennwell42953/cleanup/success
	// Selectors include:
	//    convert
	//    revertchange
	//    commitchange
	//==========================================================================
	
    protected void doPost(SlingHttpServletRequest request, 
    		             SlingHttpServletResponse response) 
    		            		 throws IOException
    {
    	
    	log.debug("[doPost]");
    	RequestPathInfo requestPathInfo = request.getRequestPathInfo();
		String selectors = requestPathInfo.getSelectorString();
		log.debug("[doPost] selectors={}", selectors);
		
		if (selectors.contains("revertchange")) {
			doRevertChangeSelector(request, response);
		} else if (selectors.contains("commitchange")) {
			doCommitChangeSelector(request, response);
		} else if (selectors.contains("convert")) {
			doConvertSelector(request, response);
		} else { // root path just returns to form
			doChangeRootPath(request, response);
		} 
	}
    
    private void doChangeRootPath(SlingHttpServletRequest request, 
            SlingHttpServletResponse response) 
              		 throws IOException {
    	renderer.renderChangeRootResult(request, response);
    }
	
	@SuppressWarnings("unused")
	private void doConvertSelector(SlingHttpServletRequest request, 
            SlingHttpServletResponse response) 
           		 throws IOException
    {
		String path = request.getParameter("nodes-to-change");
		log.debug("[doConvertSelector] nodes-to-change={}", path);
		
		CleanUpResult result = new CleanUpResult(true, "test");
		
	    final ResourceResolver resolver = 
	    		(ResourceResolver) request.getAttribute(
	    				      AuthenticationSupport.REQUEST_ATTRIBUTE_RESOLVER);
	    
	    final Session session = resolver.adaptTo(Session.class);
	    
	    Resource resource =  null;
	    Resource content = null;
	    Resource destination = null;

	    try {
		    if (path != null && path != "")
			{
			    resource = resolver.getResource(path);
			    content = resource.getChild(JcrConstants.JCR_CONTENT);
			    String contentPath = content.getPath();
			    

			    log.debug("[doConvertSelector] clean resource {}", path);
				long id = new Random().nextLong();
				String destinationName = Long.toHexString(id);
				String destinationPath = PAGE_COPY_DIRECTORY+
						destinationName;
				
				log.debug("[doConvertSelector] create destination for backup", path);
				JcrUtil.createPath(destinationPath, "nt:unstructured", session);
				
				destination = resolver.getResource(destinationPath);
				
				String msg = "Unable to resolve resource: ";
				CleanUpResult errorResult = null;
				
				if (resource == null) {
					msg += "original resource at " + path;
					errorResult = new CleanUpResult(false, msg);
				} else if (content == null) {
					msg += "content of resource";
					errorResult = new CleanUpResult(false, msg);
				} else if (destination == null) {
					msg += "destination at " + destinationPath;
					errorResult = new CleanUpResult(false, msg);
				}
				
				if (errorResult != null) {
					renderer.renderConvertFault(request, response, errorResult);
					return;
				}
				
				log.debug("[doConvertSelector] coerce nodes");
				Node resourceNode = resource.adaptTo(Node.class);
				Node contentNode = content.adaptTo(Node.class);
				Node destinationNode = destination.adaptTo(Node.class);
				
				log.debug("[doConvertSelector] copy resource ot backup {}", destinationNode.getPath());
				JcrUtil.copy(resourceNode, destinationNode, resource.getName());
			    
				session.save();
				
				log.debug("[doConvertSelector] Remove content node from {}", resourceNode.getPath());
				contentNode.remove();
				
				session.save();
				
				log.debug("[doConvertSelector] Begin copying backup of jcr:content");
				content = destination.getChild(resource.getName()).getChild(JcrConstants.JCR_CONTENT);
			    contentNode = content.adaptTo(Node.class);
			    
			    log.debug("[doConvertSelector] Create new jcr:content node");
			    JcrUtil.createPath(contentPath, JcrConstants.NT_UNSTRUCTURED, PAGE_CONTENT_PRIMARY_TYPE, session, true);
			    
			    log.debug("[doConvertSelector] Get new node and resource");
			    Resource clone = resolver.getResource(contentPath);
				Node cloneNode = clone.adaptTo(Node.class);
				
				
			    log.debug("[doConvertSelector] Copy properties");
				PropertyIterator props = contentNode.getProperties();
				
				while (props.hasNext()) {
					javax.jcr.Property property = props.nextProperty();
					String propertyName = property.getName();
					if (propertyName.contentEquals("jcr:isCheckedOut") || 
						propertyName.contentEquals("jcr:mixinTypes") ||
						propertyName.contentEquals("jcr:created") ||
						propertyName.contentEquals("jcr:primaryType") ||
						propertyName.contentEquals("jcr:createdBy")) {
						continue;
					}
					log.debug("[doConvertSelector] Copy property *{}*", propertyName);
					try {
						if (property.isMultiple()) {
							cloneNode.setProperty(propertyName, property.getValues());
						} else {
							cloneNode.setProperty(property.getName(), property.getValue());
						}
						
					} catch (Exception e) {
						log.info("The property, " + property.getName() + 
								", did not copy.");
					}
					
				}
				
				session.save();
				
				log.debug("[doConvertSelector] Copy child nodes");
				NodeIterator children = contentNode.getNodes();
				
				while(children.hasNext()) {
					Node child = children.nextNode();
					log.debug("[doConvertSelector] Copying child: {}", child.getName());
					JcrUtil.copy(child, cloneNode, child.getName());
				}
				
				log.debug("[doConvertSelector] add mixins");
				Value[] mixins = null;
				try {
					mixins = contentNode.getProperty(JcrConstants.JCR_MIXINTYPES).getValues();
				} catch (PathNotFoundException e1) {
					log.debug("Content does not have mixins");
				}
				for (int i = 0; i < mixins.length; i++) {
					cloneNode.addMixin(mixins[i].getString());
				}
								
				session.save();
				
				log.debug("[doConvertSelector] Successfully cleaned {}", resource.getName());
			    result = new CleanUpResult(true, "Success", resource, destination);
			    
			    renderer.renderConvertSuccess(request, response, result);
			    return;
			    
			} else {
				String err = "Path of page to clean is null.";
				log.error("[doConvertSelector] " + err);
				result = new CleanUpResult(false, err);
			}
	    } catch (RepositoryException e) {
			String err = "RepositoryException: " + e.getMessage();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			PrintStream ps = new PrintStream(baos);
			ps.append(err);
			ps.append("/n");
			e.printStackTrace(ps);
			log.warn("[doConvertSelector] " + baos.toString());

	    	result = new CleanUpResult(false, baos.toString(), resource, destination);
	    	
	    	
	    } 
		
		
		renderer.renderConvertFault(request, response, result);

    }
	
	
	
	private void doRevertChangeSelector(SlingHttpServletRequest request, 
            SlingHttpServletResponse response) 
           		 throws IOException
    {
		renderer.renderRevertChange(request, response, null);
    }
	
	private void doCommitChangeSelector(SlingHttpServletRequest request, 
            SlingHttpServletResponse response) 
           		 throws IOException
    {
		renderer.renderCommitChange(request, response, null);
    }
	
	class CleanUpResult {
		
		public final static String SUCCESS = "success";
		public final static String FAULT = "fault";
		
		private String message = "";
		private String type = SUCCESS;
		
		private String cleanedPage = "";
		private String backupDirectory = "";

		
		public CleanUpResult(boolean success, String message) {
			setMessage(message);
			String myStatus = (success) ? SUCCESS : FAULT;
			setType(myStatus);
		}
		
		public CleanUpResult(String type, String message) {
			setType(type);
			setMessage(message);
		}
		
		public CleanUpResult(boolean success, String message, 
				Resource cleanedPage,
				Resource backupDirectory) {
			setMessage(message);
			String myStatus = (success) ? SUCCESS : FAULT;
			setType(myStatus);
			if (cleanedPage != null) setCleanedPage(cleanedPage.getPath());
			if (backupDirectory != null) setBackupDirectory(backupDirectory.getPath());
		}

		
		public CleanUpResult() {
			setType(SUCCESS);
			setMessage("Page cleanup completed successfully.");
		}
		
		public String getMessage() {
			return message;
		}
		
		public void setMessage(String value) {
			message = value;
		}
		
		public void setMessage(String format, Object... arguments) {
			String[] parts = format.split("[{][}]");
			message = "";
			
			for (int i = 0; i < parts.length; i++) {
				message += parts[i];
				if (arguments[i] != null) {
					message += arguments[i].toString();
				}
			}
		}
		
		public String getType() {
			return type;
		}
		
		public void setType(String value) {
			type = value;
		}
		
		public String getCleanedPage() {

			return cleanedPage;
		}
		public void setCleanedPage(String value) {
			log.debug("set cleanedPage="+value);
			cleanedPage = value;
		}
				
		public String getBackupDirectory() {

			return backupDirectory;
		}
		public void setBackupDirectory(String value) {
			backupDirectory = value;
		}
		

	}


	
}



