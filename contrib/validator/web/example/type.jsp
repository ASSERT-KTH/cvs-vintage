<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/struts-validator.tld" prefix="validator" %>

<html:html>
<head>
<title><bean:message key="typeForm.title"/></title>
<html:base/>
</head>
<body bgcolor="white">

<logic:messagesPresent>
   <bean:message key="errors.header"/>
   <ul>
   <html:messages id="error">
      <li><bean:write name="error"/></li>
   </html:messages>
   </ul><hr>
</logic:messagesPresent>


<html:form action="type">
  <html:hidden property="action"/>

<table border="0">
  <tr>
    <th align="left">
      <bean:message key="typeForm.byte.displayname"/>
    </th>
    <td align="left">
      <html:text property="byte" size="15" maxlength="15"/>
    </td>
  </tr>
  <tr>
    <th align="left">
      <bean:message key="typeForm.short.displayname"/>
    </th>
    <td align="left">
      <html:text property="short" size="15" maxlength="15"/>
    </td>
  </tr>
  <tr>
    <th align="left">
      <bean:message key="typeForm.integer.displayname"/>
    </th>
    <td align="left">
      <html:text property="integer" size="15" maxlength="15"/>
    </td>
  </tr>
  <tr>
    <th align="left">
      <bean:message key="typeForm.long.displayname"/>
    </th>
    <td align="left">
      <html:text property="long" size="15" maxlength="15"/>
    </td>
  </tr>
  <tr>
    <th align="left">
      <bean:message key="typeForm.float.displayname"/>
    </th>
    <td align="left">
      <html:text property="float" size="15" maxlength="15"/>
    </td>
  </tr>
  <tr>
    <th align="left">
      <bean:message key="typeForm.double.displayname"/>
    </th>
    <td align="left">
      <html:text property="double" size="15" maxlength="15"/>
    </td>
  </tr>
  <tr>
    <th align="left">
      <bean:message key="typeForm.date.displayname"/>
    </th>
    <td align="left">
      <html:text property="date" size="15" maxlength="15"/>
    </td>
  </tr>
  <tr>
    <th align="left">
      <bean:message key="typeForm.creditCard.displayname"/>
    </th>
    <td align="left">
      <html:text property="creditCard" size="16" maxlength="16"/>
    </td>
  </tr>
  <tr>
    <td>
      <html:submit property="submit">
         <bean:message key="button.save"/>
      </html:submit>
      &nbsp;
      <html:reset>
         <bean:message key="button.reset"/>
      </html:reset>
      &nbsp;
      <html:cancel>
         <bean:message key="button.cancel"/>
      </html:cancel>    
    </td>
  </tr>
</table>

</html:form>

</body>
</html:html>
