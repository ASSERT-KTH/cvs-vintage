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
package org.columba.mail.gui.message.attachment.selection;

import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.mail.folder.AbstractMessageFolder;


public class AttachmentSelectionChangedEvent extends SelectionChangedEvent {
    private AbstractMessageFolder folder;
    private Object messageUid;
    private Integer[] address;

    public AttachmentSelectionChangedEvent(AbstractMessageFolder folder, Object messageUid,
        Integer[] address) {
        this.folder = folder;
        this.messageUid = messageUid;
        this.address = address;
    }

    /**
 * @return Integer[]
 */
    public Integer[] getAddress() {
        return address;
    }

    /**
 * @return Folder
 */
    public AbstractMessageFolder getFolder() {
        return folder;
    }

    /**
 * @return Object
 */
    public Object getMessageUid() {
        return messageUid;
    }
}
