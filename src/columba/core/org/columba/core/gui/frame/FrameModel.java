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

	/**
	 * list of frame controllers
	 */
	protected static List activeFrameCtrls;
	
	/**
	 * viewlist xml treenode
	 */
	protected static XmlElement viewList =
		Config.get("options").getElement("/options/gui/viewlist");
	
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
	 * Stores view settings for all currently open views and then
	 * closes them.
	 * Is called when exiting Columba.
	 */
	public static void saveAndCloseAll() {
		// Signal to the close method to react differently
		isSavingAll = true;
		
		// clear view list. Gets filled with currently open views below
		viewList.removeAllElements();

		for (Iterator it = activeFrameCtrls.iterator(); it.hasNext();) {
			AbstractFrameController c = (AbstractFrameController) it.next();
			ViewItem v = c.getViewItem();
			// store current view settings (gets saved in view list)
			viewList.addElement(v.getRoot());
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
			if (activeFrameCtrls.size() == 1) {
				// last frame
				//  -> exit Columba
				viewList.removeAllElements();
				ViewItem v = c.getViewItem();
				// store view settings
				viewList.addElement(v.getRoot());
				// shut down Columba 
				MainInterface.shutdownManager.shutdown();
			} else
				// just remove reference, since the view is no longer shown
				activeFrameCtrls.remove(c);
		}
	}

	/**
	 * Opens a view of a given type, i.e. with a specific id.
	 * @param id		id specifying view type,
	 * 					e.g. "ThreePaneMail" or "Addressbook"
	 * @return			Frame controller for the given view type
	 */
	public static AbstractFrameController openView(String id) {
		/*
		XmlElement view = new XmlElement("view");
		view.addAttribute("id", id);
		XmlElement window = new XmlElement("window");
		window.addAttribute("x", "0");
		window.addAttribute("y", "0");
		window.addAttribute("width", "900");
		window.addAttribute("height", "700");
		window.addAttribute("maximized", "true");
		view.addElement(window);
		viewList.addElement(view);
		*/

		/*
		 * Create a frame controller for this view
		 * (without specifying view settings, defaults will be used) 
		 */
		AbstractFrameController controller = createFrameController(id, null);
		
		// Display the view and return reference
		controller.openView();
		return controller;
	}

}