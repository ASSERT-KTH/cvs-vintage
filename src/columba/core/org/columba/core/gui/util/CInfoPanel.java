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
package org.columba.core.gui.util;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.UIManager;

public class CInfoPanel extends JPanel {
	protected JPanel panel;
	protected GridBagLayout gridbagLayout;
	protected GridBagConstraints gridbagConstraints;

	protected Font font;

	public CInfoPanel() {
		super();

		font = UIManager.getFont("Label.font");

		/*
		font = font.deriveFont(Font.BOLD);

		
		int size = font.getSize();
		font = font.deriveFont( Font.BOLD, size+2 );
		*/

		setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

		setLayout(new BorderLayout());

		panel = new JPanel();

		add(panel, BorderLayout.CENTER);

		initComponents();
	}

	public void initComponents() {
		panel.removeAll();

		gridbagLayout = new GridBagLayout();
		panel.setLayout(gridbagLayout);

		gridbagConstraints = new GridBagConstraints();
		

		panel.setLayout(gridbagLayout);

		panel.setBackground(UIManager.getColor("controlShadow"));
	}
	
	public void updateUI()
	{
		super.updateUI();
		
		if ( panel != null )
			panel.setBackground(UIManager.getColor("controlShadow"));
			
		revalidate();
		
	}
}
