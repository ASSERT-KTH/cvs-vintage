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

// File: CrMultipleAgg.java.java
// Classes: CrMultipleAgg.java
// Original Author: jrobbins@ics.uci.edu
// $Id: CrMultipleAgg.java,v 1.3 1998/04/18 23:37:34 jrobbins Exp $

package uci.uml.critics;

import java.util.*;
import uci.argo.kernel.*;
import uci.util.*;
import uci.uml.Foundation.Core.*;
import uci.uml.Foundation.Data_Types.*;

/** Well-formedness rule [2] for Associations. See page 27 of UML 1.1
 *  Semantics. OMG document ad/97-08-04. */

public class CrMultipleAgg extends CrUML {

  public CrMultipleAgg() {
    setHeadline("Multiple Aggregate Roles");
    sd("Only one role of an Association can be aggregate or composite.\n\n" +
       "A clear and consistent is-part-of hierarchy is a key to design clarity, \n"+
       "managable object storage, and the implementation of recursive methods.\n"+
       "To fix this, select the Association and set some of its role \n"+
       "aggregations to None.");

    addSupportedDecision(CrUML.decCONTAINMENT);
  }

  protected void sd(String s) { setDescription(s); }
  
  public boolean predicate(Object dm, Designer dsgr) {
    if (!(dm instanceof IAssociation)) return NO_PROBLEM;
    IAssociation asc = (IAssociation) dm;
    Vector conns = asc.getConnection();
    int aggCount = 0;
    java.util.Enumeration enum = conns.elements();
    while (enum.hasMoreElements()) {
      AssociationEnd ae = (AssociationEnd) enum.nextElement();
      AggregationKind ak = ae.getAggregation();
      if (ak != AggregationKind.UNSPEC && ak != AggregationKind.NONE)
	aggCount++;
    }
    if (aggCount > 1) return PROBLEM_FOUND;
    else return NO_PROBLEM;
  }

} /* end class CrMultipleAgg.java */

