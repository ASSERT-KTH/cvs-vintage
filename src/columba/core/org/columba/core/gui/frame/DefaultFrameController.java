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

import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.event.EventListenerList;

import org.columba.api.gui.frame.IContainer;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.selection.ISelectionManager;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.frame.event.FrameEvent;
import org.columba.core.gui.frame.event.IFrameMediatorListener;
import org.columba.core.resourceloader.GlobalResourceLoader;
import org.columba.core.selection.SelectionManager;
import org.flexdock.docking.Dockable;

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
	protected ISelectionManager selectionManager;

	/**
	 * ID of controller
	 */
	protected String id;

	private IContainer container;

	protected EventListenerList listenerList = new EventListenerList();
	
	// Menuitems use this to display a string in the statusbar
	protected TooltipMouseHandler tooltipMouseHandler;
	
	/**
	 * 
	 */
	public DefaultFrameController(ViewItem viewItem) {

		super();

		this.viewItem = viewItem;

		this.id = viewItem.get("id");

		// init selection handler
		selectionManager = new SelectionManager();
		
		tooltipMouseHandler = new TooltipMouseHandler(this);

	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#registerDockables()
	 */
	public void registerDockables() {
		
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
	public ISelectionManager getSelectionManager() {
		return selectionManager;
	}

	/**
	 * Sets the selectionManager.
	 * 
	 * @param selectionManager
	 *            The selectionManager to set
	 */
	public void setSelectionManager(ISelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#getContainer()
	 */
	public IContainer getContainer() {
		return container;
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#loadPositions(org.columba.core.config.ViewItem)
	 */
	// public void loadPositions(ViewItem viewItem) {
	//
	// }
	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#savePositions(org.columba.core.config.ViewItem)
	 */
	// public void savePositions(ViewItem viewItem) {
	//
	// }
	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#setContainer(org.columba.api.gui.frame.IContainer)
	 */
	public void setContainer(IContainer c) {
		container = c;
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#getView()
	 */
	public IContainer getView() {
		return container;
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#getString(java.lang.String,
	 *      java.lang.String, java.lang.String)
	 */
	public String getString(String sPath, String sName, String sID) {
		return GlobalResourceLoader.getString(sPath, sName, sID);
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#getContentPane()
	 */
	public JPanel getContentPane() {
		return new JPanel();
	}

	/**
	 * @see org.columba.api.gui.frame.IFrameMediator#close()
	 */
	public void close(IContainer container) {
		// overwrite this method
	}

	public void savePositions() {
		// overwrite this method

	}

	public void loadPositions() {
		// overwrite this method

	}

	public String getId() {
		return id;
	}

	
	
	class MyMouseAdapter extends MouseAdapter {
	
	}
	
	/************************ frame eventing **********************/
	
	public void addListener(IFrameMediatorListener l) {
		listenerList.add(IFrameMediatorListener.class, l);
	}

	public void removeListener(IFrameMediatorListener l) {
		listenerList.remove(IFrameMediatorListener.class, l);
	}

	public void fireTitleChanged(String title) {
		FrameEvent e = new FrameEvent(this, title);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFrameMediatorListener.class) {
				((IFrameMediatorListener) listeners[i + 1]).titleChanged(e);
			}
		}
		
	}

	public void fireStatusMessageChanged(String statusMessage) {
		FrameEvent e = new FrameEvent(this, statusMessage);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFrameMediatorListener.class) {
				((IFrameMediatorListener) listeners[i + 1]).statusMessageChanged(e);
			}
		}
		
	}

	public void fireTaskStatusChanged() {
		FrameEvent e = new FrameEvent(this);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFrameMediatorListener.class) {
				((IFrameMediatorListener) listeners[i + 1]).taskStatusChanged(e);
			}
		}
		
	}

	public void fireVisibilityChanged(boolean visible) {
		FrameEvent e = new FrameEvent(this, visible);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFrameMediatorListener.class) {
				((IFrameMediatorListener) listeners[i + 1]).visibilityChanged(e);
			}
		}
		
	}

	public void fireLayoutChanged() {
		FrameEvent e = new FrameEvent(this);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFrameMediatorListener.class) {
				((IFrameMediatorListener) listeners[i + 1]).layoutChanged(e);
			}
		}
		
	}
	
	public void fireClosed() {
		FrameEvent e = new FrameEvent(this);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFrameMediatorListener.class) {
				((IFrameMediatorListener) listeners[i + 1]).closed(e);
			}
		}
		
	}
	
	public void fireToolBarVisibilityChanged(boolean visible) {
		FrameEvent e = new FrameEvent(this, visible);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IFrameMediatorListener.class) {
				((IFrameMediatorListener) listeners[i + 1]).toolBarVisibilityChanged(e);
			}
		}
	}
	
	
	/************************* container callbacks **************/

	public void extendMenu(IContainer container) {
		// overwrite this method
	}

	public void extendToolBar(IContainer container) {
		// overwrite this method
	}
	
	public void initFrame(IContainer container) {
		// overwrite this method
	}
	/************************************************************/
	
	public MouseListener getMouseTooltipHandler() {
		return tooltipMouseHandler;
	}

	public void registerDockable(Dockable dockable) {
		// TODO Auto-generated method stub
		
	}

	
}