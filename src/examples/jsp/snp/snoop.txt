<html>
<!--
  Copyright (c) 1999 The Apache Software Foundation.  All rights 
  reserved.
-->

<body bgcolor="white">
<h1> Request Information </h1>
<font size="4">
JSP Request Method: <jsp:expr>  request.getMethod() </jsp:expr>
<br>
Request URI: <jsp:expr> request.getRequestURI()  </jsp:expr>
<br>
Request Protocol: <jsp:expr> request.getProtocol() </jsp:expr>
<br>

Servlet path: <jsp:expr> request.getServletPath() </jsp:expr>
<br>
Path info: <jsp:expr> request.getPathInfo() </jsp:expr>
<br>
Path translated: <jsp:expr> request.getPathTranslated() </jsp:expr>
<br>
Query string: <jsp:expr> request.getQueryString() </jsp:expr>
<br>
Content length: <jsp:expr> request.getContentLength() </jsp:expr>
<br>
Content type: <jsp:expr> request.getContentType() </jsp:expr>
<br>
Server name: <jsp:expr> request.getServerName() </jsp:expr>
<br>
Server port: <jsp:expr> request.getServerPort() </jsp:expr>
<br>
Remote user: <jsp:expr> request.getRemoteUser() </jsp:expr>
<br>
Remote address: <jsp:expr> request.getRemoteAddr() </jsp:expr>
<br>
Remote host: <jsp:expr> request.getRemoteHost() </jsp:expr>
<br>
Authorization scheme: <jsp:expr> request.getAuthType() </jsp:expr> 
<hr>
The browser you are using is <jsp:expr> request.getHeader("User-Agent") </jsp:expr>
<hr>
</font>
</body>
</html>
