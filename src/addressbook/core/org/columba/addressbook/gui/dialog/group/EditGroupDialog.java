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
package org.columba.addressbook.gui.dialog.group;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.addressbook.config.AdapterNode;
import org.columba.addressbook.folder.GroupListCard;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.addressbook.gui.autocomplete.DefaultAddressComboBox;
import org.columba.addressbook.gui.list.AddressbookDNDListView;
import org.columba.addressbook.gui.list.AddressbookListModel;
import org.columba.addressbook.gui.list.AddressbookListRenderer;
import org.columba.addressbook.util.AddressbookResourceLoader;

import org.columba.core.gui.util.ButtonWithMnemonic;
import org.columba.core.gui.util.DefaultFormBuilder;

import org.columba.mail.util.AddressCollector;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;


public class EditGroupDialog extends JDialog implements ActionListener,
    KeyListener {
    private AddressbookDNDListView list;
    private JButton addButton;
    private JButton removeButton;
    private JLabel nameLabel;
    private JLabel descriptionLabel;
    private JTextField nameTextField;
    private JTextField descriptionTextField;
    private DefaultAddressComboBox addComboBox;
    private AddressbookListModel members;
    private AddressbookListRenderer renderer;
    private boolean result;
    private ButtonWithMnemonic okButton;
    private ButtonWithMnemonic cancelButton;

    /**
 * Constructor
 * 
 * @param frame
 *            parent frame
 * @param groupNode
 *            null, if you want to create a new group. Otherwise, the
 *            groupNode will be modified.
 */
    public EditGroupDialog(JFrame frame, AdapterNode groupNode) {
        super(frame, true);

        result = false;

        renderer = new AddressbookListRenderer();

        setTitle(AddressbookResourceLoader.getString("dialog",
                "editgroupdialog", "contact_list_editor")); //$NON-NLS-1$

        //set title
        initComponents();

        layoutComponents();

        pack();

        setLocationRelativeTo(null);
        
        setVisible(true);
    }

    private JPanel createGroupNamePanel() {
        JPanel panel = new JPanel();
        FormLayout layout = new FormLayout("12px, right:default, 6px, default:grow",
                ""); //$NON-NLS-1$ //$NON-NLS-2$

        DefaultFormBuilder b = new DefaultFormBuilder(panel, layout);
        b.setRowGroupingEnabled(true);
        b.setLeadingColumnOffset(1);

        b.appendSeparator(AddressbookResourceLoader.getString("dialog",
                "editgroupdialog", "description_3")); //$NON-NLS-1$

        b.append(nameLabel);
        b.append(nameTextField);

        b.append(descriptionLabel);
        b.append(descriptionTextField);

        return panel;
    }

    private JPanel createGroupPanel() {
        JPanel panel = new JPanel();
        FormLayout layout = new FormLayout("6dlu, fill:default:grow, 6px, default", //$NON-NLS-1$
                "default, 12px, default, 6px, default, fill:default:grow"); //$NON-NLS-1$

        PanelBuilder builder = new PanelBuilder(panel, layout);
        CellConstraints cc = new CellConstraints();

        builder.addSeparator(AddressbookResourceLoader.getString("dialog",
                "editgroupdialog", "group_members"), cc.xywh(1, 1, 4, 1)); //$NON-NLS-1$

        builder.add(addComboBox, cc.xy(2, 3));
        builder.add(addButton, cc.xy(4, 3));
        builder.add(new JScrollPane(list), cc.xywh(2, 5, 1, 2));
        builder.add(removeButton, cc.xy(4, 5));

        return panel;
    }

    private void layoutComponents() {
        getContentPane().setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        FormLayout layout = new FormLayout("fill:default:grow", //$NON-NLS-1$
                "default, 12px, fill:default:grow"); //$NON-NLS-1$

        CellConstraints cc = new CellConstraints();
        mainPanel.setLayout(layout);

        mainPanel.add(createGroupNamePanel(), cc.xy(1, 1));
        mainPanel.add(createGroupPanel(), cc.xy(1, 3));

        getContentPane().add(mainPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

        JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 6, 0));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        getContentPane().add(bottomPanel, BorderLayout.SOUTH);
    }

    private void initComponents() {
        nameLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
                    "editgroupdialog", "name")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        nameTextField = new JTextField();

        descriptionLabel = new JLabel(AddressbookResourceLoader.getString(
                    "dialog", "editgroupdialog", "description_2")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        descriptionTextField = new JTextField();

        addComboBox = new DefaultAddressComboBox();
        ((JTextComponent) addComboBox.getEditor().getEditorComponent()).addKeyListener(this);

        members = new AddressbookListModel();
        list = new AddressbookDNDListView(members);
        list.setMinimumSize(new Dimension(200, 300));

        addButton = new JButton("Add"); //$NON-NLS-1$
        addButton.addActionListener(this);
        addButton.setActionCommand("ADD"); //$NON-NLS-1$

        removeButton = new JButton("Remove"); //$NON-NLS-1$
        removeButton.addActionListener(this);
        removeButton.setActionCommand("REMOVE"); //$NON-NLS-1$

        okButton = new ButtonWithMnemonic(AddressbookResourceLoader.getString(
                    "global", "ok")); //$NON-NLS-1$ //$NON-NLS-2$
        okButton.setActionCommand("OK"); //$NON-NLS-1$
        okButton.addActionListener(this);

        cancelButton = new ButtonWithMnemonic(AddressbookResourceLoader.getString(
                    "global", "cancel")); //$NON-NLS-1$ //$NON-NLS-2$
        cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$
        cancelButton.addActionListener(this);

        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(this, "CANCEL", //$NON-NLS-1$
            KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public boolean getResult() {
        return result;
    }

    public void updateComponents(GroupListCard card, HeaderItemList list,
        boolean b) {
        if (b) {
            // gettext
            nameTextField.setText(card.get("displayname")); //$NON-NLS-1$
            descriptionTextField.setText(card.get("description")); //$NON-NLS-1$

            members = new AddressbookListModel();

            for (int i = 0; i < list.count(); i++) {
                HeaderItem item = list.get(i);
                members.addElement(item);
            }

            this.list.setModel(members);
        } else {
            // settext
            card.set("displayname", nameTextField.getText()); //$NON-NLS-1$
            card.set("description", descriptionTextField.getText()); //$NON-NLS-1$

            // remove all children
            card.removeMembers();

            // add children
            for (int i = 0; i < members.getSize(); i++) {
                HeaderItem item = (HeaderItem) members.get(i);
                Object uid = item.getUid();
                card.addMember(((Integer) uid).toString());
            }
        }
    }

    /**
 * Add headeritem from ComboBox to List
 *  
 */
    private void addHeaderItem() {
        String s = addComboBox.getText();
        Object o = AddressCollector.getHeaderItem(s);

        if (o != null) {
            // this is a headeritem from autocompletion
            members.addElement((HeaderItem) o);
            addComboBox.setText(""); //$NON-NLS-1$
        } else {
            JOptionPane.showMessageDialog(null,
                AddressbookResourceLoader.getString("dialog",
                    "editgroupdialog", "You_can_only_add")); //$NON-NLS-1$
        }

        // in the future, it will be possible to also add new addresses

        /*
 * else { // this is a string // -> check for validity if
 * (AddressParser.isValid(s)) { HeaderItem item= new
 * HeaderItem(HeaderItem.CONTACT); item.add("displayname",
 * AddressParser.getDisplayname(s)); item.add("email;internet",
 * AddressParser.getAddress(s));
 * 
 * members.addElement(item); addComboBox.setText("");
 *  } else { JOptionPane.showMessageDialog( null, s + " is no valid
 * email address!");
 *  } }
 */
    }

    public void actionPerformed(ActionEvent e) {
        String command = e.getActionCommand();

        if (command.equals("CANCEL")) { //$NON-NLS-1$
            result = false;
            setVisible(false);
        } else if (command.equals("OK")) { //$NON-NLS-1$

            if (nameTextField.getText().length() == 0) {
                JOptionPane.showMessageDialog(this,
                    AddressbookResourceLoader.getString("dialog",
                        "editgroupdialog", "you_must_enter_a_name_for_the_group")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

                return;
            }

            result = true;
            setVisible(false);
        } else if (command.equals("ADD")) { //$NON-NLS-1$
            addHeaderItem();
        } else if (command.equals("REMOVE")) { //$NON-NLS-1$

            int[] array = list.getSelectedIndices();

            for (int j = 0; j < array.length; j++) {
                members.remove(array[j]);
            }
        }
    }

    /** ************************* KeyListener **************************** */
    public void keyTyped(KeyEvent e) {
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
        char ch = e.getKeyChar();

        if (ch == KeyEvent.VK_ENTER) {
            addHeaderItem();
        }
    }
}
