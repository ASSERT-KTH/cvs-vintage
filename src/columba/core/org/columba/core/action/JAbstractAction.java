// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.


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






