<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-logic" prefix="logic" %>
<html>
  <head>
    <title>Test struts-logic Comparison Tags</title>
  </head>
  <body>
    <div align="center">
      <h1>Test struts-logic Comparison Tags</h1>
    </div>
    <jsp:useBean id="bean" scope="page" class="org.apache.struts.webapp.exercise.TestBean" /><%
              String bool1 = "true";
              String bool2 = "false";
              String str1 = "This is a string";
              String str2 = "Less than";
              String str3 = "XYZ greater than";
            %>
    <table border="1">
      <tr>
        <th>Test Type</th>
        <th>Variable Content</th>
        <th>Value Content</th>
        <th>Correct Value</th>
        <th>Test Result</th>
      </tr>
      <tr>
        <td>boolean / EQ</td>
        <td>
          <bean:write name="bean" property="booleanProperty" />
        </td>
        <td>
          <%= bool1 %>
        </td>
        <td>equal</td>
        <td>
          <logic:equal name="bean" property="booleanProperty" value="<%= bool1 %>">equal</logic:equal>
          <logic:notEqual name="bean" property="booleanProperty" value="<%= bool1 %>">notEqual</logic:notEqual>
        </td>
      </tr>
      <tr>
        <td>boolean / EQ</td>
        <td>
          <bean:write name="bean" property="falseProperty" />
        </td>
        <td>
          <%= bool2 %>
        </td>
        <td>equal</td>
        <td>
          <logic:equal name="bean" property="falseProperty" value="<%= bool2 %>">equal</logic:equal>
          <logic:notEqual name="bean" property="falseProperty" value="<%= bool2 %>">notEqual</logic:notEqual>
        </td>
      </tr>
      <tr>
        <td>boolean / NE</td>
        <td>
          <bean:write name="bean" property="booleanProperty" />
        </td>
        <td>
          <%= bool2 %>
        </td>
        <td>notEqual</td>
        <td>
          <logic:equal name="bean" property="booleanProperty" value="<%= bool2 %>">equal</logic:equal>
          <logic:notEqual name="bean" property="booleanProperty" value="<%= bool2 %>">notEqual</logic:notEqual>
        </td>
      </tr>
      <tr>
        <td>boolean / NE</td>
        <td>
          <bean:write name="bean" property="falseProperty" />
        </td>
        <td>
          <%= bool1 %>
        </td>
        <td>notEqual</td>
        <td>
          <logic:equal name="bean" property="falseProperty" value="<%= bool1 %>">equal</logic:equal>
          <logic:notEqual name="bean" property="falseProperty" value="<%= bool1 %>">notEqual</logic:notEqual>
        </td>
      </tr>
      <tr>
        <td>string / EQ</td>
        <td>
          <bean:write name="bean" property="stringProperty" />
        </td>
        <td>
          <%= str1 %>
        </td>
        <td>equal greaterEqual lessEqual</td>
        <td>
          <logic:equal name="bean" property="stringProperty" value="<%= str1 %>">equal</logic:equal>
          <logic:greaterEqual name="bean" property="stringProperty" value="<%= str1 %>">greaterEqual</logic:greaterEqual>
          <logic:greaterThan name="bean" property="stringProperty" value="<%= str1 %>">greaterThan</logic:greaterThan>
          <logic:lessEqual name="bean" property="stringProperty" value="<%= str1 %>">lessEqual</logic:lessEqual>
          <logic:lessThan name="bean" property="stringProperty" value="<%= str1 %>">lessThan</logic:lessThan>
          <logic:notEqual name="bean" property="stringProperty" value="<%= str1 %>">notEqual</logic:notEqual>
        </td>
      </tr>
      <tr>
        <td>string / GT</td>
        <td>
          <bean:write name="bean" property="stringProperty" />
        </td>
        <td>
          <%= str2 %>
        </td>
        <td>greaterEqual greaterThan notEqual</td>
        <td>
          <logic:equal name="bean" property="stringProperty" value="<%= str2 %>">equal</logic:equal>
          <logic:greaterEqual name="bean" property="stringProperty" value="<%= str2 %>">greaterEqual</logic:greaterEqual>
          <logic:greaterThan name="bean" property="stringProperty" value="<%= str2 %>">greaterThan</logic:greaterThan>
          <logic:lessEqual name="bean" property="stringProperty" value="<%= str2 %>">lessEqual</logic:lessEqual>
          <logic:lessThan name="bean" property="stringProperty" value="<%= str2 %>">lessThan</logic:lessThan>
          <logic:notEqual name="bean" property="stringProperty" value="<%= str2 %>">notEqual</logic:notEqual>
        </td>
      </tr>
      <tr>
        <td>string / LT</td>
        <td>
          <bean:write name="bean" property="stringProperty" />
        </td>
        <td>
          <%= str3 %>
        </td>
        <td>lessEqual lessThan notEqual</td>
        <td>
          <logic:equal name="bean" property="stringProperty" value="<%= str3 %>">equal</logic:equal>
          <logic:greaterEqual name="bean" property="stringProperty" value="<%= str3 %>">greaterEqual</logic:greaterEqual>
          <logic:greaterThan name="bean" property="stringProperty" value="<%= str3 %>">greaterThan</logic:greaterThan>
          <logic:lessEqual name="bean" property="stringProperty" value="<%= str3 %>">lessEqual</logic:lessEqual>
          <logic:lessThan name="bean" property="stringProperty" value="<%= str3 %>">lessThan</logic:lessThan>
          <logic:notEqual name="bean" property="stringProperty" value="<%= str3 %>">notEqual</logic:notEqual>
        </td>
      </tr>
      <tr>
        <td>string / NULL</td>
        <td>
        <bean:write name="bean" property="nullProperty" />&#160;</td>
        <td>&#160;</td>
        <td>equal greaterEqual lessEqual</td>
        <td>
          <logic:equal name="bean" property="nullProperty" value="">equal</logic:equal>
          <logic:greaterEqual name="bean" property="nullProperty" value="">greaterEqual</logic:greaterEqual>
          <logic:greaterThan name="bean" property="nullProperty" value="">greaterThan</logic:greaterThan>
          <logic:lessEqual name="bean" property="nullProperty" value="">lessEqual</logic:lessEqual>
          <logic:lessThan name="bean" property="nullProperty" value="">lessThan</logic:lessThan>
          <logic:notEqual name="bean" property="nullProperty" value="">notEqual</logic:notEqual>
        </td>
      </tr>
    </table>
  </body>
</html>
