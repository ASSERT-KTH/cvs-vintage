/******************************************************************************* 
 * Copyright (c) 2000, 2003 IBM Corporation and others. 
 * All rights reserved. This program and the accompanying materials! 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/cpl-v10.html 
 * 
 * Contributors: 
 *  IBM Corporation - initial API and implementation 
 * 	Cagatay Kavukcuoglu <cagatayk@acm.org> - Fix for bug 10025 - Resizing views 
 *    should not use height ratios		
 ************************************************************************/

package org.eclipse.ui.internal;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.presentations.SystemMenuPinEditor;
import org.eclipse.ui.internal.presentations.SystemMenuSize;
import org.eclipse.ui.internal.presentations.UpdatingActionContributionItem;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;

/**
 * Represents a tab folder of editors. This layout part container only accepts
 * EditorPane parts.
 * 
 * TODO: Make PartStack non-abstract and delete this class. The differences between
 * editors and views should be handled by the presentation or the editors/views themselves.
 */
public class EditorStack extends PartStack {
	
    private EditorSashContainer editorArea;

    private WorkbenchPage page;  
    
    private SystemMenuSize sizeItem = new SystemMenuSize(null);
    private SystemMenuPinEditor pinEditorItem = new SystemMenuPinEditor(null);
	 
    public EditorStack(EditorSashContainer editorArea, WorkbenchPage page) {
        super(PresentationFactoryUtil.ROLE_EDITOR); //$NON-NLS-1$
        this.editorArea = editorArea;
        setID(this.toString());
        // Each folder has a unique ID so relative positioning is unambiguous.
        // save off a ref to the page
        //@issue is it okay to do this??
        //I think so since a ViewStack is
        //not used on more than one page.
        this.page = page;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#getPage()
	 */
	protected WorkbenchPage getPage() {
		return page;
	}
	
	public void addSystemActions(IMenuManager menuManager) {
		pinEditorItem = new SystemMenuPinEditor((EditorPane)getVisiblePart());
		appendToGroupIfPossible(menuManager, "misc", new UpdatingActionContributionItem(pinEditorItem)); //$NON-NLS-1$
		sizeItem = new SystemMenuSize((PartPane)getVisiblePart());
		appendToGroupIfPossible(menuManager, "size", sizeItem); //$NON-NLS-1$
	}

    public boolean isMoveable(IPresentablePart part) {
    	return true;
    }

    public boolean isCloseable(IPresentablePart part) {
        return true;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.presentations.IStackPresentationSite#supportsState(int)
	 */
	public boolean supportsState(int state) {
		return state == IStackPresentationSite.STATE_MAXIMIZED || state == IStackPresentationSite.STATE_RESTORED;
	}

    /**
     * Factory method for editor workbooks.
     */
    public static EditorStack newEditorWorkbook(EditorSashContainer editorArea,
            WorkbenchPage page) {
        return new EditorStack(editorArea, page);
    }
    
    protected void add(LayoutPart newChild, IPresentablePart position) {
    	super.add(newChild, position);
        
        ((EditorPane) newChild).setWorkbook(this);    	
    }
    
    /**
     * See IVisualContainer#add
     */
    public void add(LayoutPart child) {
    	super.add(child);

    	if (child instanceof EditorPane) {
	        ((EditorPane) child).setWorkbook(this);
    	}
    }
    
    protected void updateActions() {
        EditorPane pane = (EditorPane)getVisiblePart();
        
        sizeItem.setPane(pane);
        pinEditorItem.setPane(pane);
    }
    
    public Control[] getTabList() {
    	return getTabList(getVisiblePart());
    }

    public void removeAll() {
    	LayoutPart[] children = getChildren();
    	
        for (int i = 0; i < children.length; i++)
            remove((EditorPane) children[i]);
    }

    public boolean isActiveWorkbook() {
        EditorSashContainer area = getEditorArea();

        if (area != null)
            return area.isActiveWorkbook(this);
        else
            return false;
    }

    public void becomeActiveWorkbook(boolean hasFocus) {
        EditorSashContainer area = getEditorArea();

        if (area != null) area.setActiveWorkbook(this, hasFocus);
    }
	
    public EditorPane[] getEditors() {
    	LayoutPart[] children = getChildren();
    	
    	EditorPane[] panes = new EditorPane[children.length];
    	for (int idx = 0; idx < children.length; idx++) {
    		panes[idx] = (EditorPane)children[idx];
    	}
    	
        return panes;
    }

    public EditorSashContainer getEditorArea() {
        return editorArea;
    }

    public EditorPane getVisibleEditor() {
        return (EditorPane) getVisiblePart();
    }

    public void setVisibleEditor(EditorPane editorPane) {
        setSelection(editorPane);
    }

    public void showVisibleEditor() {
    }

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#canMoveFolder()
	 */
	protected boolean canMoveFolder() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#derefPart(org.eclipse.ui.internal.LayoutPart)
	 */
	protected void derefPart(LayoutPart toDeref) {
		EditorAreaHelper.derefPart(toDeref);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#allowsDrop(org.eclipse.ui.internal.PartPane)
	 */
	protected boolean allowsDrop(PartPane part) {
		return part instanceof EditorPane;
	}
	
	public void setFocus() {
		super.setFocus();
		becomeActiveWorkbook(true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.PartStack#close(org.eclipse.ui.presentations.IPresentablePart[])
	 */
	protected void close(IPresentablePart[] parts) {

		if (parts.length == 1) {
			close(parts[0]);
			return;
		}
		
		IEditorReference[] toClose = new IEditorReference[parts.length];
		for (int idx = 0; idx < parts.length; idx++) {
			EditorPane part = (EditorPane)getPaneFor(parts[idx]);
			toClose[idx] = part.getEditorReference();
		}
		
		WorkbenchPage page = getPage();
		
		if (page != null) {
			page.closeEditors(toClose, true);
		}
	}
}