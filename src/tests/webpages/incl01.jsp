<pre>
incl01.jsp
<%= new java.util.Date() %>
<%= request.getParameter("abc") %>
<jsp:include page="incl02.jsp" flush="true" />
<jsp:include page="incl02.jsp?abc=ghi" flush="true" />
<jsp:include page="incl02.jsp?abc=ghi&abc=jkl" flush="true" />
<jsp:include page="incl02.jsp" flush="true" >
  <jsp:param name="abc" value="jkl" />
</jsp:include>
<jsp:include page="incl02.jsp" flush="true" >
  <jsp:param name="abc" value="jkl" />
  <jsp:param name="abc" value="mno" />
</jsp:include>
<jsp:include page="incl02.jsp" flush="true" />
</pre>
