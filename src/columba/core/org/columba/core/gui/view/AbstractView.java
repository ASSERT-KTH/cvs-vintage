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

package org.columba.core.gui.view;

import org.columba.core.gui.frame.FrameMediator;

import javax.swing.JFrame;

/**
 *
 * This interface provides methods and static data
 * that are common to all views.
 *
 */
public interface AbstractView {
    /**
     * internally used toolbar ID
     */
    public static final String MAIN_TOOLBAR = "main";

    /**
     * Gets the controller of this view
     *
     * @return  the ViewController or null if none
     */
    FrameMediator getViewController();

    /**
     * Sets the ViewController for this AbstractView
     *
     * @param controller assign a controller that this AbstractView.
     */
    void setViewController(FrameMediator controller);

    /**
     * Gets the model object presented by this view.
     *
     * @return the model object of this view.
     */
//    Object getModel();

    /**
     * Sets the model object for this view.
     *
     * @param model a model object that this view will present to the user.
     */
//    void setModel(Object model);

    /**
     * Loads stored information about the previous size and
     * location of the view.
     */
    void loadPositions();

    /**
     * Saves information about the current size and location
     * of the view
     */
    void savePositions();
    
    /**
     * Provides access to the container displaying the View.
     * Container is needed (as JFrame) by methods in some classes
     * that create JDialogs.
     */
    public JFrame getFrame();

     /**
     * Specify parent continer of the view.
     */
    public void setFrame(JFrame frame);
}
