package org.eclipse.ui.part;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved. 
 */
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.*;
import org.eclipse.ui.internal.SubActionBars;
import org.eclipse.ui.internal.misc.Assert;
/**
 * This implemetation of <code>IPageSite</code> provides
 * a site for a page within a <code>PageBookView</code>. 
 * Most methods are forwarded to the view's site.
 */
public class PageSite implements IPageSite {
	/**
	 * The "parent" view site 
	 */
	private IViewSite parentSite;
	/**
	 * A selection provider set by the page.
	 * Value is <code>null</code> until set.
	 */
	private ISelectionProvider selectionProvider;
	/**
	 * The action bars for this site
	 */
	private SubActionBars subActionBars;
	/**
	 * Creates a new sub view site of the given parent 
	 * view site.
	 * 
	 * @param parentViewSite the parent view site
	 */
	public PageSite(IViewSite parentViewSite) {
		Assert.isNotNull(parentViewSite);
		parentSite = parentViewSite;
		subActionBars = new SubActionBars(parentViewSite.getActionBars());
	}
	/**
	 * The PageSite implementation of this <code>IPageSite</code>
	 * method returns the <code>SubActionBars</code> for this site.
	 * 
	 * @return the subactionbars for this site
	 */
	public IActionBars getActionBars() {
		return subActionBars;
	}
	/* (non-Javadoc)
	 * Method declared on IPageSite.
	 */
	public IWorkbenchPage getPage() {
		return parentSite.getPage();
	}
	/* (non-Javadoc)
	 * Method declared on IPageSite.
	 */
	public ISelectionProvider getSelectionProvider() {
		return selectionProvider;
	}
	/* (non-Javadoc)
	 * Method declared on IPageSite.
	 */
	public Shell getShell() {
		return parentSite.getShell();
	}
	/* (non-Javadoc)
	 * Method declared on IPageSite.
	 */
	public IWorkbenchWindow getWorkbenchWindow() {
		return parentSite.getWorkbenchWindow();
	}
	/* (non-Javadoc)
	 * Method declared on IPageSite.
	 */
	public void registerContextMenu(String menuID, MenuManager menuMgr, ISelectionProvider selProvider) {
		parentSite.registerContextMenu(menuID, menuMgr, selProvider);
	}
	/* (non-Javadoc)
	 * Method declared on IPageSite.
	 */
	public void setSelectionProvider(ISelectionProvider provider) {
		selectionProvider = provider;
	}
}

