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
package org.columba.mail.gui.tree.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.columba.core.gui.util.CInfoPanel;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.MessageFolderInfo;

public class FolderInfoPanel extends CInfoPanel
{
	private JLabel leftLabel,readLabel,unreadLabel,recentLabel;
	private JPanel rightPanel;

	private ImageIcon image1,image2,image3,image4,image5,image6;

	private MessageFolderInfo info;
	private FolderItem item;

	public void initComponents()
	{
		super.initComponents();

		image1 = ImageLoader.getImageIcon("folder.png");
		image2 = ImageLoader.getImageIcon("localhost.png");
		image3 = ImageLoader.getImageIcon("remotehost.png");
		image4 = ImageLoader.getImageIcon("virtualfolder.png");

		image5 = ImageLoader.getImageIcon("mail-read.png");
		image6 = ImageLoader.getImageIcon("mail-new.png");

		leftLabel = new JLabel("Total: Unseen:");
		leftLabel.setForeground(Color.white);
		leftLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		leftLabel.setFont(font);
		leftLabel.setIconTextGap(10);
		leftLabel.setIcon(image1);
		leftLabel.setText("Folder");
		//gridbagConstraints.gridwidth = GridBagConstraints.RELATIVE;
		gridbagConstraints.gridx = 0;
		gridbagConstraints.weightx = 0.0;
		gridbagConstraints.anchor = GridBagConstraints.WEST;

		gridbagLayout.setConstraints(leftLabel, gridbagConstraints);
		panel.add(leftLabel);

		Component box = Box.createHorizontalGlue();
		gridbagConstraints.gridx = 1;
		gridbagConstraints.weightx = 1.0;
		gridbagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridbagLayout.setConstraints(box, gridbagConstraints);
		panel.add(box);

		/*
		rightLabel = new JLabel("Total: Unseen:");
		rightLabel.setForeground( Color.white );
		rightLabel.setBorder( BorderFactory.createEmptyBorder( 2,2,2,2 ) );
		rightLabel.setFont( font );
		rightLabel.setIconTextGap( 10 );
		//rightLabel.setIcon(image1);
		rightLabel.setText("Folder");
		*/

		//gridbagConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbagConstraints.gridx = 2;
		gridbagConstraints.weightx = 0.0;
		gridbagConstraints.fill = GridBagConstraints.NONE;
		//gridbagConstraints.insets = new Insets(0, 0, 0, 20);
		gridbagConstraints.anchor = GridBagConstraints.EAST;

		GridBagLayout layout = new GridBagLayout();
		rightPanel = new JPanel();
		rightPanel.setLayout(layout);
		rightPanel.setOpaque(false);
		gridbagLayout.setConstraints(rightPanel, gridbagConstraints);
		panel.add(rightPanel);

		readLabel = new JLabel();
		//readLabel.setIcon(image5);
		readLabel.setFont(font);
		readLabel.setForeground(Color.white);
		readLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		//readLabel.setIconTextGap(10);
		//readLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		unreadLabel = new JLabel();
		//unreadLabel.setIcon(image6);
		unreadLabel.setFont(font);
		unreadLabel.setForeground(Color.white);
		unreadLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		//unreadLabel.setIconTextGap(10);
		//unreadLabel.setVerticalAlignment(SwingConstants.BOTTOM);
		recentLabel = new JLabel();
		recentLabel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		recentLabel.setFont(font);
		recentLabel.setForeground(Color.white);
		recentLabel.setIconTextGap(10);

		gridbagConstraints = new GridBagConstraints();
		gridbagConstraints.gridx = 0;
		gridbagConstraints.weightx = 0.0;
		gridbagConstraints.anchor = GridBagConstraints.SOUTH;
		gridbagConstraints.insets = new Insets(0,0,0,0);
		/*
		JLabel label = new JLabel();
		label.setIcon( image5 );
		label.setVerticalAlignment(SwingConstants.BOTTOM);
		layout.setConstraints( label, gridbagConstraints );
		rightPanel.add( label );
		*/

		gridbagConstraints.gridx = 0;
		gridbagConstraints.insets = new Insets(0,0,0,0);
		gridbagConstraints.anchor = GridBagConstraints.WEST;
		layout.setConstraints(readLabel, gridbagConstraints);
		rightPanel.add(readLabel);

		/*
		label = new JLabel();
		label.setIcon( image6 );
		label.setVerticalAlignment(SwingConstants.BOTTOM);
		gridbagConstraints.gridx = 2;
		gridbagConstraints.insets = new Insets(0,0,0,0);
		gridbagConstraints.anchor = GridBagConstraints.SOUTH;
		layout.setConstraints( label, gridbagConstraints );
		rightPanel.add( label );
		*/

		gridbagConstraints.gridx = 1;
		gridbagConstraints.insets = new Insets(0,0,0,0);
		gridbagConstraints.anchor = GridBagConstraints.WEST;
		layout.setConstraints(unreadLabel, gridbagConstraints);
		rightPanel.add(unreadLabel);

		gridbagConstraints.gridx = 2;
		gridbagConstraints.insets = new Insets(0,0,0,0);
		layout.setConstraints(recentLabel, gridbagConstraints);
		rightPanel.add(recentLabel);
	}

	public void resetRenderer()
	{
		initComponents();
	}

	public void update()
	{
		if (item == null) return;

		// FIXME
		
		/*
		int uid = item.getUid();

		int total = info.getExists();
		int unread = info.getUnseen();
		int recent = info.getRecent();

		String name = item.getName();
		String type = item.getType();

		boolean noInfo = false;

		if (type.equals("virtual"))
		{
			leftLabel.setIcon(image4);
		}
		else if ((type.equals("imaproot")) && (item.isMessageFolder() == false))
		{
			leftLabel.setIcon(image3);
			noInfo = true;
		}
		else if (item.getAccessRights().equals("system"))
		{
			if (name.equals(MailResourceLoader.getString("tree", "localfolders")))
			{
				noInfo = true;
				leftLabel.setIcon(image2);
			}
			else
			{
				leftLabel.setIcon(image1);
			}
		}
		else
		{
			leftLabel.setIcon(image1);
		}

		if (noInfo)
		{
			leftLabel.setText(name);
		}
		else
		{
			leftLabel.setText(name + " ( total: " + total + " )");
			unreadLabel.setText(" unread: " + unread);
			readLabel.setText(" read: " + (total - unread) + "  ");

			if (recent > 0)
			{
				recentLabel.setText(" recent: "+recent);

				//recentLabel.setText(" (" + recent + ")");
			}
			else
			{
				recentLabel.setText("");
			}
		}
		*/
	}

	public void set(FolderItem item, MessageFolderInfo info)
	{
		this.item = item;
		this.info = info;

		update();
	}
}
