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
package org.columba.mail.gui.composer;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.toolbar.ToolBar;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.composer.menu.ComposerMenu;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * view for message composer dialog
 */
public class ComposerView extends AbstractFrameView {

	
	private JSplitPane rightSplitPane;
	private JSplitPane mainSplitPane;
	private Container editorPane;

	public ComposerView(AbstractFrameController controller)
	{
		super(controller);
		setTitle(MailResourceLoader.getString("dialog","composer","composerview_title")); //$NON-NLS-1$
		
	}
	
	
	
	public void setMainDividerLocation( int i )
	{
		mainSplitPane.setDividerLocation(i);
	}
	
	public void setRightDividerLocation( int i )
	{
		rightSplitPane.setDividerLocation(i);
	}
	
	
	public int getMainDividerLocation()
	{
		return mainSplitPane.getDividerLocation();
	}
	
	public int getRightDividerLocation()
	{
		return rightSplitPane.getDividerLocation();
	}
	
	
	public void init()
	{
		super.init();
		Container contentPane;
		
		contentPane = getContentPane();
		
		ComposerController controller = (ComposerController)frameController;
		/*
		setJMenuBar( new ComposerMenu(composerInterface) );
		
		contentPane.setLayout(new BorderLayout());
		
		JPanel toolbarPanel = new JPanel();
		toolbarPanel.setLayout( new BorderLayout() );
		
		
		toolbarPanel.add( new ComposerToolbar(composerInterface), BorderLayout.NORTH );
		toolbarPanel.add( composerInterface.identityInfoPanel, BorderLayout.CENTER );
		contentPane.add(toolbarPanel, BorderLayout.NORTH);
		*/
		
		
		rightSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		rightSplitPane.setBorder(null);
		rightSplitPane.add( controller.getHeaderController().view, JSplitPane.LEFT);
		rightSplitPane.add( controller.getAttachmentController().view, JSplitPane.RIGHT );
		rightSplitPane.setDividerSize(5);
		rightSplitPane.setDividerLocation(400);
		
		JPanel topPanel = new JPanel();
		topPanel.setBorder( BorderFactory.createEmptyBorder(0,5,0,0) );
		
		JLabel subjectLabel = new JLabel(MailResourceLoader.getString("dialog","composer","subject"));	 //$NON-NLS-1$
		subjectLabel.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog","composer","subject"));

		JLabel smtpLabel = new JLabel(MailResourceLoader.getString("dialog","composer","identity")); //$NON-NLS-1$
		smtpLabel.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog","composer","identity"));
		
		JLabel priorityLabel = new JLabel(MailResourceLoader.getString("dialog","composer","priority"));				 //$NON-NLS-1$
		priorityLabel.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog","composer","priority"));
		
		GridBagLayout gridbag = new GridBagLayout();
		
		topPanel.setLayout(gridbag);

		GridBagConstraints c = new GridBagConstraints();


		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.0;
		c.insets = new Insets(0, 0, 5, 5);
		gridbag.setConstraints(smtpLabel, c);
		topPanel.add(smtpLabel);

		c.gridx = 1;
		c.weightx = 1.0;
		gridbag.setConstraints(controller.getAccountController().view, c);
		topPanel.add(controller.getAccountController().view);

		c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridx = 2;
		c.weightx = 0.0;
		gridbag.setConstraints(priorityLabel, c);
		topPanel.add(priorityLabel);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 3;
		gridbag.setConstraints(controller.getPriorityController().view, c);
		topPanel.add(controller.getPriorityController().view);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		gridbag.setConstraints(subjectLabel, c);
		topPanel.add(subjectLabel);
		
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		gridbag.setConstraints(controller.getSubjectController().view, c);
		topPanel.add(controller.getSubjectController().view);
		
		
		JPanel editorPanel = new JPanel();
		editorPanel.setBorder(null);
		editorPanel.setLayout( new BorderLayout() );
		JScrollPane scrollPane = new JScrollPane(controller.getEditorController().view);
		editorPanel.add( scrollPane, BorderLayout.CENTER );
		
		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
		centerPanel.setLayout( new BorderLayout() );
		
		centerPanel.add( topPanel, BorderLayout.NORTH );
		centerPanel.add( editorPanel, BorderLayout.CENTER );
		
			
		mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		mainSplitPane.setBorder(null);
		mainSplitPane.add(
			rightSplitPane,
			JSplitPane.TOP);
			
		mainSplitPane.add(centerPanel, JSplitPane.BOTTOM);
		mainSplitPane.setDividerSize(5);
		mainSplitPane.setDividerLocation(150);
		
		contentPane.add(mainSplitPane, BorderLayout.CENTER);
		
		/*
		JLabel statusBar = new JLabel(MailResourceLoader.getString("dialog","composer","statusbar_label")); //$NON-NLS-1$
		Border border = BorderFactory.createEtchedBorder(1);
		Border margin = new EmptyBorder(2, 0, 2, 0);
		statusBar.setBorder(new CompoundBorder(margin, border));

		contentPane.add(statusBar, BorderLayout.SOUTH);
		*/
		pack();
		
		//setSize( new Dimension(580,640) );
	}
	
	
	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameView#createMenu(org.columba.core.gui.FrameController)
	 */
	protected Menu createMenu(AbstractFrameController controller) {
		Menu menu = new ComposerMenu("org/columba/core/action/menu.xml",controller);
				menu.extendMenuFromFile("org/columba/mail/action/composer_menu.xml");
		return menu;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameView#createToolbar(org.columba.core.gui.FrameController)
	 */
	protected ToolBar createToolbar(AbstractFrameController controller) {
		return new ToolBar( MailConfig.get("composer_toolbar").getElement("toolbar"), controller);
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.frame.FrameView#loadWindowPosition()
	 */
	public void loadWindowPosition() {

		super.loadWindowPosition();
	}

}