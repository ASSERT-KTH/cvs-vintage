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
package org.columba.chat.ui.presence;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;

import org.columba.api.statusbar.IStatusBarExtension;
import org.columba.chat.command.ChangePresenceCommand;
import org.columba.chat.command.ChatCommandReference;
import org.columba.chat.ui.frame.api.IChatFrameMediator;
import org.columba.chat.ui.presence.api.IPresenceController;
import org.columba.chat.ui.util.ResourceLoader;
import org.columba.core.command.CommandProcessor;
import org.jivesoftware.smack.packet.Presence;

/**
 * @author fdietz
 * 
 */
public class PresenceComboBox extends JPanel implements ItemListener,
		IPresenceController, IStatusBarExtension {

	private JLabel label;

	private JComboBox comboBox;

	private ImageIcon available = ResourceLoader.getImage("online.png");

	private ImageIcon extendedaway = ResourceLoader
			.getImage("extended_away.png");

	private ImageIcon away = ResourceLoader.getImage("away.png");

	private ImageIcon busy = ResourceLoader.getImage("unavailable.png");

	private ImageIcon message = ResourceLoader.getImage("message.png");

	private ImageIcon offline = ResourceLoader.getImage("offline.png");

	private IChatFrameMediator mediator;

	public PresenceComboBox(IChatFrameMediator mediator) {
		super();

		this.mediator = mediator;

		setBorder(BorderFactory.createEmptyBorder(2,2,2,2));

		setBackground(UIManager.getColor("Tree.background"));
		
		comboBox = new JComboBox();
		// comboBox.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		comboBox.addItem("Available");
		comboBox.addItem("Busy");
		comboBox.addItem("Away");
		comboBox.addItem("Extended  Aways");
		// checkBox.addItem("Custom Message...", null);
		// checkBox.addItem("Leave...");

		comboBox.setRenderer(new ItemRenderer());

		label = new JLabel();
		label.setBorder(BorderFactory.createEmptyBorder(0,2,0,2));
		label.setIcon(offline);

		setLayout(new BorderLayout());

		add(label, BorderLayout.WEST);

		add(comboBox, BorderLayout.CENTER);

		comboBox.addItemListener(this);

		// addItemListener(this);
	}

	// private void addStatus(String tooltip, ImageIcon icon) {
	// JLabel label = new JLabel(icon);
	// label.setToolTipText(tooltip);
	//
	// comboBox.addItem(tooltip);
	// }

	public void addItemListener(ItemListener l) {
		comboBox.addItemListener(l);
	}

	/**
	 * @see java.awt.event.ItemListener#itemStateChanged(java.awt.event.ItemEvent)
	 */
	public void itemStateChanged(ItemEvent e) {
		Object source = e.getItemSelectable();

		int index = ((JComboBox) source).getSelectedIndex();

		Presence p = null;
		switch (index) {
		case 0: {
			label.setIcon(available);
			p = new Presence(Presence.Type.AVAILABLE);
			p.setStatus("Available");
			CommandProcessor.getInstance().addOp(
					new ChangePresenceCommand(mediator,
							new ChatCommandReference(p)));
			break;
		}
		case 1: {
			label.setIcon(busy);
			p = new Presence(Presence.Type.UNAVAILABLE);
			p.setStatus("Busy");
			CommandProcessor.getInstance().addOp(
					new ChangePresenceCommand(mediator,
							new ChatCommandReference(p)));

			break;
		}
		case 2: {
			label.setIcon(away);
			p = new Presence(Presence.Type.UNAVAILABLE);
			p.setStatus("Away");
			CommandProcessor.getInstance().addOp(
					new ChangePresenceCommand(mediator,
							new ChatCommandReference(p)));

			break;
		}
		case 3: {
			label.setIcon(extendedaway);
			p = new Presence(Presence.Type.UNAVAILABLE);
			p.setStatus("Extended Away");
			CommandProcessor.getInstance().addOp(
					new ChangePresenceCommand(mediator,
							new ChatCommandReference(p)));

			break;
		}
		}

	}

	public JComponent getView() {
		return this;
	}

	public class ItemRenderer extends JLabel implements ListCellRenderer {

		public ItemRenderer() {
			/*
			 * setIconTextGap(5); setVerticalAlignment(JLabel.CENTER);
			 */
		}

		/** {@inheritDoc} */
		public Component getListCellRendererComponent(JList list, Object value,
				int index, boolean isSelected, boolean cellHasFocus) {

			setText((String) value);

			return this;
		}
	}

}
