<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.ArrayList"%>
<%@page session="false"
            contentType="text/html; charset=utf-8" %><% 
%><%@include file="/libs/wcm/global.jsp"%><cq:defineObjects/>
<!DOCTYPE html>
<html>
<head><title>Page Content Node Clean Up Successful</title>
<cq:includeClientLib categories="daycare.page-content-cleanup"/>
</head>
<body>
<div class="container_12">
	<div class="mast">&nbsp;</div>
	<div class="grid_12"><h1>Page Content Node Clean Up Successful</h1></div>
	<div class="clear"></div>
	<div class="grid_12">
<% 
String nodeChanged = request.getParameter("nodes-to-change");

String rootPath = request.getParameter("rootPath");

Object resultAttr = request.getAttribute("message");
String resultString = (resultAttr != null) ? resultAttr.toString() : "";

Object backupAttr = request.getAttribute("backupDirectory");
String backupDirectory = (backupAttr != null ) ? backupAttr.toString() : "";

Object originalAttr = request.getAttribute("original");
String originalContent = (originalAttr != null) ? originalAttr.toString() : "";

%>
<ul>
<li>Cleaned Page: <a href="/crx/de/index.jsp#/crx.default/jcr%3aroot<%= originalContent %>" target="_blank"><%= originalContent %></a></li>
<li>Backup: <a href="/crx/de/index.jsp#/crx.default/jcr%3aroot<%= backupDirectory %>" target="_blank"><%= backupDirectory %></a></li>
</ul>
<p><%= resultString %>.</p>
<p><form action="cleanup.html?rootPath=<%= URLEncoder.encode(rootPath) %>" method="GET">
	<input type="submit" value="Return">
</form></p>
	</div>
	<div class="clear"></div>
	<div class="grid_12">
	<p class="page-footer">&copy; 2013 Adobe Systems, Inc.</a> This tool is provided as-is. User assumes all risk.</p>
	</div>

</div>



</body></html>


