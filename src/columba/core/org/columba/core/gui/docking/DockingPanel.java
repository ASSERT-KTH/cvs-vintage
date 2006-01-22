package org.columba.core.gui.docking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

import org.columba.core.gui.base.AscendingIcon;
import org.columba.core.gui.base.ShadowBorder;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingStub;

class DockingPanel extends JPanel implements DockingStub {

	private static Color INACTIVE_BORDER_COLOR = UIManager
			.getColor("controlDkShadow");

	private static Color ACTIVE_BORDER_COLOR = UIManager
			.getColor("Menu.selectionBackground");

	public static Color ACTIVE_MID_COLOR = UIManager.getColor(
			"Menu.selectionBackground").brighter();

	public static Color ACTIVE_FILL_COLOR = UIManager
			.getColor("Menu.selectionBackground");

	public static Color ACTIVE_START_COLOR = UIManager
			.getColor("Menu.selectionBackground");

	public static Color INACTIVE_FILL_COLOR = UIManager.getColor("control");

	public static Color INACTIVE_MID_COLOR = UIManager.getColor("control");

	public static Color INACTIVE_START_COLOR = UIManager
			.getColor("controlHighlight");

	public static Color INACTIVE_LABEL_COLOR = UIManager
			.getColor("Menu.selectionForeground");

	public static Color ACTIVE_LABEL_COLOR = UIManager
			.getColor("Menu.selectionForeground");

	protected TitleBar titlebar;

	private JPanel contentPanel;

	private Dockable dockable;

	private String dockingId;

	private boolean active = false;

	public DockingPanel(String id, String title) {
		super();

		this.dockingId = id;

		this.titlebar = new TitleBar(title, INACTIVE_MID_COLOR,
				INACTIVE_START_COLOR, INACTIVE_FILL_COLOR);

		setLayout(new BorderLayout());

		add(this.titlebar, BorderLayout.NORTH);

		contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		Border border = BorderFactory.createCompoundBorder(new LineBorder(
				INACTIVE_BORDER_COLOR), BorderFactory.createEmptyBorder(1, 1,
				1, 1));

		add(contentPanel, BorderLayout.CENTER);

		setContentPane(createContentPane());

		setBorder(new ShadowBorder());

		titlebar.setMidColor(INACTIVE_MID_COLOR);
		titlebar.setFillColor(INACTIVE_FILL_COLOR);
		titlebar.setStartColor(INACTIVE_START_COLOR);

		titlebar.setActiveTitleColor(INACTIVE_LABEL_COLOR, INACTIVE_MID_COLOR);

	}

	public void setActive(boolean active) {
		this.active = active;

		if (active) {
			// contentPanel.setBorder(new LineBorder(ACTIVE_BORDER_COLOR, 1));

			titlebar.setMidColor(ACTIVE_MID_COLOR);
			titlebar.setFillColor(ACTIVE_FILL_COLOR);
			titlebar.setStartColor(ACTIVE_START_COLOR);

			titlebar.setActiveTitleColor(ACTIVE_LABEL_COLOR, ACTIVE_FILL_COLOR);
		} else {
			Border border = BorderFactory.createCompoundBorder(new LineBorder(
					INACTIVE_BORDER_COLOR), BorderFactory.createEmptyBorder(1,
					1, 1, 1));

			// contentPanel.setBorder(border);

			titlebar.setMidColor(INACTIVE_MID_COLOR);
			titlebar.setFillColor(INACTIVE_FILL_COLOR);
			titlebar.setStartColor(INACTIVE_START_COLOR);

			titlebar.setActiveTitleColor(INACTIVE_LABEL_COLOR,
					INACTIVE_MID_COLOR);
		}

		contentPanel.repaint();
		titlebar.repaint();
	}

	public void dock(DockingPanel otherPanel) {
		DockingManager.dock(otherPanel, this);
	}

	public void dock(DockingPanel otherPanel, String region) {
		DockingManager.dock(otherPanel, this, region);
	}

	public void dock(DockingPanel otherPanel, String region, float ratio) {
		DockingManager.dock(otherPanel, this, region, ratio);
	}

	protected JComponent createContentPane() {
		JPanel panel = new JPanel();
		return panel;
	}

	public String getTitle() {
		return titlebar.getTitle();
	}

	public void setTitle(String title) {
		titlebar.setTitle(title);
	}

	public TitleBar getTitleBar() {
		return titlebar;
	}

	public void setContentPane(JComponent comp) {
		contentPanel.removeAll();

		if (comp != null)
			contentPanel.add(comp, BorderLayout.CENTER);

		// this.contentPane = comp;

		// Color color = UIManager.getColor("controlDkShadow");
		//
		// if (comp != null)
		// contentPane.setBorder(new LineBorder(color));
	}

	public void setTitleBar(JPanel panel) {
		add(panel, BorderLayout.NORTH);
	}

	public Component getDragSource() {
		return getTitleBar();
	}

	public Component getFrameDragSource() {
		return getTitleBar();
	}

	public String getPersistentId() {
		return dockingId;
	}

	public String getTabText() {
		return getTitle();
	}

	public void updateUI() {
		super.updateUI();

		INACTIVE_BORDER_COLOR = UIManager.getColor("controlDkShadow");

		ACTIVE_BORDER_COLOR = UIManager.getColor("Menu.selectionBackground");

		ACTIVE_MID_COLOR = alpha(
				UIManager.getColor("Menu.selectionBackground"), 200);

		ACTIVE_FILL_COLOR = alpha(UIManager
				.getColor("Menu.selectionBackground"), 125);

		ACTIVE_START_COLOR = brighter(UIManager
				.getColor("Menu.selectionBackground"));

		INACTIVE_FILL_COLOR = brighter(UIManager.getColor("controlDkShadow"));

		INACTIVE_MID_COLOR = UIManager.getColor("controlDkShadow");

		INACTIVE_START_COLOR = UIManager.getColor("control");

		INACTIVE_LABEL_COLOR = UIManager.getColor("Menu.selectionForeground");

		ACTIVE_LABEL_COLOR = UIManager.getColor("Menu.selectionForeground");

	}

	public Color alpha(Color c, int alpha) {
		return new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);

	}

	private final static double FACTOR = 0.75;

	public Color darker(Color c) {
		return new Color(Math.max((int) (c.getRed() * FACTOR), 0), Math.max(
				(int) (c.getGreen() * FACTOR), 0), Math.max(
				(int) (c.getBlue() * FACTOR), 0));
	}

	private Color brighter(Color c) {
		int r = c.getRed();
		int g = c.getGreen();
		int b = c.getBlue();

		return new Color(Math.min((int) (r / FACTOR), 255), Math.min(
				(int) (g / FACTOR), 255), Math.min((int) (b / FACTOR), 255));
	}

}
