/*
 * Copyright (C) The Apache Software Foundation. All rights reserved.
 *
 * This software is published under the terms of the Apache Software
 * License version 1.1, a copy of which has been included with this
 * distribution in the LICENSE.txt file.  */

package org.apache.log4j.helpers;

import java.util.Date;
import java.util.Calendar;
import java.util.TimeZone;
import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.DateFormat;

 

/**

   Formats a {@link Date} in the format "HH:mm:ss,SSS" for example,
   "15:49:37,459".
   
   @author Ceki G&uuml;lc&uuml;
   
   @since 0.7.5
*/
public class AbsoluteTimeDateFormat extends DateFormat {

  /**
     String constant used to specify {@link
     org.apache.log4j.helpers.AbsoluteTimeDateFormat} in layouts. Current
     value is <b>ABSOLUTE</b>.  */
  public final static String ABS_TIME_DATE_FORMAT = "ABSOLUTE";

  /**
     String constant used to specify {@link
     org.apache.log4j.helpers.DateTimeDateFormat} in layouts.  Current
     value is <b>DATE</b>.
  */
  public final static String DATE_AND_TIME_DATE_FORMAT = "DATE";

  /**
     String constant used to specify {@link
     org.apache.log4j.helpers.ISO8601DateFormat} in layouts. Current
     value is <b>ISO8601</b>.
  */
  public final static String ISO8601_DATE_FORMAT = "ISO8601";

  public
  AbsoluteTimeDateFormat() {
    setCalendar(Calendar.getInstance());
  }
  
  public
  AbsoluteTimeDateFormat(TimeZone timeZone) {
    setCalendar(Calendar.getInstance(timeZone));
  }
  
  /**
     Appends to <code>sbuf</code> the time in the format
     "HH:mm:ss,SSS" for example, "15:49:37,459"

     @param date the date to format
     @param sbuf the string buffer to write to
     @param fieldPosition remains untouched
    */
  public
  StringBuffer format(Date date, StringBuffer sbuf,
		      FieldPosition fieldPosition) {

    // We use a previously instantiated Date object to avoid the needless
    // creation of temporary objects. This saves a few micro-secs.
    calendar.setTime(date); 
    
    int hour = calendar.get(Calendar.HOUR_OF_DAY);
    if(hour < 10) {
      sbuf.append('0');
    }
    sbuf.append(hour);
    sbuf.append(':');

    int mins = calendar.get(Calendar.MINUTE);
    if(mins < 10) {
      sbuf.append('0');
    }
    sbuf.append(mins);
    sbuf.append(':');
    
    int secs = calendar.get(Calendar.SECOND);
    if(secs < 10) {
      sbuf.append('0');
    }
    sbuf.append(secs);
    sbuf.append(',');
    
    int millis = calendar.get(Calendar.MILLISECOND);
    if(millis < 100) 
      sbuf.append('0');
    if(millis < 10) 
      sbuf.append('0');
    
    sbuf.append(millis);
    return sbuf;
  }

  /**
     This method does not do anything but return <code>null</code>.
   */
  public
  Date parse(String s, ParsePosition pos) {
    return null;
  }  
}
