/*
 * Created on Jul 6, 2005
 */
package org.columba.core.gui.docking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import org.columba.core.gui.base.AscendingIcon;

/**
 */
class TitleBar extends JPanel {

	protected JLabel label;

	protected JButton button;

	public TitleBar() {
		super();
		init();
	}

	public TitleBar(String text) {
		super();

		label = new JLabel(text);

		init();
	}

	public TitleBar(String text, Color bgColor) {
		super();
		label = new JLabel(text);

		init();

		setBackground(bgColor);
	}

	private void init() {

		ImageIcon icon = new AscendingIcon();
		button = new JButton(icon);
		button.setMargin(new Insets(0, 1, 0, 0));
		button.setFocusable(false);
		// FIXME: it seems this only works for JToolBar
		// button.setRolloverEnabled(true);
		// button.putClientProperty("JToolBar.isRollover", Boolean.TRUE);
		setLayout(new BorderLayout());

		add(label, BorderLayout.CENTER);
		add(button, BorderLayout.EAST);

		setOpaque(true);
		setBorder(new EmptyBorder(2, 4, 2, 2));
	}

	protected void paintBorder(Graphics g) {
		int w = getWidth();
		int h = getHeight();

		g.setColor(getBackground().brighter());
		g.drawLine(0, 0, w, 0);
		g.drawLine(0, 0, 0, h);

		g.setColor(getBackground().darker());
		g.drawLine(0, h, w, h);
	}

	public void setTitle(String title) {
		if (title == null)
			title = "";

		label.setText(title);
	}

	public String getTitle() {
		return label.getText();
	}

	public JButton getButton() {
		return button;
	}

}
