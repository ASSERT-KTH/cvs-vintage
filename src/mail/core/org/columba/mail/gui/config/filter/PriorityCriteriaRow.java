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


package org.columba.mail.gui.config.filter;

import org.columba.mail.config.*;
import org.columba.mail.message.*;
import org.columba.main.*;
import org.columba.mail.filter.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import javax.swing.border.*;

import java.awt.*;
import java.awt.event.*;


import java.util.Vector;

public class PriorityCriteriaRow extends DefaultCriteriaRow
{
    private JComboBox priorityComboBox;
    private JComboBox matchComboBox;
    private JTextField textField;


    public PriorityCriteriaRow( CriteriaList criteriaList, FilterCriteria c )
    {
        super( criteriaList, c );

    }

    protected void updateComponents( boolean b )
    {
        super.updateComponents(b);


        if ( b )
        {
            matchComboBox.setSelectedItem( criteria.getCriteriaString() );
            String priority = criteria.getPattern();
            priorityComboBox.setSelectedItem( priority );
        }
        else
        {
            criteria.setCriteria( (String) matchComboBox.getSelectedItem() );
            criteria.setPattern( (String) priorityComboBox.getSelectedItem() );
        }

    }


    public void initComponents()
    {
        super.initComponents();

	matchComboBox = new JComboBox();
	matchComboBox.addItem( "is" );
	matchComboBox.addItem( "is not" );
        c.gridx = 1;
        gridbag.setConstraints(matchComboBox, c);
        add( matchComboBox );

        priorityComboBox = new JComboBox();
        priorityComboBox.addItem("Highest");
        priorityComboBox.addItem("High");
        priorityComboBox.addItem("Normal");
        priorityComboBox.addItem("Low");
        priorityComboBox.addItem("Lowest");
        c.gridx = 2;
        gridbag.setConstraints( priorityComboBox, c);
        add( priorityComboBox );

        finishRow();
    }


}





