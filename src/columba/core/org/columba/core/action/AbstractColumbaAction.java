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
package org.columba.core.action;

import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.plugin.Plugin;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;


/**
 * AbstractColumbaAction extends the Swing Action API providing
 * more action properties. It maintains a reference to the action's
 * parent frame. Every action should subclass this class
 * implementing the actionPerformed(ActionEvent) method. Action
 * properties can be accessed using the getValue(String) and
 * putValue(String, Object) methods, just as it is handled in
 * Swing.
 *
 * @author fdietz
 */
public abstract class AbstractColumbaAction extends AbstractAction
    implements Plugin {
    /**
     * special label for toolbar buttons which is smaller
     * than the regular label
     *
     * Example: Reply to Sender -> Reply
     *
     */
    public static final String TOOLBAR_NAME = "ToolbarName";

    /**
     * The toolbar uses the large icon, whereas menuitems
     * use the small one.
     *
     */
    public static final String LARGE_ICON = "LargeIcon";

    /**
     * JavaHelp topic ID
     */
    public static final String TOPIC_ID = "TopicID";

    /**
     * show button text in toolbar
     */
    protected boolean showToolbarText = true;
    protected FrameMediator frameMediator;

    /**
     *
     * default constructor
     *
     * @param frameMediator                frame controller
     * @param name                                        i18n name
     *
     */
    public AbstractColumbaAction(FrameMediator frameMediator, String name) {
        super(name);
        this.frameMediator = frameMediator;
    }

    /**
     * Returns the frame controller
     *
     * @return FrameController
     */
    public FrameMediator getFrameMediator() {
        return frameMediator;
    }

    /**
     * Sets the frameMediator.
     *
     * @param frameMediator
     */
    public void setFrameMediator(FrameMediator frameController) {
        this.frameMediator = frameController;
    }

    /**
     * Return true if toolbar text should be visible
     *
     * @return boolean        true, if toolbar text should be enabled, false otherwise
     *
     */
    public boolean isShowToolBarText() {
        return showToolbarText;
    }

    /**
     * Sets whether the toolbar text should be visible or not.
     *
     * @param showToolbarText
     */
    public void setShowToolBarText(boolean showToolbarText) {
        if (this.showToolbarText != showToolbarText) {
            Boolean oldValue = this.showToolbarText ? Boolean.TRUE : Boolean.FALSE;
            this.showToolbarText = showToolbarText;
            firePropertyChange("showToolBarText", oldValue,
                showToolbarText ? Boolean.TRUE : Boolean.FALSE);
        }
    }
}
