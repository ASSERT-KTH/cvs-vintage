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
package org.columba.mail.gui.message;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.event.HyperlinkListener;

import org.columba.core.gui.util.CScrollPane;
import org.columba.mail.message.HeaderInterface;

public class MessageView extends CScrollPane {

	public static final int VIEWER_HTML = 1;
	public static final int VIEWER_SIMPLE = 0;

	//private HtmlViewer debug;

	protected MouseListener listener;
	//protected MessageController messageViewer;
	protected int active;

	//HyperlinkTextViewer viewer;
	//JList list;
	protected JPanel panel;

	protected HeaderViewer hv;
	protected BodyTextViewer bodyTextViewer;

	protected MessageController messageController;

	public MessageView(MessageController controller) {
		super();
		this.messageController = controller;

		getViewport().setBackground(Color.white);

		panel = new MessagePanel();
		panel.setLayout(new BorderLayout());
		
		setViewportView(panel);

		active = VIEWER_SIMPLE;
		
		hv = new HeaderViewer();
		panel.add(hv, BorderLayout.NORTH);
		
		bodyTextViewer = new BodyTextViewer();	
		panel.add(bodyTextViewer, BorderLayout.CENTER);
	
		panel.add( controller.getMailFrameController().attachmentController.getView(), BorderLayout.SOUTH);		
	}

	public void addHyperlinkListener(HyperlinkListener l) {
		hv.addHyperlinkListener(l);
		bodyTextViewer.addHyperlinkListener(l);
	}

	public void addMouseListener(MouseListener l) {
		hv.addMouseListener(l);
		bodyTextViewer.addMouseListener(l);
	}

	public void setDoc(
		HeaderInterface header,
		String str,
		boolean html,
		boolean hasAttachments)
		throws Exception {

		if (header != null)
			hv.setHeader(header, hasAttachments);

		bodyTextViewer.setBodyText(str, html);

	}

}
