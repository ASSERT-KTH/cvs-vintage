// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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

	private ComposerModel model;

	//private IconPanel attachment;
	private DefaultListModel listModel;

	private JList list;

	public AttachmentView(ComposerModel model) {
		super();

		this.model = model;

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