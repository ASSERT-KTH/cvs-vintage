package org.columba.core.gui.docking;

import java.awt.Component;

import javax.swing.JComponent;

import org.flexdock.docking.DockingManager;
import org.flexdock.docking.defaults.AbstractDockable;

class DockableImpl extends AbstractDockable {
	private DockingPanel panel;
	private JComponent dragInitiator;
	
	public DockableImpl(DockingPanel dockable, JComponent dragInit, String id) {
		super(id);
		if(dockable==null)
			new IllegalArgumentException(
			"Cannot create DockableImpl with a null DockablePanel.");
		if(dragInit==null)
			new IllegalArgumentException(
			"Cannot create DockableImpl with a null drag initiator.");
			
		panel = dockable;
		dragInitiator = dragInit;
		setTabText(panel.getTitle());
		getDragSources().add(dragInit);
		getFrameDragSources().add(dockable.getTitlebar());
		DockingManager.registerDockable(this);
	}

	public Component getComponent() {
		return panel;
	}
}
