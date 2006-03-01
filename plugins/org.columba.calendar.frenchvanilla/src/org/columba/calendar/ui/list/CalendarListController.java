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
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.DefaultListSelectionModel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.calendar.config.Config;
import org.columba.calendar.ui.frame.CalendarFrameMediator;
import org.columba.calendar.ui.list.api.ICalendarListView;
import org.frapuccino.checkablelist.CheckableItemListTableModel;
import org.frapuccino.checkablelist.CheckableList;

import com.miginfocom.ashape.AShapeUtil;
import com.miginfocom.ashape.shapes.AShape;
import com.miginfocom.calendar.category.Category;
import com.miginfocom.calendar.category.CategoryDepository;
import com.miginfocom.util.gfx.GfxUtil;

/**
 * @author fdietz
 * 
 */
public class CalendarListController implements ICalendarListView,
		ListSelectionListener {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.calendar.ui.tree");

	private JPanel view;

	// private JTree tree;
	private CheckableList list;

	public static final String PROP_FILTERED = "filterRow";

	private CalendarFrameMediator frameMediator;

	// private CheckTreeManager checkTreeManager;
	//	
	// private CategoryTreeNode rootNode;
	// private CategoryTreeNode localNode;
	// private CategoryTreeNode webNode;

	private CheckableItemListTableModel model;

	private CalendarItem selection;

	private Category localCategory;

	private Category webCategory;

	public CalendarListController(CalendarFrameMediator frameMediator) {
		super();

		this.frameMediator = frameMediator;

		// rootNode = new CategoryTreeNode("root");

		model = new CheckableItemListTableModel();
		list = new CheckableList();
		list.setModel(model);
		list.getSelectionModel().addListSelectionListener(this);

		// tree = new JTree(rootNode);
		// tree.setCellRenderer(new ColorTreeCellRenderer());
		//		
		// checkTreeManager = new CheckTreeManager(tree, false);

		// create default root nodes <Local> and <Web>
		Category rootCategory = CategoryDepository.getRoot();

		localCategory = rootCategory.addSubCategory(Config.NODE_ID_LOCAL_ROOT
				.toString(), "Local");
		webCategory = rootCategory.addSubCategory(Config.NODE_ID_WEB_ROOT
				.toString(), "Web");

		// localNode = new CategoryTreeNode("local", "Local", Color.black);
		// rootNode.add(localNode);
		//
		// webNode = new CategoryTreeNode("web", "Web", Color.black);
		// rootNode.add(webNode);

		// localNode = new CategoryTreeNode("Local");
		// rootNode.add(localNode);
		// webNode = new CategoryTreeNode("Web");
		// rootNode.add(webNode);

		Preferences prefs = loadCalendarPreferences();

		// create tree view
		// tree = new JTree(CategoryDepository.getRoot()) {
		// protected void processEvent(AWTEvent e) {
		// TreeCellRenderer renderer = getCellRenderer();
		// if (e instanceof InputEvent
		// && renderer instanceof PrettyRenderer) {
		// InputEvent ie = (InputEvent) e;
		//
		// Interactor[][] interactors = ((PrettyRenderer) renderer)
		// .getInteractors();
		// for (int r = 0; r < interactors.length; r++) {
		// if (interactors[r] != null) {
		// for (int j = 0; j < interactors[r].length; j++)
		// interactors[r][j].processEvent(ie);
		// }
		// }
		//
		// if (ie.isConsumed() == false
		// && ie.getID() == MouseEvent.MOUSE_MOVED)
		// setCursor(null);
		//
		// if (ie.isConsumed())
		// return;
		// }
		//
		// super.processEvent(e);
		// }
		// };
		//
		// TreeNode root = (TreeNode) tree.getModel().getRoot();
		//
		// // Check if sub folders and expand one level
		// boolean hasRootFolders = false;
		// for (int i = 0, iSz = root.getChildCount(); i < iSz; i++) {
		// TreeNode node = root.getChildAt(i);
		// if (node.getChildCount() > 0) {
		// hasRootFolders = true;
		// tree.expandPath(new TreePath(new Object[] { root, node }));
		// }
		// }

		// for(int i=0; i<tree.getRowCount(); i++)
		// tree.expandRow(i);
		//        
		// tree.setShowsRootHandles(false);
		//
		// tree.setRootVisible(false);
		//		
		// tree.setExpandsSelectedPaths(false);

		// tree.setCellRenderer(new PrettyRenderer(this));

		list.addMouseListener(new MyMouseListener());
	}

	public void valueChanged(ListSelectionEvent e) {
		if (e.getValueIsAdjusting()) {
			return;
		}

		DefaultListSelectionModel theList = (DefaultListSelectionModel) e
				.getSource();
		if (!theList.isSelectionEmpty()) {
			int index = theList.getAnchorSelectionIndex();

			selection = (CalendarItem) ((CheckableItemListTableModel) list
					.getModel()).getElement(index);

			// String calendarId = selection.getId();
			//
			// Category root = CategoryDepository.getRoot();
			// Category category = CategoryDepository.getCategory(calendarId);
			// if (category == null)
			// return;
			//
			// boolean selected = selection.isSelected();
			// System.out.println("caleandar=" + calendarId + " selected="
			// + selected);
			//
			// category.setPropertyDeep(Category.PROP_IS_HIDDEN, Boolean
			// .valueOf(!selected), Boolean.TRUE);
			//
			// frameMediator.getCalendarView().recreateFilterRows();
		}
	}

	/**
	 * @return
	 * @throws BackingStoreException
	 */
	private Preferences loadCalendarPreferences() {

		try {
			Preferences prefs = Config.getInstance().getCalendarOptions();
			String[] children = prefs.childrenNames();
			for (int i = 0; i < children.length; i++) {
				String calendarId = children[i];
				Preferences childNode = prefs.node(calendarId);
				String[] keys = childNode.keys();
				String name = childNode.get(Config.CALENDAR_NAME, null);
				int colorInt = childNode.getInt(Config.CALENDAR_COLOR, -1);
				String type = childNode.get(Config.CALENDAR_TYPE, "local");
				boolean selected = childNode.getBoolean(Config.CALENDAR_SELECTED, true);
				
				Category category = createCategory(calendarId, name, colorInt,
						type, selected);

				model.addElement(new CalendarItem(calendarId, name, new Color(
						colorInt), selected));

				// if ( type.equals("local"))
				// localNode.add(new CategoryTreeNode(calendarId, name, new
				// Color(colorInt)));
				// else
				// webNode.add(new CategoryTreeNode(calendarId, name, new
				// Color(colorInt)));
			}

			return prefs;
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * @param calendarId
	 * @param name
	 * @param colorInt
	 * @param type
	 */
	public Category createCategory(String calendarId, String name,
			int colorInt, String type, boolean selected) {

		Category root = CategoryDepository.getRoot();
		Category calendar = null;
		if (type.equals("local"))
			calendar = localCategory.addSubCategory(calendarId, name);
		else if (type.equals("web"))
			calendar = webCategory.addSubCategory(calendarId, name);

		calendar.setPropertyDeep(Category.PROP_IS_HIDDEN, Boolean
				.valueOf(!selected), Boolean.TRUE);
		
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

		
		
		// CategoryDepository
		// .setOverride(calendarId, "treeCheckBox", AShape.A_PAINT,
		// new ShapeGradientPaint(color, 0.2f, 115, false));
		// CategoryDepository.setOverride(calendarId, "treeCheckBoxOutline",
		// AShape.A_PAINT, outlineColor);
		// CategoryDepository.setOverride(calendarId, "titleText",
		// AShape.A_PAINT,
		// outlineColor);
		// CategoryDepository.setOverride(calendarId, "mainText",
		// AShape.A_PAINT,
		// outlineColor);

		// Shape defaultShape = new RoundRectangle(0, 0, 12, 12, 6, 6);
		// CategoryDepository.setOverride(calendarId, "treeCheckBox",
		// AShape.A_SHAPE, defaultShape);
		// CategoryDepository.setOverride(calendarId, "treeCheckBoxOutline",
		// AShape.A_SHAPE, defaultShape);

		return calendar;
	}

	public JComponent getView() {
		return list;
	}

	/**
	 * Creates the labels shape for the tree panel
	 * 
	 * @return A new shape.
	 */
	// private RootAShape createLabelShape() {
	// ShapeGradientPaint bgPaint = new ShapeGradientPaint(new Color(235, 235,
	// 235), new Color(255, 255, 255), 90, 0.7f, 0.61f, false);
	// Color textColor = new Color(50, 50, 50);
	// Font labFont = new Font("Arial", Font.BOLD, 12);
	//
	// RootAShape root = new RootAShape();
	// FillAShape bgAShape = new FillAShape("treeLabelFill", new Rectangle(0,
	// 0, 1, 1), new AbsRect(SwingConstants.TOP, new Integer(25)),
	// bgPaint, Boolean.FALSE);
	// TextAShape labelTextAShape = new TextAShape("treeLabelText",
	// "Calendars", AbsRect.FILL, TextAShape.TYPE_SINGE_LINE, labFont,
	// textColor, new AtFraction(0.5f), new AtFraction(0.40f),
	// Boolean.TRUE);
	//
	// bgAShape.addSubShape(labelTextAShape);
	// root.addSubShape(bgAShape);
	//
	// return root;
	// }
	// public void interactionOccured(InteractionEvent e) {
	// Object value = e.getCommand().getValue();
	// if (MigUtil.equals(value, "selectedCheckPressed")) {
	//
	// Category category = (Category) e.getInteractor().getInteracted();
	//
	// // Toggle selection
	// boolean hidden = MigUtil.isTrue(category
	// .getProperty(Category.PROP_IS_HIDDEN));
	//
	// category.setPropertyDeep(Category.PROP_IS_HIDDEN, Boolean
	// .valueOf(!hidden), Boolean.TRUE);
	//
	// frameMediator.getCalendarView().recreateFilterRows();
	//
	// } else if (MigUtil.equals(value, "categorizeOnPressed")) {
	//
	// Category category = (Category) e.getInteractor().getInteracted();
	//
	// // Deselect all others
	// if (e.getSourceEvent().isShiftDown() == false) {
	// Collection cats = CategoryDepository.getRoot()
	// .getChildrenDeep();
	// for (Iterator it = cats.iterator(); it.hasNext();) {
	// Category tmpCat = (Category) it.next();
	// if (tmpCat != category)
	// tmpCat.setProperty(PropertyKey
	// .getKey(CalendarFrameMediator.PROP_FILTERED),
	// null, null);
	// }
	// }
	//
	// // Toggle selection
	// boolean catOn = MigUtil.isTrue(category.getProperty(PropertyKey
	// .getKey(CalendarFrameMediator.PROP_FILTERED)));
	// category.setProperty(PropertyKey
	// .getKey(CalendarFrameMediator.PROP_FILTERED), Boolean
	// .valueOf(!catOn), Boolean.TRUE);
	//
	// frameMediator.getCalendarView().recreateFilterRows();
	//
	// }
	// }
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

		private void handleEvent(MouseEvent e) {
			Point point = e.getPoint();

			CheckableItemListTableModel model = (CheckableItemListTableModel) list
					.getModel();

			int count = model.getRowCount();
			for (int i = 0; i < count; i++) {
				CalendarItem item = (CalendarItem) model.getElement(i);
				String calendarId = item.getId();
				boolean selected = item.isSelected();

				Category category = CategoryDepository.getCategory(calendarId);
				if (category == null)
					continue;

				category.setPropertyDeep(Category.PROP_IS_HIDDEN, Boolean
						.valueOf(!selected), Boolean.TRUE);

			}

			frameMediator.getCalendarView().recreateFilterRows();

		}
	}

	/**
	 * @see org.columba.calendar.ui.list.api.ICalendarListView#getSelectedId()
	 */
	public String getSelectedId() {
		if ( selection == null ) return null;
		
		return selection.getId();
	}
}
