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
package org.columba.mail.gui.config.filter.plugins;

import javax.swing.ComboBoxModel;
import javax.swing.JComboBox;

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.AccountList;
import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.gui.config.filter.CriteriaList;
import org.columba.mail.main.MailInterface;


/**
 * @author Erik Mattsson
 */
public class AccountCriteriaRow extends DefaultCriteriaRow {
    private JComboBox accountComboBox;
    private JComboBox matchComboBox;

    /**
     * @param pluginHandler the plugin handler
     * @param criteriaList the list containing criterias.
     * @param c the criteria for this filter.
     */
    public AccountCriteriaRow(AbstractPluginHandler pluginHandler,
        CriteriaList criteriaList, FilterCriteria c) {
        super(pluginHandler, criteriaList, c);
    }

    /** {@inheritDoc} */
    public void initComponents() {
        super.initComponents();

        matchComboBox = new JComboBox();
        matchComboBox.addItem("is");
        matchComboBox.addItem("is not");

        accountComboBox = new JComboBox();

        AccountList accountList = MailInterface.config.getAccountList();
        int size = accountList.count();

        for (int i = 0; i < size; i++) {
            accountComboBox.addItem(new AccountComboBoxItem(accountList.get(i)));
        }

        addComponent(matchComboBox);
        addComponent(accountComboBox);
    }

    /** {@inheritDoc} */
    public void updateComponents(boolean b) {
        super.updateComponents(b);

        if (b) {
            matchComboBox.setSelectedItem(criteria.getCriteriaString());

            int criteriaAccountUid = criteria.getInteger("account.uid", -1);

            if (criteriaAccountUid != -1) {
                ComboBoxModel model = accountComboBox.getModel();

                for (int i = 0; i < model.getSize(); i++) {
                    AccountComboBoxItem item = (AccountComboBoxItem) model.getElementAt(i);

                    if (item.getAccountID() == criteriaAccountUid) {
                        accountComboBox.setSelectedIndex(i);

                        break;
                    }
                }
            }
        } else {
            criteria.setCriteria((String) matchComboBox.getSelectedItem());

            AccountComboBoxItem item = (AccountComboBoxItem) accountComboBox.getSelectedItem();
            criteria.set("account.uid", item.getAccountID());
        }
    }

    /**
     * Combobox item for an account item.
     * @author redsolo
     */
    private class AccountComboBoxItem {
        private AccountItem accountItem;

        /**
         * Creates a combobox item that wraps the specified account item.
         * @param item the item.
         */
        public AccountComboBoxItem(AccountItem item) {
            accountItem = item;
        }

        /** {@inheritDoc} */
        public String toString() {
            return accountItem.getName();
        }

        /**
         * Returns the Account's UID.
         * @return the Account's UID.
         */
        public int getAccountID() {
            return accountItem.getUid();
        }
    }
}
