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
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.message.MimeHeader;
import org.columba.mail.message.MimePart;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class AttachmentView extends JScrollPane implements ListDataListener {

	private AttachmentController controller;
		
	//private IconPanel attachment;
	private DefaultListModel listModel;

	private JList list;

	public AttachmentView(AttachmentController controller) {
		super();

		this.controller = controller;

		listModel = new DefaultListModel();

		list = new JList(listModel);
		list.setCellRenderer(new ListRenderer());
		setViewportView(list);

	}

	public void installListener( AttachmentController c ) {
		listModel.addListDataListener(this);
		list.addKeyListener(c);
	}

	public void addPopupListener(MouseAdapter a) {
		list.addMouseListener(a);
	}

	public void add(MimePart mp) {
		listModel.addElement(mp);
	}

	public void remove(int index) {
		listModel.remove(index);
	}
	
	public void remove(MimePart mp )
	{
		listModel.removeElement(mp);
	}
	
	public MimePart get( int index )
	{
		return (MimePart) listModel.get(index);
	}

	public int count() {
		return listModel.size();
	}

	public int indexOf(MimePart mp) {
		return listModel.indexOf(mp);
	}

	public MimePart getSelectedValue() {
		return (MimePart) list.getSelectedValue();
	}

	public Object[] getSelectedValues() {
		return (Object[]) list.getSelectedValues();
	}

	public void fixSelection(int x, int y) {
		int index = list.locationToIndex(new Point(x, y));

		list.setSelectedIndex(index);
	}

	/********************* ListRenderer *******************************/

	class ListRenderer extends JLabel implements ListCellRenderer {

		ImageIcon image1 =
			ImageLoader.getSmallImageIcon("attachment.png");

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
			if ( mp == null ) return this;
			
			MimeHeader header = mp.getHeader();
			String type = header.contentType;
			String subtype = header.contentSubtype;
			
			StringBuffer buf = new StringBuffer();
			
			buf.append( (String) mp.getHeader().getFileName() );
			
			/*
			buf.append(" (");
			buf.append( type );
			buf.append("/");
			buf.append( subtype );
			buf.append(")");
			*/
			
			setText( buf.toString() );
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