// $Id: UMLActivityDiagram.java,v 1.43 2004/07/18 07:23:30 mvw Exp $
// Copyright (c) 1996-2004 The Regents of the University of California. All
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

// File: UMLActivityDiagram.java
// Classes: UMLActivityDiagram

package org.argouml.uml.diagram.activity.ui;

import java.beans.PropertyVetoException;

import javax.swing.Action;

import org.apache.log4j.Logger;

import org.argouml.kernel.ProjectManager;
import org.argouml.model.ModelFacade;
import org.argouml.ui.CmdCreateNode;
import org.argouml.ui.CmdSetMode;
import org.argouml.uml.diagram.activity.ActivityDiagramGraphModel;
import org.argouml.uml.diagram.state.StateDiagramGraphModel;
import org.argouml.uml.diagram.state.ui.ActionCreatePseudostate;
import org.argouml.uml.diagram.state.ui.StateDiagramRenderer;
import org.argouml.uml.diagram.ui.UMLDiagram;
import org.argouml.uml.diagram.ui.ActionAddNote;

import org.tigris.gef.base.LayerPerspective;
import org.tigris.gef.base.LayerPerspectiveMutable;
import org.tigris.gef.base.ModeCreatePolyEdge;

/**
 * Enabling an activity diagram connected to an actor has been
 * requested as a feature.<p>
 *
 * As well enabling swim lanes in the activity diagram is considered
 * valuable as well.<p>
 */
public class UMLActivityDiagram extends UMLDiagram {
    /**
     * @deprecated by Linus Tolke as of 0.15.4. Use your own logger in your
     * class. This will be removed.
     */
    protected static Logger cat = Logger.getLogger(UMLActivityDiagram.class);

    ////////////////
    // actions for toolbar

    protected static Action _actionState =
        new CmdCreateNode(ModelFacade.ACTION_STATE, "ActionState");

    protected static Action _actionStartPseudoState;
    protected static Action _actionFinalPseudoState;
    protected static Action _actionJunctionPseudoState;
    protected static Action _actionBranchPseudoState;
    protected static Action _actionForkPseudoState;
    protected static Action _actionJoinPseudoState;
    // protected static Action _actionNewSwimlane;

    protected static Action _actionTransition =
        new CmdSetMode(
            ModeCreatePolyEdge.class,
            "edgeClass",
            ModelFacade.TRANSITION,
            "Transition");

    ////////////////////////////////////////////////////////////////
    // contructors

    protected static int _ActivityDiagramSerial = 1;

    public UMLActivityDiagram() {

        try {
            setName(getNewDiagramName());
        } catch (PropertyVetoException pve) { }

	// start state, end state, forks, joins, etc.
	_actionStartPseudoState =
	    new ActionCreatePseudostate(ModelFacade.INITIAL_PSEUDOSTATEKIND, 
					"Initial");

	_actionFinalPseudoState =
            new CmdCreateNode(ModelFacade.FINALSTATE, "FinalState");

	_actionJunctionPseudoState =
	    new ActionCreatePseudostate(ModelFacade.JUNCTION_PSEUDOSTATEKIND,
					"Junction");

	_actionForkPseudoState =
	    new ActionCreatePseudostate(ModelFacade.FORK_PSEUDOSTATEKIND, 
					"Fork");

	_actionJoinPseudoState =
	    new ActionCreatePseudostate(ModelFacade.JOIN_PSEUDOSTATEKIND,
					"Join");
	
	//_actionNewSwimlane = new CmdCreateNode(ModelFacade.PARTITION, "Create a new swimlane");

    }

    public UMLActivityDiagram(Object m) {
        this();
        setNamespace(m);
        Object context = ModelFacade.getContext(getStateMachine());
        String name = null;
        if (context != null
            && ModelFacade.getName(context) != null
            && ModelFacade.getName(context).length() > 0) {
            name = ModelFacade.getName(context);
            try {
                setName(name);
            } catch (PropertyVetoException pve) { }
        }
    }

    public UMLActivityDiagram(Object namespace, Object agraph) {

        this();

        if (!ModelFacade.isANamespace(namespace)
            || !ModelFacade.isAActivityGraph(agraph))
            throw new IllegalArgumentException();

        if (namespace != null && ModelFacade.getName(namespace) != null) {
            String name =
                ModelFacade.getName(namespace)
                    + " activity "
                    + (ModelFacade.getBehaviors(namespace).size());
            try {
                setName(name);
            } catch (PropertyVetoException pve) { }
        }
        if (namespace != null)
            setup(namespace, agraph);
        else
            throw new NullPointerException("Namespace may not be null");
    }

    public void initialize(Object o) {
        if (!(org.argouml.model.ModelFacade.isAActivityGraph(o)))
            return;
        Object context = ModelFacade.getContext(o);
        if (context != null
            && org.argouml.model.ModelFacade.isANamespace(context))
            setup(context, o);
        else
            cat.debug("ActivityGraph without context not yet possible :-(");
    }

    /**
     * Method to perform a number of important initializations of an
     * <I>Activity Diagram</I>.<p>
     * 
     * Each diagram type has a similar <I>UMLxxxDiagram</I> class.<p>
     *
     * Changed <I>lay</I> from <I>LayerPerspective</I> to
     * <I>LayerPerspectiveMutable</I>.  This class is a child of
     * <I>LayerPerspective</I> and was implemented to correct some
     * difficulties in changing the model. <I>lay</I> is used mainly
     * in <I>LayerManager</I>(GEF) to control the adding, changing and
     * deleting layers on the diagram...  psager@tigris.org Jan. 24,
     * 2002

     * @param m  Namespace from the model
     * @param agraph ActivityGraph from the model
     */
    public void setup(Object m, Object agraph) {

        if (!ModelFacade.isANamespace(m)
            || !ModelFacade.isAActivityGraph(agraph))
            throw new IllegalArgumentException();

        super.setNamespace(m);
        ActivityDiagramGraphModel gm = new ActivityDiagramGraphModel();
        gm.setNamespace(m);
        if (agraph != null) {
            gm.setMachine(agraph);
        }
        LayerPerspective lay =
            new LayerPerspectiveMutable(ModelFacade.getName(m), gm);
        StateDiagramRenderer rend = new StateDiagramRenderer(); // singleton
        lay.setGraphNodeRenderer(rend);
        lay.setGraphEdgeRenderer(rend);

        setLayer(lay);

    }

    public Object getOwner() {
        StateDiagramGraphModel gm = (StateDiagramGraphModel) getGraphModel();
        Object sm = gm.getMachine();
        if (sm != null)
            return sm;
        return gm.getNamespace();
    }

    public Object getStateMachine() {
        return ((StateDiagramGraphModel) getGraphModel()).getMachine();
    }

    public void setStateMachine(Object sm) {

        if (!ModelFacade.isAStateMachine(sm))
            throw new IllegalArgumentException();

        ((StateDiagramGraphModel) getGraphModel()).setMachine(sm);
    }

    /**
     * Get the actions from which to create a toolbar or equivalent
     * graphic triggers.
     */
    protected Object[] getUmlActions() {
        Object actions[] =
        {
	    _actionState,
	    _actionTransition,
	    null,
	    _actionStartPseudoState,
	    _actionFinalPseudoState,
	    _actionJunctionPseudoState,
	    _actionForkPseudoState,
	    _actionJoinPseudoState,
	    //_actionNewSwimlane,
	    null,
	    ActionAddNote.SINGLETON,
	};
        return actions;
    }

    /**
     * Creates a new diagram name.<p>
     *
     * @return String
     */
    protected static String getNewDiagramName() {
        String name = null;
        name = "Activity Diagram " + _ActivityDiagramSerial;
        _ActivityDiagramSerial++;
        if (!ProjectManager.getManager().getCurrentProject()
                 .isValidDiagramName(name)) {
            name = getNewDiagramName();
        }
        return name;
    }
} /* end class UMLActivityDiagram */
