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
package org.columba.mail.pop3;

import java.util.Date;

import org.columba.mail.folder.headercache.CachedHeaderfields;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.IColumbaHeader;

import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.bind.tuple.TupleInput;
import com.sleepycat.bind.tuple.TupleOutput;

public class POP3HeaderBinding extends TupleBinding {

	public Object entryToObject(TupleInput in) {
		ColumbaHeader header = new ColumbaHeader();
		
		String[] columnNames = CachedHeaderfields.POP3_HEADERFIELDS;
		Class[] columnTypes = CachedHeaderfields.POP3_HEADERFIELDS_TYPE;

		for (int j = 0; j < columnNames.length; j++) {
				Object value = null;

				if (columnTypes[j] == Integer.class) {
					value = new Integer(in.readInt());
				} else if (columnTypes[j] == Date.class) {
					value = new Date(in.readLong());
				} else if (columnTypes[j] == String.class) {
					value = in.readString();
				} else if ( columnTypes[j] == Boolean.class){
					value = new Boolean(in.readBoolean());
				}

				if (value != null) {
					header.set(columnNames[j], value);
				}
		}
		
		return header;
	}

	public void objectToEntry(Object arg0, TupleOutput out) {
		IColumbaHeader header = (IColumbaHeader) arg0; 
		
		String[] columnNames = CachedHeaderfields.POP3_HEADERFIELDS;
		Class[] columnTypes = CachedHeaderfields.POP3_HEADERFIELDS_TYPE;
		Object o;

		for (int j = 0; j < columnNames.length; j++) {
			o = header.get(columnNames[j]);

			if (columnTypes[j] == Integer.class) {
				if (o == null) {
					out.writeInt(0);
				} else {
					out.writeInt(((Integer) o).intValue());
				}
			} else if (columnTypes[j] == Date.class) {
				if (o == null) {
					out.writeLong(System.currentTimeMillis());
				} else {
					out.writeLong(((Date) o).getTime());
				}
			} else if (columnTypes[j] == String.class) {
				if (o == null) {
					out.writeString("");
				} else {
					out.writeString((String) o);
				}
			} else if (columnTypes[j] == Boolean.class) {
				if( o == null) {
					out.writeBoolean(false);
				} else {
					out.writeBoolean(((Boolean) o).booleanValue());
				}
			}
		}
	}

}
