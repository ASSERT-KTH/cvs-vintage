/*
 * Created on 31.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.model;

import java.util.Map;

import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeModel;

import org.columba.mail.message.HeaderList;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface TreeTableModelInterface extends TableModel {

	public void set(HeaderList list);
	public void remove(Object[] uids);
	public void modify(Object[] uids);
	public void update();

	public Map getMap();
	public HeaderList getHeaderList();
	public MessageNode getRootNode();
	public int getColumnNumber(String s);
	public DefaultTreeModel getTreeModel();
	public void fireTableDataChanged();
}
