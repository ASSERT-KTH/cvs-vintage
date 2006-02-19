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
package org.columba.calendar.ui.frame;

import java.io.InputStream;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;

import org.columba.api.gui.frame.IContainer;
import org.columba.calendar.ui.action.ActionFactory;
import org.columba.calendar.ui.calendar.MainCalendarController;
import org.columba.calendar.ui.calendar.api.ICalendarView;
import org.columba.calendar.ui.frame.api.ICalendarMediator;
import org.columba.calendar.ui.list.CalendarTreeController;
import org.columba.calendar.ui.list.api.ICalendarListView;
import org.columba.calendar.ui.navigation.NavigationController;
import org.columba.calendar.ui.navigation.api.ICalendarNavigationView;
import org.columba.calendar.ui.navigation.api.SelectionChangedEvent;
import org.columba.calendar.ui.navigation.api.SelectionChangedListener;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.docking.DockableView;
import org.columba.core.gui.frame.DockFrameController;
import org.flexdock.docking.DockingConstants;

/**
 * @author fdietz
 * 
 */
public class CalendarFrameMediator extends DockFrameController implements
		ICalendarMediator {

	public static final String PROP_FILTERED = "filterRow";

	private ICalendarListView listController;

	public ICalendarView calendarController;

	private ICalendarNavigationView navigationController;

	private DockableView listPanel;

	private DockableView calendarPanel;

	private DockableView navigationPanel;

	/**
	 * @param viewItem
	 */
	public CalendarFrameMediator(ViewItem viewItem) {
		super(viewItem);

		// TestDataGenerator.generateTestData();

		calendarController = new MainCalendarController(new ActionFactory(this));

		navigationController = new NavigationController();

		navigationController
				.addSelectionChangedListener(new SelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent object) {
						calendarController.setVisibleDateRange(object
								.getDateRange());

					}
				});

		listController = new CalendarTreeController(this);

		registerDockables();

	}

	public void registerDockables() {
		// init dockable panels
		listPanel = new DockableView("calendar_tree", "Calendar");
		JScrollPane treeScrollPane = new JScrollPane(listController.getView());
		treeScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		listPanel.setContentPane(treeScrollPane);

		navigationPanel = new DockableView("navigation", "Navigation");
		JScrollPane tableScrollPane = new JScrollPane(navigationController
				.getView());
		tableScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		navigationPanel.setContentPane(tableScrollPane);

		calendarPanel = new DockableView("main_calendar", "Main Calendar");

		calendarPanel.setContentPane(calendarController.getView());

	}

	/**
	 * @see org.columba.core.gui.frame.DockFrameController#loadDefaultPosition()
	 */
	public void loadDefaultPosition() {

		super.dock(calendarPanel, DockingConstants.CENTER_REGION);
		calendarPanel.dock(listPanel, DockingConstants.WEST_REGION, 0.2f);
		listPanel.dock(navigationPanel, DockingConstants.SOUTH_REGION, 0.2f);

		super.setSplitProportion(listPanel, 0.2f);
		super.setSplitProportion(calendarPanel, 0.2f);
	}

	public String[] getDockableIds() {

		return new String[] { "calendar_tree", "navigation", "main_calendar" };
	}

	/** *********************** container callbacks ************* */

	public void extendMenu(IContainer container) {
		InputStream is = this.getClass().getResourceAsStream(
				"/org/columba/calendar/action/menu.xml");
		getContainer().extendMenu(this, is);
	}

	public void extendToolBar(IContainer container) {

		InputStream is2 = this.getClass().getResourceAsStream(
				"/org/columba/calendar/action/toolbar.xml");
		getContainer().extendToolbar(this, is2);
	}

	public void showDayView() {

		calendarController.setViewMode(MainCalendarController.VIEW_MODE_DAY);

		navigationController
				.setSelectionMode(NavigationController.SELECTION_MODE_DAY);

	}

	public void showWeekView() {
		calendarController.setViewMode(MainCalendarController.VIEW_MODE_WEEK);

		navigationController
				.setSelectionMode(NavigationController.SELECTION_MODE_WEEK);

	}

	public void showWorkWeekView() {
		calendarController
				.setViewMode(MainCalendarController.VIEW_MODE_WORK_WEEK);

		navigationController
				.setSelectionMode(NavigationController.SELECTION_MODE_WORK_WEEK);

	}

	public void showMonthView() {
		calendarController.setViewMode(MainCalendarController.VIEW_MODE_MONTH);

		navigationController
				.setSelectionMode(NavigationController.SELECTION_MODE_MONTH);

	}

	public ICalendarView getCalendarView() {
		return calendarController;
	}

	public ICalendarListView getListView() {
		return listController;
	}

	public ICalendarNavigationView getNavigationView() {
		return navigationController;
	}

}