<%@ taglib uri="/tags/struts-bean" prefix="bean" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>
<html>
  <head>
    <title>Test struts-bean:write Tag</title>
  </head>
  <body>
    <div align="center">
      <h1>Test struts-bean:write Tag</h1>
    </div>
    <h3>Test 7 - Localized format patterns</h3><%
      pageContext.setAttribute("test7.double", new Double(1234567.89));
      pageContext.setAttribute("test7.date", new java.util.Date(123456789));
    %>
    <h4>Doubles</h4>
    <table border="0">
      <tr>
        <td>
          <table border="1">
            <tr>
              <th>Language</th>
              <th>Double format</th>
            </tr>
            <tr>
              <td>de 
              <bean:message key="locale.de" /></td>
              <td>1.234.567,89</td>
            </tr>
            <tr>
              <td>en 
              <bean:message key="locale.en" /></td>
              <td>1,234,567.89</td>
            </tr>
            <tr>
              <td>fr 
              <bean:message key="locale.fr" /></td>
              <td>1 234 567,89</td>
            </tr>
          </table>
        </td>
        <td>
          <table border="1">
            <tr>
              <th>Default format</th>
              <th>Using Format Attribute</th>
              <th>Using Format Key</th>
            </tr>
            <tr>
              <td>
                <%= pageContext.getAttribute("test7.double") %>
              </td>
              <td>[#,000.00] 
              <bean:write name="test7.double" format="#,000.00" /></td>
              <td>[
              <bean:message key="double.pattern" />] 
              <bean:write name="test7.double" formatKey="double.pattern" /></td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <h4>Dates</h4>
    <table border="0">
      <tr>
        <td>
          <table border="1">
            <tr>
              <th>Language</th>
              <th>Date format</th>
            </tr>
            <tr>
              <td>de 
              <bean:message key="locale.de" /></td>
              <td>Fr, Jan 2, '70</td>
            </tr>
            <tr>
              <td>en 
              <bean:message key="locale.en" /></td>
              <td>Fri, Jan 2, '70</td>
            </tr>
            <tr>
              <td>fr 
              <bean:message key="locale.fr" /></td>
              <td>ven., janv. 2, '70</td>
            </tr>
          </table>
        </td>
        <td>
          <table border="1">
            <tr>
              <th>Default format</th>
              <th>Using Format Attribute</th>
              <th>Using Format Key</th>
            </tr>
            <tr>
              <td>
                <%= pageContext.getAttribute("test7.date") %>
              </td>
              <td>[EEE, MMM d, ''yy] 
              <bean:write name="test7.date" format="EEE, MMM d, ''yy" /></td>
              <td>[
              <bean:message key="date.pattern" />] 
              <bean:write name="test7.date" formatKey="date.pattern" /></td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
    <h4>Sprache | Language | Langage</h4>
    <ul>
      <li>
        <html:link action="/locale?page=/bean-write-2.jsp&amp;language=de">German | Deutsch</html:link>
        <bean:message key="locale.de" />
      </li>
      <li>
        <html:link action="/locale?page=/bean-write-2.jsp&amp;language=en">English | Anglais</html:link>
        <bean:message key="locale.en" />
      </li>
      <li>
        <html:link action="/locale?page=/bean-write-2.jsp&amp;language=fr">French | Francais</html:link>
        <bean:message key="locale.fr" />
      </li>
    </ul>
  </body>
</html>
