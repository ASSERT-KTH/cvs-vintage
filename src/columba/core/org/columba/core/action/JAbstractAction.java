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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import javax.swing.event.EventListenerList;

public abstract class JAbstractAction extends AbstractAction
{


    private EventListenerList actionListeners;

    public static final String JLF_IMAGE_DIR = "org/columba/core/images/";


    public String TOOLBAR_NAME;

    public ImageIcon LARGE_ICON;



    public void setName( String s )
    {
        putValue( Action.NAME, s );
    }

    public String getName()
    {
        return (String) getValue( Action.NAME );
    }



    public String getActionCommand()  {
        return (String)getValue(Action.ACTION_COMMAND_KEY);
    }

    public KeyStroke getAcceleratorKey()  {
        return (KeyStroke) getValue(Action.ACCELERATOR_KEY);
    }


    public String getShortDescription()  {
        return (String)getValue(Action.SHORT_DESCRIPTION);
    }


    public String getLongDescription()  {
        return (String)getValue(Action.LONG_DESCRIPTION);
    }

    public ImageIcon getLargeIcon()
    {
	return LARGE_ICON;
    }

    public ImageIcon getSmallIcon()
    {
        return (ImageIcon) getValue( Action.SMALL_ICON );
    }

    public String getToolbarName()
    {
	return TOOLBAR_NAME;
    }

    // ActionListener registration and invocation.


    public void actionPerformed(ActionEvent evt)  {
        if (actionListeners != null) {
            Object[] listenerList = actionListeners.getListenerList();

            // Recreate the ActionEvent and stuff the value of the ACTION_COMMAND_KEY
            ActionEvent e = new ActionEvent(evt.getSource(), evt.getID(),
                                            (String)getValue(Action.ACTION_COMMAND_KEY));
            for (int i = 0; i <= listenerList.length-2; i += 2) {
                ((ActionListener)listenerList[i+1]).actionPerformed(e);
            }
        }
    }

    public void addActionListener(ActionListener l)  {
        if (actionListeners == null) {
            actionListeners = new EventListenerList();
	}
        actionListeners.add(ActionListener.class, l);
    }

    public void removeActionListener(ActionListener l)  {
	if (actionListeners == null) {
	    return;
	}
        actionListeners.remove(ActionListener.class, l);
    }


}






