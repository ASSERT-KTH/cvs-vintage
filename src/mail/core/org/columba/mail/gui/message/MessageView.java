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
import java.awt.Component;
import java.awt.event.MouseListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.text.JTextComponent;

import org.columba.mail.message.HeaderInterface;

public class MessageView extends JScrollPane {

	public static final int VIEWER_HTML = 1;
	public static final int VIEWER_SIMPLE = 0;

	private DocumentViewer[] list;
	//private HtmlViewer debug;

	protected MouseListener listener;
	//protected MessageController messageViewer;
	protected int active;
	
	//HyperlinkTextViewer viewer;
	//JList list;
	protected JPanel panel;
	
	protected HeaderViewer hv;
	
	public MessageView() {
		super();
		
		
		//viewer = new HyperlinkTextViewer();
		panel = new JPanel();
		panel.setLayout( new BorderLayout() );
		
		setViewportView(panel);
		
		active = VIEWER_SIMPLE;
		
		setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		//this.messageViewer = messageViewer;
		list = new DocumentViewer[2];
		initViewer( VIEWER_SIMPLE );
		initViewer( VIEWER_HTML );
		
		hv = new HeaderViewer();
		
		panel.add( hv, BorderLayout.NORTH );				
		panel.add( (JTextComponent) list[active], BorderLayout.CENTER );
		//enableWheelMouseSupport();
		//initViewers();
	}

	
	protected void initViewer(int index) {
		switch (index) {
			case 0 :
				list[index] = new HyperlinkTextViewer();
				break;
			case 1 :
				list[index] = new HtmlViewer();
				break;
		}

		/*
		if (listener != null)
			 ((JComponent) list[index]).addMouseListener(listener);
		*/
		//( (JComponent) list[index]).addFocusListener( messageViewer.getFocusListener() );
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
			}
		}
	}
	
	public void setDoc( HeaderInterface header, String str, boolean html )
	{
		switchViewer( html );
		
		if ( header != null ) hv.setHeader( header );
		
		list[active].setDoc( str );
		
	}
	
	
	/*
	protected void initViewers() {

		boolean b =
			MailConfig
				.getMainFrameOptionsConfig()
				.getWindowItem()
				.getAdvancedViewer();

		if (b == true) {

			initViewer(1);
			setViewportView((JTextComponent) list[1]);
			active = 1;
		} else {

			initViewer(0);
			setViewportView((JTextComponent) list[0]);

			active = 0;
		}

	}
	*/
	
	/*
	public void setHeader(Component header) {
		DocumentViewer viewer = (DocumentViewer) list[active];

		viewer.setHeader(header);
	}
	*/
	/*
	public boolean enableViewer(int index) {

		if (active == index)
			return false;

		DocumentViewer viewer = (DocumentViewer) list[index];
		if (viewer == null) {
			initViewer(index);
			viewer = list[index];
		}

		viewer.clearDoc();

		JTextComponent v = (JTextComponent) list[index];
		setViewportView(v);

		active = index;

		return true;
	}
	*/
	
	/*
	public void insertHtml( HtmlViewer v )
	{
		viewer.setText("Hello");
		
		viewer.setCaretPosition(4);
		//viewer.select(0,0);
		viewer.insertComponent( new JButton("test") );
	}
	
	public void setDoc(String str) {
		//viewer = (DocumentViewer) list[active];
		viewer.setDoc(str);

	}
	*/	
	
	/*
	public String getSelectedText() {
		DocumentViewer viewer = (DocumentViewer) list[active];
		return viewer.getSelectedText();
	}
	*/
	
	/*
	public void setCaretPosition(int i) {
		JTextComponent viewer = (JTextComponent) list[active];
		viewer.setCaretPosition(i);
	}
	*/
	
	/*
	public DocumentViewer getViewer(int index) {
		DocumentViewer viewer = null;

		viewer = list[index];

		return viewer;
	}
	*/
	
	/*
	public DocumentViewer getActiveViewer() {
		DocumentViewer viewer = null;

		viewer = list[active];

		return viewer;
	}

	public boolean isAdvancedViewActive() {
		if (active == ADVANCED)
			return true;
		else
			return false;
	}
	*/

	public void setPopupListener(MouseListener l) {
		listener = l;

		for (int i = 0; i < list.length; i++) {
			JTextComponent c = (JTextComponent) list[i];
			if (c != null)
				c.addMouseListener(l);
		}
	}

}
