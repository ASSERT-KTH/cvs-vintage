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

package org.columba.mail.config;

import java.net.URL;

import org.columba.core.config.DefaultItem;
import org.columba.core.io.DiskIO;
import org.columba.core.xml.XmlElement;
import org.columba.core.xml.XmlIO;

public class AccountList extends DefaultItem {

    protected int nextUid;

    protected AccountItem defaultAccount;

    public AccountList(XmlElement root) {
        super(root);

        AccountItem item;

        nextUid = -1;

        int uid;

        for (int i = 0; i < count(); i++) {
            item = get(i);
            uid = item.getInteger("uid");

            if (uid > nextUid) {
                nextUid = uid;
            }
        }

        nextUid++;
    }

    public AccountItem get(int index) {
        XmlElement e = getChildElement(index);

        //XmlElement.printNode(e,"");

        /*
         * if ((index >= 0) && (index < list.size())) return (AccountItem)
         * list.get(index);
         * 
         * return null;
         */
        return new AccountItem(e);
    }

    public AccountItem uidGet(int uid) {
        XmlElement e;

        for (int i = 0; i < count(); i++) {
            e = getChildElement(i);

            int u = Integer.parseInt(e.getAttribute("uid"));

            if (uid == u) { return new AccountItem(e); }
        }

        return null;
    }

    /*
     * search for PGPItem based on To headerfield
     *  
     */
    public PGPItem getPGPItem(String to) {
        int result = -1;

        for (int i = 0; i < count(); i++) {
            AccountItem item = (AccountItem) get(i);
            PGPItem pgpItem = item.getPGPItem();
            String id = pgpItem.get("id");

            to = to.toLowerCase();
            id = id.toLowerCase();

            if (to.indexOf(id) != -1) {
                return pgpItem;
            } else if (id.indexOf(to) != -1) { return pgpItem; }
        }

        return null;
    }

    /**
     * Get account using the email address to identify it.
     * 
     * @param address
     *            email address
     * @return account item
     */
    public AccountItem getAccount(String address) {

        for (int i = 0; i < count(); i++) {
            AccountItem item = get(i);
            Identity identity = item.getIdentity();
            String str = identity.getAddress().toString();
            if (address.indexOf(str) != -1) {
                // found match
                return item;
            }
        }
        return null;
    }

    public AccountItem hostGetAccount(String host, String address) {
        XmlElement account;
        XmlElement server;
        XmlElement identity;

        if (address == null) { return get(0); }

        for (int i = 0; i < count(); i++) {
            account = getChildElement(i);

            server = account.getElement("popserver");

            if (server == null) {
                server = account.getElement("imapserver");
            }

            if (server.getAttribute("host").equals(host)) {
                return new AccountItem(account);
            }
        }

        for (int i = 0; i < count(); i++) {
            account = getChildElement(i);

            identity = account.getElement("identity");

            if (identity.getAttribute("address").indexOf(address) != -1) {
                return new AccountItem(account);
            }
        }

        return null;
    }

    public AccountItem addEmptyAccount(String type) {
        // path to account templates for POP3/IMAP
        String hstr = "org/columba/mail/config/account_template.xml";
        URL url = DiskIO.getResourceURL(hstr);
        XmlIO xmlIo = new XmlIO();
        // load xml document
        xmlIo.load(url);
        XmlElement root = xmlIo.getRoot();
        // get pop3 or imap account xml node
        XmlElement emptyAccount = root.getElement("/template/" + type
                + "/account");

        if (emptyAccount != null) {
            AccountItem newAccount = new AccountItem((XmlElement) emptyAccount
                    .clone());
            newAccount.set("uid", getNextUid());
            add(newAccount);

            return newAccount;
        }

        return null;
    }

    public void add(AccountItem item) {
        getRoot().addSubElement(item.getRoot());

        if (item.getInteger("uid") >= nextUid) {
            nextUid = item.getInteger("uid") + 1;
        }

        if (count() == 1) {
            setDefaultAccount(item.getInteger("uid"));
        }
    }

    public AccountItem remove(int index) {
        return new AccountItem(getRoot().removeElement(index));
    }

    public int count() {
        return getRoot().count();
    }

    protected int getNextUid() {
        return nextUid++;
    }

    /** ************************** default account ******************* */
    public void setDefaultAccount(int uid) {
        set("default", uid);
        defaultAccount = null;
    }

    public int getDefaultAccountUid() {
        return getInteger("default");
    }

    public AccountItem getDefaultAccount() {
        if (defaultAccount == null) {
            defaultAccount = uidGet(getDefaultAccountUid());
        }

        return defaultAccount;
    }
}