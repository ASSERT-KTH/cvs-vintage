// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.outbox;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.columba.mail.folder.headercache.DefaultHeaderBinding;
import org.columba.mail.message.IColumbaHeader;

import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class OutboxHeaderBinding extends DefaultHeaderBinding {

	public Object entryToObject(TupleInput in) {
		IColumbaHeader header = (IColumbaHeader) super.entryToObject(in);
		
		Integer accountUid = new Integer(in.readInt());
		header.getAttributes().put("columba.accountuid", accountUid);

		int listSize = in.readInt();
		List recipients = new ArrayList(listSize);
		for( int i=0; i<listSize; i++) {
			recipients.add(i,in.readString());
		}
		
		header.getAttributes().put("columba.recipients", recipients);
		
		return header;
	}

	public void objectToEntry(Object arg0, TupleOutput out) {
		super.objectToEntry(arg0, out);
		IColumbaHeader header = (IColumbaHeader)  arg0;
			
		out.writeInt(((Integer)header.getAttributes().get("columba.accountuid")).intValue());
		
		List recipients = (List)header.getAttributes().get("columba.recipients");
		out.writeInt(recipients.size());
		for( Iterator it=recipients.iterator(); it.hasNext();) {
			out.writeString((String) it.next());
		}

	}

}
