<HTML>
<HEAD>
<TITLE>JSP/Servlet test page</TITLE>
<!-- Changed by: Costin Manolache, 21-Mar-2000 -->
<%@ page session="true" import="java.util.Date" %>
</HEAD>
<BODY>
<H1>JSP bean test page</H1>
<% String appstr = ( String ) session.getValue( "APP" );
   if ( appstr == null ) appstr="Didn't work"; 
%>

The value of the string from the session is <% out.println( appstr ); %>
<p>
The value of the session id is: <% out.println( session.getId() ); %>
</p>
</BODY>
</HTML>