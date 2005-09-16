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
package org.columba.core.gui.plugin;

import javax.swing.tree.DefaultMutableTreeNode;

import org.columba.core.plugin.PluginManager;


/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class PluginNode extends DefaultMutableTreeNode {
	
	private static final java.util.logging.Logger LOG = 
        java.util.logging.Logger.getLogger("org.columba.core.gui.plugin"); //$NON-NLS-1$
	
    String id;
    String version;
    String tooltip;
    boolean category;
    boolean enabled;

    /** Lazily created Boolean stating if the plugin has info or not, can be null. */
    Boolean hasInfo;

    public PluginNode() {
        category = false;
    }

    /**
 * @param arg0
 */
    public PluginNode(Object arg0) {
        super(arg0);

        category = false;
    }

    /**
 * @return
 */
    public String getId() {
        return id;
    }

    /**
 * @return
 */
    public boolean isEnabled() {
        return enabled;
    }

    /**
 * @param string
 */
    public void setId(String string) {
        id = string;
    }

    /**
 * @param b
 */
    public void setEnabled(boolean b) {
        enabled = b;
    }

    /**
 * @return
 */
    public String getTooltip() {
        return tooltip;
    }

    /**
 * @param string
 */
    public void setTooltip(String string) {
        tooltip = string;
    }

    /**
 * @return
 */
    public String getVersion() {
        return version;
    }

    /**
 * @param string
 */
    public void setVersion(String string) {
        version = string;
    }

    /**
 * @return
 */
    public boolean isCategory() {
        return category;
    }

    /**
 * @param b
 */
    public void setCategory(boolean b) {
        category = b;
    }

    /**
 * Returns true if the plugin has information about the plugin.
 * This attribute is created lazily, and may take a while since it
 * has to check for files on the file system. (Using the <code>PluginManager</code>.)
 * @return true if the plugin has info files; false if it doesnt have an info file.
 */
    public boolean hasInfo() {
        if (hasInfo == null) {
            hasInfo = Boolean.valueOf(PluginManager.getInstance().getInfoURL(id) != null);
        }

        return hasInfo.booleanValue();
    }

    public void debug() {
        LOG.info("id=" + id); //$NON-NLS-1$
        LOG.info("version=" + version); //$NON-NLS-1$
        LOG.info("enabled=" + enabled); //$NON-NLS-1$
        LOG.info("isCategory=" + category); //$NON-NLS-1$
        LOG.info("description=" + tooltip); //$NON-NLS-1$
        LOG.info("hasInfo=" + hasInfo()); //$NON-NLS-1$
    }
}
