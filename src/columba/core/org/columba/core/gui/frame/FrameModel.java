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

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.columba.core.config.ViewItem;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.plugin.PluginLoadingFailedException;
import org.columba.core.pluginhandler.FramePluginHandler;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.core.xml.XmlElement;

/**
 * FrameModel manages all frames. It keeps a list of every controller. Its also
 * the place to create a new frame, or save and close all frames at once.
 * 
 * Frame controllers are plugins.
 * 
 * @see FramePluginHandler
 * 
 * @author fdietz
 */
public class FrameModel {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.frame");

	/** list of frame controllers */
	protected List activeFrameCtrls = new LinkedList();

	/** viewlist xml treenode */
	protected XmlElement viewList = MainInterface.config.get("options")
			.getElement("/options/gui/viewlist");

	/** Default view specifications to be used when opening a new view */
	protected XmlElement defaultViews = MainInterface.config.get("options")
			.getElement("/options/gui/defaultviews");

	protected FramePluginHandler handler;

	/**
	 * we cache instances for later re-use
	 */
	protected Map frameMediatorCache;

	/**
	 * Obtains a reference to the frame plugin handler and registers a shutdown
	 * hook with the ShutdownManager.
	 */
	public FrameModel() {

		frameMediatorCache = new HashMap();

		// get plugin handler for handling frames
		try {
			handler = (FramePluginHandler) MainInterface.pluginManager
					.getHandler("org.columba.core.frame");
		} catch (PluginHandlerNotFoundException ex) {
			throw new RuntimeException(ex);
		}

		//this is executed on shutdown: store all open frames so that they
		//can be restored on the next start
		ShutdownManager.getShutdownManager().register(new Runnable() {
			public void run() {
				storeViews();
			}
		});
	}

	/**
	 * Close all frames and re-open them again.
	 * <p>
	 * This is necessary when updating translations, adding new plugins which
	 * extend the menu and probably also look and feel changes.
	 *  
	 */
	public void refresh() {
		storeViews();
		openStoredViews();
	}

	/**
	 * Store all open frames so that they can be restored on next startup.
	 *  
	 */
	public void storeViews() {
		//used to temporarily store the values while the original
		//viewList gets modified by the close method
		List newViewList = new LinkedList();

		ViewItem v;

		//we cannot use an iterator here because the close method
		//manipulates the list
		while (activeFrameCtrls.size() > 0) {
			Container c = (Container) activeFrameCtrls.get(0);
			v = c.getViewItem();

			//store every open frame in our temporary list
			newViewList.add(v.getRoot());

			//close every open frame
			c.close();
		}

		//if not we haven't actually closed a frame, leave viewList as is
		if (newViewList.size() > 0) {
			//the close method manipulates the viewList so we have to
			//remove the existing element and fill in our temporarily
			//stored ones
			viewList.removeAllElements();

			for (Iterator it = newViewList.iterator(); it.hasNext();) {
				viewList.addElement((XmlElement) it.next());
			}
		}
	}

	/**
	 * Opens all views stored in the configuration.
	 */
	public void openStoredViews() {
		// load all frames from configuration file
		for (int i = 0; i < viewList.count(); i++) {
			// get element from view list
			XmlElement view = viewList.getElement(i);
			String id = view.getAttribute("id");

			// create frame controller for this view...
			FrameMediator c;
			try {
				c = createFrameController(new ViewItem(view));
			} catch (PluginLoadingFailedException plfe) {
				//should not occur
				continue;
			}

			// ...and display it
			/*
			 * c.openView();
			 */

		}

		/*
		 * if (activeFrameCtrls.size() == 0) { try { openView("ThreePaneMail"); }
		 * catch (PluginLoadingFailedException plfe) {} //should not occur }
		 */
	}

	/**
	 * Returns an array of all open frames.
	 */
	public Container[] getOpenFrames() {
		return (Container[]) activeFrameCtrls.toArray(new Container[0]);
	}

	/**
	 * Get active/focused frame mediator.
	 * 
	 * @return active frame mediator
	 */
	public Container getActiveFrameMediator() {
		Iterator it = activeFrameCtrls.iterator();
		while (it.hasNext()) {
			Container m = (Container) it.next();
			JFrame frame = m.getFrame();
			if (frame.isActive())
				return m;
		}

		return null;
	}

	/**
	 * Get active/focused JFrame.
	 * 
	 * @return active frame
	 */
	public JFrame getActiveFrame() {
		Container m = getActiveFrameMediator();
		if (m != null)
			return m.getFrame();

		// fall-back
		return new JFrame();
	}

	protected XmlElement createDefaultConfiguration(String id) {
		/*
		 * *20030831, karlpeder* Moved code here from constructor XmlElement
		 * child = (XmlElement) defaultView.clone(); child.addAttribute("id",
		 * id);
		 */

		// initialize default view options
		XmlElement defaultView = new XmlElement("view");
		XmlElement window = new XmlElement("window");
		window.addAttribute("x", "0");
		window.addAttribute("y", "0");
		window.addAttribute("width", "640");
		window.addAttribute("height", "480");
		window.addAttribute("maximized", "true");
		defaultView.addElement(window);

		XmlElement toolbars = new XmlElement("toolbars");
		toolbars.addAttribute("main", "true");
		defaultView.addElement(toolbars);

		defaultView.addAttribute("id", id);

		return defaultView;
	}

	/**
	 * Create new frame controller. FrameControllers are plugins.
	 * 
	 * @see FramePluginHandler
	 * 
	 * @param id
	 *            controller ID
	 * @param viewItem
	 *            ViewItem containing frame properties
	 * 
	 * @return frame controller
	 */
	protected FrameMediator createFrameController(Container c, ViewItem viewItem)
			throws PluginLoadingFailedException {

		String id = viewItem.get("id");

		//	save old framemediator in cache (use containers's old id)
		frameMediatorCache.put(c.getViewItem().get("id"), c.getFrameMediator());

		FrameMediator frame = null;
		if (frameMediatorCache.containsKey(id)) {
			LOG.fine("use cached instance " + id);

			// found cached instance
			// -> re-use this instance and remove it from cache
			frame = (FrameMediator) frameMediatorCache.remove(id);
		} else {
			LOG.fine("create new instance " + id);
			Object[] args = { c, viewItem };
			// create new instance
			// -> get frame controller using the plugin handler found above
			frame = (FrameMediator) handler.getPlugin(id, args);
		}

		c.switchFrameMediator(frame);

		return frame;
	}

	protected FrameMediator createFrameController(ViewItem viewItem)
			throws PluginLoadingFailedException {

		String id = viewItem.get("id");

		// create new default container
		boolean newContainer = false;

		Container c = new DefaultContainer(viewItem);

		FrameMediator frame = null;
		if (frameMediatorCache.containsKey(id)) {
			LOG.fine("use cached instance " + id);

			// found cached instance
			// -> re-use this instance and remove it from cache
			frame = (FrameMediator) frameMediatorCache.remove(id);
		} else {
			LOG.fine("create new instance " + id);
			Object[] args = { c, viewItem };
			// create new instance
			// -> get frame controller using the plugin handler found above
			frame = (FrameMediator) handler.getPlugin(id, args);
		}

		c.setFrameMediator(frame);

		activeFrameCtrls.add(c);

		return frame;
	}

	/**
	 * Opens a view of a given type, i.e. with a specific id.
	 * 
	 * @param id
	 *            id specifying view type, e.g. "ThreePaneMail" or "Addressbook"
	 * @return Frame controller for the given view type
	 */
	public FrameMediator openView(String id)
			throws PluginLoadingFailedException {
		// look for default view settings (if not found, null is returned)
		ViewItem view = loadDefaultView(id);

		if (view == null)
			view = new ViewItem(createDefaultConfiguration(id));

		// Create a frame controller for this view
		// view = null => defaults specified by frame controller is used
		FrameMediator controller = createFrameController(view);

		return controller;
	}

	public FrameMediator switchView(Container c, String id)
			throws PluginLoadingFailedException {
		// look for default view settings (if not found, null is returned)
		ViewItem view = loadDefaultView(id);

		if (view == null)
			view = new ViewItem(createDefaultConfiguration(id));

		// Create a frame controller for this view
		FrameMediator controller = createFrameController(c, view);

		return controller;
	}

	/**
	 * Gets default view settings for a given view type
	 * 
	 * @param id
	 *            id specifying view type
	 * @return View settings
	 */
	protected ViewItem loadDefaultView(String id) {
		// If defaultViews doesn't exist, create it (backward compatibility)
		if (defaultViews == null) {
			XmlElement gui = MainInterface.config.get("options").getElement(
					"/options/gui");
			defaultViews = new XmlElement("defaultviews");
			gui.addElement(defaultViews);
		}

		// search through defaultViews to get settings for given id
		ViewItem view = null;

		for (int i = 0; i < defaultViews.count(); i++) {
			XmlElement child = defaultViews.getElement(i);
			String childId = child.getAttribute("id");

			if ((childId != null) && childId.equals(id)) {
				view = new ViewItem(child);

				break;
			}
		}

		return view;
	}

	/**
	 * Saves default view settings for given view type. These will be used as
	 * startup values next a view of this type is opened. Though, views opened
	 * at startup will use settings from viewlist instead.
	 * 
	 * Only one set of settings are stored for each view id.
	 * 
	 * @param view
	 *            view settings to be stored
	 */
	protected void saveDefaultView(ViewItem view) {
		if (view == null) {
			return; // nothing to save
		}

		String id = view.get("id");

		// removed previous default values
		ViewItem oldView = loadDefaultView(id);

		if (oldView != null) {
			defaultViews.removeElement(oldView.getRoot());
		}

		// store current view settings
		defaultViews.addElement(view.getRoot());
	}

	/**
	 * Called when a frame is closed. The reference is removed from the list of
	 * active (shown) frames. If it's the last open view, the view settings are
	 * stored in the view list.
	 * 
	 * @param c
	 *            Reference to frame controller for the view which is closed
	 */
	public void close(Container c) {
		LOG.fine("Closing container: " + c.getClass().getName());

		// Check if the frame controller has been registered, else do nothing
		if (activeFrameCtrls.contains(c)) {
			ViewItem v = c.getViewItem();

			// save in cache
			frameMediatorCache.put(v.get("id"), c.getFrameMediator());

			saveDefaultView(v);
			activeFrameCtrls.remove(c);

			if (activeFrameCtrls.size() == 0) {
				//this is the last frame so store its data in the viewList
				viewList.removeAllElements();
				viewList.addElement(v.getRoot());

				// shutdown Columba if no frame exists anymore
				if (getOpenFrames().length == 0) {

					ShutdownManager.getShutdownManager().shutdown(0);
				}
			}
		}
	}
}