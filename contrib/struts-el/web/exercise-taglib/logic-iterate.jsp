<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html-el.tld"  prefix="html-el" %>
<%@ taglib uri="/WEB-INF/struts-bean-el.tld"  prefix="bean-el" %>
<%@ taglib uri="/WEB-INF/struts-logic-el.tld" prefix="logic-el" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<html-el:html>
<head>
<title>Test struts logic-el Iterate Tag</title>
</head>
<body bgcolor="white">

<%
  {
    java.util.ArrayList list = new java.util.ArrayList();
    list.add("First");
    list.add("Second");
    list.add("Third");
    list.add("Fourth");
    list.add("Fifth");
    pageContext.setAttribute("list", list, PageContext.PAGE_SCOPE);

    int intArray[] = new int[]
     { 0, 10, 20, 30, 40 };
    pageContext.setAttribute("intArray", intArray, PageContext.PAGE_SCOPE);
  }
%>

<div align="center">
<h1>Test struts logic-el Iterate Tag</h1>
</div>

<jsp:useBean id="bean" scope="page" class="org.apache.struts.webapp.exercise.TestBean"/>
<jsp:useBean id="list" scope="page" class="java.util.ArrayList"/>

<h3>Test 1 - Iterate Over A String Array [0..4]</h3>

<ol>
<logic-el:iterate id="element" name="bean" property="stringArray" indexId="index">
  <li><em><c:out value="${element}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 2 - Iterate Over A String Array [0..2]</h3>

<ol>
<logic-el:iterate id="element" name="bean" property="stringArray" indexId="index"
        length="3">
  <li><em><c:out value="${element}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 3 - Iterate Over A String Array [3..4]</h3>

<ol>
<logic-el:iterate id="element" name="bean" property="stringArray" indexId="index"
        offset="3">
  <li><em><c:out value="${element}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 4 - Iterate Over A String Array [1..3]</h3>

<ol>
<logic-el:iterate id="element" name="bean" property="stringArray" indexId="index"
               offset="1" length="3">
  <li><em><c:out value="${element}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 5 - Iterate Over an Array List</h3>

<ol>
<logic-el:iterate id="item" name="list" indexId="index">
  <li><em><c:out value="${item}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 6 - Iterate Over an Array List [0..2]</h3>

<ol>
<logic-el:iterate id="item" name="list" indexId="index"
       offset="0" length="3">
  <li><em><c:out value="${item}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 7 - Iterate Over an Array List [2..4]</h3>

<ol>
<logic-el:iterate id="item" name="list" indexId="index"
       offset="2" length="3">
  <li><em><c:out value="${item}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 8 - Iterate Over an int array</h3>

<ol>
<logic-el:iterate id="item" name="intArray" indexId="index">
  <li><em><c:out value="${item}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 9 - Iterate Over an int array [0..2]</h3>

<ol>
<logic-el:iterate id="item" name="intArray" indexId="index"
           length="3">
  <li><em><c:out value="${item}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 10 - Iterate Over an int array [2..4]</h3>

<ol>
<logic-el:iterate id="item" name="intArray" indexId="index"
           offset="2" length="3">
  <li><em><c:out value="${item}"/></em>&nbsp;[<c:out value="${index}"/>]</li>
</logic-el:iterate>
</ol>

<h3>Test 11 - Iterate Over HTTP Headers</h3>

<table border="1">
 <tr>
  <th>Key</th>
  <th>Value</th>
 </tr>
 <logic-el:iterate id="item" collection="${header}" indexId="index">
  <tr>
   <td><c:out value="${item.key}"/></td>
   <td><c:out value="${item.value}"/></td>
  </tr>
 </logic-el:iterate>

</body>
</html-el:html>
