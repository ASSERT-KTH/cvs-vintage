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
package org.columba.core.plugin;


/**
 * Views found in package org.columba.core.gui.view are loaded
 * dynamically.
 * <p>
 * This makes it possible to write a plugin, for the mail component
 * where the view has a completely different layout.
 *
 * @author fdietz
 */
public class ViewPluginHandler extends AbstractPluginHandler {
    public ViewPluginHandler() {
        super("org.columba.core.view", "org/columba/core/plugin/view.xml");

        parentNode = getConfig().getRoot().getElement("viewlist");
    }
}
