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

package org.columba.mail.gui.menu;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.menu.CMenu;
import org.columba.core.gui.menu.CMenuItem;
import org.columba.core.gui.menu.Menu;
import org.columba.core.gui.menu.MenuBarGenerator;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.pluginhandler.MenuPluginHandler;

import org.columba.mail.main.MailInterface;

/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class MailMenu extends Menu {
    private CMenu fetchMessageSubmenu;
    private CMenu manageSubmenu;
    private CMenu sortSubMenu;

    /**
     * @param xmlRoot
     * @param frameMediator
     */
    public MailMenu(String xmlRoot, String extension,
        FrameMediator frameController) {
        super(xmlRoot, frameController);

        extendMenuFromFile(extension);

        try {
            ((MenuPluginHandler) MainInterface.pluginManager.getHandler(
                "org.columba.mail.menu")).insertPlugins(this);
        } catch (PluginHandlerNotFoundException ex) {
            throw new RuntimeException(ex);
        }
    }

    public MenuBarGenerator createMenuBarGeneratorInstance(String xmlRoot,
        FrameMediator frameController) {
        if (menuGenerator == null) {
            menuGenerator = new MailMenuBarGenerator(frameController, xmlRoot);
        }

        return menuGenerator;
    }

    public void updatePopServerMenu() {
        CMenuItem menuItem;

        fetchMessageSubmenu.removeAll();

        AbstractColumbaAction[] actions = MailInterface.mailCheckingManager.getActions();

        for (int i = 0; i < actions.length; i++) {
            fetchMessageSubmenu.add(new CMenuItem(actions[i]));
        }

        /*
        for (int i = 0; i < MailInterface.popServerCollection.count(); i++) {
        POP3ServerController c = MailInterface.popServerCollection.get(i);
        c.updateAction();
        fetchMessageSubmenu.add(new CMenuItem(c.getCheckAction()));
        }
        */
        /*
        manageSubmenu.removeAll();

        for (int i = 0; i < MailInterface.popServerCollection.count(); i++) {
        POP3ServerController c = MailInterface.popServerCollection.get(i);
        c.updateAction();
        menuItem = new CMenuItem(c.getManageAction());
        manageSubmenu.add(menuItem);
        }
        */
    }

    public void updateSortMenu() {
        //FIXME

        /*
        HeaderTableItem v =
                MailInterface.config.getMainFrameOptionsConfig().getHeaderTableItem();

        sortSubMenu.removeAll();

        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem menuItem;
        String c;

        for (int i = 0; i < v.count(); i++) {
                c = (String) v.getName(i);

                boolean enabled = v.getEnabled(i);

                if (enabled == true) {
                        String str = null;
                        try {
                                str =
                                        MailResourceLoader.getString("header", c.toLowerCase());
                        } catch (Exception ex) {
                                //ex.printStackTrace();
                                System.out.println("exeption: " + ex.getMessage());
                                str = c;
                        }

                        menuItem = new JRadioButtonMenuItem(str);
                        menuItem.setActionCommand(c);
                        menuItem.addActionListener(
                                MainInterface
                                        .headerTableViewer
                                        .getHeaderItemActionListener());
                        if (c
                                .equals(
                                        MainInterface
                                                .headerTableViewer
                                                .getTableModelSorter()
                                                .getSortingColumn()))
                                menuItem.setSelected(true);

                        //menuItem.addActionListener( new AbstractColumbaActionListener( mainInterface ));

                        sortSubMenu.add(menuItem);
                        group.add(menuItem);
                }

        }

        menuItem = new JRadioButtonMenuItem("In Order Received");
        menuItem.addActionListener(
                MainInterface.headerTableViewer.getHeaderItemActionListener());
        sortSubMenu.add(menuItem);
        group.add(menuItem);

        sortSubMenu.addSeparator();

        group = new ButtonGroup();

        menuItem = new JRadioButtonMenuItem("Ascending");
        menuItem.addActionListener(
                MainInterface.headerTableViewer.getHeaderItemActionListener());
        if (MainInterface
                .headerTableViewer
                .getTableModelSorter()
                .getSortingOrder()
                == true)
                menuItem.setSelected(true);

        sortSubMenu.add(menuItem);
        group.add(menuItem);
        menuItem = new JRadioButtonMenuItem("Descending");
        menuItem.addActionListener(
                MainInterface.headerTableViewer.getHeaderItemActionListener());
        if (MainInterface
                .headerTableViewer
                .getTableModelSorter()
                .getSortingOrder()
                == false)
                menuItem.setSelected(true);
        sortSubMenu.add(menuItem);
        group.add(menuItem);
        */
    }
}
