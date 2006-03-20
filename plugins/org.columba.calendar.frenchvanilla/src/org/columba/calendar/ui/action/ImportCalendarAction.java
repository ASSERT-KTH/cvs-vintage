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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.calendar.base.api.ICalendarItem;
import org.columba.calendar.command.CalendarCommandReference;
import org.columba.calendar.command.ImportCalendarCommand;
import org.columba.calendar.store.CalendarStoreFactory;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.ui.frame.api.ICalendarMediator;
import org.columba.calendar.ui.list.api.CalendarSelectionChangedEvent;
import org.columba.calendar.ui.list.api.ICalendarListView;
import org.columba.calendar.ui.list.api.ICalendarSelectionChangedListener;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;

/**
 * Import all calendar events into selected calendar.
 * <p>
 * User is prompted with an open file dialog to select one or multiple iCal
 * file.
 * 
 * @author fdietz
 * 
 */
public class ImportCalendarAction extends AbstractColumbaAction implements
		ICalendarSelectionChangedListener {

	public ImportCalendarAction(IFrameMediator frameMediator) {
		super(frameMediator, "Import Calendar");

		setEnabled(false);

		ICalendarMediator m = (ICalendarMediator) getFrameMediator();
		ICalendarListView list = m.getListView();

		list.addSelectionChangedListener(this);

	}

	public void actionPerformed(ActionEvent e) {
		ICalendarMediator m = (ICalendarMediator) getFrameMediator();
		ICalendarListView list = m.getListView();

		// get selected calendar id
		ICalendarItem calendar = list.getSelected();

		if (calendar == null) {
			JOptionPane.showMessageDialog(null,
					"No calendar for import selected.");
			return;
		}

		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.setFileHidingEnabled(false);

		if (fc.showOpenDialog(frameMediator.getContainer().getFrame()) == JFileChooser.APPROVE_OPTION) {
			File[] sourceFiles = fc.getSelectedFiles();

			if (sourceFiles.length >= 1) {
				ICalendarStore store = CalendarStoreFactory.getInstance()
						.getLocaleStore();

				Command command = new ImportCalendarCommand(
						new CalendarCommandReference(store, calendar), sourceFiles);

				CommandProcessor.getInstance().addOp(command);

			}
		}
	}

	public void selectionChanged(CalendarSelectionChangedEvent event) {
		if (event.getSelection() != null)
			setEnabled(true);
		else
			setEnabled(false);

	}
}
