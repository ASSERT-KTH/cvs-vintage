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
package org.columba.core.gui.util;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.swing.AbstractButton;


/**
 * This class implements a proxy behavior for a PropertyChangeListener registered
 * on an action by an AbstractButton instance. Every selectable action peer should
 * put this proxy in between its underlying action and the PropertyChangeListener
 * it registeres on the action. This guarantees that action state changes will be
 * propagated to the peer which will then be updated accordingly.
 */
public class ButtonStateAdapter implements InvocationHandler {
    protected AbstractButton button;
    protected PropertyChangeListener listener;

    public ButtonStateAdapter(AbstractButton button,
        PropertyChangeListener listener) {
        this.button = button;
        this.listener = listener;
    }

    public Object invoke(Object proxy, Method method, Object[] args)
        throws Throwable {
        if (method.getName().equals("propertyChange") && (args.length > 0)) {
            PropertyChangeEvent e = (PropertyChangeEvent) args[0];

            if ("selected".equals(e.getPropertyName())) {
                button.setSelected(((Boolean) e.getNewValue()).booleanValue());
            }
        }

        return method.invoke(listener, args);
    }
}
