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
package org.columba.core.gui.base;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;

public class JStatusBar extends JPanel {

	private JPanel contentPanel = new JPanel();

	private FormLayout layout;

	private int layoutCoordinateX = 2;

	private int layoutCoordinateY = 2;

	public JStatusBar() {

		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(getWidth(), 23));

		JLabel resizeIconLabel = new JLabel(
				new TriangleSquareWindowsCornerIcon());
		resizeIconLabel.setOpaque(false);

		JPanel rightPanel = new JPanel();
		rightPanel.setOpaque(false);
		rightPanel.add(resizeIconLabel, BorderLayout.SOUTH);

		add(rightPanel, BorderLayout.EAST);

		contentPanel.setOpaque(false);
		layout = new FormLayout("2dlu, pref:grow", "3dlu, fill:10dlu, 2dlu");
		contentPanel.setLayout(layout);

		add(contentPanel, BorderLayout.CENTER);

		//setBackground(new Color(236, 233, 216));
	}

	public void setMainLeftComponent(JComponent c) {
		contentPanel.add(c, new CellConstraints(2, 2));
	}

	public void addRightComponent(JComponent c, int dialogUnits) {
		layout.appendColumn(new ColumnSpec("2dlu"));
		layout.appendColumn(new ColumnSpec(dialogUnits + "dlu"));

		layoutCoordinateX++;

		contentPanel.add(new SeparatorPanel(Color.GRAY, Color.WHITE),
				new CellConstraints(layoutCoordinateX, layoutCoordinateY));
		layoutCoordinateX++;

		contentPanel.add(c, new CellConstraints(layoutCoordinateX,
				layoutCoordinateY));
	}
	
	public void addRightComponent(JComponent c) {
		layout.appendColumn(new ColumnSpec("2dlu"));
		layout.appendColumn(new ColumnSpec("default"));

		layoutCoordinateX++;

		contentPanel.add(new SeparatorPanel(Color.GRAY, Color.WHITE),
				new CellConstraints(layoutCoordinateX, layoutCoordinateY));
		layoutCoordinateX++;

		contentPanel.add(c, new CellConstraints(layoutCoordinateX,
				layoutCoordinateY));
	}
	

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int y = 0;
		g.setColor(new Color(156, 154, 140));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(196, 194, 183));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(218, 215, 201));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(233, 231, 217));
		g.drawLine(0, y, getWidth(), y);

		y = getHeight() - 3;
		g.setColor(new Color(233, 232, 218));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(233, 231, 216));
		g.drawLine(0, y, getWidth(), y);
		y = getHeight() - 1;
		g.setColor(new Color(221, 221, 220));
		g.drawLine(0, y, getWidth(), y);

	}

	class SeparatorPanel extends JPanel {
		private Color leftColor;

		private Color rightColor;

		SeparatorPanel(Color l, Color r) {
			this.leftColor = l;
			this.rightColor = r;
			setOpaque(false);
		}

		protected void paintComponent(Graphics g) {
			g.setColor(leftColor);
			g.drawLine(0, 0, 0, getHeight());
			g.setColor(rightColor);
			g.drawLine(1, 0, 1, getHeight());
		}
	}
}