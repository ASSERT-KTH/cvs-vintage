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

import java.awt.Component;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;

import org.columba.core.gui.util.CScrollPane;
import org.columba.mail.config.MailConfig;

public class MessageView extends CScrollPane
{
    public static final int MAX = 3;
    public static final int HTML = 2;
    public static final int ADVANCED = 1;
    public static final int SIMPLE = 0;

    private DocumentViewer[] list;
    private HtmlViewer debug;

    protected MouseListener listener;
    //protected MessageController messageViewer;
    protected int active;
    
   


    public MessageView()
    {
        super();

        //this.messageViewer = messageViewer;
        list = new DocumentViewer[MAX];
		enableWheelMouseSupport();
        initViewers();
    }

	
	
    protected void initViewer( int index )
    {
        switch( index )
        {
            case 0:
                 list[index] = new SimpleTextViewer();
                break;
            case 1:
                 list[index] = new HyperlinkTextViewer();
                break;
            case 2:
                 list[index] = new HtmlViewer();
                 debug = (HtmlViewer) list[index];
                break;
        }

        if ( listener != null )
           ( (JComponent) list[index]).addMouseListener( listener );

        //( (JComponent) list[index]).addFocusListener( messageViewer.getFocusListener() );
    }

    protected void initViewers()
    {


        boolean b = MailConfig.getMainFrameOptionsConfig().getWindowItem().getAdvancedViewer();



            if ( b==true )
            {

                initViewer( 1 );
                setViewportView( (JTextComponent) list[1] );
                active=1;
            }
            else
            {

                initViewer( 0 );
                setViewportView( (JTextComponent) list[0] );


                active=0;
            }


    }


	public void setHeader( Component header )
	{
		DocumentViewer viewer = (DocumentViewer) list[active];
       
        viewer.setHeader( header );
	}

    public boolean enableViewer( int index )
    {


        if ( active == index ) return false;

        DocumentViewer viewer = (DocumentViewer) list[index];
        if ( viewer == null )
        {
            initViewer( index );
            viewer = list[index];
        }


        viewer.clearDoc();

        JTextComponent v = (JTextComponent) list[index];
        setViewportView( v );

        active = index;


        return true;
    }



    public void setDoc( String str )
    {
	DocumentViewer viewer = (DocumentViewer) list[active];
        viewer.setDoc( str );



    }

    public String getSelectedText()
    {
    	DocumentViewer viewer = (DocumentViewer) list[active];
    	return viewer.getSelectedText();
    }

    public void setCaretPosition( int i )
    {
        JTextComponent viewer = (JTextComponent) list[active];
        viewer.setCaretPosition( i );
    }

    public DocumentViewer getViewer( int index )
    {
        DocumentViewer viewer = null;

        viewer = list[index];

        return viewer;
    }

    public DocumentViewer getActiveViewer()
    {
        DocumentViewer viewer = null;

        viewer = list[active];

        return viewer;
    }


    public boolean isAdvancedViewActive()
    {
        if ( active == ADVANCED ) return true;
           else return false;
    }

    public void setPopupListener( MouseListener l )
    {
        listener = l;

        for ( int i=0; i<list.length; i++)
        {
            JTextComponent c = (JTextComponent) list[i];
            if ( c != null )
               c.addMouseListener( l );
        }
    }





}


