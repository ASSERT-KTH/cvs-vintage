// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

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
	
	public void setDoc( HeaderInterface header, String str, boolean html ) throws Exception
	{
		//switchViewer( html );
		
		if ( header != null ) hv.setHeader( header );
		
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
