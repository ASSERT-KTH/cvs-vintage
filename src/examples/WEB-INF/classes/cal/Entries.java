/*
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
 */

package cal;

import java.util.Enumeration;
import java.util.Hashtable;
import javax.servlet.http.*;

public class Entries {

  private Hashtable entries;
  private static final String[] time = {"8am", "9am", "10am", "11am", "12pm", 
					"1pm", "2pm", "3pm", "4pm", "5pm", "6pm",
					"7pm", "8pm" };
  public static final int rows = 12;

  public Entries () {   
   entries = new Hashtable (rows);
   for (int i=0; i < rows; i++) {
     entries.put (time[i], new Entry(time[i]));
   }
  }

  public int getRows () {
    return rows;
  }

  public Entry getEntry (int index) {
    return (Entry)this.entries.get(time[index]);
  }

  public int getIndex (String tm) {
    for (int i=0; i<rows; i++)
      if(tm.equals(time[i])) return i;
    return -1;
  }

  public void processRequest (HttpServletRequest request, String tm) {
    int index = getIndex (tm);
    if (index >= 0) {
      String descr = request.getParameter ("description");
      ((Entry)entries.get(time[index])).setDescription (descr);
    }
  }

}













