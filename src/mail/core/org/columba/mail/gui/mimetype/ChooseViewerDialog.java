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

package org.columba.mail.gui.mimetype;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class ChooseViewerDialog extends JDialog implements ActionListener
{
    private JTextField viewerName;
    private String viewer=null;
    private JCheckBox saveCButton;

    public ChooseViewerDialog( Frame owner, String contentType, String contentSubtype, boolean save )
    {
	//LOCALIZE
        super(owner,"Select Viewer for "+contentType+"/"+contentSubtype,true);
        init(save);
	setSize(300,150);
	setLocationRelativeTo(null);
        setVisible(true);
    }

    private void init(boolean save)
    {
	JPanel contentPane = new JPanel(new BorderLayout(0,0));
	contentPane.setBorder(BorderFactory.createEmptyBorder(12,12,11,11));
	JPanel viewerPanel = new JPanel();
	viewerPanel.setLayout(new BoxLayout(viewerPanel,BoxLayout.X_AXIS));
	//LOCALIZE
        JLabel label = new JLabel( "Viewer:" );
        viewerPanel.add( label );
	viewerPanel.add(Box.createHorizontalStrut(5));
        viewerName = new JTextField();
        viewerName.setActionCommand("OK");
        viewerName.addActionListener( this );
	label.setLabelFor(viewerName);
        viewerPanel.add( viewerName );
	viewerPanel.add(Box.createHorizontalStrut(10));
        JButton searchButton = new JButton( "..." );
        searchButton.addActionListener(this);
        searchButton.setActionCommand("SEARCH");
        viewerPanel.add( searchButton );
	contentPane.add(viewerPanel,BorderLayout.NORTH);
	//LOCALIZE
        saveCButton = new JCheckBox( "Always open with this Viewer", save );
	saveCButton.setBorder(BorderFactory.createEmptyBorder(10,0,17,0));
        contentPane.add( saveCButton, BorderLayout.CENTER );
	JPanel bottomPanel = new JPanel(new BorderLayout(0,0));
	JPanel buttonPanel = new JPanel(new GridLayout(1,2,5,0));
	//LOCALIZE
        JButton okButton = new JButton( "OK" );
        okButton.addActionListener(this);
        okButton.setActionCommand("OK");
	buttonPanel.add(okButton);
	//LOCALIZE
	JButton cancelButton = new JButton("Cancel");
	cancelButton.setActionCommand("CANCEL");
	cancelButton.addActionListener(this);
	buttonPanel.add(cancelButton);
	bottomPanel.add(buttonPanel,BorderLayout.EAST);
        contentPane.add( bottomPanel, BorderLayout.SOUTH );
	setContentPane(contentPane);
	getRootPane().setDefaultButton(okButton);
	getRootPane().registerKeyboardAction(this,"CANCEL",KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    public String getViewer()
    {
        return viewer;
    }

    public boolean saveViewer()
    {
        return saveCButton.isSelected();
    }

    public void actionPerformed( ActionEvent e )
    {
        String command = e.getActionCommand();
        if( command.equals("SEARCH") ) {
            JFileChooser fileChooser = new JFileChooser();
	    //LOCALIZE
            fileChooser.setDialogTitle("Choose Program");
            int returnVal = fileChooser.showDialog( this, "OK");
            if( returnVal == JFileChooser.APPROVE_OPTION ) {
                viewerName.setText( fileChooser.getSelectedFile().toString() );
            }
        }else if( command.equals("OK") ) {
	    viewer=viewerName.getText();
            dispose();
        }else if( command.equals("CANCEL") ){
	    dispose();
	}
    }
}
