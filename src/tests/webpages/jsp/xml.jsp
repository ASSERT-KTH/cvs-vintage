<?xml version="1.0" ?>

<!--  Broken doctype link, and I cannot find where it is (it's not in the distro YET)
  <!DOCTYPE root
   PUBLIC "-//Sun Microsystems Inc.//DTD JavaServer Pages Version 1.1//EN"
      "http://java.sun.com/products/jsp/dtd/jspcore_1_0.dtd">
-->

<jsp:root 
    xmlns:jsp="http://java.sun.com/products/jsp/dtd/jsp_1_0.dtd">
  <![CDATA[
<HTML>
<BODY>
]]>
  <jsp:declaration> <![CDATA[ String s = "say"; int i = s.length(); ]]> </jsp:declaration>
  <![CDATA[

]]>
  <jsp:scriptlet>
  <![CDATA[
    out.println(i + s + (new String(s)).length());
]]>
  </jsp:scriptlet>
  <![CDATA[

]]>
  <jsp:directive.include file="buffer.jsp" />
  <![CDATA[

]]>
  <jsp:include page="implicitPage.jsp" flush="true" />
  <![CDATA[

]]>
  <jsp:directive.include file="implicitOut.jsp" />
  <![CDATA[

</BODY>
</HTML>]]>
</jsp:root>
