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
package org.columba.core.gui.externaltools;

import java.awt.BorderLayout;
import java.awt.Font;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

import net.javaprog.ui.wizard.AbstractStep;
import net.javaprog.ui.wizard.DataModel;

import org.columba.core.externaltools.AbstractExternalToolsPlugin;
import org.columba.mail.gui.util.URLLabel;

/**
 * Presents some information about the external tool which the
 * user is going to configure in further steps.
 * <p>
 * Usually this should should include a short explanation about
 * what the tool does, where to download, etc.
 * 
 * @author fdietz
 */
public class DescriptionStep extends AbstractStep {

	protected DataModel data;

	/**
	 * @param arg0
	 * @param arg1
	 */
	public DescriptionStep(DataModel data) {
		// TODO: i18n
		super("Description", "Tool Description");

		this.data = data;
	}

	/* (non-Javadoc)
	 * @see net.javaprog.ui.wizard.AbstractStep#createComponent()
	 */
	protected JComponent createComponent() {

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		AbstractExternalToolsPlugin plugin =
			(AbstractExternalToolsPlugin) data.getData("Plugin");

		Font font = UIManager.getFont("Label.font");
		String name = font.getName();
		int size = font.getSize();

		JTextPane textPane = new JTextPane();
		HTMLEditorKit editorKit = new HTMLEditorKit();
		StyleSheet styles = new StyleSheet();
		String css =
			"<style type=\"text/css\"><!--p {font-family:\""
				+ name
				+ "\"; font-size:\""
				+ size
				+ "pt\"}--></style>";
		styles.addRule(css);
		editorKit.setStyleSheet(styles);

		textPane.setEditorKit(editorKit);

		textPane.setText(plugin.getDescription());
		textPane.setCaretPosition(0);
		textPane.setEditable(false);

		JScrollPane sp = new JScrollPane(textPane);

		panel.add(sp, BorderLayout.CENTER);

		try {
			URLLabel label = new URLLabel(new URL(plugin.getWebsite()));
			panel.add(label, BorderLayout.SOUTH);

		} catch (MalformedURLException e) {

			e.printStackTrace();
		}

		return panel;
	}

	/* (non-Javadoc)
	 * @see net.javaprog.ui.wizard.Step#prepareRendering()
	 */
	public void prepareRendering() {

	}

}
