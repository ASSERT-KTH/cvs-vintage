// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.core.gui.frame;

import org.columba.core.config.ViewItem;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginHandlerNotFoundException;
import org.columba.core.pluginhandler.FramePluginHandler;
import org.columba.core.shutdown.ShutdownManager;
import org.columba.core.xml.XmlElement;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * FrameModel manages all frames. It keeps a list of every controller.
 * 
 * Its also the place to create a new frame, or save and close all frames at
 * once.
 * 
 * Frame controllers are plugins.
 * 
 * @see FramePluginHandler
 * 
 * @author fdietz
 */
public class FrameModel {

    /** list of frame controllers */
    protected List activeFrameCtrls = new LinkedList();

    /** viewlist xml treenode */
    protected XmlElement viewList = MainInterface.config.get("options")
            .getElement("/options/gui/viewlist");

    /** Default view specifications to be used when opening a new view */
    protected XmlElement defaultViews = MainInterface.config.get("options")
            .getElement("/options/gui/defaultviews");

    /**
     * Constructor which initializes fields for view lists and creates the views
     * stored in the existing view list using createFrameController (used at
     * start-up to display the same views/windows as when last time Columba was
     * closed).
     */
    public FrameModel() {
        //this is executed on shutdown: store all open frames so that they
        //can be restored on the next start
        ShutdownManager.getShutdownManager().register(new Runnable() {

            public void run() {
                storeViews();
            }

        });
    }
    
    
    /**
     * Close all frames and re-open them again.
     * <p>
     * This is necessary when updating translations, adding
     * new plugins which extend the menu and probably also
     * look and feel changes.
     *
     */
    public void refresh() {
        storeViews();
        openStoredViews();
    }

    /**
     * Store all open frames so that they can be restored on next 
     * startup.
     * 
     */
    public void storeViews() {
        //used to temporarily store the values while the original
        //viewList gets modified by the close method
        List newViewList = new LinkedList();

        ViewItem v;

        //we cannot use an iterator here because the close method
        //manipulates the list
        while (activeFrameCtrls.size() > 0) {
            FrameMediator c = (FrameMediator) activeFrameCtrls.get(0);
            v = c.getViewItem();

            //store every open frame in our temporary list
            newViewList.add(v.getRoot());

            //close every open frame
            c.close();
        }

        //if not we haven't actually closed a frame, leave viewList as is
        if (newViewList.size() > 0) {
            //the close method manipulates the viewList so we have to
            //remove the existing element and fill in our temporarily
            //stored ones
            viewList.removeAllElements();

            for (Iterator it = newViewList.iterator(); it.hasNext();) {
                viewList.addElement((XmlElement) it.next());
            }
        }
    }

    /**
     * Opens all views stored in the configuration.
     */
    public void openStoredViews() {
        // load all frames from configuration file
        for (int i = 0; i < viewList.count(); i++) {
            // get element from view list
            XmlElement view = viewList.getElement(i);
            String id = view.getAttribute("id");

            // create frame controller for this view...
            FrameMediator c = createFrameController(id, new ViewItem(view));

            // ...and display it
            c.openView();
        }

        if (activeFrameCtrls.size() == 0) {
            openView("ThreePaneMail");
        }
    }

    /**
     * Returns an array of all open frames.
     */
    public FrameMediator[] getOpenFrames() {
        return (FrameMediator[]) activeFrameCtrls.toArray(new FrameMediator[0]);
    }

    /**
     * Create new frame controller.
     * <p>
     * 
     * FrameControllers are plugins.
     * 
     * @see FramePluginHandler
     * 
     * @param id
     *            controller ID
     * @param viewItem
     *            ViewItem containing frame properties
     * 
     * @return frame controller
     */
    public FrameMediator createFrameController(String id, ViewItem viewItem) {
        // get plugin handler for handling frames
        FramePluginHandler handler = null;

        try {
            handler = (FramePluginHandler) MainInterface.pluginManager
                    .getHandler("org.columba.core.frame");
        } catch (PluginHandlerNotFoundException ex) {
            NotifyDialog d = new NotifyDialog();
            d.showDialog(ex);
        }

        // get frame controller using the plugin handler found above
        Object[] args = { viewItem};
        FrameMediator frame = null;

        try {
            frame = (FrameMediator) handler.getPlugin(id, args);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // save reference to frame controller
        activeFrameCtrls.add(frame);

        return frame;
    }

    /**
     * Opens a view of a given type, i.e. with a specific id.
     * 
     * @param id
     *            id specifying view type, e.g. "ThreePaneMail" or "Addressbook"
     * @return Frame controller for the given view type
     */
    public FrameMediator openView(String id) {
        // look for default view settings (if not found, null is returned)
        ViewItem view = loadDefaultView(id);

        // Create a frame controller for this view
        // view = null => defaults specified by frame controller is used
        ////FrameMediator controller = createFrameController(id, null);
        FrameMediator controller = createFrameController(id, view);

        // Display the view and return reference
        controller.openView();

        return controller;
    }

    /**
     * Gets default view settings for a given view type
     * 
     * @param id
     *            id specifying view type
     * @return View settings
     */
    protected ViewItem loadDefaultView(String id) {
        // If defaultViews doesn't exist, create it (backward compatibility)
        if (defaultViews == null) {
            XmlElement gui = MainInterface.config.get("options").getElement(
                    "/options/gui");
            defaultViews = new XmlElement("defaultviews");
            gui.addElement(defaultViews);
        }

        // search through defaultViews to get settings for given id
        ViewItem view = null;

        for (int i = 0; i < defaultViews.count(); i++) {
            XmlElement child = defaultViews.getElement(i);
            String childId = child.getAttribute("id");

            if ((childId != null) && childId.equals(id)) {
                view = new ViewItem(child);

                break;
            }
        }

        return view;
    }

    /**
     * Saves default view settings for given view type. These will be used as
     * startup values next a view of this type is opened. Though, views opened
     * at startup will use settings from viewlist instead.
     * 
     * Only one set of settings are stored for each view id.
     * 
     * @param view
     *            view settings to be stored
     */
    protected void saveDefaultView(ViewItem view) {
        if (view == null) { return; // nothing to save
        }

        String id = view.get("id");

        // removed previous default values
        ViewItem oldView = loadDefaultView(id);

        if (oldView != null) {
            defaultViews.removeElement(oldView.getRoot());
        }

        // store current view settings
        defaultViews.addElement(view.getRoot());
    }

    /**
     * Called when a frame is closed. The reference is removed from the list of
     * active (shown) frames. If it's the last open view, the view settings are
     * stored in the view list.
     * 
     * @param c
     *            Reference to frame controller for the view which is closed
     */
    public void close(FrameMediator c) {
        // Check if the frame controller has been registered, else do nothing
        if (activeFrameCtrls.contains(c)) {
            ViewItem v = c.getViewItem();
            saveDefaultView(v);
            activeFrameCtrls.remove(c);

            if (activeFrameCtrls.size() == 0) {
                //this is the last frame so store its data in the viewList
                viewList.removeAllElements();
                viewList.addElement(v.getRoot());
            }
        }
    }
}