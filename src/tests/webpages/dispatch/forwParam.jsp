<%
  String workListID="foo";
  String wiID="bar";
  String action="open";

%>

<jsp:forward page = "/servlet/RequestDump" >
  <jsp:param name = "workList.id"       value = "<%= workListID %>" />
  <jsp:param name = "workItem.id"       value = "<%= wiID %>" />
  <jsp:param name = "open.content"      value = "<%= action.equals( \"open\" ) ? \"true\" : \"false\" %>" />
</jsp:forward>