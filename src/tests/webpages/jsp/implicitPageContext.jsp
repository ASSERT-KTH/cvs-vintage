<html>
  <body>
<%
	javax.servlet.jsp.PageContext pcon = pageContext;
%>
<%
    if (pcon != null)
        out.println("pageContext set");
%>
  </body>
</html>
