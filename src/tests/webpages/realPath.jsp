<%
String vPath = request.getRequestURI();
String rPath = getServletConfig().getServletContext().getRealPath(vPath);
%>
<html>

<head>
<title>
getRealPath() test
</title>

<!-- Changed by: Costin Manolache, 16-Mar-2000 -->
</head>
<body>
<p>The virtual path is <%=vPath%></p>
<p>The real path is <%=rPath%></p>
<p>The real path is <%= getServletConfig().getServletContext().getRealPath("/realPath.jsp") %></p>
</body>
</html>