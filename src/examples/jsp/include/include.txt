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

<font color="red">

<%@ page buffer="5" autoFlush="false" %>

<p>In place evaluation of another JSP which gives you the current time:

<%@ include file="foo.jsp" %>

<p> <jsp:include page="/jsp/include/foo.html" flush="true"/> by including the output of another JSP:

<jsp:include page="foo.jsp" flush="true"/>

:-) 

</html>
