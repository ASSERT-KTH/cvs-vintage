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
package org.columba.mail.gui.table.model;

import java.util.Map;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;

import org.columba.mail.message.IHeaderList;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface TreeTableModelInterface extends TableModel {
    public void set(IHeaderList list);

    public void remove(Object[] uids);

    public void modify(Object[] uids);

    public void update();

    public Map getMap();

    public IHeaderList getHeaderList();

    public MessageNode getRootNode();

    public int getColumnNumber(String s);

    public DefaultTreeModel getTreeModel();

    public void fireTableDataChanged();
}
