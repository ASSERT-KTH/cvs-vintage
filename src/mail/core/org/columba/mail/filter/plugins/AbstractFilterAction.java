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
package org.columba.mail.filter.plugins;

import org.columba.core.command.Command;
import org.columba.core.plugin.Plugin;

import org.columba.mail.filter.FilterAction;
import org.columba.mail.folder.MessageFolder;


/**
 * Action which is executed if a filter found a matching
 * set of messages.
 * <p>
 * If you need to run time consuming tasks which should be running 
 * in the background you need to create your own Command-Object. 
 * You should take a closer look to the Columba sourcetree. You will 
 * find dozens examples of Command- Objects which implement things 
 * like "Reply to Message", "Open Message in Composer", etc.
 *
 * @author fdietz
 */
public abstract class AbstractFilterAction implements Plugin {
    /**
 * 
 * @param filterAction      filterAction containing the filter actoin configuration
 * @param srcFolder         selected folder
 * @param uids              message UIDs
 * @return                  return null for simple tasks, all other tasks have to implement
 *                          their own {@link Command}
 * 
 * @throws Exception        exception is just passed to the upper-level
 */
    public abstract Command getCommand(FilterAction filterAction,
        MessageFolder srcFolder, Object[] uids) throws Exception;
}
