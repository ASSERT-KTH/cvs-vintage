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

import org.columba.core.plugin.Plugin;

import org.columba.mail.filter.FilterCriteria;
import org.columba.mail.folder.MessageFolder;


/**
 * A filter is used to find a set of messages, which
 * matches a certain search criteria.
 * <p>
 * The search criteria are specified in {@link Filter}.
 * <p>
 * Every FilterPlugin needs to subclass this class.
 *
 * @author fdietz
 */
public abstract class AbstractFilter implements Plugin {
    /**
 * Constructor for AbstractFilter

 */
    public AbstractFilter() {
    }

    /**
 * 
 * @param f    filter containing the configuration
 */
    public abstract void setUp(FilterCriteria f);

    /**
  *
  * Execute the plugin
  *
  *
  * @param folder        MessageFolder on which the filter gets applied
  * @param uid           uid of Message object
  *                      on the Statusbar
  * @return boolean      true if match, otherwise false
  *
  * @throws Exception    pass exception one level higher to handle it in the
  *                      correct  place
  */
    public abstract boolean process(MessageFolder folder, Object uid)
        throws Exception;
}
