<form method="GET" action="test.jsp" >
Target:
<select name="target" > 
  <option>file</option>
  <option>params</option>
  <option>dispatch</option>
  <option>get</option>
  <option>requestMap</option>
  <option>post</option>
  <option>wrong_request</option>
  <option>unavailable</option>
  <option>restricted</option>
  <option>jsp</option>
  <option>special</option>
  <option>tomcat-standalone</option>
  <option>tomcat-apache</option>
  <option selected>client</option>
</select>
<br>

Debug: <input type="checkbox" name="debug" value="10"><br>
Port: <input type="input" name="port" value="<%= request.getServerPort() %>">
<br>
Host: <input type="input" name="host" value="<%= request.getServerName() %>">
<br>
Expected protocol: 
<select name="server.proto" > 
  <option selected>HTTP/1.0</option>
  <option>HTTP/1.1</option>
</select>
 ( use HTTP/1.1 when testing with Apache or a 1.1 server connector ) <br>
<input type="submit">
</form>
