// File: ChildGenUML.java
// Classes: ChildGenUML
// Original Author: jrobbins
// $Id: ChildGenUML.java,v 1.1 1998/03/27 01:27:45 jrobbins Exp $

package uci.uml.critics;

import java.util.*;
import uci.util.*;
import uci.uml.ui.*;
import uci.uml.Foundation.Core.*;
import uci.uml.Model_Management.*;

public class ChildGenUML implements ChildGenerator {
  /** Reply a Enumeration of the children of the given Object */
  public Enumeration gen(Object o) {
    if (o instanceof Project) {
      return ((Project)o).getModels().elements();
    }

    if (o instanceof Package) {
      return ((Package)o).getOwnedElement().elements();
    }
    
    if (o instanceof ElementOwnership) {
      ModelElement me = ((ElementOwnership)o).getModelElement();
      return new EnumerationSingle(me);
    }
    
    if (o instanceof Classifier) {
      Classifier c = (Classifier) o;
      Vector beh = c.getBehavioralFeature();
      Vector str = c.getStructuralFeature();
      if (beh == null && str == null) return EnumerationEmpty.theInstance();
      if (beh == null) return str.elements();
      if (str == null) return beh.elements();
      return new EnumerationComposite(beh.elements(), str.elements());
    }

    // tons more cases
    
    return EnumerationEmpty.theInstance();
  }
} /* end class ChildGenUML */

