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
package org.columba.mail.folder.headercache;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.columba.core.config.HeaderItem;
import org.columba.core.config.TableItem;
import org.columba.core.util.BooleanCompressor;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.FolderInconsistentException;
import org.columba.mail.folder.LocalFolder;
import org.columba.mail.message.HeaderInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class LocalHeaderCache extends AbstractHeaderCache {

	private static final int NULL = 0;
	private static final int STRING = 1;
	private static final int DATE = 2;
	private static final int BOOLEAN = 3;
	private static final int INTEGER = 4;

	private String[] columnNames;

	private static final String[] standardCols =
		{ "Status", "Attachment", "Flagged", "Priority", "Flagged" };

	private static final List standardList = Arrays.asList(standardCols);

	public LocalHeaderCache(LocalFolder folder) {
		super(folder);

		columnNames = null;
	}

	protected void loadHeader(ObjectInputStream p, HeaderInterface h)
		throws Exception {
		try {
			Integer uid = new Integer(p.readInt());
			h.set("columba.uid", uid);

			int compressedFlags = p.readInt();
			h.set(
				"columba.flags.seen",
				BooleanCompressor.decompress(compressedFlags, 0));
			h.set(
				"columba.flags.answered",
				BooleanCompressor.decompress(compressedFlags, 1));
			h.set(
				"columba.flags.flagged",
				BooleanCompressor.decompress(compressedFlags, 2));
			h.set(
				"columba.flags.expunged",
				BooleanCompressor.decompress(compressedFlags, 3));
			h.set(
				"columba.flags.draft",
				BooleanCompressor.decompress(compressedFlags, 4));
			h.set(
				"columba.flags.recent",
				BooleanCompressor.decompress(compressedFlags, 5));
			h.set(
				"columba.attachment",
				BooleanCompressor.decompress(compressedFlags, 6));

			h.set("columba.date", new Date(p.readLong()));

			h.set("columba.size", new Integer(p.readInt()));

			h.set("columba.from", p.readUTF());

			h.set("columba.priority", new Integer(p.readInt()));

			h.set("columba.host", p.readUTF());

			loadColumnNames();

			int classCode;
			for (int j = 0; j < columnNames.length; j++) {
				classCode = p.readInt();

				switch (classCode) {
					case NULL :
						{
							break;
						}
					case STRING :
						{
							h.set(columnNames[j], p.readUTF());
							break;
						}

					case INTEGER :
						{
							h.set(columnNames[j], new Integer(p.readInt()));
							break;
						}

					case BOOLEAN :
						{
							h.set(columnNames[j], new Boolean(p.readBoolean()));
							break;
						}

					case DATE :
						{
							h.set(columnNames[j], new Date(p.readLong()));
							break;
						}
				}
			}
		} catch (IOException e) {
			throw new FolderInconsistentException();
		}

	}

	protected void loadColumnNames() {
		if (columnNames == null) {

			TableItem v = MailConfig.getMainFrameOptionsConfig().getTableItem();
			String column;

			ArrayList cols = new ArrayList(v.count());

			for (int j = 0; j < v.count(); j++) {
				HeaderItem headerItem = v.getHeaderItem(j);
				String name = (String) headerItem.get("name");
				if (!standardList.contains(name)) {
					cols.add(name);
				}
			}

			columnNames = new String[cols.size()];
			cols.toArray(columnNames);
		}
	}

	protected void saveHeader(ObjectOutputStream p, HeaderInterface h)
		throws Exception {
		p.writeInt(((Integer) h.get("columba.uid")).intValue());

		p.writeInt(
			BooleanCompressor.compress(
				new Boolean[] {
					(Boolean) h.get("columba.flags.seen"),
					(Boolean) h.get("columba.flags.answered"),
					(Boolean) h.get("columba.flags.flagged"),
					(Boolean) h.get("columba.flags.expunged"),
					(Boolean) h.get("columba.flags.draft"),
					(Boolean) h.get("columba.flags.recent"),
					(Boolean) h.get("columba.attachment")}));

		p.writeLong(((Date) h.get("columba.date")).getTime());

		p.writeInt(((Integer) h.get("columba.size")).intValue());

		p.writeUTF((String) h.get("columba.from"));

		p.writeInt(((Integer) h.get("columba.priority")).intValue());

		p.writeUTF((String) h.get("columba.host"));

		loadColumnNames();

		Object o;
		for (int j = 0; j < columnNames.length; j++) {
			o = h.get(columnNames[j]);
			if (o == null) {
				p.writeInt(NULL);
			} else if (o instanceof String) {
				p.writeInt(STRING);
				p.writeUTF((String) o);
			} else if (o instanceof Integer) {
				p.writeInt(INTEGER);
				p.writeInt(((Integer) o).intValue());
			} else if (o instanceof Boolean) {
				p.writeInt(BOOLEAN);
				p.writeBoolean(((Boolean) o).booleanValue());
			} else if (o instanceof Date) {
				p.writeInt(DATE);
				p.writeLong(((Date) o).getTime());
			}

		}
	}
}
