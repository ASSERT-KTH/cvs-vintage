<html>
  <body>
<%
	javax.servlet.jsp.JspWriter jout = out;
%>
<%
    if (jout != null)
        jout.println("out set");
%>
  </body>
</html>
