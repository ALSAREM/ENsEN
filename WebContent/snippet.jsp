<%@page import="ensen.util.PropertiesManager"%>
<%@page import="java.util.TreeMap"%>
<%@page import="ensen.entities.Query"%>
<%@page import="ensen.entities.Concept"%>
<%@page import="ensen.entities.Document"%>
<%@page import="java.util.ArrayList"%>
<%@page import="ensen.entities.Group"%>
<%@page import="ensen.controler.DocumentAnalyzer"%>
<%@page import="java.util.List"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@page import="com.google.api.services.customsearch.model.Result"%>
<%
	try{		
	HttpSession ensenSession = request.getSession();
	int id = Integer.parseInt(request.getParameter("id"));//documentID
	long sessionId = Long.parseLong(request.getParameter("sessionId"));//sessionId
	List<Result> Results = (List<Result>) ensenSession.getAttribute("Results"+sessionId);		
	String sessionName="documents"+sessionId;	
	DocumentAnalyzer analyzer = new DocumentAnalyzer();	
	TreeMap <Integer,Document> documents = (TreeMap <Integer,Document>) ensenSession.getAttribute(sessionName);
	if(documents==null)
		documents=new TreeMap <Integer,Document>();	
	Query query= (Query) ensenSession.getAttribute("query"+sessionId);
	analyzer.tryNumber=1;
	analyzer.run(Results.get(id),id,query);		
	
	if(analyzer.nosnippet){
		if(analyzer.Doci.originalText.length()>analyzer.toBeAnnotatedTextLen)
		{
			System.err.println("No snippet but more text, retry with text*3");	
			analyzer.retryFactor=3;		
		}else{
			System.err.println("No snippet but more text, retry with conf = 0.0");
			analyzer.selectedConfiance="0.0";
			
		}
		analyzer.tryNumber=2;
		analyzer.run(Results.get(id),id,query);	
	}
	
	if(analyzer.nosnippet){	
		System.err.println("No snippet 2 but more text, retry with text*3&con=0.0");		
		System.err.println("old text length: "+analyzer.toBeAnnotatedTextLen);
		analyzer.tryNumber=3;
		analyzer.retryFactor=3;	
		analyzer.selectedConfiance="0.0";
		analyzer.run(Results.get(id),id,query);	
		System.err.println("new text length: "+analyzer.toBeAnnotatedTextLen);	
	}
	
	if(!analyzer.nosnippet){	
		documents.put(id,analyzer.Doci);	
		ensenSession.setAttribute(sessionName, documents);
%>
<script type='text/javascript'>
$(function() {	
	
	$("#bubble<%=id %>").show(1000);
	
	$("#rdfDialog<%=id%>" ).dialog({
		modal: true,
		width: 1000,
		autoOpen: false,
		position:['middle',20],
		open: function() {		
        $('.ui-widget-overlay').bind('click', function() {
            $('#rdfDialog<%=id%>').dialog('close');
        });        
        }
    });
	$("#rdfOpener<%=id%>").click(
			function() {
				$("#rdfDialog<%=id%>").dialog("open");		
				
				/* $("#graph<%=id%>").load("RDFvisualizer.jsp?i=<%=id%>");					
				$("#graph<%=id%>").find("script").each(function(i) { eval($(this).text());});*/				
				$("#graph<%=id%>").html("<iframe src=\"RDFvisualizer.jsp?i=<%=id%>&sessionId=<%=sessionId%>\" style=\"width: 100%;height: 800px;\" ></iframe>");
				});	
	
	});
  
prepareConcepts(<%=id%>, 1);
</script>
<div class="bubble"  id="bubble<%=id %>" style="display: none">
	<div class="rectangle">
		<h2>
			<a href="<%out.print(Results.get(id).getLink());%>" target="_blank"> <%
 	out.print(Results.get(id).getTitle());
 %>
			</a>
		</h2>
	</div>
	<div class="triangle-l"></div>
	<div class="triangle-r"></div>
	<div class="bubbleinfo">
		<div class="topLinks">
			<%
			if(analyzer.wikis!=null)	
			for (int i = 0; i < analyzer.wikis.size(); i++) {
			%>
			<div class="share-wrapper left">
				<a href="<%=analyzer.wikis.get(i)%>" target="_blank" class="rc10 share-action"><%=analyzer.wikisLabels.get(i)%>   | </a>
				<div class="share-container rc10">
					<img style="padding: 5px;" alt="" src="<%=analyzer.wikisPhotos.get(i)%>" width="85" height="85" />
				</div>
			</div>
			<%
				}
			%>
		</div>
		<div id="mainPh" class="mainPhCotation">
			<blockquote><% 
			if(analyzer.mainPh!=null && analyzer.mainPh!="")
				out.print(analyzer.mainPh);
			else
				{
					/*out.print("Here will be the main sentence!");
					 throw new Exception();*/
				}
			%></blockquote>
		</div>
	
		<div id="snippet" class="mainSnippet">
			<p>
				<%
					if(analyzer.seSnippet!=null) out.print(analyzer.seSnippet);
				%>
			</p>
			<hr />
			<ul class='flips'>
				<%
					
				if(analyzer.Doci.concepts!=null)		
				for (int cIndex= 0;cIndex <Math.min(analyzer.Doci.concepts.size(),analyzer.MaxNumOfConcepts);cIndex++) {
					Concept con=analyzer.Doci.concepts.get(cIndex);
					if(con.annotated){						
				%>
				<li id='D<%=id%>V<%=cIndex%>' >
				<div class="flip flip<%=id%>" id='opener<%=id%>concept<%=cIndex%>'>					
				<div class="box box5">	
					<div class="groupTitle" ><%=con.name%></div>
					<hr />								
					<div class="rotate">					
						<img class="thumb" src="<%=con.image%>" src2="<%=con.image2%>" alt="<%=con.name%>" width="159" height="110">
						<span class="caption rotate-caption">							
							<%
							if(con.abstractTxt!=null && con.abstractTxt!="")
								out.print(con.abstractTxt.substring(0,Math.min(con.abstractTxt.length(), 172))+"...");
							else
								{
								
								}
							 %>...
						</span>
					</div>
				</div>
				</div>	
	
				  <script type="text/javascript">
					$(function() {
						prepareDialog(<%=analyzer.Doci.Rank%>,<%=cIndex%>,<%=sessionId%>);
						});
					</script>
					<div id="dialog<%=id%>V<%=cIndex%>" title="<%=con.name%>" class="snippetDialog">
						<div id="dialog<%=id%>V<%=cIndex%>Content" width="100%"><center><img alt="Loading..." src="./images/loading/82.GIF" style="width: 120px;"/></center></div>
					</div>				
				</li>
				
				<%
					//cIndex++;
							}
					}
				%>
			</ul>			
		</div>
	</div>
	<div class="buttonLinks">
		<a target="blank" href="./getCinput.jsp?id=<%=id%>&sessionId=<%=sessionId%>">C</a><a target="blank" href="./getRDF.jsp?id=<%=id%>&sessionId=<%=sessionId%>"> RDF</a>
		<img alt="Show RDF Graph" src="./images/rdf.png" id="rdfOpener<%=id%>" width='16px' height='16px' />
		<div id="rdfDialog<%=id%>" Title="RDF graph for <%=analyzer.Doci.content.getTitle()%>">
			<div id="graph<%=id%>">Please wait, loading...</div>
		</div>
		<%
		if(analyzer.links!=null)	
			for (int i = 0; i < analyzer.links.size(); i++) {
		%>
		<a href="<%=analyzer.links.get(i)%>" target="_blank" class="rc10 share-action"><%=analyzer.linksLabels.get(i)%> | </a>
		<%
			}
		%>
	</div>
</div>
<%
	}else{
		out.print("<br/><hr/><center><b class=\"isa_error err\">|Not enough information| for "+analyzer.Doci.content.getTitle()+"</b></center>");
	}
	} catch (Exception e) {
		e.printStackTrace();		
		out.print("<br/><hr/><center><b class=\"isa_error err\">|Not enough information|</b></center>");
	}
%>