<HTML>
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

<HEAD><TITLE> 
	Calendar: A JSP APPLICATION
</TITLE></HEAD>


<BODY BGCOLOR="white">
<jsp:useBean id="table" scope="session" class="cal.TableBean" />

<% 
	String time = request.getParameter ("time");
%>

<FONT SIZE=5> Please add the following event:
<BR> <h3> Date <%= table.getDate() %>
<BR> Time <%= time %> </h3>
</FONT>
<FORM METHOD=POST ACTION=cal1.jsp>
<BR> 
<BR> <INPUT NAME="date" TYPE=HIDDEN VALUE="current">
<BR> <INPUT NAME="time" TYPE=HIDDEN VALUE=<%= time %>
<BR> <h2> Description of the event <INPUT NAME="description" TYPE=TEXT SIZE=20> </h2>
<BR> <INPUT TYPE=SUBMIT VALUE="submit">
</FORM>

</BODY>
</HTML>

