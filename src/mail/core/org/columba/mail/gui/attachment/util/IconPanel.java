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
package org.columba.mail.gui.attachment.util;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.MouseInputListener;

public class IconPanel extends JPanel implements MouseInputListener {
	int count;
	Vector selection;
	ArrayList selectionListener;
	Action doubleClickAction;

	public IconPanel() {
		super(new FlowLayout(FlowLayout.LEFT, 20, 5));

		setOpaque(true);
		setBackground(UIManager.getColor("List.background"));

		addMouseListener(this);
		addMouseMotionListener(this);

		count = 0;
		selection = new Vector();
		selectionListener = new ArrayList();		
	}

	public void updateUI() {
		super.updateUI();
		setBackground(UIManager.getColor("List.background"));
	}

	public void setDoubleClickAction(Action a) {
		doubleClickAction = a;
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

		fireSelectionChanged();

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
		if ((e.getModifiers() & MouseEvent.BUTTON1_MASK) != 0) {

			if (e.isControlDown()) {
				select(e.getPoint(), 1);
				return;
			}

			select(e.getPoint(), 0);

			if (e.getClickCount() >= 2) {
				if( doubleClickAction != null)
					doubleClickAction.actionPerformed(null);
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

	public void addIconPanelSelectionListener( IconPanelSelectionListener listener ) {
		selectionListener.add(listener);
	}

	private void fireSelectionChanged() {
		
		int[] newSelection = getSelection();
		
		for( int i=0; i<selectionListener.size(); i++) {
			((IconPanelSelectionListener) selectionListener.get(i)).selectionChanged(newSelection);
		}
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