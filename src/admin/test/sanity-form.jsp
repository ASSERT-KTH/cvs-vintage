<form method="GET" action="test.jsp" >
<%!
  void listOptions(String[] opts, String sel, JspWriter out)
      throws java.io.IOException
  {
    for ( int i=0; i < opts.length; i++ ) {
      if ( sel.equals(opts[i]) )
        out.println("  <option selected>" + sel + "</option>");
      else
        out.println("  <option>" + opts[i] + "</option>");
    }
  }
  String [] targets = new String [] {
      "file","params","dispatch","get","requestMap","post",
      "wrong_request","restricted","jsp","unavailable",
      "headers","enc","security_chk_url","security_chk",
      "aaa","special","client"};
  String [] protocols = new String [] {
      "HTTP/1.0","HTTP/1.1"};
  String [] webservers = new String [] {
      "Tomcat","Apache","IIS","Netscape" };
%>
<%
   String curTarget = request.getParameter("target");
   if (curTarget == null)
     curTarget = "client";
   String curProto = request.getParameter("server.proto");
   if ( curProto == null )
     curProto = "HTTP/1.0";
   String webServer = request.getParameter("web.server");
   if ( webServer == null )
     webServer = "Tomcat";
   String staticServer = request.getParameter("static.server");
   if ( staticServer == null )
     staticServer = "Tomcat";
%>
<p>Target:
<select name="target" ><% listOptions(targets, curTarget, out); %></select>
&quot;client&quot; is the target for the main test suite.</p>
<p>Host:
<input type="text" name="host" value="<%= request.getServerName() %>"></p>
<table cellspacing="2" >
<tr><th align="left">Parameter</th>
    <th align="left">Value</th>
    <th align="left">Description</th>
<tr><td valign="top">Host Web Server:</td>
    <td valign="top"><select name="web.server" >
        <% listOptions(webservers, webServer, out); %></select></td>
    <td valign="top">The host web server to test.</td></tr>
<tr><td valign="top">Host HTTP Port:</td>
    <td valign="top"><input type="text" name="port"
        value="<%= request.getServerPort() %>" size="10"></td>
    <td valign="top">HTTP port being used by the host web server.<br>
        Specify 8080 for Tomcat and 80 for others.</td></tr>
<tr><td valign="top">Expected protocol:</td>
    <td valign="top"><select name="server.proto" >
        <% listOptions(protocols, curProto, out); %></select></td>
    <td valign="top">Specify HTTP/1.0 for Tomcat as the host web server<br>
        Specify HTTP/1.1 for others.</td></tr>
<tr><td valign="top">Static Page Server: </td>
    <td valign="top"><select name="static.server" >
        <% listOptions(webservers, staticServer, out); %></select></td>
    <td valign="top">Server that serves static pages.<br>
        Specify Tomcat if using a generated configuration file with
        <code>forwardAll=&quot;true&quot;</code>.<br>
        Specify the host web server if
        <code>forwardAll=&quot;false&quot;.</code></td></tr>
</table>
<p>Debug:
<input type="checkbox" name="debug" value="10"></p>
<input type="submit">
</form>
