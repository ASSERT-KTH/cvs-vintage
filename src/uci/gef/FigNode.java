// Copyright (c) 1995, 1996 Regents of the University of California.
// All rights reserved.
//
// This software was developed by the Arcadia project
// at the University of California, Irvine.
//
// Redistribution and use in source and binary forms are permitted
// provided that the above copyright notice and this paragraph are
// duplicated in all such forms and that any documentation,
// advertising materials, and other materials related to such
// distribution and use acknowledge that the software was developed
// by the University of California, Irvine.  The name of the
// University may not be used to endorse or promote products derived
// from this software without specific prior written permission.
// THIS SOFTWARE IS PROVIDED ``AS IS'' AND WITHOUT ANY EXPRESS OR
// IMPLIED WARRANTIES, INCLUDING, WITHOUT LIMITATION, THE IMPLIED
// WARRANTIES OF MERCHANTIBILITY AND FITNESS FOR A PARTICULAR PURPOSE.

// File: FigNode.java
// Classes: FigNode
// Original Author: ics125b spring 1996
// $Id: FigNode.java,v 1.5 1998/03/27 00:33:33 jrobbins Exp $

package uci.gef;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.beans.*;

import uci.util.*;
import uci.ui.*;
import uci.graph.*;

/** Class to display graphics for a NetNode in a diagram.
 *  <A HREF="../features.html#graph_visualization_nodes">
 *  <TT>FEATURE: graph_visualization_nodes</TT></A>
 *  <A HREF="../features.html#graph_visualization_ports">
 *  <TT>FEATURE: graph_visualization_ports</TT></A>
 */

public class FigNode extends FigGroup
implements MouseListener, PropertyChangeListener {
  ////////////////////////////////////////////////////////////////
  // constants
  
  /** Constants useful for determining what side (north, south, east,
   * or west) a port is located on. Maybe this should really be in
   * FigNode. */
  public static final double ang45 = Math.PI / 4;
  public static final double ang135 = 3*Math.PI / 4;
  public static final double ang225 = 5*Math.PI / 4;
  public static final double ang315 = 7*Math.PI / 4;

  ////////////////////////////////////////////////////////////////
  // instance variables

  /** True if you want ports to show when the mouse moves in and
   *  be invisible otherwise. */
  protected boolean _blinkPorts = false;

  /** True when we want to draw the user's attention to this FigNode. */
  protected boolean _highlight = false;

  protected Vector _figEdges = new Vector(); 
  
  ////////////////////////////////////////////////////////////////
  // constructors

  /** Constructs a new FigNode on the given node with the
   *  given Fig's. */
  public FigNode(Object node) {
    setOwner(node);
    if (node instanceof GraphNodeHooks)
      ((GraphNodeHooks)node).addPropertyChangeListener(this);
  }

  public FigNode(Object node, Vector figs) {
    this(node);
    setFigs(figs);
  }

  ////////////////////////////////////////////////////////////////
  // accessors

  /** Set the property of highlighting ports when the user moves the
   *  mouse over this FigNode.
   *  <A HREF="../features.html#graph_visualization_ports">
   *  <TT>FEATURE: graph_visualization_ports</TT></A>
   */
  public void setBlinkPorts(boolean b) {
    _blinkPorts = b;
    hidePorts();
  }
  public boolean getBlinkPorts() { return _blinkPorts; }


  public void addFigEdge(FigEdge fe) { _figEdges.addElement(fe); }
  public void removeFigEdge(FigEdge fe) { _figEdges.removeElement(fe); }

  /** Reply a collection of FigEdge's for all the edges that
   *  are connected to ports of the node being
   *  displayed. Needs-More-Work: this code is really slow. */

//   protected Vector figEdges() {
//     Vector figEdges = new Vector();
//     if (!(_layer instanceof LayerPerspective)) return figEdges;
//     GraphModel gm = ((LayerPerspective)_layer).getGraphModel();
//     Vector edges = new Vector();
//     Enumeration figEnum =  elements();
//     while (figEnum.hasMoreElements()) {
//       Fig f = (Fig) figEnum.nextElement();
//       Object port = f.getOwner();
//       if (port == null) continue;
//       Vector ins = gm.getInEdges(port);
//       Enumeration inEnum = ins.elements();
//       while (inEnum.hasMoreElements()) edges.addElement(inEnum.nextElement());

//       Vector outs = gm.getInEdges(port);
//       Enumeration outEnum = outs.elements();
//       while (outEnum.hasMoreElements()) edges.addElement(outEnum.nextElement());
//     }
//     figEnum = _layer.elements();
//     while (figEnum.hasMoreElements()) {
//       Fig f = (Fig) figEnum.nextElement();
//       Object owner = f.getOwner();
//       if (owner != null && edges.contains(owner)) {
// 	figEdges.addElement(f);
//       }
//     }
//     return figEdges;
//   }

  public void setOwner(Object own) {
    Object oldOwner = getOwner();
    if (oldOwner != null && oldOwner instanceof GraphNodeHooks) {
      ((GraphNodeHooks)oldOwner).removePropertyChangeListener(this);
    }
    if (own instanceof GraphNodeHooks) {
      ((GraphNodeHooks)own).addPropertyChangeListener(this);
    }
    super.setOwner(own);
  }

  ////////////////////////////////////////////////////////////////
  // Editor API

  /** When a FigNode is damaged, all of its arcs may need repainting. */
  public void startTrans() {
    Enumeration arcPers = _figEdges.elements();
    while (arcPers.hasMoreElements()) {
      Fig f = (Fig) arcPers.nextElement();
      f.startTrans();
    }
    super.startTrans();
  }

  public void endTrans() {
    Enumeration arcPers = _figEdges.elements();
    while (arcPers.hasMoreElements()) {
      Fig f = (Fig) arcPers.nextElement();
      f.endTrans();
    }
    super.endTrans();
  }

  public void delete() {
    Enumeration arcPers = _figEdges.elements();
    while (arcPers.hasMoreElements()) {
      Fig f = (Fig) arcPers.nextElement();
      f.delete();
    }
    super.delete();
  }


  ////////////////////////////////////////////////////////////////
  // ports

  /** Adds a port into the current FigNode.
   *  <A HREF="../features.html#graph_visualization_ports">
   *  <TT>FEATURE: graph_visualization_ports</TT></A>
   */
  public void bindPort(Object port, Fig f) {
    Fig oldPortFig = getPortFig(port);
    if (oldPortFig != null) oldPortFig.setOwner(null); //?
    f.setOwner(port);
    //if (!_ports.contains(port)) _ports.addElement(port);
  }

  /** Removes a port from the current FigNode.
   *  <A HREF="../features.html#graph_visualization_ports">
   *  <TT>FEATURE: graph_visualization_ports</TT></A>
   */
  public void removePort(Fig rep) {
    if (rep.getOwner() != null) {
      //_ports.removeElement(rep.getOwner());
      rep.setOwner(null);
    }
  }

  /** Reply the NetPort associated with the Fig under the mouse, or
   *  null if there is none.
   *  <A HREF="../features.html#graph_visualization_ports">
   *  <TT>FEATURE: graph_visualization_ports</TT></A>
   */
  public final Object hitPort(Point p) { return hitPort(p.x, p.y); }

  /** Reply the port that "owns" the Fig under the given point, or
   *  null if none.
   *  <A HREF="../features.html#graph_visualization_ports">
   *  <TT>FEATURE: graph_visualization_ports</TT></A>
   */
  public Object hitPort(int x, int y) {
    Fig f = hitFig(new Rectangle(x, y, 1, 1)); //?
    if (f != null) {
      Object own = f.getOwner();
      return own;
    }
    return null;
  }


  public Object deepHitPort(int x, int y) {
    Enumeration figs = elements();
    while (figs.hasMoreElements()) {
      Fig f = (Fig) figs.nextElement();
      Object own = f.getOwner();
      // assumes ports are always filled
      if (f.contains(x, y) && own != null) return own;
    }
    return null;
  }
  
  /** Reply the Fig that displays the given NetPort.
   *  <A HREF="../features.html#graph_visualization_ports">
   *  <TT>FEATURE: graph_visualization_ports</TT></A>
   */
  public Fig getPortFig(Object np) {
    Enumeration figs = elements();
    while (figs.hasMoreElements()) {
      Fig f = (Fig) figs.nextElement();
      if (f.getOwner() == np) return f;
    }
    return null;
  }

  /** Reply a list of Fig's that have a NetPort as their owner */
  public Vector getPortFigs() {
    Vector res = new Vector();
    Enumeration figs = elements();
    while (figs.hasMoreElements()) {
      Fig f = (Fig) figs.nextElement();
      if (f.getOwner() instanceof NetPort) res.addElement(f);
    }
    return res;
  }


  ////////////////////////////////////////////////////////////////
  // diagram-level operations

  /** Reply the port's sector within the current view.  This version
   *  works precisely with square FigNodes the angxx constants
   *  should be removed and calculated by the port if non-square
   *  FigNodes will be used.
   *
   *  <pre>Sectors
   *		      \  1   /
   *		       \    /
   *		        \  /
   *		     2   \/   -2
   *			 /\
   *		        /  \
   *		       /    \
   *		      /  -1  \ </pre>
   **/

  public int getPortSector(Fig portFig) {
    Rectangle nodeBBox = getBounds();
    Rectangle portBBox = portFig.getBounds();
    int nbbCenterX = nodeBBox.x + nodeBBox.width / 2;
    int nbbCenterY = nodeBBox.y + nodeBBox.height / 2;
    int pbbCenterX = portBBox.x + portBBox.width / 2;
    int pbbCenterY = portBBox.y + portBBox.height / 2;

    if (portFig != null) {
      int dx = (pbbCenterX - nbbCenterX) * nodeBBox.height;
      int dy = (pbbCenterY - nbbCenterY) * nodeBBox.width;
      double dist = Math.sqrt(dx * dx + dy * dy);
      double ang;
      if (dy > 0) ang = Math.acos(dx / dist);
      else ang = Math.acos(dx / dist) + Math.PI;

      if (ang < ang45) return 2;
      else if (ang < ang135) return 1;
      else if (ang < ang225) return -2;
      else if (ang < ang315) return -1;
      else return 2;
    }
    return -1;
  }

  
  ////////////////////////////////////////////////////////////////
  // painting methods

  /** Paints the FigNode to the given Graphics. */
  public void paint(Graphics g) {
    super.paint(g);
    if (_highlight) {
      g.setColor(Globals.getPrefs().getHighlightColor()); /* needs-more-work */
      g.drawRect(_x - 3, _y - 3, _w + 6 - 1, _h + 6 - 1);
      g.drawRect(_x - 2, _y - 2, _w + 4 - 1, _h + 4 - 1);
    }
  }

  ////////////////////////////////////////////////////////////////
  // notifications and updates

  public void propertyChange(PropertyChangeEvent pce) {
    System.out.println("FigNode got a PropertyChangeEvent");
    String pName = pce.getPropertyName();
    Object src = pce.getSource();
    if (pName.equals("Dispose") && src == getOwner()) { delete(); }
    if (pName.equals("Highlight") && src == getOwner()) {
      _highlight = ((Boolean)pce.getNewValue()).booleanValue();
      damage();
    }
  }

  
  /**  <A HREF="../features.html#graph_visualization_ports">
   *  <TT>FEATURE: graph_visualization_ports</TT></A>
   */
  public void showPorts() {
    startTrans();
    //    Color c = Globals.getPrefs().highlightColor();
    Enumeration figs = elements();
    while (figs.hasMoreElements()) {
      Fig f = (Fig) figs.nextElement();
      if (f.getOwner() != null) {
	f.setLineWidth(1);
	f.setFilled(true);
      }
    }
    endTrans();
  }

  /**  <A HREF="../features.html#graph_visualization_ports">
   *  <TT>FEATURE: graph_visualization_ports</TT></A>
   */
  public void hidePorts() {
    startTrans();
    Enumeration figs = elements();
    while (figs.hasMoreElements()) {
      Fig f = (Fig) figs.nextElement();
      if (f.getOwner() != null) {
	f.setLineWidth(0);
	f.setFilled(false);
      }
    }
    endTrans();
  }

  ////////////////////////////////////////////////////////////////
  // event handlers

  /** If the mouse enters this FigNode's bbox and the
   *  _blinkPorts flag is set, then display ports. */
  public void mouseEntered(MouseEvent me) {
    if (_blinkPorts) showPorts();
  }

  /** If the mouse exits this FigNode's bbox and the
   *  _blinkPorts flag is set, then unhighlight ports. */
  public void mouseExited(MouseEvent me) {
    if (_blinkPorts) hidePorts();
  }

  public void mousePressed(MouseEvent me) { }
  public void mouseReleased(MouseEvent me) { }
  public void mouseClicked(MouseEvent me) { }

} /* end class FigNode */

