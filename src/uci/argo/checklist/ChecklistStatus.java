// Copyright (c) 1996-98 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation for educational, research and non-profit
// purposes, without fee, and without a written agreement is hereby granted,
// provided that the above copyright notice and this paragraph appear in all
// copies. Permission to incorporate this software into commercial products
// must be negotiated with University of California. This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "as is",
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



// File: ChecklistStatus.java
// Class: ChecklistStatus
// Original Author: jrobbins@ics.uci.edu
// $Id: ChecklistStatus.java,v 1.2 1998/07/02 02:57:31 jrobbins Exp $

package uci.argo.checklist;

import java.util.*;

import uci.util.*;

/** A list of CheckItems that the designer has marked off as already
 *  considered.  In the Argo/UML system, this determines which items
 *  in the TabChecklist have checkmarks.
 *
 * @see uci.uml.ui.TabChecklist
 */

public class ChecklistStatus implements java.io.Serializable {

  ////////////////////////////////////////////////////////////////
  // instance variables

  /** CheckItems that the designer has marked off as already considered. */
  protected Vector _items = new Vector();

  ////////////////////////////////////////////////////////////////
  // constructor

  public ChecklistStatus() { }

  ////////////////////////////////////////////////////////////////
  // accessors

  public Vector getCheckItems() { return _items; }

  public void addItem(CheckItem item) { _items.addElement(item); }

  public synchronized void addAll(ChecklistStatus list) {
    Enumeration cur = list.elements();
    while (cur.hasMoreElements()) {
      CheckItem item = (CheckItem) cur.nextElement();
      addItem(item);
    }
  }

  public void removeItem(CheckItem item) {
    _items.removeElement(item);
  }

  public Enumeration elements() { return _items.elements(); }

  public CheckItem elementAt(int index) {
    return (CheckItem)_items.elementAt(index);
  }

  public boolean contains(CheckItem item) {
    return _items.contains(item);
  }

  public String toString() {
    String res;
    res = getClass().getName() + " {\n";
    Enumeration cur = elements();
    while (cur.hasMoreElements()) {
      CheckItem item = (CheckItem) cur.nextElement();
      res += "    " + item.toString() + "\n";
    }
    res += "  }";
    return res;
  }

} /* end class ChecklistStatus */

