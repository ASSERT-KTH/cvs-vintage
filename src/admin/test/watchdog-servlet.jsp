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
<h1>Tomcat Self-test</h1> 

<%@ taglib uri="http://jakarta.apache.org/taglibs/tomcat_admin-1.0" 
           prefix="adm" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/ant-1.0" 
           prefix="ant" %>

This page will show the result of executing the sanity test suite. 
You can see the context log <a href="/servlet-tests/context_log.txt">here</a>

<adm:admin ctxPath="/servlet-tests" 
	   action="setLogger" 
	   value="webapps/servlet-tests/context_log.txt" />

<ant:gtest />

<pre>
<ant:ant>
  <ant:target name="main" />
  <ant:property name="ant.file" 
		location="/WEB-INF/scripts/watchdog-servlet.xml" 
		webApp="/admin" />
  <ant:property name="debug"  param="debug" />
  <ant:property name="outputType" value="html"  />
  <ant:property name="port" param="port" />
  <ant:property name="http.protocol" param="server.proto" />
  <ant:property name="host" param="host" />
</ant:ant>
</pre>
