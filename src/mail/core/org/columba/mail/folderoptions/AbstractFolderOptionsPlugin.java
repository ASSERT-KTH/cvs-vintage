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
package org.columba.mail.folderoptions;

import org.columba.core.plugin.PluginInterface;
import org.columba.core.xml.XmlElement;
import org.columba.mail.config.FolderItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.MailFrameMediator;

/**
 * @author fdietz
 */
public abstract class AbstractFolderOptionsPlugin implements PluginInterface {
    private MailFrameMediator mediator;
    private String name;

    public AbstractFolderOptionsPlugin(
        String name,
        MailFrameMediator mediator) {
        this.name = name;
        this.mediator = mediator;
    }

    /**
    * Get xml configuration of this plugin.
    * <p>
    *
    * Following a simple example of a toolbar configuration:<br>
    *
    * <pre>
    * <toolbar enabled="true" show_icon="true" show_text="false">
    *  <button name="Cut"/>
    *  <button name="Copy"/>
    *  <button name="Paste"/>
    *  <button name="Delete"/>
    * </toolbar>
    * </pre>
    *
    * <p>
    * So, this method will return the the top-level xml element
    * <b>toolbar</b>.
    *
    * @return      top-level xml treenode
    */
    public abstract void saveOptionsToXml(Folder folder);

    /**
     * Load options of this plugin from xml element.
     * <p>
     * Following the example used above, this element should
     * have the name <b>toolbar</b>.
     * <p>
     * @param element       configuration options node
     */
    public abstract void loadOptionsFromXml(Folder folder);

    /**
     * Get frame mediator
     *
     * @return      frame mediator
     */
    public MailFrameMediator getMediator() {
        return mediator;
    }

    /**
     * Get configuration node.
     * <p>
     * Determine if this should be applied globally or
     * on a per-folder basis.
     * <p>
     * This way, plugins don't have to know, if they work
     * on global or local options.
     * 
     * @param folder        currently selected folder
     * @return              xml node
     */
    public XmlElement getConfigNode(Folder folder) {
        XmlElement parent = null;
        boolean global = false;

        if (folder == null) {
            parent = FolderItem.getGlobalOptions();
            global = true;
        } else {
            parent = folder.getFolderItem().getFolderOptions();
            global = false;
        }

        XmlElement child = parent.getElement(getName());

        if (child == null) {
            child = createDefaultElement(global);
            parent.addElement(child);
        }

        if (global) {
            return child;
        }

        if (child.getAttribute("overwrite").equals("true")) {
            // use folder-based options
            return child;
        } else {
            // use global options
            parent = FolderItem.getGlobalOptions();
            child = parent.getElement(getName());
            if (child == null) {
                child = createDefaultElement(true);
                parent.addElement(child);
            }

            return child;
        }
    }

    /**
     * Create default node.
     * 
     * @return      xml node
     */
    public XmlElement createDefaultElement(boolean global) {
        XmlElement parent = new XmlElement(getName());

        // only local options have overwrite attribute
        if (!global)
            parent.addAttribute("overwrite", "false");

        return parent;
    }

    /**
     * Get name of configuration
     * 
     * @return     config name
     */
    public String getName() {
        return name;
    }
}
