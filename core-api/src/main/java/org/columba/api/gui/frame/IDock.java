package org.columba.api.gui.frame;

import java.util.Iterator;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;


/**
 * A <code>IFrameMediator</code> supporting docking should also implement this
 * interface, describing the dockable views residing in this frame mediator
 * workspace.
 * 
 * @author fdietz
 */
public interface IDock {

	enum REGION {CENTER, NORTH, SOUTH, EAST, WEST}

	public static final String DOCKING_VIEW_SEARCH = "search_panel";
	public static final String DOCKING_VIEW_CONTEXTUAL_PANEL = "contextual_panel";

	/**
	 * Return iterator of dockables.
	 * 
	 * @return dockable iterator of <code>IDockable</code>
	 */
	public Iterator<IDockable> getDockableIterator();
	
	/**
	 * Register new dockable at this docking container.
	 * 
	 * @param dockable
	 */
	public void registerDockable(IDockable dockable);
	
	/**
	 * Register new dockable at this docking container.
	 * 
	 * @param id
	 *            dockable id
	 * @param name
	 *            dockable human-readable name (used in menu item)
	 * @param comp
	 *            dockable view
	 * @param popup
	 *            popup menu, can be <code>null</code>
	 */
	public IDockable registerDockable(String id, String name, JComponent comp, JPopupMenu popup);
	

	public void dock(IDockable dockable, REGION region);
	
	public void dock(IDockable dockable, REGION region, float percentage);
	
	public void dock(IDockable dockable, IDockable parentDockable, REGION region, float percentage);
	
	public void setSplitProportion(IDockable dockable, float percentage);
	
	public void showDockable(String id);
}
