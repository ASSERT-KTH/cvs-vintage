package org.columba.mail.gui.attachment;

import java.util.Vector;

import org.columba.core.command.DefaultCommandReference;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.FolderTreeNode;
import org.columba.mail.gui.table.MessageSelectionListener;
import org.columba.mail.gui.table.TableSelectionManager;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AttachmentSelectionManager extends TableSelectionManager implements MessageSelectionListener {

	protected Integer[] address;

	protected Vector attachmentListenerList;
	/**
	 * Constructor for AttachmentSelectionManager.
	 */
	public AttachmentSelectionManager() {
		super();
		attachmentListenerList = new Vector();
	}

	public void addAttachmentSelectionListener(AttachmentSelectionListener listener) {
		attachmentListenerList.add(listener);
	}

	public void fireAttachmentSelectionEvent(Integer[] old, Integer[] newAddress) {
		address = newAddress;

		for (int i = 0; i < treeListenerList.size(); i++) {
			AttachmentSelectionListener l =
				(AttachmentSelectionListener) attachmentListenerList.get(i);
			l.attachmentSelectionChanged(newAddress);
		}
	}

	public Integer[] getAddress()
	{
		return address;
	}

	public DefaultCommandReference[] getSelection() {
		//System.out.println("folder="+getFolder());
		//System.out.println("uids="+getUids());
		
		FolderCommandReference[] references = new FolderCommandReference[1];
		references[0] =
			new FolderCommandReference((Folder) getFolder(), getUids(), address);

		return references;
	}
	
	public void setFolder( FolderTreeNode node )
	{
		this.folder = node;
	}
	
	public void setUids( Object[] uids )
	{
		this.uids = uids;
	}
	
	/**
	 * @see org.columba.mail.gui.table.MessageSelectionListener#messageSelectionChanged(java.lang.Object)
	 */
	public void messageSelectionChanged(Object[] newUidList) {
		uids = newUidList;
	}

}
