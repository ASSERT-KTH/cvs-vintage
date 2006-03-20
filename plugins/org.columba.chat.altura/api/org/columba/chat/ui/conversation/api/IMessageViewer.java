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
package org.columba.chat.ui.conversation.api;

import org.columba.chat.model.api.IBuddyStatus;
import org.jivesoftware.smack.packet.Message;


/**
 * IViewer used by the conversation/chat window to present messages.
 * 
 * @author fdietz
 *
 */
public interface IMessageViewer {

    /**
     * Create viewer for a received message.
     * 
     * @param message			jabber message 	
     * @param buddy				jabber id information
     * @return
     */
    void displayReceivedMessage(Message message, IBuddyStatus buddy);
    
    /**
     * Create viewer for a message send by you.
     * 
     * @param message			jabber message 	
     * @param buddy				jabber id information
     * @return
     */
    void displaySendMessage(Message message, IBuddyStatus buddy);
}
