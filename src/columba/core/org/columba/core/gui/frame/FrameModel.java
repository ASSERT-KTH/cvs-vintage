/*
 * Created on 05.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.frame;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.core.config.Config;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.logging.ColumbaLogger;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.FramePluginHandler;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.xml.XmlElement;

/**
 * 
 * 
 * FrameModel manages all frames. It keeps a list of every
 * controller.
 * 
 * Its also the place to create a new frame, or save and close
 * all frames at once.
 * 
 * Frame controllers are plugins.
 * 
 * @see FramePluginHandler
 * 
 * @author fdietz
 */
public class FrameModel {

	/** list of frame controllers */
	protected static List activeFrameCtrls;

	/** viewlist xml treenode */
	protected static XmlElement viewList =
		Config.get("options").getElement("/options/gui/viewlist");
	
	/** Default view specifications to be used when opening a new view */	
	protected static XmlElement defaultViews =
		Config.get("options").getElement("/options/gui/defaultviews");
	
	/** Used for bookkeeping related to saveAll and close methods */
	private static boolean isSavingAll = false;


	/**
	 * Constructor which initializes static fields for view lists and
	 * creates the views stored in the existing view list using 
	 * createFrameController (used at start-up to display the same
	 * views/windows as when last time Columba was closed).
	 */
	public FrameModel() {
		activeFrameCtrls = new Vector();

		// load all frames from configuration file
		for (int i = 0; i < viewList.count(); i++) {
			// get element from view list
			XmlElement view = viewList.getElement(i);
			String id = view.getAttribute("id");

			ColumbaLogger.log.debug("id=" + id);

			// create frame controller for this view...
			AbstractFrameController c =
				createFrameController(id, new ViewItem(view));
			// ...and display it
			c.openView();

		}
		
		/*
		 * Just for extra security: If no views where stored in view list
		 * (corrupt config file or something?), a mail view is 
		 * opened as default. 
		 */
		if (activeFrameCtrls.size() == 0) {
			ColumbaLogger.log.debug("No views specified - opening mail view as default");
			openView("ThreePaneMail");
		}
	}

	/**
	 * Create new frame controller.
	 * <p>
	 * 
	 * FrameControllers are plugins.
	 * 
	 * @see FramePluginHandler
	 * 
	 * @param id			controller ID
	 * @param viewItem		ViewItem containing frame properties
	 * 
	 * @return				frame controller
	 */
	public static AbstractFrameController createFrameController(
											String id,
											ViewItem viewItem) {
		// get plugin handler for handling frames
		FramePluginHandler handler = null;
		try {

			handler =
				(FramePluginHandler) MainInterface.pluginManager.getHandler(
					"org.columba.core.frame");
		} catch (PluginHandlerNotFoundException ex) {
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
		}

		// get frame controller using the plugin handler found above
		Object[] args = { viewItem };
		AbstractFrameController frame = null;
		try {

			frame = (AbstractFrameController) handler.getPlugin(id, args);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// save reference to frame controller
		activeFrameCtrls.add(frame);

		return frame;
	}

	/**
	 * Opens a view of a given type, i.e. with a specific id.
	 * @param id		id specifying view type,
	 * 					e.g. "ThreePaneMail" or "Addressbook"
	 * @return			Frame controller for the given view type
	 */
	public static AbstractFrameController openView(String id) {

		// look for default view settings (if not found, null is returned)
		ViewItem view = loadDefaultView(id);
		
		// Create a frame controller for this view
		// view = null => defaults specified by frame controller is used
		////AbstractFrameController controller = createFrameController(id, null);
		AbstractFrameController controller = createFrameController(id, view);
		
		// Display the view and return reference
		controller.openView();
		return controller;
	}

	/**
	 * Gets default view settings for a given view type
	 * 
	 * @param	id	id specifying view type
	 * @return	View settings	
	 */
	protected static ViewItem loadDefaultView(String id) {
		// If defaultViews doesn't exist, create it (backward compatibility)
		if (defaultViews == null) {
			XmlElement gui = Config.get("options").
					getElement("/options/gui");
			defaultViews = new XmlElement("defaultviews");
			gui.addElement(defaultViews);
		}			
		// search through defaultViews to get settings for given id
		ViewItem view = null;
		for (int i=0; i<defaultViews.count(); i++) {
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
	 * Saves default view settings for given view type. These
	 * will be used as startup values next a view of this type is 
	 * opened. Though, views opened at startup will use settings
	 * from viewlist instead.
	 * 
	 * Only one set of settings are stored for each view id.
	 * 
	 * @param	view	view settings to be stored
	 */
	protected static void saveDefaultView(ViewItem view) {
		
		if (view == null)
			return;		// nothing to save
			
		String id = view.get("id");
		
		// removed previous default values
		ViewItem oldView = loadDefaultView(id);
		if (oldView != null)
			defaultViews.removeElement(oldView.getRoot());
		// store current view settings
		defaultViews.addElement(view.getRoot());
	}

	/**
	 * Stores view settings for all currently open views and then
	 * closes them.
	 * Is called when exiting Columba.
	 */
	public static void saveAndCloseAll() {
		// Signal to the close method to react differently
		isSavingAll = true;
		
		viewList.removeAllElements();

		// store view settings and close all open views
		for (Iterator it = activeFrameCtrls.iterator(); it.hasNext();) {
			AbstractFrameController c = (AbstractFrameController) it.next();
			ViewItem v = c.getViewItem();
			// store current view settings
			viewList.addElement(v.getRoot());
			saveDefaultView(v);
			/*
			 * Close the view. This will also call the close method below
			 * via the frame controllers close method. Since the isSavingAll
			 * flag is set, this will do nothing  
			 */
			c.close();
		}
		
		isSavingAll = false;	// reset flag
	}

	/**
	 * Called when a frame is closed. The reference is removed from the 
	 * list of active (shown) frames.
	 * If it's the last open view, the view settings are stored in the
	 * view list, and Columba is shut down (no more views open => no more
	 * to do actually...)
	 * @param c		Reference to frame controller for the view which is closed
	 */
	public static void close(AbstractFrameController c) {
		/*
		 * *20030828, karlpeder* If we are closing all windows (via saveAll()),
		 * the code below should not be executed for two reasons:
		 * 1) When last frame: The handling of viewList is made in saveAll
		 * 2) When not last: If elements are removed from list, the iterator used
		 *    in saveAll messes up.
		 */
		if (!isSavingAll) { 
			// Check if the frame controller has been registered, else do nothing
			if (activeFrameCtrls.contains(c)) {
				if (activeFrameCtrls.size() == 1) {
					// last frame
					//  -> exit Columba
					viewList.removeAllElements();
					ViewItem v = c.getViewItem();
					// store view settings
					viewList.addElement(v.getRoot());
					saveDefaultView(v);
					// shut down Columba 
					MainInterface.shutdownManager.shutdown();
				} else {
					// just remove reference - and save view settings
					saveDefaultView(c.getViewItem()); 
					activeFrameCtrls.remove(c);
					
				}
			}
		}
	}

}
