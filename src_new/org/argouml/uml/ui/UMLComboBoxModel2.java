// Copyright (c) 1996-99 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.uml.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;

import org.apache.log4j.Category;


import ru.novosoft.uml.MBase;
import ru.novosoft.uml.MElementEvent;
import ru.novosoft.uml.foundation.core.MModelElement;

/**
 * ComboBoxmodel for UML modelelements. This implementation does not use 
 * reflection and seperates Model, View and Controller better then does
 * UMLComboBoxModel. In the future UMLComboBoxModel and UMLComboBox will be
 * replaced with this implementation to improve performance.
 */
public abstract class UMLComboBoxModel2
    extends AbstractListModel
    implements UMLUserInterfaceComponent, ComboBoxModel {
        
    private static Category log = 
        Category.getInstance("org.argouml.uml.ui.UMLComboBoxModel2");
        
    private UMLUserInterfaceContainer _container = null;
    private Object _target = null;
    
    private List _objects = Collections.synchronizedList(new ArrayList());
    private Object _selectedObject = null;
    private boolean _clearable = false;
    
	
    /**
     * Constructs a model for a combobox. The container given is used to retreive
     * the target that is manipulated through this combobox. If clearable is true,
     * the user can select null in the combobox and thereby clear the attribute
     * in the model.
     * @param container
     * @param propertySetName
     * @param roleAddedName
     * @throws IllegalArgumentException if one of the arguments is null
     */
    public UMLComboBoxModel2(UMLUserInterfaceContainer container, boolean clearable) {
        super();
        if (container == null) throw new IllegalArgumentException("In UMLComboBoxModel2: one of the arguments is null");
        // it would be better that we don't need the container to get the target
        // this constructor can be without parameters as soon as we improve
        // targetChanged
        _clearable = clearable;
        setContainer(container);
        targetChanged();
    }
    
    

    /**
     * @see ru.novosoft.uml.MElementListener#listRoleItemSet(MElementEvent)
     */
    public void listRoleItemSet(MElementEvent e) {
    }

    /**
     * @see ru.novosoft.uml.MElementListener#propertySet(MElementEvent)
     */
    public void propertySet(MElementEvent e) {
        if (isValidRoleAdded(e)) { 
            Object o = getChangedElement(e);
            if (o instanceof Collection) {
                addAll((Collection)o);
            } else {
                if (getIndexOf(o) < 0) {
                    addElement(o);
                }
            }
            setSelectedItem(getSelectedModelElement());
        } else
        if (isValidPropertySet(e)) {
            setSelectedItem(getSelectedModelElement());
        }
    }

    /**
     * @see ru.novosoft.uml.MElementListener#recovered(MElementEvent)
     */
    public void recovered(MElementEvent e) {
    }

    /**
     * @see ru.novosoft.uml.MElementListener#removed(MElementEvent)
     */
    public void removed(MElementEvent e) {
        Object o = getChangedElement(e);
        if (getIndexOf(o) >= 0) {
            removeElement(o);
        }
    }

    /**
     * @see ru.novosoft.uml.MElementListener#roleAdded(MElementEvent)
     */
    public void roleAdded(MElementEvent e) {
        if (isValidRoleAdded(e)) { 
            Object o = getChangedElement(e);
            if (o instanceof Collection) {
                addAll((Collection)o);
            } else {
                if (getIndexOf(o) < 0) {
                    addElement(o);
                }
            }
            setSelectedItem(getSelectedModelElement());
        }
    }

    /**
     * @see ru.novosoft.uml.MElementListener#roleRemoved(MElementEvent)
     */
    public void roleRemoved(MElementEvent e) {
        if (isValidRoleRemoved(e)) {
            Object o = getChangedElement(e);
            if (o instanceof Collection) {
                removeAll((Collection)o);
            } else {
                removeElement(o);
            }      
        }
    }

    /**
     * Returns the container.
     * @return UMLUserInterfaceContainer
     */
    protected UMLUserInterfaceContainer getContainer() {
        return _container;
    }

    /**
     * Sets the container.
     * @param container The container to set
     */
    protected void setContainer(UMLUserInterfaceContainer container) {
        _container = container;
    }

    /**
     * @see org.argouml.uml.ui.UMLUserInterfaceComponent#targetChanged()
     */
    public void targetChanged() {
        // targetchanged should actually propagate an event with the source of
        // the change (the actual old and new target)
        // this must be implemented in the whole of argo one time or another
        // to improve performance and reduce errors
        setTarget(getContainer().getTarget());
        removeAllElements();
        buildModelList();
        setSelectedItem(getSelectedModelElement());
        if (getSelectedItem() != null && _clearable) {
            addElement(""); // makes sure we can select 'none'
        }
    }

    /**
     * @see org.argouml.uml.ui.UMLUserInterfaceComponent#targetReasserted()
     */
    public void targetReasserted() {
        // in the current implementation of argouml, history is not implemented
        // this event is for future releases
    }
    
    /**
     * Returns true if roleAdded(MElementEvent e) should be executed. Developers
     * should override this method and not directly override roleAdded.  
     * @param m
     * @return boolean
     */
    protected abstract boolean isValidRoleAdded(MElementEvent e);
    
    
    /**
     * Returns true if roleRemoved(MElementEvent e) should be executed. Standard
     * behaviour is such that some element that is changed allways may be 
     * removed.
     * @param m
     * @return boolean
     */
    protected boolean isValidRoleRemoved(MElementEvent e) {
        return getIndexOf(getChangedElement(e)) >= 0;
    }
    
    /**
     * Returns true if propertySet(MElementEvent e) should be executed. Developers
     * should override this method and not directly override propertySet in order
     * to let this comboboxmodel and the combobox(es) representing this model 
     * function properly.  
     * @param m
     * @return boolean
     */
    protected abstract boolean isValidPropertySet(MElementEvent e);
    
    
    
    /**
     * Builds the list of elements and sets the selectedIndex to the currently 
     * selected item if there is one. Called from targetChanged every time the 
     * target of the proppanel is changed.
     */
    protected abstract void buildModelList();
 
 
    /**
     * Utility method to change all elements in the list with modelelements
     * at once.
     * @param elements
     */
    protected void setElements(Collection elements) {
        if (elements != null) {
            removeAllElements();
            addAll(elements);
        } else
            throw new IllegalArgumentException("In setElements: may not set " +
                "elements to null collection");
    }
    
    /**
     * Utility method to get the target. Sets the _target if the _target is null
     * via the method setTarget().
     * @return MModelElement
     */
    protected Object getTarget() {
        if (_target == null) {
            setTarget(getContainer().getTarget());
        }
        return _target;
    }
    
    /**
     * Utility method to remove a collection of elements from the model
     * @param col
     */
    protected void removeAll(Collection col) {
        // we don't want to mark to many elements as changed. 
        // therefore we don't directly call removeall on the list
        Iterator it = col.iterator();
        while (it.hasNext()) {
            removeElement(it.next());
        }
    }
    
    /**
     * Utility method to add a collection of elements to the model
     * @param col
     */
    protected void addAll(Collection col) {
        Iterator it = col.iterator();
        // addElement has side effects so we have to do something for that
        Object o2 = getSelectedItem();
        while (it.hasNext()) {
            Object o = it.next();
            if (getIndexOf(o) < 0) {
                addElement(o);
            }
        }
        setSelectedItem(o2);
    }
    
    
    
    /**
     * Utility method to get the changed element from some event e
     * @param e
     * @return Object
     */
    protected Object getChangedElement(MElementEvent e) {
        if (e.getAddedValue() != null) return e.getAddedValue();
        if (e.getRemovedValue() != null) return e.getRemovedValue();
        if (e.getNewValue() != null) return e.getNewValue();
        return null;
    }
    
    /**
     * Sets the target. If the old target is instanceof MBase, it also removes
     * the model from the element listener list of the target. If the new target
     * is instanceof MBase, the model is added as element listener to the new 
     * target.
     * @param target
     */
    protected void setTarget(Object target) {
        if (_target instanceof MBase) {
            ((MBase)_target).removeMElementListener(this);
        }
        _target = target;
        if (target instanceof MBase) {
            ((MBase)_target).addMElementListener(this);
        }
    }
    
    protected abstract Object getSelectedModelElement();

   

    /**
     * @see javax.swing.ListModel#getElementAt(int)
     */
    public Object getElementAt(int index) {
        return _objects.get(index);
    }

    /**
     * @see javax.swing.ListModel#getSize()
     */
    public int getSize() {
        return _objects.size();
    }
    
    public int getIndexOf(Object o) {
        return _objects.indexOf(o);
    }
    
    public void addElement(Object o) {
        if (!_objects.contains(o)) {
            _objects.add(o);
        }
    }
    
    public void setSelectedItem(Object o) {
        if (_objects.contains(o)) {
            _selectedObject = o;
        } else
            _selectedObject = null;
    }
    
    public void removeElement(Object o) {
        _objects.remove(o);
        if (_selectedObject == o) {
            _selectedObject = null;
        } 
    }
    
    public void removeAllElements() {
        _objects.clear();
        _selectedObject = null;
    }
    
    public Object getSelectedItem() {
        return _selectedObject;
    }

}
