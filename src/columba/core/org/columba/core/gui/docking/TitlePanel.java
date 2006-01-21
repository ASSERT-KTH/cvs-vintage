/*
 * Created on Jun 24, 2005
 */
package org.columba.core.gui.docking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

/**
 */
class TitlePanel extends JPanel {
	protected DefaultTitleBar titlebar;
	private JComponent contentPane;

	public TitlePanel(String title) {
		setLayout(new BorderLayout());

		titlebar = createTitlebar(title);
		add(titlebar, BorderLayout.NORTH);
		setContentPane(createContentPane());

	}

	public String getTitle() {
		return titlebar.getTitle();
	}

	public void setTitle(String title) {
		titlebar.setTitle(title);
	}

	public JPanel getTitleBar() {
		return titlebar;
	}

	protected DefaultTitleBar createTitlebar(String title) {
		return new DefaultTitleBar(title);
	}

	public void setContentPane(JComponent comp) {
		if (contentPane != null)
			remove(contentPane);
		if (comp != null)
			add(comp, BorderLayout.CENTER);
		contentPane = comp;
	}

	protected JComponent createContentPane() {
		JPanel pane = new JPanel();
//		pane.setBorder(new LineBorder(Color.DARK_GRAY));
//		pane.setBackground(Color.WHITE);
		return pane;
	}

}
