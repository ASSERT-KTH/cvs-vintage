/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ui.internal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.Geometry;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPersistable;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.dnd.AbstractDropTarget;
import org.eclipse.ui.internal.dnd.DragUtil;
import org.eclipse.ui.internal.dnd.IDropTarget;
import org.eclipse.ui.internal.dnd.SwtUtil;
import org.eclipse.ui.internal.presentations.PresentationFactoryUtil;
import org.eclipse.ui.internal.presentations.PresentationSerializer;
import org.eclipse.ui.internal.util.Util;
import org.eclipse.ui.presentations.AbstractPresentationFactory;
import org.eclipse.ui.presentations.IPresentablePart;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackDropResult;
import org.eclipse.ui.presentations.StackPresentation;

/**
 * Implements the common behavior for stacks of Panes (ie: EditorStack and ViewStack)
 * This layout container has PartPanes as children and belongs to a PartSashContainer.
 * 
 * @since 3.0
 */
public abstract class PartStack extends LayoutPart implements ILayoutContainer {

    public static final int PROP_SELECTION = 0x42;
    
    private List children = new ArrayList(3);

    protected int appearance = PresentationFactoryUtil.ROLE_VIEW;
    
    /**
     * Stores the last value passed to setSelection. If UI updates are being deferred,
     * this may be significantly different from the other current pointers. Once UI updates
     * are re-enabled, the stack will update the presentation selection to match the requested
     * current pointer.
     */ 
    private LayoutPart requestedCurrent;
    
    /**
     * Stores the current part for the stack. Whenever the outside world asks a PartStack
     * for the current part, this is what gets returned. This pointer is only updated after
     * the presentation selection has been restored and the stack has finished updating its
     * internal state. If the stack is still in the process of updating the presentation,
     * it will still point to the previous part until the presentation is up-to-date.
     */
    private LayoutPart current;
    
    /**
     * Stores the presentable part sent to the presentation. Whenever the presentation
     * asks for the current part, this is what gets returned. This is updated before sending
     * the part to the presentation, and it is not updated while UI updates are disabled.
     * When UI updates are enabled, the stack first makes presentationCurrent match 
     * requestedCurrent. Once the presentation is displaying the correct part, the "current"
     * pointer on PartStack is updated.
     */
    private LayoutPart presentationCurrent;

    private boolean ignoreSelectionChanges = false;

    protected IMemento savedPresentationState = null;

    private DefaultStackPresentationSite presentationSite = new DefaultStackPresentationSite() {

        public void close(IPresentablePart part) {
            PartStack.this.close(part);
        }

        public void close(IPresentablePart[] parts) {
            PartStack.this.close(parts);
        }

        public void dragStart(IPresentablePart beingDragged,
                Point initialLocation, boolean keyboard) {
            PartStack.this.dragStart(beingDragged, initialLocation, keyboard);
        }

        public void dragStart(Point initialLocation, boolean keyboard) {
            PartStack.this.dragStart(null, initialLocation, keyboard);
        }

        public boolean isPartMoveable(IPresentablePart part) {
            return PartStack.this.isMoveable(part);
        }

        public void selectPart(IPresentablePart toSelect) {
            PartStack.this.presentationSelectionChanged(toSelect);
        }

        public boolean supportsState(int state) {
            return PartStack.this.supportsState(state);
        }

        public void setState(int newState) {
            PartStack.this.setState(newState);
        }

        public IPresentablePart getSelectedPart() {
            return PartStack.this.getSelectedPart();
        }

        public void addSystemActions(IMenuManager menuManager) {
            PartStack.this.addSystemActions(menuManager);
        }

        public boolean isStackMoveable() {
            return canMoveFolder();
        }
        
        public void flushLayout() {
        	PartStack.this.flushLayout();
        }

        public IPresentablePart[] getPartList() {
            List parts = getPresentableParts();
            
            return (IPresentablePart[]) parts.toArray(new IPresentablePart[parts.size()]);
        }
    };

    private static final class PartStackDropResult extends AbstractDropTarget {
        private PartPane pane;
        
        // Result of the presentation's dragOver method or null if we are stacking over the
        // client area of the pane.
        private StackDropResult dropResult;
        private PartStack stack;
        
        /**
         * Resets the target of this drop result (allows the same drop result object to be
         * reused)
         * 
         * @param stack
         * @param pane
         * @param result result of the presentation's dragOver method, or null if we are
         * simply stacking anywhere.
         * @since 3.1
         */
        public void setTarget(PartStack stack, PartPane pane, StackDropResult result) {
            this.pane = pane;
            this.dropResult = result;
            this.stack = stack;
        }
        
        public void drop() {
            // If we're dragging a pane over itself do nothing
            //if (dropResult.getInsertionPoint() == pane.getPresentablePart()) { return; };

            Object cookie = null;
            if (dropResult != null) {
                cookie = dropResult.getCookie();
            }
            
            if (pane.getContainer() != stack) {
                // Moving from another stack
                stack.derefPart(pane);
                pane.reparent(stack.getParent());
                stack.add(pane, cookie);
                stack.setSelection(pane);
                pane.setFocus();
            } else if (cookie != null) {
                // Rearranging within this stack
                stack.getPresentation().movePart(pane.getPresentablePart(), cookie);
            }
        }

        public Cursor getCursor() {
            return DragCursors.getCursor(DragCursors.CENTER);
        }

        public Rectangle getSnapRectangle() {
            if (dropResult == null) {
                return DragUtil.getDisplayBounds(stack.getControl());
            }
            return dropResult.getSnapRectangle();
        }
    }

    private static final PartStackDropResult dropResult = new PartStackDropResult();

    private boolean isMinimized;

    private ListenerList listeners = new ListenerList();

    /**
     * Custom presentation factory to use for this stack, or null to
     * use the default
     */
    private AbstractPresentationFactory factory;
            
    protected abstract boolean isMoveable(IPresentablePart part);

    protected abstract void addSystemActions(IMenuManager menuManager);

    protected abstract boolean supportsState(int newState);

    protected abstract boolean canMoveFolder();

    protected abstract void derefPart(LayoutPart toDeref);

    protected abstract boolean allowsDrop(PartPane part);

    protected static void appendToGroupIfPossible(IMenuManager m,
            String groupId, ContributionItem item) {
        try {
            m.appendToGroup(groupId, item);
        } catch (IllegalArgumentException e) {
            m.add(item);
        }
    }
    
    /**
     * Creates a new PartStack, given a constant determining which presentation to use
     * 
     * @param appearance one of the PresentationFactoryUtil.ROLE_* constants
     */
    public PartStack(int appearance) {
        this(appearance, null);
    }
    
    /**
     * Creates a new part stack that uses the given custom presentation factory
     * @param appearance
     * @param factory custom factory to use (or null to use the default)
     */
    public PartStack(int appearance, AbstractPresentationFactory factory) {
        super("PartStack"); //$NON-NLS-1$

        this.appearance = appearance;
        this.factory = factory;
    }

    /**
     * Adds a property listener to this stack. The listener will receive a PROP_SELECTION
     * event whenever the result of getSelection changes
     * 
     * @param listener
     */
    public void addListener(IPropertyListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(IPropertyListener listener) {
        listeners.remove(listener);
    }
    
    protected final boolean isStandalone() {
        return (appearance == PresentationFactoryUtil.ROLE_STANDALONE 
             || appearance == PresentationFactoryUtil.ROLE_STANDALONE_NOTITLE);
    }
    
    /**
     * Returns the currently selected IPresentablePart, or null if none
     * 
     * @return
     */
    protected IPresentablePart getSelectedPart() {
        if (presentationCurrent == null) {
            return null;
        }
        
        return presentationCurrent.getPresentablePart();
    }

    protected IStackPresentationSite getPresentationSite() {
        return presentationSite;
    }

    /**
     * Tests the integrity of this object. Throws an exception if the object's state
     * is invalid. For use in test suites.
     */
    public void testInvariants() {
        Control focusControl = Display.getCurrent().getFocusControl();

        boolean currentFound = false;

        LayoutPart[] children = getChildren();

        for (int idx = 0; idx < children.length; idx++) {
            LayoutPart child = children[idx];

            // No null children allowed
            Assert.isNotNull(child,
                    "null children are not allowed in PartStack"); //$NON-NLS-1$

            // This object can only contain placeholders or PartPanes
            Assert.isTrue(child instanceof PartPlaceholder
                    || child instanceof PartPane,
                    "PartStack can only contain PartPlaceholders or PartPanes"); //$NON-NLS-1$

            // Ensure that all the PartPanes have an associated presentable part 
            IPresentablePart part = child.getPresentablePart();
            if (child instanceof PartPane) {
                Assert.isNotNull(part,
                        "All PartPanes must have a non-null IPresentablePart"); //$NON-NLS-1$
            }

            // Ensure that the child's backpointer points to this stack
            ILayoutContainer childContainer = child.getContainer();

            // Disable tests for placeholders -- PartPlaceholder backpointers don't
            // obey the usual rules -- they sometimes point to a container placeholder
            // for this stack instead of the real stack.
            if (!(child instanceof PartPlaceholder)) {

                if (isDisposed()) {

                    // Currently, we allow null backpointers if the widgetry is disposed.
                    // However, it is never valid for the child to have a parent other than
                    // this object
                    if (childContainer != null) {
                        Assert
                                .isTrue(childContainer == this,
                                        "PartStack has a child that thinks it has a different parent"); //$NON-NLS-1$
                    }
                } else {
                    // If the widgetry exists, the child's backpointer must point to us
                    Assert
                            .isTrue(childContainer == this,
                                    "PartStack has a child that thinks it has a different parent"); //$NON-NLS-1$

                    // If this child has focus, then ensure that it is selected and that we have
                    // the active appearance.

                    if (SwtUtil.isChild(child.getControl(), focusControl)) {
                        Assert.isTrue(child == current,
                                "The part with focus is not the selected part"); //$NON-NLS-1$
                        //  focus check commented out since it fails when focus workaround in LayoutPart.setVisible is not present       			
                        //        			Assert.isTrue(getActive() == StackPresentation.AS_ACTIVE_FOCUS);
                    }
                }
            }

            // Ensure that "current" points to a valid child
            if (child == current) {
                currentFound = true;
            }

            // Test the child's internal state
            child.testInvariants();
        }

        // If we have at least one child, ensure that the "current" pointer points to one of them
        if (!isDisposed() && getPresentableParts().size() > 0) {
            Assert.isTrue(currentFound);

            if (!isDisposed()) {
                StackPresentation presentation = getPresentation();

                // If the presentation controls have focus, ensure that we have the active appearance
                if (SwtUtil.isChild(presentation.getControl(), focusControl)) {
                    Assert
                            .isTrue(
                                    getActive() == StackPresentation.AS_ACTIVE_FOCUS,
                                    "The presentation has focus but does not have the active appearance"); //$NON-NLS-1$
                }
            }
        }
        
        // Check to that we're displaying the zoomed icon iff we're actually maximized
        Assert.isTrue((getState() == IStackPresentationSite.STATE_MAXIMIZED) 
                == (getContainer() != null && getContainer().childIsZoomed(this)));

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#describeLayout(java.lang.StringBuffer)
     */
    public void describeLayout(StringBuffer buf) {
        int activeState = getActive();
        if (activeState == StackPresentation.AS_ACTIVE_FOCUS) {
            buf.append("active "); //$NON-NLS-1$
        } else if (activeState == StackPresentation.AS_ACTIVE_NOFOCUS) {
            buf.append("active_nofocus "); //$NON-NLS-1$
        }

        buf.append("("); //$NON-NLS-1$

        LayoutPart[] children = ((ILayoutContainer) this).getChildren();

        int visibleChildren = 0;

        for (int idx = 0; idx < children.length; idx++) {

            LayoutPart next = children[idx];
            if (!(next instanceof PartPlaceholder)) {
                if (idx > 0) {
                    buf.append(", "); //$NON-NLS-1$
                }

                if (next == requestedCurrent) {
                    buf.append("*"); //$NON-NLS-1$
                }

                next.describeLayout(buf);

                visibleChildren++;
            }
        }

        buf.append(")"); //$NON-NLS-1$
    }

    /**
     * See IVisualContainer#add
     */
    public void add(LayoutPart child) {
        children.add(child);
        showPart(child, null);
    }

    /**
     * Add a part at a particular position
     */
    protected void add(LayoutPart newChild, Object cookie) {
        children.add(newChild);

        showPart(newChild, cookie);
    }

    public boolean allowsAdd(LayoutPart toAdd) {
        return !isStandalone();
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.internal.ILayoutContainer#allowsAutoFocus()
     */
    public boolean allowsAutoFocus() {
        if (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) {
            return false;
        }

        return super.allowsAutoFocus();
    }

    /**
     * @param parts
     */
    protected void close(IPresentablePart[] parts) {
        for (int idx = 0; idx < parts.length; idx++) {
            IPresentablePart part = parts[idx];

            close(part);
        }
    }

    /**
     * @param part
     */
    protected void close(IPresentablePart part) {
        if (!presentationSite.isCloseable(part)) {
            return;
        }

        LayoutPart layoutPart = getPaneFor(part);

        if (layoutPart != null && layoutPart instanceof PartPane) {
            PartPane viewPane = (PartPane) layoutPart;

            viewPane.doHide();
        }
    }

    public boolean isDisposed() {
        return getPresentation() == null;
    }

    protected AbstractPresentationFactory getFactory() {
        
        if (factory != null) {
            return factory;
        }
        
        return ((WorkbenchWindow) getPage()
                .getWorkbenchWindow()).getWindowConfigurer()
                .getPresentationFactory();
    }
    
    public void createControl(Composite parent) {
        if (!isDisposed()) {
            return;
        }

        AbstractPresentationFactory factory = getFactory();

        PresentationSerializer serializer = new PresentationSerializer(
                getPresentableParts());

        StackPresentation presentation = PresentationFactoryUtil
                .createPresentation(factory, appearance, parent,
                        presentationSite, serializer, savedPresentationState);

        createControl(parent, presentation);
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#getDropTarget(java.lang.Object, org.eclipse.swt.graphics.Point)
     */
    public IDropTarget getDropTarget(Object draggedObject, Point position) {

        if (!(draggedObject instanceof PartPane)) {
            return null;
        }

        final PartPane pane = (PartPane) draggedObject;
        if (isStandalone() 
                || !allowsDrop(pane)) {
            return null;
        }

        // Don't allow views to be dragged between windows
        if (pane.getWorkbenchWindow() != getWorkbenchWindow()) {
            return null;
        }

        StackDropResult dropResult = getPresentation().dragOver(
                getControl(), position);
        
        if (dropResult == null) {
        	return null;
        }
        
        return createDropTarget(pane, dropResult); 
    }
    
    
    public void createControl(Composite parent, StackPresentation presentation) {

        Assert.isTrue(isDisposed());

        if (presentationSite.getPresentation() != null)
            return;

        presentationSite.setPresentation(presentation);

        // Add all visible children to the presentation
        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart part = (LayoutPart) iter.next();

            showPart(part, null);
        }

        Control ctrl = getPresentation().getControl();

        ctrl.setData(this);

//        updateActions(presentationCurrent);

        // We should not have a placeholder selected once we've created the widgetry
        if (requestedCurrent instanceof PartPlaceholder) {
            requestedCurrent = null;
            updateContainerVisibleTab();
        }

        refreshPresentationSelection();
    }

    public IDropTarget createDropTarget(PartPane pane, StackDropResult result) {
        dropResult.setTarget(this, pane, result);
        return dropResult;
    }
    
    /**
     * Saves the current state of the presentation to savedPresentationState, if the
     * presentation exists.
     */
    protected void savePresentationState() {
        if (isDisposed()) {
            return;
        }

        {// Save the presentation's state before disposing it
            XMLMemento memento = XMLMemento
                    .createWriteRoot(IWorkbenchConstants.TAG_PRESENTATION);
            memento.putString(IWorkbenchConstants.TAG_ID, getFactory().getId());

            PresentationSerializer serializer = new PresentationSerializer(
                    getPresentableParts());

            getPresentation().saveState(serializer, memento);

            // Store the memento in savedPresentationState
            savedPresentationState = memento;
        }
    }

    /**
     * See LayoutPart#dispose
     */
    public void dispose() {

        if (isDisposed())
            return;

        savePresentationState();

        presentationSite.dispose();

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            next.setContainer(null);
        }
        
        presentationCurrent = null;
        current = null;
        fireInternalPropertyChange(PROP_SELECTION);
    }

    public void findSashes(LayoutPart part, PartPane.Sashes sashes) {
        ILayoutContainer container = getContainer();

        if (container != null) {
            container.findSashes(this, sashes);
        }
    }

    /**
     * Gets the presentation bounds.
     */
    public Rectangle getBounds() {
        if (getPresentation() == null) {
            return new Rectangle(0, 0, 0, 0);
        }

        return getPresentation().getControl().getBounds();
    }

    /**
     * See IVisualContainer#getChildren
     */
    public LayoutPart[] getChildren() {
        return (LayoutPart[]) children.toArray(new LayoutPart[children.size()]);
    }

    public Control getControl() {
        StackPresentation presentation = getPresentation();

        if (presentation == null) {
            return null;
        }

        return presentation.getControl();
    }

    /**
     * Answer the number of children.
     */
    public int getItemCount() {
        if (isDisposed()) {
            return children.size();
        }
        return getPresentableParts().size();
    }
    
    /**
     * Returns the LayoutPart for the given IPresentablePart, or null if the given
     * IPresentablePart is not in this stack. Returns null if given a null argument.
     * 
     * @param part to locate or null
     * @return
     */
    protected LayoutPart getPaneFor(IPresentablePart part) {
        if (part == null) {
            return null;
        }

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            if (next.getPresentablePart() == part) {
                return next;
            }
        }

        return null;
    }

    /**
     * Get the parent control.
     */
    public Composite getParent() {
        return getControl().getParent();
    }

    private IPresentablePart getPresentablePartAtIndex(int idx) {
        List presentableParts = getPresentableParts();

        if (idx >= 0 && idx < presentableParts.size()) {
            return (IPresentablePart) presentableParts.get(idx);
        }

        return null;
    }

    /**
     * Returns a list of IPresentablePart
     * 
     * @return
     */
    public List getPresentableParts() {
        List result = new ArrayList(children.size());

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart part = (LayoutPart) iter.next();

            IPresentablePart presentablePart = part.getPresentablePart();

            if (presentablePart != null) {
                result.add(presentablePart);
            }
        }

        return result;
    }

    protected StackPresentation getPresentation() {
        return presentationSite.getPresentation();
    }

    /**
     * Returns the visible child.
     * @return the currently visible part, or null if none
     */
    public PartPane getSelection() {
        if (current instanceof PartPane) {
            return (PartPane) current;
        }
        return null;
    }

    private void presentationSelectionChanged(IPresentablePart newSelection) {
        // Ignore selection changes that occur as a result of removing a part
        if (ignoreSelectionChanges) {
            return;
        }
        LayoutPart newPart = getPaneFor(newSelection);

        // This method should only be called on objects that are already in the layout
        Assert.isNotNull(newPart);

        if (newPart == requestedCurrent) {
            return;
        }

        setSelection(newPart);

        if (newPart != null) {
            newPart.setFocus();
        }

    }

    /**
     * See IVisualContainer#remove
     */
    public void remove(LayoutPart child) {
        IPresentablePart presentablePart = child.getPresentablePart();

        // Need to remove it from the list of children before notifying the presentation
        // since it may setVisible(false) on the part, leading to a partHidden notification,
        // during which findView must not find the view being removed.  See bug 60039. 
        children.remove(child);

        StackPresentation presentation = getPresentation();

        if (presentablePart != null && presentation != null) {
            ignoreSelectionChanges = true;
            presentation.removePart(presentablePart);
            ignoreSelectionChanges = false;
        }

        if (!isDisposed()) {
            child.setContainer(null);
        }

        if (child == requestedCurrent) {
            updateContainerVisibleTab();
        }
    }

    /**
     * Reparent a part. Also reparent visible children...
     */
    public void reparent(Composite newParent) {

        Control control = getControl();
        if ((control == null) || (control.getParent() == newParent) || !control.isReparentable())
            return;

        super.reparent(newParent);

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();
            next.reparent(newParent);
        }
    }

    /**
     * See IVisualContainer#replace
     */
    public void replace(LayoutPart oldChild, LayoutPart newChild) {
        int idx = children.indexOf(oldChild);
        int numPlaceholders = 0;
        //subtract the number of placeholders still existing in the list 
        //before this one - they wont have parts.
        for (int i = 0; i < idx; i++) {
            if (children.get(i) instanceof PartPlaceholder)
                numPlaceholders++;
        }
        Integer cookie = new Integer(idx - numPlaceholders);
        children.add(idx, newChild);

        showPart(newChild, cookie);

        if (oldChild == requestedCurrent && !(newChild instanceof PartPlaceholder)) {
            setSelection(newChild);
        }

        remove(oldChild);
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#computePreferredSize(boolean, int, int, int)
	 */
	public int computePreferredSize(boolean width, int availableParallel,
			int availablePerpendicular, int preferredParallel) {
		
		return getPresentation().computePreferredSize(width, availableParallel, 
				availablePerpendicular, preferredParallel);
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#getSizeFlags(boolean)
     */
    public int getSizeFlags(boolean horizontal) {
        StackPresentation presentation = getPresentation();
        
        if (presentation != null) {
            return presentation.getSizeFlags(horizontal);
        } 
        
        return 0;
    }
    
    /**
     * @see IPersistable
     */
    public IStatus restoreState(IMemento memento) {
        // Read the active tab.
        String activeTabID = memento
                .getString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID);

        // Read the page elements.
        IMemento[] children = memento.getChildren(IWorkbenchConstants.TAG_PAGE);
        if (children != null) {
            // Loop through the page elements.
            for (int i = 0; i < children.length; i++) {
                // Get the info details.
                IMemento childMem = children[i];
                String partID = childMem
                        .getString(IWorkbenchConstants.TAG_CONTENT);

                // Create the part.
                LayoutPart part = new PartPlaceholder(partID);
                part.setContainer(this);
                add(part);
                //1FUN70C: ITPUI:WIN - Shouldn't set Container when not active
                //part.setContainer(this);
                if (partID.equals(activeTabID)) {
                    setSelection(part);
                    // Mark this as the active part.
                    //current = part;
                }
            }
        }

        Integer expanded = memento.getInteger(IWorkbenchConstants.TAG_EXPANDED);
        setState((expanded == null || expanded.intValue() != IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_RESTORED
                : IStackPresentationSite.STATE_MINIMIZED);

        Integer appearance = memento
                .getInteger(IWorkbenchConstants.TAG_APPEARANCE);
        if (appearance != null) {
            // Detached views always have the same appearance -- ignore anything that is persisted
            // here
            if (this.appearance != PresentationFactoryUtil.ROLE_DETACHED) {
                this.appearance = appearance.intValue();
            }
        }

        // Determine if the presentation has saved any info here
        savedPresentationState = null;
        IMemento[] presentationMementos = memento
                .getChildren(IWorkbenchConstants.TAG_PRESENTATION);

        for (int idx = 0; idx < presentationMementos.length; idx++) {
            IMemento child = presentationMementos[idx];

            String id = child.getString(IWorkbenchConstants.TAG_ID);

            if (Util.equals(id, getFactory().getId())) {
                savedPresentationState = child;
                break;
            }
        }

        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#setVisible(boolean)
     */
    public void setVisible(boolean makeVisible) {
        super.setVisible(makeVisible);
        
        StackPresentation presentation = getPresentation();
        
        if (presentation != null) {
            presentation.setVisible(makeVisible);
        }
    }
    
    /**
     * @see IPersistable
     */
    public IStatus saveState(IMemento memento) {

        // Save the active tab.
        if (requestedCurrent != null)
            memento.putString(IWorkbenchConstants.TAG_ACTIVE_PAGE_ID, requestedCurrent
                    .getCompoundId());

        Iterator iter = children.iterator();
        while (iter.hasNext()) {
            LayoutPart next = (LayoutPart) iter.next();

            IMemento childMem = memento
                    .createChild(IWorkbenchConstants.TAG_PAGE);

            IPresentablePart part = next.getPresentablePart();
            String tabText = "LabelNotFound"; //$NON-NLS-1$ 
            if (part != null) {
                tabText = part.getName();
            }
            childMem.putString(IWorkbenchConstants.TAG_LABEL, tabText);
            childMem.putString(IWorkbenchConstants.TAG_CONTENT, next
                    .getCompoundId());
        }

        memento
                .putInteger(
                        IWorkbenchConstants.TAG_EXPANDED,
                        (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED) ? IStackPresentationSite.STATE_MINIMIZED
                                : IStackPresentationSite.STATE_RESTORED);

        memento.putInteger(IWorkbenchConstants.TAG_APPEARANCE, appearance);

        savePresentationState();

        if (savedPresentationState != null) {
            IMemento presentationState = memento
                    .createChild(IWorkbenchConstants.TAG_PRESENTATION);
            presentationState.putMemento(savedPresentationState);
        }

        return new Status(IStatus.OK, PlatformUI.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
    }

    protected WorkbenchPage getPage() {
        WorkbenchWindow window = (WorkbenchWindow) getWorkbenchWindow();

        if (window == null) {
            return null;
        }

        return (WorkbenchPage) window.getActivePage();
    }

    /**
     * Set the active appearence on the tab folder.
     * 
     * @param active
     */
    public void setActive(int activeState) {

        if (activeState == StackPresentation.AS_ACTIVE_FOCUS) {
            setMinimized(false);
        }

        presentationSite.setActive(activeState);
    }

    public int getActive() {
        return presentationSite.getActive();
    }

    /**
     * Sets the presentation bounds.
     */
    public void setBounds(Rectangle r) {
    	
        if (getPresentation() != null) {
            getPresentation().setBounds(r);
        }
    }

    public void setSelection(LayoutPart part) {
        if (part == requestedCurrent) {
            return;
        }

        requestedCurrent = part;
        
        refreshPresentationSelection();
    }

    /**
     * Subclasses should override this method to update the enablement state of their
     * actions
     */
    protected abstract void updateActions(LayoutPart current);

    /* (non-Javadoc)
	 * @see org.eclipse.ui.internal.LayoutPart#handleDeferredEvents()
	 */
	protected void handleDeferredEvents() {
		super.handleDeferredEvents();
		
		refreshPresentationSelection();
	}
    
    private void refreshPresentationSelection() {        
        // If deferring UI updates, exit.
    	if (isDeferred()) {
    		return;
    	}
        
        // If the presentation is already displaying the desired part, then there's nothing
        // to do.
        if (current == requestedCurrent) {
            return;
        }

        StackPresentation presentation = getPresentation();
        if (presentation != null) {
        
            presentationCurrent = requestedCurrent;
            
            if (!isDisposed()) {
                updateActions(presentationCurrent);
            }
            
            if (presentationCurrent != null) {
                IPresentablePart presentablePart = requestedCurrent.getPresentablePart();
                    
                if (presentablePart != null && presentation != null) {
                    requestedCurrent.createControl(getParent());
                    if (requestedCurrent.getControl().getParent() != getControl()
                            .getParent()) {
                        requestedCurrent.reparent(getControl().getParent());
                    }
    
                    requestedCurrent.moveAbove(getPresentation().getControl());
                    
                    presentation.selectPart(presentationCurrent.getPresentablePart());                    
                }
            }
        
            // Update the return value of getVisiblePart
            current = requestedCurrent;
            fireInternalPropertyChange(PROP_SELECTION);
        }
    }

    public int getState() {
    	return presentationSite.getState();
    }
    
    /**
     * Sets the minimized state for this stack. The part may call this method to minimize or restore
     * itself. The minimized state only affects the view when unzoomed. If the 
     */
    public void setMinimized(boolean minimized) {
        if (minimized != isMinimized) {
            isMinimized = minimized;
            
            refreshPresentationState();            
        }
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.ILayoutContainer#obscuredByZoom(org.eclipse.ui.internal.LayoutPart)
     */
    public boolean childObscuredByZoom(LayoutPart toTest) {
        return isObscuredByZoom();
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#requestZoom(org.eclipse.ui.internal.LayoutPart)
     */
    public void childRequestZoomIn(LayoutPart toZoom) {
        super.childRequestZoomIn(toZoom);
        
        requestZoomIn();
    }
    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.LayoutPart#requestZoomOut()
     */
    public void childRequestZoomOut() {
        super.childRequestZoomOut();
        
        requestZoomOut();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.internal.ILayoutContainer#isZoomed(org.eclipse.ui.internal.LayoutPart)
     */
    public boolean childIsZoomed(LayoutPart toTest) {
        return isZoomed();
    }
    
    protected void setState(int newState) {
        int oldState = presentationSite.getState();
        if (!supportsState(newState) || newState == oldState) {
            return;
        }
        
        boolean minimized = (newState == IStackPresentationSite.STATE_MINIMIZED);
        
        setMinimized(minimized);
        
        if (newState == IStackPresentationSite.STATE_MAXIMIZED) {
            requestZoomIn();
        } else if (oldState == IStackPresentationSite.STATE_MAXIMIZED) {
            requestZoomOut();
        }
    }
    

    /**
     * Called by the workbench page to notify this part that it has been zoomed or unzoomed.
     * The PartStack should not call this method itself -- it must request zoom changes by 
     * talking to the WorkbenchPage.
     */
    public void setZoomed(boolean isZoomed) {
        
        super.setZoomed(isZoomed);
        
        LayoutPart[] children = getChildren();
        
        for (int i = 0; i < children.length; i++) {
            LayoutPart next = children[i];
            
            next.setZoomed(isZoomed);
        }
        
        refreshPresentationState();
    }
    
    public boolean isZoomed() {
        ILayoutContainer container = getContainer();
        
        if (container != null) {
            return container.childIsZoomed(this);
        }
        
        return false;
    }
    
    private void refreshPresentationState() {
        if (isZoomed()) {
            presentationSite.setPresentationState(IStackPresentationSite.STATE_MAXIMIZED);
        } else {
            
            boolean wasMinimized = (presentationSite.getState() == IStackPresentationSite.STATE_MINIMIZED);
            
            if (isMinimized) {
                presentationSite.setPresentationState(IStackPresentationSite.STATE_MINIMIZED);
            } else {
                presentationSite.setPresentationState(IStackPresentationSite.STATE_RESTORED);
            }
            
            if (isMinimized != wasMinimized) {
                flushLayout();
                
                if (isMinimized) {
	                WorkbenchPage page = getPage();
	
	                if (page != null) {
	                    page.refreshActiveView();
	                }
                }
            }
        }
    }

    /**
     * Makes the given part visible in the presentation
     * 
     * @param presentablePart
     */
    private void showPart(LayoutPart part, Object cookie) {

        if (isDisposed()) {
            return;
        }

        part.setContainer(this);

        IPresentablePart presentablePart = part.getPresentablePart();

        if (presentablePart == null) {
            return;
        }

        presentationSite.getPresentation().addPart(presentablePart, cookie);

        if (requestedCurrent == null) {
            setSelection(part);
        }
    }

    /**
     * Update the container to show the correct visible tab based on the
     * activation list.
     * 
     * @param org.eclipse.ui.internal.ILayoutContainer
     */
    private void updateContainerVisibleTab() {
        LayoutPart[] parts = getChildren();

        if (parts.length < 1) {
            setSelection(null);
            return;
        }

        PartPane selPart = null;
        int topIndex = 0;
        WorkbenchPage page = getPage();

        if (page != null) {
            IWorkbenchPartReference sortedPartsArray[] = page.getSortedParts();
            List sortedParts = Arrays.asList(sortedPartsArray);
            for (int i = 0; i < parts.length; i++) {
                if (parts[i] instanceof PartPane) {
                    IWorkbenchPartReference part = ((PartPane) parts[i])
                            .getPartReference();
                    int index = sortedParts.indexOf(part);
                    if (index >= topIndex) {
                        topIndex = index;
                        selPart = (PartPane) parts[i];
                    }
                }
            }

        }

        if (selPart == null) {
            List presentableParts = getPresentableParts();
            if (presentableParts.size() != 0) {
                IPresentablePart part = (IPresentablePart) getPresentableParts()
                        .get(0);

                selPart = (PartPane) getPaneFor(part);
            }
        }

        setSelection(selPart);
    }

    /**
     * 
     */
    public void showSystemMenu() {
        getPresentation().showSystemMenu();
    }

    public void showPaneMenu() {
        getPresentation().showPaneMenu();
    }

    public void showPartList() {
        getPresentation().showPartList();
    }

    /**
     * @param pane
     * @return
     */
    public Control[] getTabList(LayoutPart part) {
        if (part != null) {
            IPresentablePart presentablePart = part.getPresentablePart();
            StackPresentation presentation = getPresentation();

            if (presentablePart != null && presentation != null) {
                return presentation.getTabList(presentablePart);
            }
        }

        return new Control[0];
    }

    /**
     * 
     * @param beingDragged
     * @param initialLocation
     * @param keyboard
     */
    public void dragStart(IPresentablePart beingDragged, Point initialLocation,
            boolean keyboard) {
        if (beingDragged == null) {
            if (canMoveFolder()) {
                if (presentationSite.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
                    setState(IStackPresentationSite.STATE_RESTORED);
                }

                DragUtil.performDrag(PartStack.this, Geometry
                        .toDisplay(getParent(), getPresentation().getControl()
                                .getBounds()), initialLocation, !keyboard);
            }
        } else {
            if (presentationSite.isPartMoveable(beingDragged)) {
                LayoutPart pane = getPaneFor(beingDragged);

                if (pane != null) {
                    if (presentationSite.getState() == IStackPresentationSite.STATE_MAXIMIZED) {
                        presentationSite
                                .setState(IStackPresentationSite.STATE_RESTORED);
                    }

                    DragUtil.performDrag(pane, Geometry.toDisplay(getParent(),
                            getPresentation().getControl().getBounds()),
                            initialLocation, !keyboard);
                }
            }
        }
    }

    /**
     * @return Returns the savedPresentationState.
     */
    public IMemento getSavedPresentationState() {
        return savedPresentationState;
    }
    
    private void fireInternalPropertyChange(int id) {
        Object listeners[] = this.listeners.getListeners();
        for (int i = 0; i < listeners.length; i++) {
            ((IPropertyListener) listeners[i]).propertyChanged(this, id);
        }
    }
}
