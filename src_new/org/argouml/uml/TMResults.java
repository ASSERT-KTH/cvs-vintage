// $Id: TMResults.java,v 1.11 2004/08/29 09:29:51 mvw Exp $
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

package org.argouml.uml;

import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.argouml.i18n.Translator;
import org.argouml.model.ModelFacade;
import org.argouml.uml.diagram.activity.ui.UMLActivityDiagram;
import org.argouml.uml.diagram.collaboration.ui.UMLCollaborationDiagram;
import org.argouml.uml.diagram.deployment.ui.UMLDeploymentDiagram;
import org.argouml.uml.diagram.sequence.ui.UMLSequenceDiagram;
import org.argouml.uml.diagram.state.ui.UMLStateDiagram;
import org.argouml.uml.diagram.static_structure.ui.UMLClassDiagram;
import org.argouml.uml.diagram.use_case.ui.UMLUseCaseDiagram;
import org.tigris.gef.base.Diagram;

/** 
 * TMResults (Table Model Results) implements a default table model 
 * which is used by Find and Goto Operations in order to display search 
 * results. It defines a default table model with columns and can 
 * resolve found objects to strings.
 */
public class TMResults extends AbstractTableModel {

    private static final String BUNDLE = "Label";

    private Vector rowObjects;
    private Vector diagrams;

    /**
     * The constructor.
     * 
     */
    public TMResults() {
    }

    ////////////////
    // accessors
    
    /**
     * @param results the row objects
     * @param theDiagrams the diagrams
     */
    public void setTarget(Vector results, Vector theDiagrams) {
        rowObjects = results;
        diagrams = theDiagrams;
        fireTableStructureChanged();
    }

    ////////////////
    // TableModel implementation
    
    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount() {
        return 4;
    }
    
    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public int getRowCount() {
        if (rowObjects == null)
            return 0;
        return rowObjects.size();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnName(int)
     */
    public String getColumnName(int c) {
        if (c == 0)
            return "Type";
        if (c == 1)
            return "Name";
        if (c == 2)
            return "In Diagram";
        if (c == 3)
            return "Description";
        return "XXX";
    }

    /**
     * @see javax.swing.table.TableModel#getColumnClass(int)
     */
    public Class getColumnClass(int c) {
        return String.class;
    }

    /**
     * @see javax.swing.table.TableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int row, int col) {
        return false;
    }

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
        if (row < 0 || row >= rowObjects.size())
            return "bad row!";
        if (col < 0 || col >= 4)
            return "bad col!";
        Object rowObj = rowObjects.elementAt(row);
        if (rowObj instanceof Diagram) {
            Diagram d = (Diagram) rowObj;
            switch (col) {
	    case 0 :
		String name = null;
		if (d instanceof UMLClassDiagram)
		    name = "label.class-diagram";
		else if (d instanceof UMLUseCaseDiagram) {
		    name = "label.usecase-diagram";
		} else if (d instanceof UMLStateDiagram) {
		    name = "label.state-chart-diagram";
		} else if (d instanceof UMLDeploymentDiagram) {
		    name = "label.deployment-diagram";
		} else if (d instanceof UMLCollaborationDiagram) {
		    name = "label.collaboration-diagram";
		} else if (d instanceof UMLActivityDiagram) {
		    name = "label.activity-diagram";
		} else if (d instanceof UMLSequenceDiagram) {
		    name = "label.sequence-diagram";
		}
		return Translator.localize(BUNDLE, name);
	    case 1 :
		return d.getName();
	    case 2 :
		return "N/A";
	    case 3 :
		//GraphModel gm = d.getGraphModel();
		int numNodes = d.getNodes(null).size();
		int numEdges = d.getEdges(null).size();
		return numNodes + " nodes and " + numEdges + " edges";
            }
        }
        if (ModelFacade.isAModelElement(rowObj)) {
            Diagram d = null;
            if (diagrams != null)
                d = (Diagram) diagrams.elementAt(row);
            switch (col) {
	    case 0 :
		return ModelFacade.getUMLClassName(rowObj);
	    case 1 :
		return ModelFacade.getName(rowObj);
	    case 2 :
		return (d == null) ? "N/A" : d.getName();
	    case 3 :
		return "docs";
            }
        }
        switch (col) {
	case 0 :
	    String clsName = rowObj.getClass().getName();
	    int lastDot = clsName.lastIndexOf(".");
	    return clsName.substring(lastDot + 1);
	case 1 :
	    return "";
	case 2 :
	    return "??";
	case 3 :
	    return "docs";
        }
        return "unknown!";
    }

    /**
     * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
     */
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

} /* end class TMResults */
