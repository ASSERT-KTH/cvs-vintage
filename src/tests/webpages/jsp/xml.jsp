<jsp:directive scope="page" /jsp:directive>
<HTML>
<BODY>
<!-- following line does not work 
<jsp:declaration> String s = "say"; int i = s.length(); </jsp:declaration>
<jsp:declaration> <! [CDATA[ String s = "say"; int i = s.length(); ]]> </jsp:declaration>
-->
<%! String s = "say"; int i = s.length(); %>
<jsp:scriptlet>
    out.println(i + s + (new String(s)).length());
</jsp:scriptlet>

<%@include file="buffer.jsp" %>

<jsp:include page="implicitPage.jsp" flush="true" />

<jsp:directive.include file="implicitOut.jsp" />

</BODY>
</HTML>
