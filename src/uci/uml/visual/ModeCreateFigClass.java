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

// File: ModeCreateFigClass.java
// Classes: ModeCreateFigClass
// Original Author: abonner
// $Id: ModeCreateFigClass.java,v 1.2 1998/03/27 00:30:49 jrobbins Exp $

package uci.uml.visual;

import java.awt.event.*;

import uci.gef.*;

/** A Mode to interpert user input while creating a FigRect. All of
 *  the actual event handling is inherited from ModeCreate. This class
 *  just implements the differences needed to make it specific to
 *  rectangles.
 *  <A HREF="../features.html#basic_shapes_rect">
 *  <TT>FEATURE: basic_shapes_rect</TT></A>
 */

public class ModeCreateFigClass extends ModeCreateFigRect {
  ////////////////////////////////////////////////////////////////
  // Mode API
  
  public Fig createNewItem(MouseEvent me, int snapX, int snapY) {
    return new FigClass();
  }

  public String instructions() {
    return "Drag to create a class";
  }

} /* end class ModeCreateFigClass */

