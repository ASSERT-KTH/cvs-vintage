<h1>Tomcat Self-test</h1> 

<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>

This page will show the result of executing the sanity test suite. 
You can see the context log <a href="/test/context_log.txt">here</a>

<form method="GET" action="test.jsp" >
Target:
<select name="target" > 
  <option>new-style</option>
  <option>file-tomcat</option>
  <option>dispatch-tomcat</option>
  <option>get-tomcat</option>
  <option>requestMap</option>
  <option>post</option>
  <option>jsp-tomcat</option>
  <option>wrong_request</option>
  <option>unavailable</option>
  <option>restricted</option>
  <option selected>client</option>
</select>
<br>

Debug: <input type="checkbox" name="debug" value="10"><br>

<input type="submit">
</form>

<% // This is an ugly hack to make the logs easily accessible.
   // Keep in mind this is just a way to jump-start testing, not a 
   // production-quality test runner.
%>

<% out.flush(); 
   if( request.getParameter("target") == null ) return;
%>

<adm:admin ctxPath="/test" 
	   action="setLogger" 
	   value="webapps/test/context_log.txt" />


<adm:gtest testFile="WEB-INF/test-tomcat.xml" 
	   testApp="/test" 
	   target='<%= request.getParameter("target") %>' 
           debug='<%= request.getParameter("debug") %>' />

