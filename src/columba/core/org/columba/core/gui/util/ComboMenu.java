// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.util;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;

/**
 * Resembles a JComboBox, using a JButton and a JMenu. This has the
 * advantage that the user immediately sees all available items.
 * <p>
 * Use ItemListener to get notified of selection changes.
 * <p>
 * TODO: use JComboBox button layout/ui 
 * 
 * @author fdietz
 */
public class ComboMenu extends JButton implements ActionListener {

	protected JPopupMenu popupMenu;
	protected Vector listeners;

	/**
	 *  
	 */
	public ComboMenu(String[] list) {
		super();

		setIcon(ImageLoader.getImageIcon("stock_down-16.png"));
		setMargin(new Insets(1, 3, 1, 3));
		setIconTextGap(12);

		setHorizontalTextPosition(SwingConstants.LEFT);

		listeners = new Vector();

		popupMenu = new JPopupMenu();

		for (int i = 0; i < list.length; i++) {

			JMenuItem m = new JMenuItem(list[i]);
			m.setActionCommand(list[i]);
			m.addActionListener(this);
			popupMenu.add(m);
		}

		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				popupMenu.show(ComboMenu.this, 0, 0);
			}
		});
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();

		setText(action);

		fireItemStateChanged(new ItemEvent(this, 0, action, ItemEvent.SELECTED));

	}

	public void addItemListener(ItemListener l) {
		listeners.add(l);
	}

	protected void fireItemStateChanged(ItemEvent event) {
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			ItemListener l = (ItemListener) it.next();
			l.itemStateChanged(event);
		}
	}
}