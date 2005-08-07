//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.addressbook.gui.focus;

import javax.swing.JComponent;


/**
 *
 *
 * Default Implementation of FocusOwner
 *
 * @author fdietz
 */
public abstract class AbstractFocusOwner implements FocusOwner {
    /**
 * default constructor
 *
 */
    public AbstractFocusOwner() {
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#isCutActionEnabled()
 */
    public boolean isCutActionEnabled() {
        return false;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#isCopyActionEnabled()
 */
    public boolean isCopyActionEnabled() {
        return false;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#isPasteActionEnabled()
 */
    public boolean isPasteActionEnabled() {
        return false;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#isDeleteActionEnabled()
 */
    public boolean isDeleteActionEnabled() {
        return false;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#isSelectAllActionEnabled()
 */
    public boolean isSelectAllActionEnabled() {
        return false;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#isUndoActionEnabled()
 */
    public boolean isUndoActionEnabled() {
        return false;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#isRedoActionEnabled()
 */
    public boolean isRedoActionEnabled() {
        return false;
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#cut()
 */
    public void cut() {
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#copy()
 */
    public void copy() {
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#paste()
 */
    public void paste() {
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#delete()
 */
    public void delete() {
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#undo()
 */
    public void undo() {
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#redo()
 */
    public void redo() {
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#selectAll()
 */
    public void selectAll() {
    }

    /* (non-Javadoc)
 * @see org.columba.core.gui.frame.focus.FocusOwner#getComponent()
 */
    public abstract JComponent getComponent();
}
