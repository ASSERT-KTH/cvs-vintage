package org.columba.core.gui.globalactions;

import java.awt.event.ActionEvent;
import java.util.Enumeration;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.docking.DockableView;
import org.columba.core.gui.docking.DockableRegistry;
import org.columba.core.gui.docking.event.DockableEvent;
import org.columba.core.gui.docking.event.IDockableListener;
import org.columba.core.gui.frame.DefaultFrameController;
import org.columba.core.gui.menu.IMenu;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;
import org.flexdock.perspective.PerspectiveManager;

public class ShowHideViewSubmenu extends IMenu implements IDockableListener {

	public ShowHideViewSubmenu(IFrameMediator controller) {
		super(controller, "Show/Hide View",
				((DefaultFrameController) controller).getViewItem().get("id"));

		Enumeration e = DockableRegistry.getInstance().getDockableEnumeration();

		while (e.hasMoreElements()) {
			Dockable dockable = (Dockable) e.nextElement();
			DockableView impl = (DockableView) dockable;

			add(new JMenuItem(new DisplayAction(dockable.getPersistentId(),
					impl.getTitle())));
		}

		DockableRegistry.getInstance().addListener(this);
	}

	class DisplayAction extends AbstractAction {
		String id;

		DisplayAction(String id, String name) {
			this.id = id;

			putValue(AbstractAction.NAME, name);
		}

		public void actionPerformed(ActionEvent e) {
			Dockable dockable = DockingManager.getDockable(id);
			PerspectiveManager.getInstance().display(dockable);
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {

	}

	public void dockableAdded(DockableEvent event) {
		Dockable dockable = event.getDockable();
		DockableView impl = (DockableView) dockable;

		add(new JMenuItem(new DisplayAction(dockable.getPersistentId(), impl
				.getTitle())));
	}

}
