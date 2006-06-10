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
package org.columba.mail.gui.config.columns;

import javax.swing.Icon;

import org.columba.core.xml.XmlElement;
import org.frapuccino.checkablelist.CheckableItem;


/**
 * @author fdietz
 */
public class ColumnItem implements CheckableItem {
    private XmlElement element;

    public ColumnItem(XmlElement element) {
        this.element = element;
    }

    public void setSelected(boolean b) {
        element.addAttribute("enabled", Boolean.toString(b));
    }

    /**
 * @see org.columba.core.gui.checkablelist.CheckableItem#isSelected()
 */
    public boolean isSelected() {
        boolean b = Boolean.valueOf(element.getAttribute("enabled"))
                           .booleanValue();

        return b;
    }

    /**
  * @see org.columba.core.gui.checkablelist.CheckableItem#getIcon()
  */
    public Icon getIcon() {
        return null;
    }

    public String toString() {
        return element.getAttribute("name");
    }

    /**
 * @return
 */
    public XmlElement getElement() {
        return element;
    }
}
