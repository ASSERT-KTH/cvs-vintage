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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.message;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.columba.mail.gui.attachment.AttachmentView;
import org.columba.mail.gui.message.viewer.HeaderController;
import org.columba.mail.gui.message.viewer.MessageBodytextViewer;
import org.columba.mail.gui.message.viewer.SecurityInformationController;
import org.columba.mail.gui.message.viewer.SpamStatusController;

public class MessageView extends JScrollPane {

    public static final int VIEWER_HTML = 1;

    public static final int VIEWER_SIMPLE = 0;

    private MouseListener listener;

    private int active;

    private JPanel panel;

    //private BodyTextViewer bodyTextViewer;

    private MessageController messageController;

    public MessageView(MessageController controller) {
        super();
        this.messageController = controller;

        getViewport().setBackground(Color.white);

    }

    public void layoutComponents(HeaderController headerController,
            SpamStatusController spamStatusController,
            MessageBodytextViewer bodytextViewer,
            SecurityInformationController securityInformationController,
            AttachmentView attachmentView) {

        panel = new MessagePanel();

        panel.setLayout(new BorderLayout());

        setViewportView(panel);

        active = VIEWER_SIMPLE;

       
        JPanel top = new JPanel();
        top.setLayout(new BorderLayout());

        if ( spamStatusController.isVisible() ) 
            top.add(spamStatusController.getView(), BorderLayout.NORTH);
        
        if ( headerController.isVisible() )
            top.add(headerController.getView(), BorderLayout.CENTER);
        
        panel.add(top, BorderLayout.NORTH);

        panel.add(bodytextViewer, BorderLayout.CENTER);

        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout());

        if ( securityInformationController.isVisible() )
            bottom.add(securityInformationController.getView(), BorderLayout.NORTH);
        
        bottom.add(attachmentView, BorderLayout.CENTER);

        panel.add(bottom, BorderLayout.SOUTH);
    }

   
}
