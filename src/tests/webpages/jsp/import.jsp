<html>
<%@ page import="java.util.Hashtable" %>
  <body>
<% 
    Hashtable hash = new Hashtable();
    Vector vector = new Vector();
    vector.add("Value 1");
    hash.put("key 1", vector.elementAt(0));
    out.println("Hashtable get(\"key 1\") : " + (String)hash.get("key 1"));
%>
  </body>
</html>
