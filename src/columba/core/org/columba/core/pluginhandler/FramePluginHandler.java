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
package org.columba.core.pluginhandler;

import java.util.ArrayList;

import org.columba.core.plugin.AbstractPluginHandler;
import org.columba.core.xml.XmlElement;

/**
 * Frames found in package org.columba.core.gui.frame are loaded dynamically.
 * <p>
 * This makes it possible to write a plugin, for the mail component where the
 * frame has a completely different layout.
 * 
 * @author fdietz
 */
public class FramePluginHandler extends AbstractPluginHandler {
	public FramePluginHandler() {
		super("org.columba.core.frame", "org/columba/core/plugin/frame.xml");

		parentNode = getConfig().getRoot().getElement("framelist");
	}

	public String[] getManagedFrames() {
		ArrayList list = new ArrayList();
		for (int i = 0; i < parentNode.count(); i++) {
			XmlElement child = parentNode.getElement(i);
			String managed = child.getAttribute("managed");
			if  (managed == null ) managed = "false";
			
			if (managed.equals("true")) {
				list.add(child.getAttribute("name"));
			}
		}
		return (String[]) list.toArray(new String[0]);
	}
}