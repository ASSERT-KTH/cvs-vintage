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

package org.columba.core.gui.statusbar;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Timer;

import org.columba.core.util.SwingWorker;

class WorkerItem
{
    private SwingWorker worker;
    private int number;
    private String text;
    private int maximum;
    private int value;

    private Timer timer;

    private boolean allowed;

    private int priority;

    private final static int TWO_SECONDS = 2000;

    public WorkerItem( SwingWorker w, int i, int priority )
    {
    	worker = w;
        number = i;
	this.priority = priority;

        allowed = false;

        timer = new Timer( TWO_SECONDS,
                           new ActionListener()
                           {
                              public void actionPerformed( ActionEvent e )
                              {
                                  allowed = true;
                              }
                           }
                          );
    }


    public int getPriority()
    {
	return priority;
    }


    public boolean isAllowed()
    {
        return allowed;
    }


    public void setAllowed( boolean b )
    {
        allowed = b;
    }

    public void setWorker( SwingWorker w )
    {
    	worker = w;
    }

    public void setCancel( boolean b )
    {
		worker.setCancel( b );
    }

    public boolean getCancel()
    {
        return worker.getCancel();
    }

    public void setNumber( int i )
    {
        number = i;
    }

    public int getNumber()
    {
        return number;
    }

    public Thread getThread()
    {
        return worker.getThread();
    }

    public void setText( String s )
    {
        this.text = s;
    }


    public String getText()
    {
        return text;
    }

    public void setProgressBarMaximum( int m )
    {
        maximum = m;
    }

    public int getProgressBarMaximum()
    {
        return maximum;
    }

    public void setProgressBarValue( int v )
    {
        if ( v<=getProgressBarMaximum() )
           value = v;
    }

    public int getProgressBarValue()
    {
        return value;
    }


}
