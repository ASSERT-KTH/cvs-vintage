<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="junit.framework.Assert"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>


<logic:equal name="runTest" value="testSelectPropertyStyle">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:select  property="string" style="Put something here">Some Body Here</html:select>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<select name="string" style="Put something here">Some Body Here</select>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyStyleClass">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:select  property="string" styleClass="Put something here">Some Body Here</html:select>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<select name="string" class="Put something here">Some Body Here</select>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyStyleId">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:select  property="string" styleId="Put something here">Some Body Here</html:select>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<select name="string" id="Put something here">Some Body Here</select>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyTitle">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:select  property="string" title="Put something here">Some Body Here</html:select>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<select name="string" title="Put something here">Some Body Here</select>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyTitleKey">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:select  property="string" titleKey="default.bundle.message">Some Body Here</html:select>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<select name="string" title="Testing Message">Some Body Here</select>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyTitleKey_fr">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:select  property="string" titleKey="default.bundle.message">Some Body Here</html:select>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<select name="string" title="Message D'Essai">Some Body Here</select>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectTabindex">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:select  property="string" tabindex="8">Some Body Here</html:select>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<select name="string" tabindex="8">Some Body Here</select>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectSize">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:select  property="string" size="5">Some Body Here</html:select>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<select name="string" size="5">Some Body Here</select>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyValue">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:select  property="string">Some Body Here</html:select>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<select name="string">Some Body Here</select>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyIndexedArray">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst">
		<html:select  property="string" indexed="true">Some Body Here</html:select>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<select name="org.apache.struts.taglib.html.BEAN[0].string">Some Body Here</select>
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyIndexedArrayProperty">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst" property="list">
		<html:select property="string" indexed="true">Some Body Here</html:select>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<select name="org.apache.struts.taglib.html.BEAN[0].string">Some Body Here</select>
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyIndexedMap">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst">
		<html:select  property="string" indexed="true">Some Body Here</html:select>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<select name="org.apache.struts.taglib.html.BEAN[0].string">Some Body Here</select>
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyIndexedMapProperty">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst" property="map">
		<html:select  property="string" indexed="true">Some Body Here</html:select>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<select name="org.apache.struts.taglib.html.BEAN[0].string">Some Body Here</select>
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyIndexedEnumeration">
	<bean:define id="TEST_RESULTS" toScope="page">
	<logic:iterate id="indivItem" name="lst">
		<html:select  property="string" indexed="true">Some Body Here</html:select>
	</logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<select name="org.apache.struts.taglib.html.BEAN[0].string">Some Body Here</select>
	
		<select name="org.apache.struts.taglib.html.BEAN[1].string">Some Body Here</select>
	
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testSelectPropertyIndexedEnumerationProperty">
	<bean:define id="TEST_RESULTS" toScope="page">
	<logic:iterate id="indivItem" name="lst" property="enumeration">
		<html:select  property="string" indexed="true">Some Body Here</html:select>
	</logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<select name="org.apache.struts.taglib.html.BEAN[0].string">Some Body Here</select>
	
		<select name="org.apache.struts.taglib.html.BEAN[1].string">Some Body Here</select>
	
	</bean:define>
</logic:equal>



<% 
String expected = "";
String compareTo = "";

if (pageContext.getAttribute("EXPECTED_RESULTS") == null){
    throw new javax.servlet.jsp.JspException("No tests on this page were called.  Please verify that you've setup the tests correctly.");
}else{
	expected=pageContext.getAttribute("EXPECTED_RESULTS").toString();
}
if (pageContext.getAttribute("TEST_RESULTS") != null){
	compareTo=pageContext.getAttribute("TEST_RESULTS").toString();
}

Assert.assertEquals(expected, compareTo);
%>
