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
//All Rights Reserved.ndation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
package org.columba.mail.gui.composer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.mail.gui.composer.action.AddressbookAction;
import org.columba.mail.gui.composer.util.AddressCollector;
import org.columba.mail.util.MailResourceLoader;
import org.frapuccino.addresscombobox.CommaSeparatedAutoCompleter;
import org.frapuccino.addresscombobox.PatternSeparatedAutoCompleter;

import com.jgoodies.forms.layout.CellConstraints;

/**
 * 
 * @author fdietz
 */
public class HeaderView extends JPanel implements ActionListener {

	private HeaderController controller;

	private JButton toButton;

	private JButton ccButton;

	private JButton bccButton;

	private JTextField toComboBox;

	private JTextField ccComboBox;

	private JTextField bccComboBox;

	public HeaderView(HeaderController controller) {
		super();

		this.controller = controller;

		initComponents();

		// layoutComponents();
	}

	/**
	 * Init address autocompletion
	 * 
	 */
	public void initAutocompletion() {

		AddressCollector addressCollector = AddressCollector.getInstance();

		if (addressCollector != null) {
			// pass contact data along to AddressComboBox
			new EmailNameAutoCompleter(toComboBox, Arrays
					.asList(addressCollector.getAddresses()), Pattern.compile(","), true);
			new EmailNameAutoCompleter(ccComboBox, Arrays
					.asList(addressCollector.getAddresses()), Pattern.compile(","), true);
			new EmailNameAutoCompleter(bccComboBox, Arrays
					.asList(addressCollector.getAddresses()), Pattern.compile(","), true);

		}
	}

	/**
	 * 
	 */
	public void layoutComponents(JPanel panel) {

		CellConstraints cc = new CellConstraints();

		panel.add(toButton, cc.xy(1, 3, CellConstraints.FILL,
				CellConstraints.DEFAULT));
		panel.add(toComboBox, cc.xywh(3, 3, 5, 1));

		panel.add(ccButton, cc.xy(1, 5, CellConstraints.FILL,
				CellConstraints.DEFAULT));
		panel.add(ccComboBox, cc.xywh(3, 5, 5, 1));

		panel.add(bccButton, cc.xy(1, 7, CellConstraints.FILL,
				CellConstraints.DEFAULT));
		panel.add(bccComboBox, cc.xywh(3, 7, 5, 1));

	}

	protected void initComponents() {

		toButton = new JButton("To:");
		toButton.addActionListener(this);
		ccButton = new JButton("Cc:");
		ccButton.addActionListener(this);
		bccButton = new JButton("Bcc:");
		bccButton.addActionListener(this);

		toComboBox = new JTextField();
		toComboBox.setToolTipText(MailResourceLoader.getString(
				"dialog", "composer", "recipient_separator"));
		ccComboBox = new JTextField();
		ccComboBox.setToolTipText(MailResourceLoader.getString(
				"dialog", "composer", "recipient_separator"));
		bccComboBox = new JTextField();
		bccComboBox.setToolTipText(MailResourceLoader.getString(
				"dialog", "composer", "recipient_separator"));
	}

	/**
	 * @return Returns the bccComboBox.
	 */
	public JTextField getBccComboBox() {
		return bccComboBox;
	}

	/**
	 * @return Returns the ccComboBox.
	 */
	public JTextField getCcComboBox() {
		return ccComboBox;
	}

	/**
	 * @return Returns the toComboBox.
	 */
	public JTextField getToComboBox() {
		return toComboBox;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {
		new AddressbookAction(controller.getComposerController())
				.actionPerformed(null);

	}
}