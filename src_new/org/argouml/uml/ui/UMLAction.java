// $Id: UMLAction.java,v 1.28 2004/09/18 12:42:52 mvw Exp $
// Copyright (c) 1996-2004 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.

package org.argouml.uml.ui;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.KeyStroke;

import org.apache.log4j.Logger;
import org.argouml.application.helpers.ResourceLoaderWrapper;
import org.argouml.i18n.Translator;
import org.argouml.kernel.Project;
import org.argouml.kernel.ProjectManager;
import org.argouml.ui.Actions;
import org.argouml.ui.ProjectBrowser;
import org.argouml.ui.StatusBar;
import org.tigris.gef.util.Localizer;

/**
 * The prototype of all actions within ArgoUML.
 *
 */
public class UMLAction extends AbstractAction {

    private static final Logger LOG = Logger.getLogger(UMLAction.class);

    /**
     * Constant for determining the icon.
     */
    public static final boolean HAS_ICON = true;
    
    /**
     * Constant for determining the icon.
     */
    public static final boolean NO_ICON = false;

    private String iconName;

    /**
     * The constructor.
     * 
     * @param name the (to be localized) description of the action
     */
    public UMLAction(String name) {
        this(name, true, HAS_ICON);
    }
    
    /**
     * The constructor.
     * 
     * @param name the (to be localized) description of the action
     * @param hasIcon true if an icon is to be shown
     */
    public UMLAction(String name, boolean hasIcon) {
        this(name, true, hasIcon);
    }

    /**
     * The constructor.
     * 
     * @param name the (to be localized) description of the action
     * @param global if this is a global action, then it has to be added 
     *               to the list of such actions in the class Actions
     * @param hasIcon true if an icon has to be shown
     */
    public UMLAction(String name, boolean global, boolean hasIcon) {
        super(Translator.localize(name));
        if (hasIcon) {
	    iconName = name;
        }
        putValue(
		 Action.SHORT_DESCRIPTION,
		 Translator.localize(name));
        if (global)
            Actions.addAction(this);
        // Jaap B. 17-6-2003 added next line to make sure every action
        // is in the right enable condition on creation.
        setEnabled(shouldBeEnabled());
    }

    /**
     * Sets one of this object's properties using the associated key. If the
     * value has changed, a <code>PropertyChangeEvent</code> is sent to
     * listeners.
     *
     * @param key a <code>String</code> containing the key.
     * @param value an <code>Object</code> value.
     */
    public void putValue(String key, Object value) {
	if (iconName != null && Action.SMALL_ICON.equals(key)) {
	    iconName = null;
	}

	super.putValue(key, value);
    }

    /**
     * Gets one of this object's properties using the associated key.
     *
     * @param key the name of the property.
     * @return the value of the property.
     * @see #putValue(String, Object)
     */
    public Object getValue(String key) {
        if (iconName != null && Action.SMALL_ICON.equals(key)) {
            Icon icon = ResourceLoaderWrapper
                .lookupIconResource(Translator.getImageBinding(iconName),
                        Translator.localize(iconName));
            
            if (icon != null) {
                putValue(Action.SMALL_ICON, icon);
            } else {
                LOG.debug("icon not found: " + iconName);
            }
            iconName = null;
        }
        return super.getValue(key);
    }

    /** 
     * Perform the work the action is supposed to do.
     * 
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        LOG.debug("pushed " + getValue(Action.NAME));
        StatusBar sb = ProjectBrowser.getInstance().getStatusBar();
        sb.doFakeProgress(stripJunk(getValue(Action.NAME).toString()), 100);
        Actions.updateAllEnabled();
    }

    /**
     * Call this function when the  project is changed and may need saving.
     */
    public void markNeedsSave() {
        Project p = ProjectManager.getManager().getCurrentProject();
        p.setNeedsSave(true);
    }

    /**
     * @param target the action to be enabled
     */
    public void updateEnabled(Object target) {
        setEnabled(shouldBeEnabled());
    }

    /**
     * Enable the action if it should be enabled.
     */
    public void updateEnabled() {
        boolean b = shouldBeEnabled();
        setEnabled(b);
    }

    /** 
     * Return true if this action should be available to the user. This
     * method should examine the ProjectBrowser that owns it. Subclass
     * implementations of this method should always call
     * super.shouldBeEnabled first.
     *
     * @return true if the action should be available.
     */
    public boolean shouldBeEnabled() {
        return true;
    }

    /**
     * @param s the string to be stripped from junk
     * @return the cleansed string
     */
    protected static String stripJunk(String s) {
        String res = "";
        int len = s.length();
        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (Character.isJavaIdentifierPart(c))
                res += c;
        }
        return res;
    }
    
    /**
     * This function returns a localized menu shortcut key
     * to the specified key.
     *
     * @deprecated in 0.15.1. Replace by getMnemonic and the new way of
     *             retrieving shortcuts.
     *
     * @param key the shortcut key
     * @return the keystroke
     */
    public static final KeyStroke getShortcut(String key) {
        return Localizer.getShortcut("CoreMenu", key);
    }

    /**
     * This function returns a localized string corresponding
     * to the specified key.
     *
     * @param key the given key
     * @return a localized string corresponding to the given key
     */
    public static final String getMnemonic(String key) {
        return Translator.localize(key);
    }

    /**
     * @see javax.swing.Action#isEnabled()
     */
    public boolean isEnabled() {
        if (!Actions.isGlobalAction(this)) {
            return shouldBeEnabled();
        }
        return super.isEnabled();
    }

} /* end class UMLAction */
