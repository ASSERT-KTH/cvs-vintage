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
package org.columba.mail.gui.config.mailboximport;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.columba.core.gui.util.CProgressBar;
import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;

/**
 * @version 	1.0
 * @author
 */
public class ProgressPanel extends DefaultWizardPanel
{
	CProgressBar progressBar;
	JButton cancelButton;
	JLabel finishLabel;
	
	
	public ProgressPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon) {
		super(dialog, listener, title, description, icon);
	}
	
	public ProgressPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon,
		boolean b) {
		super(dialog, listener, title, description, icon);
	}
	
	
	
	
	protected JPanel createPanel(ActionListener listener) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		panel.setLayout( new BorderLayout() );
		//panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		JPanel topPanel = new JPanel();
		topPanel.setLayout( new BorderLayout() );
		topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));
		MultiLineLabel label =
			new MultiLineLabel("Press the button to start importing messages.");

		topPanel.add( label, BorderLayout.CENTER );
		
		panel.add( topPanel, BorderLayout.NORTH );
		
		

		//panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

		
		JPanel middlePanel = new JPanel();
		middlePanel.setAlignmentX(1);
		GridBagLayout layout = new GridBagLayout();
		middlePanel.setLayout( layout );
		
	
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;
		c.gridx=0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weightx = 1.0;
		progressBar = new CProgressBar();
		
		layout.setConstraints( progressBar, c );
		middlePanel.add( progressBar );
		
		cancelButton = new JButton("Start");
		cancelButton.setActionCommand("START_STOP");
		cancelButton.addActionListener( listener );
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		c.weightx = 0.0;
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(0,10,0,0);
		layout.setConstraints( cancelButton, c );
		middlePanel.add( cancelButton );
		
		c.gridx = 0;
		c.anchor = GridBagConstraints.WEST;
		c.insets = new Insets(20,0,0,0);
		finishLabel = new JLabel("This may take a while...");
		layout.setConstraints(finishLabel, c );
		middlePanel.add( finishLabel );

		panel.add(middlePanel, BorderLayout.CENTER );
		
		
		

		panel.add(middlePanel, BorderLayout.CENTER);

		return panel;
	}
	
	public void start()
	{
		progressBar.setIndeterminate(true);
	}
	
	public void stop()
	{
		progressBar.setIndeterminate(false);
	}
	
	public void setButtonText( String str )
	{
		cancelButton.setText(str);
	}
	
	public void setFinished( boolean b )
	{
		if ( b == true )
		{
			setButtonText("Finish");
			cancelButton.setEnabled(false);
			finishLabel.setText("Mailbox import finished successfully!");			
			stop();
		}		
	}
	
}
