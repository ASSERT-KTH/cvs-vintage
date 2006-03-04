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
package org.columba.calendar.ui.action;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Iterator;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.parser.CalendarImporter;
import org.columba.calendar.store.CalendarStoreFactory;
import org.columba.calendar.ui.frame.CalendarFrameMediator;
import org.columba.calendar.ui.list.api.ICalendarListView;
import org.columba.core.gui.action.AbstractColumbaAction;

/**
 * Import all calendar events into selected calendar.
 * <p>
 * User is prompted with an open file dialog to select one or
 * multiple iCal file.
 * 
 * @author fdietz
 *
 */
public class ImportCalendarAction extends AbstractColumbaAction {

	public ImportCalendarAction(IFrameMediator frameMediator) {
		super(frameMediator, "Import Calendar");
	}

	public void actionPerformed(ActionEvent e) {
		CalendarFrameMediator m = (CalendarFrameMediator) getFrameMediator();
		ICalendarListView list = m.getListView();
		String calendarId = list.getSelectedId();
		
		if ( calendarId == null ) {
			JOptionPane.showMessageDialog(null, "No calendar for import selected.");
			return;
		}
		
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileHidingEnabled(false);

		if (fc.showOpenDialog(frameMediator.getContainer().getFrame()) == JFileChooser.APPROVE_OPTION) {
			File[] sourceFiles = fc.getSelectedFiles();

			if (sourceFiles.length >= 1) {
				for ( int i=0; i<sourceFiles.length; i++) {
					try {
						Iterator<IEvent> it = new CalendarImporter().importCalendar(sourceFiles[i]);
						
						while (it.hasNext()) {
							IEvent event = it.next();
							event.setCalendar(calendarId);
							
							CalendarStoreFactory.getInstance().getLocaleStore().add(event);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		}
	}

}
