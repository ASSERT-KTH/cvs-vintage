<h1>Security test </h1>

Attempting to create /tmp/sectest
<% 
 try {
  java.io.FileWriter f=new java.io.FileWriter("/tmp/sectest");
  f.write("Can write"); 
  f.close();
 } catch(Exception ex ) {
%>
<h2>Exception <%= ex %> </h2>
<%  ex.printStackTrace( new java.io.PrintWriter(out) ); 
 }
%>
