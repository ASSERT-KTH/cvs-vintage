<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="junit.framework.Assert"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<%
String server = request.getServerName();
int port      = request.getServerPort();
String portString = (port == 80 ? "" : ":" + port);
%>


<logic:equal name="runTest" value="testBase">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:base/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<base href="http://<%=server%><%=portString%><%= request.getContextPath()  %>/test/org/apache/struts/taglib/html/TestBaseTag.jsp">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testBaseTarget">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:base target="My-Other-Frame"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<base href="http://<%=server%><%=portString%><%= request.getContextPath()  %>/test/org/apache/struts/taglib/html/TestBaseTag.jsp" target="My-Other-Frame">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testBaseServer">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:base server="www.my-server-name.com"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<base href="http://www.my-server-name.com<%=portString%><%= request.getContextPath()  %>/test/org/apache/struts/taglib/html/TestBaseTag.jsp">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testBaseServerTarget">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:base server="www.my-server-name.com" target="My-Other-Frame"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<base href="http://www.my-server-name.com<%=portString%><%= request.getContextPath()  %>/test/org/apache/struts/taglib/html/TestBaseTag.jsp" target="My-Other-Frame">
	</bean:define>
</logic:equal>


<% 
String expected = "";
String compareTo = "";

if (pageContext.getAttribute("EXPECTED_RESULTS") == null){
    throw new javax.servlet.jsp.JspException("No tests on this page were called.  Please verify that you've setup the tests correctly.");
}else{
	expected=pageContext.getAttribute("TEST_RESULTS").toString();
}
if (pageContext.getAttribute("TEST_RESULTS") != null){
	compareTo=pageContext.getAttribute("EXPECTED_RESULTS").toString();
}

Assert.assertEquals(expected, compareTo);
%>
