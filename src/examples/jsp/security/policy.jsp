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
<h1>Security test </h1>

Attempting to create /tmp/sectest
<% 
 try {
  java.io.FileWriter f=new java.io.FileWriter("/tmp/sectest");
  f.write("Can write"); 
  f.close();
 } catch(Exception ex ) {
%>
<h2>Exception <%= ex %> </h2>
<%  ex.printStackTrace( new java.io.PrintWriter(out) ); 
 }
%>
