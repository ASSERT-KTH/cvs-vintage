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
 * Gui components should implement this interface, to save
 * their configuration data.
 * <p>
 * Possible applications are the options of the message list,
 * including the sorting order, sorting column, filtering
 * properties.
 * <p>
 * @author fdietz
 */
public interface OptionsSerializer {
    /**
 * Get xml configuration of this component.
 * <p>
 * 
 * Following a simple example of a toolbar configuration:<br>
 * 
 * <pre>
 * <toolbar enabled="true" show_icon="true" show_text="false">
 *  <button name="Cut"/>
 *  <button name="Copy"/>
 *  <button name="Paste"/>
 *  <button name="Delete"/>
 * </toolbar>
 * </pre>
 * 
 * <p>
 * So, this method will return the the top-level xml element
 * <b>toolbar</b>.
 * 
 * @return      top-level xml treenode
 */
    XmlElement saveOptionsToXml();

    /**
 * Load options of this component from xml element.
 * <p>
 * Following the example used above, this element should
 * have the name <b>toolbar</b>.
 * <p>
 * @param element       configuration options node
 */
    void loadOptionsFromXml(XmlElement element);
}
