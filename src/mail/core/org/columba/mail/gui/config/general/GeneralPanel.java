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

import javax.swing.*;

import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;
import org.columba.core.util.GlobalResourceLoader;
import org.columba.core.xml.XmlElement;

import org.columba.mail.config.MailConfig;
import org.columba.mail.util.MailResourceLoader;

public class GeneralPanel extends JPanel implements ActionListener {
	JLabel markLabel1, markLabel2;
	JTextField markTextField;

	JLabel codepageLabel;
	JComboBox codepageComboBox;

	JCheckBox emptyTrashCheckBox;

	JCheckBox preferHtmlCheckBox;

	JComboBox toolbarComboBox;

	public GeneralPanel() {
		initComponent();
	}

	public void updateComponents(boolean b) {

		if (b) {

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
			if (item.getBoolean("toolbar", "enable_icon"))
				withIcon = true;
			boolean enableText = false;
			if (item.getBoolean("toolbar", "enable_text"))
				enableText = true;
			boolean alignment = false;
			if (item.getBoolean("toolbar", "text_position"))
				alignment = true;

			int state = -1;
			if (withIcon && !enableText)
				state = 0;
			else if (!withIcon && enableText)
				state = 1;
			else if (withIcon && enableText) {
				if (alignment)
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
				html.addAttribute("prefer", Boolean.TRUE.toString());
			else
				html.addAttribute("prefer", Boolean.FALSE.toString());

			GuiItem item = Config.getOptionsConfig().getGuiItem();

			int state = toolbarComboBox.getSelectedIndex();

			if (state == 0) {
				item.set("toolbar", "enable_text", Boolean.FALSE.toString());
				item.set("toolbar", "enable_icon", Boolean.TRUE.toString());
			} else if (state == 1) {
				item.set("toolbar", "enable_text", Boolean.TRUE.toString());
				item.set("toolbar", "enable_icon", Boolean.FALSE.toString());
			} else if (state >= 2) {
				item.set("toolbar", "enable_text", Boolean.TRUE.toString());
				item.set("toolbar", "enable_icon", Boolean.TRUE.toString());

				if (state == 2)
					item.set("toolbar", "text_position", Boolean.TRUE.toString());
				else
					item.set("toolbar", "text_position", Boolean.FALSE.toString());

			}

		}
	}

	protected void initComponent() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
		JPanel markPanel = new JPanel();
		markPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		markPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		markLabel1 = new JLabel(MailResourceLoader.getString(
                                        "dialog",
                                        "general",
                                        "mark_messages_read"));
		markPanel.add(markLabel1);
		markTextField = new JTextField(3);
		markPanel.add(markTextField);
		markLabel2 = new JLabel(MailResourceLoader.getString(
                                        "dialog",
                                        "general",
                                        "seconds"));
		markPanel.add(markLabel2);
		add(markPanel);
		JPanel codepagePanel = new JPanel();
		codepagePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		codepagePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		codepageLabel = new JLabel(MailResourceLoader.getString(
                                        "dialog",
                                        "general",
                                        "locale"));
		codepagePanel.add(codepageLabel);
		codepageComboBox = new JComboBox(GlobalResourceLoader.getAvailableLocales());
                codepageComboBox.setRenderer(new DefaultListCellRenderer() {
                        public Component getListCellRendererComponent(
                                                JList list, Object value,
                                                int index, boolean isSelected, boolean hasFocus) {
                                
                                JLabel label = (JLabel)super.getListCellRendererComponent(
                                                list, value, index, isSelected, hasFocus);
                                label.setText(((Locale)value).getDisplayName());
                                return label;
                        }
                });
		codepageComboBox.setActionCommand("LOCALE");
		codepageComboBox.addActionListener(this);
		codepageLabel.setLabelFor(codepageComboBox);
		codepagePanel.add(codepageComboBox);
		add(codepagePanel);
		//LOCALIZE
		emptyTrashCheckBox = new JCheckBox("Empty trash on exit");
		emptyTrashCheckBox.setEnabled(false);
		add(emptyTrashCheckBox);
		preferHtmlCheckBox = new JCheckBox(MailResourceLoader.getString(
                                        "dialog",
                                        "general",
                                        "prefer_html"));
		add(preferHtmlCheckBox);
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		toolbarPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		JLabel toolbarLabel = new JLabel(MailResourceLoader.getString(
                                        "dialog",
                                        "general",
                                        "toolbar"));
		toolbarPanel.add(toolbarLabel);
		toolbarComboBox = new JComboBox(new String[] {
                                MailResourceLoader.getString(
                                        "dialog",
                                        "general",
                                        "toolbar_icons"),
                                MailResourceLoader.getString(
                                        "dialog",
                                        "general",
                                        "toolbar_text"),
                                MailResourceLoader.getString(
                                        "dialog",
                                        "general",
                                        "toolbar_below"),
                                MailResourceLoader.getString(
                                        "dialog",
                                        "general",
                                        "toolbar_beside") });
		toolbarLabel.setLabelFor(toolbarComboBox);
		toolbarPanel.add(toolbarComboBox);
		add(toolbarPanel);
		add(Box.createVerticalGlue());
	}

	public void actionPerformed(ActionEvent ev) {
		String str = ev.getActionCommand();

		if (str.equals("LOCALE")) {
                        Locale.setDefault((Locale)codepageComboBox.getSelectedItem());
                        GlobalResourceLoader.reload();
                        //update GUI
		}
	}
}
