package org.columba.api.gui.frame;

import org.flexdock.docking.Dockable;

/**
 * 
 * @author Frederik
 */
public interface IDock {

	/**
	 * Dock component.
	 * 
	 * @param component
	 *            component
	 * @param str
	 *            <code>DockingConstants</code>
	 */
	public abstract void dock(Dockable component, String str);

	/**
	 * Set split proportion for component.
	 * 
	 * @param component
	 *            component
	 * @param propertion
	 *            percentage value
	 */
	public abstract void setSplitProportion(Dockable component,
			float propertion);
	
}
