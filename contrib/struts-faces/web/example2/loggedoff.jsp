<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="f" uri="http://java.sun.com/jsf/core" %>
<%@ taglib prefix="h" uri="http://java.sun.com/jsf/html" %>
<%@ taglib prefix="s" uri="http://struts.apache.org/tags-faces" %>
<%@ taglib prefix="t" uri="/WEB-INF/struts-tiles.tld" %>


<!--

 Copyright 2002,2004 The Apache Software Foundation.
 
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


<h:form                id="loggedoff">
  <h:panelGrid    columns="1">
    <h:commandLink     id="register"
                   action="#{loggedOff.register}"
                immediate="true">
      <s:message      key="loggedoff.register"/>
    </h:commandLink>
    <h:commandLink     id="logon"
                   action="#{loggedOff.logon}"
                immediate="true">
      <s:message      key="loggedoff.logon"/>
    </h:commandLink>
  </h:panelGrid>
</h:form>
