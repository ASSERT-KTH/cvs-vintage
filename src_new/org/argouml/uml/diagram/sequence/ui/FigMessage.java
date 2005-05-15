// $Id: FigMessage.java,v 1.1 2005/05/15 09:56:44 bobtarling Exp $
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

import java.awt.Point;

import java.beans.PropertyChangeEvent;

import java.util.Collection;
import java.util.Iterator;

import org.argouml.model.Model;
import org.argouml.uml.diagram.sequence.MessageNode;
import org.argouml.uml.diagram.ui.FigEdgeModelElement;
import org.argouml.uml.diagram.ui.FigTextGroup;

import org.tigris.gef.base.Globals;
import org.tigris.gef.base.PathConvPercent;

import org.tigris.gef.presentation.Fig;
import org.tigris.gef.presentation.FigPoly;

/**
 * The fig for a link in a sequence diagram.
 *
 * @author jaap.branderhorst@xs4all.nl
 */
public abstract class FigMessage
    extends FigEdgeModelElement {

    private FigTextGroup _textGroup;

    /**
     * Contructs a new figlink and sets the owner of the figlink.
     *
     * @param owner is the owner.
     */
    public FigMessage(Object owner) {
        super();
        _textGroup=new FigTextGroup();
        _textGroup.addFig( getNameFig());
        _textGroup.addFig( getStereotypeFig());
        addPathItem( _textGroup, new PathConvPercent( this, 50, 10));
        setOwner(owner);
    }

    /**
     * Constructor here for saving and loading purposes.
     *
     */
    public FigMessage() {
        this(null);
    }

    /**
     * Returns the action attached to this link if any.<p>
     *
     * @return the action attached to this link or null if there isn't any.
     */
    public Object getAction() {
        Object owner = getOwner();
        if (owner != null && Model.getFacade().isAMessage(owner)) {
            return Model.getFacade().getAction( owner);
        }
        return null;
    }

    public void setOwner( Object newOwner)
    {
        super.setOwner( newOwner);
        if ( Model.getFacade().isAMessage( newOwner))
        {
            Model.getPump().addModelEventListener( this, newOwner, "name");
        }
    }

    /**
     * @see org.tigris.gef.presentation.FigEdge#getSourcePortFig
     */
    public Fig getSourcePortFig()
    {
        Fig result=super.getSourcePortFig();
        if ( result instanceof FigClassifierRole.TempFig &&
             getOwner()!=null)
        {
            setSourcePortFig( result=getSrcFigClassifierRole().createFigMessagePort( getOwner(),
                result));
        }
        return result;
    }

    /**
     * @see org.tigris.gef.presentation.FigEdge#getDestPortFig
     */
    public Fig getDestPortFig()
    {
        Fig result=super.getDestPortFig();
        if ( result instanceof FigClassifierRole.TempFig &&
             getOwner()!=null)
        {
            setDestPortFig( result=getDestFigClassifierRole().createFigMessagePort( getOwner(),
                result));
        }
        return result;
    }

    /**
     * Computes the route of this {@link FigMessage} and computes the
     * connectionpoints of the figlink to the ports.  This depends on
     * the action attached to the owner of the {@link FigMessage}.  Also
     * adds FigActivations etc or moves the {@link FigClassifierRole}s
     * if necessary.<p>
     *
     * @see org.tigris.gef.presentation.FigEdge#computeRoute()
     */
    public void computeRoute() {
        Fig sourceFig=getSourcePortFig();
        Fig destFig=getDestPortFig();
        if ( sourceFig!=null && destFig!=null)
        {
            Point startPoint=sourceFig.connectionPoint( destFig.center());
            Point endPoint=destFig.connectionPoint( sourceFig.center());
            if ( sourceFig instanceof FigMessagePort &&
                 destFig instanceof FigMessagePort)
            {
                FigMessagePort srcMP=(FigMessagePort)sourceFig;
                FigMessagePort destMP=(FigMessagePort)destFig;
                // If it is a self-message
                if ( srcMP.getNode().getFigClassifierRole()==
                    destMP.getNode().getFigClassifierRole())
                {
                    if ( startPoint.x<sourceFig.center().x)
                        startPoint.x+=sourceFig.getWidth();
                    endPoint.x=startPoint.x;
                    setEndPoints( startPoint, endPoint);
                    // If this is the first time it is laid out, will only
                    // have 2 points, add the middle point
                    if ( getNumPoints()<=2)
                    {
                        insertPoint(0, startPoint.x +
                                    SequenceDiagramLayout.OBJECT_DISTANCE / 3,
                                    (startPoint.y + endPoint.y) / 2);
                    }
                    // Otherwise, move the middle point
                    else
                    {
                        int middleX=startPoint.x+SequenceDiagramLayout.OBJECT_DISTANCE/3;
                        int middleY=(startPoint.y+endPoint.y)/2;
                        Point p=getPoint( 1);
                        if ( p.x!=middleX || p.y!=middleY)
                        {
                           setPoint( new org.tigris.gef.presentation.Handle(1),middleX,middleY);
                        }
                    }
                }
                else
                    setEndPoints( startPoint, endPoint);
            }
            else
                setEndPoints( startPoint, endPoint);
            calcBounds();
            layoutEdge();
        }
    }

    public MessageNode getSrcMessagePort()
    {
        return ((FigMessagePort)getSourcePortFig()).getNode();
    }

    public MessageNode getDestMessagePort()
    {
        return ((FigMessagePort)getDestPortFig()).getNode();
    }

    /**
     * Returns the message belonging to this link if there is one
     * (otherwise null).<p>
     *
     * @return the message.
     */
    public Object getMessage() {
    	return getOwner();
    }

    /**
     * @see org.tigris.gef.presentation.FigEdgePoly#layoutEdge()
     */
    protected void layoutEdge() {
        if ( getSourcePortFig() instanceof FigMessagePort && getDestPortFig() instanceof FigMessagePort)
        {
            if ( ((FigMessagePort)getSourcePortFig()).getNode() !=null &&
                 ((FigMessagePort)getDestPortFig()).getNode() != null) {
                ( (SequenceDiagramLayout) getLayer()).updateActivations();
                Globals.curEditor().damageAll();
            }
        }
    }

    /**
     * @see org.argouml.uml.diagram.ui.FigEdgeModelElement#getSource()
     */
    protected Object getSource()
    {
        Object owner=getOwner();
        if ( owner==null)
            return null;
        return Model.getFacade().getSender( owner);
    }

    /**
     * @see org.argouml.uml.diagram.ui.FigEdgeModelElement#getDestination()
     */
    protected Object getDestination()
    {
        Object owner=getOwner();
        if ( owner==null)
            return null;

        return Model.getFacade().getReceiver( owner);
    }

    public void modelChanged( PropertyChangeEvent pce)
    {
        super.modelChanged( pce);
        _textGroup.calcBounds();
    }

    /**
     * @return the source figobject
     */
    public FigClassifierRole getSrcFigClassifierRole() {
        return (FigClassifierRole) getSourceFigNode();
    }

    /**
     * @return the destination fig object
     */
    public FigClassifierRole getDestFigClassifierRole() {
        return (FigClassifierRole) getDestFigNode();
    }

    /**
     * This won't work, so this implementation does nothing
     * @see org.argouml.uml.diagram.ui.FigEdgeModelElement#updateClassifiers
     */
    protected boolean updateClassifiers() {
        return true;
    }
}
