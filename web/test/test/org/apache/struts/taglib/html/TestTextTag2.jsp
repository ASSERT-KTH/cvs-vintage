<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@page import="junit.framework.Assert"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>


<logic:equal name="runTest" value="testTextPropertyReadonly">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:text property="string" readonly="true"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<input type="text" name="string" value="Test Value" readonly="readonly">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertySize">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:text property="string" size="10"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<input type="text" name="string" size="10" value="Test Value">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyStyle">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:text property="string" style="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<input type="text" name="string" value="Test Value" style="Put something here">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyStyleClass">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:text property="string" styleClass="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<input type="text" name="string" value="Test Value" class="Put something here">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyStyleId">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:text property="string" styleId="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<input type="text" name="string" value="Test Value" id="Put something here">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyTitle">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:text property="string" title="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<input type="text" name="string" value="Test Value" title="Put something here">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyTitleKey">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:text property="string" titleKey="default.bundle.message"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<input type="text" name="string" value="Test Value" title="Testing Message">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyTitleKey_fr">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:text property="string" titleKey="default.bundle.message"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<input type="text" name="string" value="Test Value" title="Message D'Essai">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyValue">
	<bean:define id="TEST_RESULTS" toScope="page">
		<html:text property="string" value ="Put something here"/>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
		<input type="text" name="string" value="Put something here">
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyIndexedArray">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst">
		<html:text property="string" indexed="true"/>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<input type="text" name="org.apache.struts.taglib.html.BEAN[0].string" value="Test Value">
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyIndexedArrayProperty">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst" property="list">
		<html:text property="string" indexed="true"/>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<input type="text" name="org.apache.struts.taglib.html.BEAN[0].string" value="Test Value">
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyIndexedMap">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst">
		<html:text property="string" indexed="true"/>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<input type="text" name="org.apache.struts.taglib.html.BEAN[0].string" value="Test Value">
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyIndexedMapProperty">
	<bean:define id="TEST_RESULTS" toScope="page">
	  <logic:iterate id="indivItem" name="lst" property="map">
		<html:text property="string" indexed="true"/>
	  </logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<input type="text" name="org.apache.struts.taglib.html.BEAN[0].string" value="Test Value">
		
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyIndexedEnumeration">
	<bean:define id="TEST_RESULTS" toScope="page">
	<logic:iterate id="indivItem" name="lst">
		<html:text property="string" indexed="true"/>
	</logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<input type="text" name="org.apache.struts.taglib.html.BEAN[0].string" value="Test Value">
	
		<input type="text" name="org.apache.struts.taglib.html.BEAN[1].string" value="Test Value">
	
	</bean:define>
</logic:equal>

<logic:equal name="runTest" value="testTextPropertyIndexedEnumerationProperty">
	<bean:define id="TEST_RESULTS" toScope="page">
	<logic:iterate id="indivItem" name="lst" property="enumeration">
		<html:text property="string" indexed="true"/>
	</logic:iterate>
	</bean:define>
	<bean:define id="EXPECTED_RESULTS" toScope="page">
	
		<input type="text" name="org.apache.struts.taglib.html.BEAN[0].string" value="Test Value">
	
		<input type="text" name="org.apache.struts.taglib.html.BEAN[1].string" value="Test Value">
	
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
