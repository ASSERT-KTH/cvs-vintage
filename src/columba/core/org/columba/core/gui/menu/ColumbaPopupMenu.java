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
package org.columba.core.gui.menu;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.xml.XmlElement;

import javax.swing.JPopupMenu;


/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ColumbaPopupMenu extends JPopupMenu {
    protected PopupMenuGenerator menuGenerator;

    /**
 *
 */
    public ColumbaPopupMenu(FrameMediator frameController, String path) {
        super();

        menuGenerator = createPopupMenuGeneratorInstance(path, frameController);

        menuGenerator.createPopupMenu(this);
    }

    public PopupMenuGenerator createPopupMenuGeneratorInstance(String xmlRoot,
        FrameMediator frameController) {
        if (menuGenerator == null) {
            menuGenerator = new PopupMenuGenerator(frameController, xmlRoot);
        }

        return menuGenerator;
    }

    public void extendMenuFromFile(String path) {
        menuGenerator.extendMenuFromFile(path);
        menuGenerator.createPopupMenu(this);
    }

    public void extendMenu(XmlElement menuExtension) {
        menuGenerator.extendMenu(menuExtension);
    }
}
