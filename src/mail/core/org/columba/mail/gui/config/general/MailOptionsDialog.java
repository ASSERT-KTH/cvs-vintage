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

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;
import org.columba.core.gui.util.DefaultFormBuilder;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.MailConfig;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.layout.FormLayout;

/**
 * Mail General Options Dialog
 * 
 * @author fdietz
 */
public class MailOptionsDialog extends JDialog implements ActionListener {


	JButton okButton;
	JButton cancelButton;
	JButton helpButton;

	JLabel markLabel2;
	JCheckBox markCheckBox;
	JTextField markTextField;
	JCheckBox preferHtmlCheckBox;
	JCheckBox enableSmiliesCheckBox;
	JCheckBox quotedColorCheckBox;
	JButton quotedColorButton;

	JCheckBox emptyTrashCheckBox;

	JComboBox toolbarComboBox;

	JLabel spellLabel;
	JButton spellButton;

	JCheckBox emptySubjectCheckBox;

	JLabel forwardLabel;
	JComboBox forwardComboBox;

	public MailOptionsDialog(JFrame frame) {
		super(
			frame,
			MailResourceLoader.getString("dialog", "general", "dialog_title"),
			true);

		initComponents();

		layoutComponents();

		updateComponents(true);

		pack();

		setLocationRelativeTo(null);

		setVisible(true);
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

			// composer
			String path =
				MailConfig.getComposerOptionsConfig().getSpellcheckItem().get(
					"executable");
			spellButton.setText(path);

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
					item.set(
						"toolbar",
						"text_position",
						Boolean.TRUE.toString());
				else
					item.set(
						"toolbar",
						"text_position",
						Boolean.FALSE.toString());

			}

			// get language configuration
			XmlElement locale =
				Config.get("options").getElement("/options/locale");

			// composer
			MailConfig.getComposerOptionsConfig().getSpellcheckItem().set(
				"executable",
				spellButton.getText());

		}
	}

	protected void initComponents() {

		// general
		markCheckBox =
			new JCheckBox(
				MailResourceLoader.getString(
					"dialog",
					"general",
					"mark_messages_read"));

		markTextField = new JTextField(3);

		markLabel2 =
			new JLabel(
				MailResourceLoader.getString("dialog", "general", "seconds"));

		//TODO:LOCALIZE
		emptyTrashCheckBox = new JCheckBox("Empty trash on exit");
		emptyTrashCheckBox.setEnabled(false);

		//TODO:LOCALIZE
		enableSmiliesCheckBox = new JCheckBox("Display emoticons as graphics");

		//TODO:LOCALIZE
		quotedColorCheckBox = new JCheckBox("Color quoted text");
		quotedColorButton = new JButton("..");

		preferHtmlCheckBox =
			new JCheckBox(
				MailResourceLoader.getString(
					"dialog",
					"general",
					"prefer_html"));

		JLabel toolbarLabel =
			new JLabel(
				MailResourceLoader.getString("dialog", "general", "toolbar"));

		toolbarComboBox =
			new JComboBox(
				new String[] {
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
						"toolbar_beside")});
		toolbarLabel.setLabelFor(toolbarComboBox);

		// composer
		spellLabel =
			new JLabel(
				MailResourceLoader.getString(
					"dialog",
					"general",
					"aspell_path"));

		spellButton = new JButton("aspell.exe");
		spellButton.setActionCommand("PATH");
		spellButton.addActionListener(this);
		spellLabel.setLabelFor(spellButton);

		emptySubjectCheckBox =
			new JCheckBox(
				MailResourceLoader.getString(
					"dialog",
					"general",
					"ask_on_empty_subject"));
		emptySubjectCheckBox.setEnabled(false);

		// TODO: LOCALIZE
		forwardLabel = new JLabel("Forward message ");
		String[] items = { "As Attachment", "Quoted" };

		forwardComboBox = new JComboBox(items);

		// button panel
		okButton = new JButton(MailResourceLoader.getString("global", "ok"));
		//mnemonic
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);

		cancelButton =
			new JButton(MailResourceLoader.getString("global", "cancel"));
		//mnemonic
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);

		helpButton =
			new JButton(MailResourceLoader.getString("global", "help"));
		helpButton.setActionCommand("HELP");
		helpButton.addActionListener(this);

	}

	protected void layoutComponents() {

		JPanel contentPane = new JPanel();
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout());
		
		// Create a FormLayout instance. 
			FormLayout layout =
				new FormLayout(
					"12dlu, pref, 3dlu, min(10dlu;pref), 3dlu, pref",
		// 3 columns
	"");

		// create a form builder
		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
		
		// create EmptyBorder between components and dialog-frame 
		builder.setDefaultDialogBorder();
		
		// skip the first column
		builder.setLeadingColumnOffset(1);

		// Add components to the panel:
		// TODO: LOCALIZE
		builder.appendSeparator("General Options");
		builder.nextLine();

		builder.append(preferHtmlCheckBox, 4);
		builder.nextLine();

		builder.append(enableSmiliesCheckBox, 4);
		builder.nextLine();

		builder.append(quotedColorCheckBox, quotedColorButton);
		builder.nextLine();

		builder.append(markCheckBox, markTextField, markLabel2);

		builder.nextLine();

		// TODO: LOCALIZE
		builder.appendSeparator("Composing Messages");
		builder.nextLine();

		builder.append(emptySubjectCheckBox, 4);
		builder.nextLine();

		builder.append(forwardLabel, forwardComboBox);
		builder.nextLine();

		/*
		builder.append(spellLabel, spellButton);
		builder.nextLine();
		*/
		
		contentPane.add(builder.getPanel(), BorderLayout.CENTER);

		// init bottom panel with OK, Cancel buttons
		
		JPanel bottomPanel = new JPanel(new BorderLayout(0, 0));
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(11, 11, 11, 11));
		
		buttonPanel.add(okButton);
		
		buttonPanel.add(cancelButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		contentPane.add(bottomPanel, BorderLayout.SOUTH);
		
		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(
			this,
			"CANCEL",
			KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
			JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();

		if (action.equals("OK")) {

			setVisible(false);

			updateComponents(false);

		} else if (action.equals("CANCEL")) {

			setVisible(false);

		} else if (action.equals("PATH")) {
			final JFileChooser fc = new JFileChooser();

			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();

				spellButton.setText(file.getPath());
			}
		}
	}
}
