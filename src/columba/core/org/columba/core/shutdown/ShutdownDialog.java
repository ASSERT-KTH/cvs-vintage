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
package org.columba.core.shutdown;

import java.awt.BorderLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.core.gui.util.ImageLoader;
import org.columba.core.util.GlobalResourceLoader;

/**
 * Dialog shown while closing Columba.
 *
 * @author fdietz
 */
public class ShutdownDialog extends JFrame {

	protected static final String RESOURCE_PATH= "org.columba.core.i18n.dialog";

	public ShutdownDialog() {
		super(
			GlobalResourceLoader.getString(
				RESOURCE_PATH,
				"session",
				"exit_title"));

		JLabel icon= new JLabel();
		icon.setIcon(ImageLoader.getImageIcon("out-of-office-48.png"));

		JLabel text=
			new JLabel(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"session",
					"exit_msg"));
		text.setFont( text.getFont().deriveFont(Font.BOLD));
		JPanel panel= new JPanel();
		
		panel.setLayout(new BorderLayout());

		icon.setBorder(BorderFactory.createEmptyBorder(12, 12, 6, 12));
		text.setBorder(BorderFactory.createEmptyBorder(0, 6, 12, 12));
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));
				
		JPanel borderPanel = new JPanel();
		borderPanel.setBorder(BorderFactory.createEmptyBorder(3,0,0,0));
		bottomPanel.add( borderPanel, BorderLayout.EAST);
		getContentPane().setLayout(new BorderLayout());

		getContentPane().add(panel, BorderLayout.CENTER);
		
		getContentPane().add( icon, BorderLayout.WEST);
		
		getContentPane().add( bottomPanel, BorderLayout.SOUTH);
		
		panel.add(text, BorderLayout.SOUTH);

		pack();
		//setSize(new Dimension(300, 50));

		setLocationRelativeTo(null);
	}
}
