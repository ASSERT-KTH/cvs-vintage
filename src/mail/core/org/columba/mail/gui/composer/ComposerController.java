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
import java.awt.event.ContainerListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

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
import org.columba.mail.gui.composer.text.TextEditorController;
import org.columba.mail.gui.composer.util.IdentityInfoPanel;
import org.columba.mail.parser.text.HtmlParser;
import org.columba.mail.util.AddressCollector;

/**
 * @author frd
 *
 * controller for message composer dialog
 */
public class ComposerController
		extends AbstractFrameController
		implements CharsetListener, CharsetOwnerInterface, 
				   ComponentListener, WindowListener,
				   Observer {

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

	/** Buffer for listeners used by addContainerListenerForEditor and createView */
	private List containerListenerBuffer;

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
		
		// *20030917, karlpeder* If ContainerListeners are waiting to be
		// added, add them now.
		if (containerListenerBuffer != null) {
			ColumbaLogger.log.debug("Adding ContainerListeners from buffer");
			Iterator ite = containerListenerBuffer.iterator();
			while (ite.hasNext()) {
				ContainerListener cl = (ContainerListener) ite.next();
				view.getEditorPanel().addContainerListener(cl);
			}
			containerListenerBuffer = null; // done, the buffer has been emptied
		}

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

		// set default html or text based on stored option
		// ... can be overridden by setting the composer model
		
		XmlElement optionsElement =
			MailConfig.get("composer_options").getElement("/options");
		XmlElement htmlElement = optionsElement.getElement("html");
		// create default element if not available
		if (htmlElement == null)
			htmlElement = optionsElement.addSubElement("html");
		String enableHtml = htmlElement.getAttribute("enable", "false");
		
		// set model based on configuration
		if ( enableHtml.equals("true")) {
			getModel().setHtml(true);
		} else {
			getModel().setHtml(false);
		}
		
		// Add the composer controller as observer
		htmlElement.addObserver(this);

		// init controller for the editor depending on message type
		if (getModel().isHtml())
			editorController = new HtmlEditorController(this);
		else
			editorController = new TextEditorController(this);

		// init charset handling
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
			switchEditor(composerModel.isHtml());

			/*
			 * *20030917, karlpeder* We need to change the html option, 
			 * since menues etc. are observing that option. This is how
			 * the current setup is - but one could argue that it is not
			 * good, since it changes the format used as default next time
			 * the composer is started.
			 * 
			 * TODO: Find a solution - maybe an additional option: default_enable !?
			 */
			XmlElement optionsElement =
				MailConfig.get("composer_options").getElement("/options");
			XmlElement htmlElement = optionsElement.getElement("html");
			//	create default element if not available
			if (htmlElement == null)
				htmlElement = optionsElement.addSubElement("html");
			// change configuration based on new model	 
			htmlElement.addAttribute("enable",
					new Boolean(composerModel.isHtml()).toString());
			// notify observers - this includes this object - but here it will
			// do nothing, since the model is already setup correctly
			htmlElement.notifyObservers();

		}

		// Update all component according to the new model
		updateComponents(true);
	}

	/**
	 * Private utility for switching btw. html and text.
	 * This includes instantiating a new editor controller
	 * and refreshing the editor view accordingly.
	 * <br>
	 * Pre-condition: The caller should set the composer model
	 * before calling this method. If a message was already entered
	 * in the UI, then updateComponents should have been called to 
	 * synchronize model with view before switching, else data will be lost.
	 * <br>
	 * Post-condition: The caller must call updateComponents afterwards
	 * to display model data using the new controller-view pair
	 * 
	 * @param	html	True if we should switch to html, false for text
	 */
	private void switchEditor(boolean html) {
		if (composerModel.isHtml()) {
			ColumbaLogger.log.info("Switching to html editor");
			editorController.deleteObservers(); // clean up
			editorController = new HtmlEditorController(this);
		} else {
			ColumbaLogger.log.info("Switching to text editor");
			editorController.deleteObservers(); // clean up
			editorController = new TextEditorController(this);
		}

		// an update of the view is also necessary.
		((ComposerView) getView()).setNewEditorView();
	}

	/**
	 * Register ContainerListener for the panel, that holds the
	 * editor view. By registering as listener it is possible to 
	 * get information when the editor changes.
	 * <br>
	 * If the view is not yet created, the listener is stored in
	 * a buffer - add then added in createView. This is necessary to
	 * handle the timing involved in setting up the controller-view
	 * framework for the composer
	 */
	public void addContainerListenerForEditor(ContainerListener cl) {
		ComposerView view = (ComposerView) getView();
		if (view != null) {
			// add listener
			view.getEditorPanel().addContainerListener(cl);
		} else {
			// view not yet created - store listener in buffer
			if (containerListenerBuffer == null) {
				containerListenerBuffer = new ArrayList();
			}
			containerListenerBuffer.add(cl);
		}
	}
	
	/** 
	 * Removes a ContainerListener from the panel, that holds the
	 * editor view (previously registered using 
	 * addContainListenerForEditor)
	 */
	public void removeContainerListenerForEditor(ContainerListener cl) {
		((ComposerView) getView()).
				getEditorPanel().removeContainerListener(cl);
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
	
	
	/**
	 * Used for listenen to the enable html option
	 * 
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable o, Object arg) {
		XmlElement e = (XmlElement) o;

		if (e.getName().equals("html")) {
			// switch btw. html and text if necessary
			String enableHtml = e.getAttribute("enable", "false");
			boolean html = (new Boolean(enableHtml)).booleanValue();
			boolean wasHtml = composerModel.isHtml();
			
			if (html != wasHtml) {
				composerModel.setHtml(html);

				// sync model with the current (old) view
				updateComponents(false);
				
				// convert body text to comply with new editor format
				String oldBody = composerModel.getBodyText();
				String newBody;
				if (html) {
					ColumbaLogger.log.info("Converting body text to html");
					newBody = HtmlParser.textToHtml(oldBody, "", null);
				} else {
					ColumbaLogger.log.info("Converting body text to text");
					newBody = HtmlParser.htmlToText(oldBody);
				}
				composerModel.setBodyText(newBody);
				
				// switch editor and resync view with model
				switchEditor(composerModel.isHtml());
				updateComponents(true);
			}
		}
	}
	
}
