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

package org.columba.mail.gui.view;

import org.columba.core.gui.view.AbstractView;

import org.columba.mail.gui.composer.util.IdentityInfoPanel;

import javax.swing.JPanel;

/**
 *
 * This interface provides methods and static data
 * that are common to all mail views.
 *
 */
public interface AbstractComposerView extends AbstractView {
    public static final String ACCOUNTINFOPANEL = "accountinfopanel";
    
    public JPanel getEditorPanel();

    public void showToolbar();
    
    /**
     * Gets the AccountInfoPanel of this view
     *
     * @return  the IdentityInfoPanel or null if none
     */
    public IdentityInfoPanel getAccountInfoPanel();

    /**
     * Used to update the panel, that holds the editor viewer. This is
     * necessary e.g. if the ComposerModel is changed to hold another
     * message type (text / html), which the previous editor can not
     * handle. If so a new editor controller is created, and thereby
     * a new view.
     */
    public void setNewEditorView();
    
    /**
     * Shows the AccountInfoPanel of this view
     */
    public void showAccountInfoPanel();
    
    /* Methods that might be needed later...
    public void setToolBar(ToolBar toolBar);
    public boolean isAccountInfoPanelVisible();
    */
}
