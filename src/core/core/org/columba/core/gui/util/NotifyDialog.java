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

package org.columba.core.gui.util;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;

import org.columba.mail.message.*;
import org.columba.core.gui.util.*;
import org.columba.main.*;

public class NotifyDialog
{

    
    private JDialog dialog;
    private boolean bool = false;
    private MultiLineLabel textField;



    public NotifyDialog(  )
    {
        

    }


	public void showDialog( Exception ex )
	{
		showDialog( ex.getMessage() );
	}

    public void showDialog( String message )
    {
        JLabel topLabel = new JLabel( "An Error occured:",
                                      ImageLoader.getImageIcon("stock_dialog_error_48.png"),
                                      SwingConstants.LEFT
                                      );

        JButton[] buttons = new JButton[1];

        MultiLineLabel textArea = new MultiLineLabel( message );
        textArea.setColumns(40);
        textArea.setRows(4);
        textArea.setLineWrap( true );
        textArea.setWrapStyleWord( true );
        JScrollPane scrollPane = new JScrollPane( textArea );



        buttons[0] = new JButton("Close");
        buttons[0].setActionCommand("CLOSE");
		buttons[0].setDefaultCapable(true);



        GridBagLayout layout = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        dialog = DialogStore.getDialog("Error occured...");
        dialog.getContentPane().setLayout( layout );
		dialog.getRootPane().setDefaultButton(buttons[0]);

        c.gridx=0;
        c.gridy=0;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(10,10,0,20);
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints( topLabel, c);


        c.gridx=0;
        c.gridy=1;
        c.gridwidth = 1;
        c.weightx = 1;
        c.insets = new Insets(10,20,10,15);
        c.anchor = GridBagConstraints.WEST;
        layout.setConstraints( scrollPane, c);



        JPanel panel = new JPanel();
        //panel.add( buttons[1] );
        panel.add( buttons[0] );

        c.gridx=0;
        c.gridy=5;
        c.weightx = 1.0;
        //c.gridwidth = GridBagConstraints.REMAINDER;
        c.gridwidth = 1;
        c.insets = new Insets(5,10,10,10);
        c.anchor = GridBagConstraints.SOUTHEAST;
        layout.setConstraints(panel, c);

        dialog.getContentPane().add ( scrollPane );
        dialog.getContentPane().add( topLabel );
        dialog.getContentPane().add( panel );
        //dialog.getContentPane().add( buttons[1] );

        dialog.pack();


        java.awt.Dimension dim = dialog.getPreferredSize();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

        dialog.setLocation(screenSize.width/2 - dim.width/2, screenSize.height/2 - dim.height/2);


        buttons[0].addActionListener(
                new ActionListener()
                    {
                        public void actionPerformed(ActionEvent e)
                        {
                            String action = e.getActionCommand();

                            if  ( action.equals("CLOSE") )
                                {

                                    bool = true;

                                    dialog.dispose();
                                }


                        }
                    }
                    );





        dialog.show();

    }


    public boolean success()
    {
        return bool;

    }



}
