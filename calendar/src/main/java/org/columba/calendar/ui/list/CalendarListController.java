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
package org.columba.calendar.ui.list;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStream;
import java.util.Enumeration;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.calendar.base.api.ICalendarItem;
import org.columba.calendar.config.Config;
import org.columba.calendar.config.api.ICalendarList;
import org.columba.calendar.ui.frame.api.ICalendarMediator;
import org.columba.calendar.ui.list.api.CalendarSelectionChangedEvent;
import org.columba.calendar.ui.list.api.ICalendarListView;
import org.columba.calendar.ui.list.api.ICalendarSelectionChangedListener;
import org.columba.core.gui.menu.ExtendablePopupMenu;
import org.columba.core.gui.menu.MenuXMLDecoder;

import com.miginfocom.ashape.AShapeUtil;
import com.miginfocom.ashape.shapes.AShape;
import com.miginfocom.calendar.category.Category;
import com.miginfocom.calendar.category.CategoryDepository;
import com.miginfocom.util.gfx.GfxUtil;

/**
 * CalendarListController class
 * @author fdietz
 * 
 */
public class CalendarListController implements ICalendarListView,
		ListSelectionListener {

	private CheckableList list;

	public static final String PROP_FILTERED = "filterRow";

	private ICalendarMediator frameMediator;

	private CheckableItemListTableModel model;

	private ICalendarItem selection;

	private Category localCategory;

	private Category webCategory;

	private EventListenerList listenerList = new EventListenerList();

	private ExtendablePopupMenu menu;

	public CalendarListController(ICalendarMediator frameMediator) {
		super();

		this.frameMediator = frameMediator;

		model = new CheckableItemListTableModel();
		list = new CheckableList();
		list.setModel(model);
		list.getSelectionModel().addListSelectionListener(this);

		// create default root nodes <Local> and <Web>
		Category rootCategory = CategoryDepository.getRoot();

		localCategory = rootCategory.addSubCategory("local", "Local");
		webCategory = rootCategory.addSubCategory("web", "Web");

		loadCalendarPreferences();

		list.addMouseListener(new MyMouseListener());
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

		InputStream is = this.getClass().getResourceAsStream(
				"/org/columba/calendar/action/contextmenu_list.xml");

		menu = new MenuXMLDecoder(mediator).createPopupMenu(is);

	}

	/* (non-Javadoc)
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}

		DefaultListSelectionModel theList = (DefaultListSelectionModel) e
				.getSource();
		if (!theList.isSelectionEmpty()) {
			int index = theList.getAnchorSelectionIndex();

			selection = (ICalendarItem) ((CheckableItemListTableModel) list
					.getModel()).getElement(index);

			fireSelectionChanged(selection);

		} else {
			fireSelectionChanged(null);
		}
	}

	/**
	 * 
	 */
	private void loadCalendarPreferences() {
		ICalendarList list = Config.getInstance().getCalendarList();
		Enumeration<ICalendarItem> e = list.getElements();
		while (e.hasMoreElements()) {
			ICalendarItem item = e.nextElement();

			Category category = createCalendar(item.getId(), item.getName(),
					item.getColor().getRGB(), item.getType());

			// if (calendarId.equals("work"))
			// category.setPropertyDeep(Category.PROP_IS_HIDDEN, Boolean
			// .valueOf(false), Boolean.TRUE);
			// else
			// category.setPropertyDeep(Category.PROP_IS_HIDDEN, Boolean
			// .valueOf(true), Boolean.TRUE);

			// category filtering is disabled as default
			category.setPropertyDeep(Category.PROP_IS_HIDDEN, Boolean
					.valueOf(true), Boolean.TRUE);

			// calendar is selected as default
			item.setSelected(true);

			model.addElement(item);

		}

	}

	/**
	 * @param calendarId
	 * @param name
	 * @param colorInt
	 * @param type
	 */
	public Category createCalendar(String calendarId, String name,
			int colorInt, ICalendarItem.TYPE type) {

		Category calendar = null;
		if (type == ICalendarItem.TYPE.LOCAL)
			calendar = localCategory.addSubCategory(calendarId, name);
		else if (type == ICalendarItem.TYPE.WEB)
			calendar = webCategory.addSubCategory(calendarId, name);

		String bgName = AShapeUtil.DEFAULT_BACKGROUND_SHAPE_NAME;
		String outlineName = AShapeUtil.DEFAULT_OUTLINE_SHAPE_NAME;
		String titleName = AShapeUtil.DEFAULT_TITLE_TEXT_SHAPE_NAME;
		String textName = AShapeUtil.DEFAULT_MAIN_TEXT_SHAPE_NAME;

		Color color = new Color(colorInt);
		Color outlineColor = GfxUtil.tintColor(color, -0.4f);

		CategoryDepository.setOverride(calendarId, bgName, AShape.A_PAINT,
				GfxUtil.alphaColor(color, 145));
		CategoryDepository.setOverride(calendarId, outlineName, AShape.A_PAINT,
				outlineColor);
		CategoryDepository.setOverride(calendarId, titleName, AShape.A_PAINT,
				outlineColor);
		CategoryDepository.setOverride(calendarId, textName, AShape.A_PAINT,
				outlineColor);

		return calendar;
	}

	/* (non-Javadoc)
	 * @see org.columba.calendar.ui.list.api.ICalendarListView#getView()
	 */
	public JComponent getView() {
		return list;
	}

	class MyMouseListener extends MouseAdapter {

		MyMouseListener() {

		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseClicked(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseClicked(MouseEvent e) {
			handleEvent(e);
		}

		/**
		 * @see java.awt.event.MouseAdapter#mousePressed(java.awt.event.MouseEvent)
		 */
		@Override
		public void mousePressed(MouseEvent e) {
			handleEvent(e);
		}

		/**
		 * @see java.awt.event.MouseAdapter#mouseReleased(java.awt.event.MouseEvent)
		 */
		@Override
		public void mouseReleased(MouseEvent e) {
			handleEvent(e);
		}

		/**
		 * @param e
		 */
		private void handleEvent(MouseEvent e) {
			CheckableItemListTableModel model = (CheckableItemListTableModel) list
					.getModel();

			int count = model.getRowCount();
			for (int i = 0; i < count; i++) {
				ICalendarItem item = (ICalendarItem) model.getElement(i);
				String calendarId = item.getId();
				boolean selected = item.isSelected();

				Category category = CategoryDepository.getCategory(calendarId);
				if (category == null)
					continue;

				category.setPropertyDeep(Category.PROP_IS_HIDDEN, Boolean
						.valueOf(!selected), Boolean.TRUE);

			}

			frameMediator.fireFilterUpdated();

		}
	}

	/* (non-Javadoc)
	 * @see org.columba.calendar.ui.list.api.ICalendarListView#getSelected()
	 */
	public ICalendarItem getSelected() {
		if (selection == null)
			return null;

		return selection;
	}

	/**
	 * Adds a listener.
	 */
	public void addSelectionChangedListener(
			ICalendarSelectionChangedListener listener) {
		listenerList.add(ICalendarSelectionChangedListener.class, listener);
	}

	/**
	 * Removes a previously registered listener.
	 */
	public void removeSelectionChangedListener(
			ICalendarSelectionChangedListener listener) {
		listenerList.remove(ICalendarSelectionChangedListener.class, listener);
	}

	/**
	 * Propagates an event to all registered listeners notifying them that this
	 * folder has been renamed.
	 */
	public void fireSelectionChanged(ICalendarItem selection) {
		CalendarSelectionChangedEvent e = new CalendarSelectionChangedEvent(
				this, selection);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == ICalendarSelectionChangedListener.class) {
				((ICalendarSelectionChangedListener) listeners[i + 1])
						.selectionChanged(e);
			}
		}
	}
}
