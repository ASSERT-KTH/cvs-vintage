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
package org.columba.mail.gui.config.account;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import org.columba.core.gui.util.CheckBoxWithMnemonic;
import org.columba.mail.config.PopItem;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;


/**
 *
 * @author  freddy
 * @version
 */
public class PopAttributPanel implements ActionListener {
    private PopItem item;
    private JCheckBox secureCheckBox;
    private JCheckBox leaveOnServerCheckBox;
    private JCheckBox storePasswordCheckBox;
    private JCheckBox excludeCheckBox;
    private JCheckBox enablePreProcessingFilterCheckBox;
    private JCheckBox removeOldMessagesCheckBox;
    private JSpinner olderThanSpinner;
    private JLabel daysLabel;
    
    //private JCheckBox intervalCheckingCheckBox;
    //private JButton mailcheckButton;
    //private JCheckBox limitMessageDownloadCheckBox;
    private JCheckBox limitMessageDownloadCheckBox;
    private JTextField limitMessageDownloadTextField;
    private JButton configurePreProcessingFilterButton;
    private JPanel jPanel1;
    private JPanel jPanel4;
    private JPanel deleteLocallyPanel;
    private JCheckBox deleteLocallyCheckBox;
    private JPanel jPanel2;
    private JPanel jPanel3;

    //private JLabel destinationLabel;
    //private JTextField destinationTextField;
    private JButton selectButton;

    //MailCheckDialog mailCheckDialog;
    // private ConfigFrame frame;
    private JDialog dialog;

    public PopAttributPanel(JDialog dialog, PopItem item) {
        super();
        this.item = item;
        this.dialog = dialog;

        //this.frame = frame;
        //mailCheckDialog = new MailCheckDialog( item );
        initComponents();
    }

    /*
public String getDestinationFolder()
{
    return destinationTextField.getText();
}
*/
    public void updateComponents(boolean b) {
        //mailCheckDialog.updateComponents(b);
        if (b) {
            leaveOnServerCheckBox.setSelected(item.getBoolean(
                    "leave_messages_on_server"));
        	removeOldMessagesCheckBox.setSelected(item.getBooleanWithDefault("remove_old_from_server", false));
        	
        	updateRemoveOldMessagesEnabled();

        	olderThanSpinner.getModel().setValue(new Integer( item.getIntegerWithDefault("older_than", 30)));
        	
            excludeCheckBox.setSelected(item.getBooleanWithDefault(
                    "exclude_from_checkall", false));

            limitMessageDownloadCheckBox.setSelected(item.getBoolean(
                    "enable_download_limit"));

            limitMessageDownloadTextField.setText(item.get("download_limit"));

            /*
enablePreProcessingFilterCheckBox.setSelected(item.getBoolean(
"enable_pop3preprocessingfilter", false));
*/
        } else {
        	item.setBoolean("remove_old_from_server", removeOldMessagesCheckBox.isSelected());

        	item.setInteger("older_than", ((SpinnerNumberModel)olderThanSpinner.getModel()).getNumber().intValue() );
        	
        	item.setBoolean("leave_messages_on_server",
                leaveOnServerCheckBox.isSelected()); //$NON-NLS-1$

            item.setBoolean("exclude_from_checkall", excludeCheckBox.isSelected()); //$NON-NLS-1$

            item.setString("download_limit", limitMessageDownloadTextField.getText());

            item.setBoolean("enable_download_limit",
                limitMessageDownloadCheckBox.isSelected());

            /*
item.set("enable_pop3preprocessingfilter",
enablePreProcessingFilterCheckBox.isSelected());
*/
        }
    }

    /**
	 * 
	 */
	private void updateRemoveOldMessagesEnabled() {
		removeOldMessagesCheckBox.setEnabled(leaveOnServerCheckBox.isSelected());
		olderThanSpinner.setEnabled(leaveOnServerCheckBox.isSelected());
		daysLabel.setEnabled(leaveOnServerCheckBox.isSelected());
	}

	public void createPanel(DefaultFormBuilder builder) {
    	JPanel panel;
    	FormLayout l;
    	DefaultFormBuilder b;
		
    	builder.appendSeparator(MailResourceLoader.getString("dialog",
                "account", "options"));

        builder.append(leaveOnServerCheckBox, 4);
        builder.nextLine();

        builder.setLeadingColumnOffset(2);
        
        panel = new JPanel();
        l = new FormLayout("default, 3dlu, min(50;default), 3dlu, default",
            // 2 columns
            ""); // rows are added dynamically (no need to define them here)

        // create a form builder
        b = new DefaultFormBuilder(panel, l);
        b.append(removeOldMessagesCheckBox);
        b.append(olderThanSpinner);
        b.append(daysLabel);
        builder.append(panel,3);
        builder.nextLine();
        
        builder.setLeadingColumnOffset(1);
        builder.append(excludeCheckBox, 4);
        builder.nextLine();

        panel = new JPanel();
        l = new FormLayout("max(100;default), 3dlu, left:max(50dlu;default)",
                
            // 2 columns
            ""); // rows are added dynamically (no need to define them here)

        // create a form builder
        b = new DefaultFormBuilder(panel, l);
        b.append(limitMessageDownloadCheckBox, limitMessageDownloadTextField);

        builder.append(panel, 4);

        /*
builder.nextLine();

JPanel panel2 = new JPanel();
l = new FormLayout("max(100;default), 3dlu, left:max(50dlu;default)", 
    // 2 columns
    ""); // rows are added dynamically (no need to define them here)

// create a form builder
b = new DefaultFormBuilder(panel2, l);
b.append(enablePreProcessingFilterCheckBox,
    configurePreProcessingFilterButton);

builder.append(panel2, 4);
builder.nextLine();
*/
    }

    protected void initComponents() {
        leaveOnServerCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "leave_messages_on_server"));
        leaveOnServerCheckBox.setActionCommand("LEAVE_ON_SERVER");
        leaveOnServerCheckBox.addActionListener(this);
        
        limitMessageDownloadCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "limit_message_download_to"));

        limitMessageDownloadCheckBox.setActionCommand("LIMIT_MESSAGE_DOWNLOAD");
        limitMessageDownloadCheckBox.addActionListener(this);


        limitMessageDownloadTextField = new JTextField();

        excludeCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
                    "dialog", "account", "exclude_from_fetch_all"));

        removeOldMessagesCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
                "dialog", "account", "remove_old_from_server"));
        
        olderThanSpinner = new JSpinner(new SpinnerNumberModel(1,1,Integer.MAX_VALUE,1));
        
        daysLabel = new JLabel(MailResourceLoader.getString(
                "dialog", "account", "days"));
        /*
enablePreProcessingFilterCheckBox = new CheckBoxWithMnemonic(MailResourceLoader.getString(
    "dialog", "account", "enable_pop3_preprocessing"));

configurePreProcessingFilterButton = new JButton(MailResourceLoader.getString(
    "dialog", "account", "configure"));
configurePreProcessingFilterButton.setActionCommand("CONFIGURE_FILTER");
configurePreProcessingFilterButton.addActionListener(this);
*/
    }

    /*
private void initComponents_old() {

        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(layout);

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
        c.gridx = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(panel, c);
        add(panel);

        leaveOnServerCheckBox =
                new JCheckBox(
                        MailResourceLoader.getString(
                                "dialog",
                                "account",
                                "leave_messages_on_server"));
        leaveOnServerCheckBox.setMnemonic(
                MailResourceLoader.getMnemonic(
                        "dialog",
                        "account",
                        "leave_messages_on_server"));
        //$NON-NLS-1$
        c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(leaveOnServerCheckBox, c);
        add(leaveOnServerCheckBox);

        JPanel limitMessageDownloadPanel = new JPanel();
        limitMessageDownloadPanel.setLayout(
                new BoxLayout(limitMessageDownloadPanel, BoxLayout.X_AXIS));
        c.gridx = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(limitMessageDownloadPanel, c);
        add(limitMessageDownloadPanel);

        limitMessageDownloadCheckBox =
                new JCheckBox(
                        MailResourceLoader.getString(
                                "dialog",
                                "account",
                                "limit_message_download_to"));
        limitMessageDownloadCheckBox.setMnemonic(
                MailResourceLoader.getMnemonic(
                        "dialog",
                        "account",
                        "limit_message_download_to"));
        limitMessageDownloadCheckBox.setActionCommand("LIMIT_MESSAGE_DOWNLOAD");
        limitMessageDownloadCheckBox.addActionListener(this);
        limitMessageDownloadPanel.add(limitMessageDownloadCheckBox);
        limitMessageDownloadPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        limitMessageDownloadTextField = new JTextField(5);
        limitMessageDownloadPanel.add(limitMessageDownloadTextField);
        limitMessageDownloadPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        limitMessageDownloadLabel2 =
                new JLabel(
                        MailResourceLoader.getString(
                                "dialog",
                                "account",
                                "KB_per_message"));
        //$NON-NLS-1$
        limitMessageDownloadPanel.add(limitMessageDownloadLabel2);

        excludeCheckBox =
                new JCheckBox(
                        MailResourceLoader.getString(
                                "dialog",
                                "account",
                                "exclude_from_fetch_all"));
        excludeCheckBox.setMnemonic(
                MailResourceLoader.getMnemonic(
                        "dialog",
                        "account",
                        "exclude_from_fetch_all"));
        //$NON-NLS-1$
        c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(excludeCheckBox, c);
        add(excludeCheckBox);

        enablePreProcessingFilterCheckBox =
                new JCheckBox(
                        MailResourceLoader.getString(
                                "dialog",
                                "account",
                                "enable_pop3_preprocessing"));


        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new BoxLayout(filterPanel, BoxLayout.X_AXIS));

        filterPanel.add(enablePreProcessingFilterCheckBox);

        filterPanel.add(Box.createRigidArea(new Dimension(5, 0)));

        configurePreProcessingFilterButton =
                new JButton(
                        MailResourceLoader.getString("dialog", "account", "configure"));
        configurePreProcessingFilterButton.setActionCommand("CONFIGURE_FILTER");
        configurePreProcessingFilterButton.addActionListener(this);

        filterPanel.add(configurePreProcessingFilterButton);

        c = new GridBagConstraints();
        c.gridx = 0;
        c.weightx = 1.0;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.gridwidth = GridBagConstraints.REMAINDER;
        layout.setConstraints(filterPanel, c);
        add(filterPanel);
}
*/
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        if (action.equals("CONFIGURE_FILTER")) {
            /*
XmlElement list = item.getElement("pop3preprocessingfilterlist");

if (list == null) {
list = new XmlElement("pop3preprocessingfilterlist");
item.getRoot().addElement(list);
}

new org.columba.mail.gui.config.pop3preprocessor.ConfigFrame(dialog,
list);
*/
        } else if (action.equals("LIMIT_MESSAGE_DOWNLOAD")) {
            limitMessageDownloadTextField.setEnabled(limitMessageDownloadCheckBox.isSelected());
        } else if (action.equals("LEAVE_ON_SERVER")) {
        	updateRemoveOldMessagesEnabled();
        }
    }
}
