<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ page language="java" %>
<%@ taglib uri="/WEB-INF/struts-html-el.tld"  prefix="html-el"  %>
<%@ taglib uri="/WEB-INF/struts-bean-el.tld"  prefix="bean-el" %>
<%@ taglib uri="/WEB-INF/c.tld" prefix="c" %>
<html-el:html>
<head>
<title>Test Replacements for struts comparison tags</title>
</head>
<body bgcolor="white">

<div align="center">
<h1>Test Replacements for struts comparison tags</h1>
</div>

<jsp:useBean id="bean" scope="page" class="org.apache.struts.webapp.exercise.TestBean"/>
<%
  String bool1 = "true";
  String bool2 = "false";
  String doub1 = "321.0";
  String doub2 = "111.0";
  String doub3 = "333.0";
  String long1 = "321";
  String long2 = "111";
  String long3 = "333";
  String short1 = "987";
  String short2 = "654";
  String short3 = "999";
  String str1 = "This is a string";
  String str2 = "Less than";
  String str3 = "XYZ greater than";

  pageContext.setAttribute("bool1", bool1);
  pageContext.setAttribute("bool2", bool2);
  pageContext.setAttribute("doub1", doub1);
  pageContext.setAttribute("doub2", doub2);
  pageContext.setAttribute("doub3", doub3);
  pageContext.setAttribute("long1", long1);
  pageContext.setAttribute("long2", long2);
  pageContext.setAttribute("long3", long3);
  pageContext.setAttribute("short1", short1);
  pageContext.setAttribute("short2", short2);
  pageContext.setAttribute("short3", short3);
  pageContext.setAttribute("str1", str1);
  pageContext.setAttribute("str2", str2);
  pageContext.setAttribute("str3", str3);
%>

<table border="1">
  <tr>
    <th>Test Type</th>
    <th>Variable Content</th>
    <th>Value Content </th>
    <th>Correct Value</th>
    <th>Test Result</th>
  </tr>
  <tr>
    <td>boolean / EQ</td>
    <td><c:out value="${bean.booleanProperty}"/></td>
    <td><c:out value="${bool1}"/></td>
    <td>equal</td>
    <td>
      <c:choose>
       <c:when test="${bean.booleanProperty eq bool1}">
        equal
       </c:when>
       <c:otherwise>
        notEqual
       </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <td>boolean / EQ</td>
    <td><c:out value="${bean.falseProperty}"/></td>
    <td><c:out value="${bool2}"/></td>
    <td>equal</td>
    <td>
      <c:choose>
       <c:when test="${bean.falseProperty eq bool2}">
        equal
       </c:when>
       <c:otherwise>
        notEqual
       </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <td>boolean / NE</td>
    <td><c:out value="${bean.booleanProperty}"/></td>
    <td><c:out value="${bool2}"/></td>
    <td>notEqual</td>
    <td>
      <c:choose>
       <c:when test="${bean.booleanProperty eq bool2}">
        equal
       </c:when>
       <c:otherwise>
        notEqual
       </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <td>boolean / NE</td>
    <td><c:out value="${bean.falseProperty}"/></td>
    <td><c:out value="${bool1}"/></td>
    <td>notEqual</td>
    <td>
      <c:choose>
       <c:when test="${bean.falseProperty eq bool1}">
        equal
       </c:when>
       <c:otherwise>
        notEqual
       </c:otherwise>
      </c:choose>
    </td>
  </tr>
  <tr>
    <td>double / EQ</td>
    <td><c:out value="${bean.doubleProperty}"/></td>
    <td><c:out value="${doub1}"/></td>
    <td>equal greaterEqual lessEqual</td>
    <td>
      <c:if test="${bean.doubleProperty eq doub1}">
        equal
      </c:if>
      <c:if test="${bean.doubleProperty ge doub1}">
        greaterEqual
      </c:if>
      <c:if test="${bean.doubleProperty gt doub1}">
        greaterThan
      </c:if>
      <c:if test="${bean.doubleProperty le doub1}">
        lessEqual
      </c:if>
      <c:if test="${bean.doubleProperty lt doub1}">
        lessThan
      </c:if>
      <c:if test="${bean.doubleProperty ne doub1}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>double / GT</td>
    <td><c:out value="${bean.doubleProperty}"/></td>
    <td><c:out value="${doub2}"/></td>
    <td>greaterEqual greaterThan notEqual</td>
    <td>
      <c:if test="${bean.doubleProperty eq doub2}">
        equal
      </c:if>
      <c:if test="${bean.doubleProperty ge doub2}">
        greaterEqual
      </c:if>
      <c:if test="${bean.doubleProperty gt doub2}">
        greaterThan
      </c:if>
      <c:if test="${bean.doubleProperty le doub2}">
        lessEqual
      </c:if>
      <c:if test="${bean.doubleProperty lt doub2}">
        lessThan
      </c:if>
      <c:if test="${bean.doubleProperty ne doub2}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>double / LT</td>
    <td><c:out value="${bean.doubleProperty}"/></td>
    <td><c:out value="${doub3}"/></td>
    <td>lessEqual lessThan notEqual</td>
    <td>
      <c:if test="${bean.doubleProperty eq doub3}">
        equal
      </c:if>
      <c:if test="${bean.doubleProperty ge doub3}">
        greaterEqual
      </c:if>
      <c:if test="${bean.doubleProperty gt doub3}">
        greaterThan
      </c:if>
      <c:if test="${bean.doubleProperty le doub3}">
        lessEqual
      </c:if>
      <c:if test="${bean.doubleProperty lt doub3}">
        lessThan
      </c:if>
      <c:if test="${bean.doubleProperty ne doub3}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>float / EQ</td>
    <td><c:out value="${bean.floatProperty}"/></td>
    <td><c:out value="${doub1}"/></td>
    <td>lessEqual lessThan notEqual</td>
    <td>
      <c:if test="${bean.floatProperty eq doub1}">
        equal
      </c:if>
      <c:if test="${bean.floatProperty ge doub1}">
        greaterEqual
      </c:if>
      <c:if test="${bean.floatProperty gt doub1}">
        greaterThan
      </c:if>
      <c:if test="${bean.floatProperty le doub1}">
        lessEqual
      </c:if>
      <c:if test="${bean.floatProperty lt doub1}">
        lessThan
      </c:if>
      <c:if test="${bean.floatProperty ne doub1}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>float / GT</td>
    <td><c:out value="${bean.floatProperty}"/></td>
    <td><c:out value="${doub2}"/></td>
    <td>greaterEqual greaterThan notEqual</td>
    <td>
      <c:if test="${bean.floatProperty eq doub2}">
        equal
      </c:if>
      <c:if test="${bean.floatProperty ge doub2}">
        greaterEqual
      </c:if>
      <c:if test="${bean.floatProperty gt doub2}">
        greaterThan
      </c:if>
      <c:if test="${bean.floatProperty le doub2}">
        lessEqual
      </c:if>
      <c:if test="${bean.floatProperty lt doub2}">
        lessThan
      </c:if>
      <c:if test="${bean.floatProperty ne doub2}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>float / LT</td>
    <td><c:out value="${bean.floatProperty}"/></td>
    <td><c:out value="${doub3}"/></td>
    <td>lessEqual lessThan notEqual</td>
    <td>
      <c:if test="${bean.floatProperty eq doub3}">
        equal
      </c:if>
      <c:if test="${bean.floatProperty ge doub3}">
        greaterEqual
      </c:if>
      <c:if test="${bean.floatProperty gt doub3}">
        greaterThan
      </c:if>
      <c:if test="${bean.floatProperty le doub3}">
        lessEqual
      </c:if>
      <c:if test="${bean.floatProperty lt doub3}">
        lessThan
      </c:if>
      <c:if test="${bean.floatProperty ne doub3}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>int / EQ</td>
    <td><c:out value="${bean.intProperty}"/></td>
    <td><c:out value="${long1}"/></td>
    <td>lessEqual lessThan notEqual</td>
    <td>
      <c:if test="${bean.intProperty eq long1}">
        equal
      </c:if>
      <c:if test="${bean.intProperty ge long1}">
        greaterEqual
      </c:if>
      <c:if test="${bean.intProperty gt long1}">
        greaterThan
      </c:if>
      <c:if test="${bean.intProperty le long1}">
        lessEqual
      </c:if>
      <c:if test="${bean.intProperty lt long1}">
        lessThan
      </c:if>
      <c:if test="${bean.intProperty ne long1}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>int / GT</td>
    <td><c:out value="${bean.intProperty}"/></td>
    <td><c:out value="${long2}"/></td>
    <td>greaterEqual greaterThan notEqual</td>
    <td>
      <c:if test="${bean.intProperty eq long2}">
        equal
      </c:if>
      <c:if test="${bean.intProperty ge long2}">
        greaterEqual
      </c:if>
      <c:if test="${bean.intProperty gt long2}">
        greaterThan
      </c:if>
      <c:if test="${bean.intProperty le long2}">
        lessEqual
      </c:if>
      <c:if test="${bean.intProperty lt long2}">
        lessThan
      </c:if>
      <c:if test="${bean.intProperty ne long2}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>int / LT</td>
    <td><c:out value="${bean.intProperty}"/></td>
    <td><c:out value="${long3}"/></td>
    <td>lessEqual lessThan notEqual</td>
    <td>
      <c:if test="${bean.intProperty eq long3}">
        equal
      </c:if>
      <c:if test="${bean.intProperty ge long3}">
        greaterEqual
      </c:if>
      <c:if test="${bean.intProperty gt long3}">
        greaterThan
      </c:if>
      <c:if test="${bean.intProperty le long3}">
        lessEqual
      </c:if>
      <c:if test="${bean.intProperty lt long3}">
        lessThan
      </c:if>
      <c:if test="${bean.intProperty ne long3}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>long / EQ</td>
    <td><c:out value="${bean.longProperty}"/></td>
    <td><c:out value="${long1}"/></td>
    <td>equal greaterEqual lessEqual</td>
    <td>
      <c:if test="${bean.longProperty eq long1}">
        equal
      </c:if>
      <c:if test="${bean.longProperty ge long1}">
        greaterEqual
      </c:if>
      <c:if test="${bean.longProperty gt long1}">
        greaterThan
      </c:if>
      <c:if test="${bean.longProperty le long1}">
        lessEqual
      </c:if>
      <c:if test="${bean.longProperty lt long1}">
        lessThan
      </c:if>
      <c:if test="${bean.longProperty ne long1}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>long / GT</td>
    <td><c:out value="${bean.longProperty}"/></td>
    <td><c:out value="${long2}"/></td>
    <td>greaterEqual greaterThan notEqual</td>
    <td>
      <c:if test="${bean.longProperty eq long2}">
        equal
      </c:if>
      <c:if test="${bean.longProperty ge long2}">
        greaterEqual
      </c:if>
      <c:if test="${bean.longProperty gt long2}">
        greaterThan
      </c:if>
      <c:if test="${bean.longProperty le long2}">
        lessEqual
      </c:if>
      <c:if test="${bean.longProperty lt long2}">
        lessThan
      </c:if>
      <c:if test="${bean.longProperty ne long2}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>long / LT</td>
    <td><c:out value="${bean.longProperty}"/></td>
    <td><c:out value="${long3}"/></td>
    <td>lessEqual lessThan notEqual</td>
    <td>
      <c:if test="${bean.longProperty eq long3}">
        equal
      </c:if>
      <c:if test="${bean.longProperty ge long3}">
        greaterEqual
      </c:if>
      <c:if test="${bean.longProperty gt long3}">
        greaterThan
      </c:if>
      <c:if test="${bean.longProperty le long3}">
        lessEqual
      </c:if>
      <c:if test="${bean.longProperty lt long3}">
        lessThan
      </c:if>
      <c:if test="${bean.longProperty ne long3}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>short / EQ</td>
    <td><c:out value="${bean.shortProperty}"/></td>
    <td><c:out value="${short1}"/></td>
    <td>equal greaterEqual lessEqual</td>
    <td>
      <c:if test="${bean.shortProperty eq short1}">
        equal
      </c:if>
      <c:if test="${bean.shortProperty ge short1}">
        greaterEqual
      </c:if>
      <c:if test="${bean.shortProperty gt short1}">
        greaterThan
      </c:if>
      <c:if test="${bean.shortProperty le short1}">
        lessEqual
      </c:if>
      <c:if test="${bean.shortProperty lt short1}">
        lessThan
      </c:if>
      <c:if test="${bean.shortProperty ne short1}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>short / GT</td>
    <td><c:out value="${bean.shortProperty}"/></td>
    <td><c:out value="${short2}"/></td>
    <td>greaterEqual greaterThan notEqual</td>
    <td>
      <c:if test="${bean.shortProperty eq short2}">
        equal
      </c:if>
      <c:if test="${bean.shortProperty ge short2}">
        greaterEqual
      </c:if>
      <c:if test="${bean.shortProperty gt short2}">
        greaterThan
      </c:if>
      <c:if test="${bean.shortProperty le short2}">
        lessEqual
      </c:if>
      <c:if test="${bean.shortProperty lt short2}">
        lessThan
      </c:if>
      <c:if test="${bean.shortProperty ne short2}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>short / LT</td>
    <td><c:out value="${bean.shortProperty}"/></td>
    <td><c:out value="${short3}"/></td>
    <td>lessEqual lessThan notEqual</td>
    <td>
      <c:if test="${bean.shortProperty eq short3}">
        equal
      </c:if>
      <c:if test="${bean.shortProperty ge short3}">
        greaterEqual
      </c:if>
      <c:if test="${bean.shortProperty gt short3}">
        greaterThan
      </c:if>
      <c:if test="${bean.shortProperty le short3}">
        lessEqual
      </c:if>
      <c:if test="${bean.shortProperty lt short3}">
        lessThan
      </c:if>
      <c:if test="${bean.shortProperty ne short3}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>string / EQ</td>
    <td><c:out value="${bean.stringProperty}"/></td>
    <td><c:out value="${str1}"/></td>
    <td>equal greaterEqual lessEqual</td>
    <td>
      <c:if test="${bean.stringProperty eq str1}">
        equal
      </c:if>
      <c:if test="${bean.stringProperty ge str1}">
        greaterEqual
      </c:if>
      <c:if test="${bean.stringProperty gt str1}">
        greaterThan
      </c:if>
      <c:if test="${bean.stringProperty le str1}">
        lessEqual
      </c:if>
      <c:if test="${bean.stringProperty lt str1}">
        lessThan
      </c:if>
      <c:if test="${bean.stringProperty ne str1}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>string / GT</td>
    <td><c:out value="${bean.stringProperty}"/></td>
    <td><c:out value="${str2}"/></td>
    <td>greaterEqual greaterThan notEqual</td>
    <td>
      <c:if test="${bean.stringProperty eq str2}">
        equal
      </c:if>
      <c:if test="${bean.stringProperty ge str2}">
        greaterEqual
      </c:if>
      <c:if test="${bean.stringProperty gt str2}">
        greaterThan
      </c:if>
      <c:if test="${bean.stringProperty le str2}">
        lessEqual
      </c:if>
      <c:if test="${bean.stringProperty lt str2}">
        lessThan
      </c:if>
      <c:if test="${bean.stringProperty ne str2}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>string / LT</td>
    <td><c:out value="${bean.stringProperty}"/></td>
    <td><c:out value="${str3}"/></td>
    <td>lessEqual lessThan notEqual</td>
    <td>
      <c:if test="${bean.stringProperty eq str3}">
        equal
      </c:if>
      <c:if test="${bean.stringProperty ge str3}">
        greaterEqual
      </c:if>
      <c:if test="${bean.stringProperty gt str3}">
        greaterThan
      </c:if>
      <c:if test="${bean.stringProperty le str3}">
        lessEqual
      </c:if>
      <c:if test="${bean.stringProperty lt str3}">
        lessThan
      </c:if>
      <c:if test="${bean.stringProperty ne str3}">
        notEqual
      </c:if>
    </td>
  </tr>
  <tr>
    <td>string / NULL</td>
    <td><c:out value="${bean.nullProperty}" default=""/></td>
    <td>&nbsp;</td>
    <td>equal greaterEqual lessEqual</td>
    <td>
      <c:if test='${bean.nullProperty eq ""}'>
        equal
      </c:if>
      <c:if test='${bean.nullProperty ge ""}'>
        greaterEqual
      </c:if>
      <c:if test='${bean.nullProperty gt ""}'>
        greaterThan
      </c:if>
      <c:if test='${bean.nullProperty le ""}'>
        lessEqual
      </c:if>
      <c:if test='${bean.nullProperty lt ""}'>
        lessThan
      </c:if>
      <c:if test='${bean.nullProperty ne ""}'>
        notEqual
      </c:if>
    </td>
  </tr>
</table>


</body>
</html-el:html>
