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
package org.columba.mail.gui.composer;

import java.awt.Font;
import java.util.Observable;

import javax.swing.JComponent;

import org.columba.core.gui.focus.FocusOwner;


/**
 * This class serves as a common super class for the Editor
 * Controllers used in Composer: TextEditorController and
 * HtmlEditorController. As such, it defines the common
 * interface needed by mainly the ComposerController.
 * <p>
 * It extends Observable to allow all actions to enable/disable
 * themselves on text selection changes.
 *
 * @author Karl Peder Olesen (karlpeder), 2003-09-06
 */
public abstract class AbstractEditorController extends Observable
    implements FocusOwner {
    /** Reference to the controller */
    protected ComposerController controller;

    /**
 * Default constructor. Stores a reference to the controller
 * @param        ctrl        Controller controlling this object
 */
    public AbstractEditorController(ComposerController ctrl) {
        controller = ctrl;
    }

    /**
 * Returns the controller
 */
    public ComposerController getController() {
        return controller;
    }

    /**
 * Used for synchronization between view and model.
 *
 * @param        b        If true. view is synchronized with model.
 *                                 If false, model is synchronized with view,
 */
    public abstract void updateComponents(boolean b);

    // ********** Methods necessary to hide view from clients ********

    /**
 * Returns the GUI component which should be added to the parent,
 * i.e. to the Composer frame.
 *
 * @return        GUI component to be added to parent frame
 */
    public abstract JComponent getViewUIComponent();

    /**
 * Enables or disables the editor view.
 *
 * @param        enabled                Whether the view should be enabled
 */
    public abstract void setViewEnabled(boolean enabled);

    /**
 * Sets the text of the editor view
 *
 * @param        text        New text, which replaces the current view text
 */
    public abstract void setViewText(String text);

    /**
 * Gets the current text of the editor view
 *
 */
    public abstract String getViewText();

    /**
 * Sets the font of the editor view
 *
 * @param        f        Font to set in the view
 */
    public abstract void setViewFont(Font f);

    /**
 * Gets the font of the editor view
 */
    public abstract Font getViewFont();

    // ********************* FocusOwner methods **********************
    public abstract boolean isCutActionEnabled();

    public abstract boolean isCopyActionEnabled();

    public abstract boolean isPasteActionEnabled();

    public abstract boolean isDeleteActionEnabled();

    public abstract boolean isSelectAllActionEnabled();

    public abstract boolean isUndoActionEnabled();

    public abstract boolean isRedoActionEnabled();

    public abstract void cut();

    public abstract void copy();

    public abstract void paste();

    public abstract void delete();

    public abstract void undo();

    public abstract void redo();

    public abstract void selectAll();

    public abstract JComponent getComponent();
}
