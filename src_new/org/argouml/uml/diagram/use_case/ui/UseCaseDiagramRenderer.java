// $Id: UseCaseDiagramRenderer.java,v 1.19 2005/01/22 22:32:52 bobtarling Exp $
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

package org.argouml.uml.diagram.use_case.ui;

import org.apache.log4j.Logger;
import org.argouml.model.Model;
import org.argouml.model.ModelFacade;
import org.argouml.uml.diagram.static_structure.ui.CommentEdge;
import org.argouml.uml.diagram.static_structure.ui.FigComment;
import org.argouml.uml.diagram.static_structure.ui.FigEdgeNote;
import org.argouml.uml.diagram.ui.FigAssociation;
import org.argouml.uml.diagram.ui.FigDependency;
import org.argouml.uml.diagram.ui.FigGeneralization;
import org.tigris.gef.base.Layer;
import org.tigris.gef.graph.GraphEdgeRenderer;
import org.tigris.gef.graph.GraphModel;
import org.tigris.gef.graph.GraphNodeRenderer;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.presentation.FigNode;

// could be singleton

/**
 * This class defines a renderer object for UML Use Case Diagrams. In a
 * Class Diagram the following UML objects are displayed with the
 * following Figs:<p>
 *
 * <pre>
 *   UML Object       ---  Fig
 *   ---------------------------------------
 *   MActor           ---  FigActor
 *   MUseCase         ---  FigUseCase
 * </pre>
 *
 * Provides {@link #getFigNodeFor} to implement the {@link
 * GraphNodeRenderer} interface and {@link #getFigEdgeFor} to implement the
 * {@link GraphEdgeRenderer} interface.<p>
 *
 * <em>Note</em>. Should be implemented as a singleton - we don't really
 * need a separate instance for each use case diagram.<p>
 *
 * @author abonner
 */
public class UseCaseDiagramRenderer
    implements GraphNodeRenderer, GraphEdgeRenderer {
    private static final Logger LOG =
        Logger.getLogger(UseCaseDiagramRenderer.class);


    /**
     * <p>Return a Fig that can be used to represent the given node.</p>
     *
     * @param gm    The graph model for which we are rendering.
     *
     * @param lay   The layer in the graph on which we want this figure.
     *
     * @param node  The node to be rendered (an NSUML object)
     *
     * @return      The fig to be used, or <code>null</code> if we can't create
     *              one.
     */

    public FigNode getFigNodeFor(GraphModel gm, Layer lay, Object node) {

        // Create a new version of the relevant fig

        if (org.argouml.model.ModelFacade.isAActor(node)) {
            return new FigActor(gm, node);
        }
        else if (org.argouml.model.ModelFacade.isAUseCase(node)) {
            return new FigUseCase(gm, node);
        }
        else if (ModelFacade.isAComment(node)) {
            return new FigComment(gm, node);
        }

        // If we get here we were asked for a fig we can't handle.

        LOG.debug(this.getClass().toString()
		  + ": getFigNodeFor(" + gm.toString() + ", "
		  + lay.toString() + ", " + node.toString()
		  + ") - cannot create this sort of node.");
        return null;
    }


    /**
     * <p>Return a Fig that can be used to represent the given edge.</p>
     *
     * <p>Generally the same code as for the ClassDiagram, since it's very
     *   related to it. Deal with each of the edge types in turn.</p>
     *
     * @param gm    The graph model for which we are rendering.
     *
     * @param lay   The layer in the graph on which we want this figure.
     *
     * @param edge  The edge to be rendered (an NSUML object)
     *
     * @return      The fig to be used, or <code>null</code> if we can't create
     *              one.
     *
     * @see org.tigris.gef.graph.GraphEdgeRenderer#getFigEdgeFor(
     * org.tigris.gef.graph.GraphModel, org.tigris.gef.base.Layer,
     * java.lang.Object)
     */
    public FigEdge getFigEdgeFor(GraphModel gm, Layer lay, Object edge) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("making figedge for " + edge);
        }
        
        if (edge == null) {
            throw new IllegalArgumentException("A model edge must be supplied");
        }
        
        FigEdge newEdge = null;
        
        if (org.argouml.model.ModelFacade.isAAssociation(edge)) {
            // If the edge is an association, we'll need a FigAssociation
            Object   asc         = /*(MAssociation)*/ edge;
            FigAssociation ascFig      = new FigAssociation(asc, lay);

            newEdge = ascFig;
        } else if (org.argouml.model.ModelFacade.isAGeneralization(edge)) {
            // Generalization needs a FigGeneralization
            Object   gen    = /*(MGeneralization)*/ edge;
            FigGeneralization genFig = new FigGeneralization(gen, lay);
            newEdge = genFig;
        } else if (org.argouml.model.ModelFacade.isAExtend(edge)) {
            // Extend relationship
            Object   ext    = /*(MExtend)*/ edge;
            FigExtend extFig = new FigExtend(ext);

            // The nodes at the two ends

            Object base      = ModelFacade.getBase(ext);
            Object extension = ModelFacade.getExtension(ext);

            // The figs for the two end nodes

            FigNode baseFN      = (FigNode) lay.presentationFor(base);
            FigNode extensionFN = (FigNode) lay.presentationFor(extension);

            // Link the new extend relationship in to the ends. Remember we
            // draw from the extension use case to the base use case.

            extFig.setSourcePortFig(extensionFN);
            extFig.setSourceFigNode(extensionFN);

            extFig.setDestPortFig(baseFN);
            extFig.setDestFigNode(baseFN);

            newEdge = extFig;
        } else if (org.argouml.model.ModelFacade.isAInclude(edge)) {
            // Include relationship is very like extend. Watch out for the NSUML
            // bug here.
            Object   inc    = /*(MInclude)*/ edge;
            FigInclude incFig = new FigInclude(inc);

            Object base     = ModelFacade.getBase(inc);
            Object addition = ModelFacade.getAddition(inc);

            // The figs for the two end nodes

            FigNode baseFN     = (FigNode) lay.presentationFor(base);
            FigNode additionFN = (FigNode) lay.presentationFor(addition);

            // Link the new include relationship in to the ends

            incFig.setSourcePortFig(baseFN);
            incFig.setSourceFigNode(baseFN);

            incFig.setDestPortFig(additionFN);
            incFig.setDestFigNode(additionFN);

            newEdge = incFig;
        } else if (org.argouml.model.ModelFacade.isADependency(edge)) {
            // Dependency needs a FigDependency
            Object   dep    = /*(MDependency)*/ edge;
            FigDependency depFig = new FigDependency(dep);

            // Where there is more than one supplier or client, take the first
            // element in each case. There really ought to be a check that
            // there are some here for safety.

            Object supplier = /*(MModelElement)*/
                 ((ModelFacade.getSuppliers(dep).toArray())[0]);
            Object client = /*(MModelElement)*/
                 ((ModelFacade.getClients(dep).toArray())[0]);

            // The figs for the two end nodes

            FigNode supplierFN = (FigNode) lay.presentationFor(supplier);
            FigNode clientFN   = (FigNode) lay.presentationFor(client);

            // Link the new dependency in to the ends

            depFig.setSourcePortFig(clientFN);
            depFig.setSourceFigNode(clientFN);

            depFig.setDestPortFig(supplierFN);
            depFig.setDestFigNode(supplierFN);

            newEdge = depFig;
        } else if (edge instanceof CommentEdge) {
            newEdge = new FigEdgeNote(edge, lay);
        }

        if (newEdge == null) {
            throw new IllegalArgumentException(
                    "Don't know how to create FigEdge for model type "
                    + edge.getClass().getName());
        } else {
            if (newEdge.getSourcePortFig() == null) {
                Object source;
                if (edge instanceof CommentEdge) {
                    source = ((CommentEdge) edge).getSource();
                } else {
                    source = Model.getUmlHelper().getSource(edge);
                }
                setSourcePort(newEdge, (FigNode) lay.presentationFor(source));
            }
            if (newEdge.getDestPortFig() == null) {
                Object dest;
                if (edge instanceof CommentEdge) {
                    dest = ((CommentEdge) edge).getDestination();
                } else {
                    dest = Model.getUmlHelper().getDestination(edge);
                }
                setDestPort(newEdge, (FigNode) lay.presentationFor(dest));
            }
            if (newEdge.getSourcePortFig() == null
                    || newEdge.getDestPortFig() == null) {
                throw new IllegalStateException("Edge of type "
                    + newEdge.getClass().getName()
                    + " created with no source or destination port");
            }
        }
        return newEdge;
    }
    
    private void setSourcePort(FigEdge edge, FigNode source) {
        edge.setSourcePortFig(source);
        edge.setSourceFigNode(source);
    }

    private void setDestPort(FigEdge edge, FigNode dest) {
        edge.setDestPortFig(dest);
        edge.setDestFigNode(dest);
    }

    static final long serialVersionUID = 2217410137377934879L;

} /* end class UseCaseDiagramRenderer */
