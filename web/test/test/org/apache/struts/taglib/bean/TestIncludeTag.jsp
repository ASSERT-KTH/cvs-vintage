<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="junit.framework.Assert"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<logic:equal name="runTest" value="testIncludeTagForward">
	<bean:define id="TEST_RESULTS" toScope="page">
		<bean:include id="INCLUDE_TAG_KEY" forward="testIncludeTagForward"/>
		<bean:write name="INCLUDE_TAG_KEY"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		Test Value
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testIncludeTagHref">
	<bean:define id="TEST_RESULTS" toScope="page">
		<bean:include id="INCLUDE_TAG_KEY" href="<%=request.getContextPath() + "/test/org/apache/struts/taglib/bean/resources/IncludeTagTest.jsp"%>"/>
		<bean:write name="INCLUDE_TAG_KEY"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		Test Value
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testIncludeTagPage">
	<bean:define id="TEST_RESULTS" toScope="page">
		<bean:include id="INCLUDE_TAG_KEY" page="/test/org/apache/struts/taglib/bean/resources/IncludeTagTest.jsp"/>
		<bean:write name="INCLUDE_TAG_KEY"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		Test Value
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
