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
package org.columba.mail.gui.composer.command;

import org.columba.core.command.DefaultCommandReference;

import org.columba.mail.composer.MessageBuilderHelper;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.MessageFolder;

import org.columba.ristretto.message.Address;
import org.columba.ristretto.message.BasicHeader;
import org.columba.ristretto.message.Header;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Reply to All senders.
 * 
 * @author fdietz
 */
public class ReplyToAllCommand extends ReplyCommand {
    protected final String[] headerfields = new String[] {
            "Subject", "From", "To", "Cc", "Reply-To", "Message-ID",
            "In-Reply-To", "References"
        };

    /**
     * Constructor for ReplyToAllCommand.
     * 
     * @param frameMediator
     * @param references
     */
    public ReplyToAllCommand(DefaultCommandReference[] references) {
        super(references);
    }

    protected void initHeader(MessageFolder folder, Object[] uids)
        throws Exception {
        // get headerfields
        Header header = folder.getHeaderFields(uids[0], headerfields);

        BasicHeader rfcHeader = new BasicHeader(header);

        // set subject
        model.setSubject(MessageBuilderHelper.createReplySubject(
                rfcHeader.getSubject()));

        LinkedList toList = new LinkedList();
        toList.addAll(Arrays.asList(rfcHeader.getReplyTo()));
        toList.add(rfcHeader.getFrom());
        toList.addAll(Arrays.asList(rfcHeader.getTo()));
        
        // bug #997560 (fdietz): CC: should be in Cc:, instead of To:
        //toList.addAll(Arrays.asList(rfcHeader.getCc()));

        // remove duplicates
        Collections.sort(toList);

        Iterator it = toList.iterator();
        Address last = (Address) it.next();

        while (it.hasNext()) {
            Address act = (Address) it.next();

            if (last.equals(act)) {
                it.remove();
            } else {
                last = act;
            }
        }

        Address[] to = (Address[]) toList.toArray(new Address[] {  });

        // Add addresses to the addressbook
        MessageBuilderHelper.addAddressesToAddressbook(to);
        model.setTo(to);
        
        // bug #997560 (fdietz): CC: should be in Cc:, instead of To:
        model.setCc(rfcHeader.getCc());

        // create In-Reply-To:, References: headerfields
        MessageBuilderHelper.createMailingListHeaderItems(header, model);

        // select the account this mail was received from
        Integer accountUid = (Integer) folder.getAttribute(uids[0],
                "columba.accountuid");
        AccountItem accountItem = MessageBuilderHelper.getAccountItem(accountUid);
        model.setAccountItem(accountItem);
    }
}
