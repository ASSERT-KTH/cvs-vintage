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
// $Id: PropPanelGuard.java,v 1.2 2000/11/17 16:23:36 carnold Exp $

package org.argouml.uml.ui.behavior.state_machines;

import java.awt.*;
import javax.swing.*;
import ru.novosoft.uml.foundation.core.*;
import org.argouml.uml.ui.*;
import java.util.*;
import ru.novosoft.uml.behavior.state_machines.*;
import ru.novosoft.uml.foundation.data_types.*;

public class PropPanelGuard extends PropPanel {

  ////////////////////////////////////////////////////////////////
  // contructors
    public PropPanelGuard() {
        super("State Properties",2);

        Class mclass = MGuard.class;

        addCaption(new JLabel("Name:"),0,0,0);
        addField(new UMLTextField(this,new UMLTextProperty(mclass,"name","getName","setName")),0,0,0);

        addCaption(new JLabel("Stereotype:"),1,0,0);
        JComboBox stereotypeBox = new UMLStereotypeComboBox(this);
        addField(stereotypeBox,1,0,0);

        addCaption(new JLabel("Transition:"),2,0,1);
        JList transitionList = new UMLList(new UMLReflectionListModel(this,"transition",false,"getTransition",null,null,null),true);
        addLinkField(transitionList,2,0,0);

        UMLExpressionModel expressionModel = new UMLExpressionModel(this,MGuard.class,"expression",
            MBooleanExpression.class,"getExpression","setExpression");

        addCaption(new JLabel("Expression:"),0,1,1);
        addField(new UMLExpressionBodyField(expressionModel,true),0,1,1);

        addCaption(new JLabel("Language:"),1,1,0);
        addField(new UMLExpressionLanguageField(expressionModel,false),1,1,0);

  }

    public MTransition getTransition() {
        MTransition transition = null;
        Object target = getTarget();
        if(target instanceof MGuard) {
            transition = ((MGuard) target).getTransition();
        }
        return transition;
    }

    protected boolean isAcceptibleBaseMetaClass(String baseClass) {
        return baseClass.equals("Guard");
    }

} /* end class PropPanelState */



