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
