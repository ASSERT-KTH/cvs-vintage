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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IContributionManager;
import org.eclipse.jface.action.LegacyActionTools;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.SubMenuManager;
import org.eclipse.jface.menus.IWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.internal.ActionSetContributionItem;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.internal.misc.Policy;

/**
 * <p>
 * A wrapper around the new command-based menu services that speaks in terms of
 * the old menu manager class.
 * </p>
 * <p>
 * This class is not intended to be used outside of the
 * <code>org.eclipse.ui.workbench</code> plug-in.
 * </p>
 * 
 * @since 3.2
 */
public class LegacyMenuManager extends MenuManager {

	/**
	 * The window on which this menu manager exists; never <code>null</code>.
	 */
	private final WorkbenchWindow fWindow;

	private IMenuService fMenuService;

	private SMenuLayout fLayout;

	/**
	 * Constructs a new instance of <code>LegacyMenuManager</code>.
	 * 
	 * @param window
	 *            The window on which this menu manager exists; must not be
	 *            <code>null</code>.
	 */
	public LegacyMenuManager(final WorkbenchWindow window) {
		if (window == null) {
			throw new NullPointerException("The window cannot be null"); //$NON-NLS-1$
		}
		this.fWindow = window;
		fMenuService = (IMenuService) fWindow.getService(IMenuService.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.action.MenuManager#update(boolean, boolean)
	 */
	protected void update(boolean force, boolean recursive) {
		if (isDirty()) {
			generateMenus();
			fLayout = fMenuService.getLayout();
			Menu menu = getMenu();
			if (menu.getItemCount() > 0) {
				MenuItem[] items = menu.getItems();
				for (int i = 0; i < items.length; i++) {
					items[i].dispose();
				}
			}
			SMenuBuilder builder = new SMenuBuilder(fLayout, menu, fWindow);
			builder.build();
			setDirty(false);
		}
	}

	/**
	 * 
	 */
	private void generateMenus() {
		IContributionItem[] items = getItems();
		SLocation location = new SLocation(new SBar());
		processChildren(items, location);
	}

	/**
	 * @param item
	 * @param location
	 */
	private void createMenu(IContributionItem item, SLocation location) {
		if (item == null) {
			return;
		}
		if (item instanceof ActionContributionItem) {
			addActionContribution((ActionContributionItem) item, location);
		} else if (item instanceof ActionSetContributionItem) {
			addActionSetContribution((ActionSetContributionItem) item, location);
		} else if (item instanceof MenuManager) {
			addMenu((MenuManager) item, location);
		} else if (item instanceof SubMenuManager) {
			addSubMenuManager((SubMenuManager) item, location);
		} else if (item instanceof IContributionManager) {
			addContributionManager((IContributionManager) item, location);
		} else {
			if (Policy.EXPERIMENTAL_MENU) {
				System.err.println("createMenu: unknown: " //$NON-NLS-1$
						+ item.getClass().getName());
			}
			addWidget(item, location);
		}
	}

	/**
	 * @param manager
	 * @param location
	 */
	private void addSubMenuManager(SubMenuManager manager,
			SLocation parentLocation) {
		processChildren(manager.getItems(), parentLocation);
	}

	/**
	 * @param manager
	 * @param parentLocation
	 */
	private void addContributionManager(IContributionManager manager,
			SLocation parentLocation) {
		if (Policy.EXPERIMENTAL_MENU) {
			System.err.println("addContributionManager: unknown: " //$NON-NLS-1$
					+ manager.getClass().getName());
		}

		processChildren(manager.getItems(), parentLocation);
	}

	/**
	 * @param manager
	 * @param location
	 */
	private void addMenu(MenuManager menu, SLocation parentLocation) {
		String id = normalizeId(menu);
		SMenu smenu = fMenuService.getMenu(id);
		if (smenu.isDefined()) {
			smenu.addLocation(parentLocation);
		} else {
			smenu.define(LegacyActionTools.removeMnemonics(menu.getMenuText()),
					parentLocation);
		}
		final char mnemonic = LegacyActionTools.extractMnemonic(menu
				.getMenuText());
		SLocation location = new SLocation(parentLocation, id, mnemonic);

		processChildren(menu.getItems(), location);
	}

	/**
	 * @param items
	 * @param location
	 */
	private void processChildren(IContributionItem[] items, SLocation location) {
		for (int i = 0; i < items.length; i++) {
			if (items[i].isGroupMarker()) {
				addGroup(items[i], location);
			} else if (items[i].isSeparator()) {
				addWidget(items[i], location);
			} else {
				createMenu(items[i], location);
			}
		}
	}

	/**
	 * @param item
	 * @param location
	 */
	private void addWidget(final IContributionItem item, SLocation location) {
		String id = normalizeId(item);

		SWidget swidget = fMenuService.getWidget(id);
		if (swidget.isDefined()) {
			swidget.addLocation(location);
		} else {
			swidget.define(new IWidget() {

				public void dispose() {
					item.dispose();
				}

				public void fill(Composite parent) {
					item.fill(parent);
				}

				public void fill(Menu parent, int index) {
					item.fill(parent, index);
				}

				public void fill(ToolBar parent, int index) {
					item.fill(parent, index);
				}

				public void fill(CoolBar parent, int index) {
					item.fill(parent, index);
				}

			}, location);
		}
	}

	/**
	 * @param item
	 * @param id
	 * @return the ID, or a made up one.
	 */
	private String normalizeId(final IContributionItem item) {
		String id = item.getId();
		if (id == null || id.length() < 1) {
			id = item.getClass().getName() + "@" + item.hashCode(); //$NON-NLS-1$
		}
		return id;
	}

	/**
	 * @param marker
	 * @param location
	 * @return the group location for use as a parent location
	 */
	private SLocation addGroup(IContributionItem marker, SLocation location) {
		String id = normalizeId(marker);
		return addGroup(id, marker.isSeparator(), location);
	}

	/**
	 * @param manager
	 * @param location
	 * @return the group location for use as a parent location
	 */
	protected SLocation addGroup(IContributionManager manager,
			SLocation location) {
		String id = manager.getClass().getName() + "@" + manager.hashCode(); //$NON-NLS-1$
		return addGroup(id, false, location);
	}

	/**
	 * @param id
	 * @param isSeparator
	 * @param location
	 * @return the group location for use as a parent location
	 */
	private SLocation addGroup(String id, boolean isSeparator,
			SLocation location) {
		SGroup group = fMenuService.getGroup(id);
		if (group.isDefined()) {
			group.addLocation(location);
		} else {
			group.define(isSeparator, location);
		}
		return new SLocation(location, group.getId());
	}

	/**
	 * @param actionSetContribution
	 * @param parentLocation
	 */
	private void addActionSetContribution(
			ActionSetContributionItem actionSetContribution,
			SLocation parentLocation) {
		createMenu(actionSetContribution.getInnerItem(), parentLocation);
	}

	/**
	 * @param item
	 * @param location
	 */
	private void addActionContribution(
			ActionContributionItem actionContribution, SLocation parentLocation) {
		String id = normalizeId(actionContribution);
		String commandId = actionContribution.getAction()
				.getActionDefinitionId();
		if (commandId == null && Policy.EXPERIMENTAL_MENU) {
			System.err
					.println("addActionContribution: When is a command not a command! " //$NON-NLS-1$
							+ actionContribution.getId());
			return;
		}
		SItem sitem = fMenuService.getItem(id);
		if (sitem.isDefined()) {
			sitem.addLocation(parentLocation);
		} else {
			ICommandService commandService = (ICommandService) fWindow
					.getService(ICommandService.class);
			Command c = commandService.getCommand(commandId);
			if (c.isDefined()) {
				ParameterizedCommand pc = new ParameterizedCommand(c, null);
				sitem.define(pc, actionContribution.getAction().getText(),
						parentLocation);
			} else if (Policy.EXPERIMENTAL_MENU) {
				System.err
						.println("addActionContribution: undefined command " + commandId); //$NON-NLS-1$
			}
		}
	}

	private static class SMenuBuilder {

		private static class IndexManager {
			/**
			 * 
			 */
			public int index = 0;
		}

		private SMenuLayout fLayout;

		private Menu fRootMenu;

		private WorkbenchWindow fWindow;

		/**
		 * @param rootNode
		 * @param menu
		 * @param window
		 */
		public SMenuBuilder(SMenuLayout rootNode, Menu menu,
				WorkbenchWindow window) {
			fLayout = rootNode;
			fRootMenu = menu;
			fWindow = window;
		}

		/**
		 * 
		 */
		public void build() {
			ILayoutNode root = fLayout.getMenuBar();

			List children = root.getChildrenSorted();
			IndexManager index = new IndexManager();
			for (Iterator i = children.iterator(); i.hasNext();) {
				ILayoutNode child = (ILayoutNode) i.next();
				addNode(fRootMenu, child, index);
			}
		}

		/**
		 * @param menu
		 * @param node
		 * @param index
		 */
		public void addNode(Menu menu, ILayoutNode node, IndexManager index) {
			MenuElement element = node.getMenuElement();
			if (Policy.EXPERIMENTAL_MENU
					&& node.getLocation().getPath().toString().indexOf(
							LeafLocationElement.BREAKPOINT_PATH) > -1) {
				System.err
						.println("addNode: tree: " + node.getLocation() + "\n\t" //$NON-NLS-1$ //$NON-NLS-2$
								+ element);
			}
			// System.err.println("addNode: " + element); //$NON-NLS-1$
			if (element instanceof SMenu) {
				SMenu smenu = (SMenu) element;
				if (!smenu.isVisible(fWindow)) {
					return;
				}
				MenuItem item = new MenuItem(menu, SWT.CASCADE, index.index++);
				item.setData(smenu);

				try {
					item.setText(smenu.getLabel());
				} catch (NotDefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Menu itemMenu = new Menu(menu);
				item.setMenu(itemMenu);

				List children = node.getChildrenSorted();
				IndexManager childIndex = new IndexManager();
				for (Iterator i = children.iterator(); i.hasNext();) {
					ILayoutNode child = (ILayoutNode) i.next();
					addNode(itemMenu, child, childIndex);
				}

			} else if (element instanceof SItem) {
				final SItem sitem = (SItem) element;
				if (!sitem.isVisible(fWindow)) {
					return;
				}
				final MenuItem item = new MenuItem(menu, SWT.PUSH,
						index.index++);
				item.setData(element);

				try {
					item.setText(sitem.getCommand().getName());
					item.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent e) {
							try {
								sitem.getCommand()
										.executeWithChecks(null, null);
							} catch (ExecutionException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (NotDefinedException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (NotEnabledException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							} catch (NotHandledException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
						}

						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
				} catch (NotDefinedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else if (element instanceof SWidget) {
				SWidget swidget = (SWidget) element;
				if (!swidget.isVisible(fWindow)) {
					return;
				}
				try {
					swidget.getWidget().fill(menu, index.index);
				} catch (NotDefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else if (element instanceof SGroup) {
				SGroup sgroup = (SGroup) element;
				if (!sgroup.isVisible(fWindow)) {
					return;
				}
				try {
					if (sgroup.isSeparatorsVisible()) {
						MenuItem item = new MenuItem(menu, SWT.SEPARATOR,
								index.index++);
						item.setData(sgroup);
					}

					List children = node.getChildrenSorted();
					for (Iterator i = children.iterator(); i.hasNext();) {
						ILayoutNode child = (ILayoutNode) i.next();
						addNode(menu, child, index);
					}
				} catch (NotDefinedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				if (Policy.EXPERIMENTAL_MENU) {
					System.err.println("addNode: no element for " //$NON-NLS-1$
							+ node.getLocation());
				}
				if (node.isEmpty()) {
					return;
				}

				List children = node.getChildrenSorted();

				if (!children.isEmpty()) {

					MenuItem item = new MenuItem(menu, SWT.CASCADE,
							index.index++);
					item.setData(node);

					item
							.setText(node.getId() == null ? "node" + (index.index - 1) //$NON-NLS-1$
									: node.getId());

					Menu itemMenu = new Menu(menu);
					item.setMenu(itemMenu);

					IndexManager childIndex = new IndexManager();
					for (Iterator i = children.iterator(); i.hasNext();) {
						ILayoutNode child = (ILayoutNode) i.next();
						addNode(itemMenu, child, childIndex);
					}
				}
			}
		}
	}

}

