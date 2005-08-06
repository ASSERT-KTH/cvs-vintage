// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.frame;

import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JPanel;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.selection.SelectionManager;
import org.columba.core.util.GlobalResourceLoader;

/**
 * @author fdietz
 *  
 */
public class DefaultFrameController implements IFrameMediator {

	
	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.frame");

	/**
	 * Saves view information like position, size and maximization state
	 */
	protected ViewItem viewItem;

	/**
	 * Selection handler
	 */
	protected SelectionManager selectionManager;

	/**
	 * ID of controller
	 */
	protected String id;

	private IContainer container;

	/**
	 *  
	 */
	public DefaultFrameController(ViewItem viewItem) {

		super();

		this.viewItem = viewItem;

		this.id = viewItem.get("id");

		// init selection handler
		selectionManager = new SelectionManager();

	}
	
	public DefaultFrameController(String id) {
		this(ViewItem.createDefault(id));

	}

	/**
	 * @return ViewItem
	 */
	public ViewItem getViewItem() {
		return viewItem;
	}

	/**
	 * Sets the item.
	 * 
	 * @param item
	 *            The item to set
	 */
	public void setViewItem(ViewItem item) {
		this.viewItem = item;
	}

	/**
	 * @return SelectionManager
	 */
	public SelectionManager getSelectionManager() {
		return selectionManager;
	}

	/**
	 * Sets the selectionManager.
	 * 
	 * @param selectionManager
	 *            The selectionManager to set
	 */
	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	/**
	 * @see org.columba.core.gui.frame.IFrameMediator#getContainer()
	 */
	public IContainer getContainer() {
		return container;
	}

	/**
	 * @see org.columba.core.gui.frame.IFrameMediator#loadPositions(org.columba.core.config.ViewItem)
	 */
	public void loadPositions(ViewItem viewItem) {

	}

	/**
	 * @see org.columba.core.gui.frame.IFrameMediator#savePositions(org.columba.core.config.ViewItem)
	 */
	public void savePositions(ViewItem viewItem) {

	}

	/**
	 * @see org.columba.core.gui.frame.IFrameMediator#setContainer(org.columba.core.gui.frame.IContainer)
	 */
	public void setContainer(IContainer c) {
		container = c;
	}

	/**
	 * @see org.columba.core.gui.frame.IFrameMediator#getView()
	 */
	public IContainer getView() {
		return container;
	}

	/**
	 * @see org.columba.core.gui.frame.IFrameMediator#getString(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getString(String sPath, String sName, String sID) {
		return GlobalResourceLoader.getString(sPath, sName, sID);
	}

	/**
	 * @see org.columba.core.gui.frame.IFrameMediator#getContentPane()
	 */
	public IContentPane getContentPane() {
		return new EmptyContentPane();
	}
	
	class EmptyContentPane implements IContentPane {
		public EmptyContentPane() {
			super();
		}

		/**
		 * @see org.columba.core.gui.frame.IContentPane#getComponent()
		 */
		public JComponent getComponent() {
			return new JPanel();
		}

	}
	
	/**
	 * @see org.columba.core.gui.frame.IFrameMediator#close()
	 */
	public void close() {

	}
}