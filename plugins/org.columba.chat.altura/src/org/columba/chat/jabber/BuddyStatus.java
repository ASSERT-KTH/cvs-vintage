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
package org.columba.chat.jabber;

import org.columba.chat.ui.conversation.ChatMediator;
import org.jivesoftware.smack.packet.Presence;


/**
 * @author fdietz
 *
 */
public class BuddyStatus {
    private String name;
    private String jabberId;
    private Presence.Mode presenceMode;
    private String statusMessage;
    private boolean signedOn;
    private ChatMediator mediator;
    
    public BuddyStatus(String jabberId) {
        this.jabberId = jabberId;
    }
    /**
     * @return Returns the jabberId.
     */
    public String getJabberId() {
        return jabberId;
    }
    /**
     * @return Returns the presenceMode.
     */
    public Presence.Mode getPresenceMode() {
        return presenceMode;
    }
    /**
     * @return Returns the signedOn.
     */
    public boolean isSignedOn() {
        return signedOn;
    }
    /**
     * @return Returns the statusMessage.
     */
    public String getStatusMessage() {
        return statusMessage;
    }
    /**
     * @return Returns the mediator.
     */
    public ChatMediator getChatMediator() {
        return mediator;
    }
    /**
     * @param mediator The mediator to set.
     */
    public void setChatMediator(ChatMediator mediator) {
        this.mediator = mediator;
    }
    /**
     * @param presenceMode The presenceMode to set.
     */
    public void setPresenceMode(Presence.Mode presenceMode) {
        this.presenceMode = presenceMode;
    }
    /**
     * @param signedOn The signedOn to set.
     */
    public void setSignedOn(boolean signedOn) {
        this.signedOn = signedOn;
    }
    /**
     * @param statusMessage The statusMessage to set.
     */
    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
    /**
     * @return Returns the user.
     */
    public String getName() {
        return name;
    }
    /**
     * @param user The user to set.
     */
    public void setName(String user) {
        this.name = user;
    }
}
