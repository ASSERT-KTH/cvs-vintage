/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ui.internal.menus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.menus.IWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.WindowTrimProxy;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.layout.IWindowTrim;
import org.eclipse.ui.internal.layout.TrimLayout;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.menus.IWorkbenchWidget;

/**
 * <p>
 * An implementation that supports 'trim' elements defined in using the
 * <code>org.eclipse.ui.menus</code> extension point.
 * </p>
 * <p>
 * This class is not intended to be used outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public class TrimBarManager {

	/**
	 * The window on which this menu manager exists; never <code>null</code>.
	 */
	private STrimBuilder fTrimBuilder;

	private IMenuService fMenuService;

	private boolean fDirty;

	/**
	 * Constructs a new instance of <code>TrimBarManager</code>.
	 * 
	 * @param window
	 *            The window on which this menu manager exists; must not be
	 *            <code>null</code>.
	 */
	public TrimBarManager(final WorkbenchWindow window) {
		if (window == null) {
			throw new IllegalArgumentException("The window cannot be null"); //$NON-NLS-1$
		}

		// Remember the parameters
		fMenuService = (IMenuService) window.getService(IMenuService.class);
		fTrimBuilder = new STrimBuilder(window);

		// New layouts are always 'dirty'
		fDirty = true;
	}

	/**
	 * Hacked version of the update method that allows the hiding of any trim
	 * sited at SWT.TOP. This is because the Intro management wants there to be
	 * no trim at the top but can only currently indicate this by using the
	 * CoolBar's visibility...
	 * 
	 * @param force
	 * @param recursive
	 * @param hideTopTrim
	 */
	public void update(boolean force, boolean recursive, boolean hideTopTrim) {
		if (force || isDirty()) {
			// Re-render the trim based on the new layout
			SMenuLayout layout = fMenuService.getLayout();
			fTrimBuilder.build(layout, hideTopTrim);
			setDirty(false);
		}
	}

	/**
	 * Copied from the <code>MenuManager</code> method...
	 * 
	 * @param force
	 *            If true then do the update even if not 'dirty'
	 * @param recursive
	 *            Update recursively
	 * 
	 * @see org.eclipse.jface.action.MenuManager#update(boolean, boolean)
	 */
	public void update(boolean force, boolean recursive) {
		update(force, recursive, false);
	}

	/**
	 * Set the dirty state of the layout
	 * 
	 * @param isDirty
	 */
	private void setDirty(boolean isDirty) {
		fDirty = isDirty;
	}

	/**
	 * Returns the 'dirty' state of the layout
	 * 
	 * @return Always returns 'true' for now
	 */
	private boolean isDirty() {
		return fDirty;
	}

	/**
	 * This is a convenience class that maintains the list of the widgets in the
	 * group. This allows any position / orientation changes to the group to be
	 * passed on to all the widgets for that group.
	 * 
	 * @since 3.2
	 * 
	 */
	private class TrimWidgetProxy extends WindowTrimProxy {

		private List widgets;

		private int curSide;

		private Composite parent;

		private SGroup group;

		/**
		 * Constructor that takes in any information necessary to implement an
		 * IWindowTrim and also has enoughstate to manage a group with multiple
		 * IWidget contributions.
		 * 
		 * @param widgets
		 *            The list of IWidget instances representing the elements of
		 *            this group.
		 * @param side
		 *            The SWT side that the trim will initially be placed on
		 * @param parent
		 *            The group's composite (and the parent of its widgets)
		 * @param id
		 *            The id of the group
		 * @param displayName
		 *            The display name of the group
		 * @param validSides
		 *            Which SWT sides this trim can be moved to
		 * @param resizeable
		 *            <code>true</code> iff the group contains at least one
		 *            widget that can use extra space in the trim.
		 */
		public TrimWidgetProxy(List widgets, int side, Composite parent,
				SGroup group, int validSides, boolean resizeable) {
			super(parent, group.getId(), group.getId(), validSides, resizeable);

			// Remember our widget structure
			this.widgets = widgets;
			this.curSide = side;
			this.parent = parent;
			this.group = group;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ui.internal.WindowTrimProxy#dock(int)
		 */
		public void dock(int newSide) {
			// out with the old...
			for (Iterator iter = widgets.iterator(); iter.hasNext();) {
				IWidget widget = (IWidget) iter.next();
				widget.dispose();
			}

			// ...in with the new
			for (Iterator iter = widgets.iterator(); iter.hasNext();) {
				WidgetProxy widget = (WidgetProxy) iter.next();
				widget.fill(parent, curSide, newSide);
			}

			curSide = newSide;

			parent.layout();
		}

		public SGroup getGroup() {
			return group;
		}

		/**
		 * Disposes all the widgets contributed into this group and then
		 * disposes the group's 'proxy' control
		 */
		public void dispose() {
			for (Iterator iter = widgets.iterator(); iter.hasNext();) {
				IWidget widget = (IWidget) iter.next();
				widget.dispose();
			}

			getControl().dispose();
		}
	}

	/**
	 * A convenience class that implements the 'rendering' code necessary to
	 * turn the contributions to the 'trim' bar into actual SWT controls.
	 * 
	 * @since 3.2
	 * 
	 */
	private class STrimBuilder {
		/**
		 * The WorkbenchWindow that this builder is for
		 */
		private WorkbenchWindow fWindow;
		
		/**
		 * Map of trim that fails during the trim 'fill' handling.
		 * This is used to prevent multiple error dialogs from appearing
		 * during a single session. 
		 */
		private Set bogusTrim = new HashSet();

		/**
		 * The list of <code>WindowTrimProxy</code> elements currently
		 * rendered in the WorkbenchWindow. Used to support the update mechanism
		 * (specifically, it's needed to implement the <code>tearDown</code>
		 * method).
		 */
		private List curGroups = new ArrayList();

		/**
		 * Map to cache which trim has already been initialized
		 */
		private Map initializedTrim = new HashMap();
		
		/**
		 * Construct a trim builder for the given WorkbenchWindow
		 * 
		 * @param window
		 *            The WorkbenchWindow to render the trim on
		 */
		public STrimBuilder(WorkbenchWindow window) {
			fWindow = window;
		}

		/**
		 * Remove any rendered trim. This method will always be directly
		 * followed by a call to the 'build' method to update the contents.
		 */
		public void tearDown() {
			// First, remove all trim
			for (Iterator iter = curGroups.iterator(); iter.hasNext();) {
				TrimWidgetProxy proxy = (TrimWidgetProxy) iter.next();
				fWindow.getTrimManager().removeTrim(proxy);

				proxy.dispose();
			}

			// Clear out the old list
			curGroups.clear();
		}

		/**
		 * Construct the trim based on the contributions.
		 * 
		 * @param layout
		 *            The new layout information
		 * @param hideTopTrim
		 *            <code>true</code> iff we don't want to display trim
		 *            contributed into the SWT.TOP area. This is because the
		 *            'Intro' View hides the CBanner (and so, presumably, also
		 *            wants to not show any other trim at the top.
		 * 
		 * @param window
		 *            The widnow to 'render' the trim into
		 * 
		 */
		public void build(SMenuLayout layout, boolean hideTopTrim) {
			tearDown();

			// Get all 'trim' related info
			ILayoutNode trimInfo = layout.getBar(SBar.TYPE_TRIM);

			// Walk the layout tree 'rendering' its elements
			List kids = trimInfo.getChildrenSorted();
			for (Iterator iter = kids.iterator(); iter.hasNext();) {
				ILayoutNode node = (ILayoutNode) iter.next();
				MenuElement element = node.getMenuElement();
				if (element instanceof SWidget) {
					// SWidget widget = (SWidget) element;
					// renderTrim(null, widget, SWT.BOTTOM);
				} else if (element instanceof SGroup) {
					// Only render the top trim if
					renderGroup(node, hideTopTrim);
				}
			}
		}

		/**
		 * Create a composite to contain the group and then call the 'fill' for
		 * any 'trim' widgets located in that group.
		 * 
		 * @param groupNode
		 *            The layout node representing the group
		 */
		private void renderGroup(ILayoutNode groupNode, boolean hideTopTrim) {
			SGroup group = (SGroup) groupNode.getMenuElement();
			if ("testGroup".equals(group.getId())) { //$NON-NLS-1$
				int i = 0;
				i++;
			}
			List kids = groupNode.getChildrenSorted();

			// Don't show empty groups
			if (kids.size() == 0)
				return;

			int side = getSide(group);

			if (hideTopTrim && side == SWT.TOP)
				return;

			// Create a 'container' composite for the group
			Composite grpComposite = new Composite(fWindow.getShell(), SWT.NONE);
			grpComposite.setToolTipText(group.getId());
			
			// Create the layout for the 'group' container...-no- border margins
			RowLayout rl = new RowLayout();
	        rl.marginBottom = rl.marginHeight = rl.marginLeft = rl.marginRight = rl.marginTop = rl.marginWidth = 0;
			grpComposite.setLayout(rl);

			// are -any- of the widgets resizeable?
			boolean resizeable = false;

			// Walk the layout tree 'rendering' its trim elements
			List groupWidgets = new ArrayList();
			for (Iterator iter = kids.iterator(); iter.hasNext();) {
				ILayoutNode node = (ILayoutNode) iter.next();
				MenuElement element = node.getMenuElement();
				if (element instanceof SWidget) {
					SWidget sWidget = (SWidget) element;

					// update the resizeable state for each widget
					resizeable = resizeable || isResizeable(sWidget);

					// Add any successful defs into the group's list
					IWidget iw = renderTrim(grpComposite, sWidget, side);
					if (iw != null)
						groupWidgets.add(iw);
				} else if (element instanceof SGroup) {
					// No sub-group support
				}
			}

			// Create the trim proxy for this group
			TrimWidgetProxy groupTrimProxy = new TrimWidgetProxy(groupWidgets,
					side, grpComposite, group, SWT.TOP | SWT.BOTTOM | SWT.LEFT
							| SWT.RIGHT, resizeable);
			curGroups.add(groupTrimProxy);

			// 'Site' the group in its default location
			placeGroup(groupTrimProxy);
		}

		private void placeGroup(final TrimWidgetProxy proxy) {
			// Get the placement parameters
			final int side = getSide(proxy.getGroup());
			boolean atStart = isAtStart(proxy.getGroup());

			// Place the trim before any other trim if it's
			// at the 'start'; otherwise place it at the end
			IWindowTrim beforeMe = null;
			if (atStart) {
				List trim = fWindow.getTrimManager().getAreaTrim(side);
				if (trim.size() > 0)
					beforeMe = (IWindowTrim) trim.get(0);
			}

			// Add the group into trim...safely
			try {
    			proxy.dock(side); // ensure that the widgets are properly oriented
    			TrimLayout tl = (TrimLayout) fWindow.getShell().getLayout();
    			tl.addTrim(side, proxy, beforeMe);
	        } catch (Throwable e) {
	            IStatus status = null;
	            if (e instanceof CoreException) {
	                status = ((CoreException) e).getStatus();
	            } else {
	                status = StatusUtil
	                        .newStatus(
	                                IStatus.ERROR,
	                                "Internal plug-in widget delegate error on dock.", e); //$NON-NLS-1$
	            }
	            WorkbenchPlugin
	                    .log(
	                            "widget delegate failed on dock: id = " + proxy.getId(), status); //$NON-NLS-1$
	        }
		}

		/**
		 * Render a particular SWidget into a given group
		 * 
		 * @param groupComposite
		 *            The parent to create the widgets under
		 * @param widget
		 *            The SWidget to render
		 * @param side
		 */
		private IWidget renderTrim(final Composite groupComposite, final SWidget widget,
				final int side) {
			// Have we already tried (and failed) to load this??
			if (bogusTrim.contains(widget))
				return null;
			
			// OK, fill the widget
			IWidget iw = null;
            try {
            	iw = widget.getWidget();
				if (iw != null) {
	            	// The -first- time trim is displayed we'll initialize it
	            	if (iw instanceof IWorkbenchWidget && initializedTrim.get(iw) == null) {
	            		IWorkbenchWidget iww = (IWorkbenchWidget) iw;
	            		iww.init(fWindow);
	            		initializedTrim.put(iw, iw);
	            	}
	            	
					if (iw instanceof WidgetProxy)
						((WidgetProxy) iw).fill(groupComposite, SWT.DEFAULT, side);
					else
						iw.fill(groupComposite);
				}
            } catch (Throwable e) {
            	// Remember that this is a 'failed' widget
            	bogusTrim.add(widget);
            	
                IStatus status = null;
                if (e instanceof CoreException) {
                    status = ((CoreException) e).getStatus();
                } else {
                    status = StatusUtil
                            .newStatus(
                                    IStatus.ERROR,
                                    "Internal plug-in widget delegate error on creation.", e); //$NON-NLS-1$
                }
                WorkbenchPlugin
                        .log(
                                "Could not create widget delegate for id: " + widget.getId(), status); //$NON-NLS-1$
                
                return null;
            }

			return iw;
		}
		
		/**
		 * Returns <code>true</code> if the trim want extra space if possible.
		 * 
		 * @param widget
		 *            The SWidget to check
		 * @return <code>true</code> iff the widget wants extra space
		 */
		private boolean isResizeable(SWidget widget) {
			try {
				SLayout layout = widget.getLayout();
				return layout.fillMajor();
			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return false;
		}

		/**
		 * Determine the SWT curSide that a particular group should be placed on
		 * by default. This is usually accomplished by the contributor making
		 * their own group 'relativeTo' one of the workbench defined groups
		 * (which equate to the available set of trim 'areas'.
		 * 
		 * @param group
		 *            The group to determine the curSide for
		 * @return The 'curSide' to place the group on. <code>SWT.BOTTOM</code>
		 *         by default
		 */
		private int getSide(SGroup group) {
			int side = SWT.BOTTOM; // Default
			try {
				SLocation[] locs = group.getLocations();
				if (locs.length == 0)
					return side;

				// We don't support multiple locations yet so use the first one
				SOrder order = locs[0].getOrdering();
				String relTo = order.getRelativeTo();
				if ("command1".equals(relTo)) //$NON-NLS-1$
					side = SWT.TOP;
				else if ("command2".equals(relTo)) //$NON-NLS-1$
					side = SWT.TOP;
				else if ("vertical1".equals(relTo)) //$NON-NLS-1$
					side = SWT.LEFT;
				else if ("vertical2".equals(relTo)) //$NON-NLS-1$
					side = SWT.RIGHT;
				else if ("status".equals(relTo)) //$NON-NLS-1$
					side = SWT.BOTTOM;
				else {
					// TODO: reursively walk the 'relTo' chain until we find
					// one of the 'magic' groups or find one with no relTo
				}
			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return side;
		}

		/**
		 * Return the trim element that this trim should be placed before in the
		 * order
		 * 
		 * @param group
		 * @return
		 */
		private boolean isAtStart(SGroup group) {
			boolean atStart = false;
			try {
				SLocation[] locs = group.getLocations();
				if (locs.length == 0)
					return atStart;

				// We don't support multiple locations yet so use the first one
				SOrder order = locs[0].getOrdering();
				atStart = SOrder.POSITION_BEFORE == order.getPosition();
			} catch (NotDefinedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return atStart;
		}

		/**
		 * Reposition any contributed trim whose id is -not- a 'knownId'. If the
		 * id is known then the trim has already been positioned from the stored
		 * workbench state. If it isn't then it's a new contribution whose
		 * default position may have been trashed by the WorkbenchWindow's
		 * 'restoreState' handling.
		 * 
		 * @param knownIds
		 *            A List of strings containing the ids of any trim that was
		 *            explicitly positioned during the restore state.
		 */
		public void updateLocations(List knownIds) {
			for (Iterator iter = curGroups.iterator(); iter.hasNext();) {
				TrimWidgetProxy proxy = (TrimWidgetProxy) iter.next();
				if (!knownIds.contains(proxy.getGroup().getId())) {
					placeGroup(proxy);
				}
			}
		}
	}

	/**
	 * Updates the placement of any contributed trim that is -not- in the
	 * 'knownIds' list (which indicates that it has already been placed using
	 * cached workspace data.
	 * 
	 * Forward on to the bulder for implementation
	 */
	public void updateLocations(List knownIds) {
		fTrimBuilder.updateLocations(knownIds);
	}
	
	/**
	 * unhook the menu service.
	 */
	public void dispose() {
		fMenuService = null;
		fTrimBuilder = null;
	}
}

