package org.columba.mail.folder;

import java.io.IOException;

import org.columba.mail.message.IHeaderList;

public interface IHeaderListStore {

	public void persistHeaderList(IHeaderList list) throws IOException;
	
	public void restoreHeaderList(IHeaderList list) throws IOException;
}
