<%@ page isErrorPage="true" %>
<html>
  <body>
<%
	java.lang.Throwable exc = exception;
%>
<%
    if (exc != null)
        out.println("exception set");
     else 
        out.println("exception not set");
%>
  </body>
</html>
