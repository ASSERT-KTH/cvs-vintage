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

// File: CmdShowURL.java
// Classes: CmdShowURL
// Original Author: jrobbins@ics.uci.edu
// $Id: CmdShowURL.java,v 1.2 1998/03/27 00:33:25 jrobbins Exp $

package uci.gef;

import java.awt.Event;
import java.net.URL;

/** Cmd to display the contents of the given URL in the browser.
 *  Needs-More-Work: This Cmd can only be used from an applet.
 *  <A HREF="../features.html#show_document">
 *  <TT>FEATURE: show_document</TT></A>
 */

public class CmdShowURL extends Cmd {

  protected URL _url;

  public CmdShowURL(URL url) { this(); url(url); }

  public CmdShowURL(String s) throws java.net.MalformedURLException {
    this();
    url(s);
  }

  public CmdShowURL() { super("Show URL in browser"); }

  public void url(URL u) { _url = u; }

  public void url(String u) throws java.net.MalformedURLException {
    _url = new URL(u);
  }

  public URL url() { return _url; }

  /** Translate all selected Fig's in the current editor. */
  public void doIt() {
    Globals.showDocument(_url);
  }

  public void undoIt() { System.out.println("Needs-More-Work"); }

} /* end class CmdShowURL */

