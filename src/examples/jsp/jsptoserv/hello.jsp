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

<h1>
I have been invoked by
<% out.print (request.getAttribute("servletName").toString()); %>
Servlet.
</h1>

</html>