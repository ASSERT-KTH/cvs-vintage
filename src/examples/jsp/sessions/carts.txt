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

<jsp:useBean id="cart" scope="session" class="sessions.DummyCart" />

<jsp:setProperty name="cart" property="*" />
<%
	cart.processRequest(request);
%>


<FONT size = 5 COLOR="#CC0000">
<br> You have the following items in your cart:
<ol>
<% 
	String[] items = cart.getItems();
	for (int i=0; i<items.length; i++) {
%>
<li> <%= items[i] %> 
<%
	}
%>
</ol>

</FONT>

<hr>
<%@ include file ="/jsp/sessions/carts.html" %>
</html>
