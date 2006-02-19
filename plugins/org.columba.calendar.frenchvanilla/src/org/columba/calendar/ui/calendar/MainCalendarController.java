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
package org.columba.calendar.ui.calendar;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import org.columba.calendar.model.api.IDateRange;
import org.columba.calendar.ui.action.ActionFactory;
import org.columba.calendar.ui.action.ActivityMovedAction;
import org.columba.calendar.ui.action.EditActivityAction;
import org.columba.calendar.ui.base.Activity;
import org.columba.calendar.ui.base.api.IActivity;
import org.columba.calendar.ui.calendar.api.ICalendarView;

import com.miginfocom.ashape.AShapeUtil;
import com.miginfocom.ashape.DefaultAShapeProvider;
import com.miginfocom.ashape.interaction.InteractionEvent;
import com.miginfocom.ashape.interaction.InteractionListener;
import com.miginfocom.ashape.interaction.MouseKeyInteractor;
import com.miginfocom.ashape.layout.CutEdgeAShapeLayout;
import com.miginfocom.ashape.shapes.AShape;
import com.miginfocom.ashape.shapes.ContainerAShape;
import com.miginfocom.ashape.shapes.DrawAShape;
import com.miginfocom.ashape.shapes.FillAShape;
import com.miginfocom.ashape.shapes.RootAShape;
import com.miginfocom.ashape.shapes.TextAShape;
import com.miginfocom.calendar.activity.renderer.AShapeRenderer;
import com.miginfocom.calendar.activity.view.ActivityView;
import com.miginfocom.calendar.category.Category;
import com.miginfocom.calendar.category.CategoryDepository;
import com.miginfocom.calendar.category.CategoryFilter;
import com.miginfocom.calendar.datearea.ActivityMoveEvent;
import com.miginfocom.calendar.datearea.ActivityMoveListener;
import com.miginfocom.calendar.datearea.DateArea;
import com.miginfocom.calendar.datearea.DefaultDateArea;
import com.miginfocom.calendar.datearea.ThemeDateArea;
import com.miginfocom.calendar.datearea.ThemeDateAreaContainer;
import com.miginfocom.calendar.grid.GridSegment;
import com.miginfocom.calendar.header.CellDecorationRow;
import com.miginfocom.calendar.theme.CalendarTheme;
import com.miginfocom.theme.Theme;
import com.miginfocom.theme.Themes;
import com.miginfocom.util.MigUtil;
import com.miginfocom.util.PropertyKey;
import com.miginfocom.util.dates.DateFormatList;
import com.miginfocom.util.dates.DateRange;
import com.miginfocom.util.dates.DateRangeI;
import com.miginfocom.util.dates.ImmutableDateRange;
import com.miginfocom.util.dates.MutableDateRange;
import com.miginfocom.util.expression.LogicalExpression;
import com.miginfocom.util.filter.ExpressionFilter;
import com.miginfocom.util.filter.Filter;
import com.miginfocom.util.gfx.RoundRectangle;
import com.miginfocom.util.gfx.geometry.AbsRect;
import com.miginfocom.util.gfx.geometry.PlaceRect;
import com.miginfocom.util.gfx.geometry.numbers.AtEnd;
import com.miginfocom.util.gfx.geometry.numbers.AtFixed;
import com.miginfocom.util.gfx.geometry.numbers.AtFraction;
import com.miginfocom.util.gfx.geometry.numbers.AtStart;
import com.miginfocom.util.repetition.DefaultRepetition;
import com.miginfocom.util.states.ToolTipProvider;

/**
 * @author fdietz
 * 
 */
public class MainCalendarController implements InteractionListener,
		ActivityMoveListener, ICalendarView {

	public static final int VIEW_MODE_DAY = 0;

	public static final int VIEW_MODE_WEEK = 1;

	public static final int VIEW_MODE_WORK_WEEK = 2;

	public static final int VIEW_MODE_MONTH = 3;

	public static final String MAIN_WEEKS_CONTEXT = "mainWeeks";

	public static final String MAIN_DAYS_CONTEXT = "mainDays";

	public static final RootAShape VERSHAPE = AShapeUtil
			.createDefault(SwingConstants.VERTICAL);

	public static final RootAShape HORSHAPE = MainCalendarController
			.createTraslucentShapeHorizontal();

	private int currentViewMode = VIEW_MODE_WEEK;

	private ThemeDateAreaContainer view;

	public static final String PROP_FILTERED = "filterRow";

	private IActivity selectedActivity;

	private ActionFactory actionFactory;

	/**
	 * 
	 */
	public MainCalendarController(ActionFactory actionFactory) {
		super();

		this.actionFactory = actionFactory;

		view = new ThemeDateAreaContainer();

		try {
			initThemes();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setViewMode(currentViewMode);

		ToolTipProvider ttp = new ToolTipProvider() {
			public int configureToolTip(JToolTip toolTip, MouseEvent e,
					Object source) {
				if (e.getID() == MouseEvent.MOUSE_MOVED
						&& source instanceof ActivityView) {
					// toolTip.setForeground(Color.DARK_GRAY);
					toolTip.setTipText(((ActivityView) source).getModel()
							.getSummary());
					return ToolTipProvider.SHOW_TOOLTIP;
				} else {
					return ToolTipProvider.HIDE_TOOLTIP;
				}
			}
		};

		view.getDateArea().setToolTipProvider(ttp);

		((DefaultDateArea) view.getDateArea()).addInteractionListener(this);

		((DefaultDateArea) view.getDateArea()).addActivityMoveListener(this);

		// view.getDateArea().setActivitiesSupported(true);

	}

	public IActivity getSelectedActivity() {
		return selectedActivity;
	}

	public JComponent getView() {
		return view;
	}

	/**
	 * 
	 */
	private void initThemes() {

		try {

			InputStream is = getClass().getResourceAsStream(
					"/org/columba/calendar/themes/DemoAppMainWeeks.tme");
			Themes.loadTheme(is, MAIN_WEEKS_CONTEXT, true, true);
			is.close();

			is = getClass().getResourceAsStream(
					"/org/columba/calendar/themes/DemoAppMainDays.tme");
			Themes.loadTheme(is, MAIN_DAYS_CONTEXT, true, true);
			is.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

//		Font defaultFont = UIManager.getFont("Label.font");
//		Color foreground = UIManager.getColor("Label.foreground");
//		Color background = UIManager.getColor("Table.background");
//		Color gridColor = UIManager.getColor("Button.darkShadow");
//
//		Theme theme = Themes.getTheme(MAIN_DAYS_CONTEXT);
//
//
//		// main background color
//		theme.putValue(CalendarTheme.KEY_GENERIC_BACKGROUND, background);
//
//		String mainKey = CalendarTheme.KEY_HEADER_;
//		theme.putValue(mainKey + "West/antiAlias", Boolean.TRUE);
//
//		theme.setInList(mainKey + "West/CellDecorationRows#", 0,
//				new CellDecorationRow(DateRange.RANGE_TYPE_HOUR,
//						new DateFormatList("HH"), null, null, background,
//						foreground, new DefaultRepetition(0, 2), defaultFont));
//
//		theme.removeFromList(mainKey + "West/CellDecorationRows#", 1);
//
//
//		// gridlines
//		AtFixed min = new AtFixed(1);
//		AtFraction preferred = new AtFraction(0.1f); // Preferred can be
//		// absolute or relative
//		AtFixed max = new AtFixed(4);
//		GridSegment segment = new GridSegment(0, min, preferred, max);
//		theme.addToList(CalendarTheme.KEY_GRID_SEGMENTS_ + "PrimaryDim#", 0,
//				segment);
//		theme.removeAllFromList(CalendarTheme.KEY_GRID_SEGMENTS_
//				+ "PrimaryDim#");
//		theme.removeAllFromList(CalendarTheme.KEY_GRID_SEGMENTS_
//				+ "SecondaryDim#");

		view.setThemeContext(MAIN_DAYS_CONTEXT);
	}

	public void setViewMode(int mode) {

		this.currentViewMode = mode;

		ThemeDateArea themeDateArea = (ThemeDateArea) view.getDateArea();
		DefaultAShapeProvider defaultShapeFactory = ((AShapeRenderer) ((DefaultDateArea) themeDateArea)
				.getActivityViewRenderer()).getShapeProvider();

		int viewMode = -1;
		String theme = null;
		int days = -1;
		switch (mode) {
		case VIEW_MODE_DAY:
			viewMode = DateRangeI.RANGE_TYPE_DAY;
			theme = MainCalendarController.MAIN_DAYS_CONTEXT;
			days = 1;
			defaultShapeFactory.setShape(VERSHAPE, null);

			break;
		case VIEW_MODE_WEEK:
			viewMode = DateRangeI.RANGE_TYPE_DAY;
			theme = MainCalendarController.MAIN_DAYS_CONTEXT;
			days = 7;
			defaultShapeFactory.setShape(VERSHAPE, null);

			break;
		case VIEW_MODE_WORK_WEEK:
			viewMode = DateRangeI.RANGE_TYPE_DAY;
			theme = MainCalendarController.MAIN_DAYS_CONTEXT;
			defaultShapeFactory.setShape(VERSHAPE, null);
			days = 5;

			break;
		case VIEW_MODE_MONTH:
			viewMode = DateRangeI.RANGE_TYPE_MONTH;
			theme = MainCalendarController.MAIN_WEEKS_CONTEXT;
			days = 1;
			defaultShapeFactory.setShape(HORSHAPE, null);

			break;
		}

		view.setThemeContext(theme);

		DateRange newVisRange = new DateRange(view.getDateArea()
				.getVisibleDateRange());

		newVisRange.setSize(viewMode, days, MutableDateRange.ALIGN_CENTER_UP);
		view.getDateArea().setVisibleDateRange(newVisRange);

		view.validate();
		view.repaint();

		/*
		 * DefaultAShapeProvider defaultShapeFactory = ((AShapeRenderer)
		 * ((DefaultDateArea) calendar
		 * .getDateArea()).getActivityViewRenderer()).getShapeProvider();
		 * 
		 * defaultShapeFactory.setShape(VERSHAPE, null);
		 */
	}

	/**
	 * Creates the default shape.
	 */
	public static RootAShape createTraslucentShapeHorizontal() {
		Color bgPaint = new Color(255, 200, 200);
		Color bulletPaint = null;

		Color outlinePaint = new Color(128, 0, 0);
		Color moOutlinePaint = new Color(0, 0, 0);

		Color textPaint = new Color(50, 50, 50);

		Font titleFont = new Font("SansSerif", Font.BOLD, 10);

		RootAShape root = new RootAShape();
		FillAShape bgAShape = new FillAShape("bg", new RoundRectangle(0, 0, 1,
				1, 8, 8), AbsRect.FILL_INSIDE, bgPaint, Boolean.TRUE);

		PlaceRect bulletRect = new AbsRect(new AtStart(2), new AtStart(2));
		FillAShape bulletAShape = new FillAShape("bulletBackground",
				new Ellipse2D.Float(0, 0, 8, 8), bulletRect, bulletPaint,
				Boolean.TRUE);

		PlaceRect contentAbsRect = new AbsRect(new AtStart(3), new AtStart(0),
				new AtEnd(-1), new AtEnd(-1));
		ContainerAShape content = new ContainerAShape("dock", contentAbsRect,
				new CutEdgeAShapeLayout());
		PlaceRect titleTextAbsRect = new AbsRect(new AtStart(0),
				new AtStart(0), new AtEnd(0), new AtEnd(-1), null, null, null);
		TextAShape timeTitleText = new TextAShape("titleText",
				"$startTime$-$endTimeExcl$ $summary$", titleTextAbsRect,
				TextAShape.TYPE_WRAP_TEXT, titleFont, textPaint,
				new AtStart(0), new AtStart(-2), Boolean.FALSE);

		DrawAShape outlineAShape = new DrawAShape("outline",
				new RoundRectangle(0, 0, 1, 1, 12, 12), AbsRect.FILL,
				outlinePaint, new BasicStroke(1.2f), Boolean.TRUE);
		outlineAShape.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR));
		outlineAShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		bgAShape.addSubShape(bulletAShape);
		content.addSubShape(timeTitleText);
		bgAShape.addSubShape(content);
		root.addSubShape(bgAShape);

		root.addSubShape(outlineAShape);

		AShapeUtil.enableMouseOverCursor(root);
		AShapeUtil.enableMouseOverState(outlineAShape);

		// AShapeUtil.addResizeBoxes(root, SwingConstants.HORIZONTAL, 4);

		// Drag, resize interactions

		Integer button = new Integer(MouseEvent.BUTTON1);
		AShapeUtil.addMouseFireEvent(outlineAShape,
				MouseKeyInteractor.MOUSE_PRESS,
				DefaultDateArea.AE_SELECTED_PRESSED, true, false, button);
		AShapeUtil.addMouseFireEvent(outlineAShape,
				MouseKeyInteractor.MOUSE_PRESS,
				DefaultDateArea.AE_DRAG_PRESSED, true, true, button);

		AShapeUtil.addMouseEventBlock(outlineAShape, false, new Integer(
				MouseEvent.MOUSE_MOVED));

		return root;
	}

	public void printDebug(DateRange dateRange) {
		Calendar todayCalendar = Calendar.getInstance();
		int today = todayCalendar.get(java.util.Calendar.DAY_OF_YEAR);

		Calendar startCalendar = dateRange.getStart();
		int selectedStartDay = startCalendar
				.get(java.util.Calendar.DAY_OF_YEAR);

		Calendar endCalendar = dateRange.getStart();
		int selectedEndDay = endCalendar.get(java.util.Calendar.DAY_OF_YEAR);

		System.out.println("start-day=" + selectedStartDay + " "
				+ startCalendar.getTime().toString());
		System.out.println("end-day=" + selectedEndDay + " "
				+ endCalendar.getTime().toString());
		System.out.println("today=" + today + " "
				+ todayCalendar.getTime().toString());

	}

	public void recreateFilterRows() {
		Collection cats = CategoryDepository.getRoot().getChildrenDeep();

		DateArea dateArea = view.getDateArea();

		for (Iterator it = cats.iterator(); it.hasNext();) {
			Category cat = (Category) it.next();
			if (MigUtil.isTrue(cat.getProperty(PropertyKey
					.getKey(PROP_FILTERED))) == false)
				it.remove();
		}

		Filter showFilter = new CategoryFilter(new ExpressionFilter(
				"hideFilter", new LogicalExpression(Category.PROP_IS_HIDDEN,
						LogicalExpression.NOT_EQUALS, Boolean.TRUE)));
		dateArea.setActivityViewFilter(showFilter);

		if (cats.size() == 0) {

			dateArea.setRowFilters(null);

		} else {

			CategoryFilter[] catRestr = new CategoryFilter[cats.size()];
			int i = 0;
			for (Iterator it = cats.iterator(); it.hasNext();) {
				Category cat = (Category) it.next();
				catRestr[i++] = new CategoryFilter(cat, true, true);
			}

			dateArea.setRowFilters(catRestr);
		}

		view.revalidate();
		//view.repaint();
	}

	public void interactionOccured(InteractionEvent e) {
		Object value = e.getCommand().getValue();

		if (MigUtil.equals(value, DefaultDateArea.AE_MOUSE_ENTERED)) {
			// mouse hovers over activity
			com.miginfocom.calendar.activity.Activity activity = ((ActivityView) e
					.getInteractor().getInteracted()).getModel();
			System.out.println("MouseOver - activity=" + activity.getID());
			System.out.println("summary=" + activity.getSummary());
			System.out.println("description=" + activity.getDescription());

		}

		final Object o = e.getInteractor().getInteracted();

		if (o instanceof ActivityView
				&& e.getSourceEvent() instanceof MouseEvent) {

			final Point p = ((MouseEvent) e.getSourceEvent()).getPoint();
			Object commandValue = e.getCommand().getValue();

			com.miginfocom.calendar.activity.Activity act = ((ActivityView) o)
					.getModel();

			// remember selected activity
			selectedActivity = new Activity(act);

			if (DefaultDateArea.AE_POPUP_TRIGGER.equals(commandValue)) {

				JPopupMenu pop = new JPopupMenu();
				pop.add(actionFactory.createEditAction());
				pop.add(actionFactory.createDeleteAction());

				pop.show(getView(), p.x, p.y);

			} else if (DefaultDateArea.AE_DOUBLE_CLICKED.equals(commandValue)) {
				actionFactory.createEditAction().actionPerformed(null);
			}
		}

		// else if (MigUtil.equals(value, "selectedPressed")) {
		// // left mouse click selected activity
		// com.miginfocom.calendar.activity.Activity activity = ((ActivityView)
		// e
		// .getInteractor().getInteracted()).getModel();
		// System.out.println("Selected Pressed - activity="
		// + activity.getID());
		// System.out.println("summary=" + activity.getSummary());
		// System.out.println("description=" + activity.getDescription());
		//
		// selectedActivity = new Activity(activity);
		// }

	}

	// trigged if activity is moved or daterange is modified
	public void activityMoved(ActivityMoveEvent e) {

		com.miginfocom.calendar.activity.Activity activity = e.getActivity();
		System.out.println("Moved -  activity=" + activity.getID());
		System.out.println("summary=" + activity.getSummary());
		System.out.println("description=" + activity.getDescription());
		ImmutableDateRange dateRange = activity.getBaseDateRange();
		System.out.println("dateRange=" + dateRange);

//		actionFactory.createActivityMovedAction(dateRange.getStart(), dateRange
//				.getEnd(true)).actionPerformed(null);

	}

	public void viewToday() {
		DateRange newVisRange = new DateRange(view.getDateArea()
				.getVisibleDateRange());
		printDebug(newVisRange);

		Calendar todayCalendar = Calendar.getInstance();
		int today = todayCalendar.get(java.util.Calendar.DAY_OF_YEAR);

		int selectedStartDay = newVisRange.getStart().get(
				java.util.Calendar.DAY_OF_YEAR);
		int selectedEndDay = newVisRange.getStart().get(
				java.util.Calendar.DAY_OF_YEAR);

		int diff = selectedStartDay - today;

		newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, -diff);

		view.getDateArea().setVisibleDateRange(newVisRange);

		view.revalidate();
		//view.repaint();
	}

	public void viewNext() {
		DateRange newVisRange = new DateRange(view.getDateArea()
				.getVisibleDateRange());

		switch (currentViewMode) {
		case VIEW_MODE_DAY:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, 1);

			break;
		case VIEW_MODE_WEEK:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, 7);

			break;
		case VIEW_MODE_WORK_WEEK:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, 7);

			break;
		case VIEW_MODE_MONTH:
			newVisRange.roll(java.util.Calendar.WEEK_OF_YEAR, 1);

			break;
		}

		view.getDateArea().setVisibleDateRange(newVisRange);

		view.revalidate();
		//view.repaint();
	}

	public void viewPrevious() {
		DateRange newVisRange = new DateRange(view.getDateArea()
				.getVisibleDateRange());

		switch (currentViewMode) {
		case VIEW_MODE_DAY:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, -1);

			break;
		case VIEW_MODE_WEEK:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, -7);

			break;
		case VIEW_MODE_WORK_WEEK:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, -7);

			break;
		case VIEW_MODE_MONTH:
			newVisRange.roll(java.util.Calendar.WEEK_OF_YEAR, -1);

			break;
		}

		view.getDateArea().setVisibleDateRange(newVisRange);

		view.revalidate();
	//	view.repaint();
	}

	public void setVisibleDateRange(IDateRange dateRange) {
		ImmutableDateRange newRange = new ImmutableDateRange(dateRange
				.getStartTime().getTimeInMillis(), dateRange.getEndTime()
				.getTimeInMillis(), false, null, null);

		view.getDateArea().setVisibleDateRange(newRange);

		view.revalidate();
		//view.repaint();
	}

}