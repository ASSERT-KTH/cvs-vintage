/*
 * Created on 05.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.core.gui.frame;

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
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class FrameModel {

	protected static Vector list;
	protected static XmlElement viewList =
		Config.get("options").getElement("/options/gui/viewlist");

	public FrameModel() {

		list = new Vector();

		for (int i = 0; i < viewList.count(); i++) {
			XmlElement view = viewList.getElement(i);
			String id = view.getAttribute("id");

			ColumbaLogger.log.debug("id=" + id);

			AbstractFrameController c =
				createFrameController(id, new ViewItem(view));

			c.openView();

		}
	}

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

	public static void saveAll() {

		viewList.removeAllElements();

		for (int i = 0; i < list.size(); i++) {
			AbstractFrameController c = (AbstractFrameController) list.get(i);
			ViewItem v = c.getViewItem();

			viewList.addElement(v.getRoot());

			c.close();
		}
	}

	public static void unregister(AbstractFrameController c) {

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

	public static void openView(String id) {
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

		AbstractFrameController c = createFrameController(id, null);

		c.openView();
	}
}