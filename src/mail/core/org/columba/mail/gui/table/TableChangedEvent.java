package org.columba.mail.gui.table;

import org.columba.mail.folder.Folder;
import org.columba.mail.message.HeaderInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TableChangedEvent {
	
	public final static int UPDATE = 0;
	public final static int ADD = 1;
	public final static int REMOVE = 2;
	public final static int MARK = 3;
	
	protected Folder srcFolder;
	protected HeaderInterface[] headerList;
	protected Object[] uids;
	protected int markVariant;
	protected int eventType; 
	
	
	/**
	 * Constructor for TableChangedEvent.
	 */
	public TableChangedEvent( int eventType) {
		this.eventType = eventType;
	}
	
	public TableChangedEvent( int eventType, Folder srcFolder) {
		this.eventType = eventType;
		this.srcFolder = srcFolder;
	
	}
	
	public TableChangedEvent( int eventType, Folder srcFolder, Object[] uids) {
		this.eventType = eventType;
		this.srcFolder = srcFolder;
		this.uids = uids;
	}
	
	public TableChangedEvent( int eventType, Folder srcFolder, HeaderInterface[] headerList) {
		this.eventType = eventType;
		this.srcFolder = srcFolder;
		this.headerList = headerList;
	}
	
	public TableChangedEvent( int eventType, Folder srcFolder, Object[] uids, int markVariant) {
		this.eventType = eventType;
		this.srcFolder = srcFolder;
		this.uids = uids;
		this.markVariant = markVariant;
	}
	
	

	/**
	 * Returns the markVariant.
	 * @return int
	 */
	public int getMarkVariant() {
		return markVariant;
	}

	/**
	 * Returns the srcFolder.
	 * @return Folder
	 */
	public Folder getSrcFolder() {
		return srcFolder;
	}

	/**
	 * Returns the uids.
	 * @return Object[]
	 */
	public Object[] getUids() {
		return uids;
	}

	/**
	 * Returns the eventType.
	 * @return int
	 */
	public int getEventType() {
		return eventType;
	}

	/**
	 * Returns the headerList.
	 * @return HeaderInterface[]
	 */
	public HeaderInterface[] getHeaderList() {
		return headerList;
	}

}
