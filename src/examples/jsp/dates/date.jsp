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
<jsp:useBean id='clock' scope='page' class='dates.JspCalendar' type="dates.JspCalendar" />

<font size=4>
<ul>
<li>	Day of month: is  <jsp:getProperty name="clock" property="dayOfMonth"/>
<li>	Year: is  <jsp:getProperty name="clock" property="year"/>
<li>	Month: is  <jsp:getProperty name="clock" property="month"/>
<li>	Time: is  <jsp:getProperty name="clock" property="time"/>
<li>	Date: is  <jsp:getProperty name="clock" property="date"/>
<li>	Day: is  <jsp:getProperty name="clock" property="day"/>
<li>	Day Of Year: is  <jsp:getProperty name="clock" property="dayOfYear"/>
<li>	Week Of Year: is  <jsp:getProperty name="clock" property="weekOfYear"/>
<li>	era: is  <jsp:getProperty name="clock" property="era"/>
<li>	DST Offset: is  <jsp:getProperty name="clock" property="dSTOffset"/>
<li>	Zone Offset: is  <jsp:getProperty name="clock" property="zoneOffset"/>
</ul>
</font>

</body>
</html>
