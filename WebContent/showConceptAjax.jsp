<%@page import="java.util.TreeMap"%>
<%@page import="ensen.entities.Concept"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%
HttpSession ensenSession = request.getSession();
long sessionId = Long.parseLong(request.getParameter("sessionId"));//sessionId
TreeMap <Integer,Document>  documents = (TreeMap <Integer,Document> ) ensenSession.getAttribute("documents"+sessionId);
int id = Integer.parseInt(request.getParameter("i"));
int cid = Integer.parseInt(request.getParameter("c"));
Document doc=documents.get(id);
Concept con=doc.concepts.get(cid);
con.buildSnippet();

%>    
   
<a href='<%=con.wikiURL%>' target='_blank'>							
	<img src="./images/link.png" alt="<%=con.wikiURL%>" width="16" height="16" style='cursor: pointer; float: right;'>								
</a>
<h5 class="flipsh5">
	<img class="thumb" src="<%=con.image%>" src2="<%=con.image2%>" alt="<%=con.name%>" width="200" height="200">	
	
</h5>
<%
if(con.abstractTxt!=null && con.abstractTxt!="" && con.abstractTxt.length()>10) {%>
	<fieldset class="groupAbstract" style="border-color: #AF3104;min-height: 210px;">
		<legend style="color: #AF3104;">Abstract</legend>
		<%=con.getAbstract()%>...
	</fieldset>
<% 
}
	
if(con.mainPhHtml.length()>10){ %>	
	<fieldset class="groupMainph" style=" width: 100%;">
		<legend style="color: #AF3104;">In the document</legend>
		<%=con.mainPhHtml%>
		<a href="viewer.jsp?url=<%=doc.content.getLink()%>&sent=<%=con.mainPh%>" target="_blank"><img src="images/show_property.png" width="24"/></a>
		<hr />
		<%if(con.mainPh2!=""){ %>
		<%=con.mainPhHtml2%>
		<a href="viewer.jsp?url=<%=doc.content.getLink()%>&sent=<%=con.mainPh2%>" target="_blank"><img src="images/show_property.png" width="24"/></a>
		<hr />
	
		<%} %>
		<div class="colorLegend">
		<span style="color: #10B1CF;font-size: 20px;">□ </span><span class="mainRes">this Concept </span> |  
		<span style="color: #EC2828;font-size: 20px;">□ </span><span class="qRes"> related to the query </span> |  
		<span style="color: #44A240;font-size: 20px;">□ </span><span class="resInSent"> main Concepts</span>
		</div>
	</fieldset>
	<%
}
%>

<hr />
<div class="conceptDescription">
<%=con.Descs%>
</div>
<% if(con.mapLink!=null){ %>
	<div class="conceptmapdiv"><a href="http://maps.google.com/maps?q=<%=con.name %>" target="_blank"><img src="<%=con.mapLink%>"/></a></div>
<% } %>
<% if(con.photos!=null&&con.photos.size()>1){ %>
	
	
	<fieldset class="conceptphotos">
	<legend style="color: #AF3104;">Gallery</legend>
		<% 
			for(String photo:con.photos)
				out.print("<a href='"+photo+"' target='_blanck'><img src='"+photo+"' /></a>");
		%>

	</fieldset>
<% } %>
