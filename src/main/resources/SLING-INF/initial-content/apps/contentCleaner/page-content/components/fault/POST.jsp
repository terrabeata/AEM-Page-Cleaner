<%@page import="java.net.URLEncoder"%>
<%@page import="java.util.ArrayList"%>
<%@page session="false"
            contentType="text/html; charset=utf-8" %><% 
%><%@include file="/libs/wcm/global.jsp"%><cq:defineObjects/>

<!DOCTYPE html>
<html>
<head><title>Page Content Node Clean Up Failed</title>
<cq:includeClientLib categories="daycare.page-content-cleanup"/>
</head>
<body>
<div class="container_12">
	<div class="mast">&nbsp;</div>
	<div class="grid_12"><h1>Page Content Node Clean Up Failed</h1></div>
	<div class="clear"></div>
	<div class="grid_12">
<% 
String rootPath = request.getParameter("rootPath");

String nodeChanged = request.getParameter("nodes-to-change");
Object resultStrAttr = request.getAttribute("message");
String resultString = (resultStrAttr != null) ? resultStrAttr.toString() : "";

Object backupAttr = request.getAttribute("backupDirectory");
String backupDirectory = (backupAttr != null) ? backupAttr.toString() : "";

Object originalAttr = request.getAttribute("original");
String original = (originalAttr != null) ? originalAttr.toString() : "";
%>

<ul>
<li>Backup Directory: <a href="/crx/de/index.jsp#/crx.default/jcr%3aroot<%= backupDirectory %>" target="_blank"><%= backupDirectory %></a></li>
<li>Original Content: <a href="/crx/de/index.jsp#/crx.default/jcr%3aroot<%= original %>" target="_blank"><%= original %></a></li>
</ul>
<p><form action="cleanup.html?rootPath=<%= URLEncoder.encode(rootPath)  %>" method="GET">
	<input type="submit" value="Return">
</form></p>
<pre><%= resultString %></pre>
	</div>
	<div class="clear"></div>
	<div class="grid_12">
	<p class="page-footer">&copy; 2013 Adobe Systems, Inc.</a> This tool is provided as-is. User assumes all risk.</p>
	</div>

</div>



</body></html>








