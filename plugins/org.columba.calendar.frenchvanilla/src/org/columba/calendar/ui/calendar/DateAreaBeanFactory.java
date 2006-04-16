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

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

import com.miginfocom.ashape.DefaultAShapeProvider;
import com.miginfocom.ashape.shapes.RootAShape;
import com.miginfocom.beans.DateAreaBean;
import com.miginfocom.beans.DateHeaderBean;
import com.miginfocom.beans.GridDimensionLayoutBean;
import com.miginfocom.calendar.activity.renderer.AShapeRenderer;
import com.miginfocom.calendar.datearea.DateArea;
import com.miginfocom.calendar.datearea.DefaultDateArea;
import com.miginfocom.calendar.decorators.AbstractGridDecorator;
import com.miginfocom.calendar.decorators.DateSeparatorDecorator;
import com.miginfocom.calendar.grid.DateGrid;
import com.miginfocom.calendar.grid.DefaultGridLineProvider;
import com.miginfocom.calendar.grid.Grid;
import com.miginfocom.calendar.grid.GridLineRepetition;
import com.miginfocom.calendar.grid.GridLineSpecProvider;
import com.miginfocom.calendar.grid.GridLineSpecification;
import com.miginfocom.calendar.header.CellDecorationRow;
import com.miginfocom.calendar.header.DateGridHeader;
import com.miginfocom.calendar.layout.ActivityLayout;
import com.miginfocom.calendar.layout.TimeBoundsLayout;
import com.miginfocom.util.dates.BoundaryRounder;
import com.miginfocom.util.dates.DateFormatList;
import com.miginfocom.util.dates.DateRange;
import com.miginfocom.util.dates.DateRangeI;
import com.miginfocom.util.gfx.GfxUtil;
import com.miginfocom.util.gfx.ShapeGradientPaint;
import com.miginfocom.util.gfx.geometry.AbsRect;
import com.miginfocom.util.gfx.geometry.SizeSpec;
import com.miginfocom.util.gfx.geometry.numbers.AtEnd;
import com.miginfocom.util.gfx.geometry.numbers.AtFixed;
import com.miginfocom.util.gfx.geometry.numbers.AtFraction;
import com.miginfocom.util.gfx.geometry.numbers.AtStart;
import com.miginfocom.util.repetition.DefaultRepetition;

public class DateAreaBeanFactory {

	private static final Color labelColor = Color.DARK_GRAY;

	// glocal grid line colors
	private static final Color darkGrayColor = new Color(220, 220, 220);

	private static final Color lightGrayColor = new Color(240, 240, 240);

	private static final Color darkDarkGrayColor = new Color(180, 180, 180);

	public static final RootAShape HORSHAPE = ActivityShapeFactory
			.createDefaultShape(SwingConstants.HORIZONTAL);

	public static final RootAShape VERSHAPE = ActivityShapeFactory
			.createDefaultShape(SwingConstants.VERTICAL);

	/**
	 * 
	 */
	public static DateAreaBean initDailyDateArea() {

		GridDimensionLayoutBean verticalGridDimensionLayout = new GridDimensionLayoutBean();
		DateHeaderBean westDateHeaderBean = new DateHeaderBean();
		// activityAShapeBean = new ActivityAShapeBean();
		DateHeaderBean northDateHeaderBean = new DateHeaderBean();
		com.miginfocom.beans.DateAreaBean dateAreaBean = new DateAreaBean();

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
		dateAreaBean.getDateArea().setActivitiesSupported(true);

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

		// dateAreaBean.getDateArea().addDecorator(
		// new CellLabelDecorator(dateAreaBean.getDateArea(), 20) {
		// public void paintCell(java.awt.Graphics2D g2, int r, int c,
		// java.awt.Rectangle b) {
		// DateGrid dateGrid = (DateGrid) getGrid();
		// DateRangeI dr = new DateRange(System
		// .currentTimeMillis(),
		// DateRangeI.RANGE_TYPE_DAY, 1, null, null);
		// Rectangle[] rects = dateGrid.getBoundsForDateRange(dr,
		// Grid.SIZE_MODE_INSIDE);
		//
		// g2.drawString("test", 0,0);
		// //super.paintCell(g2, r, c, b);
		// }
		// });

		// dateAreaBean.setActivityDepositoryContext();
		// dateAreaBean.getDateArea().setActivitiesSupported(true);

		//
		//activity setup
		// 
		
		// define activity layout
		AtFixed forcedSize = new AtFixed(17);
		TimeBoundsLayout layout = new TimeBoundsLayout(new AtFixed(2),
				new AtStart(2), new AtEnd(-2), 2);
		dateAreaBean.setActivityLayouts(new ActivityLayout[] { layout });

		DefaultAShapeProvider defaultShapeFactory = ((AShapeRenderer) dateAreaBean
				.getDateArea().getActivityViewRenderer()).getShapeProvider();

		defaultShapeFactory.setShape(VERSHAPE, null);

		return dateAreaBean;

	}

	public static DateAreaBean initMonthlyDateArea() {

		GridDimensionLayoutBean monthlyVerticalGridDimensionLayout = new GridDimensionLayoutBean();
		GridDimensionLayoutBean monthlyHorizontalGridDimensionLayout = new GridDimensionLayoutBean();
		// monthlyActivityAShapeBean = new ActivityAShapeBean();
		DateHeaderBean monthlyNorthDateHeaderBean = new DateHeaderBean();
		DateAreaBean monthlyDateAreaBean = new DateAreaBean();

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

		DateHeaderBean monthlyWestDateHeaderBean = new DateHeaderBean();
		monthlyWestDateHeaderBean
				.setHeaderRows(new CellDecorationRow[] {
				// first row showing the hour
				new CellDecorationRow(
						DateRangeI.RANGE_TYPE_WEEK,
						new DateFormatList("'w'w", null),
						new AtFixed(20.0f),
						new AbsRect(new AtStart(0.0f), new AtStart(0.0f),
								new AtEnd(0.0f), new AtEnd(0.0f), null, null,
								null),
						(java.awt.Paint[]) null,
						new java.awt.Paint[] { labelColor },
						new DefaultRepetition(0, 1, null, null),
						new java.awt.Font[] { UIManager.getFont("Label.font") },
						new java.lang.Integer[] { null }, new AtStart(3.0f),
						new AtStart(10.0f)), });

		monthlyWestDateHeaderBean
				.setTextAntiAlias(com.miginfocom.util.gfx.GfxUtil.AA_HINT_ON);

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

		monthlyDateAreaBean.setNorthDateHeader(monthlyNorthDateHeaderBean);
		// monthlyDateAreaBean.setWestDateHeader(monthlyWestDateHeaderBean);
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
														-1f), 1, lightGrayColor) }),
								// vertical grid lines
								new DefaultGridLineProvider(
										new GridLineRepetition[] {
										// light gray line every day
										new GridLineRepetition(0, 1, null,
												null, 1, lightGrayColor) }),// vertical
								// grid
								// lines
								new DefaultGridLineProvider(
										new GridLineRepetition[] {
										// light gray line every day
										new GridLineRepetition(0, 1,
												new AtStart(1f),
												new AtEnd(-2f), 1,
												lightGrayColor) }));
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

		monthlyDateAreaBean.setPrimaryDimension(SwingConstants.HORIZONTAL);
		monthlyDateAreaBean
				.setPrimaryDimensionCellType(DateRangeI.RANGE_TYPE_DAY);
		monthlyDateAreaBean.setPrimaryDimensionCellTypeCount(1);
		monthlyDateAreaBean.setWrapBoundary(DateRangeI.RANGE_TYPE_WEEK);

		DefaultDateArea dateArea = monthlyDateAreaBean.getDateArea();
		dateArea.setActivitiesSupported(true);

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

		final DateFormat defaultFormat = new SimpleDateFormat("MMMM dd");

		monthlyDateAreaBean.getDateArea()
				.addDecorator(
						new AbstractGridDecorator(monthlyDateAreaBean
								.getDateArea(), 20) {
							public void doPaint(Graphics2D g2, Rectangle bounds) {
								DateGrid dateGrid = (DateGrid) getGrid();

								RenderingHints qualityHints = new RenderingHints(
										RenderingHints.KEY_ANTIALIASING,
										RenderingHints.VALUE_ANTIALIAS_ON);
								g2.setRenderingHints(qualityHints);

								g2.setFont(UIManager.getFont("Label.font")
										.deriveFont(Font.BOLD));

								for (int i = 0; i < dateGrid.getRowCount(); i++) {
									for (int j = 0; j < dateGrid
											.getColumnCount(); j++) {
										Rectangle r = dateGrid.getBoundsOfCell(
												i, j, Grid.SIZE_MODE_INSIDE,
												true);
										DateRangeI range = dateGrid
												.getDateRangeForCell(i, j);
										int day = range.getStart().get(
												Calendar.DAY_OF_MONTH);
										int weekday = range.getStart().get(
												Calendar.DAY_OF_WEEK);
										if (weekday == Calendar.SUNDAY)
											g2
													.setColor(new Color(255,
															102, 102));
										else if (weekday == Calendar.SATURDAY)
											g2.setColor(darkGrayColor);
										else
											g2.setColor(darkDarkGrayColor);
										String dayString = null;
										if (day == 1) {
											dayString = defaultFormat
													.format(range.getStart()
															.getTime());
										} else
											dayString = new Integer(day)
													.toString();

										Rectangle2D rect = g2.getFontMetrics()
												.getStringBounds(dayString, g2);

										int x2 = r.x + r.width - 1;
										x2 -= Math.abs(rect.getWidth());
										int y2 = r.y;
										y2 += Math.abs(rect.getHeight());
										g2.drawString(dayString, x2, y2);

									}

								}

							}

							public void gridChanged(PropertyChangeEvent e) {
							}

							public void dispose() {
							}
						});

		dateArea.addDecorator(
				new DateSeparatorDecorator(dateArea,
						700, DateRangeI.RANGE_TYPE_MONTH, darkDarkGrayColor));
		
		//
		//activity setup
		// 
		
		dateArea.setActivitiesSupported(true);

		// define activity layout
		AtFixed forcedSize = new AtFixed(17);
		TimeBoundsLayout layout = new TimeBoundsLayout(new AtFixed(2),
				new AtStart(17), new AtEnd(-2), 2, forcedSize, null, null,
				null, new BoundaryRounder(DateRangeI.RANGE_TYPE_DAY, true,
						true, false));
		monthlyDateAreaBean.setActivityLayouts(new ActivityLayout[] { layout });

		DefaultAShapeProvider defaultShapeFactory = ((AShapeRenderer) dateArea
				.getActivityViewRenderer()).getShapeProvider();

		defaultShapeFactory.setShape(HORSHAPE, null);
		// defaultShapeFactory.setShape(AShapeCreator.createTraslucentShapeHorizontal(),
		// null);

		return monthlyDateAreaBean;
	}

}
