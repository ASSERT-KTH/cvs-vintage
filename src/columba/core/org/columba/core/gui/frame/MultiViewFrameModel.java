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
package org.columba.core.gui.frame;
import java.util.Enumeration;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.action.OpenNewMailWindowAction;
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
public abstract class MultiViewFrameModel extends DefaultFrameModel {

	protected XmlElement viewList;


	public MultiViewFrameModel(XmlElement viewList) {
		super();

		this.viewList = viewList;

		for (int i = 0; i < viewList.count(); i++) {
			XmlElement view = viewList.getElement(i);
			String id = view.getAttribute("id");

			FrameController c = createInstance(new Integer(id).toString());

			c.getView().loadWindowPosition();
			c.getView().setVisible(true);

			nextId = Integer.parseInt(id) + 1;
		}
	}


	protected XmlElement createDefaultConfiguration(String key) {
		XmlElement child; // = getChild(new Integer(key).toString());

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

		return child;
	}

	public void saveAll() {
		for (Enumeration e = controllers.keys(); e.hasMoreElements();) {
			String key = (String) e.nextElement();
			FrameController frame = (FrameController) controllers.get(key);
			frame.close();
		}
		/*
		saveAndExit();
		*/
	}

	/**
		 * Registers the View
		 * @param view
		 */
	protected void register(String id, FrameController controller) {
		controllers.put(id, controller);
		controller.setItem(getViewItem(id));
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


	protected ViewItem getViewItem(String id) {
		XmlElement viewElement = getChild(id);
		if( viewElement == null ) {
			viewElement = createDefaultConfiguration(id);
			viewList.addElement(viewElement);
		}
		
		return new ViewItem(viewElement);
	}

	/**
			 * Unregister the View from the Model
			 * @param view
			 * @return boolean true if there are no more views for the model
			 */
	protected void unregister(String id) {
		FrameController controller = (FrameController) controllers.get(id);
		if (controllers.size() == 1) {
			// last window closed
			//  close application
			
			//viewList.removeAllElements();
			//ensureViewConfigurationExists(id);
			//saveWindowPosition(id);
			controllers.remove(id);
	
			MainInterface.shutdownManager.shutdown();
	
			/*
			saveAndExit();
			*/
		} else {
			controllers.remove(id);
		}
	}
	
	public OpenNewMailWindowAction getOpenWindowAction()
	{
		
		return null;
	}


}
