/*
 *
 * Copyright (c) 1999 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 *
 * SUN MAKES NO REPRESENTATIONS OR WARRANTIES ABOUT THE SUITABILITY OF THE
 * SOFTWARE, EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
 * PURPOSE, OR NON-INFRINGEMENT. SUN SHALL NOT BE LIABLE FOR ANY DAMAGES
 * SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING OR DISTRIBUTING
 * THIS SOFTWARE OR ITS DERIVATIVES.
 *
 * @author Mandar Raje
 */

package cal;

import java.beans.*;
import javax.servlet.http.*;
import javax.servlet.*;
import java.util.Hashtable;

public class TableBean {

  Hashtable table;
  JspCalendar JspCal;
  Entries entries;
  String date;
  String name = null;
  String email = null;
  boolean processError = false;

  public TableBean () {
    this.table = new Hashtable (10);
    this.JspCal = new JspCalendar ();
    this.date = JspCal.getCurrentDate ();
  }

  public void setName (String nm) {
    this.name = nm;
  }

  public String getName () {
    return this.name;
  }
  
  public void setEmail (String mail) {
    this.email = mail;
  }

  public String getEmail () {
    return this.email;
  }

  public String getDate () {
    return this.date;
  }

  public Entries getEntries () {
    return this.entries;
  }

  public void processRequest (HttpServletRequest request) {

    // Get the name and e-mail.
    this.processError = false;
    if (name == null || name.equals("")) setName(request.getParameter ("name"));  
    if (email == null || email.equals("")) setEmail(request.getParameter ("email"));
    if (name == null || email == null ||
		name.equals("") || email.equals("")) {
      this.processError = true;
      return;
    }

    // Get the date.
    String dateR = request.getParameter ("date");
    if (dateR == null) date = JspCal.getCurrentDate ();
    else if (dateR.equalsIgnoreCase("next")) date = JspCal.getNextDate ();
    else if (dateR.equalsIgnoreCase("prev")) date = JspCal.getPrevDate ();

    entries = (Entries) table.get (date);
    if (entries == null) {
      entries = new Entries ();
      table.put (date, entries);
    }

    // If time is provided add the event.
	String time = request.getParameter("time");
    if (time != null) entries.processRequest (request, time);
  }

  public boolean getProcessError () {
    return this.processError;
  }
}






