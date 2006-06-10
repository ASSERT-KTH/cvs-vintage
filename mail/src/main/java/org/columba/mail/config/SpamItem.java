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
        return getBooleanWithDefault("enabled", false);
    }
    
    /**
     * Enable/Disable spam filter.
     * 
     * @param enabled 	true or false
     */
    public void setEnabled(boolean enabled) {
        setBoolean("enabled", enabled);
        
    }
    
    public boolean isMoveIncomingJunkMessagesEnabled() {
        return getBooleanWithDefault("move_incoming_junk_messages", false);
    }
    
    public void enableMoveIncomingJunkMessage(boolean enabled) {
        setBoolean("move_incoming_junk_messages", enabled);
    }
    
    public boolean isIncomingTrashSelected() {
        return getBooleanWithDefault("incoming_trash", true);
    }
    
    public void selectedIncomingTrash(boolean select) {
        setBoolean("incoming_trash", select);
    }
    
    public int getIncomingCustomFolder() {
        return getIntegerWithDefault("incoming_folder", 101);
    }
    
    public void setIncomingCustomFolder(int folder) {
        setInteger("incoming_folder", folder);
    }
    
    public boolean isMoveMessageWhenMarkingEnabled() {
        return getBooleanWithDefault("move_message_when_marking", false);
    }
    
    public void enableMoveMessageWhenMarking(boolean enabled) {
        setBoolean("move_message_when_marking", enabled);
    }
    
    public boolean isMoveTrashSelected() {
        return getBooleanWithDefault("move_trash", true);
    }
    
    public void selectMoveTrash(boolean select) {
        setBoolean("move_trash", select);
    }
    
    public int getMoveCustomFolder() {
        return getIntegerWithDefault("move_folder", 101);
    }
    
    public void setMoveCustomFolder(int folder) {
        setInteger("move_folder", folder);
    }
    
    public boolean checkAddressbook() {
        return getBooleanWithDefault("check_addressbook", false);
    }
    
    public void enableCheckAddressbook(boolean enable) {
        setBoolean("check_addressbook", enable);
    }
}
