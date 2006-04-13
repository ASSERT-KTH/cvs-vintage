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

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.calendar.base.api.IActivity;
import org.columba.calendar.command.CalendarCommandReference;
import org.columba.calendar.command.DeleteEventCommand;
import org.columba.calendar.store.CalendarStoreFactory;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.ui.calendar.api.ActivitySelectionChangedEvent;
import org.columba.calendar.ui.calendar.api.IActivitySelectionChangedListener;
import org.columba.calendar.ui.calendar.api.ICalendarView;
import org.columba.calendar.ui.frame.api.ICalendarMediator;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;

/**
 * Delete selected activity.
 * 
 * @author fdietz
 */

public class DeleteActivityAction extends AbstractColumbaAction implements
		IActivitySelectionChangedListener {

	public DeleteActivityAction(IFrameMediator frameMediator) {
		super(frameMediator, "Remove Activity");

		putValue(AbstractColumbaAction.TOOLBAR_NAME, "Remove");
		setShowToolBarText(true);

		putValue(AbstractColumbaAction.SMALL_ICON,  ImageLoader.getSmallIcon(IconKeys.EDIT_DELETE));
		putValue(AbstractColumbaAction.LARGE_ICON,  ImageLoader.getIcon(IconKeys.EDIT_DELETE));
	
		
		setEnabled(false);

		ICalendarMediator m = (ICalendarMediator) getFrameMediator();
		m.getCalendarView().addSelectionChangedListener(this);
	}

	public void actionPerformed(ActionEvent e) {
		ICalendarMediator m = (ICalendarMediator) getFrameMediator();

		ICalendarView c = m.getCalendarView();

		IActivity activity = c.getSelectedActivity();

		ICalendarStore store = CalendarStoreFactory.getInstance()
				.getLocaleStore();

		Command command = new DeleteEventCommand(new CalendarCommandReference(
				store, activity));

		CommandProcessor.getInstance().addOp(command);

	}

	public void selectionChanged(ActivitySelectionChangedEvent event) {
		if (event.getSelection().length == 0)
			setEnabled(false);
		else
			setEnabled(true);

	}

}
