<html>
  <body>
<%
	javax.servlet.http.HttpServletResponse res = response;
%>
<%
    if (res != null)
        out.println("response set");
%>
  </body>
</html>
