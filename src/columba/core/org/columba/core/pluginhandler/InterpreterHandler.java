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
package org.columba.core.pluginhandler;

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.plugin.PluginLoader;
import org.columba.core.scripting.AbstractInterpreter;
import org.columba.core.xml.XmlElement;

import java.util.Hashtable;


/**
 * This handler makes it possible to add new interpreter support
 * in Columba.
 * <p>
 * This is the way we realized the python support for plugins.
 *
 * @author fdietz
 */
public class InterpreterHandler extends AbstractPluginHandler {
    private Hashtable interpreterTable;

    /**
 * Constructor for InterpreterHandler.
 * @param id
 * @param config
 */
    public InterpreterHandler() {
        super("org.columba.core.interpreter", null);
        interpreterTable = new Hashtable();
    }

    /**
 * @see org.columba.core.plugin.AbstractPluginHandler#getDefaultNames()
 */
    public String[] getPluginIdList() {
        return null;
    }

    public AbstractInterpreter getInterpreter(String type) {
        return (AbstractInterpreter) interpreterTable.get(type);
    }

    /* (non-Javadoc)
 * @see org.columba.core.plugin.AbstractPluginHandler#addExtension(java.lang.String, org.columba.core.xml.XmlElement)
 */
    public void addExtension(String id, XmlElement extension) {
        XmlElement interpreter = extension.getElement("interpreter");

        try {
            interpreterTable.put(interpreter.getAttribute("name"),
                PluginLoader.loadExternalPlugin(interpreter.getAttribute(
                        "main_class"), pluginManager.getPluginType(id),
                    pluginManager.getJarFile(id), null));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
