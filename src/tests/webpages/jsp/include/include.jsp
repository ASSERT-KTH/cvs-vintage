<html>
<!--
-->

<%@ page buffer="5" autoFlush="false" %>

<%@ include file="foo.jsp" %>

<p> <jsp:include page="/jsp/include/foo.html" flush="true"/> :

<jsp:include page="foo.jsp" flush="true"/>

</html>
