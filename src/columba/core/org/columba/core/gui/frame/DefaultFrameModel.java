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

import java.util.Hashtable;

import org.columba.core.config.ViewItem;
import org.columba.core.main.MainInterface;
import org.columba.core.xml.XmlElement;

public abstract class DefaultFrameModel {

	

	protected int nextId = 0;

	public abstract AbstractFrameController createInstance(String id);

	protected XmlElement defaultView;

	public DefaultFrameModel() {
		

		defaultView = new XmlElement("view");
		XmlElement window = new XmlElement("window");
		window.addAttribute("width", "640");
		window.addAttribute("height", "480");
		window.addAttribute("maximized", "false");
		defaultView.addElement(window);

	}

	public AbstractFrameController openView() {

		int id = nextId++;

		AbstractFrameController c = createInstance(new Integer(id).toString());
		c.getView().loadWindowPosition();
		c.getView().setVisible(true);

		return c;
	}

	/**
		 * Registers the View
		 * @param view
		 */
	/*
	protected void register(String id, FrameController controller) {
		controllers.put(id, controller);
		controller.setItem(new ViewItem(createDefaultConfiguration(id)));
		
		MainInterface.frameModelManager.register(controller);
	}
	*/


	protected XmlElement createDefaultConfiguration(String id) {
		XmlElement child = (XmlElement) defaultView.clone();
		child.addAttribute("id", new Integer(id).toString());

		return child;
	}

	void register(String id, AbstractFrameController controller)
	{
	}
	
	
	/**
			 * Unregister the View from the Model
			 * @param view
			 * @return boolean true if there are no more views for the model
			 */
	
	void unregister(String id )
	{
	}
	
	void saveAndUnregister(String id)
	{
	}
	
	/*
	protected void unregister(String id) {
		FrameController controller = (FrameController) controllers.get(id);
		controllers.remove(id);
		defaultView = controller.getItem().getRoot();
	}
	*/

	/*
	public void saveAndUnregister(String id) {
		//		remove from list
		FrameController controller = (FrameController) controllers.get(id);
		controllers.remove(id);

		MainInterface.frameModelManager.unregister(controller);
	}
	*/

}
