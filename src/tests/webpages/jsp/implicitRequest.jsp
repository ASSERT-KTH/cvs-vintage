<html>
  <body>
<%
	javax.servlet.http.HttpServletRequest req = request;
%>
<%
    if (req != null)
        out.println("request set");
%>
  </body>
</html>
