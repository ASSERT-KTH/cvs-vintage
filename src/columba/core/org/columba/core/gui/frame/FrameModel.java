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
	protected static List list;
	
	/**
	 * viewlist xml treenode
	 */
	protected static XmlElement viewList =
		Config.get("options").getElement("/options/gui/viewlist");

	public FrameModel() {

		list = new Vector();

		// load all frames from configuration file
		for (int i = 0; i < viewList.count(); i++) {
			XmlElement view = viewList.getElement(i);
			String id = view.getAttribute("id");

			ColumbaLogger.log.debug("id=" + id);

			AbstractFrameController c =
				createFrameController(id, new ViewItem(view));

			c.openView();

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
		FramePluginHandler handler = null;
		try {

			handler =
				(FramePluginHandler) MainInterface.pluginManager.getHandler(
					"org.columba.core.frame");
		} catch (PluginHandlerNotFoundException ex) {
			NotifyDialog d = new NotifyDialog();
			d.showDialog(ex);
		}

		Object[] args = { viewItem };

		AbstractFrameController frame = null;
		try {

			frame = (AbstractFrameController) handler.getPlugin(id, args);

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		// save FrameController in Vector
		list.add(frame);

		return frame;
	}

	/**
	 * 
	 * Save all open frames and close them.
	 *
	 */
	public static void saveAll() {

		viewList.removeAllElements();
		for (Iterator it = list.iterator(); it.hasNext();) {
			AbstractFrameController c = (AbstractFrameController) it.next();
		// for (int i = 0; i < list.size(); i++) {
			// AbstractFrameController c = (AbstractFrameController) list.get(i);
			ViewItem v = c.getViewItem();

			viewList.addElement(v.getRoot());

			c.close();
			//c.saveAndClose();
		}
	}

	/**
	 * Close specific frame
	 * 
	 * @param c		frame controller to close
	 */
	public static void close(AbstractFrameController c) {
		if (list.size() == 1) {
			// last frame
			//  -> exit Columba
			viewList.removeAllElements();
			ViewItem v = c.getViewItem();

			viewList.addElement(v.getRoot());

			MainInterface.shutdownManager.shutdown();
		} else
			list.remove(c);
	}

	

	/**
	 * 
	 * Open view with specific ID.
	 * 
	 * @param id		controller ID
	 * @return			frame controller
	 */
	public static AbstractFrameController openView(String id) {
		
		AbstractFrameController c = createFrameController(id, null);
		
		c.openView();

		return c;
	}
}