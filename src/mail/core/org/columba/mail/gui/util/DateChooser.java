//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.gui.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 *  taken some code from the Kiwi Toolkit:
 * 		http://www.dystance.net/ping/kiwi/
 * 		author:  Mark Lindner 
 * 
 */
public class DateChooser extends JPanel implements ActionListener
{
	CalendarPane calendarPane;

	//private JLabel l_date, l_year, l_month;
	private JLabel l_date, l_month;
	
	//private JButton b_lyear, b_ryear, b_lmonth, b_rmonth;
	private JButton b_lmonth, b_rmonth;
	
	private SimpleDateFormat datefmt = new SimpleDateFormat("E  d MMM yyyy");
	private Calendar selectedDate = null, minDate = null, maxDate = null;
	private int selectedDay, firstDay, minDay = -1, maxDay = -1;
	private static final int cellSize = 22;
	private static final int[] daysInMonth =
		{ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private static final int[] daysInMonthLeap =
		{ 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
	private String months[], labels[] = new String[7];
	private static final Color weekendColor = Color.red.darker();
	private Color highlightColor, disabledColor;
	private boolean clipMin = false,
		clipMax = false,
		clipAllMin = false,
		clipAllMax = false;
	private int weekendCols[] = { 0, 0 };

	//private ActionSupport asupport;

	/** <i>Date changed</i> event command. */
	public static final String DATE_CHANGE_CMD = "dateChanged";
	/** <i>Month changed</i> event command. */
	public static final String MONTH_CHANGE_CMD = "monthChanged";
	/** <i>Year changed</i> event command. */
	public static final String YEAR_CHANGE_CMD = "yearChanged";

	/** Construct a new <code>DateChooser</code>. The selection will be
	* initialized to the current date.
	*/

	public DateChooser()
	{
		this(Calendar.getInstance());
	}

	/** Construct a new <code>DateChooser</code> with the specified selected
	 * date.
	 *
	 * @param <code>date</code> The date for the selection.
	 */

	public DateChooser(Calendar date) 
	{
		//asupport = new ActionSupport(this);

		DateFormatSymbols sym = new DateFormatSymbols();

		months = sym.getShortMonths();

		String wkd[] = sym.getShortWeekdays();

		for (int i = 0; i < 7; i++)
		{
			int l = Math.min(wkd[i + 1].length(), 2);
			labels[i] = wkd[i + 1].substring(0, l);
		}

		// Let's at least make a half-assed attempt at conforming to the Metal
		// PLAF colors.

		highlightColor = UIManager.getColor("List.selectionBackground");
		disabledColor = Color.red;


		setBorder( BorderFactory.createEtchedBorder() );
		
		setLayout(new BorderLayout(5, 5));

		JPanel top = new JPanel();
		top.setLayout(new BorderLayout(0, 0));
		top.setBorder( BorderFactory.createEmptyBorder(2,2,2,2) );
		//top.setBorder( BorderFactory.createEtchedBorder() );

		JPanel p1 = new JPanel();
		p1.setLayout( new BorderLayout() );
		top.add(p1, BorderLayout.CENTER );

		b_lmonth = new JButton("<");
		b_lmonth.addActionListener( this );
		b_lmonth.setMargin( new Insets(0,0,0,0) );
		//b_lmonth.setFocusPainted(false);
		//b_lmonth.setOpaque(false);
		//b_lmonth.addActionListener(this);
		p1.add(b_lmonth, BorderLayout.WEST );

		
		l_month = new JLabel();
		//p1.add(l_month);
		
		/*
		l_year = new JLabel();
		p1.add(l_year);
		*/
		
		l_date = new JLabel("Date");
		l_date.setAlignmentX(0);
		p1.add(l_date, BorderLayout.CENTER );
		

		b_rmonth = new JButton(">");
		b_rmonth.addActionListener( this );
		b_rmonth.setMargin(new Insets(0,0,0,0));
		
		//b_rmonth.setFocusPainted(false);
		//b_rmonth.setOpaque(false);
		//b_rmonth.addActionListener(this);
		p1.add(b_rmonth, BorderLayout.EAST );
		
		/*
		JPanel p2 = new JPanel();
		p2.setLayout(new FlowLayout(FlowLayout.LEFT));
		top.add("East", p2);

		b_lyear = new JButton("<");
		b_lyear.addActionListener( this );
		//b_lyear.setMargin(KiwiUtils.emptyInsets);
		b_lyear.setFocusPainted(false);
		b_lyear.setOpaque(false);
		//b_lyear.addActionListener(this);
		p2.add(b_lyear);

		l_year = new JLabel();
		p2.add(l_year);

		b_ryear = new JButton(">");
		b_ryear.addActionListener( this );
		//b_ryear.setMargin(KiwiUtils.emptyInsets);
		b_ryear.setFocusPainted(false);
		b_ryear.setOpaque(false);
		//b_ryear.addActionListener(this);
		p2.add(b_ryear);
		*/
		
		add("North", top);

		calendarPane = new CalendarPane();
		calendarPane.setOpaque(false);
		add("Center", calendarPane);

		
		
		/*
		Font f = getFont();
		setFont(new Font(f.getName(), Font.BOLD, f.getSize()));
		*/
		
		int fd = date.getFirstDayOfWeek();
		weekendCols[0] = (Calendar.SUNDAY - fd + 7) % 7;
		weekendCols[1] = (Calendar.SATURDAY - fd + 7) % 7;
		

		setSelectedDate(date);
	}

	public static boolean isLeapYear(int year)
	{
		return ((((year % 4) == 0) && ((year % 100) != 0)) || ((year % 400) == 0));
	}

	private class CalendarPane extends JComponent
	{
		private int ww = 0, hh = 0, dp = 0, x0 = 0, y0 = 0;

		/** Construct a new <code>CalendarView</code>. */

		CalendarPane()
		{
			addMouseListener(new _MouseListener2());
		}

		/** Paint the component. */

		public void paint(Graphics gc)
		{
			gc.setFont( UIManager.getFont("Label.font") );
			FontMetrics fm = gc.getFontMetrics();
			Insets ins = getInsets();
			int h = fm.getMaxAscent();

			gc.setColor(Color.white);
			gc.fillRect(0,0,getSize().width,getSize().height);
			
			// figure out how many blank spaces there are before first day of month,
			// and calculate coordinates of first drawn cell

			dp = ((firstDay - selectedDate.getFirstDayOfWeek() + 7) % 7);
			int x = dp, y = 0;
			y0 = ((getSize().height - getPreferredSize().height) / 2);
			int yp = y0;
			x0 = ((getSize().width - getPreferredSize().width) / 2);
			int xp = x0;

			// paint the border

			paintBorder(gc);

			// set the clip rect to exclude the border & insets

			gc.setColor(Color.black);
			gc.clipRect(
				ins.left,
				ins.top,
				(getSize().width - ins.left - ins.right),
				(getSize().height - ins.top - ins.bottom));
			gc.translate(ins.left, ins.top);

			
			// draw the weekday headings

			for (int i = 0, ii = selectedDate.getFirstDayOfWeek() - 1; i < 7; i++)
			{
				gc.drawString(labels[ii], xp + 5 + i * (cellSize + 2), yp + h);
				if (++ii == 7)
					ii = 0;
			}

			
			yp += 20;
			xp += dp * (cellSize + 2);

			// find out how many days there are in the current month

			int month = DateChooser.this.selectedDate.get(Calendar.MONTH);
			int dmax =
				(isLeapYear(DateChooser.this.selectedDate.get(Calendar.YEAR))
					? daysInMonthLeap[month]
					: daysInMonth[month]);

			// draw all the day cells

			for (int d = 1; d <= dmax; d++)
			{
				// draw the outline of the cell

				//gc.setColor(MetalLookAndFeel.getPrimaryControlShadow());
				gc.setColor(Color.gray);

				//gc.draw3DRect(xp, yp, cellSize, cellSize, true);

				// if the cell is selected, fill it with the highlight color

				if (d == selectedDay)
				{
					gc.setColor(highlightColor);
					gc.fillRect(xp + 1, yp + 1, cellSize - 2, cellSize - 2);
				}

				// set the pen color depending on weekday or weekend, and paint the
				// day number in the cell

				if ((clipMin && (d < minDay))
					|| (clipMax && (d > maxDay))
					|| clipAllMin
					|| clipAllMax)
					gc.setColor(disabledColor);
				else
					gc.setColor(
						((weekendCols[0] == x) || (weekendCols[1] == x)) ? weekendColor : Color.black);

				String ss = String.valueOf(d);
				int sw = fm.stringWidth(ss);
				if ( d == selectedDay ) gc.setColor( UIManager.getColor("List.selectionForeground") );
				gc.drawString(ss, xp - 3 + (cellSize - sw), yp + 3 + h);

				// advance to the next cell position

				if (++x == 7)
				{
					x = 0;
					xp = x0;
					y++;
					yp += (cellSize + 2);
				}
				else
					xp += (cellSize + 2);
			}
		}

		/* Get the preferred size of the component. */

		public Dimension getPreferredSize()
		{
			Insets ins = getInsets();
			return (
				new Dimension(
					(((cellSize + 2) * 7) + ins.left + ins.right),
					(((cellSize + 2) * 6) + 20) + ins.top + ins.bottom));
		}

		/* Get the minimum size of the component. */

		public Dimension getMinimumSize()
		{
			return (getPreferredSize());
		}

		/* mouse listener */

		private class _MouseListener2 extends MouseAdapter
		{
			public void mouseReleased(MouseEvent evt)
			{
				int d = getDay(evt);
				if (d < 0)
					return;

				selectedDay = d;

				selectedDate.set(Calendar.DAY_OF_MONTH, selectedDay);
				_refresh();

				//asupport.fireActionEvent(DATE_CHANGE_CMD);
			}
		}

		/* Figure out which day the mouse click is on. */

		private int getDay(MouseEvent evt)
		{
			Insets ins = getInsets();

			int x = evt.getX() - ins.left - x0;
			int y = evt.getY() - ins.top - 20 - y0;
			int maxw = (cellSize + 2) * 7;
			int maxh = (cellSize + 2) * 6;

			// check if totally out of range.

			if ((x < 0) || (x > maxw) || (y < 0) || (y > maxh))
				return (-1);

			y /= (cellSize + 2);
			x /= (cellSize + 2);

			int d = (7 * y) + x - (dp - 1);

			if ((d < 1) || (d > selectedDate.getMaximum(Calendar.DAY_OF_MONTH)))
				return (-1);

			if ((clipMin && (d < minDay)) || (clipMax && (d > maxDay)))
				return (-1);

			return (d);
		}
	}

	/* Copy the relevant portions of a date. */

	private Calendar copyDate(Calendar source, Calendar dest)
	{
		if (dest == null)
			dest = Calendar.getInstance();

		dest.set(Calendar.YEAR, source.get(Calendar.YEAR));
		dest.set(Calendar.MONTH, source.get(Calendar.MONTH));
		dest.set(Calendar.DATE, source.get(Calendar.DATE));

		return (dest);
	}

	/** Add a <code>ActionListener</code> to this component's list of listeners.
	  *
	  * @param listener The listener to add.
	  */

	public void addActionListener(ActionListener listener)
	{
		//asupport.addActionListener(listener);
	}

	/** Remove a <code>ActionListener</code> from this component's list of
	  * listeners.
	  *
	  * @param listener The listener to remove.
	  */

	public void removeActionListener(ActionListener listener)
	{
		//asupport.removeActionListener(listener);
	}

	/** Set the highlight color for this component.
	 *
	 * @param color The new highlight color.
	 */

	public void setHighlightColor(Color color)
	{
		highlightColor = color;
	}

	/** Get the highlight color for this component.
	 *
	 * @return The current highlight color.
	 */

	public Color getHighlightColor()
	{
		return (highlightColor);
	}

	/** Get a copy of the <code>Calendar</code> object that represents the
	* currently selected date.
	*
	* @return The currently selected date.
	*/

	
	public Calendar getSelectedDate()
	{
		return ((Calendar) selectedDate.clone());
	}

	/**
	 * Set the selected date for the chooser.
	 *
	 * @param date The date to select.
	 */

	public void setSelectedDate(Calendar date)
	{
		selectedDate = copyDate(date, selectedDate);
		selectedDay = selectedDate.get(Calendar.DAY_OF_MONTH);

		_refresh();
	}

	/** Set the earliest selectable date for the chooser.
	 *
	 * @param date The (possibly <code>null</code>) minimum selectable date.
	 */

	public void setMinimumDate(Calendar date)
	{
		minDate = ((date == null) ? null : copyDate(date, minDate));
		minDay = ((date == null) ? -1 : minDate.get(Calendar.DATE));

		_refresh();
	}

	/** Get the earliest selectable date for the chooser.
	 *
	 * @return The minimum selectable date, or <code>null</code> if there is no
	 * minimum date currently set.
	 */

	public Calendar getMinimumDate()
	{
		return (minDate);
	}

	/** Set the latest selectable date for the chooser.
	 *
	 * @param date The (possibly <code>null</code>) maximum selectable date.
	 */

	public void setMaximumDate(Calendar date)
	{
		maxDate = ((date == null) ? null : copyDate(date, maxDate));
		maxDay = ((date == null) ? -1 : maxDate.get(Calendar.DATE));

		_refresh();
	}

	/** Get the latest selectable date for the chooser.
	 *
	 * @return The maximum selectable date, or <code>null</code> if there is no
	 * maximum date currently set.
	 */

	public Calendar getMaximumDate()
	{
		return (maxDate);
	}

	/**
	 * Set the format for the textual date display at the bottom of the
	 * component.
	 *
	 * @param <code>format</code> The new date format to use.
	 */

	public void setDateFormat(SimpleDateFormat format)
	{
		datefmt = format;

		_refresh();
	}

	/** Handle events. This method is public as an implementation side-effect. */

	public void actionPerformed(ActionEvent evt)
	{
		Object o = evt.getSource();

		if (o == b_lmonth)
			selectedDate.add(Calendar.MONTH, -1);

		else if (o == b_rmonth)
			selectedDate.add(Calendar.MONTH, 1);

		/*
		else if (o == b_lyear)
		{
			selectedDate.add(Calendar.YEAR, -1);
			if (minDate != null)
			{
				int m = minDate.get(Calendar.MONTH);
				if (selectedDate.get(Calendar.MONTH) < m)
					selectedDate.set(Calendar.MONTH, m);
			}
		}

		else if (o == b_ryear)
		{
			selectedDate.add(Calendar.YEAR, 1);
			if (maxDate != null)
			{
				int m = maxDate.get(Calendar.MONTH);
				if (selectedDate.get(Calendar.MONTH) > m)
					selectedDate.set(Calendar.MONTH, m);
			}
		}
		*/

		selectedDay = 1;
		selectedDate.set(Calendar.DATE, selectedDay);

		_refresh();

		/*
		asupport.fireActionEvent(((o == b_lmonth) || (o == b_rmonth))
		                         ? MONTH_CHANGE_CMD : YEAR_CHANGE_CMD);
		                */
	}

	/* Determine what day of week the first day of the month falls on. It's too
	 * bad we have to resort to this hack; the Java API provides no means of
	 * doing this any other way.
	 */

	private void _computeFirstDay()
	{
		int d = selectedDate.get(Calendar.DAY_OF_MONTH);
		selectedDate.set(Calendar.DAY_OF_MONTH, 1);
		firstDay = selectedDate.get(Calendar.DAY_OF_WEEK);
		selectedDate.set(Calendar.DAY_OF_MONTH, d);
	}

	/* This method is called whenever the month or year changes. It's job is to
	 * repaint the labels and determine whether any selection range limits have
	 * been reached.
	 */

	private void _refresh()
	{
		l_date.setText(datefmt.format(selectedDate.getTime()));
		//l_year.setText(String.valueOf(selectedDate.get(Calendar.YEAR)));
		l_month.setText(months[selectedDate.get(Calendar.MONTH)]);

		_computeFirstDay();
		clipMin = clipMax = clipAllMin = clipAllMax = false;

		//b_lyear.setEnabled(true);
		//b_ryear.setEnabled(true);
		b_lmonth.setEnabled(true);
		b_rmonth.setEnabled(true);

		// Disable anything that would cause the date to go out of range. This
		// logic is extremely sensitive so be very careful when making changes.
		// Every condition test in here is necessary, so don't remove anything.

		if (minDate != null)
		{
			int y = selectedDate.get(Calendar.YEAR);
			int y0 = minDate.get(Calendar.YEAR);
			int m = selectedDate.get(Calendar.MONTH);
			int m0 = minDate.get(Calendar.MONTH);
			
			//b_lyear.setEnabled(y > y0);
			if (y == y0)
			{
				b_lmonth.setEnabled(m > m0);

				if (m == m0)
				{
					clipMin = true;
					int d0 = minDate.get(Calendar.DATE);

					if (selectedDay < d0)
						selectedDate.set(Calendar.DATE, selectedDay = d0);

					// allow out-of-range selection
					// selectedDate.set(Calendar.DATE, selectedDay);
				}
			}

			clipAllMin = ((m < m0) || (y < y0));
		}

		if (maxDate != null)
		{
			int y = selectedDate.get(Calendar.YEAR);
			int y1 = maxDate.get(Calendar.YEAR);
			int m = selectedDate.get(Calendar.MONTH);
			int m1 = maxDate.get(Calendar.MONTH);

			//b_ryear.setEnabled(y < y1);
			if (y == y1)
			{
				b_rmonth.setEnabled(m < m1);
				if (m == m1)
				{
					clipMax = true;
					int d1 = maxDate.get(Calendar.DATE);
					if (selectedDay > d1)
						selectedDate.set(Calendar.DATE, selectedDay = d1);

					// allow out-of-range selection
					// selectedDate.set(Calendar.DATE, selectedDay);          
				}
			}

			clipAllMax = ((m > m1) || (y > y1));
		}

		// repaint the calendar pane

		calendarPane.repaint();
	}

}