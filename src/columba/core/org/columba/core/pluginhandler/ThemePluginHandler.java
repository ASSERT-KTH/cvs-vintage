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

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.xml.XmlElement;

import javax.swing.UIManager;


/**
 * Look and Feel plugin handler.
 * <p>
 *
 * @author fdietz
 */
public class ThemePluginHandler extends AbstractPluginHandler {
    public static String[] SYSTEM_DEFAULT_THEMES = {
        "Windows", "CDE/Motif", "Gtk", "Mac"
    };

    /**
 * @param id
 * @param config
 */
    public ThemePluginHandler() {
        super("org.columba.core.theme", "org/columba/core/plugin/theme.xml");

        parentNode = getConfig().getRoot().getElement("themelist");

        // remove all Look and Feels from the list
        // which aren't supported by this system
        UIManager.LookAndFeelInfo[] list = UIManager.getInstalledLookAndFeels();

        for (int i = 0; i < parentNode.count(); i++) {
            XmlElement child = parentNode.getElement(i);
            String name = child.getAttribute("name");

            boolean isSupported = false;

            if (isSystemTheme(name)) {
                // this is a system installed look and feel
                // -> check if os supports it
                for (int j = 0; j < list.length; j++) {
                    String s = list[j].getName();

                    if (s.equals(name)) {
                        isSupported = true;

                        break;
                    }
                }

                if (!isSupported) {
                    child.removeFromParent();
                }
            }
        }
    }

    /**
 *
 * is this a installed system look and feel ?
 *
 * @param name
 *
 * @return
 */
    protected boolean isSystemTheme(String name) {
        for (int i = 0; i < SYSTEM_DEFAULT_THEMES.length; i++) {
            if (name.equals(SYSTEM_DEFAULT_THEMES[i])) {
                return true;
            }
        }

        return false;
    }
}
