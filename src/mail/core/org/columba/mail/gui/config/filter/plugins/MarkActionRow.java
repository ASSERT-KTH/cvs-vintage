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


package org.columba.mail.gui.config.filter.plugins;

import org.columba.mail.filter.FilterAction;
import org.columba.mail.gui.config.filter.ActionList;

public class MarkActionRow extends DefaultActionRow
{

	
    public MarkActionRow( ActionList list, FilterAction action )
    {
        super( list, action );

    }
	
	
    public void updateComponents( boolean b)
    {
        super.updateComponents( b );


    }


    public void initComponents()
    {
        super.initComponents();

        /*
        c.gridx = GridBagConstraints.REMAINDER;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        Box box = Box.createHorizontalBox();
        gridbag.setConstraints( box, c );

        add( box );
        */


        //validate();
        //repaint();
    }



}












