/*
 * Created on Jun 12, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.frame;

import org.columba.mail.gui.table.TableController;


/**
 * FrameControllers having a table component should implement
 * this interface.
 * <p>
 * The reason for this interface are simple. We have many kinds of
 * frame controllers. Some have a table component, some of them don't.
 * <p>
 * To recognize we can just test it:
 * <pre>
 *  if ( frameMediator instanceof TableOwner )
 *   {
 *      TableController table = ( (TableOwner) frameMediator).getTableController()
 *      // do something here
 *   }
 * </pre>
 *
 * @author fdietz
 */
public interface TableViewOwner {
    public abstract TableController getTableController();
}
