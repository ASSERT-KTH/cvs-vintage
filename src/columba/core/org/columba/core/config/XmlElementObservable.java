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

import java.util.Observable;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.xml.XmlElement;

/**
 * Observable for a single XmlElement. 
 * 
 * Should be used for gui elements which are interested in 
 * configuration changes.
 * 
 * It has several wrapper methods to change attributes easily
 * which also notify all Observers. So, its recommend to only
 * change XmlElement in using those wrapper methods.
 * 
 * When making bigger changes on XmlElement and probably its
 * subnodes and/or a greater number of attributes at once,
 * you should just change XmlElement directly and manually
 * notify the Observers by calling:
 * 
 * <pre>
 *  ConfigObservableManager.notifyObservers(xmlElement);
 * </pre>
 * 
 *  
 * @see org.columba.core.config.ConfigObservableManager
 * @see org.columba.mail.gui.config.general.MailOptionsDialog#initObservables()
 *
 * @author fdietz
 */
public class XmlElementObservable extends Observable {

	private XmlElement element;
	
	/**
	 * Constructor
	 *  
	 */
	public XmlElementObservable(XmlElement element) {
		super();
		
		this.element = element;
	}
	
	/**
	 * Set or change Attribute of observable XmlElement.
	 * 
	 * Notifies the changes to all Observers.
	 * 
	 * @param key		attribute key
	 * @param value		new attribute value
	 */
	public void addAttribute(String key, String value)
	{
		element.addAttribute(key, value);
				
		// notify all Observers
		setChanged();
		notifyObservers();
		
		ColumbaLogger.log.debug("notify observers of changes...key="+key+" value="+value);
	}

}
