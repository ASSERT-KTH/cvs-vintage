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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Locale;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.core.main.MainInterface;
import org.columba.core.util.GlobalResourceLoader;

public class FontSelectionDialog extends JDialog implements ActionListener,
		ListSelectionListener {
	public static final int APPROVE_OPTION = 1;

	public static final int CANCEL_OPTION = 0;

	private static final String RESOURCE_BUNDLE_PATH = "org.columba.core.i18n.dialog";

	protected JList fontList;

	protected JList styleList;

	protected JList sizeList;

	protected JTextField preview;

	protected JTextField fontName;

	protected JTextField styleName;

	protected JTextField sizeName;

	protected JLabel fontLabel;

	protected JLabel sizeLabel;

	protected JLabel styleLabel;

	protected JLabel previewLabel;

	protected JButton okButton;

	protected JButton cancelButton;

	protected Font font;

	protected int status;

	public FontSelectionDialog(Font f) {
		super(MainInterface.frameModel.getActiveFrame(), true);

		setTitle(GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "font",
				"title"));

		font = f;

		setSize(450, 325);

		initData();
		initComponents();

		setLocationRelativeTo(null);
	}

	private void initComponents() {
		JScrollPane scroller;
		GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		final double w1 = 3.0;
		final double w2 = 2.0;
		final double w3 = 0.7;

		getContentPane().setLayout(gridbag);

		// Spacings
		c.insets = new Insets(1, 3, 1, 3);

		// First Line with Labels
		c.gridheight = 1;
		c.weighty = 1.0;
		c.weightx = w1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		gridbag.setConstraints(fontLabel, c);
		getContentPane().add(fontLabel);

		c.weightx = w2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(styleLabel, c);
		getContentPane().add(styleLabel);

		c.weightx = w3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(sizeLabel, c);
		getContentPane().add(sizeLabel);

		// Second Line with Names
		c.weightx = w1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		gridbag.setConstraints(fontName, c);
		getContentPane().add(fontName);

		c.weightx = w2;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(styleName, c);
		getContentPane().add(styleName);

		c.weightx = w3;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(sizeName, c);
		getContentPane().add(sizeName);

		// Third Line with Lists
		c.weighty = 6.0;

		scroller = new JScrollPane(fontList);
		c.weightx = w1;
		c.fill = GridBagConstraints.BOTH;
		c.gridwidth = 1;
		gridbag.setConstraints(scroller, c);
		getContentPane().add(scroller);

		scroller = new JScrollPane(styleList);
		c.weightx = w2;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(scroller, c);
		getContentPane().add(scroller);

		scroller = new JScrollPane(sizeList);
		c.weightx = w3;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(scroller, c);
		getContentPane().add(scroller);

		// 4. Line with PreviewLabel
		c.weighty = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(previewLabel, c);
		getContentPane().add(previewLabel);

		// 5. Line with Preview
		c.weightx = 1.0;
		c.weighty = 5.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.RELATIVE;
		c.fill = GridBagConstraints.BOTH;
		gridbag.setConstraints(preview, c);
		getContentPane().add(preview);

		// 6. Line with Buttons
		okButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				"global", "global", "ok"));
		okButton.addActionListener(this);

		c.weightx = 1.0;
		c.weighty = 1.0;
		c.insets = new Insets(10, 5, 10, 5);
		c.anchor = GridBagConstraints.EAST;
		c.gridwidth = 1;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(okButton, c);
		getContentPane().add(okButton);

		cancelButton = new ButtonWithMnemonic(GlobalResourceLoader.getString(
				"global", "global", "cancel"));
		cancelButton.addActionListener(this);

		c.weightx = 1.0;
		c.anchor = GridBagConstraints.WEST;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		gridbag.setConstraints(cancelButton, c);
		getContentPane().add(cancelButton);

		Dimension okSize;
		Dimension cancelSize;

		okSize = okButton.getPreferredSize();
		cancelSize = cancelButton.getPreferredSize();

		if (okSize.width < cancelSize.width) {
			okSize.width = cancelSize.width;
			okButton.setPreferredSize(okSize);
		}

		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this,
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);
	}

	void initData() {
		GraphicsEnvironment gEnv = GraphicsEnvironment
				.getLocalGraphicsEnvironment();

		//String envfonts[] = gEnv.getAvailableFontFamilyNames();
		String[] envfonts = gEnv.getAvailableFontFamilyNames(Locale
				.getDefault());
		fontList = new JList(envfonts);
		fontList.setSelectedIndex(0);

		styleList = new JList(new Object[] {
				GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "font",
						"plain"),
				GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "font",
						"bold"),
				GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "font",
						"italic"),
				GlobalResourceLoader.getString(RESOURCE_BUNDLE_PATH, "font",
						"bold_italic") });
		styleList.setSelectedIndex(0);

		//fill sizes string array with numbers from 7 to 18
		Object[] sizes = new String[12];

		for (int i = 7; i < 19; i++) {
			sizes[i - 7] = Integer.toString(i);
		}

		sizeList = new JList(sizes);
		sizeList.setSelectedIndex(0);

		preview = new JTextField("abcdefgh ABCDEFGH");
		preview.setHorizontalAlignment(JTextField.CENTER);

		styleName = new JTextField();
		sizeName = new JTextField();
		fontName = new JTextField();

		if (font == null) {
			font = preview.getFont();
		}

		fontList.setSelectedValue(font.getName(), true);
		styleList.setSelectedIndex(font.getStyle());
		sizeList.setSelectedValue(new Integer(font.getSize()).toString(), true);

		fontName.setText((String) fontList.getSelectedValue());
		styleName.setText((String) styleList.getSelectedValue());
		sizeName.setText((String) sizeList.getSelectedValue());

		styleList.addListSelectionListener(this);
		sizeList.addListSelectionListener(this);
		fontList.addListSelectionListener(this);

		fontLabel = new JLabel(GlobalResourceLoader.getString(
				RESOURCE_BUNDLE_PATH, "font", "font"));
		sizeLabel = new JLabel(GlobalResourceLoader.getString(
				RESOURCE_BUNDLE_PATH, "font", "size"));
		styleLabel = new JLabel(GlobalResourceLoader.getString(
				RESOURCE_BUNDLE_PATH, "font", "style"));
		previewLabel = new JLabel(GlobalResourceLoader.getString(
				RESOURCE_BUNDLE_PATH, "font", "preview"));
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == okButton) {
			status = APPROVE_OPTION;
		} else if (source == cancelButton) {
			status = CANCEL_OPTION;
		}

		dispose();
	}

	public int showDialog() {
		setVisible(true);

		return status;
	}

	public Font getSelectedFont() {
		return font;
	}

	public void valueChanged(ListSelectionEvent e) {
		Object list = e.getSource();
		String fontchoice;
		int stChoice;
		int siChoice;

		if (list == fontList) {
			fontName.setText((String) fontList.getSelectedValue());
		} else if (list == styleList) {
			styleName.setText((String) styleList.getSelectedValue());
		} else if (list == sizeList) {
			sizeName.setText((String) sizeList.getSelectedValue());
		}

		fontchoice = fontName.getText();
		stChoice = styleList.getSelectedIndex();

		siChoice = new Integer(sizeName.getText()).intValue();

		font = new Font(fontchoice, stChoice, siChoice);

		preview.setFont(font);
		preview.repaint();
	}
}