package org.columba.mail.folder.virtual;

import java.util.Enumeration;

import org.columba.mail.folder.Folder;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class VirtualHeader extends ColumbaHeader implements HeaderInterface {

	protected Folder srcFolder;
	protected Object srcUid;
	protected ColumbaHeader srcHeader;

	public VirtualHeader(
		ColumbaHeader header,
		Folder srcFolder,
		Object srcUid) {
		super();
		
		if( header == null )
			System.out.println("test");

		for (Enumeration e = header.getHashtable().keys();
			e.hasMoreElements();
			) {
			Object o = e.nextElement();

			getHashtable().put(
				(String) o,
				header.getHashtable().get((String) o));

		}

		this.srcFolder = srcFolder;
		this.srcUid = srcUid;
	}

	/**
	 * Returns the srcFolder.
	 * @return Folder
	 */
	public Folder getSrcFolder() {
		return srcFolder;
	}

	/**
	 * Returns the srcUid.
	 * @return Object
	 */
	public Object getSrcUid() {
		return srcUid;
	}

}
