package org.columba.core.gui.globalactions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.columba.api.gui.frame.IDock;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.gui.frame.event.FrameEvent;
import org.columba.api.gui.frame.event.IFrameMediatorListener;
import org.columba.core.gui.frame.DefaultFrameController;
import org.columba.core.gui.frame.FrameMediatorAdapter;
import org.columba.core.gui.menu.IMenu;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;

public class ShowHideViewSubmenu extends IMenu {

	public ShowHideViewSubmenu(IFrameMediator controller) {
		super(controller, "Show/Hide View",
				((DefaultFrameController) controller).getViewItem().get("id"));

		controller.addListener(new MyListener());
	}

	class DisplayAction extends AbstractAction {
		String id;

		DisplayAction(String id, String name) {
			this.id = id;

			putValue(AbstractAction.NAME, name);
		}

		public void actionPerformed(ActionEvent e) {
			Dockable dockable = DockingManager.getDockable(id);
			DockingManager.display(dockable);
		}
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent arg0) {

	}


	class MyListener extends FrameMediatorAdapter {

		MyListener() {

		}

		/**
		 * @see org.columba.core.gui.frame.FrameMediatorAdapter#switchedComponent(org.columba.api.gui.frame.event.FrameEvent)
		 */
		@Override
		public void switchedComponent(FrameEvent event) {
			IFrameMediator mediator = getFrameMediator();

			if (mediator instanceof IDock) {
				String[] ids = ((IDock) mediator).getDockableIds();

				removeAll();
				for (int i = 0; i < ids.length; i++) {
					// i18n
					add(new JMenuItem(new DisplayAction(ids[i], ids[i])));
				}

			}
		}
	}

}
