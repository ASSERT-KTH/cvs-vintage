<html>
<jsp:useBean id="bt" class="jsp.TestBean"/>
<jsp:getProperty name="bt"  property="age" />
Name: <jsp:getProperty name="bt"  property="name" />
<jsp:setProperty name="bt"  property="age" value="99" />
<jsp:setProperty name="bt"  property="name" value="new Name" />
<P>
<jsp:getProperty name="bt"  property="age" />
Name: <jsp:getProperty name="bt"  property="name" />
</html>
