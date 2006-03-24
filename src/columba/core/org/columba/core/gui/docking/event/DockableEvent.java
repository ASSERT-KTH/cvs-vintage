package org.columba.core.gui.docking.event;

import java.util.EventObject;

import org.flexdock.docking.Dockable;


public class DockableEvent extends EventObject {

	private Dockable dockable;

	public DockableEvent(Object source, Dockable dockable) {
		super(source);
		
		this.dockable = dockable;
	}
	
	public Dockable getDockable() {
		return dockable;
	}

}
