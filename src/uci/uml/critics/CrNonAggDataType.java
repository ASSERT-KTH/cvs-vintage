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

// File: CrNonAggDataType.java.java
// Classes: CrNonAggDataType.java
// Original Author: jrobbins@ics.uci.edu
// $Id: CrNonAggDataType.java,v 1.4 1998/04/23 23:51:47 jrobbins Exp $

package uci.uml.critics;

import java.util.*;
import uci.argo.kernel.*;
import uci.util.*;
import uci.uml.Foundation.Core.*;

/** Well-formedness rule [1] for DataType. See page 28 of UML 1.1
 *  Semantics. OMG document ad/97-08-04. */

public class CrNonAggDataType extends CrUML {

  public CrNonAggDataType() {
    setHeadline("Free Standing DataType");
    sd("DataTypes are not full classes and cannot be associated with \n"+
       "classes, unless the DataType is part of a composite (black diamond) \n"+
       "aggregation. \n\n"+
       "Good OO design depends on careful choices about which entities to \n"+
       "represent as full objects and which to represent as attributes of \n"+
       "objects.\n\n"+
       "To fix this, use the FixIt button, or manually replace the DataType \n"+
       "with a full class or change the association aggregation to containment\n"+
       "by a full class.");

    addSupportedDecision(CrUML.decCONTAINMENT);
    addSupportedDecision(CrUML.decCLASS_SELECTION);
  }

  protected void sd(String s) { setDescription(s); }
  
  public boolean predicate(Object dm, Designer dsgr) {
    // needs-more-work: not implemented
    return NO_PROBLEM;
  }

} /* end class CrNonAggDataType.java */

