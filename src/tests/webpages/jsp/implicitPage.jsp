<html>
  <body>
<%
	java.lang.Object obj = page;
%>
<%
    if (obj == this)
        out.println("page set");
%>
  </body>
</html>
