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

package org.columba.mail.gui.attachment.util;

import javax.swing.*;
import javax.swing.event.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;

public class IconPanel extends JPanel implements MouseInputListener {
	int count;
	Vector selection;
	String doubleClickActionCommand;
	Vector actionListeners;

	public IconPanel() {
		super(new FlowLayout(FlowLayout.LEFT, 20, 5));

		setOpaque(true);
		setBackground(UIManager.getColor("List.background"));

		addMouseListener(this);
		addMouseMotionListener(this);

		count = 0;
		selection = new Vector();
		actionListeners = new Vector();
	}

	public void updateUI() {
		super.updateUI();
		setBackground(UIManager.getColor("List.background"));
	}

	public void setDoubleClickActionCommand(String a) {
		doubleClickActionCommand = a;
	}

	public void addActionListener(ActionListener l) {
		actionListeners.add(l);
	}

	public void removeActionListener(ActionListener l) {
		actionListeners.remove(l);
	}

	private void fireDoubleClickAction() {
		ActionEvent da =
			new ActionEvent(this, ActionEvent.ACTION_FIRST, doubleClickActionCommand);

		for (int i = 0; i < actionListeners.size(); i++) {
			((ActionListener) actionListeners.get(i)).actionPerformed(da);
		}
	}

	protected void addItem(ClickableIcon icon) {
		super.add(icon);
	}

	public void add(Icon image, String text) {
		addItem(new ClickableIcon(image, text, count));
		count++;

		revalidate();
		repaint();

	}

	public void removeAll() {
		super.removeAll();
		count = 0;

		selection.removeAllElements();

		revalidate();
		repaint();
	}

	public void removeSelected() {
		for (int i = 0; i < selection.size(); i++) {
			super.remove((ClickableIcon) selection.get(i));
		}

		count -= selection.size();

		selection.removeAllElements();

		revalidate();
		repaint();
	}

	public int getSelected() {
		if (selection.size() != 0)
			return ((ClickableIcon) selection.get(0)).getIndex();

		return -1;
	}

	public int countSelected() {
		return selection.size();
	}

	public int[] getSelection() {
		int[] output = new int[selection.size()];

		for (int i = 0; i < selection.size(); i++) {
			output[i] = ((ClickableIcon) selection.get(i)).getIndex();
		}

		return output;
	}

	public void select(Point pos, int mode) {
		Object clicked;
		ClickableIcon aktIcon;

		clicked = getComponentAt(pos);

		if (clicked.getClass().isInstance(new ClickableIcon(null, null, 0))) {
			aktIcon = (ClickableIcon) clicked;

			switch (mode) {
				case (0) :
					{
						clearSelection();
						selection.add(aktIcon);
						aktIcon.setSelection(true);

						break;
					}
				case (1) :
					{
						if (selection.contains(aktIcon)) {
							selection.remove(aktIcon);
							aktIcon.setSelection(false);
						} else {
							selection.add(aktIcon);
							aktIcon.setSelection(true);
						}
						break;
					}

			}

		} else {
			if (mode == 0)
				clearSelection();
		}

		revalidate();
		repaint();
	}

	private void clearSelection() {
		for (int i = 0; i < selection.size(); i++) {
			((ClickableIcon) selection.get(i)).setSelection(false);

		}

		selection.removeAllElements();
	}

	public void mouseClicked(MouseEvent e) {
	}
	public void mouseEntered(MouseEvent e) {
	}
	public void mouseExited(MouseEvent e) {
	}

	public void mousePressed(MouseEvent e) {
		if ((e.getModifiers() & e.BUTTON1_MASK) != 0) {

			if (e.isControlDown()) {
				select(e.getPoint(), 1);
				return;
			}

			select(e.getPoint(), 0);

			if (e.getClickCount() >= 2) {
				fireDoubleClickAction();
			}
		}

	}

	public void mouseReleased(MouseEvent e) {
	}

	public void mouseDragged(MouseEvent e) {
		/*        Graphics g = getGraphics();
		        
		        if( saveRegion!= null ) {
		            g.drawImage( saveRegion, 0,0,null ) ;
		        }
		        
		        saveRegion = createImage( getWidth(), getHeight() );
		        
		        g.drawRect(selectionPoint.x,
		                   selectionPoint.y,
		                   e.getPoint().x - selectionPoint.x,
		                   e.getPoint().y - selectionPoint.y);
		*/
	}

	public void mouseMoved(MouseEvent e) {
	}

}

class ClickableIcon extends JComponent {
	private boolean selected;
	private int index;
	private Color selectionForeground;
	private Color selectionBackground;
	private Color foreground;
	private Color background;

	private JLabel icon;
	private JLabel label;

	public ClickableIcon(Icon image, String text, int index) {
		selectionForeground = UIManager.getColor("List.selectionForeground");
		selectionBackground = UIManager.getColor("List.selectionBackground");
		foreground = UIManager.getColor("List.foreground");
		background = UIManager.getColor("List.background");

		//setLayout( new GridLayout(2,1) );
		setLayout(new BorderLayout());

		label = new JLabel(text);
		label.setOpaque(true);
		label.setBackground(background);
		label.setForeground(foreground);

		icon = new JLabel(image);
		icon.setOpaque(true);
		icon.setBackground(background);
		icon.setForeground(foreground);

		add(icon, BorderLayout.CENTER);
		add(label, BorderLayout.SOUTH);

		selected = false;

		this.index = index;
	}

	public void setSelection(boolean set) {
		selected = set;

		if (set) {
			label.setForeground(selectionForeground);
			label.setBackground(selectionBackground);
		} else {
			label.setBackground(background);
			label.setForeground(foreground);
		}
	}

	public boolean isSelected() {
		return selected;
	}

	public int getIndex() {
		return index;
	}

}