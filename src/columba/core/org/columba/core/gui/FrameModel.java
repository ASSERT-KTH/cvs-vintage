package org.columba.core.gui;
import java.util.Enumeration;
import java.util.Hashtable;

import org.columba.core.config.ViewItem;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;
/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public abstract class FrameModel {

	protected Hashtable controllers;
	protected XmlElement viewList;
	protected int nextId = 0;

	public FrameModel(XmlElement viewList) {

		this.viewList = viewList;
		controllers = new Hashtable();

		
		for (int i = 0; i < viewList.count(); i++) {
			XmlElement view = viewList.getElement(i);
			String id = view.getAttribute("id");

			/*
			FrameController c = new MailFrameController(id);
			*/

			FrameController c = createInstance(new Integer(id).toString());

			
			c.getView().loadWindowPosition(new ViewItem(view));
			c.getView().setVisible(true);
			register(id, c);

			nextId = Integer.parseInt(id) + 1;
		}
	}

	public abstract FrameController createInstance(String id);

	public void openView() {

		int id = nextId++;
		/*
		MailFrameController c =
			new MailFrameController(new Integer(id).toString());
		*/
		FrameController c = createInstance(new Integer(id).toString());

		c.getView().setVisible(true);
		//c.getView().loadWindowPosition(new ViewItem(child));
		register(new Integer(id).toString(), c);
	}

	/**
	 * Registers the View
	 * @param view
	 */
	public void register(String id, FrameController controller) {
		controllers.put(id, controller);
	}

	protected XmlElement ensureViewConfigurationExists(String key) {
		XmlElement child = getChild(new Integer(key).toString());
		if (child == null) {
			// create new node
			child = new XmlElement("view");
			child.addAttribute("id", new Integer(key).toString());
			XmlElement window = new XmlElement("window");
			window.addAttribute("x", "0");
			window.addAttribute("y", "0");
			window.addAttribute("width", "900");
			window.addAttribute("height", "700");
			window.addAttribute("maximized", "true");
			child.addElement(window);
			/*
			XmlElement toolbars = new XmlElement("toolbars");
			toolbars.addAttribute("show_main", "true");
			toolbars.addAttribute("show_filter", "true");
			toolbars.addAttribute("show_folderinfo", "true");
			child.addElement(toolbars);
			XmlElement splitpanes = new XmlElement("splitpanes");
			splitpanes.addAttribute("main", "200");
			splitpanes.addAttribute("header", "200");
			splitpanes.addAttribute("attachment", "100");
			child.addElement(splitpanes);
			*/
			viewList.addElement(child);
		}
		
		return child;
	}

	public void saveAll() {
		viewList.removeAllElements();
		for (Enumeration e = controllers.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			FrameController frame = (FrameController) controllers.get(key);
			ensureViewConfigurationExists(key);
			controllers.remove(key);
		}
		/*
		saveAndExit();
		*/
	}
	/**
		 * Unregister the View from the Model
		 * @param view
		 * @return boolean true if there are no more views for the model
		 */

	public void unregister(String id) {
		FrameController controller = (FrameController) controllers.get(id);
		if (controllers.size() == 1) {
			// last window closed
			//  close application
			viewList.removeAllElements();
			ensureViewConfigurationExists(id);
			saveWindowPosition(id);
			controllers.remove(id);
			
			MainInterface.shutdownManager.shutdown();
			
			
			/*
			saveAndExit();
			*/
		} else {
			controllers.remove(id);
		}
	}

	protected XmlElement getChild(String id) {
		for (int i = 0; i < viewList.count(); i++) {
			XmlElement view = viewList.getElement(i);
			String str = view.getAttribute("id");
			if (str.equals(id))
				return view;
		}
		return null;
	}

	public void saveWindowPosition(String id) {
		XmlElement child = getChild(id);
		FrameController frame = (FrameController) controllers.get(id);
		frame.getView().saveWindowPosition(new ViewItem(child));
	}
}
