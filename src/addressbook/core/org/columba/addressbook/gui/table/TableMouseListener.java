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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//Portions created by Celso Pinto are Copyright (C) 2004.
//
//All Rights Reserved.

package org.columba.addressbook.gui.table;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.columba.addressbook.folder.AbstractFolder;
import org.columba.addressbook.folder.ContactStorage;
import org.columba.addressbook.folder.GroupFolder;
import org.columba.addressbook.gui.dialog.contact.ContactDialog;
import org.columba.addressbook.gui.frame.AddressbookFrameMediator;
import org.columba.addressbook.model.Contact;
import org.columba.core.gui.util.DoubleClickListener;
import org.columba.core.gui.util.ErrorDialog;
import org.columba.core.main.MainInterface;


/**
 * @author Celso Pinto &lt;cpinto@yimports.com&gt;
 */
public class TableMouseListener extends DoubleClickListener
{
	private TableController controller = null;
	private AddressbookFrameMediator mediator = null;
	public TableMouseListener(TableController tableController)
	{
		controller = tableController;  
		mediator = controller.getMediator();
	}

  public void doubleClick(MouseEvent e)
  {
	  /*
	   * does exactly the same thing as EditPropertiesAction when contact
	   * table is focused
	   * */
	  if (e.getButton()==MouseEvent.BUTTON1 &&
	      e.getClickCount() > 1)
	  {
	    
			// get selected contact/group card
      Object[] uids = mediator.getTable().getUids();

      // get selected folder
      ContactStorage folder = (ContactStorage) mediator.getTree()
                                                       .getSelectedFolder();

      if (uids.length == 0)
        return;

      Contact card = null;
      try
      {
        card = (Contact) folder.get(uids[0]);
      }
      catch (Exception ex)
      {

        if (MainInterface.DEBUG)
          ex.printStackTrace();

        new ErrorDialog(ex.getMessage(), ex);
      }

      ContactDialog dialog = new ContactDialog(mediator.getView().getFrame(),
                                               card);

      if (dialog.getResult())
      {

        try
        {
          // modify card properties in folder
          folder.modify(uids[0], card);
        }
        catch (Exception e1)
        {
          if (MainInterface.DEBUG)
            e1.printStackTrace();

          new ErrorDialog(e1.getMessage(), e1);
        }

        if (folder instanceof GroupFolder)
          //					 re-select folder
          mediator.getTree().setSelectedFolder((AbstractFolder) folder);

			}
	  	
	  }
  }
}
