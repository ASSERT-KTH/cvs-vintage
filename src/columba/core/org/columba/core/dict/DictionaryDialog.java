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
package org.columba.core.dict;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

public class DictionaryDialog extends JDialog implements ActionListener
{
	private SimpleAttributeSet[] attr = new SimpleAttributeSet[2];
	JTextPane tp;

	public DictionaryDialog(JFrame frame, String answer)
	{
		super(frame, true);
		setTitle("Online dictionary lookup results...");

		JPanel contentPane = new JPanel(new BorderLayout(0,17));
		contentPane.setBorder(BorderFactory.createEmptyBorder(12,12,11,11));
		tp = new JTextPane();
		tp.setText(answer.toString());
		tp.setPreferredSize( new Dimension(640,480) );
		tp.setCaretPosition(0);
		//colorResult();

		JScrollPane pane = new JScrollPane(tp);
		contentPane.add(pane, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		JPanel buttonPanel = new JPanel(new GridLayout(1,1,5,0));
		JButton closeButton = new JButton("Close");
		closeButton.setActionCommand("CLOSE");
		closeButton.addActionListener(this);
		buttonPanel.add(closeButton);
		bottomPanel.add(buttonPanel,BorderLayout.EAST);
		contentPane.add(bottomPanel,BorderLayout.SOUTH);
		setContentPane(contentPane);
		getRootPane().setDefaultButton(closeButton);
		getRootPane().registerKeyboardAction(this,"CLOSE",KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);

		addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				setVisible(false);
			}
		});

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	int nextResult(String str, int start)
	{
		int pos = start;

		char c;

		if ( pos == str.length() ) return -1;
		
		c = str.charAt(pos);
		if (c == '1')
		{
			pos++;
			c = str.charAt(pos);
			if (c == '5')
			{
				pos++;
				c = str.charAt(pos);
				if (c == '1')
				{
					return pos + 1;
				}
			}

		}

		return -1;
	}

	public void colorResult()
	{
		init();

		String str = null;
		try
		{
			str = tp.getStyledDocument().getText(0, tp.getStyledDocument().getLength());
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		setStyle(0, str.length(), 0);
		
		int pos = 0;

		char c;

		while (pos < str.length())
		{
			c = str.charAt(pos);
			//System.out.println("pos1:" + pos + " :" + c);

			if (c == '\n')
			{
				pos++;
				int nextResult = nextResult(str, pos);
				

				if (nextResult != -1)
				{
					//System.out.println("found result");
					
					pos = nextResult;
					while ((c != '\n') && (pos < str.length()))
					{
						c = str.charAt(pos);
						pos++;

						//System.out.println("pos2:" + pos + " :" + c);
					}

					setStyle(nextResult, pos, 1);
				}
			}

			pos++;
		}

	}

	protected void init()
	{
		for (int i = 0; i < 2; i++)
		{
			attr[i] = new SimpleAttributeSet();

		}

		StyleConstants.setForeground(attr[0], Color.black);

		StyleConstants.setBold(attr[1], true);
		StyleConstants.setForeground(attr[1], Color.blue);
	}

	public void setStyle(int pos, int length, int i)
	{
		tp.getStyledDocument().setCharacterAttributes(pos, length, attr[i], true);
	}
	
	public void actionPerformed(ActionEvent e)
	{
		String action = e.getActionCommand();
		if ( action.equals("CLOSE") ) dispose();
	}
}
