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

import java.util.HashMap;
import java.util.Map;
import java.util.Observer;

import org.columba.core.xml.XmlElement;

/**
 * Every interest for an XmlElement using XmlElementObservable should
 * be registered here. This is the central place.
 * 
 * Don't use XmlElementObservable directly. 
 * 
 * This makes it much more convienient, because you don't have to know,
 * where the XmlElement was instanciated (who owns it).
 * 
 * Example of registering interest on XmlElement changes:
 * 
 * <pre>
 *  ConfigObservable.register(xmlElement, observer);
 * </pre>
 * 
 * Example of notifying all observers:
 * 
 * <pre>
 *  ConfigObservableManager.notifyObservers(xmlElement);
 * </pre>
 * 
 * There a good introduction for the Observable/Observer pattern in
 * Model/View/Controller based applications at www.javaworld.com:
 * - http://www.javaworld.com/javaworld/jw-10-1996/jw-10-howto.html
 * 
 * @see org.columba.core.config.XmlElementObservable
 * @see org.columba.mail.gui.config.general.MailOptionsDialog#initObservables()
 * @see org.columba.mail.gui.table.util.MarkAsReadTimer
 * 
 * @author fdietz
 * 
 */
public class ConfigObservableManager {

	/**
	 * associate XmlElement with List
	 */
	static Map map;

	/**
	 * initialize hashmap
	 */
	static {
		map = new HashMap();
	}

	/**
	 * Register interest on changes.
	 * 
	 * @param element		XmlElement you want to watch changes 
	 * @param observer		Observer watching the changes
	 */
	public static void register(XmlElement element, Observer observer) {

		if (map.containsKey(element)) {
			// this XmlElement is already observed by someone else

			XmlElementObservable observable =
				(XmlElementObservable) map.get(element);

			// add another observer to this observable
			observable.addObserver(observer);

		} else {
			// this is the first time someone shows interest
			// -> create a new Observable for this XmlElement

			XmlElementObservable observable = new XmlElementObservable(element);

			// add observer to observable
			observable.addObserver(observer);
			
			map.put(element, observable);
		}
	}

	/**
	 * Notify XmlElement changes to all Observers.
	 * 
	 * @param element	changed XmlElement
	 */
	public static void notifyObservers(XmlElement element) {
		// if someone is interested in these changes
		if (map.containsKey(element)) {
			XmlElementObservable observable =
				(XmlElementObservable) map.get(element);

			// notify all observers
			observable.notifyAll();
		}
	}

	/**
	 * Add Observable for this XmlElement
	 * 
	 * @param element	element which is going to be observed
	 */
	public static XmlElementObservable addObservable(XmlElement element) {

		if (map.containsKey(element)) {
			return (XmlElementObservable) map.get(element);
		} else {
			// if no observable for this element exists

			// create Observable for this element
			XmlElementObservable observable = new XmlElementObservable(element);

			map.put(element, observable);

			return observable;
		}

	}

}
