<html>
  <body>
<%
	javax.servlet.ServletConfig tcon = config;
%>
<%
    if (tcon != null)
        out.println("config set");
%>
  </body>
</html>
