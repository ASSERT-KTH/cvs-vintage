<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="junit.framework.Assert"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>


<logic:equal name="runTest" value="testTextareaPropertyReadonly">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:textarea  property="string" readonly="true"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<textarea name="string" readonly="readonly">Test Value</textarea>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyRows">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:textarea  property="string" rows="10"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<textarea name="string" rows="10">Test Value</textarea>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyStyle">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:textarea  property="string" style="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<textarea name="string" style="Put something here">Test Value</textarea>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyStyleClass">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:textarea  property="string" styleClass="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<textarea name="string" class="Put something here">Test Value</textarea>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyStyleId">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:textarea  property="string" styleId="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<textarea name="string" id="Put something here">Test Value</textarea>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyTitle">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:textarea  property="string" title="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<textarea name="string" title="Put something here">Test Value</textarea>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyTitleKey">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:textarea  property="string" titleKey="default.bundle.message"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<textarea name="string" title="Testing Message">Test Value</textarea>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyTitleKey_fr">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:textarea  property="string" titleKey="default.bundle.message"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<textarea name="string" title="Message D'Essai">Test Value</textarea>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyValue">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:textarea  property="string" value ="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<textarea name="string">Put something here</textarea>
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyIndexedArray">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst">
		<html:textarea  property="string" indexed="true"/>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<textarea name="org.apache.struts.taglib.html.BEAN[0].string">Test Value</textarea>
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyIndexedArrayProperty">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst" property="list">
		<html:textarea  property="string" indexed="true"/>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<textarea name="org.apache.struts.taglib.html.BEAN[0].string">Test Value</textarea>
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyIndexedMap">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst">
		<html:textarea  property="string" indexed="true"/>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<textarea name="org.apache.struts.taglib.html.BEAN[0].string">Test Value</textarea>
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyIndexedMapProperty">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst" property="map">
		<html:textarea  property="string" indexed="true"/>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<textarea name="org.apache.struts.taglib.html.BEAN[0].string">Test Value</textarea>
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyIndexedEnumeration">
	<bean:define id="TEST_RESULTS" toScope="page">
	<logic:iterate id="indivItem" name="lst">
		<html:textarea  property="string" indexed="true"/>
	</logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<textarea name="org.apache.struts.taglib.html.BEAN[0].string">Test Value</textarea>
	
		<textarea name="org.apache.struts.taglib.html.BEAN[1].string">Test Value</textarea>
	
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextareaPropertyIndexedEnumerationProperty">
	<bean:define id="TEST_RESULTS" toScope="page">
	<logic:iterate id="indivItem" name="lst" property="enumeration">
		<html:textarea  property="string" indexed="true"/>
	</logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<textarea name="org.apache.struts.taglib.html.BEAN[0].string">Test Value</textarea>
	
		<textarea name="org.apache.struts.taglib.html.BEAN[1].string">Test Value</textarea>
	
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
