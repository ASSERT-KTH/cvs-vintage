<html>
<!--
  Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.

 This software is the confidential and proprietary information of Sun
 Microsystems, Inc. ("Confidential Information").  You shall not
 disclose such Confidential Information and shall use it only in
 accordance with the terms of the license agreement you entered into
 with Sun.

 SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 THIS SOFTWARE OR ITS DERIVATIVES.
-->

<jsp:useBean id="cb" scope="session" class="colors.ColorGameBean" />
<jsp:setProperty name="cb" property="*" />

<%
	cb.processRequest(request);
%>

<body bgcolor=<%= cb.getColor1() %>>
<font size=6 color=<%= cb.getColor2() %>>
<p>

<% if (cb.getHint()==true) { %>
	
	<p> Hint #1: Vampires prey at night!
	<p>  <p> Hint #2: Nancy without the n.

<% } %>

<% if  (cb.getSuccess()==true) { %>

    <p> CONGRATULATIONS!!
	<% if  (cb.getHintTaken()==true) { %>
    
        <p> ( although I know you cheated and peeked into the hints)

	<% } %>

<% } %>

<p> Total attempts so far: <%= cb.getAttempts() %>
<p>

<p>

<form method=POST action=colrs.jsp>

Color #1: <input type=text name= color1 size=16>

<br>

Color #2: <input type=text name= color2 size=16>

<p>

<input type=submit name=action value="Submit">
<input type=submit name=action value="Hint">

</form>

</font>
</body>
</html>
