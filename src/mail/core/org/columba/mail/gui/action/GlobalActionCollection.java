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

package org.columba.mail.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.*;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.frame.action.FrameActionListener;
import org.columba.mail.util.MailResourceLoader;


public class GlobalActionCollection
{
    public BasicAction cutAction;
    public BasicAction copyAction;
    public BasicAction pasteAction;
    public BasicAction selectAllAction;
    public BasicAction deleteAction;
    public BasicAction newMessageAction;
    public BasicAction searchMessageAction;
    public BasicAction viewFilterToolbarAction;
    public BasicAction viewToolbarAction;
    public BasicAction viewFolderInfoAction;
    public BasicAction printSetupAction;
    public BasicAction addressbookAction;
    public BasicAction receiveSendAction;
    //public BasicAction useAdvancedViewerAction;

	protected MailFrameController frameController;

    public GlobalActionCollection ( MailFrameController frameController )
    {
		this.frameController = frameController;
		
        cutAction = new BasicAction(
				    MailResourceLoader.getString("action","mainframe","menu_edit_cut"),
				    MailResourceLoader.getString("action","mainframe","menu_edit_cut"),
				    "CUT_FOR_FUN",
                                    ImageLoader.getSmallImageIcon("stock_cut-16.png"),
                                    ImageLoader.getImageIcon("stock_cut.png"),
				    'T',
				    KeyStroke.getKeyStroke( KeyEvent.VK_X, ActionEvent.CTRL_MASK ),
				    false
				    );
	cutAction.setEnabled( false );
	



	copyAction = new BasicAction(
					MailResourceLoader.getString("menu","mainframe","menu_edit_copy"),
				    MailResourceLoader.getString("menu","mainframe","menu_edit_copy"),
				    "COPY_FOR_FUN",
                                    ImageLoader.getSmallImageIcon("stock_copy-16.png"),
                                    ImageLoader.getImageIcon("stock_copy.png"),
				    'C',
				    KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK ),
				    false
				    );
        copyAction.setEnabled( false );

	pasteAction = new BasicAction(
				    MailResourceLoader.getString("menu","mainframe","menu_edit_paste"),
				    MailResourceLoader.getString("menu","mainframe","menu_edit_paste"),
				    "PASTE",
                                    ImageLoader.getImageIcon("stock_paste-16.png"),
                                    ImageLoader.getImageIcon("stock_paste.png"),
				    'V',
				    KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK ),
				    false
				    );
        pasteAction.setEnabled( false );

        deleteAction = new BasicAction(
        			MailResourceLoader.getString("menu","mainframe","menu_edit_delete"),
				    MailResourceLoader.getString("menu","mainframe","menu_edit_delete"),
				    "DELETE",
                                    ImageLoader.getImageIcon("stock_delete-16.png"),
                                    ImageLoader.getImageIcon("stock_delete.png"),
                                    'D',
				    KeyStroke.getKeyStroke( KeyEvent.VK_DELETE,0 ),
				    false
				    );
        deleteAction.setEnabled( false );
        
        //MainInterface.focusManager.setActions( cutAction,copyAction,pasteAction,deleteAction);

        selectAllAction = new BasicAction(
        			MailResourceLoader.getString("menu","mainframe","menu_edit_selectall"),
				    MailResourceLoader.getString("menu","mainframe","menu_edit_selectall"),

				    "SELECTALL",
				    null,
				    null,
				    'A',
				    KeyStroke.getKeyStroke( KeyEvent.VK_A, ActionEvent.CTRL_MASK )
				    );
        selectAllAction.setEnabled( false );


	newMessageAction = new BasicAction(
					MailResourceLoader.getString("menu","mainframe","menu_message_new"),
				    MailResourceLoader.getString("menu","mainframe","menu_message_new_toolbar"),
				    MailResourceLoader.getString("menu","mainframe","menu_message_new_tooltip"),
				    "NEW_MESSAGE",
                                    ImageLoader.getSmallImageIcon("stock_edit-16.png"),
                                    ImageLoader.getImageIcon("stock_edit.png"),
				    'N',
				    KeyStroke.getKeyStroke( KeyEvent.VK_M, ActionEvent.CTRL_MASK )
				    );
        searchMessageAction = new BasicAction(
        			MailResourceLoader.getString("menu","mainframe","menu_edit_searchmessages"),
				    MailResourceLoader.getString("menu","mainframe","menu_edit_searchmessages"),

				    "SEARCH_MESSAGE",
                                    ImageLoader.getSmallImageIcon("virtualfolder.png"),
                                    ImageLoader.getImageIcon("virtualfolder.png"),
				    'S',
				    KeyStroke.getKeyStroke( KeyEvent.VK_S, ActionEvent.CTRL_MASK )
				    );

        viewFilterToolbarAction = new BasicAction(
        	MailResourceLoader.getString("menu","mainframe","menu_view_showfiltertoolbar"),
				    MailResourceLoader.getString("menu","mainframe","menu_view_showfiltertoolbar"),

            "SHOW_FILTERTOOLBAR",
            null,
            null,
            '1',
            null
            );

        viewToolbarAction = new BasicAction(
        	MailResourceLoader.getString("menu","mainframe","menu_view_showtoolbar"),
			MailResourceLoader.getString("menu","mainframe","menu_view_showtoolbar"),
            "SHOW_TOOLBAR",
            null,
            null,
            '1',
            null
            );

        viewFolderInfoAction = new BasicAction(
        	MailResourceLoader.getString("menu","mainframe","menu_view_showfolderinfo"),
			MailResourceLoader.getString("menu","mainframe","menu_view_showfolderinfo"),
            "SHOW_FOLDERINFO",
            null,
            null,
            '1',
            null
            );

        printSetupAction = new BasicAction(
        	MailResourceLoader.getString("menu","mainframe","menu_file_printsetup"),
			MailResourceLoader.getString("menu","mainframe","menu_file_printsetup"),

            "PRINT_SETUP",
            ImageLoader.getSmallImageIcon("stock_print-16.png"),
            ImageLoader.getImageIcon("stock_print.png"),
            '0',
            null
            );

        printSetupAction.setEnabled( false );

        addressbookAction = new BasicAction(
        	MailResourceLoader.getString("menu","mainframe","menu_preferences_addressbook"),
			MailResourceLoader.getString("menu","mainframe","menu_preferences_addressbook"),
            "ADDRESSBOOK",
            ImageLoader.getSmallImageIcon("stock_book-16.png"),
            ImageLoader.getImageIcon("stock_book.png"),
            '0',
            null
            );


        receiveSendAction = new BasicAction(
        	MailResourceLoader.getString("menu","mainframe","menu_file_receivesend"),
        	MailResourceLoader.getString("menu","mainframe","menu_file_receivesend_toolbar"),
        	MailResourceLoader.getString("menu","mainframe","menu_file_receivesend_tooltip"),
            "RECEIVESEND",
            ImageLoader.getSmallImageIcon("send-receive.png"),
            ImageLoader.getImageIcon("send-24-receive.png"),
            '0',
            KeyStroke.getKeyStroke( KeyEvent.VK_F9, 0 )
            );



    }


    public void addActionListeners()
    {
    	
        cutAction.addActionListener( frameController.tableController.getActionListener() );

        copyAction.addActionListener( frameController.tableController.getActionListener() );
        //copyAction.addActionListener( MainInterface.messageViewer.getActionListener() );

        pasteAction.addActionListener( frameController.tableController.getActionListener() );

        deleteAction.addActionListener( frameController.tableController.getActionListener() );

        selectAllAction.addActionListener( frameController.tableController.getActionListener() );
        //selectAllAction.addActionListener( MainInterface.messageViewer.getActionListener() );

        searchMessageAction.addActionListener( new FrameActionListener(frameController) );

        viewFilterToolbarAction.addActionListener( new FrameActionListener(frameController) );
        viewToolbarAction.addActionListener( new FrameActionListener(frameController) );
        viewFolderInfoAction.addActionListener( new FrameActionListener(frameController) );

        printSetupAction.addActionListener( new FrameActionListener(frameController) );

        addressbookAction.addActionListener( new FrameActionListener(frameController) );

        receiveSendAction.addActionListener( new FrameActionListener(frameController) );


		newMessageAction.addActionListener( new FrameActionListener(frameController) );
		// FIXME
		/*
        cancelAction.addActionListener( frameController.taskManager );
		*/
		
        //useAdvancedViewerAction.addActionListener( new FrameActionListener() );
         
    }


}













