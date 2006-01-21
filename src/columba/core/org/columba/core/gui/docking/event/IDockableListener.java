package org.columba.core.gui.docking.event;

import java.util.EventListener;

public interface IDockableListener extends EventListener{

	public abstract void dockableAdded(DockableEvent event);
}
