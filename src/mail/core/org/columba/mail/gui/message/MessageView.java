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
import java.awt.Dimension;
import java.awt.event.MouseListener;

import javax.swing.JPanel;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.JTextComponent;

import org.columba.core.gui.util.CScrollPane;
import org.columba.mail.message.HeaderInterface;

public class MessageView extends CScrollPane {

	public static final int VIEWER_HTML = 1;
	public static final int VIEWER_SIMPLE = 0;

	private DocumentViewerInterface[] list;
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
	
	public MessageView( MessageController c) {
		super();
		this.messageController = c;
		
		getViewport().setBackground(Color.white);
		
		panel = new MessagePanel();
		panel.setLayout( new BorderLayout() );
		
		setViewportView(panel);
		
		active = VIEWER_SIMPLE;
		
		//setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		hv = new HeaderViewer();
		bodyTextViewer = new BodyTextViewer();
		
		panel.add( hv, BorderLayout.NORTH );				
		panel.add( bodyTextViewer, BorderLayout.CENTER );
		panel.add( c.getMailFrameController().attachmentController.getView(), BorderLayout.SOUTH );
		
		
	}
	
	protected void adjustSize()
	{
		
		Dimension d = getPreferredSize();
		
		Dimension d2 = hv.getPreferredSize();
		d2.width = d.width;
		
		
		hv.setPreferredSize( d2 );
		
	}
	
	
	public void addHyperlinkListener( HyperlinkListener l )
	{
		hv.addHyperlinkListener(l);
		bodyTextViewer.addHyperlinkListener(l);
	}
	
	public void addMouseListener( MouseListener l )
	{
		hv.addMouseListener(l);
		bodyTextViewer.addMouseListener(l);
	}

	
	
	
	protected void switchViewer( boolean html )
	{
		if ( html )
		{
			if ( active == VIEWER_HTML )
			{
				// do nothing
			}
			else
			{
				active = VIEWER_HTML;
				panel.removeAll();
				panel.add( hv, BorderLayout.NORTH );				
				panel.add( (JTextComponent) list[active], BorderLayout.CENTER );
				panel.validate();
				
				adjustSize();
			}
		}
		else
		{
			if ( active == VIEWER_SIMPLE )
			{
				// do nothing
			}
			else
			{
				active = VIEWER_SIMPLE;
				panel.removeAll();
				panel.add( hv, BorderLayout.NORTH );				
				panel.add( (JTextComponent) list[active], BorderLayout.CENTER );
				panel.validate();
				adjustSize();
			}
		}
	}
	
	public void setDoc( HeaderInterface header, String str, boolean html, boolean hasAttachments ) throws Exception
	{
		//switchViewer( html );
		
		if ( header != null ) hv.setHeader( header, hasAttachments );
		
		//list[active].setDoc( str );
		bodyTextViewer.setBodyText(str, html);
		
	}
	
	

	public void setPopupListener(MouseListener l) {
		listener = l;

		for (int i = 0; i < list.length; i++) {
			JTextComponent c = (JTextComponent) list[i];
			if (c != null)
				c.addMouseListener(l);
		}
	}

}
