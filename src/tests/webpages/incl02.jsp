incl02.jsp
<%= request.getParameter("abc") %>
<%
  String s[]=request.getParameterValues("abc");
  if (s!=null)
    for (int i=0; i<s.length; i++)
    {
      out.println(i+"="+s[i]);
    }
%>