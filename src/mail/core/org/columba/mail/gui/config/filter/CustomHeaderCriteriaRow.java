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

public class CustomHeaderCriteriaRow extends DefaultCriteriaRow
{

    private JComboBox matchComboBox;
    private JTextField patternTextField;
    private JTextField headerTextField;


    public CustomHeaderCriteriaRow( CriteriaList criteriaList, FilterCriteria c )
    {
        super( criteriaList, c );
    }

    protected void updateComponents( boolean b )
    {
        super.updateComponents(b);


        if ( b )
        {
            matchComboBox.setSelectedItem( criteria.getCriteriaString() );
            patternTextField.setText( criteria.getPattern() );
            headerTextField.setText( criteria.getHeaderItemString() );
        }
        else
        {
            criteria.setCriteria( (String) matchComboBox.getSelectedItem() );
            criteria.setPattern( (String) patternTextField.getText() );
            criteria.setHeaderItem( (String) headerTextField.getText() );
        }


    }


    public void initComponents()
    {
        super.initComponents();

        headerTextField = new JTextField("header", 12);
        c.gridx = 1;
        gridbag.setConstraints( headerTextField, c);
        add( headerTextField );

	matchComboBox = new JComboBox();
	matchComboBox.addItem( "contains" );
	matchComboBox.addItem( "contains not" );
	matchComboBox.addItem( "is" );
	matchComboBox.addItem( "is not" );
	matchComboBox.addItem( "begins with" );
	matchComboBox.addItem( "ends with" );
        c.gridx = 2;
        gridbag.setConstraints(matchComboBox, c);
        add( matchComboBox );


        patternTextField = new JTextField("pattern", 12);
        c.gridx = 3;
        gridbag.setConstraints( patternTextField, c);
        add( patternTextField );

        finishRow();

    }


}





