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

package org.columba.core.dict;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.text.*;

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
