<html>
<jsp:useBean id="bt" scope="session" class="jsp.TestBean"/>
<jsp:setProperty name="bt"  property="name" value="before forwarding" />

<jsp:forward page="/jsp/beanScopeTarget.jsp"/>

</html>
