/*
 * Created on Jun 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.frame;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.mail.gui.table.model.TableModelChangedEvent;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class TableUpdater {

	protected static List list = new Vector();

	
	public static void tableChanged(TableModelChangedEvent ev) throws Exception {
		for (Iterator it = list.iterator(); it.hasNext();) {
						AbstractMailFrameController frame = (AbstractMailFrameController) it.next();

			(
				(
					ThreePaneMailFrameController) frame)
						.tableController
						.tableChanged(
				ev);
		}

	}

	public static void add(TableOwnerInterface frameController) {
		list.add(frameController);
	}

}
