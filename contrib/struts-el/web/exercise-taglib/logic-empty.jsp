<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html-el.tld"  prefix="html-el"  %>
<%@ taglib uri="/WEB-INF/struts-bean-el.tld"  prefix="bean-el" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<html-el:html>
<head>
<title>Test Replacements for struts emptiness tags</title>
</head>
<body bgcolor="white">

<div align="center">
<h1>Test Replacements for struts emptiness tags</h1>
</div>

<jsp:useBean id="bean" scope="page" class="org.apache.struts.webapp.exercise.TestBean"/>

<table border="1">
  <tr>
    <th>Test Type</th>
    <th>Correct Value</th>
    <th>Test Result</th>
  </tr>
  <tr>
    <td>null</td>
    <td>empty</td>
    <td>
      <c:choose>
       <c:when test="${empty bean.nullProperty}">
        empty
       </c:when>
       <c:otherwise>
        notEmpty
       </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <td>empty string</td>
    <td>empty</td>
    <td>
      <c:choose>
       <c:when test="${empty bean.emptyStringProperty}">
        empty
       </c:when>
       <c:otherwise>
        notEmpty
       </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <td>non-empty string</td>
    <td>notEmpty</td>
    <td>
      <c:choose>
       <c:when test="${empty bean.stringProperty}">
        empty
       </c:when>
       <c:otherwise>
        notEmpty
       </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <td>non-string object</td>
    <td>notEmpty</td>
    <td>
      <c:choose>
       <c:when test="${empty bean.intProperty}">
        empty
       </c:when>
       <c:otherwise>
        notEmpty
       </c:otherwise>
      </c:choose>
    </td>
  </tr>
</table>

</body>
</html-el:html>
