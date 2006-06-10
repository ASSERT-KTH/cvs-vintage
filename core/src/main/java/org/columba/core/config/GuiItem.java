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
 * User Interface specific options, including the Look&Feel, font settings,
 * toolbar settings.
 *  
 * @author fdietz
 */

// 
// options.xml:
//
//<gui>
// <theme name="Plastic" theme="Experience Blue"></theme>
// <fonts overwrite="false">
//  <text name="Default" size="12"></text>
//  <main name="Default" size="12"></main>
// </fonts>
// <toolbar text_position="false" enable_icon="true" enable_text="true"></toolbar>
//</gui>

public class GuiItem extends DefaultItem {
	
	public final static String THEME = "theme";	
	public final static String NAME = "name";
	
	public final static String FONT = "fonts";
	public final static String FONT_MAIN = "fonts/main";
	public final static String FONT_TEXT = "fonts/text";
	public final static String OVERWRITE_BOOL = "overwrite"; 
	public final static String SIZE_INT = "size";
	
	public final static String TOOLBAR = "toolbar";
	public final static String TEXT_POSITION_BOOL = "text_position";
	public final static String ENABLE_ICON_BOOL = "enable_icon";
	public final static String ENABLE_TEXT_BOOL = "enable_text";
	
    public GuiItem(XmlElement root) {
        super(root);
    }
}
