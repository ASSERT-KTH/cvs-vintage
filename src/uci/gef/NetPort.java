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

// File: NetPort.java
// Classes: NetPort
// Original Author: ics125b spring 1996
// $Id: NetPort.java,v 1.4 1998/03/27 00:33:52 jrobbins Exp $

package uci.gef;

import java.awt.*;
import java.util.*;
import java.io.*;

import uci.graph.*;

/** This class models a port in our underlying connected graph model.
 *  <A HREF="../features.html#graph_representation_ports">
 *  <TT>FEATURE: graph_representation_ports</TT></A>
 */

public class NetPort extends NetPrimitive implements GraphPortHooks {

  ////////////////////////////////////////////////////////////////
  // constants
  public static String DEFAULT_EDGE_CLASS = "uci.gef.demo.SampleEdge";


  ////////////////////////////////////////////////////////////////
  // instance variables

  /** The NetEdge's that are connected to this port. */
  protected Vector _edges;

  /** The NetNode that this port is a part of. */
  protected Object _parent;

  ////////////////////////////////////////////////////////////////
  // constructors

  /** Construct a new NetPort with the given parent node and no arcs. */
  public NetPort(Object parent) {
    _parent = parent;
    _edges = new Vector();
  }

  ////////////////////////////////////////////////////////////////
  // accessors

  /** Reply the NetNode that owns this port. */
  public NetNode getParentNode() { return (NetNode) _parent; }
  public NetEdge getParentEdge() { return (NetEdge) _parent; }
  public Object getParent() { return _parent; }

  /** Reply a vector of NetEdges that are connected here. */
  public Vector getEdges() { return _edges; }

  ////////////////////////////////////////////////////////////////
  // net-level operations

  /** Add an arc to the list of arcs connected to this port. Called
   *  when the user defines a new arc. Normally, you would not call
   *  this directly, you would call NetEdge#connect(). */
  public void addEdge(NetEdge edge) { _edges.addElement(edge); }

  /** Remove an arc from the list of arcs connected to this
   *  port. Called when the user deletes an arc. Normally, you would
   *  not call this directly, you would call NetEdge#dispose().*/
  public void removeEdge(NetEdge edge) { _edges.removeElement(edge); }

  ////////////////////////////////////////////////////////////////
  // Editor API

  /** Remove this port from the underlying connected graph model and
   *  dispose all arcs connected to it. */
  public void dispose() {
    Enumeration edges = _edges.elements();
    while (edges.hasMoreElements()) {
      NetEdge e = (NetEdge) edges.nextElement();
      e.dispose();
    }
    firePropertyChange("Disposed", false, true);
  }

  ////////////////////////////////////////////////////////////////
  // net-level hooks

  /** Application specific hook that is called after a successful
   *  connection. */
  public void postConnect(GraphModel gm, Object otherPort) {
    NetPort otherNetPort = (NetPort) otherPort;
    NetNode parent = getParentNode();
    parent.postConnect(gm, otherNetPort.getParentNode(), this, otherNetPort);
  }

  /** Application specific hook that is called after a
   *  disconnection. (for now, all disconnections are assumed
   *  legal). */
  public void postDisconnect(GraphModel gm, Object otherPort) {
    NetPort otherNetPort = (NetPort) otherPort;
    NetNode parent = getParentNode();
    parent.postDisconnect(gm, otherNetPort.getParentNode(), this, otherNetPort);
  }

  /** reply the java Class to be used to make new arcs. This is a
   *  utility function called from NetPort#makeEdgeFor */
  protected Class defaultEdgeClass(NetPort otherPort) {
    try { return Class.forName(DEFAULT_EDGE_CLASS); }
    catch (java.lang.ClassNotFoundException ignore) { return null; }
  }

  /** reply a new NetEdge from this port to the given NetPort. */
  public NetEdge makeEdgeFor(NetPort otherPort) {
    Class edgeClass;
    NetEdge edge;
    edgeClass = defaultEdgeClass(otherPort);
    if (edgeClass == null) return null;
    try { edge = (NetEdge) edgeClass.newInstance(); }
    catch (java.lang.IllegalAccessException ignore) {
      System.out.println("asdasd"); return null; }
    catch (java.lang.InstantiationException ignore) {
      System.out.println("ASdasd"); return null; }
    return edge;
  }

  ////////////////////////////////////////////////////////////////
  // net-level constraints

  /** Reply true if this port can legally be connected to the given
   *  port. Subclasses may implement this to reflect application
   *  specific connection constraints. By default, each port just
   *  defers that decision to its parent NetNode. By convention, your
   *  implementation should return false if super.canConnectTo() would
   *  return false (i.e., deeper subclasses get more constrained). I
   *  don't know if that is a good convention. */
  public boolean canConnectTo(GraphModel gm, Object anotherPort) {
    NetNode myNode = getParentNode();
    NetNode otherNode = ((NetPort)anotherPort).getParentNode();
    return myNode.canConnectTo(gm, otherNode, anotherPort, this);
  }


} /* end class NetPort */

