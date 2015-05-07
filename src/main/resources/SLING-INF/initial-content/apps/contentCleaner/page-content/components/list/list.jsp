<%@page import="java.util.ListIterator"%>
<%@page import="java.util.ArrayList"%>
<%@page session="false"
            contentType="text/html; charset=utf-8" %><%
%><%@include file="/libs/wcm/global.jsp"%><cq:defineObjects/>
<!DOCTYPE html>
<html>
<head><title>Page Content Node Clean Up</title>
<cq:includeClientLib categories="daycare.page-content-cleanup"/>
</head>
<body>
<div class="container_12">
	<div class="mast">&nbsp;</div>
	<div class="grid_12"><h1>Page Content Node Clean Up</h1></div>
	<div class="clear"></div>
	<div class="grid_12">
	
	<p>All of the pages below have content nodes of type nt:unstructured. Select a node to change its content from nt:unstructured to cq:PageContent. Only one node can be converted at a time.</p>
	
	<h3>Warning</h3>
	<p>This software has not been fully tested and you should test on a test server first. USE AT YOUR OWN RISK. Be sure to have a backup available to restore from in case this process causes unforeseen problems.</p>
	<form name="root-path-form" action="/apps/daycare/page-content/cleanup.rootpath.html" method="get">
<%
String rootPath = request.getAttribute("rootPath").toString();
%>
	<input type="text" name="rootPath" value="<%= rootPath %>"/><input type="submit" value="Change Path" />
	</form>
<% 
ArrayList<String> paths =  (ArrayList<String>) request.getAttribute("hit-paths");

if (paths != null ) {

ListIterator<String> listIterator = paths.listIterator();
%>
	<form class="item_form" name="node-list" action="/apps/daycare/page-content/cleanup.convert.html" method="post">
		<input type="submit" value="Clean Page" />
		<div class="item_list">
<%
while(listIterator.hasNext())
{
	String pathName = listIterator.next();
	%>
	<input type="radio" name="nodes-to-change" value="<%= pathName %>"/><a href="/crx/de/index.jsp#/crx.default/jcr%3aroot<%= pathName%>" target="_blank"><%= pathName%></a><br/><%
}
%>
		</div>
		<input type="hidden" name="rootPath" value="<%= rootPath %>" />
		<input type="submit" value="Clean Page" />
	</form>	
<%
}
%>	
	</div>
	<div class="clear"></div>
	<div class="grid_12">
	<p class="page-footer">&copy; 2013 Adobe Systems, Inc.</a> This tool is provided as-is. User assumes all risk.</p>
	</div>

</div>



</body></html>
