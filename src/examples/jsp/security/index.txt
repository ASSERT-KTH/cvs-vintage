<html>
<head>
<title>Protected Area Page</title>
</head>
<body bgcolor="white">

You are logged in as remote user <b><%= request.getRemoteUser() %></b><br><br>

<%
  if (request.getUserPrincipal() != null) {
%>
    Your user principal name is <b><%= request.getUserPrincipal().getName() %></b><br><br>
<%
  } else {
%>
    No user principal could be identified.
<%
  }
%>

<%
  String role = request.getParameter("role");
  if (role == null)
    role = "";
  if (role.length() > 0) {
    if (request.isUserInRole(role)) {
%>
      You have been granted role <b><%= role %></b><br><br>
<%
    } else {
%>
      You have <i>not</i> been granted role <b><%= role %></b><br><br>
<%
    }
  }
%>

To check whether your username has been granted a particular role,
enter it here:
<form method="GET">
<input type="text" name="role" value="<%= role %>">
</form>

</body>
</html>
