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

package org.columba.mail.config;

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;

import org.columba.mail.main.MailInterface;

import org.columba.ristretto.parser.ParserException;

public class AccountItem extends DefaultItem {
    private AccountItem defaultAccount;
    private boolean pop3;
    private Identity identity;
    private PopItem pop;
    private ImapItem imap;
    private SmtpItem smtp;
    private PGPItem pgp;
    private SpecialFoldersItem folder;
    private SpamItem spam;

    public AccountItem(XmlElement e) {
        super(e);

        pop3 = (e.getElement("/popserver") != null);
    }

    public boolean isPopAccount() {
        return pop3;
    }

    public SpecialFoldersItem getSpecialFoldersItem() {
        if (folder == null) {
            folder = new SpecialFoldersItem(getRoot().getElement("specialfolders"));
        }

        if (folder.getBoolean("use_default_account")) {
            // return default-account ImapItem instead 
            SpecialFoldersItem item = MailInterface.config.getAccountList()
                                                          .getDefaultAccount()
                                                          .getSpecialFoldersItem();

            return item;
        }

        return folder;
    }

    private AccountItem getDefaultAccount() {
        if (defaultAccount == null) {
            defaultAccount = MailInterface.config.getAccountList()
                                                 .getDefaultAccount();
        }

        return defaultAccount;
    }

    public PopItem getPopItem() {
        if (pop == null) {
            pop = new PopItem(getRoot().getElement("popserver"));
        }

        if (pop.getBoolean("use_default_account")) {
            // return default-account ImapItem instead 
            PopItem item = MailInterface.config.getAccountList()
                                               .getDefaultAccount().getPopItem();

            return item;
        }

        return pop;
    }

    public SmtpItem getSmtpItem() {
        if (smtp == null) {
            smtp = new SmtpItem(getRoot().getElement("smtpserver"));
        }

        if (smtp.getBoolean("use_default_account")) {
            // return default-account ImapItem instead 
            return getDefaultAccount().getSmtpItem();
        }

        return smtp;
    }
    
    public SpamItem getSpamItem() {
        if ( spam == null) {
            XmlElement parent = getRoot().getElement("spam");
            // create if not available
            if ( parent == null) parent = getRoot().addSubElement("spam");
            
            spam = new SpamItem(parent);
        }
        
        if (spam.getBoolean("use_default_account")) {
            // return default-account SpamItem instead 
            return getDefaultAccount().getSpamItem();
        }
        
        return spam;
    }

    public PGPItem getPGPItem() {
        if (pgp == null) {
            pgp = new PGPItem(getRoot().getElement("pgp"));
        }

        if (pgp.getBoolean("use_default_account")) {
            // return default-account ImapItem instead 
            PGPItem item = MailInterface.config.getAccountList()
                                               .getDefaultAccount().getPGPItem();

            return item;
        }

        return pgp;
    }

    public ImapItem getImapItem() {
        if (imap == null) {
            imap = new ImapItem(getRoot().getElement("imapserver"));
        }

        if (imap.getBoolean("use_default_account")) {
            // return default-account ImapItem instead 
            ImapItem item = MailInterface.config.getAccountList()
                                                .getDefaultAccount()
                                                .getImapItem();

            return item;
        }

        return imap;
    }

    /**
     * Returns the identity used with this account.
     */
    public Identity getIdentity() {
        if (identity == null) {
            XmlElement e = getRoot().getElement("identity");
            if (Boolean.valueOf(e.getAttribute("use_default_account", "false"))
                    .booleanValue()) {
                // return default-account identityItem instead
                return MailInterface.config.getAccountList().getDefaultAccount()
                        .getIdentity();
            } else {
                try {
                    identity = new Identity(e);
                } catch (ParserException ex) {
                    ex.printStackTrace();
                }
            }
        }

        return identity;
    }

    public void setName(String str) {
        set("name", str);
    }

    public String getName() {
        return get("name");
    }

    public void setUid(int i) {
        set("uid", i);
    }

    public int getUid() {
        return getInteger("uid");
    }

    public boolean isDefault() {
        if (MailInterface.config.getAccountList().getDefaultAccountUid() == getUid()) {
            return true;
        }

        return false;
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (this == obj) {
            return true; // same object
        }

        if ((obj == null) || !(obj instanceof AccountItem)) {
            return false;
        }

        /*
         * The fields on this object is in fact represented in the xml
         * structure found as getRoot(). Therefore super.equals()
         * should do the job
         */
        return super.equals(obj);
    }

    /** {@inheritDoc} */
    public int hashCode() {
        /*
         * The fields on this object is in fact represented in the xml
         * structure found as getRoot(). Therefore super.hashCode()
         * should do the job.
         */
        return super.hashCode();
    }
}
