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

package org.columba.mail.gui.config.mailboximport;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.mail.util.MailResourceLoader;

/**
 * @version 	1.0
 * @author
 */
public class ListPanel
	extends DefaultWizardPanel
	implements ListSelectionListener
{
	private JList list;
	private JLabel label;
	private MultiLineLabel descriptionLabel;
	
	private String[] importerList;
	private String[] name;
	private String[] description;

	public ListPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon)
	{
		super(dialog, listener, title, description, icon);

	}

	public ListPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon,
		boolean b)
	{
		super(dialog, listener, title, description, icon);
		
	}

	public String getSelection()
	{
		int index = list.getSelectedIndex();
		
		String key = importerList[index];
		
		
		
		return key; 
	}

	protected JPanel createPanel(ActionListener listener)
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		//panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

		MultiLineLabel label =
			new MultiLineLabel("Please choose the mailbox format you want to import to Columba.");

		topPanel.add(label, BorderLayout.CENTER);
		//panel.add(label);

		//panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

		panel.add(topPanel, BorderLayout.NORTH);

		JPanel middlePanel = new JPanel();
		middlePanel.setAlignmentX(1);
		GridBagLayout layout = new GridBagLayout();
		middlePanel.setLayout(layout);

		generateList();
		list = new JList( name );

		JScrollPane scrollPane = new JScrollPane(list);
		//scrollPane.setPreferredSize( new Dimension(200,200) );
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.4;
		//c.gridwidth = GridBagConstraints.RELATIVE;
		c.weighty = 1.0;
		layout.setConstraints(scrollPane, c);
		middlePanel.add(scrollPane);

		descriptionLabel = new MultiLineLabel("description");
		descriptionLabel.setWrapStyleWord(true);
		descriptionLabel.setLineWrap(true);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.6;
		c.gridx = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 10, 0, 0);
		JScrollPane scrollPane2 = new JScrollPane(descriptionLabel);
		//scrollPane2.setPreferredSize( new Dimension(200,100) );
		layout.setConstraints(scrollPane2, c);
		middlePanel.add(scrollPane2);

		panel.add(middlePanel, BorderLayout.CENTER);

		list.addListSelectionListener(this);
		list.setSelectedIndex(0);

		return panel;
	}

	protected void generateList()
	{
		importerList = new String[] { "MBOX",
						"PegasusMail",
						"Mozilla",
						"Evolution"};
		
	

		name = new String[ importerList.length ];
		description = new String[ importerList.length ];
		
		for ( int i=0; i<importerList.length; i++ )
		{
			name[i] = MailResourceLoader.getString("dialog", "mailboximport",importerList[i]+"_name");
			description[i] = MailResourceLoader.getString("dialog","mailboximport", importerList[i]+"_description");
		}
		
		
	}

	public void valueChanged(ListSelectionEvent event)
	{
		
		int index = list.getSelectedIndex();
		
		String str = description[index];
		
		descriptionLabel.setText( str );
		
	}

}
