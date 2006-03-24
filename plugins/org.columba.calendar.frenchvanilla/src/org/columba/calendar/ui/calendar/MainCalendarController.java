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
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;

import org.columba.calendar.base.Activity;
import org.columba.calendar.base.api.IActivity;
import org.columba.calendar.model.api.IDateRange;
import org.columba.calendar.ui.calendar.api.ActivitySelectionChangedEvent;
import org.columba.calendar.ui.calendar.api.IActivitySelectionChangedListener;
import org.columba.calendar.ui.calendar.api.ICalendarView;
import org.columba.calendar.ui.frame.api.ICalendarMediator;
import org.columba.core.gui.menu.ExtendablePopupMenu;
import org.columba.core.gui.menu.MenuXMLDecoder;
import org.columba.core.io.DiskIO;

import com.miginfocom.ashape.AShapeUtil;
import com.miginfocom.ashape.DefaultAShapeProvider;
import com.miginfocom.ashape.interaction.DefaultInteractionBroker;
import com.miginfocom.ashape.interaction.InteractionEvent;
import com.miginfocom.ashape.interaction.InteractionListener;
import com.miginfocom.ashape.interaction.MouseKeyInteractor;
import com.miginfocom.ashape.shapes.AShape;
import com.miginfocom.ashape.shapes.ContainerAShape;
import com.miginfocom.ashape.shapes.DrawAShape;
import com.miginfocom.ashape.shapes.FeatherAShape;
import com.miginfocom.ashape.shapes.FillAShape;
import com.miginfocom.ashape.shapes.RootAShape;
import com.miginfocom.ashape.shapes.TextAShape;
import com.miginfocom.beans.DateAreaBean;
import com.miginfocom.beans.DateHeaderBean;
import com.miginfocom.beans.GridDimensionLayoutBean;
import com.miginfocom.calendar.activity.renderer.AShapeRenderer;
import com.miginfocom.calendar.activity.view.ActivityView;
import com.miginfocom.calendar.category.Category;
import com.miginfocom.calendar.category.CategoryDepository;
import com.miginfocom.calendar.category.CategoryFilter;
import com.miginfocom.calendar.datearea.ActivityDragResizeEvent;
import com.miginfocom.calendar.datearea.ActivityDragResizeListener;
import com.miginfocom.calendar.datearea.ActivityMoveEvent;
import com.miginfocom.calendar.datearea.ActivityMoveListener;
import com.miginfocom.calendar.datearea.DateArea;
import com.miginfocom.calendar.datearea.DefaultDateArea;
import com.miginfocom.calendar.decorators.AbstractGridDecorator;
import com.miginfocom.calendar.grid.DateGrid;
import com.miginfocom.calendar.grid.DefaultGridLineProvider;
import com.miginfocom.calendar.grid.Grid;
import com.miginfocom.calendar.grid.GridLineRepetition;
import com.miginfocom.calendar.grid.GridLineSpecProvider;
import com.miginfocom.calendar.grid.GridLineSpecification;
import com.miginfocom.calendar.header.CellDecorationRow;
import com.miginfocom.calendar.header.DateGridHeader;
import com.miginfocom.calendar.layout.TimeBoundsLayout;
import com.miginfocom.util.MigUtil;
import com.miginfocom.util.PropertyKey;
import com.miginfocom.util.command.DefaultCommand;
import com.miginfocom.util.dates.DateFormatList;
import com.miginfocom.util.dates.DateRange;
import com.miginfocom.util.dates.DateRangeI;
import com.miginfocom.util.dates.ImmutableDateRange;
import com.miginfocom.util.dates.MutableDateRange;
import com.miginfocom.util.expression.LogicalExpression;
import com.miginfocom.util.filter.ExpressionFilter;
import com.miginfocom.util.filter.Filter;
import com.miginfocom.util.gfx.GfxUtil;
import com.miginfocom.util.gfx.RoundRectangle;
import com.miginfocom.util.gfx.ShapeGradientPaint;
import com.miginfocom.util.gfx.SliceSpec;
import com.miginfocom.util.gfx.geometry.AbsRect;
import com.miginfocom.util.gfx.geometry.PlaceRect;
import com.miginfocom.util.gfx.geometry.SizeSpec;
import com.miginfocom.util.gfx.geometry.numbers.AtEnd;
import com.miginfocom.util.gfx.geometry.numbers.AtFixed;
import com.miginfocom.util.gfx.geometry.numbers.AtFraction;
import com.miginfocom.util.gfx.geometry.numbers.AtStart;
import com.miginfocom.util.repetition.DefaultRepetition;
import com.miginfocom.util.states.GenericStates;
import com.miginfocom.util.states.ToolTipProvider;

/**
 * @author fdietz
 * 
 */
public class MainCalendarController implements InteractionListener,
		ActivityMoveListener, ICalendarView, ActivityDragResizeListener {

	public static final String MAIN_WEEKS_CONTEXT = "mainWeeks";

	public static final String MAIN_DAYS_CONTEXT = "mainDays";

	public static final RootAShape HORSHAPE = createDefaultShape(SwingConstants.HORIZONTAL);

	public static final RootAShape VERSHAPE = createDefaultShape(SwingConstants.VERTICAL);

	private int currentViewMode = ICalendarView.VIEW_MODE_WEEK;

	// private ThemeDateAreaContainer view;

	public static final String PROP_FILTERED = "filterRow";

	private IActivity selectedActivity;

	// private com.miginfocom.beans.ActivityAShapeBean activityAShapeBean;

	private com.miginfocom.beans.DateAreaBean dateAreaBean;

	private com.miginfocom.beans.DateHeaderBean northDateHeaderBean;

	private com.miginfocom.beans.GridDimensionLayoutBean verticalGridDimensionLayout;

	private com.miginfocom.beans.DateHeaderBean westDateHeaderBean;

	// private com.miginfocom.beans.ActivityAShapeBean
	// monthlyActivityAShapeBean;

	private com.miginfocom.beans.DateAreaBean monthlyDateAreaBean;

	private com.miginfocom.beans.DateHeaderBean monthlyNorthDateHeaderBean;

	private com.miginfocom.beans.GridDimensionLayoutBean monthlyVerticalGridDimensionLayout;

	private com.miginfocom.beans.GridDimensionLayoutBean monthlyHorizontalGridDimensionLayout;

	private com.miginfocom.beans.DateAreaBean currentDateAreaBean;

	private com.miginfocom.calendar.activity.Activity selectedInternalActivitiy;

	private JPanel panel = new JPanel();

	final Color labelColor = Color.DARK_GRAY;

	// glocal grid line colors
	final Color darkGrayColor = new Color(220, 220, 220);

	final Color lightGrayColor = new Color(240, 240, 240);

	final Color darkDarkGrayColor = new Color(180, 180, 180);

	private EventListenerList listenerList = new EventListenerList();

	private ExtendablePopupMenu menu;

	private ICalendarMediator mediator;

	/**
	 * 
	 */
	public MainCalendarController(ICalendarMediator mediator) {
		super();

		this.mediator = mediator;

		panel = new JPanel();
		panel.setLayout(new BorderLayout());

		initDailyDateArea();
		initMonthlyDateArea();

		setViewMode(currentViewMode);

		panel.repaint();
		// view.getDateArea().setActivitiesSupported(true);

	}

	/**
	 * Get popup menu
	 * 
	 * @return popup menu
	 */
	public JPopupMenu getPopupMenu() {
		return menu;
	}

	/**
	 * create the PopupMenu
	 */
	public void createPopupMenu(ICalendarMediator mediator) {
		try {
			InputStream is = DiskIO
					.getResourceStream("org/columba/calendar/action/contextmenu_calendar.xml");

			menu = new MenuXMLDecoder(mediator).createPopupMenu(is);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private DateAreaBean initComponents(boolean dailyView) {
		panel.removeAll();

		if (dailyView) {
			panel.add(dateAreaBean, BorderLayout.CENTER);
			return dateAreaBean;
		} else {
			panel.add(monthlyDateAreaBean, BorderLayout.CENTER);
			return monthlyDateAreaBean;
		}

	}

	/**
	 * 
	 */
	private void registerListeners(DateAreaBean localDateAreaBean) {
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

		localDateAreaBean.getDateArea().setToolTipProvider(ttp);

		((DefaultDateArea) localDateAreaBean.getDateArea())
				.addInteractionListener(this);

		((DefaultDateArea) localDateAreaBean.getDateArea())
				.addActivityMoveListener(this);

		((DefaultDateArea) localDateAreaBean.getDateArea())
				.addActivityDragResizeListener(this);
	}

	private void initMonthlyDateArea() {
		monthlyVerticalGridDimensionLayout = new GridDimensionLayoutBean();
		monthlyHorizontalGridDimensionLayout = new GridDimensionLayoutBean();
		// monthlyActivityAShapeBean = new ActivityAShapeBean();
		monthlyNorthDateHeaderBean = new DateHeaderBean();
		monthlyDateAreaBean = new DateAreaBean();

		// monthlyVerticalGridDimensionLayout.setRowSizeNormal(new SizeSpec(
		// new AtFixed(20.0f), null, null));

		// monthlyActivityAShapeBean.setBackground(new java.awt.Color(255, 0, 0,
		// 92));
		// monthlyActivityAShapeBean.setOutlinePaint(new java.awt.Color(255,
		// 121,
		// 122));
		// monthlyActivityAShapeBean.setTextFont(UIManager.getFont("Label.font")
		// .deriveFont(Font.BOLD));
		// monthlyActivityAShapeBean
		// .setTextForeground(new com.miginfocom.util.gfx.UIColor(
		// "controlText", null, null));
		// monthlyActivityAShapeBean.setTitleFont(new java.awt.Font("Dialog", 1,
		// 11));
		// monthlyActivityAShapeBean
		// .setPrimaryDimension(SwingUtilities.HORIZONTAL);

		monthlyNorthDateHeaderBean
				.setBackgroundPaint(new com.miginfocom.util.gfx.ShapeGradientPaint(
						new java.awt.Color(240, 240, 240), new java.awt.Color(
								255, 255, 255), 90.0f, 0.7f, 0.6f, false));

		monthlyNorthDateHeaderBean
				.setHeaderRows(new CellDecorationRow[] { new com.miginfocom.calendar.header.CellDecorationRow(
						DateRangeI.RANGE_TYPE_DAY, new DateFormatList(
								"EEEE|EEE|1E|", null), new AtFixed(20.0f),
						new AbsRect(new AtStart(0.0f), new AtStart(0.0f),
								new AtEnd(0.0f), new AtEnd(0.0f), null, null,
								null), (java.awt.Paint[]) null,
						new java.awt.Paint[] { labelColor }, null,
						new Font[] { UIManager.getFont("Label.font") },
						new Integer[] { null }, new AtFraction(0.5f),
						new AtFraction(0.5f)) });

		monthlyNorthDateHeaderBean.setTextAntiAlias(GfxUtil.AA_HINT_ON);

		monthlyDateAreaBean.setDateAreaOuterBorder(BorderFactory
				.createLineBorder(lightGrayColor));

		// monthlyDateAreaBean.setHorizontalGridLinePaintEven(new UIColor(
		// "controlShadow", null, null));
		// monthlyDateAreaBean.setHorizontalGridLinePaintOdd(new UIColor(
		// "controlShadow", null, null));

		// monthlyDateAreaBean.setVisibleDateRangeString("20060101T000000000-20060131T235959999");
		monthlyDateAreaBean.setNorthDateHeader(monthlyNorthDateHeaderBean);
		monthlyDateAreaBean
				.setPrimaryDimensionLayout(monthlyVerticalGridDimensionLayout);
		monthlyDateAreaBean
				.setSecondaryDimensionLayout(monthlyHorizontalGridDimensionLayout);

		// date area grid line
		monthlyDateAreaBean.getDateArea().setGridLineSpecProvider(
				new GridLineSpecProvider() {
					public GridLineSpecification createSpecification(
							DateArea dateArea) {
						return new GridLineSpecification(
						// horizontal grid lines
								new DefaultGridLineProvider(
										new GridLineRepetition[] {
										// dark gray line at 12 and 13
										new GridLineRepetition(0, 1,
												new AtStart(0.1f), new AtEnd(
														-1f), 1,
												darkDarkGrayColor) }),
								// vertical grid lines
								new DefaultGridLineProvider(
										new GridLineRepetition[] {
										// light gray line every day
										new GridLineRepetition(0, 1, null,
												null, 1, darkDarkGrayColor) }),// vertical
								// grid
								// lines
								new DefaultGridLineProvider(
										new GridLineRepetition[] {
										// light gray line every day
										new GridLineRepetition(0, 1,
												new AtStart(1f),
												new AtEnd(-2f), 1,
												darkGrayColor) }));
					}
				});

		((DateGridHeader) monthlyNorthDateHeaderBean.getHeader())
				.setGridLineSpecification(new GridLineSpecification(
						new DefaultGridLineProvider(new GridLineRepetition[] {
						// vertical light gray column separator line
								new GridLineRepetition(0, 1, new AtStart(0f),
										null, 1, lightGrayColor,
										new AtStart(3f), new AtEnd(-3f))

								}), null

				));

		// select current day
		monthlyDateAreaBean.getDateArea()
				.addDecorator(
						new AbstractGridDecorator(monthlyDateAreaBean
								.getDateArea(), 20) {
							public void doPaint(Graphics2D g2, Rectangle bounds) {
								DateGrid dateGrid = (DateGrid) getGrid();

								DateRangeI dr = new DateRange(System
										.currentTimeMillis(),
										DateRangeI.RANGE_TYPE_DAY, 1, null,
										null);
								Rectangle[] rects = dateGrid
										.getBoundsForDateRange(dr,
												Grid.SIZE_MODE_INSIDE);

								g2.setColor(new Color(250, 250, 250));
								for (int i = 0; i < rects.length; i++)
									g2.fill(rects[i]);
							}

							public void gridChanged(PropertyChangeEvent e) {
							}

							public void dispose() {
							}
						});

		/*
		 * monthlyDateAreaBean.getDateArea() .addDecorator( new
		 * AbstractGridDecorator(monthlyDateAreaBean .getDateArea(), 20) {
		 * public void doPaint(Graphics2D g2, Rectangle bounds) { DateGrid
		 * dateGrid = (DateGrid) getGrid();
		 * 
		 * g2.setColor(Color.red); g2.drawString("hello", bounds.x, bounds.y); }
		 * 
		 * public void gridChanged(PropertyChangeEvent e) { }
		 * 
		 * public void dispose() { } });
		 */

		monthlyDateAreaBean.setPrimaryDimension(SwingConstants.HORIZONTAL);
		monthlyDateAreaBean
				.setPrimaryDimensionCellType(DateRangeI.RANGE_TYPE_DAY);
		monthlyDateAreaBean.setPrimaryDimensionCellTypeCount(1);
		monthlyDateAreaBean.setWrapBoundary(DateRangeI.RANGE_TYPE_WEEK);

		DefaultDateArea dateArea = monthlyDateAreaBean.getDateArea();

		dateArea.setActivitiesSupported(true);

		AtFixed forcedSize = new AtFixed(20);
		TimeBoundsLayout layout = new TimeBoundsLayout(new AtFixed(2),
				new AtStart(2), new AtEnd(-2), 2, forcedSize, forcedSize,
				forcedSize);
		dateArea.getActivityLayouts().clear();
		dateArea.addActivityLayout(layout);

		// List list = dateArea.getActivityLayouts();

		// dateArea.addDecorator(dateArea.new ActivityViewDecorator(70));

		DefaultAShapeProvider defaultShapeFactory = ((AShapeRenderer) dateArea
				.getActivityViewRenderer()).getShapeProvider();

		defaultShapeFactory.setShape(HORSHAPE, null);

		// dateArea.recreateActivityViews();

		registerListeners(monthlyDateAreaBean);
	}

	/**
	 * 
	 */
	private void initDailyDateArea() {
		verticalGridDimensionLayout = new GridDimensionLayoutBean();
		westDateHeaderBean = new DateHeaderBean();
		// activityAShapeBean = new ActivityAShapeBean();
		northDateHeaderBean = new DateHeaderBean();
		dateAreaBean = new DateAreaBean();

		// general grid size - if we don't set this property there won't be
		// vertical scrollbars
		verticalGridDimensionLayout.setRowSizeNormal(new SizeSpec(new AtFixed(
				20.0f), null, null));

		// background
		westDateHeaderBean.setBackgroundPaint(new ShapeGradientPaint(new Color(
				240, 240, 240), new Color(255, 255, 255), 0.0f, 0.7f, 0.6f,
				false));

		// west header
		westDateHeaderBean
				.setHeaderRows(new CellDecorationRow[] {
						// first row showing the hour
						new CellDecorationRow(DateRangeI.RANGE_TYPE_HOUR,
								new DateFormatList("HH", null), new AtFixed(
										20.0f), new AbsRect(new AtStart(0.0f),
										new AtStart(0.0f), new AtEnd(0.0f),
										new AtEnd(0.0f), null, null, null),
								(java.awt.Paint[]) null,
								new java.awt.Paint[] { labelColor },
								new DefaultRepetition(0, 1, null, null),
								new java.awt.Font[] { UIManager.getFont(
										"Label.font")
										.deriveFont(Font.PLAIN, 16) },
								new java.lang.Integer[] { null }, new AtStart(
										3.0f), new AtStart(10.0f)),
						// second row showing the minutes
						new com.miginfocom.calendar.header.CellDecorationRow(
								com.miginfocom.util.dates.DateRangeI.RANGE_TYPE_MINUTE,
								new DateFormatList("mm", null), new AtFixed(
										20.0f), new AbsRect(new AtStart(0.0f),
										new AtStart(0.0f), new AtEnd(0.0f),
										new AtEnd(0.0f), null, null, null),
								(java.awt.Paint[]) null, null,
								new DefaultRepetition(0, 2, null, null),
								new java.awt.Font[] { UIManager
										.getFont("Label.font") },
								new java.lang.Integer[] { null }, new AtStart(
										5.0f), new AtStart(5.0f)) });

		westDateHeaderBean
				.setTextAntiAlias(com.miginfocom.util.gfx.GfxUtil.AA_HINT_ON);

		// north header background
		northDateHeaderBean
				.setBackgroundPaint(new com.miginfocom.util.gfx.ShapeGradientPaint(
						new Color(240, 240, 240), new java.awt.Color(255, 255,
								255), 90.0f, 0.7f, 0.6f, false));

		// north header cell decorator
		CellDecorationRow northCellDecorationRow = new CellDecorationRow(
				DateRangeI.RANGE_TYPE_DAY, new DateFormatList("EE' 'dd MMM",
						null), new AtFixed(19.0f), new AbsRect(
						new AtStart(0.0f), new AtStart(0.0f), new AtEnd(0.0f),
						new AtEnd(0.0f), null, null, null),
				(java.awt.Paint[]) null, new java.awt.Paint[] { labelColor },
				new DefaultRepetition(0, 1, null, null),
				new java.awt.Font[] { UIManager.getFont("Label.font") },
				new java.lang.Integer[] { null }, new AtFraction(0.5f),
				new AtFraction(0.5f));

		northDateHeaderBean.setHeaderRows(new CellDecorationRow[] {
		// first header showing Day info
				northCellDecorationRow });

		northDateHeaderBean
				.setTextAntiAlias(com.miginfocom.util.gfx.GfxUtil.AA_HINT_ON);

		// basic activity shape
		// activityAShapeBean.setBackground(new Color(255, 0, 0, 92));
		// activityAShapeBean.setOutlinePaint(new Color(255, 121, 122));
		// activityAShapeBean.setTextFont(UIManager.getFont("Label.font")
		// .deriveFont(Font.BOLD));
		// activityAShapeBean.setTextForeground(new UIColor("controlText", null,
		// null));
		// activityAShapeBean.setTitleFont(UIManager.getFont("Label.font")
		// .deriveFont(Font.BOLD));

		// date area grid line
		dateAreaBean.getDateArea().setGridLineSpecProvider(
				new GridLineSpecProvider() {
					public GridLineSpecification createSpecification(
							DateArea dateArea) {
						return new GridLineSpecification(
						// horizontal grid lines
								new DefaultGridLineProvider(
										new GridLineRepetition[] {
												// dark gray line at 12 and 13
												new GridLineRepetition(0, 2,
														new AtStart(24f),
														new AtStart(26f), 1,
														darkDarkGrayColor,
														new AtStart(6f),
														new AtEnd(-6f)),
												// dark gray line every hour
												new GridLineRepetition(1, 2,
														new AtStart(0.0f),
														null, 1,
														lightGrayColor,
														new AtStart(6f),
														new AtEnd(-6f)),
												// light gray line every 30 min
												new GridLineRepetition(1, 1,
														new AtStart(0.0f),
														null, 1, darkGrayColor,
														new AtStart(6f),
														new AtEnd(-6f))

										}),
								// vertical grid lines
								new DefaultGridLineProvider(
										new GridLineRepetition[] {
										// light gray line every day
										new GridLineRepetition(1, 1,
												new AtStart(1f),
												new AtEnd(-1f), 1,
												darkGrayColor) }));
					}
				});

		dateAreaBean.setDateAreaOuterBorder(javax.swing.BorderFactory
				.createLineBorder(lightGrayColor));

		dateAreaBean.setNorthDateHeader(northDateHeaderBean);
		dateAreaBean.setPrimaryDimension(javax.swing.SwingConstants.VERTICAL);
		dateAreaBean
				.setPrimaryDimensionCellType(com.miginfocom.util.dates.DateRangeI.RANGE_TYPE_MINUTE);
		dateAreaBean.setPrimaryDimensionCellTypeCount(30);
		dateAreaBean.setPrimaryDimensionLayout(verticalGridDimensionLayout);
		// dateAreaBean
		// .setVisibleDateRangeString("20060101T000000000-20060107T235959999");
		dateAreaBean.setWestDateHeader(westDateHeaderBean);
		dateAreaBean.setWrapBoundary(new Integer(
				com.miginfocom.util.dates.DateRangeI.RANGE_TYPE_DAY));

		((DateGridHeader) northDateHeaderBean.getHeader())
				.setGridLineSpecification(new GridLineSpecification(
						new DefaultGridLineProvider(new GridLineRepetition[] {
						// vertical light gray column separator line
								new GridLineRepetition(0, 1, new AtStart(0f),
										null, 1, lightGrayColor,
										new AtStart(3f), new AtEnd(-3f))

								}), null

				));
		((DateGridHeader) westDateHeaderBean.getHeader())
				.setGridLineSpecification(new GridLineSpecification(
						new DefaultGridLineProvider(new GridLineRepetition[] {
						// horizontal light gray column separator line
								new GridLineRepetition(0, 2, new AtStart(0f),
										null, 1, darkGrayColor,
										new AtStart(6f), new AtEnd(-6f)) }),
						null

				));

		// select current day
		/*
		 * dateAreaBean.getDateArea().addDecorator( new
		 * AbstractGridDecorator(dateAreaBean.getDateArea(), 20) { public void
		 * doPaint(Graphics2D g2, Rectangle bounds) { DateGrid dateGrid =
		 * (DateGrid) getGrid();
		 * 
		 * DateRangeI dr = new DateRange(System .currentTimeMillis(),
		 * DateRangeI.RANGE_TYPE_DAY, 1, null, null); Rectangle[] rects =
		 * dateGrid.getBoundsForDateRange(dr, Grid.SIZE_MODE_INSIDE);
		 * 
		 * g2.setColor(new Color(250, 250, 250)); for (int i = 0; i <
		 * rects.length; i++) g2.fill(rects[i]); }
		 * 
		 * public void gridChanged(PropertyChangeEvent e) { }
		 * 
		 * public void dispose() { } });
		 */

		// dateAreaBean.setActivityDepositoryContext();
		dateAreaBean.getDateArea().setActivitiesSupported(true);

		DefaultAShapeProvider defaultShapeFactory = ((AShapeRenderer) dateAreaBean
				.getDateArea().getActivityViewRenderer()).getShapeProvider();

		defaultShapeFactory.setShape(VERSHAPE, null);

		registerListeners(dateAreaBean);
	}

	public IActivity getSelectedActivity() {
		return selectedActivity;
	}

	public JComponent getView() {
		return panel;
	}

	public void setViewMode(int mode) {

		this.currentViewMode = mode;

		int viewMode = -1;

		int days = -1;

		switch (mode) {
		case ICalendarView.VIEW_MODE_DAY:

			viewMode = DateRangeI.RANGE_TYPE_DAY;

			days = 1;

			currentDateAreaBean = initComponents(true);

			break;
		case ICalendarView.VIEW_MODE_WEEK:

			viewMode = DateRangeI.RANGE_TYPE_DAY;

			days = 7;

			currentDateAreaBean = initComponents(true);

			break;
		case ICalendarView.VIEW_MODE_WORK_WEEK:

			viewMode = DateRangeI.RANGE_TYPE_DAY;

			days = 5;

			currentDateAreaBean = initComponents(true);

			break;
		case ICalendarView.VIEW_MODE_MONTH:

			viewMode = DateRangeI.RANGE_TYPE_MONTH;

			days = 1;

			currentDateAreaBean = initComponents(false);

			break;
		}

		DefaultDateArea dateArea = currentDateAreaBean.getDateArea();

		// DefaultAShapeProvider defaultShapeFactory = ((AShapeRenderer)
		// dateArea
		// .getActivityViewRenderer()).getShapeProvider();
		//
		// if (days == 1)
		// defaultShapeFactory.setShape(HORSHAPE, null);
		// else
		// defaultShapeFactory.setShape(VERSHAPE, null);
		//
		// dateArea.recreateActivityViews();

		DateRange newVisRange = new DateRange(dateArea.getVisibleDateRange());

		newVisRange.setSize(viewMode, days, MutableDateRange.ALIGN_CENTER_UP);
		dateArea.setVisibleDateRange(newVisRange);

		panel.revalidate();
		panel.repaint();

	}

	public static final String DEFAULT_CONTAINER_SHAPE_NAME = "defaultContainer";

	public static final String DEFAULT_BACKGROUND_SHAPE_NAME = "defaultBackground";

	public static final String DEFAULT_TITLE_TEXT_SHAPE_NAME = "defaultTitleText";

	public static final String DEFAULT_MAIN_TEXT_SHAPE_NAME = "defaultMainText";

	public static final String DEFAULT_OUTLINE_SHAPE_NAME = "defaultOutline";

	public static final String DEFAULT_SHADOW_SHAPE_SHAPE_NAME = "defaultShadowShape";

	public static final String DEFAULT_SHADOW_SHAPE_NAME = "defaultShadow";

	/**
	 * Creates the default shape.
	 * 
	 * @param dimension
	 *            <code>SwingConstants.VERTICAL</code> or
	 *            <code>SwingConstants.HORIZONTAL</code>.
	 */
	private static RootAShape createDefaultShape(int dimension) {
		Color bgPaint = new Color(0, 0, 255, 40);
		Color outlinePaint = new Color(100, 100, 150);
		Color textPaint = new Color(50, 50, 50);
		// Color shadowPaint = new Color(0, 0, 0, 100);
		Color shadowPaint = null;

		Font textFont = UIManager.getFont("Label.font");

		RootAShape root = new RootAShape();
		ContainerAShape container = new ContainerAShape(
				DEFAULT_CONTAINER_SHAPE_NAME, AbsRect.FILL);

		FillAShape bgAShape = new FillAShape(DEFAULT_BACKGROUND_SHAPE_NAME,
				new RoundRectangle(0, 0, 1, 1, 8, 8), AbsRect.FILL_INSIDE,
				bgPaint, GfxUtil.AA_HINT_ON);

		PlaceRect titleTextAbsRect = new AbsRect(new AtStart(2),
				new AtStart(1), new AtEnd(0), new AtStart(14), null, null, null);
		TextAShape titleText = new TextAShape(DEFAULT_TITLE_TEXT_SHAPE_NAME,
				"$startTime$ - $endTimeExcl$ ($timeZoneShort$)",
				titleTextAbsRect, TextAShape.TYPE_SINGE_LINE, textFont,
				textPaint, new AtStart(0), new AtStart(-3), GfxUtil.AA_HINT_ON);
		titleText.setAttribute(AShape.A_CLIP_TYPE, AShape.CLIP_PARENT_BOUNDS);

		PlaceRect mainTextAbsRect = new AbsRect(new AtStart(2),
				new AtStart(16), new AtEnd(0), new AtEnd(0), null, null, null);
		TextAShape mainText = new TextAShape(DEFAULT_MAIN_TEXT_SHAPE_NAME,
				"$summary$", mainTextAbsRect, TextAShape.TYPE_WRAP_TEXT,
				textFont, textPaint, new AtStart(0), new AtStart(0),
				GfxUtil.AA_HINT_ON);

		DrawAShape outlineAShape = new DrawAShape(DEFAULT_OUTLINE_SHAPE_NAME,
				new RoundRectangle(0, 0, 1, 1, 8, 8), AbsRect.FILL,
				outlinePaint, new BasicStroke(1f), GfxUtil.AA_HINT_ON);
		outlineAShape.setAttribute(AShape.A_MOUSE_CURSOR, Cursor
				.getPredefinedCursor(Cursor.MOVE_CURSOR));
		outlineAShape.setAttribute(AShape.A_REPORT_HIT_AREA, Boolean.TRUE);

		PlaceRect bgAbsRect = new AbsRect(new AtStart(0), new AtStart(0),
				new AtEnd(0), new AtEnd(0), null, null,
				new Insets(-2, -2, 2, 2));
		FillAShape filledShadow = new FillAShape(
				DEFAULT_SHADOW_SHAPE_SHAPE_NAME, new RoundRectangle(0, 0, 1, 1,
						5, 5), bgAbsRect, shadowPaint, GfxUtil.AA_HINT_ON);
		SliceSpec shwSI = new SliceSpec(new Insets(10, 10, 10, 10),
				SliceSpec.TYPE_TILE_CUT, SliceSpec.OPT_BORDER);
		FeatherAShape shadowShape = new FeatherAShape(
				DEFAULT_SHADOW_SHAPE_NAME, filledShadow, new Color(255, 255,
						255, 0), 5, shwSI);

		bgAShape.addSubShape(titleText);
		bgAShape.addSubShape(mainText);

		container.addSubShape(shadowShape);
		container.addSubShape(bgAShape);
		container.addSubShape(outlineAShape);
		root.addSubShape(container);
		root.setRepaintPadding(new Insets(4, 4, 4, 4));

		if (dimension == SwingConstants.VERTICAL) {
			AShapeUtil.enableMouseOverCursor(root);
			AShapeUtil.enableMouseOverState(outlineAShape);

			AShapeUtil.setResizeBoxes(outlineAShape, dimension, 4);

			// Drag, resize interactions
			Integer button = new Integer(MouseEvent.BUTTON1);

			AShapeUtil.addMouseFireEvent(outlineAShape,
					MouseKeyInteractor.MOUSE_PRESS,
					DefaultDateArea.AE_SELECTED_PRESSED, true, false, button);
			AShapeUtil.addMouseFireEvent(outlineAShape,
					MouseKeyInteractor.MOUSE_PRESS,
					DefaultDateArea.AE_DRAG_PRESSED, true, true, button);

			DefaultCommand entCmd = new DefaultCommand(
					DefaultInteractionBroker.CMD_FIRE_INTERACTION_EVENT, null,
					DefaultDateArea.AE_MOUSE_ENTERED, null);
			DefaultCommand exitCmd = new DefaultCommand(
					DefaultInteractionBroker.CMD_FIRE_INTERACTION_EVENT, null,
					DefaultDateArea.AE_MOUSE_EXITED, null);
			AShapeUtil.addEnterExitCommands(outlineAShape, entCmd, exitCmd,
					true);

			AShapeUtil.addMouseFireEvent(outlineAShape,
					MouseKeyInteractor.MOUSE_CLICK, DefaultDateArea.AE_CLICKED,
					true, false, button);
			AShapeUtil.addMouseFireEvent(outlineAShape,
					MouseKeyInteractor.MOUSE_DOUBLE_CLICK,
					DefaultDateArea.AE_DOUBLE_CLICKED, true, true, button);
			AShapeUtil.addMouseFireEvent(outlineAShape,
					MouseKeyInteractor.MOUSE_POPUP_TRIGGER,
					DefaultDateArea.AE_POPUP_TRIGGER, true, true, null);

			// Block mouse moves to the underlaying component won't restore the
			// Cursor
			AShapeUtil.addMouseEventBlock(outlineAShape, false, new Integer(
					MouseEvent.MOUSE_MOVED));
		} else {
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
		}

		// differnt border for recurrent events
		// ActivityInteractor.setStaticOverride("outline", AShape.A_PAINT,
		// new OverrideFilter() {
		// public Object getOverride(Object subject,
		// Object defaultObject) {
		// return ((ActivityView) subject).getModel()
		// .isRecurrent() ? Color.YELLOW : defaultObject;
		// }
		// });

		// differnt outline color is selected
		// AShapeUtil.setStateOverride(outlineAShape, GenericStates.SELECTED,
		// AShape.A_PAINT, new Color(255, 255, 50));

		// bold outline if selected
		AShapeUtil.setStateOverride(outlineAShape, GenericStates.SELECTED,
				AShape.A_STROKE, new BasicStroke(2.5f));

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
		filterDateArea(dateAreaBean);
		filterDateArea(monthlyDateAreaBean);
	}

	/**
	 * 
	 */
	private void filterDateArea(DateAreaBean localDateAreaBean) {
		Collection cats = CategoryDepository.getRoot().getChildrenDeep();

		DateArea dateArea = localDateAreaBean.getDateArea();

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

		localDateAreaBean.revalidate();
		localDateAreaBean.repaint();
	}

	public void interactionOccured(InteractionEvent e) {
		Object value = e.getCommand().getValue();

		System.out.println("interactionOccured="+value.toString());
		
		if (MigUtil.equals(value, DefaultDateArea.AE_MOUSE_ENTERED)) {
			// mouse hovers over activity
			com.miginfocom.calendar.activity.Activity activity = ((ActivityView) e
					.getInteractor().getInteracted()).getModel();
			System.out.println("MouseOver - activity=" + activity.getID());
			// System.out.println("summary=" + activity.getSummary());
			// System.out.println("description=" + activity.getDescription());

		}

		final Object o = e.getInteractor().getInteracted();

		if (e.getSourceEvent() instanceof MouseEvent) {
			final Point p = ((MouseEvent) e.getSourceEvent()).getPoint();
			Object commandValue = e.getCommand().getValue();

//			if (DefaultDateArea.AE_CLICKED.equals(commandValue)
//					|| DefaultDateArea.AE_DOUBLE_CLICKED.equals(commandValue)) {

				if (o instanceof ActivityView) {
					// retrieve new selection
					selectedInternalActivitiy = ((ActivityView) o).getModel();

					// remember selected activity
					selectedActivity = new Activity(selectedInternalActivitiy);

					// notify all listeners
					fireSelectionChanged(new IActivity[] { selectedActivity });
				} else {
					// clicked on calendar - not activity
					selectedInternalActivitiy = null;

					selectedActivity = null;
					// fireSelectionChanged(new Activity[] {});
				}
//			}

			if (o instanceof ActivityView) {
				// check if happens on the selected activity
				if (DefaultDateArea.AE_POPUP_TRIGGER.equals(commandValue)) {

					// select activity before opening context context-menu
					// selectedInternalActivitiy.getStates().setStates(
					// GenericStates.SELECTED_BIT, true);

					// show context menu
					menu.show(currentDateAreaBean.getDateArea(), p.x, p.y);

				} else if (DefaultDateArea.AE_DOUBLE_CLICKED
						.equals(commandValue)) {

					mediator.fireStartActivityEditing(selectedActivity);
				}
			} else {
				// check if happens in calendar, but not on activity
				
				if (DefaultDateArea.AE_DOUBLE_CLICKED.equals(commandValue)) {

					// double-click on empty calendar
					mediator.fireCreateActivity(null);
				}
			}
		}
	}

	// trigged if activity is moved or daterange is modified
	public void activityMoved(ActivityMoveEvent e) {

		com.miginfocom.calendar.activity.Activity activity = e.getActivity();
		System.out.println("activity moved=" + activity.getID());
	}

	public void viewToday() {
		DateRange newVisRange = new DateRange(dateAreaBean.getDateArea()
				.getVisibleDateRange());
		printDebug(newVisRange);

		Calendar todayCalendar = Calendar.getInstance();
		int today = todayCalendar.get(java.util.Calendar.DAY_OF_YEAR);

		int selectedStartDay = newVisRange.getStart().get(
				java.util.Calendar.DAY_OF_YEAR);
//		int selectedEndDay = newVisRange.getStart().get(
//				java.util.Calendar.DAY_OF_YEAR);

		int diff = selectedStartDay - today;

		newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, -diff);

		dateAreaBean.getDateArea().setVisibleDateRange(newVisRange);

		dateAreaBean.revalidate();
		// view.repaint();
	}

	public void viewNext() {
		DateRange newVisRange = new DateRange(dateAreaBean.getDateArea()
				.getVisibleDateRange());

		switch (currentViewMode) {
		case ICalendarView.VIEW_MODE_DAY:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, 1);

			break;
		case ICalendarView.VIEW_MODE_WEEK:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, 7);

			break;
		case ICalendarView.VIEW_MODE_WORK_WEEK:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, 7);

			break;
		case ICalendarView.VIEW_MODE_MONTH:
			newVisRange.roll(java.util.Calendar.WEEK_OF_YEAR, 1);

			break;
		}

		currentDateAreaBean.getDateArea().setVisibleDateRange(newVisRange);

		currentDateAreaBean.revalidate();
		// view.repaint();
	}

	public void viewPrevious() {
		DateRange newVisRange = new DateRange(dateAreaBean.getDateArea()
				.getVisibleDateRange());

		switch (currentViewMode) {
		case ICalendarView.VIEW_MODE_DAY:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, -1);

			break;
		case ICalendarView.VIEW_MODE_WEEK:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, -7);

			break;
		case ICalendarView.VIEW_MODE_WORK_WEEK:
			newVisRange.roll(java.util.Calendar.DAY_OF_YEAR, -7);

			break;
		case ICalendarView.VIEW_MODE_MONTH:
			newVisRange.roll(java.util.Calendar.WEEK_OF_YEAR, -1);

			break;
		}

		currentDateAreaBean.getDateArea().setVisibleDateRange(newVisRange);

		currentDateAreaBean.revalidate();
		// view.repaint();
	}

	public void setVisibleDateRange(IDateRange dateRange) {
		ImmutableDateRange newRange = new ImmutableDateRange(dateRange
				.getStartTime().getTimeInMillis(), dateRange.getEndTime()
				.getTimeInMillis(), false, null, null);

		currentDateAreaBean.getDateArea().setVisibleDateRange(newRange);

		currentDateAreaBean.revalidate();
		// view.repaint();
	}

	public void activityDragResized(ActivityDragResizeEvent e) {
		System.out.println(e);

		com.miginfocom.calendar.activity.ActivityList activityList = (com.miginfocom.calendar.activity.ActivityList) e
				.getSource();

		for (int i = 0, size = activityList.size(); i < size; i++) {
			System.out.println("Changed: " + activityList.get(i));
			// TimeSpan span = activityList.get(i);

			mediator.fireActivityMoved(selectedActivity);
		}

	}

	/**
	 * Adds a listener.
	 */
	public void addSelectionChangedListener(
			IActivitySelectionChangedListener listener) {
		listenerList.add(IActivitySelectionChangedListener.class, listener);
	}

	/**
	 * Removes a previously registered listener.
	 */
	public void removeSelectionChangedListener(
			IActivitySelectionChangedListener listener) {
		listenerList.remove(IActivitySelectionChangedListener.class, listener);
	}

	/**
	 * Propagates an event to all registered listeners notifying them that the
	 * selectoin has been changed.
	 */
	private void fireSelectionChanged(IActivity[] selection) {
		ActivitySelectionChangedEvent e = new ActivitySelectionChangedEvent(
				this, selection);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IActivitySelectionChangedListener.class) {
				((IActivitySelectionChangedListener) listeners[i + 1])
						.selectionChanged(e);
			}
		}
	}

}