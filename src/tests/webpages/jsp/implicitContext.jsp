<html>
  <body>
<%
	javax.servlet.ServletContext con = application;
%>
<%
    if (con != null)
        out.println("context set");
%>
  </body>
</html>
