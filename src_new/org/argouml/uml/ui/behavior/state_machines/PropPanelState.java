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



// File: PropPanelState.java
// Classes: PropPanelState
// Original Author: your email address here
// $Id: PropPanelState.java,v 1.5 2001/01/10 06:24:01 carnold Exp $

package org.argouml.uml.ui.behavior.state_machines;

import java.awt.*;
import javax.swing.*;
import ru.novosoft.uml.foundation.core.*;
import org.argouml.uml.ui.*;
import java.util.*;
import ru.novosoft.uml.behavior.state_machines.*;
import ru.novosoft.uml.foundation.data_types.*;

public class PropPanelState extends PropPanel {

  ////////////////////////////////////////////////////////////////
  // contructors
    public PropPanelState() {
        super("State Properties",2);

        Class mclass = MState.class;
    
        addCaption("Name:",0,0,0);
        addField(new UMLTextField(this,new UMLTextProperty(mclass,"name","getName","setName")),0,0,0);

        addCaption("Stereotype:",1,0,0);
        JComboBox stereotypeBox = new UMLStereotypeComboBox(this);
        addField(stereotypeBox,1,0,0);

        addCaption("State Machine:",2,0,0);
        JList stateList = new UMLList(new UMLReflectionListModel(this,"statemachine",false,"getStateMachine",null,null,null),true);
        addLinkField(stateList,2,0,0);
        
        addCaption("Namespace:",3,0,1);
        JList namespaceList = new UMLList(new UMLNamespaceListModel(this),true);
        addLinkField(namespaceList,3,0,0);
    

        addCaption("Incoming:",0,1,0);
        addCaption("Outgoing:",1,1,1);
    
    
  }

    public MStateMachine getStateMachine() {
        MStateMachine machine = null;
        Object target = getTarget();
        if(target instanceof MState) {
            machine = ((MState) target).getStateMachine();
        }
        return machine;
    }

    protected boolean isAcceptibleBaseMetaClass(String baseClass) {
        return baseClass.equals("State");
    }
  


} /* end class PropPanelState */



