<html>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<head>
<!--
  Copyright 2001-2004 The Apache Software Foundation
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

    <title>Session info</title>
</head>

<body bgcolor="white">

<adm:admin ctxPathParam="ctx"/>
<adm:moduleAdmin var="store" 
		 type="org.apache.tomcat.modules.session.SimpleSessionStore" />

  <h3>Invalidating  <%= request.getParameter("id" ) %></h3>

<% 
   org.apache.tomcat.core.ServerSession s=store.findSession( ctx, 
             request.getParameter("id" ));
   if (s != null ) {
        s.setState( org.apache.tomcat.core.ServerSession.STATE_EXPIRED );
%>
<h4> Session invalidated successfully</h4>
<%        
   } else
%>

<h4> Session not found , already invalidated </h4>

</body>
</html>
