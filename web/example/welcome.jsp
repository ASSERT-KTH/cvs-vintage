<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>

<html>
<head>
<title><bean:message key="index.title"/></title>
<link rel="stylesheet" type="text/css" href="base.css" />
</head>

<h3><bean:message key="index.heading"/></h3>
<ul>
<li><html:link action="/EditRegistration?action=Create"><bean:message key="index.registration"/></html:link></li>
<li><html:link action="/Logon"><bean:message key="index.logon"/></html:link></li>
</ul>

<h3>Language Options</h3>
<ul>
<li><html:link action="/Locale?language=en">English</html:link></li>
<li><html:link action="/Locale?language=ja" useLocalEncoding="true">Japanese</html:link></li>
<li><html:link action="/Locale?language=ru" useLocalEncoding="true">Russian</html:link></li>
</ul>

<hr />

<p><html:img bundle="alternate" pageKey="struts.logo.path" altKey="struts.logo.alt"/></p>

<p><html:link action="/Tour"><bean:message key="index.tour"/></html:link></p>

</body>
</html>
