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

package org.columba.addressbook.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;
import java.util.Vector;

import org.columba.mail.config.*;
import org.columba.mail.gui.util.*;
import org.columba.core.config.*;
import org.columba.core.gui.util.*;
import org.columba.addressbook.config.*;
import org.columba.addressbook.gui.util.*;
import org.columba.addressbook.main.*;
import org.columba.addressbook.gui.table.util.*;
import org.columba.addressbook.gui.table.*;
import org.columba.addressbook.gui.tree.util.*;

import org.columba.addressbook.util.*;
//import org.columba.modules.addressbook.gui.tree.util.*;

import org.columba.addressbook.folder.*;

public class EditGroupDialog extends JDialog implements ActionListener
{
	private JDialog dialog;

	private AddressbookXmlConfig config;
	//private AddressbookTable addressbook;
	private AddressbookDNDListView addressbook;
	//private DefaultListModel addressbookModel;

	private AddressbookDNDListView list;
	private JButton addButton;
	private JButton removeButton;
	private JLabel nameLabel;
	private JTextField nameTextField;
	private JLabel descriptionLabel;
	private JTextField descriptionTextField;
	//private AdapterNode groupNode;

	private AddressbookListModel members;

	private JButton cancelButton;
	private JButton okButton;
	private AddressbookInterface addressbookInterface;

	private AddressbookListRenderer renderer;
	
	boolean result;

	public EditGroupDialog(
		JFrame frame,
		AddressbookInterface i,
		AddressbookXmlConfig config,
		AdapterNode groupNode)
	{
		super(frame, true);

		this.addressbookInterface = i;

		this.config = config;
		//this.groupNode = groupNode;

		result = false;

		renderer = new AddressbookListRenderer();
		//set title
		init();
	}

	protected void init()
	{
		getContentPane().setLayout(new BorderLayout());
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		/*
		mainPanel.setBorder(BorderFactory.createTitledBorder(" Group " ) );
		Border b = mainPanel.getBorder();
		*/
		Border b = BorderFactory.createEmptyBorder(12, 12, 10, 11);
		mainPanel.setBorder(b);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BorderLayout());

		LabelTextFieldPanel infoPanel = new LabelTextFieldPanel();
		infoPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				" Description "));
		Border border = infoPanel.getBorder();
		Border margin = BorderFactory.createEmptyBorder(5, 10, 10, 10);
		infoPanel.setBorder(new CompoundBorder(border, margin));

		nameLabel = new JLabel("Name:");
		nameTextField = new JTextField();
		infoPanel.addLabel(nameLabel);
		infoPanel.addTextField(nameTextField);

		descriptionLabel = new JLabel("Description:");
		descriptionTextField = new JTextField();
		infoPanel.addLabel(descriptionLabel);
		infoPanel.addTextField(descriptionTextField);

		leftPanel.add(infoPanel, BorderLayout.NORTH);

		/*
		JPanel namePanel = new JPanel();
		namePanel.setLayout(new BorderLayout());
		namePanel.setBorder( BorderFactory.createEmptyBorder(0, 0, 10, 0));
		
		nameLabel = new JLabel("Name: ");
		namePanel.add(nameLabel, BorderLayout.WEST);
		
		nameTextField = new JTextField();
		
		namePanel.add(nameTextField, BorderLayout.CENTER);
		*/

		leftPanel.add(infoPanel, BorderLayout.NORTH);

		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BorderLayout());
		listPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				" Members "));
		border = listPanel.getBorder();
		margin = BorderFactory.createEmptyBorder(5, 10, 10, 10);
		listPanel.setBorder(new CompoundBorder(border, margin));

		members = new AddressbookListModel();

		list = new AddressbookDNDListView(members);
		//list = new JList(members);
		//list.setCellRenderer(renderer);
		JScrollPane toPane = new JScrollPane(list);
		toPane.setPreferredSize(new Dimension(250, 200));
		listPanel.add(toPane, BorderLayout.CENTER);

		leftPanel.add(listPanel, BorderLayout.CENTER);

		panel.add(leftPanel, BorderLayout.CENTER);

		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));
		middlePanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		middlePanel.add(Box.createVerticalGlue());

		addButton = new JButton("<-");
		addButton.addActionListener(this);
		addButton.setActionCommand("ADD");
		middlePanel.add(addButton);
		middlePanel.add(Box.createRigidArea(new Dimension(0, 5)));
		removeButton = new JButton("->");
		removeButton.addActionListener(this);
		removeButton.setActionCommand("REMOVE");
		middlePanel.add(removeButton);

		middlePanel.add(Box.createVerticalGlue());

		panel.add(middlePanel, BorderLayout.EAST);

		mainPanel.add(panel, BorderLayout.WEST);

		JPanel rightPanel = new JPanel();
		rightPanel.setLayout(new BorderLayout());
		rightPanel.setBorder(
			BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				" Addressbook "));
		border = rightPanel.getBorder();
		margin = BorderFactory.createEmptyBorder(5, 10, 10, 10);
		rightPanel.setBorder(new CompoundBorder(border, margin));

		/*
		addressbook = new AddressbookTable(addressbookInterface);
		HeaderColumn c =
			new HeaderColumn(
				GlobalResourceLoader.getString("header", "displayname"),
				"displayname");
		addressbook.setupColumn(c);
		String prefix = GlobalResourceLoader.getString("header", "email");
		String suffix = GlobalResourceLoader.getString("header", "internet");
		c = new HeaderColumn(prefix + "(" + suffix + ")", "email;internet");
		addressbook.setupColumn(c);

		addressbook.scrollPane.setPreferredSize(new Dimension(250, 200));
		*/
		addressbook = new AddressbookDNDListView();
		addressbook.setAcceptDrop(false);
		JScrollPane scrollPane = new JScrollPane( addressbook );
		rightPanel.add(scrollPane, BorderLayout.CENTER);

		mainPanel.add(rightPanel, BorderLayout.CENTER);

		getContentPane().add(mainPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new CompoundBorder(
					new WizardTopBorder(),
					BorderFactory.createEmptyBorder(17, 12, 11, 11)));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 10));
		okButton = new JButton("Ok");
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);
		cancelButton = new JButton("Cancel");
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		bottomPanel.add(buttonPanel, BorderLayout.EAST);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
		getRootPane().setDefaultButton(okButton);
		getRootPane().registerKeyboardAction(this,"CANCEL",KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE,0),JComponent.WHEN_IN_FOCUSED_WINDOW);
		pack();
		setLocationRelativeTo(null);
	}

	public boolean getResult()
	{
		return result;
	}

	public void setHeaderList(HeaderItemList list)
	{
		//addressbook.setHeaderItemList(list);
		//Vector v = list.getVector();
		
		addressbook.setHeaderList(list);
	}

	public void updateComponents(
		GroupListCard card,
		HeaderItemList list,
		boolean b)
	{
		if (b == true)
		{
			// gettext
			nameTextField.setText(card.get("displayname"));
			descriptionTextField.setText(card.get("description"));

			members = new AddressbookListModel();
			for (int i = 0; i < list.count(); i++)
			{
				HeaderItem item = list.get(i);
				members.addElement(item);
			}

			this.list.setModel(members);

		}
		else
		{
			// settext
			card.set("displayname", nameTextField.getText());
			card.set("description", descriptionTextField.getText());

			// remove all children
			card.removeMembers();

			// add children
			for (int i = 0; i < members.getSize(); i++)
			{
				HeaderItem item = (HeaderItem) members.get(i);
				Object uid = item.getUid();
				card.addMember(((Integer) uid).toString());
			}
		}
	}

	public void actionPerformed(ActionEvent e)
	{
		String command;

		command = e.getActionCommand();

		if (command.equals("CANCEL"))
		{
			result = false;

			setVisible(false);
		}
		else if (command.equals("CHOOSE"))
		{
			SelectAddressbookFolderDialog dialog =
				addressbookInterface.tree.getSelectAddressbookFolderDialog();

			Folder selectedFolder = dialog.getSelectedFolder();
			HeaderItemList list = selectedFolder.getHeaderItemList();
			setHeaderList(list);

		}
		else if (command.equals("OK"))
		{

			if (nameTextField.getText().length() == 0)
			{
				JOptionPane.showMessageDialog(
					addressbookInterface.frame,
					"You must enter a name for the group!");
				return;
			}
			result = true;

			
			setVisible(false);

		}
		else if (command.equals("ADD"))
		{

			int[] array = addressbook.getSelectedIndices();
			HeaderItem item;

			for (int j = 0; j < array.length; j++)
			{
				item = (HeaderItem) addressbook.get(array[j]);
				System.out.println("add item:"+item);
				
				members.addElement(item);
			}

		}
		else if (command.equals("REMOVE"))
		{

			int[] array = list.getSelectedIndices();
			for (int j = 0; j < array.length; j++)
			{
				System.out.println("remove index:"+array[j]);
				members.remove(array[j]);
			}

		}
	}

	public class WizardTopBorder extends AbstractBorder
	{
		protected Insets borderInsets = new Insets(2, 0, 0, 0);
		public void paintBorder(Component c, Graphics g, int x, int y, int w, int h)
		{
			g.setColor(UIManager.getColor("Button.darkShadow"));
			g.drawLine(x, y, x + w - 1, y);
			g.setColor(Color.white);
			g.drawLine(x, y + 1, x + w - 1, y + 1);
		}
		public Insets getBorderInsets(Component c)
		{
			return borderInsets;
		}
	}
}
