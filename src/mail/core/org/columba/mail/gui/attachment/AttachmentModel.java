package org.columba.mail.gui.attachment;

import java.util.LinkedList;
import java.util.Vector;

import org.columba.mail.folder.Folder;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AttachmentModel {

	private Folder folder;
	private Object uid;

	private LinkedList displayedMimeParts;

	private MimePartTree collection;

	public AttachmentModel() {
	}

	public synchronized void setFolder(Folder folder) {
		this.folder = folder;
	}

	public synchronized void setUid(Object uid) {
		this.uid = uid;
	}

	public Folder getFolder() {
		return folder;
	}

	public Object getUid() {
		return uid;
	}

	/**
	 * Returns the collection.
	 * @return MimePartTree
	 */
	public MimePartTree getCollection() {
		return collection;
	}

	/**
	 * Sets the collection.
	 * @param collection The collection to set
	 */
	public void setCollection(MimePartTree collection) {
		this.collection = collection;

		// Get all MimeParts
		displayedMimeParts = collection.getAllLeafs();

		// Remove the BodyPart(s) if any
		MimePart bodyPart = collection.getFirstTextPart("plain");
		if (bodyPart != null) {
			MimePart bodyParent = (MimePart) bodyPart.getParent();
			if (bodyParent != null) {
				if (bodyParent
					.getHeader()
					.contentSubtype
					.equals("alternative")) {
					Vector bodyParts = bodyParent.getChilds();
					displayedMimeParts.removeAll(bodyParts);
				} else {
					displayedMimeParts.remove(bodyPart);
				}
			}
		}
	}

	/**
	 * Returns the displayedMimeParts.
	 * @return LinkedList
	 */
	public LinkedList getDisplayedMimeParts() {
		return displayedMimeParts;
	}

	/**
	 * Sets the displayedMimeParts.
	 * @param displayedMimeParts The displayedMimeParts to set
	 */
	public void setDisplayedMimeParts(LinkedList displayedMimeParts) {
		this.displayedMimeParts = displayedMimeParts;
	}

}
