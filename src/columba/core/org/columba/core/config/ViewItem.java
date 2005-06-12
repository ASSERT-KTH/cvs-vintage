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
package org.columba.core.config;

import org.columba.core.xml.XmlElement;

/**
 * View configuration item includes window properties, like position and
 * dimensions, toolbar properties, splitpane position.
 * 
 * @author fdietz
 */

//
// views.xml
//
//<view header="100" header_enabled="true" id="ThreePaneMail" 
//	  infopanel="true" main="100">
// <window maximized="true" height="721" width="1034" y="26" x="0"></window>
// <toolbars infopanel="true" main="true"></toolbars>
// <splitpanes header="200" header_enabled="true" main="283" attachment="100"></splitpanes>
//</view>

public class ViewItem extends DefaultItem {

	public static final String ID = "id";
	public static final String VIEW = "view";
	
	public final static String WINDOW = "window";

	public final static String MAXIMIZED_BOOL = "maximized";

	public final static String HEIGHT_INT = "height";

	public final static String WIDTH_INT = "width";

	public final static String POSITION_X_INT = "x";

	public final static String POSITION_Y_INT = "y";

	public final static String TOOLBARS = "toolbars";

	public final static String MAIN_BOOL = "main";

	public final static String INFOPANEL_BOOL = "infopanel";

	public final static String SPLITPANES = "splitpanes";

	public final static String HEADER_INT = "header";

	public ViewItem(XmlElement root) {
		super(root);
	}

	public static ViewItem createDefault(String id) {

		// initialize default view options
		XmlElement defaultView = new XmlElement(ViewItem.VIEW);
		XmlElement window = new XmlElement(WINDOW);
		// tstich: Default values are set in the loadPostions() method
		defaultView.addElement(window);

		XmlElement toolbars = new XmlElement(TOOLBARS);
		toolbars.addAttribute(MAIN_BOOL, "true");
		defaultView.addElement(toolbars);

		defaultView.addAttribute(ViewItem.ID, id);
		
		return new ViewItem(defaultView);
	}
}