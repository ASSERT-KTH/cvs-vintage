// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.calendar;

import java.io.InputStream;
import java.util.Iterator;

import org.apache.commons.cli.CommandLine;
import org.columba.api.exception.PluginHandlerNotFoundException;
import org.columba.calendar.config.Config;
import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IComponentInfo;
import org.columba.calendar.model.api.IComponentInfoList;
import org.columba.calendar.model.api.IEventInfo;
import org.columba.calendar.store.CalendarStoreFactory;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.StoreException;
import org.columba.calendar.ui.calendar.CalendarHelper;
import org.columba.core.component.IComponentPlugin;
import org.columba.core.plugin.PluginManager;
import org.columba.core.pluginhandler.ActionExtensionHandler;

import com.miginfocom.calendar.activity.Activity;
import com.miginfocom.calendar.activity.ActivityDepository;

/**
 * @author fdietz
 * 
 */
public class CalendarComponent implements IComponentPlugin {

	/**
	 * 
	 */
	public CalendarComponent() throws Exception {
		super();

		try {
			// com.miginfocom.util.LicenseValidator.setLicenseKey("Cu=Frederik_Dietz\nCo=OpenSource\nDm=false\nEx=0\nSignature=302C021408B54A0B041E79362B1951E9FDB9AFEAD0EBFBDD021409F4830B4AD832766388107CB1D38126A9473C0C");
			com.miginfocom.util.LicenseValidator.setLicenseKey(getClass()
					.getResourceAsStream("/license.lic"));

		} catch (IllegalAccessError e) {
			System.out.println("License Code Invalid");

			e.printStackTrace();
		}
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#init()
	 */
	public void init() {

		try {
			InputStream is = this.getClass().getResourceAsStream(
					"/org/columba/calendar/action/action.xml");

			((ActionExtensionHandler) PluginManager.getInstance().getHandler(
					ActionExtensionHandler.NAME)).loadExtensionsFromStream(is);

		} catch (PluginHandlerNotFoundException ex) {
		}

	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#postStartup()
	 */
	public void postStartup() {
		// read all events from calendar store
		ICalendarStore store = CalendarStoreFactory.getInstance()
				.getLocaleStore();

		try {

			IComponentInfoList list = store.getComponentInfoList();
			Iterator<IComponentInfo> it = list.iterator();
			while (it.hasNext()) {
				IComponentInfo item = (IComponentInfo) it.next();

				if (item.getType() == IComponent.TYPE.EVENT) {
					IEventInfo event = (IEventInfo) item;
					Activity act = CalendarHelper.createActivity(event);

					ActivityDepository.getInstance().addBrokedActivity(act,
							CalendarComponent.class);
				}
			}

		} catch (StoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#registerCommandLineArguments()
	 */
	public void registerCommandLineArguments() {
		// TODO Auto-generated method stub

	}

	/**
	 * @see org.columba.core.main.IComponentPlugin#handleCommandLineParameters(org.apache.commons.cli.CommandLine)
	 */
	public void handleCommandLineParameters(CommandLine commandLine) {
		// TODO Auto-generated method stub

	}

}