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
package org.columba.mail.gui.composer;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseAdapter;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.columba.core.gui.util.ImageLoader;
import org.columba.ristretto.message.MimeHeader;
import org.columba.ristretto.message.MimePart;

/**
 * Attachment view. Used in the composer to show a list of
 * attachments. Is part of a controller-view framework together
 * with AttachmentController.
 * 
 * @author frd
 * 
 */
public class AttachmentView extends JList implements ListDataListener {

	/** Reference to the controller controlling this view */
	private AttachmentController controller;

	/** Underlying data model for the list view */
	private DefaultListModel listModel;

	/**
	 * Default constructor. Sets up the view and stores a
	 * reference to the controller for later use.
	 * @param controller	Reference to the controller of this view
	 */
	public AttachmentView(AttachmentController controller) {
		super();

		this.controller = controller;

		listModel = new DefaultListModel();

		setModel(listModel);

		setCellRenderer(new ListRenderer());

	}

	/**
	 * Installs the attachment controller as listener
	 * @param c	Controller of this view
	 */
	public void installListener(AttachmentController c) {
		listModel.addListDataListener(this);
		addKeyListener(c);
	}

	/**
	 * Adds a popup listener
	 * @param a	Listener to add
	 */
	public void addPopupListener(MouseAdapter a) {
		addMouseListener(a);
	}

	/**
	 * Adds an attachment to be displayed in the view
	 * @param mp	Attachment to add
	 */
	public void add(MimePart mp) {
		listModel.addElement(mp);
	}

	/**
	 * Remove attachment from the view by index
	 * @param index	Index of attachment to remove (zero based)
	 */
	public void remove(int index) {
		listModel.remove(index);
	}

	/**
	 * Remove attachment from the view
	 * @param mp	Attachment to remove
	 */
	public void remove(MimePart mp) {
		listModel.removeElement(mp);
	}

	/**
	 * Clears the view, i.e. removes all attachments.
	 */
	public void clear() {
		listModel.clear();
	}
		
	/**
	 * Gets an attachment from the view by index
	 * @param index	Index of attachment (zero based)
	 * @return		The specified attachment
	 */
	public MimePart get(int index) {
		return (MimePart) listModel.get(index);
	}

	/**
	 * Gets number of attachments currently displayed in the view
	 */
	public int count() {
		return listModel.size();
	}

	/**
	 * Returns the index of an attachment
	 * @param mp	Attachment to get index of
	 * @return		Index of attachment in the list
	 */
	public int indexOf(MimePart mp) {
		return listModel.indexOf(mp);
	}

	/**
	 * TODO: Add javadoc comment
	 */
	public void fixSelection(int x, int y) {
		int index = locationToIndex(new Point(x, y));

		setSelectedIndex(index);
	}

	/********************* ListRenderer *******************************/

	class ListRenderer extends JLabel implements ListCellRenderer {

		ImageIcon image1 = ImageLoader.getSmallImageIcon("attachment.png");

		public ListRenderer() {
			setOpaque(true);
			//setHorizontalAlignment(CENTER);
			//setVerticalAlignment(CENTER);
		}
		public Component getListCellRendererComponent(
			JList list,
			Object value,
			int index,
			boolean isSelected,
			boolean cellHasFocus) {
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}

			MimePart mp = (MimePart) value;
			if (mp == null)
				return this;

			MimeHeader header = mp.getHeader();
			//String type = header.getContentType();
			String type = header.getMimeType().getType();
			//String subtype = header.getContentSubtype();
			String subtype = header.getMimeType().getSubtype();

			StringBuffer buf = new StringBuffer();

			if (mp.getHeader().getFileName() != null)
				buf.append((String) mp.getHeader().getFileName());
			else {

				buf.append(" (");
				buf.append(type);
				buf.append("/");
				buf.append(subtype);
				buf.append(")");
			}

			setText(buf.toString());
			setIcon(image1);
			return this;
		}
	}

	/***************** ListDataModelListener *******************/

	public void contentsChanged(ListDataEvent e) {
		change();
	}
	public void intervalAdded(ListDataEvent e) {
		change();
	}
	public void intervalRemoved(ListDataEvent e) {
		change();
	}

	protected void change() {
	}

}
