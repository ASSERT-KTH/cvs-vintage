// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.gui.util.treetable;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.plaf.metal.MetalTreeUI;
import javax.swing.tree.TreePath;

public class ColumbaTreeUI extends MetalTreeUI {
	final static float dash1[] = { 1.1f };
	final static BasicStroke dashed =
		new BasicStroke(
			0.5f,
			BasicStroke.CAP_BUTT,
			BasicStroke.JOIN_ROUND,
			10.0f,
			dash1,
			0.0f);

	public ColumbaTreeUI() {
		super();
	}

	public static ComponentUI createUI(JComponent c) {

		return new ColumbaTreeUI();
	}

	protected void installDefaults() {
		super.installDefaults();

		setExpandedIcon(new ExpandedIcon(true));
		setCollapsedIcon(new ExpandedIcon(false));

	}

	protected void paintVerticalLine(
		Graphics g,
		JComponent c,
		int x,
		int top,
		int bottom) {

		Graphics2D graphics = (Graphics2D) g;

		
		g.setColor(MetalLookAndFeel.getControlDarkShadow());

		drawDashedVerticalLine(g, x, top, bottom);
	}

	protected void paintHorizontalLine(
		Graphics g,
		JComponent c,
		int y,
		int left,
		int right) {
		Graphics2D graphics = (Graphics2D) g;

		g.setColor(MetalLookAndFeel.getControlDarkShadow());

		drawDashedHorizontalLine(g, y, left, right);

	}

	protected MouseListener createMouseListener() {
		return new ColumbaTreeMouseHandler();
	}

	/**
	* TreeMouseListener is responsible for updating the selection
	* based on mouse events.
	*/
	public class ColumbaTreeMouseHandler
		extends MouseAdapter
		implements MouseMotionListener {
		/**
		 * Invoked when a mouse button has been pressed on a component.
		 */

		/*
		public void mousePressed(MouseEvent e)
		{
			
			if (! e.isConsumed()) {
			handleSelection(e);
			selectedOnPress = true;
			} else {
			selectedOnPress = false;
			}
			
		}
		*/

		public void mouseClicked(MouseEvent e) {

			if (!e.isConsumed()) {
				handleSelection(e);
				selectedOnPress = true;
			} else {
				selectedOnPress = false;
			}

		}

		void handleSelection(MouseEvent e) {
			if (tree != null && tree.isEnabled()) {
				if (isEditing(tree)
					&& tree.getInvokesStopCellEditing()
					&& !stopEditing(tree)) {
					return;
				}

				if (tree.isRequestFocusEnabled()) {
					tree.requestFocus();
				}
				TreePath path =
					getClosestPathForLocation(tree, e.getX(), e.getY());

				if (path != null) {
					Rectangle bounds = getPathBounds(tree, path);

					if (e.getY() > (bounds.y + bounds.height)) {
						return;
					}

					// Preferably checkForClickInExpandControl could take
					// the Event to do this it self!
					if (SwingUtilities.isLeftMouseButton(e))
						checkForClickInExpandControl(path, e.getX(), e.getY());

					int x = e.getX();

					// Perhaps they clicked the cell itself. If so,
					// select it.
					if (x > bounds.x) {
						if (x <= (bounds.x + bounds.width)
							&& !startEditing(path, e)) {
							selectPathForEvent(path, e);
						}
					}
					// PENDING: Should select on mouse down, start a drag if
					// the mouse moves, and fire selection change notice on
					// mouse up. That is, the explorer highlights on mouse
					// down, but doesn't update the pane to the right (and
					// open the folder icon) until mouse up.
				}
			}
		}

		public void mouseDragged(MouseEvent e) {
		}

		/**
		* Invoked when the mouse button has been moved on a component
		* (with no buttons no down).
		*/
		public void mouseMoved(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
			if ((!e.isConsumed()) && (!selectedOnPress)) {
				handleSelection(e);
			}
		}

		boolean selectedOnPress;
	} // End of BasicTreeUI.MouseHandler

}