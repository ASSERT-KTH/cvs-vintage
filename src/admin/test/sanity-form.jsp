<form method="GET" action="test.jsp" >
Target:
<select name="target" > 
  <option>file</option>
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
Expected protocol: <input type="input" name="server.proto" 
			  value="<%= request.getProtocol() %>">
 ( use when testing Apache - tomcat3.x returns HTTP/1.0 ) <br>
<input type="submit">
</form>
