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

package sessions;

import javax.servlet.http.*;
import java.util.Vector;
import java.util.Enumeration;

public class DummyCart {
    Vector v = new Vector();
    String submit = null;
    String item = null;
    
    private void addItem(String name) {
	v.addElement(name);
    }

    private void removeItem(String name) {
	v.removeElement(name);
    }

    public void setItem(String name) {
	item = name;
    }
    
    public void setSubmit(String s) {
	submit = s;
    }

    public String[] getItems() {
	String[] s = new String[v.size()];
	v.copyInto(s);
	return s;
    }
    
    public void processRequest(HttpServletRequest request) {
	// null value for submit - user hit enter instead of clicking on 
	// "add" or "remove"
	if (submit == null) 
	    addItem(item);

	if (submit.equals("add"))
	    addItem(item);
	else if (submit.equals("remove")) 
	    removeItem(item);
	
	// reset at the end of the request
	reset();
    }

    // reset
    private void reset() {
	submit = null;
	item = null;
    }
}
