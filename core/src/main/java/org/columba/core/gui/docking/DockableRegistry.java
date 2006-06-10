package org.columba.core.gui.docking;

import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.event.EventListenerList;

import org.columba.core.gui.docking.event.DockableEvent;
import org.columba.core.gui.docking.event.IDockableListener;
import org.flexdock.docking.Dockable;

public class DockableRegistry {

	private Hashtable<String,Dockable> hashtable = new Hashtable<String,Dockable>();

	private static DockableRegistry instance = new DockableRegistry();

	protected EventListenerList listenerList = new EventListenerList();

	private DockableRegistry() {
		super();
	}

	public static DockableRegistry getInstance() {
		return instance;
	}

	public void register(Dockable dockable) {
		hashtable.put(dockable.getPersistentId(), dockable);
		
		fireDockableAdded(dockable);
	}

	public Enumeration getDockableEnumeration() {
		return hashtable.elements();
	}

	public void addListener(IDockableListener l) {
		listenerList.add(IDockableListener.class, l);
	}

	public void removeListener(IDockableListener l) {
		listenerList.remove(IDockableListener.class, l);
	}
	
	public void fireDockableAdded(Dockable dockable) {
		DockableEvent e = new DockableEvent(this, dockable);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IDockableListener.class) {
				((IDockableListener) listeners[i + 1]).dockableAdded(e);
			}
		}
	}

}
