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

<body bgcolor="white">
<font size=5 color="red">
<%! String[] fruits; %>
<jsp:useBean id="foo" scope="page" class="checkbox.CheckTest" />

<jsp:setProperty name="foo" property="fruit" param="fruit" />
<hr>
The checked fruits (got using request) are: <br>
<% 
	fruits = request.getParameterValues("fruit");
%>
<ul>
<%
    if (fruits != null) {
	  for (int i = 0; i < fruits.length; i++) {
%>
<li>
<%
	      out.println (fruits[i]);
	  }
	} else out.println ("none selected");
%>
</ul>
<br>
<hr>

The checked fruits (got using beans) are <br>

<% 
		fruits = foo.getFruit();
%>
<ul>
<%
    if (!fruits[0].equals("1")) {
	  for (int i = 0; i < fruits.length; i++) {
%>
<li>
<%
		  out.println (fruits[i]);
	  }
	} else out.println ("none selected");
%>
</ul>
</font>
</body>
</html>
