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

import java.util.List;

import org.columba.ristretto.message.Attributes;
import org.columba.ristretto.message.Flags;
import org.columba.ristretto.message.Header;


/**
 * Contains additional attributes useful for sending
 * messages.
 * <p>
 * This includes a list of recipients and a UID uniquely
 * identifying an account.
 * <p>
 *
 *  @author fdietz, tstich
 */
public class SendableHeader extends ColumbaHeader {
    public SendableHeader() {
        super();
    }

    public int getAccountUid() {
        return ((Integer) attributes.get("columba.accountuid")).intValue();
    }

    public List getRecipients() {
        return ((List) attributes.get("columba.recipients"));
    }

    public void setAccountUid(int uid) {
        attributes.put("columba.accountuid", new Integer(uid));
    }

    public void setRecipients(List rcpt) {
        attributes.put("columba.recipients", rcpt);
    }

    public Object clone() {
        SendableHeader clone = new SendableHeader();
        clone.attributes = (Attributes) this.attributes.clone();
        clone.flags = (Flags) this.flags.clone();
        clone.header = (Header) this.header.clone();

        return clone;
    }
}
