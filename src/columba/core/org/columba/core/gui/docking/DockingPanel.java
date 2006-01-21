package org.columba.core.gui.docking;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
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
			.getColor("controlText");

	public static Color ACTIVE_LABEL_COLOR = UIManager
			.getColor("Menu.selectionForeground");

	protected GradientTitleBar titlebar;

	private JPanel contentPanel;

	private Dockable dockable;

	private String dockingId;

	private JPopupMenu menu;

	protected JButton menuButton;

	private boolean active = false;

	public DockingPanel(String id, String title) {
		super();

		this.dockingId = id;

		this.titlebar = new GradientTitleBar(title, INACTIVE_MID_COLOR,
				INACTIVE_START_COLOR, INACTIVE_FILL_COLOR);

		ImageIcon icon = new AscendingIcon();
		menuButton = new JButton(icon);

		titlebar.addButton(menuButton);

		setLayout(new BorderLayout());

		add(this.titlebar, BorderLayout.NORTH);
		
		contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		Border border = BorderFactory.createCompoundBorder(new LineBorder(
				INACTIVE_BORDER_COLOR), BorderFactory.createEmptyBorder(1,
				1, 1, 1));

		contentPanel.setBorder(border);
		
		add(contentPanel, BorderLayout.CENTER);
		
		setContentPane(createContentPane());

	}

	public void setActive(boolean active) {
		this.active = active;

		if (active) {
			contentPanel.setBorder(new LineBorder(ACTIVE_BORDER_COLOR, 2));

			titlebar.setMidColor(ACTIVE_MID_COLOR);
			titlebar.setFillColor(ACTIVE_FILL_COLOR);
			titlebar.setStartColor(ACTIVE_START_COLOR);

			titlebar.setActiveTitleColor(ACTIVE_LABEL_COLOR);
		} else {
			Border border = BorderFactory.createCompoundBorder(new LineBorder(
					INACTIVE_BORDER_COLOR), BorderFactory.createEmptyBorder(1,
					1, 1, 1));

			contentPanel.setBorder(border);

			titlebar.setMidColor(INACTIVE_MID_COLOR);
			titlebar.setFillColor(INACTIVE_FILL_COLOR);
			titlebar.setStartColor(INACTIVE_START_COLOR);

			titlebar.setActiveTitleColor(INACTIVE_LABEL_COLOR);
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

	public DefaultTitleBar getTitleBar() {
		return titlebar;
	}

	public void setContentPane(JComponent comp) {
		contentPanel.removeAll();

		if (comp != null)
			contentPanel.add(comp, BorderLayout.CENTER);

		//this.contentPane = comp;

//		Color color = UIManager.getColor("controlDkShadow");
//
//		if (comp != null)
//			contentPane.setBorder(new LineBorder(color));
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

	public void setPopupMenu(final JPopupMenu menu) {
		if (menu == null)
			throw new IllegalArgumentException("menu == null");

		this.menu = menu;

		menuButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// FIXME: should we align the menu to the left instead?
				// menu.show(b, b.getWidth() - menu.getWidth(), b.getHeight());
				menu.show(menuButton, 0, menuButton.getHeight());
			}
		});
	}

	public void updateUI() {
		super.updateUI();

		INACTIVE_BORDER_COLOR = UIManager.getColor("controlDkShadow");

		ACTIVE_BORDER_COLOR = UIManager.getColor("Menu.selectionBackground");

		ACTIVE_MID_COLOR = UIManager.getColor("Menu.selectionBackground");

		ACTIVE_FILL_COLOR = UIManager.getColor("Menu.selectionBackground");

		ACTIVE_START_COLOR = UIManager.getColor("Menu.selectionBackground")
				.brighter();

		INACTIVE_FILL_COLOR = UIManager.getColor("control");

		INACTIVE_MID_COLOR = UIManager.getColor("control");

		INACTIVE_START_COLOR = UIManager.getColor("controlHighlight");

		INACTIVE_LABEL_COLOR = UIManager.getColor("controlText");

		ACTIVE_LABEL_COLOR = UIManager.getColor("Menu.selectionForeground");

	}

}
