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

package org.columba.mail.gui.header;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.columba.mail.gui.header.util.HeaderFieldTree;
import org.columba.mail.gui.message.SecurityIndicator;
import org.columba.mail.gui.util.AddressLabel;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.util.MailResourceLoader;

public class HeaderView extends JPanel implements ActionListener
{
	//GridBagLayout gridLayout;
	JComponent[] values;
	String[] keys;
	int count;
	Font font;

	int rows;

	JPanel mainPanel;

	public SecurityIndicator securityIndicator;

	public HeaderView(String[] keys, int rows )
	{
		this.keys = keys;
		this.rows = rows;
		securityIndicator = new SecurityIndicator();

		//setBorder( BorderFactory.createEmptyBorder( 0,0,5,0 ) );
		//setBorder(UIManager.getBorder("HeaderView.border"));
		
		Border border = BorderFactory.createEtchedBorder();
		Border margin = new EmptyBorder(2, 2, 2, 2);
		setBorder( new CompoundBorder(border,margin) );
		
		setLayout(new BorderLayout());

		add(securityIndicator, BorderLayout.EAST);

		mainPanel = new JPanel();

		/*
		values = new JComponent[ 4 ];
		for ( int i=0; i<count; i++ )
		{
		    values[i] = new JLabel("");
		}

		initComponents();
		*/		

	}

	public void setSecurityValue(int value)
	{
		securityIndicator.setValue(value);
	}

	protected boolean isListHeaderField(String s)
	{
		boolean result = false;
		final String[] fields = { "To", "Cc", "From" };

		for (int i = 0; i < fields.length; i++)
		{
			String key = fields[i];

			if (key.equalsIgnoreCase(s))
			{
				result = true;
			}
		}

		return result;
	}

	protected boolean between(String s, int index)
	{
		boolean result = false;

		int index1 = s.indexOf("\"", index);
		int index2 = s.indexOf("<", index);

		if ((index1 != -1) && (index2 != -1))
		{
			if (index1 < index2)
			{
				// this @ is between ""
				result = true;
			}
		}

		return result;
	}

	protected boolean isList(String s, Object o)
	{
		String item = (String) o;
		boolean result = false;

		int index = item.indexOf("@");

		if (index != -1)
		{
			// found first address

			int index2 = item.indexOf("@", index + 1);

			if (index2 != -1)
			{

				// found second address
				int index3 = item.indexOf(",", index + 1);

				if ((index3 < index2) && (index3 > index))
					result = true;

			}

		}

		return result;
	}

	public void setHeader(HeaderInterface header)
	{
		if (header == null)
			return;

		

		values = new JComponent[keys.length];

		for (int i = 0; i < keys.length; i++)
		{
			String key = keys[i];
			Object item = header.get(key);
			if (item == null)
			{
				item = new String();
			}

			if (isListHeaderField(key) == true)
			{
				if (isList(key, item) == true)
				{
					//System.out.println("headerfieldtree");
					values[i] = new HeaderFieldTree((String) item);
				}
				else
				{
					//System.out.println("addresslabel");
					if (item instanceof String)
					{
						values[i] = new AddressLabel( (String) item );
					}
					else
					{
						values[i] = new AddressLabel( item.toString() );
					}
				}

			}
			else
			{
				if (item instanceof String)
				{
					values[i] = new JLabel((String) item);
				}
				else
				{
					values[i] = new JLabel(item.toString());
				}

			}

		}

		initComponents();

		//add( securityIndicator, BorderLayout.EAST );

		revalidate();

		repaint();

	}

	protected void initComponents()
	{
		mainPanel.removeAll();

		font = UIManager.getFont("Label.font");

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.X_AXIS));

		count = keys.length;

		int columnCount = count / rows;

		int lastColumn = -1;
		for (int i = 0; i < count; i++)
		{
			JPanel p = null;

			if (((i + 1) % rows) == 0)
			{

				p = createHeaderPanel(i, rows);

				mainPanel.add(p);

				mainPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));

				lastColumn = i + 1;
			}
			else if (i + 1 == count)
			{
				p = createHeaderPanel(i, count - lastColumn);

				mainPanel.add(p);

				mainPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));

			}

		}

		add(mainPanel, BorderLayout.CENTER);

	}

	public void resetRenderer()
	{
		initComponents();

	}

	public void updateUI()
	{
		super.updateUI();

		font = UIManager.getFont("Label.font");

		/*
		Border border = UIManager.getBorder("HeaderView.border");
		if (border != null)
			setBorder(border);
		*/
	}

	private JPanel createHeaderPanel(int index, int row)
	{
		JPanel panel = new JPanel();

		GridBagLayout layout = new GridBagLayout();
		panel.setLayout(layout);
		GridBagConstraints constraints = new GridBagConstraints();
		for (int i = index - row + 1; i < index + 1; i++)
		{

			constraints.insets = new Insets(0, 2, 0, 0);

			JLabel label;

			String str = keys[i];
			str = str.toLowerCase();

			label = new JLabel(MailResourceLoader.getString("header", str));
			Font newFont = font.deriveFont(Font.BOLD);
			label.setFont(newFont);
			panel.add(label);

			constraints.ipadx = 5;
			constraints.anchor = GridBagConstraints.NORTHWEST;
			constraints.fill = GridBagConstraints.NONE;
			constraints.gridwidth = 1; //next to Last
			constraints.weightx = 0.0;
			layout.setConstraints(label, constraints);

			label = new JLabel();
			label.setText(":");
			constraints.insets = new Insets(0, 0, 0, 0);
			constraints.gridwidth = GridBagConstraints.RELATIVE;
			constraints.weightx = 0.0;
			layout.setConstraints(label, constraints);
			panel.add(label);

			constraints.insets = new Insets(0, 0, 0, 10);
			constraints.gridwidth = GridBagConstraints.REMAINDER;
			constraints.weightx = 1.0;
			layout.setConstraints(values[i], constraints);
			panel.add(values[i]);

		}

		return panel;
	}

	public void actionPerformed(ActionEvent e)
	{
		Object source = e.getSource();

		System.out.println("action performed");
	}
}