<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html-el.tld"  prefix="html-el"  %>
<%@ taglib uri="/WEB-INF/struts-bean-el.tld"  prefix="bean-el" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<html-el:html>
<head>
<title>Test Replacements for struts-bean:parameter Tag</title>
</head>
<body bgcolor="white">

<div align="center">
<h1>Test Replacements for struts-bean:parameter Tag</h1>
</div>

<p>If called from the <code>index.jsp</code> page, two request parameters will
be included and their values displayed below.  If you call this page without
including the appropriate request parameters, you will receive a JSP runtime
error instead.</p>

<c:set var="param1" value='${param["param1"]}'/>
<c:set var="param2" value='${param["param2"]}'/>
<c:set var="param3" value='${param["param3"]}'/>
<c:if test="${empty param3}">
 <c:set var="param3" value="UNKNOWN VALUE"/>
</c:if>

<table border="1">
  <tr>
    <th>Parameter Name</th>
    <th>Correct Value</th>
    <th>Test Result</th>
  </tr>
  <tr>
    <td>param1</td>
    <td>value1</td>
    <td><c:out value="${param1}"/></td>
  </tr>
  <tr>
    <td>param2</td>
    <td>value2</td>
    <td><c:out value="${param2}"/></td>
  </tr>
  <tr>
    <td>param3</td>
    <td>UNKNOWN VALUE</td>
    <td><c:out value="${param3}"/></td>
  </tr>
</table>

</body>
</html-el:html>
