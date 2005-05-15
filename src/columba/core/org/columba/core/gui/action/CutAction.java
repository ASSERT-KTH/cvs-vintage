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
package org.columba.core.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.focus.FocusManager;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.util.GlobalResourceLoader;


public class CutAction extends AbstractColumbaAction {
	
	private Action internalAction = new DefaultEditorKit.CutAction();
	
    public CutAction(FrameMediator controller) {
        super(controller,
            GlobalResourceLoader.getString(null, null, "menu_edit_cut"));

        // tooltip text
        putValue(SHORT_DESCRIPTION,
            GlobalResourceLoader.getString(null, null, "menu_edit_cut_tooltip")
                                .replaceAll("&", ""));

        // small icon for menu
        putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("stock_cut-16.png"));

        // large icon for toolbar
        putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_cut.png"));

        // disable toolbar text
        setShowToolBarText(false);

        
        // short cut key
        putValue(ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));
            
        
        setEnabled(false);
        FocusManager.getInstance().setCutAction(this);
    }

   
	
    
    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
    	//FocusManager.getInstance().cut();
    	
    	internalAction.actionPerformed(evt);
    }

    /* (non-Javadoc)
     * @see org.columba.core.action.AbstractColumbaAction#isSingleton()
     */
    public boolean isSingleton() {
        return true;
    }
}
