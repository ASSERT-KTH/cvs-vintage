// Copyright (c) 1996-98 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation for educational, research and non-profit
// purposes, without fee, and without a written agreement is hereby granted,
// provided that the above copyright notice and this paragraph appear in all
// copies. Permission to incorporate this software into commercial products may
// be obtained by contacting the University of California. David F. Redmiles
// Department of Information and Computer Science (ICS) University of
// California Irvine, California 92697-3425 Phone: 714-824-3823. This software
// program and documentation are copyrighted by The Regents of the University
// of California. The software program and documentation are supplied "as is",
// without any accompanying services from The Regents. The Regents do not
// warrant that the operation of the program will be uninterrupted or
// error-free. The end-user understands that the program was developed for
// research purposes and is advised not to rely exclusively on the program for
// any reason. IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY
// PARTY FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES,
// INCLUDING LOST PROFITS, ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS
// DOCUMENTATION, EVEN IF THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY
// DISCLAIMS ANY WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE
// SOFTWARE PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
// ENHANCEMENTS, OR MODIFICATIONS.

// File: DefaultGraphNodeRenderer.java
// Classes: DefaultGraphNodeRenderer
// Original Author: jrobbins@ics.uci.edu
// $Id: DefaultGraphNodeRenderer.java,v 1.2 1998/02/12 02:27:55 jrobbins Exp $

package uci.graph;

import uci.gef.NetNode;
import uci.gef.FigNode;
import uci.gef.Layer;

/** An interface for FigNode factories. Similiar in concept to the
 *  Swing class TreeCellRenderer.
 *
 * @see uci.graph.demo.WordNodeRenderer */

public class DefaultGraphNodeRenderer {
  /** Return a Fig that can be used to represent the given node */
  public FigNode getFigNodeFor(GraphModel graph, Layer lay, Object node) {
    if (node instanceof NetNode)
      return ((NetNode)node).presentationFor(lay);
    return null;
  }

} /* end class DefaultGraphNodeRenderer */
