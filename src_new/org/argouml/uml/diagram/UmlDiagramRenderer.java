// $Id: UmlDiagramRenderer.java,v 1.1 2005/01/24 23:20:31 bobtarling Exp $
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

package org.argouml.uml.diagram;

import java.util.Map;

import org.apache.log4j.Logger;

import org.tigris.gef.graph.GraphEdgeRenderer;
import org.tigris.gef.graph.GraphNodeRenderer;
import org.tigris.gef.presentation.FigEdge;
import org.tigris.gef.presentation.FigNode;

/**
 * <p>This defines a renderer object for UML Diagrams.
 *   The following UML objects are displayed with the
 *   following Figs:</p>
 *
 * <pre>
 *   UML Object       ---  Fig
 *   ---------------------------------------
 *   TODO
 * </pre>
 *
 * <p>Provides {@link #getFigNodeFor} to implement the {@link
 *   GraphNodeRenderer} interface and {@link #getFigEdgeFor} to implement the
 *   {@link GraphEdgeRenderer} interface.</p>
 */
public abstract class UmlDiagramRenderer
    implements GraphNodeRenderer, GraphEdgeRenderer {
    private static final Logger LOG =
	Logger.getLogger(UmlDiagramRenderer.class);

    /**
     * Return a Fig that can be used to represent the given node
     *
     * @see org.tigris.gef.graph.GraphNodeRenderer#getFigNodeFor(
     * org.tigris.gef.graph.GraphModel, org.tigris.gef.base.Layer,
     * java.lang.Object)
     */
    public FigNode getFigNodeFor(Object node, Map styleAttributes) {
        return null;
    }

    /**
     * Return a Fig that can be used to represent the given edge.
     *
     * @see org.tigris.gef.graph.GraphEdgeRenderer#getFigEdgeFor(
     * org.tigris.gef.graph.GraphModel,
     * org.tigris.gef.base.Layer, java.lang.Object)
     */
    public FigEdge getFigEdgeFor(Object edge, Map styleAttributes) {
        return null;
    }

} /* end class CollabDiagramRenderer */
