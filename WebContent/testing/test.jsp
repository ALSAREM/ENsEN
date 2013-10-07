<%@page import="ensen.entities.MultiZoneSnippet"%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <style type="text/css">          div{ border: thin rgb(0,0,255);}  
    .sn_media{  
    border: thin rgb(255,0,51);
    display: block;
    float: left;
    }    
    span{
    background-color: rgb(240,240,240);
    cursor: hand;
    }
    </style>  
<title>Insert title here</title>
</head>
<body>
    <div class="snippet">        
      <div class="sn_header">                   
        <h3> Here is the title  Here is the title Here is the title Here is the title</h3>      
      </div>        
      <div class="sn_body">          
        <div class="sn_media">
        <img src="http://1.bp.blogspot.com/-3XQ18R67TdQ/TsW5Yw-YU7I/AAAAAAAAAXY/a4a9ZzcgjKs/s320/PhotoFunia-7b3b24.jpg" alt="moiiii" title="Photo title" height="100" width="100"> 
        </div>           
        <div class="sn_text">          
          <div class="sn_modified_snippet">  
          		<%
          	/*	MultiZoneSnippet MZS= new MultiZoneSnippet();
          		out.print(MZS.testModfingSnpt());*/
          		%>	             
          </div>                       
          <div class="sn_new_snippet">  new snippet  new snippet  new snippet  new snippet  new snippet  new snippet          
          </div>          
        </div>         
        <div class="sn_links">           
          <a > link1 </a>   
          <a > link2 </a>  
          <a > link3 </a>  <a> link4 </a>        
        </div>        
      </div>              
    </div>  
</body>
</html>