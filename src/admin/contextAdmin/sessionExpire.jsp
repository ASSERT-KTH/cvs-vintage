<html>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<head>
    <title>Session info</title>
</head>

<body bgcolor="white">

<adm:admin ctxPathParam="ctx"/>
<adm:moduleAdmin var="store" 
		 type="org.apache.tomcat.modules.session.SimpleSessionStore" />

  <h3>Invalidate <%= request.getParameter("id" ) %></h3>

<% org.apache.tomcat.core.ServerSession s=store.findSession( ctx, 
             request.getParameter("id" ));
   s.setState( org.apache.tomcat.core.ServerSession.STATE_EXPIRED );
%>

</body>
</html>
