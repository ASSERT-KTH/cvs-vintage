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

import org.columba.core.config.DefaultItem;
import org.columba.core.xml.XmlElement;


/**
 * Configuration data for spam.
 * <p>
 * 
 * @author fdietz
 *
 */
public class SpamItem extends DefaultItem {

    
    public SpamItem(XmlElement e) {
        super(e);
    }
    
    /**
     * Check if spam filtering is enabled.
     * 
     * @return 	true, if enabled. False, otherwise.
     */
    public boolean isEnabled() {
        return getBoolean("enabled", false);
    }
    
    /**
     * Enable/Disable spam filter.
     * 
     * @param enabled 	true or false
     */
    public void setEnabled(boolean enabled) {
        set("enabled", enabled);
        
    }
    
    public boolean isMoveIncomingJunkMessagesEnabled() {
        return getBoolean("move_incoming_junk_messages", false);
    }
    
    public void enableMoveIncomingJunkMessage(boolean enabled) {
        set("move_incoming_junk_messages", enabled);
    }
    
    public boolean isIncomingTrashSelected() {
        return getBoolean("incoming_trash", true);
    }
    
    public void selectedIncomingTrash(boolean select) {
        set("incoming_trash", select);
    }
    
    public int getIncomingCustomFolder() {
        return getInteger("incoming_folder", 101);
    }
    
    public void setIncomingCustomFolder(int folder) {
        set("incoming_folder", folder);
    }
    
    public boolean isMoveMessageWhenMarkingEnabled() {
        return getBoolean("move_message_when_marking", false);
    }
    
    public void enableMoveMessageWhenMarking(boolean enabled) {
        set("move_message_when_marking", enabled);
    }
    
    public boolean isMoveTrashSelected() {
        return getBoolean("move_trash", true);
    }
    
    public void selectMoveTrash(boolean select) {
        set("move_trash", select);
    }
    
    public int getMoveCustomFolder() {
        return getInteger("move_folder", 101);
    }
    
    public void setMoveCustomFolder(int folder) {
        set("move_folder", folder);
    }
    
    public boolean checkAddressbook() {
        return getBoolean("check_addressbook", false);
    }
    
    public void enableCheckAddressbook(boolean enable) {
        set("check_addressbook", enable);
    }
}
