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
package org.columba.mail.folder.headercache;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Date;

import org.columba.core.logging.ColumbaLogger;

/**
 * @author fdietz
 */
public class ObjectWriter {

	protected File file;
	protected FileOutputStream ostream;
	protected ObjectOutputStream oos;

	private static final int NULL = 0;
	private static final int STRING = 1;
	private static final int DATE = 2;
	private static final int BOOLEAN = 3;
	private static final int INTEGER = 4;
	private static final int COLOR = 5;
	private static final int OBJECT = 6;

	public ObjectWriter(File file) throws Exception {
		this.file = file;
		ostream = new FileOutputStream(file.getPath());
		oos = new ObjectOutputStream(ostream);
	}

	public void writeObject(Object value) throws Exception {
		Object o = value;

		ColumbaLogger.log.debug("value=" + value);
		if (o == null) {
			oos.writeInt(NULL);
		} else if (o instanceof String) {
			oos.writeInt(STRING);
			oos.writeUTF((String) o);
		} else if (o instanceof Integer) {
			oos.writeInt(INTEGER);
			oos.writeInt(((Integer) o).intValue());
		} else if (o instanceof Boolean) {
			oos.writeInt(BOOLEAN);
			oos.writeBoolean(((Boolean) o).booleanValue());
		} else if (o instanceof Date) {
			oos.writeInt(DATE);
			oos.writeLong(((Date) o).getTime());
		} else if (o instanceof Color) {
			oos.writeInt(COLOR);
			oos.writeInt( ((Color)o).getRGB());
		}	
		else {		
			oos.writeInt(OBJECT);
			oos.writeObject(value);
		}
	}

	public void close() throws Exception {

		oos.close();
		ostream.close();
	}
}
