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

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.columba.addressbook.folder.Folder;
import org.columba.addressbook.folder.HeaderItem;
import org.columba.addressbook.folder.HeaderItemList;
import org.columba.core.charset.CharsetEvent;
import org.columba.core.charset.CharsetListener;
import org.columba.core.charset.CharsetManager;
import org.columba.core.charset.CharsetOwnerInterface;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.frame.AbstractFrameView;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.MailConfig;
import org.columba.mail.gui.composer.html.HtmlEditorController;
import org.columba.mail.gui.composer.text.*;
import org.columba.mail.gui.composer.util.IdentityInfoPanel;
import org.columba.mail.util.AddressCollector;

/**
 * @author frd
 *
 * controller for message composer dialog
 */
public class ComposerController
	extends AbstractFrameController
	implements CharsetListener, CharsetOwnerInterface, ComponentListener, WindowListener {

	private IdentityInfoPanel identityInfoPanel;
	private AttachmentController attachmentController;
	private SubjectController subjectController;
	private PriorityController priorityController;
	private AccountController accountController;
	//private TextEditorController editorController;
	private AbstractEditorController editorController;
	private HeaderController headerController;
	//private MessageComposer messageComposer;
	private ComposerSpellCheck composerSpellCheck;
	private ComposerModel composerModel;
	protected CharsetManager charsetManager;

	/*
	Message message;
	AccountItem accountItem;
	String bodytext;
	String charsetName;
	
	Vector attachments;
	
	Vector toList;
	Vector ccList;
	Vector bccList;
	
	boolean signMessage;
	boolean encryptMessage;
	*/

	public ComposerController() {
		super(
			"Composer",
			new ViewItem(
				MailConfig.get("composer_options").getElement(
					"/options/gui/view")));

		getView().addWindowListener(this);
		getView().loadWindowPosition();
		headerController.view.getTable().initFocus(subjectController.view);
		getView().setVisible(true);
		initAddressCompletion();
		headerController.editLastRow();
	}

	public void charsetChanged(CharsetEvent e) {
		((ComposerModel) getModel()).setCharsetName(e.getValue());
		//editorController.getView().setCharset(e.getValue());
		editorController.setViewCharset(e.getValue());
	}

	public boolean checkState() {
		// update ComposerModel based on user-changes in ComposerView
		updateComponents(false);

		if (!subjectController.checkState())
			return false;
		return !headerController.checkState();
	}

	public void updateComponents(boolean b) {
		subjectController.updateComponents(b);
		editorController.updateComponents(b);
		priorityController.updateComponents(b);
		accountController.updateComponents(b);
		attachmentController.updateComponents(b);
		headerController.updateComponents(b);
		getCharsetManager().displayCharset(composerModel.getCharsetName());
		//headerController.appendRow();
	}

	protected void initAddressCompletion() {
		AddressCollector.clear();

		HeaderItemList list =
			((Folder) MainInterface.addressbookTreeModel.getFolder(101))
				.getHeaderItemList();

		for (int i = 0; i < list.count(); i++) {
			HeaderItem item = list.get(i);

			if (item.contains("displayname"))
				AddressCollector.addAddress((String) item.get("displayname"), item); //$NON-NLS-1$ //$NON-NLS-2$
			if (item.contains("email;internet"))
				AddressCollector.addAddress((String) item.get("email;internet"), item); //$NON-NLS-1$ //$NON-NLS-2$
		}

		list =
			((Folder) MainInterface.addressbookTreeModel.getFolder(102))
				.getHeaderItemList();

		for (int i = 0; i < list.count(); i++) {
			HeaderItem item = list.get(i);

			if (item.contains("displayname"))
				AddressCollector.addAddress((String) item.get("displayname"), item); //$NON-NLS-1$ //$NON-NLS-2$
			if (item.contains("email;internet"))
				AddressCollector.addAddress((String) item.get("email;internet"), item); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	/*
	protected void updateAddressbookFrame() {
	
		if ((view.getLocation().x
			- composerInterface.addressbookFrame.getSize().width
			< 0)
			|| (view.getLocation().y < 0)) {
			int x =
				view.getLocation().x
					- composerInterface.addressbookFrame.getSize().width;
			int y = view.getLocation().y;
	
			if (x <= 0)
				x = 0;
			if (y <= 0)
				y = 0;
	
			view.setLocation(
				x + composerInterface.addressbookFrame.getSize().width,
				y);
	
		}
	
		composerInterface.addressbookFrame.setLocation(
			view.getLocation().x
				- composerInterface.addressbookFrame.getSize().width,
			view.getLocation().y);
	
	}*/

	public void componentHidden(ComponentEvent e) {
	}

	public void componentMoved(ComponentEvent e) {

		/*		
		if (composerInterface.addressbookFrame.isVisible()) {
			updateAddressbookFrame();
		}*/

	}

	public void componentResized(ComponentEvent e) {
	}

	public void componentShown(ComponentEvent e) {
	}

	public void windowIconified(WindowEvent e) {
	}

	public void windowActivated(WindowEvent e) {
	}

	public void windowClosed(WindowEvent e) {
	}

	public void windowClosing(WindowEvent e) {
		close();
	}

	public void windowDeactivated(WindowEvent e) {
	}

	public void windowDeiconified(WindowEvent e) {
	}

	public void windowOpened(WindowEvent e) {
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#createView()
	 */
	protected AbstractFrameView createView() {
		ComposerView view = new ComposerView(this);

		//view.init();

		return view;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#initInternActions()
	 */
	protected void initInternActions() {

	}

	/**
	 * @return AccountController
	 */
	public AccountController getAccountController() {
		return accountController;
	}

	/**
	 * @return AttachmentController
	 */
	public AttachmentController getAttachmentController() {
		return attachmentController;
	}

	/**
	 * @return ComposerSpellCheck
	 */
	public ComposerSpellCheck getComposerSpellCheck() {
		return composerSpellCheck;
	}

	/**
	 * @return TextEditorController
	 */
	public AbstractEditorController getEditorController() {
		/*
		 * *20030906, karlpeder* Method signature changed to 
		 * return an AbstractEditorController
		 */
		return editorController;
	}

	/**
	 * @return HeaderController
	 */
	public HeaderController getHeaderController() {
		return headerController;
	}

	/**
	 * @return IdentityInfoPanel
	 */
	public IdentityInfoPanel getIdentityInfoPanel() {
		return identityInfoPanel;
	}

	/**
	 * @return PriorityController
	 */
	public PriorityController getPriorityController() {
		return priorityController;
	}

	/**
	 * @return SubjectController
	 */
	public SubjectController getSubjectController() {
		return subjectController;
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.FrameController#init()
	 */
	protected void init() {
		// init model (defaults to empty plain text message)
		composerModel = new ComposerModel();
		
		// init controllers for different parts of the composer
		identityInfoPanel = new IdentityInfoPanel();
		attachmentController = new AttachmentController(this);
		headerController = new HeaderController(this);
		subjectController = new SubjectController(this);
		priorityController = new PriorityController(this);
		accountController = new AccountController(this);
		composerSpellCheck = new ComposerSpellCheck(this);

		// init controller for the editor depending on message type
		if (getModel().isHtml())
			editorController = new HtmlEditorController(this);		
		else
			editorController = new TextEditorController(this);
		
		// init charset handling
		XmlElement optionsElement =
			MailConfig.get("composer_options").getElement("/options");
		XmlElement charsetElement = optionsElement.getElement("charset");
		if (charsetElement == null) {
			charsetElement = new XmlElement("charset");
			charsetElement.addAttribute("name", "auto");

			optionsElement.addElement(charsetElement);
		}
		setCharsetManager(new CharsetManager(charsetElement));
		getCharsetManager().addCharsetListener(this);
		
		// Hack to ensure charset is set correctly at start-up
		String charset = charsetElement.getAttribute("name");
		if (charset != null) {
			((ComposerModel) getModel()).setCharsetName(charset);
			//editorController.getView().setCharset(charset);
			editorController.setViewCharset(charset);
		}
	}

	/**
	 * Returns the composer model
	 * @return	Composer model
	 */
	public ComposerModel getModel() {
		//if (composerModel == null) // *20030907, karlpeder* initialized in init
		//	composerModel = new ComposerModel();
		return composerModel;
	}

	/**
	 * Sets the composer model. If the message type of the new 
	 * model (html / text) is different from the message type of
	 * the existing, the editor controller is changed and the 
	 * view is changed accordingly.
	 * <br>
	 * Finally the components are updated according to the new model.
	 * 
	 * @param 	model	New composer model
	 */
	public void setComposerModel(ComposerModel model) {
		boolean wasHtml = composerModel.isHtml();
		composerModel = model;
		
		if (wasHtml != composerModel.isHtml()) {
			// new editor controller needed
			if (composerModel.isHtml())
				editorController = new HtmlEditorController(this);
			else
				editorController = new TextEditorController(this);
		
			// an update of the view is also necessary.
			((ComposerView) getView()).setNewEditorView();
		}
		
		// Update all component according to the new model
		updateComponents(true);
	}

	/* *20030831, karlpeder* Using method on super class instead
	public void close() {
                ColumbaLogger.log.info("closing ComposerController");
		view.saveWindowPosition();
		view.setVisible(false);
	}
	*/
	
	/**
	 * @return CharsetManager
	 */
	public CharsetManager getCharsetManager() {
		return charsetManager;
	}

	/**
	 * @param manager
	 */
	public void setCharsetManager(CharsetManager manager) {
		charsetManager = manager;
	}
}
