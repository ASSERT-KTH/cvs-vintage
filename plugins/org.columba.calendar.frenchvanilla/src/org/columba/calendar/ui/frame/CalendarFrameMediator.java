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
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.calendar.ui.calendar.MainCalendarController;
import org.columba.calendar.ui.navigation.NavigationController;
import org.columba.calendar.ui.navigation.SelectionChangedEvent;
import org.columba.calendar.ui.navigation.SelectionChangedListener;
import org.columba.calendar.ui.tree.CalendarTreeController;
import org.columba.core.config.ViewItem;
import org.columba.core.gui.docking.DockableView;
import org.columba.core.gui.frame.DockFrameController;
import org.flexdock.docking.DockingConstants;

/**
 * @author fdietz
 * 
 */
public class CalendarFrameMediator extends DockFrameController implements
		IFrameMediator {

	public static final String PROP_FILTERED = "filterRow";

	private CalendarTreeController treeController;

	public MainCalendarController mainCalendarController;

	private NavigationController navigationCalendarController;

	private DockableView treePanel;

	private DockableView mainCalendarPanel;

	private DockableView navigationCalendarPanel;

	/**
	 * @param viewItem
	 */
	public CalendarFrameMediator(ViewItem viewItem) {
		super(viewItem);

		// TestDataGenerator.generateTestData();

		mainCalendarController = new MainCalendarController();

		navigationCalendarController = new NavigationController();

		navigationCalendarController
				.addSelectionChangedListener(new SelectionChangedListener() {
					public void selectionChanged(SelectionChangedEvent object) {
						mainCalendarController.setVisibleDateRange(object
								.getDateRange());

					}
				});

		treeController = new CalendarTreeController(this);

		registerDockables();

	}

	public void registerDockables() {
		// init dockable panels
		treePanel = new DockableView("calendar_tree", "Calendar");
		JScrollPane treeScrollPane = new JScrollPane(treeController.getView());
		treeScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		treePanel.setContentPane(treeScrollPane);

		navigationCalendarPanel = new DockableView("navigation", "Navigation");
		JScrollPane tableScrollPane = new JScrollPane(
				navigationCalendarController.getView());
		tableScrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
		navigationCalendarPanel.setContentPane(tableScrollPane);

		mainCalendarPanel = new DockableView("main_calendar", "Main Calendar");

		mainCalendarPanel.setContentPane(mainCalendarController.getView());

	}

	/**
	 * @see org.columba.core.gui.frame.DockFrameController#loadDefaultPosition()
	 */
	public void loadDefaultPosition() {

		super.dock(mainCalendarPanel, DockingConstants.CENTER_REGION);
		mainCalendarPanel.dock(treePanel, DockingConstants.WEST_REGION, 0.2f);
		treePanel.dock(navigationCalendarPanel,
				DockingConstants.SOUTH_REGION, 0.2f);

		super.setSplitProportion(treePanel, 0.2f);
		super.setSplitProportion(mainCalendarPanel, 0.2f);
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

	/**
	 * @see org.columba.core.gui.frame.ContentPane#getComponent()
	 */
	// public JComponent getComponent() {
	// JSplitPane mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
	// mainSplitPane.setDividerLocation(200);
	// // mainSplitPane.setBorder(null);
	//
	// JPanel leftPanel = new JPanel();
	// leftPanel.setLayout(new BorderLayout());
	// mainSplitPane.setLeftComponent(leftPanel);
	//
	// JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
	// leftSplitPane.setBorder(null);
	// leftSplitPane.setDividerLocation(200);
	//
	// leftSplitPane.setTopComponent(treeController.getView());
	//
	// leftSplitPane
	// .setBottomComponent(navigationCalendarController.getView());
	//
	// leftPanel.add(leftSplitPane, BorderLayout.CENTER);
	//
	// mainSplitPane.setRightComponent(mainCalendarController.getView());
	//
	// InputStream is = this.getClass().getResourceAsStream(
	// "/org/columba/calendar/action/menu.xml");
	// getContainer().extendMenu(this, is);
	//
	// InputStream is2 = this.getClass().getResourceAsStream(
	// "/org/columba/calendar/action/toolbar.xml");
	// getContainer().extendToolbar(this, is2);
	//
	// return mainSplitPane;
	// }
	/**
	 * @see org.columba.core.gui.frame.FrameMediator#getContentPane()
	 */
	// public IContentPane getContentPane() {
	// return this;
	// }
	/**
	 * @return Returns the mainCalendarController.
	 */
	public MainCalendarController getMainCalendarController() {
		return mainCalendarController;
	}

	/**
	 * @return Returns the tree.
	 */
	public CalendarTreeController getTreeController() {
		return treeController;
	}

	/**
	 * @return Returns the navigationCalendarController.
	 */
	public NavigationController getNavigationCalendarController() {
		return navigationCalendarController;
	}

	public void goToday() {
		mainCalendarController.goToday();

	}

	public void goBack() {
		mainCalendarController.goBack();

	}

	public void goNext() {
		mainCalendarController.goNext();

	}

	public void showDayView() {

		mainCalendarController
				.setViewMode(MainCalendarController.VIEW_MODE_DAY);

		navigationCalendarController
				.setSelectionMode(NavigationController.SELECTION_MODE_DAY);

	}

	public void showWeekView() {
		mainCalendarController
				.setViewMode(MainCalendarController.VIEW_MODE_WEEK);

		navigationCalendarController
				.setSelectionMode(NavigationController.SELECTION_MODE_WEEK);

	}

	public void showWorkWeekView() {
		mainCalendarController
				.setViewMode(MainCalendarController.VIEW_MODE_WORK_WEEK);

		navigationCalendarController
				.setSelectionMode(NavigationController.SELECTION_MODE_WORK_WEEK);

	}

	public void showMonthView() {
		mainCalendarController
				.setViewMode(MainCalendarController.VIEW_MODE_MONTH);

		navigationCalendarController
				.setSelectionMode(NavigationController.SELECTION_MODE_MONTH);

	}

}