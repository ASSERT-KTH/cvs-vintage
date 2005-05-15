// $Id: ModeChangeHeight.java,v 1.1 2005/05/15 09:56:44 bobtarling Exp $
// Copyright (c) 1996-2005 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.
package org.argouml.uml.diagram.sequence.ui;

import java.awt.Color;
import java.awt.Graphics;

import java.awt.event.MouseEvent;

import org.tigris.gef.base.Editor;
import org.tigris.gef.base.FigModifyingModeImpl;
import org.tigris.gef.base.Globals;

import org.argouml.i18n.Translator;

public class ModeChangeHeight extends FigModifyingModeImpl {

    public ModeChangeHeight()
    {
        _contractSet=false;
        _editor=Globals.curEditor();
        _rubberbandColor=Globals.getPrefs().getRubberbandColor();
    }

    public void mousePressed( MouseEvent me)
    {
        if ( me.isConsumed())
            return;
        _startY=me.getY();
        _startX=me.getX();
        start();
        me.consume();
    }

    public void mouseDragged( MouseEvent me)
    {
        if ( me.isConsumed())
            return;
        _currentY=me.getY();
        _editor.damageAll();
        me.consume();
    }

    public void mouseReleased( MouseEvent me)
    {
        if ( me.isConsumed())
            return;
        SequenceDiagramLayout layout=(SequenceDiagramLayout)Globals.curEditor().getLayerManager().getActiveLayer();
        int endY=me.getY();
        if ( isContract())
        {
            int startOffset=layout.getNodeIndex( _startY);
            int endOffset;
            if ( _startY>endY)
            {
                endOffset=startOffset;
                startOffset=layout.getNodeIndex( endY);
            }
            else
                endOffset=layout.getNodeIndex( endY);
            int diff=endOffset-startOffset;
            if ( diff>0)
            {
                layout.contractDiagram( startOffset, diff);
            }
        }
        else
        {
            int startOffset=layout.getNodeIndex( _startY);
            if ( startOffset>0 && endY<_startY)
                startOffset--;
            int diff=layout.getNodeIndex( endY)-startOffset;
            if ( diff<0)
                diff= -diff;
            if ( diff>0)
                layout.expandDiagram( startOffset, diff);
        }

        me.consume();
        done();
    }

    public void paint(Graphics g)
    {
        g.setColor( _rubberbandColor);
        g.drawLine( _startX, _startY, _startX, _currentY);
    }

    public String instructions()
    {
        if ( isContract())
        {
            return Translator.localize( "action.sequence-contract");
        }
        return Translator.localize( "action.sequence-expand");
    }

    private boolean isContract()
    {
        if ( ! _contractSet)
        {
            _contract=getArg("name").equals( "SequenceContract");
            _contractSet=true;
        }
        return _contract;
    }

    private boolean _contract;
    private boolean _contractSet;
    private int _startX, _startY, _currentY;
    private Editor _editor;
    private Color _rubberbandColor;
}