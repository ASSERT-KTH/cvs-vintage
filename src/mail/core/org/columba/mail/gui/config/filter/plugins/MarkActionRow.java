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












