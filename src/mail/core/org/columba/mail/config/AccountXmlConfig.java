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

import org.columba.addressbook.config.AdapterNode;

import org.columba.core.config.DefaultXmlConfig;

import java.io.File;

import java.util.List;

public class AccountXmlConfig extends DefaultXmlConfig {
    private AccountList list;

    public AccountXmlConfig(File file) {
        super(file);
    }
    
    public AccountList getAccountList() {
        if (list == null) {
            list = new AccountList(getRoot().getElement("/accountlist"));
        }

        return list;
    }

    // create uid list from all accounts
    protected void getUids(List v, AdapterNode parent) {
        int childCount = parent.getChildCount();

        if (childCount > 0) {
            for (int i = 0; i < childCount; i++) {
                AdapterNode child = parent.getChild(i);

                //System.out.println("name: "+ child.getName() );
                if (child.getName().equals("account")) {
                    AdapterNode uidNode = child.getChild("uid");

                    Integer j = new Integer(uidNode.getValue());

                    v.add(j);
                }
            }
        }
    }
}
