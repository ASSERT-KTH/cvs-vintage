<%
  String redirect = request.getScheme() + "://" + request.getServerName();
  if (request.getServerPort() != 80)
  {
    redirect += ":" + request.getServerPort();
  }
  response.sendRedirect(response.encodeRedirectURL(redirect + "@SCARAB_HTML_REDIRECT@"));
%>

