<html>
  <body>
<%
	javax.servlet.http.HttpSession ses = session;
%>
<%
    if (ses != null)
        out.println("session set");
%>
  </body>
</html>
