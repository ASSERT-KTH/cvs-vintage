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

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Proxy;

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;

import org.columba.core.gui.base.ButtonStateAdapter;
import org.columba.core.gui.base.MnemonicSetter;


/**
 * Adds an Observer to JCheckBoxMenuItem in order to make it possible
 * to receive selection state changes from its underlying action.
 *
 * @author fdietz
 */

public class CCheckBoxMenuItem extends JCheckBoxMenuItem {
    /**
     * default constructor
     */
    public CCheckBoxMenuItem() {
        super();
    }

    /**
     * Creates a checkbox menu item with a given action attached.
     * <br>
     * If the name of the action contains &, the next character is used as
     * mnemonic. If not, the fall-back solution is to use default behaviour,
     * i.e. the mnemonic defined using setMnemonic on the action.
     *
     * @param action        The action to attach to the menu item
     */
    public CCheckBoxMenuItem(AbstractSelectableAction action) {
        super(action);

        // Set text, possibly with a mnemonic if defined using &
        MnemonicSetter.setTextWithMnemonic(this,
            (String) action.getValue(Action.NAME));
        getModel().addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    AbstractSelectableAction a = (AbstractSelectableAction) getAction();

                    if (a != null) {
                        a.setState(e.getStateChange() == ItemEvent.SELECTED);
                    }
                }
            });
    }

    /**
     * Overridden to react to state changes of the underlying action.
     */
    protected PropertyChangeListener createActionPropertyChangeListener(
        Action a) {
        return (PropertyChangeListener) Proxy.newProxyInstance(getClass()
                                                                   .getClassLoader(),
            new Class[] { PropertyChangeListener.class },
            new ButtonStateAdapter(this,
                super.createActionPropertyChangeListener(a)));
    }

    /**
     * Overridden to initialize selection state according to action
     */
    protected void configurePropertiesFromAction(Action a) {
        super.configurePropertiesFromAction(a);
        setSelected(((AbstractSelectableAction) a).getState());
    }
}
