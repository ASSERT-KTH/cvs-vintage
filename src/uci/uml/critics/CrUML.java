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

// File: CrUML.java
// Classes: CrUML
// Original Author: jrobbins@ics.uci.edu
// $Id: CrUML.java,v 1.1 1998/03/27 01:27:46 jrobbins Exp $

package uci.uml.critics;

import uci.argo.kernel.*;

/** "Abstract" Critic subclass that captures commonalities among all
 *  critics in the UML domain. */

public class CrUML extends Critic {
  public static final String decINHERITANCE = "Inheritance";
  public static final Decision decisionINHERITANCE = new
  Decision(decINHERITANCE, 1);
  public static final String decCONTAINMENT = "Containment";
  public static final Decision decisionCONTAINMENT = new
  Decision(decCONTAINMENT, 1);

  
  public CrUML() {
    //decisionCategory("UML Decisions");
    // what do UML critics have in common? anything?
  }

  /** Static initializer for this class. Called when the class is
   *  loaded (which is before any subclass instances are instanciated). */
  static {
    Designer.theDesigner().defineDecision(decINHERITANCE, 1);
    Designer.theDesigner().defineDecision(decCONTAINMENT, 1);
  }

} /* end class CrUML */
