<html>
<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<%@ page buffer="none" %>
<!--
  Copyright 1999-2004 The Apache Software Foundation
 
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

<head>

    <title>Context list</title>
</head>

<body bgcolor="white">

<adm:admin/>

<h2>Prepare to restart</h2>

<%
try {
  cm.stop(); // all contexts stopped
  cm.shutdown(); // all contexts removed
  cm.init();
  cm.start();
} catch(Exception ex) {
  ex.printStackTrace();
}
 System.out.println("Done restarting ");
%>

<h1>Server restarted </h1>

</body>
</html>
