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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.*;

import org.columba.mail.gui.util.URLController;

public class ExceptionDialog {

	private JDialog dialog;
	private boolean bool = false;
	private JTextField textField;
	private String stackTrace;

	public ExceptionDialog() {

	}

	public void showDialog(Exception ex) {
		final Exception exception = ex;

		JLabel topLabel =
			new JLabel(
				"An Exception occured. It is possible that you found a bug!",
				ImageLoader.getImageIcon("stock_dialog_error_48.png"),
				SwingConstants.LEFT);

		JButton[] buttons = new JButton[2];
		JLabel label = new JLabel("Exception message:");
		/*
		MultiLineLabel mlLabel = new MultiLineLabel( ex.getMessage() );
		mlLabel.setLineWrap( true );
		mlLabel.setWrapStyleWord( true );
		mlLabel.setColumns(70);
		mlLabel.setRows(3);
		*/

		JTextArea textArea = new JTextArea(ex.getMessage(), 3, 50);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		JScrollPane scrollPane = new JScrollPane(textArea);

		JLabel label2 = new JLabel("Stack Trace:");
		JTextArea textArea2 = new JTextArea(10, 50);
		StringWriter stringWriter = new StringWriter();
		ex.printStackTrace(new PrintWriter(stringWriter));
		stackTrace = stringWriter.toString();
		textArea2.append(stringWriter.toString());
		//textArea2.setLineWrap( true );
		//textArea2.setWrapStyleWord( true );
		JScrollPane scrollPane2 = new JScrollPane(textArea2);

		buttons[0] = new JButton("Close");
		buttons[0].setActionCommand("CLOSE");
		buttons[0].setDefaultCapable(true);

		buttons[1] = new JButton("Send Bugreport");
		buttons[1].setActionCommand("BUG");

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		dialog = DialogStore.getDialog("Exception found");
		dialog.getContentPane().setLayout(layout);
		dialog.getRootPane().setDefaultButton(buttons[0]);

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(10, 10, 0, 10);
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(topLabel, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(10, 10, 0, 10);
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(label, c);

		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(0, 10, 10, 10);
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(scrollPane, c);

		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 1;
		c.weightx = 1;
		c.insets = new Insets(10, 10, 0, 10);
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(label2, c);

		c.gridx = 0;
		c.gridy = 4;
		c.weightx = 1.0;
		c.gridwidth = 1;
		//c.gridwidth = GridBagConstraints.RELATIVE;
		c.insets = new Insets(0, 10, 10, 10);
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(scrollPane2, c);

		JPanel panel = new JPanel();
		panel.add(buttons[1]);
		panel.add(buttons[0]);

		c.gridx = 0;
		c.gridy = 5;
		c.weightx = 1.0;
		//c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridwidth = 1;
		c.insets = new Insets(10, 10, 10, 5);
		c.anchor = GridBagConstraints.SOUTHEAST;
		layout.setConstraints(panel, c);

		/*
		c.gridx=GridBagConstraints.REMAINDER;
		c.gridy=4;
		c.weightx = 1.0;
		//c.gridwidth = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(10,10,10,10);
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(buttons[1], c);
		*/

		dialog.getContentPane().add(label);
		dialog.getContentPane().add(scrollPane);
		dialog.getContentPane().add(label2);
		dialog.getContentPane().add(scrollPane2);
		dialog.getContentPane().add(topLabel);
		dialog.getContentPane().add(panel);
		//dialog.getContentPane().add( buttons[1] );

		dialog.pack();

		dialog.setLocationRelativeTo(null);
		/*
		java.awt.Dimension dim = dialog.getPreferredSize();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		dialog.setLocation(
			screenSize.width / 2 - dim.width / 2,
			screenSize.height / 2 - dim.height / 2);
		*/
		
		for (int i = 0; i < 2; i++) {
			buttons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String action = e.getActionCommand();

					if (action.equals("CLOSE")) {

						bool = true;

						dialog.dispose();
					} else if (action.equals("TRACE")) {
						bool = false;

						//dialog.dispose();
					} else if (action.equals("BUG")) {
						bool = false;

						URLController c = new URLController();
						try{
							c.open(new URL("http://www.sourceforge.net/projects/columba/bugs"));
						}catch(MalformedURLException mue){}
						/*
						dialog.dispose();
						
						
						ComposerFrame composer = new ComposerFrame();
						
						composer.setSubject("bug report:");
						
						composer.setTo("columba-bugs@lists.sourceforge.net");
						
						StringBuffer buf = new StringBuffer();
						StringWriter stringWriter2 = new StringWriter();
						exception.printStackTrace(
							new PrintWriter(stringWriter2));
						String str = new String(stringWriter2.toString());
						buf.append("\nTrace:\n");
						buf.append(str);
						buf.append("\n\n");
						buf.append(
							"Columba version: "
								+ org.columba.main.MainInterface.version);
						buf.append("\n");
						buf.append(
							"JDK version: "
								+ System.getProperty("java.version"));
						buf.append("\n");
						buf.append(
							"JDK vendor: " + System.getProperty("java.vendor"));
						buf.append("\n");
						buf.append(
							"OS version: " + System.getProperty("os.name"));
						
						composer.setBodyText(buf.toString());
						
						//dialog.dispose();
						*/
					}

				}
			});
		}

		dialog.show();

	}

	public boolean success() {
		return bool;

	}

}