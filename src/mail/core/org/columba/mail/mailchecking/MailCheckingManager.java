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
package org.columba.mail.mailchecking;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.main.ConnectionStateImpl;

import org.columba.mail.config.AccountItem;
import org.columba.mail.config.AccountList;
import org.columba.mail.main.MailInterface;

import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Vector;


/**
 * Manages automatic mail-checking for all accounts.
 * <p>
 * {@link AbstractMailCheckingAction} contains a timer object
 * for automatic mail-checking, which triggers the account-type
 * specific <code>actionPerformed</code> method.
 *
 *
 * @author fdietz
 */
public class MailCheckingManager extends Observable {
    private List list;

    public MailCheckingManager() {
        super();

        list = new Vector();

        // get list of all accounts
        AccountList accountList = MailInterface.config.getAccountList();

        // for each account
        for (int i = 0; i < accountList.count(); i++) {
            AccountItem accountItem = accountList.get(i);

            add(accountItem);
        }
    }

    /**
     * Return array of actions to create the mail-checking
     * menu.
     *
     *
     * @return                array of actions
     *
     * @see
     */
    public AbstractColumbaAction[] getActions() {
        AbstractColumbaAction[] actions = new AbstractColumbaAction[list.size()];

        Iterator it = list.iterator();
        int i = 0;

        while (it.hasNext()) {
            actions[i++] = ((AbstractMailCheckingAction) it.next());
        }

        return actions;
    }

    public AbstractMailCheckingAction get(int uid) {
        Iterator it = list.iterator();

        // for each account
        while (it.hasNext()) {
            AbstractMailCheckingAction action = (AbstractMailCheckingAction) it.next();

            AccountItem accountItem = action.getAccountItem();
            int i = accountItem.getUid();

            if (i == uid) {
                // found matching account
                return action;
            }
        }

        return null;
    }

    public void remove(int uid) {
        AbstractMailCheckingAction action = get(uid);

        // remove this account
        if (action != null) {
            list.remove(action);
        }
    }

    public void restartTimer(int uid) {
        AbstractMailCheckingAction action = get(uid);

        // restart timer
        if (action != null) {
            action.restartTimer();
        }
    }

    public void add(AccountItem accountItem) {
        if (accountItem.isPopAccount()) {
            list.add(new POP3MailCheckingAction(accountItem));
        } else {
            list.add(new IMAPMailCheckingAction(accountItem));
        }
    }

    /**
     * Check for new messages in all accounts
     *
     */
    public void checkAll() {
		//  check if we are online
		if (ConnectionStateImpl.getInstance().isOnline() == false) {
			// offline -> go online
			ConnectionStateImpl.getInstance().setOnline(true);
		}
        Iterator it = list.iterator();

        // for each account that is enabled and in the fetchalllist
        while (it.hasNext()) {
            AbstractMailCheckingAction action = (AbstractMailCheckingAction) it.next();
            if( action.isCheckAll() && action.isEnabled()) action.check();
        }
    }

    /**
     * Notify all observers.
     *
     */
    public void update() {
        setChanged();

        notifyObservers();
    }
}
