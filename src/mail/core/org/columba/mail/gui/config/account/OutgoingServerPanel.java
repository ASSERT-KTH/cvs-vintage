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
package org.columba.mail.gui.config.account;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.CheckBoxWithMnemonic;
import org.columba.core.gui.util.DefaultFormBuilder;
import org.columba.core.gui.util.LabelWithMnemonic;
import org.columba.core.util.ListTools;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.MailConfig;
import org.columba.mail.config.SmtpItem;
import org.columba.mail.util.MailResourceLoader;
import org.columba.ristretto.auth.AuthenticationFactory;
import org.columba.ristretto.smtp.SMTPException;
import org.columba.ristretto.smtp.SMTPProtocol;

import com.jgoodies.forms.layout.FormLayout;

/**
 * @author freddy
 * @version
 */
public class OutgoingServerPanel
    extends DefaultPanel
    implements ActionListener {
    private JLabel hostLabel;
    private JTextField hostTextField;
    private JLabel portLabel;
    private JTextField portTextField;
    private JRadioButton esmtpRadioButton;
    private JLabel loginLabel;
    private JTextField loginTextField;
    private JCheckBox secureCheckBox;
    private JCheckBox needAuthCheckBox;
    private JCheckBox bccyourselfCheckBox;
    private JLabel bccanotherLabel;
    private JTextField bccanotherTextField;
    private JButton selectButton;
    private JCheckBox storePasswordCheckBox;
    private JLabel authenticationLabel;
    private JComboBox authenticationComboBox;
    private JCheckBox defaultAccountCheckBox;
    private SmtpItem item;
    private AccountItem accountItem;
    private JButton checkAuthMethods;
    private static final Pattern authModeTokenizePattern =
        Pattern.compile("([^;]+);?");

    public OutgoingServerPanel(AccountItem accountItem) {
        super();

        this.accountItem = accountItem;
        item = accountItem.getSmtpItem();

        initComponents();

        updateComponents(true);
    }

    public String getHost() {
        return hostTextField.getText();
    }

    public String getLogin() {
        return loginTextField.getText();
    }

    public boolean isESmtp() {
        return needAuthCheckBox.isSelected();
    }

    protected void updateComponents(boolean b) {
        if (b) {
            hostTextField.setText(item.get("host"));

            portTextField.setText(item.get("port"));

            loginTextField.setText(item.get("user"));

            storePasswordCheckBox.setSelected(item.getBoolean("save_password"));

            secureCheckBox.setSelected(item.getBoolean("enable_ssl", false));

            if (!item.get("login_method").equals("NONE")) {
                needAuthCheckBox.setSelected(true);

                storePasswordCheckBox.setEnabled(true);
                loginLabel.setEnabled(true);
                loginTextField.setEnabled(true);

                String loginMethod = item.get("login_method");
                authenticationComboBox.setSelectedItem(loginMethod);
            } else {
                needAuthCheckBox.setSelected(false);

                storePasswordCheckBox.setEnabled(false);
                loginLabel.setEnabled(false);
                loginTextField.setEnabled(false);
                authenticationLabel.setEnabled(false);
                authenticationComboBox.setEnabled(false);
            }

            defaultAccountCheckBox.setEnabled(
                MailConfig.getAccountList().getDefaultAccountUid()
                    != accountItem.getInteger("uid"));

            if (defaultAccountCheckBox.isEnabled()
                && defaultAccountCheckBox.isSelected()) {
                showDefaultAccountWarning();
            } else {
                layoutComponents();
            }
        } else {
            item.set("user", loginTextField.getText());

            item.set("save_password", storePasswordCheckBox.isSelected()); //$NON-NLS-1$

            item.set("port", portTextField.getText());

            item.set("host", hostTextField.getText());

            // *20031025, karlpeder* Fixed bug which meant that it was impossible
            // to disable ssl.
            //item.set("enable_ssl", secureCheckBox.isEnabled());
            item.set("enable_ssl", secureCheckBox.isSelected());

            if (needAuthCheckBox.isSelected()) {
                String loginMethod =
                    (String) authenticationComboBox.getSelectedItem();
                item.set("login_method", loginMethod);
            } else {
                item.set("login_method", "NONE"); //$NON-NLS-1$
            }

            item.set(
                "use_default_account",
                defaultAccountCheckBox.isSelected());
        }
    }

    protected void layoutComponents() {
        //		Create a FormLayout instance.
        FormLayout layout =
            new FormLayout(
                "10dlu, 10dlu, max(100;default), 3dlu, fill:max(150dlu;default):grow ",

            // 2 columns
    ""); // rows are added dynamically (no need to define them here)

        JPanel topPanel = new JPanel();
        DefaultFormBuilder builder = new DefaultFormBuilder(this, layout);

        // create EmptyBorder between components and dialog-frame
        builder.setDefaultDialogBorder();

        // skip the first column
        builder.setLeadingColumnOffset(1);

        // Add components to the panel:
        builder.append(defaultAccountCheckBox, 5);
        builder.nextLine();

        builder.appendSeparator(
            MailResourceLoader.getString("dialog", "account", "configuration"));
        builder.nextLine();

        builder.append(hostLabel, 2);
        builder.append(hostTextField);
        builder.nextLine();

        builder.append(portLabel, 2);
        builder.append(portTextField);
        builder.nextLine();

        builder.appendSeparator(
            MailResourceLoader.getString("dialog", "account", "security"));
        builder.nextLine();

        builder.append(needAuthCheckBox, 4);
        builder.nextLine();

        builder.setLeadingColumnOffset(2);

        JPanel panel = new JPanel();
        FormLayout l =
            new FormLayout(
                "max(80dlu;default), 3dlu, fill:max(50dlu;default), 2dlu, left:max(50dlu;default)",

            // 2 columns
    ""); // rows are added dynamically (no need to define them here)

        // create a form builder
        DefaultFormBuilder b = new DefaultFormBuilder(panel, l);
        b.append(authenticationLabel, authenticationComboBox, checkAuthMethods);
        b.nextLine();
        b.append(loginLabel, loginTextField);
        builder.append(panel, 3);
        builder.nextLine();

        /*
         * JPanel panel2 = new JPanel(); l = new FormLayout("max(100;default),
         * 3dlu, left:max(50dlu;default)", // 2 columns ""); // rows are added
         * dynamically (no need to define them here) // create a form builder b =
         * new DefaultFormBuilder(panel2, l); b.append(loginLabel,
         * loginTextField);
         *
         * builder.append(panel2, 3); builder.nextLine();
         */
        //builder.setLeadingColumnOffset(1);
        builder.append(storePasswordCheckBox, 3);
        builder.nextLine();

        builder.setLeadingColumnOffset(1);

        builder.append(secureCheckBox, 4);
        builder.nextLine();
    }

    protected void showDefaultAccountWarning() {
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        GridBagLayout mainLayout = new GridBagLayout();
        GridBagConstraints mainConstraints = new GridBagConstraints();

        setLayout(mainLayout);

        mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
        mainConstraints.anchor = GridBagConstraints.NORTHWEST;
        mainConstraints.weightx = 1.0;
        mainConstraints.insets = new Insets(0, 10, 5, 0);
        mainLayout.setConstraints(defaultAccountCheckBox, mainConstraints);
        add(defaultAccountCheckBox);

        mainConstraints = new GridBagConstraints();
        mainConstraints.weighty = 1.0;
        mainConstraints.gridwidth = GridBagConstraints.REMAINDER;

        JLabel label =
            new JLabel(
                MailResourceLoader.getString(
                    "dialog",
                    "account",
                    "using_default_account_settings"));
        Font newFont = label.getFont().deriveFont(Font.BOLD);
        label.setFont(newFont);
        mainLayout.setConstraints(label, mainConstraints);
        add(label);
    }

    protected void initComponents() {
        defaultAccountCheckBox =
            new CheckBoxWithMnemonic(
                MailResourceLoader.getString(
                    "dialog",
                    "account",
                    "use_default_account_settings"));

        /*
         * defaultAccountCheckBox.setMnemonic( MailResourceLoader.getMnemonic(
         * "dialog", "account", "use_default_account_settings"));
         */

        //defaultAccountCheckBox.setEnabled(false);
        defaultAccountCheckBox.setActionCommand("DEFAULT_ACCOUNT");
        defaultAccountCheckBox.addActionListener(this);

        hostLabel = new LabelWithMnemonic(MailResourceLoader.getString("dialog", "account", "host")); //$NON-NLS-1$

        /*
         * hostLabel.setDisplayedMnemonic(
         */
        hostTextField = new JTextField();
        hostLabel.setLabelFor(hostTextField);
        portLabel = new LabelWithMnemonic(MailResourceLoader.getString("dialog", "account", "port")); //$NON-NLS-1$

        /*
         * portLabel.setDisplayedMnemonic(
         */
        portTextField = new JTextField();
        portLabel.setLabelFor(portTextField);

        needAuthCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString("dialog", "account", "server_needs_authentification")); //$NON-NLS-1$

        /*
         * needAuthCheckBox.setMnemonic( MailResourceLoader.getMnemonic(
         * "dialog", "account", "server_needs_authentification"));
         */
        needAuthCheckBox.setActionCommand("AUTH"); //$NON-NLS-1$
        needAuthCheckBox.addActionListener(this);

        storePasswordCheckBox =
            new CheckBoxWithMnemonic(
                MailResourceLoader.getString(
                    "dialog",
                    "account",
                    "store_password_in_configuration_file"));

        /*
         * storePasswordCheckBox.setMnemonic( MailResourceLoader.getMnemonic(
         * "dialog", "account",
         */
        secureCheckBox =
            new CheckBoxWithMnemonic(
                MailResourceLoader.getString(
                    "dialog",
                    "account",
                    "use_SSL_for_secure_connection"));

        /*
         * secureCheckBox.setMnemonic(MailResourceLoader.getMnemonic("dialog",
         * "account", "use_SSL_for_secure_connection")); //$NON-NLS-1$
         */
        authenticationLabel =
            new LabelWithMnemonic(
                MailResourceLoader.getString(
                    "dialog",
                    "account",
                    "authentication_type"));

		authenticationComboBox = new JComboBox();
		authenticationLabel.setLabelFor(authenticationComboBox);

        updateAuthenticationComboBox();

        checkAuthMethods =
            new ButtonWithMnemonic(
                MailResourceLoader.getString(
                    "dialog",
                    "account",
                    "authentication_checkout_methods"));
        checkAuthMethods.setActionCommand("CHECK_AUTHMETHODS");
        checkAuthMethods.addActionListener(this);

        //authenticationComboBox.addActionListener(this);
        authenticationLabel.setLabelFor(authenticationComboBox);

        loginLabel =
            new LabelWithMnemonic(
                MailResourceLoader.getString("dialog", "account", "login"));

        /*
         * loginLabel.setDisplayedMnemonic(
         */
        loginTextField = new JTextField();
        loginLabel.setLabelFor(loginTextField);
    }

    /**
     * 
     */
    private void updateAuthenticationComboBox() {
        authenticationComboBox.removeAllItems();
        
		authenticationComboBox.addItem(
            MailResourceLoader.getString(
                "dialog",
                "account",
                "authentication_securest"));

		if (accountItem.isPopAccount()) {
			authenticationComboBox.addItem("POP before SMTP");
		}


        String authMethods =
            accountItem.get("smtpserver", "authentication_methods");

        // Add previously fetch authentication modes
        if (authMethods != null) {
            Matcher matcher = authModeTokenizePattern.matcher(authMethods);

            while (matcher.find()) {
                authenticationComboBox.addItem(matcher.group(1));
            }
        }
        
        authenticationComboBox.setSelectedItem(accountItem.get("smtpserver", "login_method"));
    }

    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (e.getSource().equals(authenticationComboBox)) {
            String selection =
                (String) authenticationComboBox.getSelectedItem();

            loginLabel.setEnabled(true);
            loginTextField.setEnabled(true);
            storePasswordCheckBox.setEnabled(true);
        } else if (action.equals("DEFAULT_ACCOUNT")) {
            removeAll();

            if (defaultAccountCheckBox.isSelected()) {
                showDefaultAccountWarning();
            } else {
                layoutComponents();
            }

            revalidate();
        } else 
            if (action.equals("AUTH")) {
                if (needAuthCheckBox.isSelected()) {
                    loginLabel.setEnabled(true);
                    loginTextField.setEnabled(true);
                    storePasswordCheckBox.setEnabled(true);
                    authenticationLabel.setEnabled(true);
                    authenticationComboBox.setEnabled(true);
					checkAuthMethods.setEnabled(true);
                } else {
                    loginLabel.setEnabled(false);
                    loginTextField.setEnabled(false);
                    storePasswordCheckBox.setEnabled(false);
                    authenticationLabel.setEnabled(false);
                    authenticationComboBox.setEnabled(false);
					checkAuthMethods.setEnabled(false);
                }
            
        } else if ( action.equals("CHECK_AUTHMETHODS")) {
			getAuthMechanisms();
        }
    }
    
    
    private void getAuthMechanisms() {
        {
            List list = new LinkedList();

            try {
                list = getAuthSMTP();
                ListTools.intersect_astable(list, AuthenticationFactory.getInstance().getSupportedMechanisms());                
            } catch (IOException e1) {
                String name = e1.getClass().getName();
                JOptionPane.showMessageDialog(
                    null,
                    e1.getLocalizedMessage(),
                    name.substring(name.lastIndexOf(".")),
                    JOptionPane.ERROR_MESSAGE);
            } catch (SMTPException e1) {
                //TODO sever does not support ehlo
            }

            // Save the authentication modes
            if (list.size() > 0) {
                StringBuffer authMethods = new StringBuffer();
                Iterator it = list.iterator();
                authMethods.append(it.next());

                while (it.hasNext()) {
                    authMethods.append(';');
                    authMethods.append(it.next());
                }

                accountItem.set(
                    "smtpserver",
                    "authentication_methods",
                    authMethods.toString());
            }

            updateAuthenticationComboBox();
        }
    }
	/**
	  * @return
	  */
	 private List getAuthSMTP() throws IOException, SMTPException {
		 List result = new LinkedList();
		 SMTPProtocol protocol =
			 new SMTPProtocol(
				 accountItem.get("smtpserver", "host"),
				 accountItem.getInteger("smtpserver", "port"));

		 protocol.openPort();
		 String[] capas = protocol.ehlo("localhost");
		 for (int i = 0; i < capas.length; i++) {
			 if (capas[i].startsWith("AUTH")) {
				 result = parseAuthCapas(capas[i]);
			 }
		 }

		 return result;
	 }
	 /**
	  * @param string
	  * @return
	  */
	 private List parseAuthCapas(String string) {
		 Matcher tokenizer = Pattern.compile("\\b[^\\s]+\\b").matcher(string);
		 tokenizer.find();

		 List mechanisms = new LinkedList();

		 while (tokenizer.find()) {
			 mechanisms.add(tokenizer.group());
		 }

		 return mechanisms;
	 }

    public boolean isFinished() {
        boolean result = false;
        String host = getHost();
        boolean esmtp = isESmtp();

        if (host.length() == 0) {
            JOptionPane.showMessageDialog(null, MailResourceLoader.getString("dialog", "account", "You_have_to_enter_a_host_name")); //$NON-NLS-1$

            return false;
        } else if (esmtp == true) {
            String login = getLogin();

            if (login.length() == 0) {
                JOptionPane.showMessageDialog(null, MailResourceLoader.getString("dialog", "account", "You_have_to_enter_a_login_name")); //$NON-NLS-1$

                return false;
            }
        }

        return true;
    }
}
