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

// File: CmdSelectNext.java
// Classes: CmdSelectNext
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdSelectNext.java,v 1.2 1998/03/27 00:33:23 jrobbins Exp $

package uci.gef;

import java.awt.*;
import java.util.*;

/** Cmd to select the next (or previous) Fig in the
 *  editor's current view.  This is very convienent for moving among
 *  lots of small diagram elements.  It also provides a simple example
 *  of an Cmd that is bound to a key.
 *  <A HREF="../features.html#select_by_tab_key">
 *  <TT>FEATURE: select_by_tab_key</TT></A>
 *
 * @see ModeSelect#bindKeys */

public class CmdSelectNext extends Cmd {

  ////////////////////////////////////////////////////////////////
  // constants

  public static final String DIR = "Direction";
  public static final String DIR_NEXT = "Next";
  public static final String DIR_PREV = "Previous";

  ////////////////////////////////////////////////////////////////
  // constructors

  public CmdSelectNext() { this(true); }
  public CmdSelectNext(boolean next) {
    this(next ? DIR_NEXT : DIR_PREV);
  }
  public CmdSelectNext(String d) {
    super("Select " + d);
    setArg(DIR, d);
  }

  ////////////////////////////////////////////////////////////////
  // Cmd API

  public void doIt() {
    Selection curSel;
    Fig newFig = null;
    int offset = 1;
    String dir = (String) getArg(DIR);
    if (DIR_PREV.equals(dir)) offset = -1;
    Editor ce = Globals.curEditor();
    SelectionManager sm = ce.getSelectionManager();
    Vector diagramContents = ce.getLayerManager().contents();
    int diagramSize = diagramContents.size();
    int newIndex = diagramSize + 1;

    if (sm.size() == 0) newIndex = 0;
    else if (sm.size() == 1) {
      Fig curFig;
      curSel = (Selection) sm.selections().firstElement();
      curFig = (Fig) curSel.getContent();
      int curIndex = diagramContents.indexOf(curFig);
      newIndex = (curIndex + offset + diagramSize) % diagramSize;
    }
    if (diagramSize > newIndex)
      newFig = (Fig) diagramContents.elementAt(newIndex);
    if (newFig != null) ce.getSelectionManager().select(newFig);
  }

  public void undoIt() {
    System.out.println("Undo does not make sense for CmdSelectNext");
  }

} /* end class CmdSelectNext */

