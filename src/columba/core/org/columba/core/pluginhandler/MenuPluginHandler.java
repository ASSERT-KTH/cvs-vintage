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
package org.columba.core.pluginhandler;

import org.columba.core.gui.menu.ContextMenu;
import org.columba.core.gui.menu.Menu;
import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.xml.XmlElement;

import java.util.Iterator;


/**
 * A basic core menu is initially created by this plugin-handler.
 * <p>
 * Mail and addressbook components plug themselves in this menu.
 *
 * @author fdietz
 */
public class MenuPluginHandler extends AbstractPluginHandler {
    public MenuPluginHandler(String handlerName) {
        super(handlerName, "org/columba/core/plugin/menus.xml");

        parentNode = new XmlElement("menus");
    }

    public void insertPlugins(Menu menu) {
        for (Iterator it = parentNode.getElements().listIterator();
                it.hasNext();) {
            menu.extendMenu((XmlElement) it.next());
        }
    }

    public void insertPlugins(ContextMenu menu) {
        for (Iterator it = parentNode.getElements().listIterator();
                it.hasNext();) {
            menu.extendMenu((XmlElement) it.next());
        }
    }
}
