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

package org.argouml.ui;

import org.apache.log4j.Category;
import org.argouml.model.ModelFacade;
import org.argouml.ui.targetmanager.TargetManager;
import org.argouml.uml.DocumentationManager;
import org.tigris.gef.presentation.Fig;

public class TabDocs extends TabText {
    protected static Category cat = 
        Category.getInstance(TabDocs.class);
  ////////////////////////////////////////////////////////////////
  // constructor
  public TabDocs() {
    super("Javadocs");
  }

  ////////////////////////////////////////////////////////////////
  // accessors
  protected String genText(Object modelObject) {
    modelObject = (modelObject instanceof Fig) ? ((Fig)modelObject).getOwner() : modelObject;    
    return !(ModelFacade.isAElement(modelObject)) ? null : DocumentationManager.getDocs(modelObject,"");
  }

  protected void parseText(String s) {
    Object modelObject = TargetManager.getInstance().getTarget();
   modelObject = (modelObject instanceof Fig) ? ((Fig)modelObject).getOwner() : modelObject;    
    if (modelObject == null) return;
    DocumentationManager.setDocs(modelObject, s);
  }

} /* end class TabDocs */
