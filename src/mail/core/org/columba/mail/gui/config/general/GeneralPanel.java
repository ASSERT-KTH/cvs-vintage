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
package org.columba.mail.gui.config.general;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.MailConfig;

public class GeneralPanel extends JPanel implements ActionListener {
	JLabel markLabel1, markLabel2;
	JTextField markTextField;

	JLabel codepageLabel;
	JButton codepageButton;

	JCheckBox emptyTrashCheckBox;

	JCheckBox preferHtmlCheckBox;

	JComboBox toolbarComboBox;

	public GeneralPanel() {
		initComponent();
	}

	public void updateComponents(boolean b) {

		if (b == true) {

			XmlElement markasread =
				MailConfig.get("options").getElement("/options/markasread");

			String delay = markasread.getAttribute("delay", "2");

			markTextField.setText(delay);

			XmlElement html =
				MailConfig.getMainFrameOptionsConfig().getRoot().getElement(
					"/options/html");

			boolean preferhtml =
				new Boolean(html.getAttribute("prefer")).booleanValue();
			if (preferhtml == true)
				preferHtmlCheckBox.setSelected(true);
			else
				preferHtmlCheckBox.setSelected(false);

			GuiItem item = Config.getOptionsConfig().getGuiItem();
			boolean withIcon = false;
			if (item.getBoolean("toolbar", "enable_icon") == true)
				withIcon = true;
			boolean enableText = false;
			if (item.getBoolean("toolbar", "enable_text") == true)
				enableText = true;
			boolean alignment = false;
			if (item.getBoolean("toolbar", "text_position") == true)
				alignment = true;

			int state = -1;
			if ((withIcon == true) && (enableText == false))
				state = 0;
			else if ((withIcon == false) && (enableText == true))
				state = 1;
			else if ((withIcon == true) && (enableText == true)) {
				if (alignment == true)
					state = 2;
				else
					state = 3;
			}

			toolbarComboBox.setSelectedIndex(state);

		} else {

			XmlElement markasread =
				MailConfig.get("options").getElement("/options/markasread");

			markasread.addAttribute("delay", markTextField.getText());

			XmlElement html =
				MailConfig.getMainFrameOptionsConfig().getRoot().getElement(
					"/options/html");

			if (preferHtmlCheckBox.isSelected())
				html.addAttribute("prefer", "true");
			else
				html.addAttribute("prefer", "false");

			GuiItem item = Config.getOptionsConfig().getGuiItem();

			int state = toolbarComboBox.getSelectedIndex();

			if (state == 0) {
				item.set("toolbar", "enable_text", "false");
				item.set("toolbar", "enable_icon", "true");
			} else if (state == 1) {
				item.set("toolbar", "enable_text", "true");
				item.set("toolbar", "enable_icon", "false");
			} else if (state >= 2) {
				item.set("toolbar", "enable_text", "true");
				item.set("toolbar", "enable_icon", "true");

				if (state == 2)
					item.set("toolbar", "text_position", "true");
				else
					item.set("toolbar", "text_position", "false");

			}

		}
	}

	protected void initComponent() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
		JPanel markPanel = new JPanel();
		markPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		markPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		//LOCALIZE
		markLabel1 = new JLabel("Mark messages as read after");
		markPanel.add(markLabel1);
		markTextField = new JTextField("2", 3);
		markPanel.add(markTextField);
		markLabel2 = new JLabel("seconds");
		markPanel.add(markLabel2);
		add(markPanel);
		JPanel codepagePanel = new JPanel();
		codepagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		codepagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		//LOCALIZE
		codepageLabel = new JLabel("Default locale:");
		codepagePanel.add(codepageLabel);
		codepageButton = new JButton(Locale.getDefault().getDisplayName());
		codepageButton.setEnabled(false);
		codepageButton.setActionCommand("CODEPAGE");
		codepageButton.addActionListener(this);
		codepageLabel.setLabelFor(codepageButton);
		codepagePanel.add(codepageButton);
		add(codepagePanel);
		//LOCALIZE
		emptyTrashCheckBox = new JCheckBox("Empty trash on exit");
		emptyTrashCheckBox.setEnabled(false);
		add(emptyTrashCheckBox);
		//LOCALIZE
		preferHtmlCheckBox =
			new JCheckBox("Prefer HTML messages, if available");
		add(preferHtmlCheckBox);
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		toolbarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		//LOCALIZE
		JLabel toolbarLabel = new JLabel("Toolbar:");
		toolbarPanel.add(toolbarLabel);
		//LOCALIZE
		toolbarComboBox =
			new JComboBox(
				new String[] {
					"Icons only",
					"Text only",
					"Text below Icons",
					"Text beside Icons" });
		toolbarLabel.setLabelFor(toolbarComboBox);
		toolbarPanel.add(toolbarComboBox);
		add(toolbarPanel);
		add(Box.createVerticalGlue());
	}

	protected JMenu createSubMenu(
		Locale[] locales,
		int startIndex,
		int stopIndex) {
		JMenu menu = new JMenu("more..");

		for (int i = startIndex; i < stopIndex; i++) {
			JMenuItem item = new JMenuItem(locales[i].getDisplayName());
			menu.add(item);
		}

		return menu;
	}

	public void actionPerformed(ActionEvent ev) {
		String str = ev.getActionCommand();

		if (str.equals("CODEPAGE")) {
			JPopupMenu menu = new JPopupMenu();
			JMenu selectedMenu = null;
			Locale[] locales = Locale.getAvailableLocales();
			int counter = 0;
			boolean firstMenu = true;
			for (int i = 0; i < locales.length; i++) {
				Locale locale = (Locale) locales[i];

				if (firstMenu == true) {
					if (counter < 10) {
						JMenuItem item = new JMenuItem(locale.getDisplayName());
						menu.add(item);
					} else {
						firstMenu = false;
					}
				} else {
					if (counter % 10 == 0) {
						JMenu submenu = createSubMenu(locales, i - 10, i);
						if (menu.getComponents().length <= 10) {
							menu.add(submenu);
							selectedMenu = submenu;
						} else {
							selectedMenu.add(submenu);
							selectedMenu = submenu;
						}

						counter = 0;
					}
				}

				counter++;

				/*
				System.out.println("locale name:"+locale.getDisplayName() );
				System.out.println("locale language:"+locale.getDisplayLanguage() );
				System.out.println("locale variant:"+locale.getDisplayVariant() );
				System.out.println("locale country:"+locale.getCountry() );
				System.out.println("locale language:"+locale.getLanguage() );
				System.out.println("locale language2:"+locale.getISO3Country() );
				System.out.println("locale language3:"+locale.getISO3Language() );
				System.out.println("locale tostring:"+locale.toString() );
				*/

			}

			JButton button = (JButton) ev.getSource();
			menu.show(button, button.getX(), button.getY());
		}
	}
}