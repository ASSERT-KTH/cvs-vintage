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

// File: CmdSelectAll.java
// Classes: CmdSelectAll
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdSelectAll.java,v 1.2 1998/03/27 00:33:21 jrobbins Exp $

package uci.gef;

import java.awt.*;
import java.util.*;

/** Cmd to select all the Figs in the editor's current
 *  view.
 *  <A HREF="../features.html#select_all">
 *  <TT>FEATURE: select_all</TT></A>
 *
 * @see ModeSelect#bindKeys */

public class CmdSelectAll extends Cmd {

  public CmdSelectAll() { super("Select All"); }

  public void doIt() {
    Editor ce = Globals.curEditor();
    Vector diagramContents = ce.getLayerManager().contents();
    ce.getSelectionManager().select(diagramContents);
  }

  public void undoIt() {
    System.out.println("Undo does not make sense for CmdSelectAll");
  }

} /* end class CmdSelectAll */

