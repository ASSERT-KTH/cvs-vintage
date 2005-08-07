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
package org.columba.core.gui.base;

import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;

/**
 * Resembles a JComboBox, using a JButton and a JMenu. This has the advantage
 * that the user immediately sees all available items.
 * <p>
 * Use ItemListener to get notified of selection changes.
 * <p>
 * TODO (@author fdietz): use JComboBox button layout/ui
 * 
 * @author fdietz
 */
public class ComboMenu extends JButton implements ActionListener {

	protected JPopupMenu popupMenu;

	protected Vector listeners;

	private ButtonGroup group;

	public ComboMenu() {
		super();

		//setIcon(ImageLoader.getImageIcon("stock_down-16.png"));
		setIcon(new AscendingIcon());
		setMargin(new Insets(1, 3, 1, 3));
		setIconTextGap(12);

		setHorizontalTextPosition(SwingConstants.LEFT);

		listeners = new Vector();

		group = new ButtonGroup();

		popupMenu = new JPopupMenu();

		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				popupMenu.show(ComboMenu.this, 0, getHeight() - 2);
			}
		});
	}

	/**
	 * @deprecated use default constructor instead and <code>addMenuItem</code>.
	 *             this way the menu entries can be localized correctly
	 */
	public ComboMenu(String[] list) {
		this();

		for (int i = 0; i < list.length; i++) {

			if (i == 0)
				setText(list[i]);

			if (list[i].equalsIgnoreCase("separator")) {
				popupMenu.addSeparator();
			} else {
				addMenuItem(list[i], list[i]);
			}
		}

	}

	public JRadioButtonMenuItem addMenuItem(String name, String localizedName) {

		JRadioButtonMenuItem m = new JRadioButtonMenuItem(localizedName);

		m.setActionCommand(name);

		m.addActionListener(this);

		group.add(m);

		popupMenu.add(m);

		if (popupMenu.getComponentCount() == 1) {
			setText(localizedName);
			m.setSelected(true);
		}
		
		return m;
	}

	public void addSeparator() {
		popupMenu.addSeparator();
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		String action = arg0.getActionCommand();
		JRadioButtonMenuItem m = (JRadioButtonMenuItem) arg0.getSource();

		setText(m.getText());

		fireItemStateChanged(new ItemEvent(this, 0, action, ItemEvent.SELECTED));

	}

	public void addItemListener(ItemListener l) {
		listeners.add(l);
	}
	
	public void setSelectedItem( int nr ) {
		JRadioButtonMenuItem item = (JRadioButtonMenuItem)popupMenu.getComponent(0);
		
		setText(item.getText());
		fireItemStateChanged(new ItemEvent(this, 0, item.getActionCommand(), ItemEvent.SELECTED));
	}

	protected void fireItemStateChanged(ItemEvent event) {
		Iterator it = listeners.iterator();
		while (it.hasNext()) {
			ItemListener l = (ItemListener) it.next();
			l.itemStateChanged(event);
		}
	}
}