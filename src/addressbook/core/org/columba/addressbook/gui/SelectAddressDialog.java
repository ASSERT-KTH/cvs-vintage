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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;

import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.GroupListCard;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.addressbook.gui.tree.util.SelectAddressbookFolderDialog;
import org.columba.addressbook.gui.util.AddressbookDNDListView;
import org.columba.addressbook.gui.util.AddressbookListModel;
import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.parser.AddressParser;

public class SelectAddressDialog extends JDialog implements ActionListener
{

	private AddressbookDNDListView toList;
	private AddressbookDNDListView ccList;
	private AddressbookDNDListView bccList;
	private AddressbookDNDListView addressbook;

	private JButton toButton;
	private JButton ccButton;
	private JButton bccButton;
	private JButton toRemoveButton;
	private JButton ccRemoveButton;
	private JButton bccRemoveButton;

	private JButton chooseButton;
	private JLabel chooseLabel;

	private AddressbookListModel[] dialogList;

	private HeaderItemList[] headerItemList;
	
	private JButton cancelButton;
	private JButton okButton;

	private AddressbookInterface addressbookInterface;

	public SelectAddressDialog(
		AddressbookInterface addressbookInterface,
		JFrame frame,
		HeaderItemList[] list)
	{
		super(frame, true);

		this.headerItemList = list;
		this.addressbookInterface = addressbookInterface;

		dialogList = new AddressbookListModel[3];

		init();

	}

	public HeaderItemList[] getHeaderItemLists()
	{
		return headerItemList;
	}
	
	protected void init()
	{
		getContentPane().setLayout(new BorderLayout());

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		Border b = BorderFactory.createEmptyBorder(10, 10, 10, 10);
		mainPanel.setBorder(b);
		//panel.setLayout( new BoxLayout( panel, BoxLayout.X_AXIS ) );

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());

		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

		leftPanel.add(Box.createVerticalGlue());

		JPanel list1Panel = new JPanel();
		list1Panel.setLayout(new BorderLayout());
		JPanel list1TopPanel = new JPanel();
		list1TopPanel.setLayout(new BorderLayout());
		list1TopPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		JLabel label1 = new JLabel("To:");
		list1TopPanel.add(label1, BorderLayout.WEST);
		list1Panel.add(list1TopPanel, BorderLayout.NORTH);

		/*
		JPanel list1EastPanel = new JPanel();
		list1EastPanel.setLayout( new GridLayout(2,1) );
		toButton = new JButton("<- To");
		toButton.addActionListener(this);
		toButton.setActionCommand("TO");
		list1EastPanel.add( toButton );
		toRemoveButton = new JButton("To ->");
		toRemoveButton.addActionListener(this);
		toRemoveButton.setActionCommand("TO_REMOVE");
		list1EastPanel.add( toRemoveButton );
		
		list1Panel.add( list1EastPanel, BorderLayout.EAST );
		*/

		dialogList[0] = new AddressbookListModel();
		dialogList[0].setHeaderList( headerItemList[0]);
		toList = new AddressbookDNDListView(dialogList[0]);
		//toList.setCellRenderer(new AddressbookListRenderer());
		JScrollPane toPane = new JScrollPane(toList);
		toPane.setPreferredSize(new Dimension(250, 150));
		list1Panel.add(toPane, BorderLayout.CENTER);
		leftPanel.add(list1Panel);

		leftPanel.add(Box.createRigidArea(new java.awt.Dimension(0, 20)));

		JPanel list2Panel = new JPanel();
		list2Panel.setLayout(new BorderLayout());
		JPanel list2TopPanel = new JPanel();
		list2TopPanel.setLayout(new BorderLayout());
		list2TopPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		JLabel label2 = new JLabel("Cc:");
		list2TopPanel.add(label2, BorderLayout.WEST);
		list2Panel.add(list2TopPanel, BorderLayout.NORTH);
		dialogList[1] = new AddressbookListModel();
		dialogList[1].setHeaderList( headerItemList[1] );
		ccList = new AddressbookDNDListView(dialogList[1]);
		//ccList.setCellRenderer(new AddressbookListRenderer());
		JScrollPane ccPane = new JScrollPane(ccList);
		ccPane.setPreferredSize(new Dimension(250, 150));
		list2Panel.add(ccPane, BorderLayout.CENTER);
		leftPanel.add(list2Panel);

		leftPanel.add(Box.createRigidArea(new java.awt.Dimension(0, 20)));

		JPanel list3Panel = new JPanel();
		list3Panel.setLayout(new BorderLayout());
		JPanel list3TopPanel = new JPanel();
		list3TopPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));

		JLabel label3 = new JLabel("Bcc:");

		list3TopPanel.setLayout(new BorderLayout());
		list3TopPanel.add(label3, BorderLayout.WEST);
		list3Panel.add(list3TopPanel, BorderLayout.NORTH);

		dialogList[2] = new AddressbookListModel();
		dialogList[2].setHeaderList( headerItemList[2] );
		bccList = new AddressbookDNDListView(dialogList[2]);
		//bccList.setCellRenderer(new AddressbookListRenderer());
		JScrollPane bccPane = new JScrollPane(bccList);
		bccPane.setPreferredSize(new Dimension(250, 150));
		list3Panel.add(bccPane, BorderLayout.CENTER);
		leftPanel.add(list3Panel);

		/*
		for (int i = 0; i < 3; i++)
		{
			Object[] array = list[i].toArray();

			for (int j = 0; j < array.length; j++)
			{
				dialogList[i].addElement(array[j]);
			}

		}
		*/

		leftPanel.add(Box.createVerticalGlue());

		//panel.add( Box.createRigidArea( new java.awt.Dimension(20,0) ) );

		panel.add(leftPanel, BorderLayout.CENTER);

		mainPanel.add(panel, BorderLayout.WEST);

		JPanel middlePanel = new JPanel();
		middlePanel.setLayout(new BoxLayout(middlePanel, BoxLayout.Y_AXIS));

		middlePanel.add(Box.createVerticalGlue());

		toButton = new JButton("<- To");
		toButton.addActionListener(this);
		toButton.setActionCommand("TO");
		middlePanel.add(toButton);
		toRemoveButton = new JButton("To ->");
		toRemoveButton.addActionListener(this);
		toRemoveButton.setActionCommand("TO_REMOVE");
		middlePanel.add(toRemoveButton);

		//middlePanel.add( Box.createRigidArea( new java.awt.Dimension(0,20) ) );
		middlePanel.add(Box.createVerticalGlue());

		ccButton = new JButton("<- Cc");
		ccButton.addActionListener(this);
		ccButton.setActionCommand("CC");
		middlePanel.add(ccButton);
		ccRemoveButton = new JButton("Cc ->");
		ccRemoveButton.addActionListener(this);
		ccRemoveButton.setActionCommand("CC_REMOVE");
		middlePanel.add(ccRemoveButton);

		//middlePanel.add( Box.createRigidArea( new java.awt.Dimension(0,20) ) );
		middlePanel.add(Box.createVerticalGlue());

		bccButton = new JButton("<- Bcc");
		bccButton.addActionListener(this);
		bccButton.setActionCommand("BCC");
		middlePanel.add(bccButton);
		bccRemoveButton = new JButton("Bcc ->");
		bccRemoveButton.addActionListener(this);
		bccRemoveButton.setActionCommand("BCC_REMOVE");
		middlePanel.add(bccRemoveButton);

		middlePanel.add(Box.createVerticalGlue());

		//panel.add( Box.createRigidArea( new java.awt.Dimension(20,0) ) );

		panel.add(middlePanel, BorderLayout.EAST);

		JPanel rightPanel = new JPanel();
		rightPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		rightPanel.setLayout(new BorderLayout());
		JPanel rightTopPanel = new JPanel();
		//rightTopPanel.setLayout( new GridBagLayout() );
		chooseLabel = new JLabel("Addressbook:");
		rightTopPanel.add(chooseLabel);
		chooseButton = new JButton("Personal Addressbook");
		chooseButton.setActionCommand("CHOOSE");
		chooseButton.addActionListener(this);
		rightTopPanel.add(chooseButton);
		rightPanel.add(rightTopPanel, BorderLayout.NORTH);

		addressbook = new AddressbookDNDListView();
		addressbook.setAcceptDrop(false);
		//addressbook.setCellRenderer(new AddressbookListRenderer());

		JScrollPane scrollPane = new JScrollPane(addressbook);
		scrollPane.setPreferredSize(new Dimension(250, 200));
		rightPanel.add(scrollPane, BorderLayout.CENTER);

		mainPanel.add(rightPanel, BorderLayout.CENTER);

		getContentPane().add(mainPanel, BorderLayout.CENTER);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new CompoundBorder(
					new WizardTopBorder(),
					BorderFactory.createEmptyBorder(17, 12, 11, 11)));
		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 5, 0));
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

		/*
		java.awt.Dimension dim = new Dimension(640, 480);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		setLocation(
			screenSize.width / 2 - dim.width / 2,
			screenSize.height / 2 - dim.height / 2);
		*/
	}

	public void setHeaderList(HeaderItemList list)
	{
		//members = new DefaultListModel();

		/*
		for ( int i=0; i<list.count(); i++ )
		{
			HeaderItem item = list.get(i);
			//members.addElement( item );
		}
		*/
		Vector v = list.getVector();

		addressbook.setListData(v);
	}

	public void updateComponents(
		GroupListCard card,
		HeaderItemList list,
		boolean b)
	{
		/*
		if ( b == true )
		{
			// gettext
			nameTextField.setText( card.get("displayname") );
			
			
			members = new DefaultListModel();
			for ( int i=0; i<list.count(); i++ )
			{
				HeaderItem item = list.get(i);
				members.addElement(item);
			}
			
			this.list.setModel( members );
			
		}
		else
		{
			// settext
			card.set("displayname",nameTextField.getText() );
			
			// remove all children
			card.removeMembers();
			
			// add children
			for ( int i=0; i<members.size(); i++ )
			{
				HeaderItem item = (HeaderItem) members.get(i);
				Object uid = item.getUid();
				card.addMember( ((Integer) uid).toString() );
			}
		}
		*/
	}

	public void actionPerformed(ActionEvent e)
	{
		String command;

		command = e.getActionCommand();

		if (command.equals("CANCEL"))
		{
			setVisible(false);
		}
		else if (command.equals("OK"))
		{
			setVisible(false);

			for (int i = 0; i < 3; i++)
			{
				Object[] array = dialogList[i].toArray();
				headerItemList[i].clear();
		
				System.out.println("array-size="+array.length);
				
				for (int j = 0; j < array.length; j++)
				{
					HeaderItem item = (HeaderItem) array[j];
					if (item.isContact())
					{

						String address = (String) item.get("email;internet");
						System.out.println("old address:" + address);
						if ( address == null ) address = "";
						/*
						String newaddress = AddressParser.quoteAddress(address);
						System.out.println("new address:" + newaddress);
						item.add("email;internet", newaddress);
						*/
					}
					
					if ( i == 0 ) item.add("field","To");
					else if ( i == 1 ) item.add("field","Cc");
					else if ( i == 2 ) item.add("field","Bcc");					

					headerItemList[i].add((HeaderItem)item.clone());
					//headerItemList[i].add(item);
				}

			}
		}
		else if (command.equals("TO"))
		{
			int[] array = addressbook.getSelectedIndices();
			ListModel model = addressbook.getModel();
			HeaderItem item;

			for (int j = 0; j < array.length; j++)
			{
				item = (HeaderItem) model.getElementAt(array[j]);

				dialogList[0].addElement((HeaderItem)item.clone());
			}
		}
		else if (command.equals("CC"))
		{
			int[] array = addressbook.getSelectedIndices();
			ListModel model = addressbook.getModel();
			HeaderItem item;

			for (int j = 0; j < array.length; j++)
			{
				item = (HeaderItem) model.getElementAt(array[j]);

				dialogList[1].addElement((HeaderItem)item.clone());
			}
		}
		else if (command.equals("BCC"))
		{
			int[] array = addressbook.getSelectedIndices();
			ListModel model = addressbook.getModel();
			HeaderItem item;

			for (int j = 0; j < array.length; j++)
			{
				item = (HeaderItem) model.getElementAt(array[j]);

				dialogList[2].addElement((HeaderItem)item.clone());
			}
		}
		else if (command.equals("TO_REMOVE"))
		{
			Object[] array = toList.getSelectedValues();
			for (int j = 0; j < array.length; j++)
			{
				dialogList[0].removeElement(array[j]);
			}
		}
		else if (command.equals("CC_REMOVE"))
		{
			Object[] array = ccList.getSelectedValues();
			for (int j = 0; j < array.length; j++)
			{
				dialogList[1].removeElement(array[j]);
			}
		}
		else if (command.equals("BCC_REMOVE"))
		{
			Object[] array = bccList.getSelectedValues();
			for (int j = 0; j < array.length; j++)
			{
				dialogList[2].removeElement(array[j]);
			}

		}
		else if (command.equals("CHOOSE"))
		{
			SelectAddressbookFolderDialog dialog =
				addressbookInterface.tree.getSelectAddressbookFolderDialog();

			Folder selectedFolder = dialog.getSelectedFolder();
			HeaderItemList list = selectedFolder.getHeaderItemList();
			setHeaderList(list);

			chooseButton.setText(selectedFolder.getName());
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
