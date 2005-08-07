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
package org.columba.core.gui.toolbar;

import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Proxy;

import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

import org.columba.core.gui.action.AbstractSelectableAction;
import org.columba.core.gui.base.ButtonStateAdapter;
import org.columba.core.gui.base.ImageUtil;


/**
 * Customized JToogleButton for a Toolbar.
 * <p>
 * Adds an Observer to get notified when selection state changes
 * from action.
 * <p>
 * Focus is disabled for toolbar buttons. ToggleButton should use
 * small icons as default.
 * <p>
 *
 * @author fdietz
 */
public class ToggleToolbarButton extends JToggleButton {
    /**
     *
     */
    public ToggleToolbarButton() {
        super();
        setRequestFocusEnabled(false);
    }

    /**
     * @param icon
     */
    public ToggleToolbarButton(Icon icon) {
        super(icon);
        setRequestFocusEnabled(false);
    }

    /**
     * @param action
     */
    public ToggleToolbarButton(AbstractSelectableAction action) {
        super(action);
        setRequestFocusEnabled(false);
        setMargin(new Insets(1, 1, 1, 1));

        // no text!
        setText("");

        ImageIcon icon = (ImageIcon) action.getValue(Action.SMALL_ICON);

        if (icon != null) {
            setIcon(icon);

            // apply transparent icon
            setDisabledIcon(ImageUtil.createTransparentIcon((ImageIcon) icon));
        }

        getModel().addItemListener(new ItemListener() {
                public void itemStateChanged(ItemEvent e) {
                    AbstractSelectableAction a = (AbstractSelectableAction) getAction();

                    if (a != null) {
                        a.setState(e.getStateChange() == ItemEvent.SELECTED);
                    }
                }
            });
    }

    public boolean isFocusTraversable() {
        return isRequestFocusEnabled();
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
