//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.message;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class SendableHeader extends ColumbaHeader implements HeaderInterface{
	
	private Vector recipients;
    private int accountUid;
    
	public SendableHeader()
	{
		super();
		
		recipients = new Vector();
	}
	
	 public int getAccountUid()
    {
        return accountUid;
    }


    public Vector getRecipients()
    {
        return recipients;
    }

    public void setAccountUid( int uid )
    {
        accountUid = uid;
    }

    public void setRecipients( Vector rcpt )
    {
        recipients = rcpt;
    }
    
    public Object clone() {

		SendableHeader header = new SendableHeader();
		Hashtable ht = new Hashtable();

		for (Enumeration e = getHashtable().keys(); e.hasMoreElements();) {
			Object o = e.nextElement();
			ht.put(o, getHashtable().get(o));

		}
		header.setHashtable(ht);
		
		header.setRecipients( getRecipients() );
		
		header.setAccountUid( getAccountUid() );

		//header.setHashtable( (Hashtable) hashTable.clone() );

		//ColumbaHeader header = (ColumbaHeader) super.clone();

		return header;
	}
	

}
