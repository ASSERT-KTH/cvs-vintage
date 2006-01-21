package org.columba.core.gui.docking;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.border.LineBorder;

import org.columba.core.resourceloader.ImageLoader;
import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.AbstractDockable;
import org.flexdock.util.DockingUtility;

public class DockableView extends AbstractDockable implements
		PropertyChangeListener {

	private DockingPanel panel;

	private JComponent dragInitiator;

	private DefaultTitleBar titleBar;

	public DockableView(String id, String title) {
		super(id);

		panel = new DockingPanel(id, title);

		titleBar = (DefaultTitleBar) panel.getTitleBar();

		JButton closeButton = new JButton();
		closeButton.setAction(new CloseAction(this.getPersistentId()));
		closeButton.setText("");
		closeButton.setIcon(ImageLoader.getImageIcon("close_default.png"));
		titleBar.addButton(closeButton);

		dragInitiator = panel.getTitleBar();
		setTabText(panel.getTitle());

		getDragSources().add(dragInitiator);
		getFrameDragSources().add(dragInitiator);

		DockingManager.registerDockable(this);
		DockableRegistry.getInstance().register(this);

		addPropertyChangeListener(this);
	}

	public boolean isActive() {
		return getDockingProperties().isActive().booleanValue();
	}

	public void setTitle(String title) {
		DefaultTitleBar titleBar = (DefaultTitleBar) panel.getTitleBar();
		titleBar.setTitle(title);
	}

	public String getTitle() {
		DefaultTitleBar titleBar = (DefaultTitleBar) panel.getTitleBar();
		return titleBar.getTitle();
	}

	public void setContentPane(JComponent c) {
		this.panel.setContentPane(c);

	}

	public Component getComponent() {
		return panel;
	}

	public void setPopupMenu(final JPopupMenu menu) {
		panel.setPopupMenu(menu);
	}

	class CloseAction extends AbstractAction {
		String id;

		CloseAction(String id) {
			this.id = id;

			putValue(AbstractAction.NAME, "x");
		}

		public void actionPerformed(ActionEvent e) {
			DockingManager.close(DockableView.this);
		}
	}

	class PinAction extends AbstractAction {
		String id;

		PinAction(String id) {
			this.id = id;

			putValue(AbstractAction.NAME, "P");

		}

		public void actionPerformed(ActionEvent e) {
			boolean minimize = DockingUtility.isMinimized(DockableView.this) ? false
					: true;
			DockingManager.setMinimized(DockableView.this, minimize);

		}
	}

	public void propertyChange(PropertyChangeEvent evt) {

		panel.setActive(isActive());

	}

}
