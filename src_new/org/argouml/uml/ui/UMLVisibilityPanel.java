// $Id: UMLVisibilityPanel.java,v 1.6 2003/09/21 14:11:12 bobtarling Exp $
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

package org.argouml.uml.ui;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.*;
import org.argouml.model.ModelFacade;

import ru.novosoft.uml.foundation.data_types.*;

/**
 * @deprecated as of ArgoUml 0.13.5 (10-may-2003),
 *             replaced by {@link org.argouml.uml.ui.foundation.core.UMLModelElementVisibilityRadioButtonPanel},
 *             this class is part of the 'old'(pre 0.13.*) implementation of proppanels
 *             that used reflection a lot.
 */
public class UMLVisibilityPanel extends JPanel {


    ////////////////////////////////////////////////////////////////
    // contructors
    public UMLVisibilityPanel(UMLUserInterfaceContainer container,
			      Class mclass, int columns, boolean border)
    {
	setLayout(new GridLayout(0, columns));
	ButtonGroup group = new ButtonGroup();
	UMLRadioButton publicButton = 
	    new UMLRadioButton("public", container,
		    new UMLEnumerationBooleanProperty("visibility",
						      mclass,
						      "getVisibility",
						      "setVisibility",
						      (Class)ModelFacade.VISIBILITYKIND,
						      (Class)ModelFacade.PUBLIC_VISIBILITYKIND,
						      null));
	publicButton.setSelected(true);
	add(publicButton);
	group.add(publicButton);
    
	UMLRadioButton protectedButton = new UMLRadioButton("protected", container, new UMLEnumerationBooleanProperty("visibility", mclass, "getVisibility", "setVisibility", (Class)ModelFacade.VISIBILITYKIND, (Class)ModelFacade.PROTECTED_VISIBILITYKIND, null));
	add(protectedButton);
	group.add(protectedButton);
    
	UMLRadioButton privateButton = new UMLRadioButton("private", container, new UMLEnumerationBooleanProperty("visibility", mclass, "getVisibility", "setVisibility", (Class)ModelFacade.VISIBILITYKIND, (Class)ModelFacade.PRIVATE_VISIBILITYKIND, null));
	add(privateButton);
	group.add(privateButton);
    
	if (border) {
	    Border titled = BorderFactory.createTitledBorder("Visibility");
	    setBorder(titled);
	}
    }

  
}
