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
package org.columba.mail.plugin;

import java.util.List;
import java.util.Vector;

import org.columba.core.main.MainInterface;
import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.xml.XmlElement;


/**
 * MessageFolder specific options.
 *
 * @author fdietz
 */
public class FolderOptionsPluginHandler extends AbstractPluginHandler {
    /**
 * @param id
 * @param config
 */
    public FolderOptionsPluginHandler() {
        super("org.columba.mail.folderoptions",
            "org/columba/mail/plugin/folderoptions.xml");

        parentNode = getConfig().getRoot().getElement("folderoptions");
    }

    /**
 * Adds a state check to <code>getPluginIdList()</code>.
 *
 * @param state      0, if state is "before". Otherwise, is "after"
 *
 * @return           array of plugin ids
 */
    public String[] getPluginIdList(int state) {
        int count = parentNode.count();

        //String[] list = new String[count];
        List list = new Vector();

        for (int i = 0; i < count; i++) {
            XmlElement action = parentNode.getElement(i);
            String s = action.getAttribute("name");
            String stateString = action.getAttribute("state");

            if (state == 0) {
                // before
                if (!stateString.equals("before")) {
                    continue;
                }
            } else {
                // after
                if (!stateString.equals("after")) {
                    continue;
                }
            }

            XmlElement element = MainInterface.pluginManager.getPluginElement(s);

            if (element == null) {
                // this is no external plugin
                // -> just add it to the list
                list.add(s);

                continue;
            }

            String enabled = element.getAttribute("enabled");

            if (enabled == null) {
                enabled = "true";
            }

            boolean e = Boolean.valueOf(enabled).booleanValue();

            if (e) {
                list.add(s);
            }

            //list[i] = s;
        }

        String[] strs = new String[list.size()];

        for (int i = 0; i < list.size(); i++) {
            strs[i] = (String) list.get(i);
        }

        //return list;
        return strs;
    }
}
