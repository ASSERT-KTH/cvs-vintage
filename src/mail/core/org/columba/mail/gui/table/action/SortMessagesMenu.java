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
package org.columba.mail.gui.table.action;

import org.columba.core.action.IMenu;
import org.columba.core.config.DefaultItem;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.selection.SelectionChangedEvent;
import org.columba.core.gui.selection.SelectionListener;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.xml.XmlElement;

import org.columba.mail.folder.MessageFolder;
import org.columba.mail.folder.AbstractFolder;
import org.columba.mail.gui.frame.MailFrameMediator;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.gui.table.SortingStateObservable;
import org.columba.mail.gui.tree.selection.TreeSelectionChangedEvent;
import org.columba.mail.util.MailResourceLoader;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.JRadioButtonMenuItem;


public class SortMessagesMenu extends IMenu implements ActionListener, Observer,
    SelectionListener {
    private ButtonGroup columnGroup;
    private ButtonGroup orderGroup;
    private JRadioButtonMenuItem ascendingMenuItem;
    private JRadioButtonMenuItem descendingMenuItem;
    private Observable observable;
    private MessageFolder selectedFolder;

    public SortMessagesMenu(FrameMediator controller) {
        super(controller,
            MailResourceLoader.getString("menu", "mainframe", "menu_view_sort"));

        setIcon(ImageLoader.getSmallImageIcon("stock_sort-ascending-16.png"));

        ((MailFrameMediator) controller).registerTreeSelectionListener(this);

        // register as Observer
        TableViewOwner table = (TableViewOwner) getController();
        observable = table.getTableController().getTableModelSorter()
                          .getSortingStateObservable();
        observable.addObserver(this);

        //createSubMenu();
    }

    protected void createSubMenu() {
        removeAll();

        TableViewOwner table = (TableViewOwner) getController();

        XmlElement columns = ((MailFrameMediator) getController()).getFolderOptionsController()
                              .getConfigNode(selectedFolder, "ColumnOptions");

        Vector v = new Vector();

        for (int i = 0; i < columns.count(); i++) {
            XmlElement column = columns.getElement(i);

            String name = column.getAttribute("name");
            v.add(name);
        }

        Object[] items = new String[v.size()];
        items = v.toArray();

        columnGroup = new ButtonGroup();

        JRadioButtonMenuItem headerMenuItem;

        for (int i = 0; i < items.length; i++) {
            String item = (String) items[i];

            // all headerfields are lowercase in property file
            String i18n = MailResourceLoader.getString("header",
                    item.toLowerCase());

            headerMenuItem = new JRadioButtonMenuItem(i18n);
            headerMenuItem.setActionCommand(item);
            headerMenuItem.addActionListener(this);
            columnGroup.add(headerMenuItem);
            add(headerMenuItem);
        }

        addSeparator();

        orderGroup = new ButtonGroup();
        ascendingMenuItem = new JRadioButtonMenuItem(MailResourceLoader.getString("menu", "mainframe", "menu_view_sort_asc"));
        ascendingMenuItem.setActionCommand("Ascending");
        ascendingMenuItem.addActionListener(this);
        orderGroup.add(ascendingMenuItem);
        add(ascendingMenuItem);
        descendingMenuItem = new JRadioButtonMenuItem(MailResourceLoader.getString("menu", "mainframe", "menu_view_sort_desc"));
        descendingMenuItem.setActionCommand("Descending");
        descendingMenuItem.addActionListener(this);
        orderGroup.add(descendingMenuItem);
        add(descendingMenuItem);

        //update(observable, null);
    }

    /*
 * (non-Javadoc)
 *
 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 */
    public void actionPerformed(ActionEvent e) {
        String action = e.getActionCommand();

        TableViewOwner table = (TableViewOwner) getController();

        if (action.equals("Ascending")) {
            table.getTableController().getTableModelSorter().setSortingOrder(true);
            table.getTableController().getUpdateManager().update();
        } else if (action.equals("Descending")) {
            table.getTableController().getTableModelSorter().setSortingOrder(false);
            table.getTableController().getUpdateManager().update();
        } else {
            table.getTableController().getTableModelSorter().setSortingColumn(action);
            table.getTableController().getUpdateManager().update();
        }

        table.getTableController().getTableModelSorter()
             .getSortingStateObservable().notifyObservers();

        //update(observable, null);
    }

    /*
 * (non-Javadoc)
 *
 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
 */
    public void update(Observable observable, Object object) {
        String column = ((SortingStateObservable) observable).getColumn();
        boolean ascending = ((SortingStateObservable) observable).isOrder();

        updateState(column, ascending);
    }

    private void updateState(String column, boolean ascending) {
        if (columnGroup == null) {
            return;
        }

        Enumeration enum = columnGroup.getElements();

        while (enum.hasMoreElements()) {
            JRadioButtonMenuItem item = (JRadioButtonMenuItem) enum.nextElement();

            if (item.getActionCommand().equals(column)) {
                item.setSelected(true);

                break;
            }
        }

        if (ascending) {
            ascendingMenuItem.setSelected(true);
        } else {
            descendingMenuItem.setSelected(true);
        }
    }

    /**
    * @see org.columba.core.gui.util.SelectionListener#selectionChanged(org.columba.core.gui.util.SelectionChangedEvent)
    */
    public void selectionChanged(SelectionChangedEvent e) {
        AbstractFolder[] selection = ((TreeSelectionChangedEvent) e).getSelected();

        if (selection.length == 1) {
            if (!(selection[0] instanceof MessageFolder)) {
                return;
            }

            selectedFolder = (MessageFolder) selection[0];

            createSubMenu();

            XmlElement xmlElement = ((MailFrameMediator) getController()).getFolderOptionsController()
                                     .getConfigNode(selectedFolder,
                    "SortingOptions");

            DefaultItem item = new DefaultItem(xmlElement);

            //String column = xmlElement.getAttribute("column");
            //String s = threadedview.getAttribute("order");
            boolean order = item.getBoolean("order");
        }
    }
}
