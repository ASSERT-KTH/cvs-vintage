// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.


package org.columba.addressbook.gui.toolbar;


import java.util.ResourceBundle;

import javax.swing.JToolBar;

import org.columba.addressbook.main.AddressbookInterface;
import org.columba.core.gui.util.ToolbarButton;



public class AddressbookToolBar extends JToolBar
{
    private AddressbookInterface addressbookInterface;

    private ResourceBundle toolbarLabels;

    public AddressbookToolBar( AddressbookInterface addressbookInterface )
        {
            super();
            this.addressbookInterface = addressbookInterface;




            addCButtons();

	    putClientProperty("JToolBar.isRollover",Boolean.TRUE);

            setFloatable(false);
        }

    public void addButton( ToolbarButton button )
    {
        add( button  );
        button.setRolloverEnabled( true );
          //button.setBorder( new ColumbaButtonBorder() );
    }

    public void addCButtons()
        {
        	


	    //MouseAdapter handler = MainInterface.statusBar.getHandler();
	    ToolbarButton button;

	    addSeparator();

		
	    button = new ToolbarButton( addressbookInterface.actionListener.addContactAction);
	    addButton( button  );

            button = new ToolbarButton( addressbookInterface.actionListener.addGroupAction);
	    addButton( button  );

            addSeparator();

            button = new ToolbarButton( addressbookInterface.actionListener.propertiesAction);
	    addButton( button  );

            button = new ToolbarButton( addressbookInterface.actionListener.removeAction);
	    addButton( button  );

            addSeparator();

		
        }


}
