// Copyright (c) 1996-99 The Regents of the University of California. All
// Rights Reserved. Permission to use, copy, modify, and distribute this
// software and its documentation without fee, and without a written
// agreement is hereby granted, provided that the above copyright notice
// and this paragraph appear in all copies.  This software program and
// documentation are copyrighted by The Regents of the University of
// California. The software program and documentation are supplied "AS
// IS", without any accompanying services from The Regents. The Regents
// does not warrant that the operation of the program will be
// uninterrupted or error-free. The end-user understands that the program
// was developed for research purposes and is advised not to rely
// exclusively on the program for any reason.  IN NO EVENT SHALL THE
// UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY FOR DIRECT, INDIRECT,
// SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES, INCLUDING LOST PROFITS,
// ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
// THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
// SUCH DAMAGE. THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY
// WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
// MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
// PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
// CALIFORNIA HAS NO OBLIGATIONS TO PROVIDE MAINTENANCE, SUPPORT,
// UPDATES, ENHANCEMENTS, OR MODIFICATIONS.





// File: CrEmptyPackage.java
// Classes: CrEmptyPackage
// Original Author: jrobbins@ics.uci.edu
// $Id: CrEmptyPackage.java,v 1.10 1999/04/27 00:20:18 jrobbins Exp $

package uci.uml.critics;

import java.util.*;
import uci.argo.kernel.*;
import uci.util.*;
import uci.uml.Foundation.Core.*;
import uci.uml.Foundation.Data_Types.*;
import uci.uml.Foundation.Extension_Mechanisms.*;
import uci.uml.Model_Management.*;

/** A critic to detect when a class can never have instances (of
 *  itself of any subclasses). */

public class CrEmptyPackage extends CrUML {

  public CrEmptyPackage() {
    setHeadline("Add Elements to Package <ocl>self</ocl>");
    sd("You have not yet put anything in package <ocl>self</ocl>. "+
       "Normally packages contain groups of related classes.\n\n"+
       "Defining and using packages is key to making a maintainable "+
       "design. \n\n"+
       "To fix this, select package <ocl>self</ocl> in the navigator panel and add  "+
       "diagrams or model elements such as classes or use cases. ");
       
    addSupportedDecision(CrUML.decMODULARITY);
    addTrigger("ownedElement");
  }

  public boolean predicate2(Object dm, Designer dsgr) {
    //System.out.println("predicate2 on " + dm);
    if (!(dm instanceof Model)) return NO_PROBLEM;
    Model mod = (Model) dm;
    Vector elms = mod.getOwnedElement();
    if (elms == null || elms.size() == 0) return PROBLEM_FOUND;
    return NO_PROBLEM;
    //needs-more-work: different critic for packages consisting only
    //of references to elements of other packages?
  }

} /* end class CrEmptyPackage */

