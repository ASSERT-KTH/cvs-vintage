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
package org.columba.mail.gui.table.model;

import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.HeaderList;

import java.util.Enumeration;
import java.util.Map;

/**
 * @author fdietz
 * 
 * Extends <class>BasicTableModelFilter </class> with Columba specific features.
 * 
 * It especially implements <interface>TableModelModifier </interface>.
 *  
 */
public class TableModelFilter extends BasicTableModelFilter {

    public TableModelFilter(TreeTableModelInterface tableModel) {
        super(tableModel);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.columba.mail.gui.table.model.TableModelModifier#add(org.columba.mail.message.HeaderInterface[])
     */

    /**
     * ***************************** implements TableModelModifier
     * ******************
     */


    public void update() {
        if (isEnabled()) {
            HeaderList headerList = getHeaderList();
            MessageNode rootNode = getRootNode();
            Map map = getMap();

            // remove all children from tree
            rootNode.removeAllChildren();

            // clear messagenode cache
            map.clear();

            ColumbaHeader header;

            for (Enumeration e = headerList.keys(); e.hasMoreElements();) {
                Object uid = e.nextElement();
                header = (ColumbaHeader) headerList.get(uid);

                if (addItem(header)) {
                    MessageNode childNode = new MessageNode(header, uid);
                    rootNode.add(childNode);
                    map.put(uid, childNode);
                }
            }
        } else {
            // do not filter anything
            super.update();
        }
    }

  
}