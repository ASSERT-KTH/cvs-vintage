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
<body bgcolor="lightblue">

	<%@ page errorPage="errorpge.jsp" %>
	<jsp:useBean id="foo" scope="request" class="error.Smart" />
	<% 
		String name = null;

		if (request.getParameter("name") == null) {
	%>
	<%@ include file="/jsp/error/error.html" %>
	<%
		} else {
		  foo.setName(request.getParameter("name"));
		  if (foo.getName().equalsIgnoreCase("integra"))
		  	name = "acura";
		  if (name.equalsIgnoreCase("acura")) {
	%>

	<H1> Yes!!! <a href="http://www.acura.com">Acura</a> is my favorite car.

	<% 
		  }
		}	
	%>	
</body>
</html>

