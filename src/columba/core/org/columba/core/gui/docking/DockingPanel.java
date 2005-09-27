package org.columba.core.gui.docking;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;

import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.DockingStub;

public class DockingPanel extends TitlePanel implements DockingStub {

	private Dockable dockable;

	private String dockingId;

	private JPopupMenu menu = null;

	public DockingPanel(String id, String title) {
		super(title);

		this.dockingId = id;

		DockingManager.registerDockable(this);
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
		return null;
	}

	protected TitleBar createTitlebar(String title) {
		return new GradientTitlebar(title);
	}

	public Component getDragSource() {
		return getTitlebar();
	}

	public Component getFrameDragSource() {
		return getTitlebar();
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

		titlebar.getButton().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JButton b = titlebar.getButton();
				
				// FIXME: should we align the menu to the left instead?
				//menu.show(b, b.getWidth() - menu.getWidth(), b.getHeight());
				menu.show(b, 0, b.getHeight());
			}
		});
	}
}
