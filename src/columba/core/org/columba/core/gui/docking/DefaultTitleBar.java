/*
 * Created on Jul 6, 2005
 */
package org.columba.core.gui.docking;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;

import org.columba.core.gui.base.RoundedBorder;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.layout.Sizes;

/**
 */
class DefaultTitleBar extends JPanel {

	private static final Border HOVER_BORDER = new RoundedBorder(UIManager.getColor("controlDkShadow"));

	private static final Border LINK_BORDER = BorderFactory.createEmptyBorder(
			1,1,1,1);

	protected JLabel label;

	private Vector vector = new Vector();

	public DefaultTitleBar(String text) {
		super();

		label = new JLabel(text);

		setLayout(new BorderLayout());

		layoutComponents();

		setBorder(BorderFactory.createEmptyBorder(4, 5, 0, 5));

	}

	private void layoutComponents() {
		removeAll();

		add(label, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setOpaque(false);
		ButtonBarBuilder builder = new ButtonBarBuilder(buttonPanel);

		builder.addGlue();

		for (int i = 0; i < vector.size(); i++) {
			builder.addFixedNarrow((JButton) vector.get(i));
			builder.addStrut(Sizes.pixel(2));
			//builder.addRelatedGap();
		}

		add(buttonPanel, BorderLayout.EAST);

	}

	public void addButton(JButton button) {

		button.setBorder(LINK_BORDER);
		button.addMouseListener(new LinkMouseListener());
		button.setOpaque(false);
		button.setPreferredSize(new Dimension(16,16));
		//button.setMargin(new Insets(2, 0, 2, 0));
		vector.add(button);
		layoutComponents();
	}

	public void setTitle(String title) {
		if (title == null)
			title = "";

		label.setText(title);
	}

	public String getTitle() {
		return label.getText();
	}

	protected void paintComponent(Graphics g) {

		int x = 0;
		int y = 0;

		int w = getWidth();
		int h = getHeight();

		int arc = 10;

		g.setColor(UIManager.getColor("control"));

		g.fillRect(x, y, w - 1, h);

		g.setColor(UIManager.getColor("controlDkShadow"));

		g.drawRect(x, y, w - 1, h);

	}

	private static class LinkMouseListener extends MouseAdapter {
		public void mouseEntered(MouseEvent e) {
			((JComponent) e.getComponent()).setBorder(HOVER_BORDER);
		}

		public void mouseReleased(MouseEvent e) {
			((JComponent) e.getComponent()).setBorder(LINK_BORDER);
		}

		public void mouseExited(MouseEvent e) {
			((JComponent) e.getComponent()).setBorder(LINK_BORDER);
		}
	};

}
