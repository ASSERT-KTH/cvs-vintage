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




package org.argouml.uml.ui.foundation.core;

import org.argouml.uml.ui.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import ru.novosoft.uml.model_management.*;
import ru.novosoft.uml.foundation.core.*;
import ru.novosoft.uml.*;

import org.tigris.gef.util.Util;

import org.argouml.uml.ui.foundation.core.*;

public abstract class PropPanelNamespace extends PropPanelModelElement {

    private static ImageIcon _classIcon = Util.loadIconResource("Class");
    private static ImageIcon _interfaceIcon = Util.loadIconResource("Interface");
    private static ImageIcon _datatypeIcon = Util.loadIconResource("DataType");
    private static ImageIcon _packageIcon = Util.loadIconResource("Package");




  ////////////////////////////////////////////////////////////////
  // contructors
    public PropPanelNamespace(String panelName,int columns) {
        super(panelName,columns);
    }

    public void addClass() {
        Object target = getTarget();
        if(target instanceof MNamespace) {
            MNamespace ns = (MNamespace) target;
            MModelElement ownedElem = ns.getFactory().createClass();
            ns.addOwnedElement(ownedElem);
            navigateTo(ownedElem);
        }
    }

    public void addInterface() {
        Object target = getTarget();
        if(target instanceof MNamespace) {
            MNamespace ns = (MNamespace) target;
            MModelElement ownedElem = ns.getFactory().createInterface();
            ns.addOwnedElement(ownedElem);
            navigateTo(ownedElem);
        }
    }

    public void addDataType() {
        Object target = getTarget();
        if(target instanceof MNamespace) {
            MNamespace ns = (MNamespace) target;
            MModelElement ownedElem = ns.getFactory().createDataType();
            ns.addOwnedElement(ownedElem);
            navigateTo(ownedElem);
        }
    }

    public void addPackage() {
        Object target = getTarget();
        if(target instanceof MNamespace) {
            MNamespace ns = (MNamespace) target;
            MModelElement ownedElem = ns.getFactory().createPackage();
            ns.addOwnedElement(ownedElem);
            navigateTo(ownedElem);
        }
    }

    protected boolean isAcceptibleBaseMetaClass(String baseClass) {
        return baseClass.equals("Namespace");
    }



} /* end class PropPanelClass */
