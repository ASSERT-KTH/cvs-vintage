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
package org.columba.core.help;

import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.help.HelpSet;
import javax.help.JHelp;
import javax.help.JHelpContentViewer;
import javax.help.TextHelpModel;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.text.JTextComponent;

/**
 * @author fdietz
 *
 * Frame showing the user manual 
 * 
 */
public class HelpFrame {
	private static String title = "";

	//	The initial width and height of the frame
	public static int WIDTH = 645;
	public static int HEIGHT = 495;

	protected static boolean debug = false;

	private static JHelp jh = null;

	private static JFrame frame;

	public HelpFrame(JHelp jh)  {
		super();

		HelpFrame.jh = jh;
	}

	private JTextComponent getEditor() {
		JHelpContentViewer viewer = jh.getContentViewer();
		JScrollPane sp = (JScrollPane) viewer.getComponent(0);
		JViewport vp = sp.getViewport();
		return (JTextComponent) vp.getView();
	}

	protected static JFrame createFrame(String title, JMenuBar bar) {
		if (jh == null)
			return null;
		if (title == null || title.equals("")) {
			TextHelpModel m = jh.getModel();
			HelpSet hs = m.getHelpSet();
			String hsTitle = hs.getTitle();
			if (hsTitle == null || hsTitle.equals("")) {
				setTitle("Unnamed HelpSet"); // maybe based on HS?
			} else {
				setTitle(hsTitle);
			}
		} else {
			setTitle(title);
		}
		if (frame == null) {

			frame = new JFrame(getTitle());
			frame.setSize(WIDTH, HEIGHT);
			/*
			frame.setForeground(Color.black);
			frame.setBackground(Color.lightGray);
			*/

			//frame.addWindowListener(closer);
			frame.getContentPane().add(jh); // the JH panel
			if (bar == null) {
				bar = createMenuBar();
			}
			frame.setJMenuBar(bar);
		} else {
			frame.setTitle(getTitle());
		}
		frame.pack();

		// maximize frame
		frame.setExtendedState(Frame.MAXIMIZED_BOTH);

		return frame;
	}

	/**
		 * MenuBar
		 */
	private static JMenuBar createMenuBar() {
		// MenuBar
		JMenuBar menuBar = new JMenuBar();

		JMenuItem mi;

		// File Menu
		JMenu file = (JMenu) menuBar.add(new JMenu("File"));
		file.setMnemonic('F');

		mi = (JMenuItem) file.add(new JMenuItem("Exit"));
		mi.setMnemonic('x');
		mi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.setVisible(false);
			}
		});

		// Option Menu
		JMenu options = (JMenu) menuBar.add(new JMenu("Options"));
		options.setMnemonic('O');

		return menuBar;
	}

	public static void setTitle(String s) {
		title = s;
	}

	public static String getTitle() {
		return title;
	}

}
