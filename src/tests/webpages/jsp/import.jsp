<html>
<%@ page import="java.util.Hashtable" %>
<%@ page import="java.util.Vector" %>
  <body>
<% 
    Hashtable hash = new Hashtable();
    Vector vector = new Vector();
    vector.addElement("Value 1");
    hash.put("key 1", vector.elementAt(0));
    out.println("Hashtable get(\"key 1\") : " + (String)hash.get("key 1"));
%>
  </body>
</html>
