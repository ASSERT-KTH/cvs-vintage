package org.columba.core.gui.globalactions;

import java.awt.event.ActionEvent;
import java.util.Iterator;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;

import org.columba.api.gui.frame.IDock;
import org.columba.api.gui.frame.IDockable;
import org.columba.api.gui.frame.IFrameMediator;
import org.columba.api.gui.frame.event.FrameEvent;
import org.columba.core.gui.frame.DefaultFrameController;
import org.columba.core.gui.frame.FrameMediatorAdapter;
import org.columba.core.gui.menu.IMenu;
import org.flexdock.docking.Dockable;
import org.flexdock.docking.DockingManager;

/**
 * Menu in "View->Hide/Show" shows all dockables for the current frame mediator.
 * A listener is used to update the submenu in case the frame mediator is
 * changed.
 * 
 * @author fdietz
 */
public class ShowHideViewSubmenu extends IMenu {

	public ShowHideViewSubmenu(IFrameMediator controller) {
		super(controller, "Show/Hide View",
				((DefaultFrameController) controller).getViewItem().get("id"));

		// register for change of the frame mediator
		controller.addListener(new MyListener());
		
		if ( getMenuComponentCount() == 0) setEnabled(false);
		else setEnabled(true);
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

	/**
	 * Listener checks if a workspace switch happened and replaces all the
	 * Hide/Show menuentries.
	 * 
	 * @author fdietz
	 */
	class MyListener extends FrameMediatorAdapter {

		MyListener() {

		}

		/**
		 * @see org.columba.core.gui.frame.FrameMediatorAdapter#switchedComponent(org.columba.api.gui.frame.event.FrameEvent)
		 */
		@Override
		public void switchedComponent(FrameEvent event) {
			IFrameMediator mediator = getFrameMediator();

			// check if mediator supports docking
			if (mediator instanceof IDock) {
				Iterator<IDockable> it = ((IDock) mediator)
						.getDockableIterator();
				while (it.hasNext()) {
					IDockable dockable = it.next();

					DisplayAction action = new DisplayAction(dockable.getId(),
							dockable.resolveName());
					add(new JMenuItem(action));
				}
			}
			
			if ( getMenuComponentCount() == 0) setEnabled(false);
			else setEnabled(true);
		}
	}

}
