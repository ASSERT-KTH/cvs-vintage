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

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.columba.calendar.config.Config;
import org.columba.calendar.ui.frame.CalendarFrameMediator;
import org.columba.calendar.ui.list.api.ICalendarListView;

import com.miginfocom.ashape.interaction.InteractionEvent;
import com.miginfocom.ashape.interaction.InteractionListener;
import com.miginfocom.ashape.interaction.Interactor;
import com.miginfocom.ashape.shapes.AShape;
import com.miginfocom.ashape.shapes.FillAShape;
import com.miginfocom.ashape.shapes.RootAShape;
import com.miginfocom.ashape.shapes.TextAShape;
import com.miginfocom.calendar.category.Category;
import com.miginfocom.calendar.category.CategoryDepository;
import com.miginfocom.util.MigUtil;
import com.miginfocom.util.PropertyKey;
import com.miginfocom.util.gfx.GfxUtil;
import com.miginfocom.util.gfx.RoundRectangle;
import com.miginfocom.util.gfx.ShapeGradientPaint;
import com.miginfocom.util.gfx.geometry.AbsRect;
import com.miginfocom.util.gfx.geometry.numbers.AtFraction;

/**
 * @author fdietz
 * 
 */
public class CalendarTreeController implements ICalendarListView, InteractionListener {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.calendar.ui.tree");

	private JPanel view;

	private JTree tree;

	public static final String PROP_FILTERED = "filterRow";

	private CalendarFrameMediator frameMediator;

	public CalendarTreeController(CalendarFrameMediator frameMediator) {
		super();

		this.frameMediator = frameMediator;

		// view = new JPanel();
		// view.setLayout(new BorderLayout());

		// create north label component
		// AShapeComponent labelAShape = new AShapeComponent();
		// labelAShape.setShape(createLabelShape(), true);
		// labelAShape.setPreferredSize(new Dimension(1, 20));
		// labelAShape
		// .setToolTipText("<html>Check the calendars that should be
		// visible.</html>");
		// view.add(labelAShape, BorderLayout.NORTH);

		// create default root nodes <Local> and <Web>
		Category rootCategory = CategoryDepository.getRoot();
		Category calendars = rootCategory.addSubCategory(
				Config.NODE_ID_LOCAL_ROOT.toString(), "Local");
		rootCategory.addSubCategory(Config.NODE_ID_WEB_ROOT.toString(), "Web");

		Preferences prefs = loadCalendarPreferences();

		// create tree view
		tree = new JTree(CategoryDepository.getRoot()) {
			protected void processEvent(AWTEvent e) {
				TreeCellRenderer renderer = getCellRenderer();
				if (e instanceof InputEvent
						&& renderer instanceof PrettyRenderer) {
					InputEvent ie = (InputEvent) e;

					Interactor[][] interactors = ((PrettyRenderer) renderer)
							.getInteractors();
					for (int r = 0; r < interactors.length; r++) {
						if (interactors[r] != null) {
							for (int j = 0; j < interactors[r].length; j++)
								interactors[r][j].processEvent(ie);
						}
					}

					if (ie.isConsumed() == false
							&& ie.getID() == MouseEvent.MOUSE_MOVED)
						setCursor(null);

					if (ie.isConsumed())
						return;
				}

				super.processEvent(e);
			}
		};

		TreeNode root = (TreeNode) tree.getModel().getRoot();

		// Check if sub folders and expand one level
		boolean hasRootFolders = false;
		for (int i = 0, iSz = root.getChildCount(); i < iSz; i++) {
			TreeNode node = root.getChildAt(i);
			if (node.getChildCount() > 0) {
				hasRootFolders = true;
				tree.expandPath(new TreePath(new Object[] { root, node }));
			}
		}
		tree.setShowsRootHandles(hasRootFolders);

		tree.setRootVisible(false);
		tree.setExpandsSelectedPaths(false);

		tree.setCellRenderer(new PrettyRenderer(this));

		// JScrollPane treeScrollPane = new JScrollPane(tree);
		// treeScrollPane.setBorder(null);
		// view.add(treeScrollPane, BorderLayout.CENTER);

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

				createCalendar(calendarId, name, colorInt, type);
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
	public void createCalendar(String calendarId, String name, int colorInt,
			String type) {
		Category root = CategoryDepository.getRoot();
		Category calendar = null;
		if (type.equals("local"))
			calendar = (Category) root.getChildAt(0);
		else if (type.equals("web"))
			calendar = (Category) root.getChildAt(1);

		calendar.addSubCategory(calendarId, name);

		Color color = new Color(colorInt);
		Color outlineColor = GfxUtil.tintColor(color, -0.4f);
		CategoryDepository.setOverride(calendarId, "bg", AShape.A_PAINT, color);
		CategoryDepository.setOverride(calendarId, "bgTrans", AShape.A_PAINT,
				GfxUtil.alphaColor(color, 145));
		CategoryDepository.setOverride(calendarId, "outline", AShape.A_PAINT,
				outlineColor);
		CategoryDepository
				.setOverride(calendarId, "treeCheckBox", AShape.A_PAINT,
						new ShapeGradientPaint(color, 0.2f, 115, false));
		CategoryDepository.setOverride(calendarId, "treeCheckBoxOutline",
				AShape.A_PAINT, outlineColor);
		CategoryDepository.setOverride(calendarId, "titleText", AShape.A_PAINT,
				outlineColor);
		CategoryDepository.setOverride(calendarId, "mainText", AShape.A_PAINT,
				outlineColor);
		Shape defaultShape = new RoundRectangle(0, 0, 12, 12, 6, 6);
		CategoryDepository.setOverride(calendarId, "treeCheckBox",
				AShape.A_SHAPE, defaultShape);
		CategoryDepository.setOverride(calendarId, "treeCheckBoxOutline",
				AShape.A_SHAPE, defaultShape);
	}

	public JComponent getView() {
		return tree;
	}

	/**
	 * Creates the labels shape for the tree panel
	 * 
	 * @return A new shape.
	 */
	private RootAShape createLabelShape() {
		ShapeGradientPaint bgPaint = new ShapeGradientPaint(new Color(235, 235,
				235), new Color(255, 255, 255), 90, 0.7f, 0.61f, false);
		Color textColor = new Color(50, 50, 50);
		Font labFont = new Font("Arial", Font.BOLD, 12);

		RootAShape root = new RootAShape();
		FillAShape bgAShape = new FillAShape("treeLabelFill", new Rectangle(0,
				0, 1, 1), new AbsRect(SwingConstants.TOP, new Integer(25)),
				bgPaint, Boolean.FALSE);
		TextAShape labelTextAShape = new TextAShape("treeLabelText",
				"Calendars", AbsRect.FILL, TextAShape.TYPE_SINGE_LINE, labFont,
				textColor, new AtFraction(0.5f), new AtFraction(0.40f),
				Boolean.TRUE);

		bgAShape.addSubShape(labelTextAShape);
		root.addSubShape(bgAShape);

		return root;
	}

	public void interactionOccured(InteractionEvent e) {
		Object value = e.getCommand().getValue();
		if (MigUtil.equals(value, "selectedCheckPressed")) {

			Category category = (Category) e.getInteractor().getInteracted();

			// Toggle selection
			boolean hidden = MigUtil.isTrue(category
					.getProperty(Category.PROP_IS_HIDDEN));

			category.setPropertyDeep(Category.PROP_IS_HIDDEN, Boolean
					.valueOf(!hidden), Boolean.TRUE);

			frameMediator.getCalendarView().recreateFilterRows();

		} else if (MigUtil.equals(value, "categorizeOnPressed")) {

			Category category = (Category) e.getInteractor().getInteracted();

			// Deselect all others
			if (e.getSourceEvent().isShiftDown() == false) {
				Collection cats = CategoryDepository.getRoot()
						.getChildrenDeep();
				for (Iterator it = cats.iterator(); it.hasNext();) {
					Category tmpCat = (Category) it.next();
					if (tmpCat != category)
						tmpCat.setProperty(PropertyKey
								.getKey(CalendarFrameMediator.PROP_FILTERED),
								null, null);
				}
			}

			// Toggle selection
			boolean catOn = MigUtil.isTrue(category.getProperty(PropertyKey
					.getKey(CalendarFrameMediator.PROP_FILTERED)));
			category.setProperty(PropertyKey
					.getKey(CalendarFrameMediator.PROP_FILTERED), Boolean
					.valueOf(!catOn), Boolean.TRUE);

			frameMediator.getCalendarView().recreateFilterRows();

		}
	}

}
