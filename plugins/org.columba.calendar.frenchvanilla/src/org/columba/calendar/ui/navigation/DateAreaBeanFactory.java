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
package org.columba.calendar.ui.navigation;

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

import org.columba.calendar.ui.calendar.ActivityShapeFactory;

import com.miginfocom.ashape.shapes.RootAShape;
import com.miginfocom.ashape.shapes.TextAShape;
import com.miginfocom.beans.DateAreaBean;
import com.miginfocom.beans.DateHeaderBean;
import com.miginfocom.beans.GridDimensionLayoutBean;
import com.miginfocom.calendar.datearea.DateArea;
import com.miginfocom.calendar.datearea.DefaultDateArea;
import com.miginfocom.calendar.decorators.AbstractGridDecorator;
import com.miginfocom.calendar.decorators.DateSeparatorDecorator;
import com.miginfocom.calendar.decorators.SelectionGridDecorator;
import com.miginfocom.calendar.grid.DateGrid;
import com.miginfocom.calendar.grid.DefaultGridLineProvider;
import com.miginfocom.calendar.grid.Grid;
import com.miginfocom.calendar.grid.GridLineRepetition;
import com.miginfocom.calendar.grid.GridLineSpecProvider;
import com.miginfocom.calendar.grid.GridLineSpecification;
import com.miginfocom.calendar.header.CellDecorationRow;
import com.miginfocom.calendar.header.DateGridHeader;
import com.miginfocom.util.dates.DateFormatList;
import com.miginfocom.util.dates.DateRange;
import com.miginfocom.util.dates.DateRangeI;
import com.miginfocom.util.gfx.GfxUtil;
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

	public static DateAreaBean initDateArea() {

		GridDimensionLayoutBean monthlyVerticalGridDimensionLayout = new GridDimensionLayoutBean();
		GridDimensionLayoutBean monthlyHorizontalGridDimensionLayout = new GridDimensionLayoutBean();
		// monthlyActivityAShapeBean = new ActivityAShapeBean();
		DateHeaderBean monthlyNorthDateHeaderBean = new DateHeaderBean();

		DateAreaBean monthlyDateAreaBean = new DateAreaBean();

		monthlyHorizontalGridDimensionLayout.setRowSizeNormal(new SizeSpec(
				new AtFixed(17.0f), null, null));

		DateHeaderBean monthlyWestDateHeaderBean = new DateHeaderBean();
		// monthlyWestDateHeaderBean
		// .setLabelRotation(TextAShape.TYPE_SINGE_LINE_ROT_CCW);

		monthlyWestDateHeaderBean.setHeaderRows(new CellDecorationRow[] {
		// showing the week number
				new CellDecorationRow(DateRangeI.RANGE_TYPE_WEEK,
						new DateFormatList("'w'w", null), new AtFixed(24f),
						new AbsRect(new AtStart(0.0f), new AtStart(0.0f),
								new AtEnd(0.0f), new AtEnd(0.0f), null, null,
								null), (java.awt.Paint[]) null,
						new java.awt.Paint[] { labelColor },
						new DefaultRepetition(0, 1, null, null),
						new java.awt.Font[] { UIManager.getFont("Label.font")
								.deriveFont(9f) },
						new java.lang.Integer[] { null }, new AtFraction(0.5f),
						new AtStart(4f)) });

		monthlyWestDateHeaderBean
				.setBackgroundPaint(new com.miginfocom.util.gfx.ShapeGradientPaint(
						new java.awt.Color(255, 255, 255), new java.awt.Color(
								247, 247, 247), 0.0f, 0.7f, 0.6f, false));

		monthlyWestDateHeaderBean
				.setTextAntiAlias(com.miginfocom.util.gfx.GfxUtil.AA_HINT_ON);

		DateHeaderBean eastDateHeaderBean = new DateHeaderBean();
		eastDateHeaderBean
				.setHeaderRows(new CellDecorationRow[] {
				// showing the month number
				new CellDecorationRow(
						DateRangeI.RANGE_TYPE_MONTH,
						new DateFormatList("MMMM|MMM", null),
						new AtFixed(17f),
						new AbsRect(new AtStart(0.0f), new AtStart(0.0f),
								new AtEnd(0.0f), new AtEnd(0.0f), null, null,
								null),
						(java.awt.Paint[]) null,
						new java.awt.Paint[] { labelColor },
						new DefaultRepetition(0, 1, null, null),
						new java.awt.Font[] { UIManager.getFont("Label.font") },
						new java.lang.Integer[] { null }, new AtFraction(0.5f),
						new AtStart(4f)) });

		eastDateHeaderBean
				.setBackgroundPaint(new com.miginfocom.util.gfx.ShapeGradientPaint(
						new java.awt.Color(255, 255, 255), new java.awt.Color(
								247, 247, 247), 180.0f, 0.7f, 0.6f, false));

		eastDateHeaderBean
				.setTextAntiAlias(com.miginfocom.util.gfx.GfxUtil.AA_HINT_ON);

		eastDateHeaderBean.setLabelRotation(TextAShape.TYPE_SINGE_LINE_ROT_CW);

		monthlyNorthDateHeaderBean
				.setBackgroundPaint(new com.miginfocom.util.gfx.ShapeGradientPaint(
						new java.awt.Color(240, 240, 240), new java.awt.Color(
								255, 255, 255), 90.0f, 0.7f, 0.6f, false));

		monthlyNorthDateHeaderBean
				.setHeaderRows(new CellDecorationRow[] { new com.miginfocom.calendar.header.CellDecorationRow(
						DateRangeI.RANGE_TYPE_DAY, new DateFormatList("1E",
								null), new AtFixed(20.0f), new AbsRect(
								new AtStart(0.0f), new AtStart(0.0f),
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
		monthlyDateAreaBean.setWestDateHeader(monthlyWestDateHeaderBean);
		monthlyDateAreaBean.setEastDateHeader(eastDateHeaderBean);
		monthlyDateAreaBean
				.setPrimaryDimensionLayout(monthlyVerticalGridDimensionLayout);
		monthlyDateAreaBean
				.setSecondaryDimensionLayout(monthlyHorizontalGridDimensionLayout);

		((DateGridHeader) monthlyWestDateHeaderBean.getHeader())
				.setGridLineSpecification(new GridLineSpecification(
						new DefaultGridLineProvider(new GridLineRepetition[] {
						// horizontal light gray column separator line
								new GridLineRepetition(0, 1, new AtStart(0f),
										null, 1, lightGrayColor,
										new AtStart(3f), new AtEnd(-3f))

								}), null

				));
		((DateGridHeader) monthlyNorthDateHeaderBean.getHeader())
				.setGridLineSpecification(new GridLineSpecification(
						new DefaultGridLineProvider(new GridLineRepetition[] {
						// vertical light gray column separator line
								new GridLineRepetition(0, 1, new AtStart(0f),
										null, 1, lightGrayColor,
										new AtStart(3f), new AtEnd(-3f))

								}), null

				));
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

		monthlyDateAreaBean.setPrimaryDimension(SwingConstants.HORIZONTAL);
		monthlyDateAreaBean
				.setPrimaryDimensionCellType(DateRangeI.RANGE_TYPE_DAY);
		monthlyDateAreaBean.setPrimaryDimensionCellTypeCount(1);
		monthlyDateAreaBean.setWrapBoundary(DateRangeI.RANGE_TYPE_WEEK);

		DefaultDateArea dateArea = monthlyDateAreaBean.getDateArea();
		dateArea.setActivitiesSupported(false);

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

								g2.setFont(UIManager.getFont("Label.font"));

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
										else
											g2.setColor(darkDarkGrayColor);
										String dayString = new Integer(day)
												.toString();

										Rectangle2D rect = g2.getFontMetrics()
												.getStringBounds(dayString, g2);

										int x2 = r.x + r.width / 2 - 1;
										x2 -= Math.abs(rect.getWidth() / 2);
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
		monthlyDateAreaBean.getDateArea()
				.addDecorator(
						new SelectionGridDecorator(monthlyDateAreaBean
								.getDateArea(), 10, new java.awt.Paint[] {
								lightGrayColor, null, new Color(255,255,230,255), null },
								new int[] { Grid.SIZE_MODE_INSIDE,
										Grid.SIZE_MODE_INSIDE,
										Grid.SIZE_MODE_INSIDE,
										Grid.SIZE_MODE_INSIDE }));
		dateArea.addDecorator(new DateSeparatorDecorator(dateArea, 700,
				DateRangeI.RANGE_TYPE_MONTH, darkDarkGrayColor));

		//
		// activity setup
		// 

		dateArea.setActivitiesSupported(false);

		return monthlyDateAreaBean;
	}

}
