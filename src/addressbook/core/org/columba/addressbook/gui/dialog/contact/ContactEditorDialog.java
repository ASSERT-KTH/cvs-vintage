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
package org.columba.addressbook.gui.dialog.contact;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.columba.addressbook.model.AddressModel;
import org.columba.addressbook.model.ContactModel;
import org.columba.addressbook.model.EmailModel;
import org.columba.addressbook.model.IContactModel;
import org.columba.addressbook.model.InstantMessagingModel;
import org.columba.addressbook.model.LabelModel;
import org.columba.addressbook.model.PhoneModel;
import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.resourceloader.ImageLoader;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

/**
 * Contact Editor Dialog.
 * <p>
 * Editor doesn't change contact model, instead it uses a new contact model
 * instance. Use <code>getDestModel()</code> to retrieve the resulting contact
 * model, after the user is finished with editing.
 * 
 * @author Frederik Dietz
 */
public class ContactEditorDialog extends JDialog implements ActionListener {

	private JPanel dialogPane;

	private JPanel contentPane;

	private JTabbedPane tabbedPane2;

	private JPanel contactPanel;

	private JPanel panel7;

	private JButton pictureButton;

	private JButton formattedNameButton;

	private JTextField formattedNameTextField;

	private JLabel nicknameLabel;

	private JTextField nicknameTextField;

	private JLabel fileunderLabel;

	private JComboBox fileunderComboBox;

	private JButton categoriesButton;

	private JTextField categoriesTextField;

	private JComponent emailSeparator;

	private JComboBox emailComboBox1;

	private JTextField emailTextField1;

	private JComboBox emailComboBox2;

	private JTextField emailTextField2;

	private JComboBox emailComboBox3;

	private JTextField emailTextField3;

	private JComboBox emailComboBox4;

	private JTextField emailTextField4;

	private JCheckBox preferHtmlCheckBox;

	private JComponent telephoneSeparator;

	private JComboBox telephoneComboBox1;

	private JTextField telephoneTextField1;

	private JComboBox telephoneComboBox2;

	private JTextField telephoneTextField2;

	private JComboBox telephoneComboBox3;

	private JTextField telephoneTextField3;

	private JComboBox telephoneComboBox4;

	private JTextField telephoneTextField4;

	private JComponent imSeparator;

	private JComboBox imComboBox1;

	private JTextField imTextField1;

	private JComboBox imComboBox2;

	private JTextField imTextField2;

	private JComboBox imComboBox3;

	private JTextField imTextField3;

	private JComboBox imComboBox4;

	private JTextField imTextField4;

	private JPanel personalInfoPanel;

	private JComponent webAddressSeparator;

	private JLabel homepageLabel;

	private JPanel panel1;

	private JTextField homepageTextField;

	private JButton homepageButton;

	private JLabel weblogLabel;

	private JPanel panel5;

	private JTextField weblogTextField;

	private JButton weblogButton;

	private JLabel calendarLabel;

	private JPanel panel6;

	private JTextField calendarTextField;

	private JButton calendarButton;

	private JLabel freebusyLabel;

	private JPanel panel8;

	private JTextField freebusyTextField;

	private JButton freebusyButton;

	private JComponent jobSeparator;

	private JLabel professionLabel;

	private JTextField professionTextField;

	private JLabel titleLabel;

	private JTextField titleTextField;

	private JLabel companyLabel;

	private JTextField companyTextField;

	private JLabel departmentLabel;

	private JTextField departmentTextField;

	private JLabel managerLabel;

	private JTextField managerTextField;

	private JLabel officeLabel;

	private JTextField officeTextField;

	private JComponent miscellaneousSeparator;

	private JLabel birthdayLabel;

	private JComboBox birthdayComboBox;

	private JLabel notesLabel;

	private JScrollPane scrollPane3;

	private JTextArea notesTextArea;

	private JPanel mailingAddressPanel;

	private JComponent privateSeparator;

	private JLabel privateAddressLabel;

	private JScrollPane scrollPane1;

	private JTextArea privateAddressTextArea;

	private JLabel privateCityLabel;

	private JTextField privateCityTextField;

	private JLabel privateZipPostalCodeLabel;

	private JTextField privateZipPostalCodeTextField;

	private JLabel label1;

	private JTextField privateStreetTextField;

	private JLabel privateStateProvinceCountyLabel;

	private JTextField privateStateProvinceCountyTextField;

	private JLabel privatePOBoxLabel;

	private JTextField privatePOBoxTextField;

	private JLabel privateCountryLabel;

	private JTextField privateCountryTextField;

	private JComponent workSeparator;

	private JLabel workAddressLabel;

	private JScrollPane scrollPane2;

	private JTextArea workAddressTextArea;

	private JLabel workCityLabel;

	private JTextField workCityTextField;

	private JLabel workZipPostalCodeLabel;

	private JTextField workZipPostalCodeTextField;

	private JLabel label2;

	private JTextField workStreetTextField;

	private JLabel workStateProvinceCountyLabel;

	private JTextField workStateProvinceCountyTextField;

	private JLabel workPOBoxLabel;

	private JTextField workPOBoxTextField;

	private JLabel workCountryLabel;

	private JTextField workCountryTextField;

	private JComponent otherSeparator;

	private JLabel otherAddressLabel;

	private JScrollPane scrollPane4;

	private JTextArea otherAddressTextArea;

	private JLabel otherCityLabel;

	private JTextField otherCityTextField;

	private JLabel otherZipPostalCodeLabel;

	private JTextField otherZipPostalCodeTextField;

	private JLabel label3;

	private JTextField otherStreetTextField;

	private JLabel otherStateProvinceCountyLabel;

	private JTextField otherStateProvinceCountyTextField;

	private JLabel otherPOBoxLabel;

	private JTextField otherPOBoxTextField;

	private JLabel otherCountryLabel;

	private JTextField otherCountryTextField;

	private JPanel buttonBar;

	private JButton okButton;

	private JButton cancelButton;

	private FullNameDialog fullNameDialog;

	private ResourceBundle bundle;

	private IContactModel sourceModel;

	private ContactModel destModel;

	private boolean result = false;

	public ContactEditorDialog(Frame owner, IContactModel sourceModel) {
		super(owner, true);

		this.sourceModel = sourceModel;

		init();
	}

	public ContactEditorDialog(Frame owner) {
		super(owner, true);

		init();
	}

	/**
	 * 
	 */
	private void init() {
		bundle = ResourceBundle
				.getBundle("org.columba.addressbook.i18n.dialog.contact");

		initComponents();
		layoutComponents();

		destModel = new ContactModel();

		fullNameDialog = new FullNameDialog(this);

		updateComponents(true);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void updateFullNameDialogComponents(boolean b) {
		if (b) {

			// leave dialog empty if no source contact model specified
			if (sourceModel == null)
				return;

			fullNameDialog.getNamePrefixTextField().setText(
					sourceModel.getNamePrefix());
			fullNameDialog.getLastNameTextField().setText(
					sourceModel.getFamilyName());
			fullNameDialog.getFirstNameTextField().setText(
					sourceModel.getGivenName());
			fullNameDialog.getMiddleNameTextField().setText(
					sourceModel.getAdditionalNames());
			fullNameDialog.getNameSuffixTextField().setText(
					sourceModel.getNameSuffix());

		} else {
			destModel.setNamePrefix(fullNameDialog.getNamePrefixTextField()
					.getText());
			destModel.setFamilyName(fullNameDialog.getLastNameTextField()
					.getText());
			destModel.setGivenName(fullNameDialog.getFirstNameTextField()
					.getText());
			destModel.setAdditionalNames(fullNameDialog
					.getMiddleNameTextField().getText());
			destModel.setNameSuffix(fullNameDialog.getNameSuffixTextField()
					.getText());
		}
	}

	public void updateComponents(boolean b) {

		updateFullNameDialogComponents(b);

		if (b) {
			// leave dialog empty if no source contact model specified
			if (sourceModel == null)
				return;

			// model -> view
			nicknameTextField.setText(sourceModel.getNickName());
			formattedNameTextField.setText(sourceModel.getFormattedName());

			fillSortStringComboBox();
			if ( sourceModel.getSortString() != null) {
				fileunderComboBox.setSelectedItem(sourceModel.getSortString());
			}

			companyTextField.setText(sourceModel.getOrganisation());
			professionTextField.setText(sourceModel.getProfession());
			titleTextField.setText(sourceModel.getTitle());

			companyTextField.setText(sourceModel.getOrganisation());
			departmentTextField.setText(sourceModel.getDepartment());
			officeTextField.setText(sourceModel.getOffice());

			homepageTextField.setText(sourceModel.getHomePage());
			weblogTextField.setText(sourceModel.getWeblog());
			freebusyTextField.setText(sourceModel.getFreeBusy());
			calendarTextField.setText(sourceModel.getCalendar());

			ImageIcon image = null;
			if (sourceModel.getPhoto() != null)
				image = new ImageIcon(sourceModel.getPhoto());
			// fall back to default image
			if (image == null)
				image = ImageLoader.getImageIcon("malehead.png");
			pictureButton.setIcon(image);

			// we support up to 4 email addresses
			Iterator it = sourceModel.getEmailIterator();
			if (it.hasNext()) {
				EmailModel m = (EmailModel) it.next();
				fillEmailView(m, emailComboBox1, emailTextField1);
			}
			if (it.hasNext()) {
				EmailModel m = (EmailModel) it.next();
				fillEmailView(m, emailComboBox2, emailTextField2);
			}
			if (it.hasNext()) {
				EmailModel m = (EmailModel) it.next();
				fillEmailView(m, emailComboBox3, emailTextField3);
			}
			if (it.hasNext()) {
				EmailModel m = (EmailModel) it.next();
				fillEmailView(m, emailComboBox4, emailTextField4);
			}

			// we support up to 4 phone entries
			it = sourceModel.getPhoneIterator();
			if (it.hasNext()) {
				PhoneModel m = (PhoneModel) it.next();
				fillPhoneView(m, telephoneComboBox1, telephoneTextField1);
			}
			if (it.hasNext()) {
				PhoneModel m = (PhoneModel) it.next();
				fillPhoneView(m, telephoneComboBox2, telephoneTextField2);
			}
			if (it.hasNext()) {
				PhoneModel m = (PhoneModel) it.next();
				fillPhoneView(m, telephoneComboBox3, telephoneTextField3);
			}
			if (it.hasNext()) {
				PhoneModel m = (PhoneModel) it.next();
				fillPhoneView(m, telephoneComboBox4, telephoneTextField4);
			}

			// we support up to 4 im entries
			it = sourceModel.getInstantMessagingIterator();
			if (it.hasNext()) {
				InstantMessagingModel m = (InstantMessagingModel) it.next();
				fillIMView(m, imComboBox1, imTextField1);
			}
			if (it.hasNext()) {
				InstantMessagingModel m = (InstantMessagingModel) it.next();
				fillIMView(m, imComboBox2, imTextField2);
			}
			if (it.hasNext()) {
				InstantMessagingModel m = (InstantMessagingModel) it.next();
				fillIMView(m, imComboBox3, imTextField3);
			}
			if (it.hasNext()) {
				InstantMessagingModel m = (InstantMessagingModel) it.next();
				fillIMView(m, imComboBox4, imTextField4);
			}

			// we support up to 3 address entries (work/private/other)
			it = sourceModel.getAddressIterator();
			if (it.hasNext()) {
				AddressModel m = (AddressModel) it.next();
				fillAddressView(m);
			}
			if (it.hasNext()) {
				AddressModel m = (AddressModel) it.next();
				fillAddressView(m);
			}
			if (it.hasNext()) {
				AddressModel m = (AddressModel) it.next();
				fillAddressView(m);
			}

			// we support up to 3 label entries (work/private/other)
			it = sourceModel.getLabelIterator();
			if (it.hasNext()) {
				LabelModel l = (LabelModel) it.next();
				fillLabelView(l);
			}
			if (it.hasNext()) {
				LabelModel l = (LabelModel) it.next();
				fillLabelView(l);
			}
			if (it.hasNext()) {
				LabelModel l = (LabelModel) it.next();
				fillLabelView(l);
			}

			notesTextArea.setText(sourceModel.getNote());

		} else {
			// view -> model
			destModel.setNickName(nicknameTextField.getText());
			destModel.setFormattedName(formattedNameTextField.getText());

			if (fileunderComboBox.getSelectedItem() != null)
				destModel.setSortString((String) fileunderComboBox
						.getSelectedItem());

			destModel.setOrganisation(companyTextField.getText());
			destModel.setProfession(professionTextField.getText());
			destModel.setTitle(titleTextField.getText());

			destModel.setOrganisation(companyTextField.getText());
			destModel.setDepartment(departmentTextField.getText());
			destModel.setOffice(officeTextField.getText());

			destModel.setHomePage(homepageTextField.getText());
			destModel.setWeblog(weblogTextField.getText());
			destModel.setFreeBusy(freebusyTextField.getText());
			destModel.setCalendar(calendarTextField.getText());

			// we support up to 4 email addresses
			fillEmailModel(emailTextField1, emailComboBox1, destModel);
			fillEmailModel(emailTextField2, emailComboBox2, destModel);
			fillEmailModel(emailTextField3, emailComboBox3, destModel);
			fillEmailModel(emailTextField4, emailComboBox4, destModel);

			// we support up to 4 phones
			fillTelephoneModel(telephoneTextField1, telephoneComboBox1,
					destModel);
			fillTelephoneModel(telephoneTextField2, telephoneComboBox2,
					destModel);
			fillTelephoneModel(telephoneTextField3, telephoneComboBox3,
					destModel);
			fillTelephoneModel(telephoneTextField4, telephoneComboBox4,
					destModel);

			// we support up to 4 im
			fillIMModel(imTextField1, imComboBox1, destModel);
			fillIMModel(imTextField2, imComboBox2, destModel);
			fillIMModel(imTextField3, imComboBox3, destModel);
			fillIMModel(imTextField4, imComboBox4, destModel);

			// we support 3 address types (work/home/other)
			fillAddressModel(AddressModel.TYPE_WORK, destModel);
			fillAddressModel(AddressModel.TYPE_HOME, destModel);
			fillAddressModel(AddressModel.TYPE_OTHER, destModel);

			// we support 3 label types (work/home/other)
			fillLabelModel(LabelModel.TYPE_WORK, destModel);
			fillLabelModel(LabelModel.TYPE_HOME, destModel);
			fillLabelModel(LabelModel.TYPE_OTHER, destModel);

			destModel.setNote(notesTextArea.getText());
		}
	}

	/**
	 * for example: "lastname, firstname" or "firstname lastname"
	 */
	private void fillSortStringComboBox() {
		String ln = fullNameDialog.getLastNameTextField().getText();
		String fn = fullNameDialog.getFirstNameTextField().getText();

		if (ln != null && fn != null) {
			if (ln.length() != 0 && fn.length() != 0) {
				fileunderComboBox.addItem(ln + ", " + fn);
				fileunderComboBox.addItem(fn + " " + ln);
			} else if (ln.length() != 0 && fn.length() == 0) {
				fileunderComboBox.addItem(ln);
			} else if (ln.length() == 0 && fn.length() != 0) {
				fileunderComboBox.addItem(fn);
			}
			
			if( fileunderComboBox.getModel().getSize() > 0)
				fileunderComboBox.setSelectedIndex(0);
		}
	}

	private void fillIMModel(JTextField imTextField, JComboBox imComboBox,
			ContactModel destModel2) {
		if (imTextField.getText() != null) {
			if (imComboBox.getSelectedIndex() == 0)
				destModel.addPhone(new PhoneModel(imTextField.getText(),
						InstantMessagingModel.TYPE_JABBER));
			else if (imComboBox.getSelectedIndex() == 1)
				destModel.addPhone(new PhoneModel(imTextField.getText(),
						InstantMessagingModel.TYPE_AIM));
			else if (imComboBox.getSelectedIndex() == 2)
				destModel.addPhone(new PhoneModel(imTextField.getText(),
						InstantMessagingModel.TYPE_YAHOO));
			else if (imComboBox.getSelectedIndex() == 3)
				destModel.addPhone(new PhoneModel(imTextField.getText(),
						InstantMessagingModel.TYPE_MSN));
			else if (imComboBox.getSelectedIndex() == 4)
				destModel.addPhone(new PhoneModel(imTextField.getText(),
						InstantMessagingModel.TYPE_ICQ));
		}

	}

	private void fillTelephoneModel(JTextField telephoneTextField,
			JComboBox telephoneComboBox, ContactModel destModel2) {
		if (telephoneTextField.getText() != null) {
			if (telephoneComboBox.getSelectedIndex() == 0)
				destModel.addPhone(new PhoneModel(telephoneTextField.getText(),
						PhoneModel.TYPE_BUSINESS_PHONE));
			else if (telephoneComboBox.getSelectedIndex() == 1)
				destModel.addPhone(new PhoneModel(telephoneTextField.getText(),
						PhoneModel.TYPE_ASSISTANT_PHONE));
			else if (telephoneComboBox.getSelectedIndex() == 2)
				destModel.addPhone(new PhoneModel(telephoneTextField.getText(),
						PhoneModel.TYPE_BUSINESS_FAX));

			// TODO finish for all left phone types
		}

	}

	private void fillIMView(InstantMessagingModel m, JComboBox imComboBox,
			JTextField imTextField) {

		if (m.getType() == InstantMessagingModel.TYPE_JABBER)
			imComboBox.setSelectedIndex(0);
		else if (m.getType() == InstantMessagingModel.TYPE_AIM)
			imComboBox.setSelectedIndex(1);
		else if (m.getType() == InstantMessagingModel.TYPE_YAHOO)
			imComboBox.setSelectedIndex(2);
		else if (m.getType() == InstantMessagingModel.TYPE_MSN)
			imComboBox.setSelectedIndex(3);
		else if (m.getType() == InstantMessagingModel.TYPE_ICQ)
			imComboBox.setSelectedIndex(4);

		imTextField.setText(m.getUserId());
	}

	private void fillPhoneView(PhoneModel m, JComboBox telephoneComboBox,
			JTextField telephoneTextField) {

		if (m.getType() == PhoneModel.TYPE_BUSINESS_PHONE)
			telephoneComboBox.setSelectedIndex(0);
		else if (m.getType() == PhoneModel.TYPE_ASSISTANT_PHONE)
			telephoneComboBox.setSelectedIndex(1);
		else if (m.getType() == PhoneModel.TYPE_BUSINESS_FAX)
			telephoneComboBox.setSelectedIndex(2);

		// TODO finish for all left phone types

		telephoneTextField.setText(m.getNumber());
	}

	/**
	 * Fill label model using values from ui-controls
	 * 
	 * @param type
	 * @param destModel
	 */
	private void fillLabelModel(int type, ContactModel destModel) {
		LabelModel model = null;
		if (type == LabelModel.TYPE_WORK)
			model = new LabelModel(workAddressTextArea.getText(), type);
		else if (type == LabelModel.TYPE_HOME)
			model = new LabelModel(privateAddressTextArea.getText(), type);
		else if (type == LabelModel.TYPE_OTHER)
			model = new LabelModel(otherAddressTextArea.getText(), type);

		destModel.addLabel(model);
	}

	/**
	 * Fill address model using values from ui-controls.
	 * 
	 * @param type
	 * @param destModel
	 */
	private void fillAddressModel(int type, ContactModel destModel) {
		AddressModel model = null;
		if (type == AddressModel.TYPE_WORK)
			// "street" is missing
			model = new AddressModel(workCityTextField.getText(), "",
					workCountryTextField.getText(), workPOBoxTextField
							.getText(), workStateProvinceCountyTextField
							.getText(), workZipPostalCodeTextField.getText(),
					type);
		else if (type == AddressModel.TYPE_HOME)
			// "street" is missing
			model = new AddressModel(privateCityTextField.getText(), "",
					privateCountryTextField.getText(), privatePOBoxTextField
							.getText(), privateStateProvinceCountyTextField
							.getText(),
					privateZipPostalCodeTextField.getText(), type);
		else if (type == AddressModel.TYPE_OTHER)
			// "street" is missing
			model = new AddressModel(otherCityTextField.getText(), "",
					otherCountryTextField.getText(), otherPOBoxTextField
							.getText(), otherStateProvinceCountyTextField
							.getText(), otherZipPostalCodeTextField.getText(),
					type);

		destModel.addAddress(model);
	}

	/**
	 * Fill email model using values from ui-controls.
	 * 
	 * @param destModel
	 * @param emailComboBox
	 * @param emailTextField
	 * 
	 */
	private void fillEmailModel(JTextField emailTextField,
			JComboBox emailComboBox, ContactModel destModel) {
		if (emailTextField.getText() != null) {
			if (emailComboBox.getSelectedIndex() == 0)
				destModel.addEmail(new EmailModel(emailTextField.getText(),
						EmailModel.TYPE_WORK));
			if (emailComboBox.getSelectedIndex() == 1)
				destModel.addEmail(new EmailModel(emailTextField.getText(),
						EmailModel.TYPE_HOME));
			if (emailComboBox.getSelectedIndex() == 2)
				destModel.addEmail(new EmailModel(emailTextField.getText(),
						EmailModel.TYPE_OTHER));
		}
	}

	/**
	 * Fill all email ui-controls from model.
	 * 
	 */
	private void fillEmailView(EmailModel m, JComboBox comboBox,
			JTextField textField) {
		if (m.getType() == EmailModel.TYPE_WORK)
			comboBox.setSelectedIndex(0);
		else if (m.getType() == EmailModel.TYPE_HOME)
			comboBox.setSelectedIndex(1);
		else if (m.getType() == EmailModel.TYPE_OTHER)
			comboBox.setSelectedIndex(2);

		textField.setText(m.getAddress());
	}

	/**
	 * Fill all label ui-controls from model.
	 * 
	 * @param l
	 *            label model
	 */
	private void fillLabelView(LabelModel l) {
		if (l.getType() == LabelModel.TYPE_WORK) {
			workAddressTextArea.setText(l.getLabel());
		} else if (l.getType() == LabelModel.TYPE_HOME) {
			privateAddressTextArea.setText(l.getLabel());
		} else if (l.getType() == LabelModel.TYPE_OTHER) {
			otherAddressTextArea.setText(l.getLabel());
		}
	}

	/**
	 * Fill all address ui-controls from model.
	 * 
	 * @param m
	 *            address model
	 */
	private void fillAddressView(AddressModel m) {
		if (m.getType() == AddressModel.TYPE_WORK) {
			workCityTextField.setText(m.getCity());
			workCountryTextField.setText(m.getCountry());
			workPOBoxTextField.setText(m.getPoBox());
			workStateProvinceCountyTextField
					.setText(m.getStateProvinceCounty());
			workZipPostalCodeTextField.setText(m.getZipPostalCode());
		} else if (m.getType() == AddressModel.TYPE_HOME) {
			privateCityTextField.setText(m.getCity());
			privateCountryTextField.setText(m.getCountry());
			privatePOBoxTextField.setText(m.getPoBox());
			privateStateProvinceCountyTextField.setText(m
					.getStateProvinceCounty());
			privateZipPostalCodeTextField.setText(m.getZipPostalCode());
		} else if (m.getType() == AddressModel.TYPE_OTHER) {
			otherCityTextField.setText(m.getCity());
			otherCountryTextField.setText(m.getCountry());
			otherPOBoxTextField.setText(m.getPoBox());
			otherStateProvinceCountyTextField.setText(m
					.getStateProvinceCounty());
			otherZipPostalCodeTextField.setText(m.getZipPostalCode());
		}
	}

	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();
		if (action.equals("OK")) {
			result = true;

			updateComponents(false);

			setVisible(false);
		} else if (action.equals("CANCEL")) {
			result = false;
			setVisible(false);
		} else if (action.equals("FORMATTED_NAME_DIALOG")) {
			fullNameDialog.setVisible(true);

			boolean success = fullNameDialog.getResult();
			if (success) {

				String fn = fullNameDialog.getFormattedName();
				// fill formatted textfield
				formattedNameTextField.setText(fn);

				fillSortStringComboBox();
			}
		}

	}

	public boolean getResult() {
		return result;
	}

	private void initComponents() {

		DefaultComponentFactory compFactory = DefaultComponentFactory
				.getInstance();

		dialogPane = new JPanel();
		contentPane = new JPanel();
		tabbedPane2 = new JTabbedPane();
		contactPanel = new JPanel();
		panel7 = new JPanel();
		pictureButton = new JButton();
		formattedNameButton = new JButton();
		formattedNameButton.setActionCommand("FORMATTED_NAME_DIALOG");
		formattedNameButton.addActionListener(this);

		formattedNameTextField = new JTextField();
		nicknameLabel = new JLabel();
		nicknameTextField = new JTextField();
		fileunderLabel = new JLabel();
		fileunderComboBox = new JComboBox();
		categoriesButton = new JButton();
		categoriesTextField = new JTextField();
		emailSeparator = compFactory.createSeparator(bundle
				.getString("emailSeparator.text"));
		String[] emailTypes = new String[] { "Work", "Home", "Other" };
		emailComboBox1 = new JComboBox(emailTypes);
		emailTextField1 = new JTextField();
		emailComboBox2 = new JComboBox(emailTypes);
		emailTextField2 = new JTextField();
		emailComboBox3 = new JComboBox(emailTypes);
		emailTextField3 = new JTextField();
		emailComboBox4 = new JComboBox(emailTypes);
		emailTextField4 = new JTextField();
		preferHtmlCheckBox = new JCheckBox();
		telephoneSeparator = compFactory.createSeparator(bundle
				.getString("telephoneSeparator.text"));
		String[] phoneTypes = new String[] { "Business Phone",
				"Assistant Phone", "Business Fax", "Callback Phone",
				"Car Phone", "Company Phone", "Home Phone", "Home Fax", "ISDN",
				"Mobile Phone", "Other Phone", "Other Fax", "Pager",
				"Primary Phone", "Radio", "Telex", "TTY" };
		telephoneComboBox1 = new JComboBox(phoneTypes);
		telephoneTextField1 = new JTextField();
		telephoneComboBox2 = new JComboBox(phoneTypes);
		telephoneTextField2 = new JTextField();
		telephoneComboBox3 = new JComboBox(phoneTypes);
		telephoneTextField3 = new JTextField();
		telephoneComboBox4 = new JComboBox(phoneTypes);
		telephoneTextField4 = new JTextField();
		imSeparator = compFactory.createSeparator(bundle
				.getString("imSeparator.text"));
		String[] imTypes = new String[] { "Jabber", "AIM", "Yahoo", "MSN",
				"ICQ" };
		imComboBox1 = new JComboBox(imTypes);
		imTextField1 = new JTextField();
		imComboBox2 = new JComboBox(imTypes);
		imTextField2 = new JTextField();
		imComboBox3 = new JComboBox(imTypes);
		imTextField3 = new JTextField();
		imComboBox4 = new JComboBox(imTypes);
		imTextField4 = new JTextField();
		personalInfoPanel = new JPanel();
		webAddressSeparator = compFactory.createSeparator(bundle
				.getString("webAddressSeparator.text"));
		homepageLabel = new JLabel();
		panel1 = new JPanel();
		homepageTextField = new JTextField();
		homepageButton = new JButton(ImageLoader
				.getImageIcon("stock_internet-16.png"));
		homepageButton.setMargin(new Insets(0, 0, 0, 0));
		homepageButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = homepageTextField.getText();
				try {
					URL url = new URL(s);
					ColumbaDesktop.getInstance().browse(url);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
		});

		weblogLabel = new JLabel();
		panel5 = new JPanel();
		weblogTextField = new JTextField();
		weblogButton = new JButton(ImageLoader
				.getImageIcon("stock_internet-16.png"));
		weblogButton.setMargin(new Insets(0, 0, 0, 0));
		weblogButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = weblogTextField.getText();
				try {
					URL url = new URL(s);
					ColumbaDesktop.getInstance().browse(url);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
		});
		calendarLabel = new JLabel();
		panel6 = new JPanel();
		calendarTextField = new JTextField();
		calendarButton = new JButton(ImageLoader
				.getImageIcon("stock_internet-16.png"));
		calendarButton.setMargin(new Insets(0, 0, 0, 0));
		calendarButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = calendarTextField.getText();
				try {
					URL url = new URL(s);
					ColumbaDesktop.getInstance().browse(url);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
		});
		freebusyLabel = new JLabel();
		panel8 = new JPanel();
		freebusyTextField = new JTextField();
		freebusyButton = new JButton(ImageLoader
				.getImageIcon("stock_internet-16.png"));
		freebusyButton.setMargin(new Insets(0, 0, 0, 0));
		freebusyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String s = freebusyTextField.getText();
				try {
					URL url = new URL(s);
					ColumbaDesktop.getInstance().browse(url);
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
			}
		});
		jobSeparator = compFactory.createSeparator(bundle
				.getString("jobSeparator.text"));
		professionLabel = new JLabel();
		professionTextField = new JTextField();
		titleLabel = new JLabel();
		titleTextField = new JTextField();
		companyLabel = new JLabel();
		companyTextField = new JTextField();
		departmentLabel = new JLabel();
		departmentTextField = new JTextField();
		managerLabel = new JLabel();
		managerTextField = new JTextField();
		officeLabel = new JLabel();
		officeTextField = new JTextField();
		miscellaneousSeparator = compFactory.createSeparator(bundle
				.getString("miscellaneousSeparator.text"));
		birthdayLabel = new JLabel();
		birthdayComboBox = new JComboBox();
		notesLabel = new JLabel();
		scrollPane3 = new JScrollPane();
		notesTextArea = new JTextArea();
		mailingAddressPanel = new JPanel();
		privateSeparator = compFactory.createSeparator(bundle
				.getString("privateSeparator.text"));
		privateAddressLabel = new JLabel();
		scrollPane1 = new JScrollPane();
		privateAddressTextArea = new JTextArea();
		privateCityLabel = new JLabel();
		privateCityTextField = new JTextField();
		privateZipPostalCodeLabel = new JLabel();
		privateZipPostalCodeTextField = new JTextField();
		label1 = new JLabel();
		privateStreetTextField = new JTextField();
		privateStateProvinceCountyLabel = new JLabel();
		privateStateProvinceCountyTextField = new JTextField();
		privatePOBoxLabel = new JLabel();
		privatePOBoxTextField = new JTextField();
		privateCountryLabel = new JLabel();
		privateCountryTextField = new JTextField();
		workSeparator = compFactory.createSeparator(bundle
				.getString("workSeparator.text"));
		workAddressLabel = new JLabel();
		scrollPane2 = new JScrollPane();
		workAddressTextArea = new JTextArea();
		workCityLabel = new JLabel();
		workCityTextField = new JTextField();
		workZipPostalCodeLabel = new JLabel();
		workZipPostalCodeTextField = new JTextField();
		label2 = new JLabel();
		workStreetTextField = new JTextField();
		workStateProvinceCountyLabel = new JLabel();
		workStateProvinceCountyTextField = new JTextField();
		workPOBoxLabel = new JLabel();
		workPOBoxTextField = new JTextField();
		workCountryLabel = new JLabel();
		workCountryTextField = new JTextField();
		otherSeparator = compFactory.createSeparator(bundle
				.getString("otherSeparator.text"));
		otherAddressLabel = compFactory.createLabel(bundle
				.getString("otherAddressLabel.textWithMnemonic"));
		scrollPane4 = new JScrollPane();
		otherAddressTextArea = new JTextArea();
		otherCityLabel = new JLabel();
		otherCityTextField = new JTextField();
		otherZipPostalCodeLabel = new JLabel();
		otherZipPostalCodeTextField = new JTextField();
		label3 = new JLabel();
		otherStreetTextField = new JTextField();
		otherStateProvinceCountyLabel = new JLabel();
		otherStateProvinceCountyTextField = new JTextField();
		otherPOBoxLabel = new JLabel();
		otherPOBoxTextField = new JTextField();
		otherCountryLabel = new JLabel();
		otherCountryTextField = new JTextField();

		buttonBar = new JPanel();
		okButton = new JButton();
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		cancelButton = new JButton();
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);

	}

	private void layoutComponents() {
		CellConstraints cc = new CellConstraints();

		// ======== this ========
		Container contentPane2 = getContentPane();
		contentPane2.setLayout(new BorderLayout());

		// ======== dialogPane ========
		{
			dialogPane.setBorder(Borders.DIALOG_BORDER);
			dialogPane.setLayout(new BorderLayout());

			// ======== contentPane ========
			{
				contentPane.setLayout(new BorderLayout());

				// ======== tabbedPane2 ========
				{
					tabbedPane2
							.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);

					// ======== contactPanel ========
					{
						contactPanel.setBorder(Borders.DIALOG_BORDER);
						contactPanel
								.setLayout(new FormLayout(
										new ColumnSpec[] {
												FormFactory.DEFAULT_COLSPEC,
												FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
												new ColumnSpec(ColumnSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.DEFAULT_GROW),
												FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
												FormFactory.DEFAULT_COLSPEC,
												FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
												new ColumnSpec(ColumnSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.DEFAULT_GROW) },
										new RowSpec[] {
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW) }));
						((FormLayout) contactPanel.getLayout())
								.setRowGroups(new int[][] { { 7, 9, 11, 17, 19,
										25, 27 } });

						// ======== panel7 ========
						{
							panel7.setLayout(new FormLayout(new ColumnSpec[] {
									FormFactory.DEFAULT_COLSPEC,
									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
									FormFactory.DEFAULT_COLSPEC,
									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
									new ColumnSpec(ColumnSpec.FILL,
											Sizes.DEFAULT,
											FormSpec.DEFAULT_GROW) },
									new RowSpec[] {
											new RowSpec(RowSpec.FILL,
													Sizes.DEFAULT,
													FormSpec.NO_GROW),
											FormFactory.LINE_GAP_ROWSPEC,
											new RowSpec(RowSpec.FILL,
													Sizes.DEFAULT,
													FormSpec.NO_GROW),
											FormFactory.LINE_GAP_ROWSPEC,
											new RowSpec(RowSpec.FILL,
													Sizes.DEFAULT,
													FormSpec.NO_GROW),
											FormFactory.LINE_GAP_ROWSPEC,
											new RowSpec(RowSpec.FILL,
													Sizes.DEFAULT,
													FormSpec.NO_GROW) }));
							((FormLayout) panel7.getLayout())
									.setRowGroups(new int[][] { { 1, 3, 5, 7 } });

							// ---- pictureButton ----

							panel7.add(pictureButton, cc.xywh(1, 1, 1, 7));

							// ---- fullnameButton ----
							formattedNameButton.setText(bundle
									.getString("formattedNameButton.text"));
							panel7.add(formattedNameButton, cc.xy(3, 1));
							panel7.add(formattedNameTextField, cc.xy(5, 1));

							// ---- nicknameLabel ----
							nicknameLabel.setText(bundle
									.getString("nicknameLabel.text"));
							nicknameLabel.setLabelFor(nicknameTextField);
							panel7.add(nicknameLabel, cc.xywh(3, 3, 1, 1,
									CellConstraints.RIGHT,
									CellConstraints.DEFAULT));
							panel7.add(nicknameTextField, cc.xy(5, 3));

							// ---- fileunderLabel ----
							fileunderLabel.setText(bundle
									.getString("fileunderLabel.text"));
							fileunderLabel.setLabelFor(fileunderComboBox);
							panel7.add(fileunderLabel, cc.xywh(3, 5, 1, 1,
									CellConstraints.RIGHT,
									CellConstraints.DEFAULT));
							panel7.add(fileunderComboBox, cc.xy(5, 5));

							// ---- categoriesButton ----
							categoriesButton.setText(bundle
									.getString("categoriesButton.text"));
							panel7.add(categoriesButton, cc.xy(3, 7));
							panel7.add(categoriesTextField, cc.xy(5, 7));
						}
						contactPanel.add(panel7, cc.xywh(1, 1, 7, 1));
						contactPanel.add(emailSeparator, cc.xywh(1, 5, 7, 1));
						contactPanel.add(emailComboBox1, cc.xy(1, 7));

						// ---- emailTextField1 ----
						emailTextField1.setColumns(20);
						contactPanel.add(emailTextField1, cc.xy(3, 7));
						contactPanel.add(emailComboBox2, cc.xy(5, 7));

						// ---- emailTextField2 ----
						emailTextField2.setColumns(20);
						contactPanel.add(emailTextField2, cc.xy(7, 7));
						contactPanel.add(emailComboBox3, cc.xy(1, 9));

						// ---- emailTextField3 ----
						emailTextField3.setColumns(20);
						contactPanel.add(emailTextField3, cc.xy(3, 9));
						contactPanel.add(emailComboBox4, cc.xy(5, 9));

						// ---- emailTextField4 ----
						emailTextField4.setColumns(20);
						contactPanel.add(emailTextField4, cc.xy(7, 9));

						// ---- preferHtmlCheckBox ----
						preferHtmlCheckBox.setText(bundle
								.getString("preferHtmlCheckBox.text"));
						contactPanel.add(preferHtmlCheckBox, cc.xy(3, 11));
						contactPanel.add(telephoneSeparator, cc.xywh(1, 15, 7,
								1));
						contactPanel.add(telephoneComboBox1, cc.xy(1, 17));

						// ---- telephoneTextField1 ----
						telephoneTextField1.setColumns(20);
						contactPanel.add(telephoneTextField1, cc.xy(3, 17));
						contactPanel.add(telephoneComboBox2, cc.xy(5, 17));

						// ---- telephoneTextField2 ----
						telephoneTextField2.setColumns(20);
						contactPanel.add(telephoneTextField2, cc.xy(7, 17));
						contactPanel.add(telephoneComboBox3, cc.xy(1, 19));

						// ---- telephoneTextField3 ----
						telephoneTextField3.setColumns(20);
						contactPanel.add(telephoneTextField3, cc.xy(3, 19));
						contactPanel.add(telephoneComboBox4, cc.xy(5, 19));

						// ---- telephoneTextField4 ----
						telephoneTextField4.setColumns(20);
						contactPanel.add(telephoneTextField4, cc.xy(7, 19));
						contactPanel.add(imSeparator, cc.xywh(1, 23, 7, 1));
						contactPanel.add(imComboBox1, cc.xy(1, 25));

						// ---- imTextField1 ----
						imTextField1.setColumns(20);
						contactPanel.add(imTextField1, cc.xy(3, 25));
						contactPanel.add(imComboBox2, cc.xy(5, 25));

						// ---- imTextField2 ----
						imTextField2.setColumns(20);
						contactPanel.add(imTextField2, cc.xy(7, 25));
						contactPanel.add(imComboBox3, cc.xy(1, 27));

						// ---- imTextField3 ----
						imTextField3.setColumns(20);
						contactPanel.add(imTextField3, cc.xy(3, 27));
						contactPanel.add(imComboBox4, cc.xy(5, 27));

						// ---- imTextField4 ----
						imTextField4.setColumns(20);
						contactPanel.add(imTextField4, cc.xy(7, 27));
					}
					tabbedPane2.addTab(bundle
							.getString("contactPanel.tab.title"), contactPanel);

					// ======== personalInfoPanel ========
					{
						personalInfoPanel.setBorder(Borders.DIALOG_BORDER);
						personalInfoPanel
								.setLayout(new FormLayout(
										new ColumnSpec[] {
												FormFactory.DEFAULT_COLSPEC,
												FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
												new ColumnSpec(ColumnSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.DEFAULT_GROW),
												FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
												FormFactory.DEFAULT_COLSPEC,
												FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
												new ColumnSpec(ColumnSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.DEFAULT_GROW) },
										new RowSpec[] {
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC }));
						((FormLayout) personalInfoPanel.getLayout())
								.setRowGroups(new int[][] { { 3, 5, 7, 9, 15,
										17, 19, 25 } });
						personalInfoPanel.add(webAddressSeparator, cc.xywh(1,
								1, 7, 1));

						// ---- homepageLabel ----
						homepageLabel.setText(bundle
								.getString("homepageLabel.text"));
						homepageLabel.setLabelFor(homepageTextField);
						personalInfoPanel.add(homepageLabel, cc.xywh(1, 3, 1,
								1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ======== panel1 ========
						{
							panel1.setLayout(new FormLayout(new ColumnSpec[] {
									new ColumnSpec(ColumnSpec.FILL,
											Sizes.DEFAULT,
											FormSpec.DEFAULT_GROW),
									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
									new ColumnSpec(ColumnSpec.CENTER,
											Sizes.DEFAULT, FormSpec.NO_GROW) },
									RowSpec.decodeSpecs("fill:default")));

							// ---- homepageTextField ----
							homepageTextField.setColumns(20);
							panel1.add(homepageTextField, cc.xy(1, 1));

							// ---- homepageButton ----

							panel1.add(homepageButton, cc.xy(3, 1));
						}
						personalInfoPanel.add(panel1, cc.xywh(3, 3, 5, 1));

						// ---- weblogLabel ----
						weblogLabel.setText(bundle
								.getString("weblogLabel.text"));
						weblogLabel.setLabelFor(weblogTextField);
						personalInfoPanel
								.add(weblogLabel, cc.xywh(1, 5, 1, 1,
										CellConstraints.RIGHT,
										CellConstraints.DEFAULT));

						// ======== panel5 ========
						{
							panel5.setLayout(new FormLayout(new ColumnSpec[] {
									new ColumnSpec(ColumnSpec.FILL,
											Sizes.DEFAULT,
											FormSpec.DEFAULT_GROW),
									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
									FormFactory.DEFAULT_COLSPEC }, RowSpec
									.decodeSpecs("fill:default")));

							// ---- weblogTextField ----
							weblogTextField.setColumns(20);
							panel5.add(weblogTextField, cc.xy(1, 1));

							// ---- weblogButton ----

							panel5.add(weblogButton, cc.xy(3, 1));
						}
						personalInfoPanel.add(panel5, cc.xywh(3, 5, 5, 1));

						// ---- calendarLabel ----
						calendarLabel.setText(bundle
								.getString("calendarLabel.text"));
						calendarLabel.setLabelFor(calendarTextField);
						personalInfoPanel.add(calendarLabel, cc.xywh(1, 7, 1,
								1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ======== panel6 ========
						{
							panel6.setLayout(new FormLayout(new ColumnSpec[] {
									new ColumnSpec(ColumnSpec.FILL,
											Sizes.DEFAULT,
											FormSpec.DEFAULT_GROW),
									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
									FormFactory.DEFAULT_COLSPEC }, RowSpec
									.decodeSpecs("fill:default")));

							// ---- calendarTextField ----
							calendarTextField.setColumns(20);
							panel6.add(calendarTextField, cc.xy(1, 1));

							// ---- calendarButton ----
							panel6.add(calendarButton, cc.xy(3, 1));
						}
						personalInfoPanel.add(panel6, cc.xywh(3, 7, 5, 1));

						// ---- freebusyLabel ----
						freebusyLabel.setText(bundle
								.getString("freebusyLabel.text"));
						freebusyLabel.setLabelFor(freebusyTextField);
						personalInfoPanel.add(freebusyLabel, cc.xywh(1, 9, 1,
								1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ======== panel8 ========
						{
							panel8.setLayout(new FormLayout(new ColumnSpec[] {
									new ColumnSpec(ColumnSpec.FILL,
											Sizes.DEFAULT,
											FormSpec.DEFAULT_GROW),
									FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
									FormFactory.DEFAULT_COLSPEC }, RowSpec
									.decodeSpecs("fill:default")));

							// ---- freebusyTextField ----
							freebusyTextField.setColumns(20);
							panel8.add(freebusyTextField, cc.xy(1, 1));

							// ---- freebusyButton ----

							panel8.add(freebusyButton, cc.xy(3, 1));
						}
						personalInfoPanel.add(panel8, cc.xywh(3, 9, 5, 1));
						personalInfoPanel.add(jobSeparator, cc
								.xywh(1, 13, 7, 1));

						// ---- professionLabel ----
						professionLabel.setText(bundle
								.getString("professionLabel.text"));
						professionLabel.setLabelFor(professionTextField);
						personalInfoPanel.add(professionLabel, cc.xywh(1, 15,
								1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ---- professionTextField ----
						professionTextField.setColumns(20);
						personalInfoPanel
								.add(professionTextField, cc.xy(3, 15));

						// ---- titleLabel ----
						titleLabel.setText(bundle.getString("titleLabel.text"));
						titleLabel.setLabelFor(titleTextField);
						personalInfoPanel
								.add(titleLabel, cc.xywh(5, 15, 1, 1,
										CellConstraints.RIGHT,
										CellConstraints.DEFAULT));

						// ---- titleTextField ----
						titleTextField.setColumns(20);
						personalInfoPanel.add(titleTextField, cc.xy(7, 15));

						// ---- companyLabel ----
						companyLabel.setText(bundle
								.getString("companyLabel.text"));
						companyLabel.setLabelFor(companyTextField);
						personalInfoPanel.add(companyLabel, cc.xywh(1, 17, 1,
								1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ---- companyTextField ----
						companyTextField.setColumns(20);
						personalInfoPanel.add(companyTextField, cc.xy(3, 17));

						// ---- departmentLabel ----
						departmentLabel.setText(bundle
								.getString("departmentLabel.text"));
						departmentLabel.setLabelFor(departmentTextField);
						personalInfoPanel.add(departmentLabel, cc.xywh(5, 17,
								1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ---- departmentTextField ----
						departmentTextField.setColumns(20);
						personalInfoPanel
								.add(departmentTextField, cc.xy(7, 17));

						// ---- managerLabel ----
						managerLabel.setText(bundle
								.getString("managerLabel.text"));
						managerLabel.setLabelFor(managerTextField);
						personalInfoPanel.add(managerLabel, cc.xywh(1, 19, 1,
								1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ---- managerTextField ----
						managerTextField.setColumns(20);
						personalInfoPanel.add(managerTextField, cc.xy(3, 19));

						// ---- officeLabel ----
						officeLabel.setText(bundle
								.getString("officeLabel.text"));
						officeLabel.setLabelFor(officeTextField);
						personalInfoPanel
								.add(officeLabel, cc.xywh(5, 19, 1, 1,
										CellConstraints.RIGHT,
										CellConstraints.DEFAULT));

						// ---- officeTextField ----
						officeTextField.setColumns(10);
						personalInfoPanel.add(officeTextField, cc.xy(7, 19));
						personalInfoPanel.add(miscellaneousSeparator, cc.xywh(
								1, 23, 7, 1));

						// ---- birthdayLabel ----
						birthdayLabel.setText(bundle
								.getString("birthdayLabel.text"));
						birthdayLabel.setLabelFor(birthdayComboBox);
						personalInfoPanel.add(birthdayLabel, cc.xywh(1, 25, 1,
								1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));
						personalInfoPanel.add(birthdayComboBox, cc.xy(3, 25));

						// ---- notesLabel ----
						notesLabel.setText(bundle.getString("notesLabel.text"));
						notesLabel.setLabelFor(notesTextArea);
						personalInfoPanel.add(notesLabel, cc.xywh(1, 27, 1, 1,
								CellConstraints.RIGHT, CellConstraints.TOP));

						// ======== scrollPane3 ========
						{
							scrollPane3
									.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

							// ---- notesTextArea ----
							notesTextArea.setRows(5);
							notesTextArea.setColumns(20);
							notesTextArea.setTabSize(4);
							scrollPane3.setViewportView(notesTextArea);
						}
						personalInfoPanel
								.add(scrollPane3, cc.xywh(3, 27, 5, 1));
					}
					tabbedPane2.addTab(bundle
							.getString("personalInfoPanel.tab.title"),
							personalInfoPanel);

					// ======== mailingAddressPanel ========
					{
						mailingAddressPanel.setBorder(Borders.DIALOG_BORDER);
						mailingAddressPanel
								.setLayout(new FormLayout(
										new ColumnSpec[] {
												FormFactory.DEFAULT_COLSPEC,
												FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
												new ColumnSpec(ColumnSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.DEFAULT_GROW),
												FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
												FormFactory.DEFAULT_COLSPEC,
												FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
												new ColumnSpec(ColumnSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.DEFAULT_GROW) },
										new RowSpec[] {
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC,
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												new RowSpec(RowSpec.FILL,
														Sizes.DEFAULT,
														FormSpec.NO_GROW),
												FormFactory.LINE_GAP_ROWSPEC,
												FormFactory.DEFAULT_ROWSPEC }));
						mailingAddressPanel.add(privateSeparator, cc.xywh(1, 1,
								7, 1));

						// ---- privateAddressLabel ----
						privateAddressLabel.setText(bundle
								.getString("privateAddressLabel.text"));
						privateAddressLabel.setLabelFor(privateAddressTextArea);
						mailingAddressPanel.add(privateAddressLabel, cc.xywh(1,
								3, 1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ======== scrollPane1 ========
						{
							scrollPane1
									.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
							scrollPane1.setViewportView(privateAddressTextArea);
						}
						mailingAddressPanel.add(scrollPane1, cc
								.xywh(3, 3, 1, 3));

						// ---- privateCityLabel ----
						privateCityLabel.setText(bundle
								.getString("privateCityLabel.text"));
						privateCityLabel.setLabelFor(privateCityTextField);
						mailingAddressPanel.add(privateCityLabel, cc.xywh(5, 3,
								1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ---- privateCityTextField ----
						privateCityTextField.setColumns(5);
						mailingAddressPanel.add(privateCityTextField, cc.xy(7,
								3));

						// ---- privateZipPostalCodeLabel ----
						privateZipPostalCodeLabel.setText(bundle
								.getString("privateZipPostalCodeLabel.text"));
						privateZipPostalCodeLabel
								.setLabelFor(privateZipPostalCodeTextField);
						mailingAddressPanel.add(privateZipPostalCodeLabel, cc
								.xywh(5, 5, 1, 1, CellConstraints.RIGHT,
										CellConstraints.DEFAULT));
						mailingAddressPanel.add(privateZipPostalCodeTextField,
								cc.xy(7, 5));

						// ---- label1 ----
						label1.setText(bundle.getString("streetLabel.text"));
						label1.setLabelFor(privateStreetTextField);
						mailingAddressPanel
								.add(label1, cc.xywh(1, 7, 1, 1,
										CellConstraints.RIGHT,
										CellConstraints.DEFAULT));
						mailingAddressPanel.add(privateStreetTextField, cc.xy(
								3, 7));

						// ---- privateStateProvinceCountyLabel ----
						privateStateProvinceCountyLabel
								.setText(bundle
										.getString("privateStateProvinceCountyLabel.text"));
						privateStateProvinceCountyLabel
								.setLabelFor(privateStateProvinceCountyTextField);
						mailingAddressPanel.add(
								privateStateProvinceCountyLabel, cc.xywh(5, 7,
										1, 1, CellConstraints.RIGHT,
										CellConstraints.DEFAULT));
						mailingAddressPanel.add(
								privateStateProvinceCountyTextField, cc
										.xy(7, 7));

						// ---- privatePOBoxLabel ----
						privatePOBoxLabel.setText(bundle
								.getString("privatePOBoxLabel.text"));
						privatePOBoxLabel.setLabelFor(privatePOBoxTextField);
						mailingAddressPanel.add(privatePOBoxLabel, cc.xywh(1,
								9, 1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));
						mailingAddressPanel.add(privatePOBoxTextField, cc.xy(3,
								9));

						// ---- privateCountryLabel ----
						privateCountryLabel.setText(bundle
								.getString("privateCountryLabel.text"));
						privateCountryLabel
								.setLabelFor(privateCountryTextField);
						mailingAddressPanel.add(privateCountryLabel, cc.xywh(5,
								9, 1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));
						mailingAddressPanel.add(privateCountryTextField, cc.xy(
								7, 9));
						mailingAddressPanel.add(workSeparator, cc.xywh(1, 13,
								7, 1));

						// ---- workAddressLabel ----
						workAddressLabel.setText(bundle
								.getString("workAddressLabel.text"));
						workAddressLabel.setLabelFor(workAddressTextArea);
						mailingAddressPanel.add(workAddressLabel, cc.xywh(1,
								15, 1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ======== scrollPane2 ========
						{
							scrollPane2
									.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
							scrollPane2.setViewportView(workAddressTextArea);
						}
						mailingAddressPanel.add(scrollPane2, cc.xywh(3, 15, 1,
								3));

						// ---- workCityLabel ----
						workCityLabel.setText(bundle
								.getString("workCityLabel.text"));
						workCityLabel.setLabelFor(workCityTextField);
						mailingAddressPanel.add(workCityLabel, cc.xywh(5, 15,
								1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ---- workCityTextField ----
						workCityTextField.setColumns(5);
						mailingAddressPanel
								.add(workCityTextField, cc.xy(7, 15));

						// ---- workZipPostalCodeLabel ----
						workZipPostalCodeLabel.setText(bundle
								.getString("workZipPostalCodeLabel.text"));
						workZipPostalCodeLabel
								.setLabelFor(workZipPostalCodeTextField);
						mailingAddressPanel.add(workZipPostalCodeLabel, cc
								.xywh(5, 17, 1, 1, CellConstraints.RIGHT,
										CellConstraints.DEFAULT));
						mailingAddressPanel.add(workZipPostalCodeTextField, cc
								.xy(7, 17));

						// ---- label2 ----
						label2.setText(bundle.getString("streetLabel.text"));
						label2.setLabelFor(workStreetTextField);
						mailingAddressPanel
								.add(label2, cc.xywh(1, 19, 1, 1,
										CellConstraints.RIGHT,
										CellConstraints.DEFAULT));
						mailingAddressPanel.add(workStreetTextField, cc.xy(3,
								19));

						// ---- workStateProvinceCountyLabel ----
						workStateProvinceCountyLabel
								.setText(bundle
										.getString("workStateProvinceCountyLabel.text"));
						workStateProvinceCountyLabel
								.setLabelFor(workStateProvinceCountyTextField);
						mailingAddressPanel.add(workStateProvinceCountyLabel,
								cc.xywh(5, 19, 1, 1, CellConstraints.RIGHT,
										CellConstraints.DEFAULT));
						mailingAddressPanel.add(
								workStateProvinceCountyTextField, cc.xy(7, 19));

						// ---- workPOBoxLabel ----
						workPOBoxLabel.setText(bundle
								.getString("workPOBoxLabel.text"));
						workPOBoxLabel.setLabelFor(workPOBoxTextField);
						mailingAddressPanel.add(workPOBoxLabel, cc.xywh(1, 21,
								1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));
						mailingAddressPanel.add(workPOBoxTextField, cc
								.xy(3, 21));

						// ---- workCountryLabel ----
						workCountryLabel.setText(bundle
								.getString("workCountryLabel.text"));
						workCountryLabel.setLabelFor(workCountryTextField);
						mailingAddressPanel.add(workCountryLabel, cc.xywh(5,
								21, 1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));
						mailingAddressPanel.add(workCountryTextField, cc.xy(7,
								21));
						mailingAddressPanel.add(otherSeparator, cc.xywh(1, 25,
								7, 1));

						// ---- otherAddressLabel ----
						otherAddressLabel.setLabelFor(otherAddressTextArea);
						mailingAddressPanel.add(otherAddressLabel, cc.xywh(1,
								27, 1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));

						// ======== scrollPane4 ========
						{
							scrollPane4
									.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
							scrollPane4.setViewportView(otherAddressTextArea);
						}
						mailingAddressPanel.add(scrollPane4, cc.xywh(3, 27, 1,
								3));

						// ---- otherCityLabel ----
						otherCityLabel.setText(bundle
								.getString("otherCityLabel.text"));
						otherCityLabel.setLabelFor(otherCityTextField);
						mailingAddressPanel.add(otherCityLabel, cc.xywh(5, 27,
								1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));
						mailingAddressPanel.add(otherCityTextField, cc
								.xy(7, 27));

						// ---- otherZipPostalCodeLabel ----
						otherZipPostalCodeLabel.setText(bundle
								.getString("otherZipPostalCodeLabel.text"));
						otherZipPostalCodeLabel
								.setLabelFor(otherZipPostalCodeTextField);
						mailingAddressPanel.add(otherZipPostalCodeLabel, cc
								.xywh(5, 29, 1, 1, CellConstraints.RIGHT,
										CellConstraints.DEFAULT));
						mailingAddressPanel.add(otherZipPostalCodeTextField, cc
								.xy(7, 29));

						// ---- label3 ----
						label3.setText(bundle.getString("streetLabel.text"));
						label3.setLabelFor(otherStreetTextField);
						mailingAddressPanel
								.add(label3, cc.xywh(1, 31, 1, 1,
										CellConstraints.RIGHT,
										CellConstraints.DEFAULT));
						mailingAddressPanel.add(otherStreetTextField, cc.xy(3,
								31));

						// ---- otherStateProvinceCountyLabel ----
						otherStateProvinceCountyLabel
								.setText(bundle
										.getString("otherStateProvinceCountyLabel.text"));
						otherStateProvinceCountyLabel
								.setLabelFor(otherStateProvinceCountyTextField);
						mailingAddressPanel.add(otherStateProvinceCountyLabel,
								cc.xywh(5, 31, 1, 1, CellConstraints.RIGHT,
										CellConstraints.DEFAULT));
						mailingAddressPanel
								.add(otherStateProvinceCountyTextField, cc.xy(
										7, 31));

						// ---- otherPOBoxLabel ----
						otherPOBoxLabel.setText(bundle
								.getString("otherPOBoxLabel.text"));
						otherPOBoxLabel.setLabelFor(otherPOBoxTextField);
						mailingAddressPanel.add(otherPOBoxLabel, cc.xywh(1, 33,
								1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));
						mailingAddressPanel.add(otherPOBoxTextField, cc.xy(3,
								33));

						// ---- otherCountryLabel ----
						otherCountryLabel.setText(bundle
								.getString("otherCountryLabel.text"));
						otherCountryLabel.setLabelFor(otherCountryTextField);
						mailingAddressPanel.add(otherCountryLabel, cc.xywh(5,
								33, 1, 1, CellConstraints.RIGHT,
								CellConstraints.DEFAULT));
						mailingAddressPanel.add(otherCountryTextField, cc.xy(7,
								33));
					}
					tabbedPane2.addTab(bundle
							.getString("mailingAddressPanel.tab.title"),
							mailingAddressPanel);

				}
				contentPane.add(tabbedPane2, BorderLayout.NORTH);
			}
			dialogPane.add(contentPane, BorderLayout.CENTER);

			// ======== buttonBar ========
			{
				buttonBar.setBorder(Borders.BUTTON_BAR_GAP_BORDER);
				buttonBar.setLayout(new FormLayout(new ColumnSpec[] {
						FormFactory.GLUE_COLSPEC, FormFactory.BUTTON_COLSPEC,
						FormFactory.RELATED_GAP_COLSPEC,
						FormFactory.BUTTON_COLSPEC }, RowSpec
						.decodeSpecs("pref")));

				// ---- okButton ----
				okButton.setText(bundle.getString("okButton.text"));
				buttonBar.add(okButton, cc.xy(2, 1));

				// ---- cancelButton ----
				cancelButton.setText(bundle.getString("cancelButton.text"));
				buttonBar.add(cancelButton, cc.xy(4, 1));
			}
			dialogPane.add(buttonBar, BorderLayout.SOUTH);
		}
		contentPane2.add(dialogPane, BorderLayout.CENTER);
	}

	/**
	 * @return Returns the destModel.
	 */
	public ContactModel getDestModel() {
		return destModel;
	}

}
