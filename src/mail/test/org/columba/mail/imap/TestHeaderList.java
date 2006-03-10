package org.columba.mail.imap;

import java.io.IOException;

import org.columba.mail.folder.IHeaderListCorruptedListener;
import org.columba.mail.folder.headercache.MemoryHeaderList;
import org.columba.mail.message.IPersistantHeaderList;

public class TestHeaderList extends MemoryHeaderList implements
		IPersistantHeaderList {

	public void persist() throws IOException {
	}

	public void addHeaderListCorruptedListener(
			IHeaderListCorruptedListener listener) {
	}

}
