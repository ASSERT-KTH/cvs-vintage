<html>
<title>jspInit and jspDestroy</title>
<% 
/*
 First time output:
   20 before Init() decl: 20 between: 20 after Destroy() decl: 5 
 Subsequent output:
   5 before Init() decl: 5 between: 5 after Destroy() decl: 5 
*/
%>
<body>
<%! int id = 10; %>
<%=id %>
<% out.println("before Init() decl: " + id); %>
<%!
public void jspInit() {
    //System.out.println("jspInit()");
    id = 20;
}
%>
<% out.println("between: " + id); id = 5; %>
<%!
public void jspDestroy() {
    //System.out.println("jspDestroy()");
    id = 30;
}
%>
<% out.println("after Destroy() decl: " + id); %>
</body>
</html>
