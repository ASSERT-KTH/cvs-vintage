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

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.toolbar.ToolBar;
import org.columba.core.gui.util.LabelWithMnemonic;
import org.columba.core.xml.XmlElement;
import org.columba.mail.gui.composer.html.HtmlToolbar;
import org.columba.mail.gui.composer.menu.ComposerMenu;
import org.columba.mail.gui.composer.util.IdentityInfoPanel;
import org.columba.mail.gui.view.AbstractComposerView;
import org.columba.mail.main.MailInterface;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.debug.FormDebugPanel;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * View for message composer dialog
 * 
 * @author frd
 */
public class ComposerView extends AbstractFrameView implements
		AbstractComposerView {

	private JSplitPane attachmentSplitPane;

	/** Editor viewer resides in this panel */
	private JPanel editorPanel;

	private LabelWithMnemonic subjectLabel;

	private LabelWithMnemonic smtpLabel;

	private LabelWithMnemonic priorityLabel;

	private JPanel centerPanel = new FormDebugPanel();

	private JPanel topPanel;
	
	public ComposerView(FrameMediator ctrl) {
		super(ctrl);
		setTitle(MailResourceLoader.getString("dialog", "composer",
				"composerview_title")); //$NON-NLS-1$

		ComposerController controller = (ComposerController) frameController;

		if (isAccountInfoPanelVisible()) {
			toolbarPane.add(controller.getIdentityInfoPanel());
		}

		Container contentPane = getContentPane();
		contentPane.add(centerPanel, BorderLayout.CENTER);

		initComponents();

		layoutComponents();

		showAttachmentPanel();
		
		pack();

	}

	/**
	 * init components
	 */
	protected void initComponents() {
		subjectLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "composer", "subject"));
		smtpLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "composer", "identity"));
		priorityLabel = new LabelWithMnemonic(MailResourceLoader.getString(
				"dialog", "composer", "priority"));
	}

	/**
	 * Layout components
	 */
	public void layoutComponents() {
		ComposerController controller = (ComposerController) frameController;

		centerPanel.removeAll();

		topPanel = new JPanel();
		topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 0));

		// Create a FormLayout instance.
		FormLayout layout = new FormLayout(
				"center:max(50dlu;default), 3dlu, fill:default:grow, 2dlu",

				// 2 columns
				"fill:default, 3dlu,fill:default, 3dlu, fill:default, 3dlu, fill:default, 3dlu");

		// 3 row
		PanelBuilder builder = new PanelBuilder(topPanel, layout);
		CellConstraints cc = new CellConstraints();

		layout.setColumnGroups(new int[][] { { 1 } });

		layout.setRowGroups(new int[][] { { 1, 5, 7 } });

		builder.add(smtpLabel, cc.xy(1, 1));

		JPanel smtpPanel = new JPanel();
		FormLayout l = new FormLayout(
				"default, 3dlu, right:default:grow, 3dlu, right:default",
				"fill:default:grow");
		PanelBuilder b = new PanelBuilder(smtpPanel, l);

		CellConstraints c = new CellConstraints();
		b.add(controller.getAccountController().getView(), c.xy(1, 1));
		b.add(priorityLabel, c.xy(3, 1));
		b.add(controller.getPriorityController().getView(), c.xy(5, 1));

		builder.add(smtpPanel, cc.xy(3, 1));

		builder.add(controller.getHeaderController().getView(), cc.xywh(1, 3,
				4, 1));

		builder.add(subjectLabel, cc.xy(1, 5));
		builder.add(controller.getSubjectController().getView(), cc.xy(3, 5));

		// add JPanel with useful HTML related actions.
		HtmlToolbar htmlToolbar = new HtmlToolbar(controller);

		builder.add(htmlToolbar, cc.xywh(3, 7, 2, 1));

		editorPanel = new JPanel();
		editorPanel.setBorder(null);
		editorPanel.setLayout(new BorderLayout());

		// *20030907, karlpeder* getViewUIComponent returns view
		//            already encapsulated in a scroll pane.
		//JScrollPane scrollPane =
		//	new JScrollPane(controller.getEditorController().view);
		//editorPanel.add(scrollPane, BorderLayout.CENTER);
		editorPanel.add(controller.getEditorController().getViewUIComponent());

		centerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		centerPanel.setLayout(new BorderLayout());

		centerPanel.add(topPanel, BorderLayout.NORTH);

		JScrollPane attachmentScrollPane = new JScrollPane(controller
				.getAttachmentController().getView());
		attachmentScrollPane
				.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		attachmentScrollPane.setBorder(BorderFactory.createEmptyBorder(1, 1, 1,
				1));

		attachmentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
				editorPanel, attachmentScrollPane);
		attachmentSplitPane.setDividerLocation(0.80);
		attachmentSplitPane.setBorder(null);

		centerPanel.add(attachmentSplitPane, BorderLayout.CENTER);

		XmlElement viewElement = MailInterface.config.get("composer_options")
				.getElement("/options/gui/view");
		ViewItem viewItem = new ViewItem(viewElement);
		int pos = viewItem.getInteger("splitpanes", "attachment", 200);
		attachmentSplitPane.setDividerLocation(pos);

	}

	/**
	 * Returns a reference to the panel, that holds the editor view. This is
	 * used by the ComposerController when adding a listener to that panel.
	 */
	public JPanel getEditorPanel() {
		return editorPanel;
	}

	/**
	 * Used to update the panel, that holds the editor viewer. This is necessary
	 * e.g. if the ComposerModel is changed to hold another message type (text /
	 * html), which the previous editor can not handle. If so a new editor
	 * controller is created, and thereby a new view.
	 */
	public void setNewEditorView() {
		// get reference to composer controller
		ComposerController controller = (ComposerController) frameController;

		// update panel
		editorPanel.removeAll();
		editorPanel.add(controller.getEditorController().getViewUIComponent());
		editorPanel.validate();
	}

	/**
	 * @see org.columba.core.gui.FrameView#createMenu(org.columba.core.gui.FrameController)
	 */
	protected Menu createMenu(FrameMediator controller) {
		Menu menu = new ComposerMenu("org/columba/core/action/menu.xml",
				controller);
		menu.extendMenuFromFile("org/columba/mail/action/composer_menu.xml");

		return menu;
	}

	/**
	 * @see org.columba.core.gui.FrameView#createToolbar(org.columba.core.gui.FrameController)
	 */
	protected ToolBar createToolbar(FrameMediator controller) {
		return new ToolBar(MailInterface.config.get("composer_toolbar")
				.getElement("toolbar"), controller);
	}

	public IdentityInfoPanel getAccountInfoPanel() {
		ComposerController controller = (ComposerController) frameController;

		return controller.getIdentityInfoPanel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.columba.core.gui.frame.AbstractFrameView#showToolbar()
	 */
	public void showToolbar() {
		boolean b = isToolbarVisible();

		if (toolbar == null) {
			return;
		}

		if (b) {
			toolbarPane.remove(toolbar);
			((FrameMediator) frameController)
					.enableToolbar(MAIN_TOOLBAR, false);
		} else {
			if (isAccountInfoPanelVisible()) {
				toolbarPane.removeAll();
				toolbarPane.add(toolbar);
				toolbarPane.add(getAccountInfoPanel());
			} else {
				toolbarPane.add(toolbar);
			}

			((FrameMediator) frameController).enableToolbar(MAIN_TOOLBAR, true);
		}

		validate();
		repaint();
	}

	/**
	 * Show attachment panel 
	 * <p>
	 * Asks the ComposerModel if message contains attachments. If so,
	 * show the attachment panel.
	 * Otherwise, hide the attachment panel.
	 */
	public void showAttachmentPanel() {
		ComposerController mediator = (ComposerController) getViewController();
	
		// remove all components from container
		centerPanel.removeAll();
		
		// re-add all top components like recipient editor/subject editor
		centerPanel.add(topPanel, BorderLayout.NORTH);
		
		// if message contains attachments
		if (mediator.getAttachmentController().getView().count() > 0) {
			// create scrollapen
			JScrollPane attachmentScrollPane = new JScrollPane(mediator
					.getAttachmentController().getView());
			attachmentScrollPane
					.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
			attachmentScrollPane.setBorder(BorderFactory.createEmptyBorder(1,
					1, 1, 1));
			// create splitpane containing the bodytext editor and the
			// attachment panel
			attachmentSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					editorPanel, attachmentScrollPane);
			attachmentSplitPane.setDividerLocation(0.80);
			attachmentSplitPane.setBorder(null);

			// add splitpane to the center
			centerPanel.add(attachmentSplitPane, BorderLayout.CENTER);

			// set splitpane position based on configuration settings
			XmlElement viewElement = MailInterface.config.get(
					"composer_options").getElement("/options/gui/view");
			ViewItem viewItem = new ViewItem(viewElement);
			// default value is 200 pixel
			int pos = viewItem.getInteger("splitpanes", "attachment", 200);
			attachmentSplitPane.setDividerLocation(pos);
		} else {
			// no attachments
			// -> only show bodytext editor
			centerPanel.add(editorPanel, BorderLayout.CENTER);
		}
		
		// re-paint composer-view
		validate();
		repaint();
	}

	public void showAccountInfoPanel() {
		boolean b = isAccountInfoPanelVisible();

		if (b) {
			toolbarPane.remove(getAccountInfoPanel());
			((FrameMediator) frameController).enableToolbar(ACCOUNTINFOPANEL,
					false);
		} else {
			toolbarPane.add(getAccountInfoPanel());

			((FrameMediator) frameController).enableToolbar(ACCOUNTINFOPANEL,
					true);
		}

		validate();
		repaint();
	}

	public boolean isAccountInfoPanelVisible() {
		return ((FrameMediator) frameController)
				.isToolbarEnabled(ACCOUNTINFOPANEL);
	}

	/**
	 * @return Returns the attachmentSplitPane.
	 */
	public JSplitPane getAttachmentSplitPane() {
		return attachmentSplitPane;
	}

	public void savePositions() {
		super.savePositions();

		XmlElement viewElement = MailInterface.config.get("composer_options")
				.getElement("/options/gui/view");
		ViewItem viewItem = new ViewItem(viewElement);

		// splitpanes
		if (attachmentSplitPane != null)
			viewItem.set("splitpanes", "attachment", attachmentSplitPane
					.getDividerLocation());

	}
}