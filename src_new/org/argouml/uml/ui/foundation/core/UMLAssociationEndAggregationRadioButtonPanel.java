// Copyright (c) 1996-2002 The Regents of the University of California. All
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

// $Id: UMLAssociationEndAggregationRadioButtonPanel.java,v 1.1 2003/01/04 16:48:38 kataka Exp $
package org.argouml.uml.ui.foundation.core;

import java.util.HashMap;
import java.util.Map;

import org.argouml.application.api.Argo;
import org.argouml.uml.ui.UMLRadioButtonPanel;

import ru.novosoft.uml.foundation.core.MAssociationEnd;
import ru.novosoft.uml.foundation.data_types.MAggregationKind;

/**
 * 
 * @author jaap.branderhorst@xs4all.nl	
 * @since Jan 4, 2003
 */
public class UMLAssociationEndAggregationRadioButtonPanel extends UMLRadioButtonPanel {

    private static Map labelTextsAndActionCommands = new HashMap();

    static {
        labelTextsAndActionCommands.put(Argo.localize("UMLMenu", "label.aggregationkind-aggregate"), ActionSetAssociationEndAggregation.AGGREGATE_COMMAND);
        labelTextsAndActionCommands.put(Argo.localize("UMLMenu", "label.aggregationkind-composite"), ActionSetAssociationEndAggregation.COMPOSITE_COMMAND);
        labelTextsAndActionCommands.put(Argo.localize("UMLMenu", "label.aggregationkind-none"), ActionSetAssociationEndAggregation.NONE_COMMAND);
    }

    /**
     * Constructor for UMLAssociationEndAggregationRadioButtonPanel.
     * @param title
     * @param labeltexts
     * @param propertySetName
     * @param setAction
     * @param horizontal
     */
    public UMLAssociationEndAggregationRadioButtonPanel(String title, boolean horizontal) {
        super(title, labelTextsAndActionCommands, "aggregation", ActionSetAssociationEndAggregation.SINGLETON, horizontal);
    }

    /**
     * @see org.argouml.uml.ui.UMLRadioButtonPanel#buildModel()
     */
    public void buildModel() {
        if (getTarget() != null) {
            MAssociationEnd target = (MAssociationEnd)getTarget();
            MAggregationKind kind = target.getAggregation();
            if (kind == null || kind.equals(MAggregationKind.NONE)) {
                setSelected(ActionSetAssociationEndAggregation.NONE_COMMAND);
            } else
            if (kind.equals(MAggregationKind.AGGREGATE)) {
                setSelected(ActionSetAssociationEndAggregation.AGGREGATE_COMMAND); 
            } else
            if (kind.equals(MAggregationKind.COMPOSITE)) {
                setSelected(ActionSetAssociationEndAggregation.COMPOSITE_COMMAND);
            } else
                setSelected(ActionSetAssociationEndAggregation.NONE_COMMAND);
        }
    }

}
