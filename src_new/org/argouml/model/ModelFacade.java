// $Id: ModelFacade.java,v 1.95 2003/08/31 23:34:03 bobtarling Exp $
// Copyright (c) 2003 The Regents of the University of California. All
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

package org.argouml.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.argouml.model.uml.foundation.core.CoreHelper;
import org.argouml.model.uml.foundation.extensionmechanisms.ExtensionMechanismsHelper;
import org.argouml.uml.MMUtil;
import org.tigris.gef.base.Diagram;

import ru.novosoft.uml.MBase;
import ru.novosoft.uml.MElementListener;
import ru.novosoft.uml.MFactory;
import ru.novosoft.uml.behavior.activity_graphs.MActionState;
import ru.novosoft.uml.behavior.activity_graphs.MActivityGraph;
import ru.novosoft.uml.behavior.activity_graphs.MPartition;
import ru.novosoft.uml.behavior.collaborations.MAssociationEndRole;
import ru.novosoft.uml.behavior.collaborations.MAssociationRole;
import ru.novosoft.uml.behavior.collaborations.MClassifierRole;
import ru.novosoft.uml.behavior.collaborations.MCollaboration;
import ru.novosoft.uml.behavior.collaborations.MInteraction;
import ru.novosoft.uml.behavior.collaborations.MMessage;
import ru.novosoft.uml.behavior.common_behavior.MAction;
import ru.novosoft.uml.behavior.common_behavior.MActionSequence;
import ru.novosoft.uml.behavior.common_behavior.MAttributeLink;
import ru.novosoft.uml.behavior.common_behavior.MCallAction;
import ru.novosoft.uml.behavior.common_behavior.MComponentInstance;
import ru.novosoft.uml.behavior.common_behavior.MCreateAction;
import ru.novosoft.uml.behavior.common_behavior.MDataValue;
import ru.novosoft.uml.behavior.common_behavior.MDestroyAction;
import ru.novosoft.uml.behavior.common_behavior.MInstance;
import ru.novosoft.uml.behavior.common_behavior.MLink;
import ru.novosoft.uml.behavior.common_behavior.MLinkEnd;
import ru.novosoft.uml.behavior.common_behavior.MNodeInstance;
import ru.novosoft.uml.behavior.common_behavior.MObject;
import ru.novosoft.uml.behavior.common_behavior.MReception;
import ru.novosoft.uml.behavior.common_behavior.MReturnAction;
import ru.novosoft.uml.behavior.common_behavior.MSendAction;
import ru.novosoft.uml.behavior.common_behavior.MSignal;
import ru.novosoft.uml.behavior.common_behavior.MStimulus;
import ru.novosoft.uml.behavior.state_machines.MCallEvent;
import ru.novosoft.uml.behavior.state_machines.MCompositeState;
import ru.novosoft.uml.behavior.state_machines.MFinalState;
import ru.novosoft.uml.behavior.state_machines.MGuard;
import ru.novosoft.uml.behavior.state_machines.MPseudostate;
import ru.novosoft.uml.behavior.state_machines.MState;
import ru.novosoft.uml.behavior.state_machines.MStateMachine;
import ru.novosoft.uml.behavior.state_machines.MStateVertex;
import ru.novosoft.uml.behavior.state_machines.MTransition;
import ru.novosoft.uml.behavior.use_cases.MActor;
import ru.novosoft.uml.behavior.use_cases.MExtend;
import ru.novosoft.uml.behavior.use_cases.MExtensionPoint;
import ru.novosoft.uml.behavior.use_cases.MInclude;
import ru.novosoft.uml.behavior.use_cases.MUseCase;
import ru.novosoft.uml.foundation.core.MAbstraction;
import ru.novosoft.uml.foundation.core.MAssociation;
import ru.novosoft.uml.foundation.core.MAssociationClass;
import ru.novosoft.uml.foundation.core.MAssociationEnd;
import ru.novosoft.uml.foundation.core.MAttribute;
import ru.novosoft.uml.foundation.core.MBehavioralFeature;
import ru.novosoft.uml.foundation.core.MClass;
import ru.novosoft.uml.foundation.core.MClassifier;
import ru.novosoft.uml.foundation.core.MComment;
import ru.novosoft.uml.foundation.core.MComponent;
import ru.novosoft.uml.foundation.core.MConstraint;
import ru.novosoft.uml.foundation.core.MDataType;
import ru.novosoft.uml.foundation.core.MDependency;
import ru.novosoft.uml.foundation.core.MElement;
import ru.novosoft.uml.foundation.core.MElementResidence;
import ru.novosoft.uml.foundation.core.MFeature;
import ru.novosoft.uml.foundation.core.MBehavioralFeature;
import ru.novosoft.uml.foundation.core.MFlow;
import ru.novosoft.uml.foundation.core.MGeneralizableElement;
import ru.novosoft.uml.foundation.core.MGeneralization;
import ru.novosoft.uml.foundation.core.MInterface;
import ru.novosoft.uml.foundation.core.MMethod;
import ru.novosoft.uml.foundation.core.MModelElement;
import ru.novosoft.uml.foundation.core.MNamespace;
import ru.novosoft.uml.foundation.core.MNode;
import ru.novosoft.uml.foundation.core.MOperation;
import ru.novosoft.uml.foundation.core.MParameter;
import ru.novosoft.uml.foundation.core.MPermission;
import ru.novosoft.uml.foundation.core.MRelationship;
import ru.novosoft.uml.foundation.core.MStructuralFeature;
import ru.novosoft.uml.foundation.core.MUsage;
import ru.novosoft.uml.foundation.data_types.MActionExpression;
import ru.novosoft.uml.foundation.data_types.MAggregationKind;
import ru.novosoft.uml.foundation.data_types.MCallConcurrencyKind;
import ru.novosoft.uml.foundation.data_types.MChangeableKind;
import ru.novosoft.uml.foundation.data_types.MExpression;
import ru.novosoft.uml.foundation.data_types.MMultiplicity;
import ru.novosoft.uml.foundation.data_types.MParameterDirectionKind;
import ru.novosoft.uml.foundation.data_types.MProcedureExpression;
import ru.novosoft.uml.foundation.data_types.MPseudostateKind;
import ru.novosoft.uml.foundation.data_types.MScopeKind;
import ru.novosoft.uml.foundation.data_types.MVisibilityKind;
import ru.novosoft.uml.foundation.extension_mechanisms.MStereotype;
import ru.novosoft.uml.foundation.extension_mechanisms.MTaggedValue;
import ru.novosoft.uml.model_management.MElementImport;
import ru.novosoft.uml.model_management.MModel;
import ru.novosoft.uml.model_management.MPackage;
import ru.novosoft.uml.model_management.MSubsystem;

/**
 * Facade object for the Model component in ArgoUML.<p>
 *
 * The purpose of this Facade object is to allow for decoupling other modules
 * from the insides of the model. For this purpose all of the methods in this
 * class give away and accept handles (of type java.lang.Object) to the
 * objects within the model.<p>
 *
 * This is just getters and recognizers. This is because the Model
 * component has an extremely complicated internal data structure
 * with lots of internal dependencies. To manipulate these there is
 * a whole set of factories and helpers within the Model that is to
 * be used but to use them you need knowledge of the internals of
 * the Model, specifically the NS-UML objects.<p>
 *
 * All methods in this facade are static.<p>
 *
 * Signature for all recognizers in this Facade:
 * <ul>
 * <li>public static boolean isA<TYPE>(Object handle)
 * <li>public static boolean is<PROPERTY>(Object handle)
 * </ul>
 *
 * Signature for all getters in this Facade:
 * <ul>
 * <li>public static Object get<TYPE>(Object handle) - 1..1
 * <li>public static Iterator get<TYPES>(Object handle) - 0..*
 * <li>public static String getName(Object handle) - Name
 * </ul>
 *
 * @stereotype utility
 * @author Linus Tolke
 */
public class ModelFacade {
    ////////////////////////////////////////////////////////////////
    // constants

    public static final short ACC_PUBLIC = 1;
    public static final short ACC_PRIVATE = 2;
    public static final short ACC_PROTECTED = 3;

    public static final short CLASSIFIER_SCOPE = 1;
    public static final short INSTANCE_SCOPE = 2;

    public static final short GUARDED = 1;
    public static final short SEQUENTIAL = 2;

    // Types of line
    public static final Object ABSTRACTION      = MAbstraction.class;
    public static final Object ASSOCIATION      = MAssociation.class;
    public static final Object ASSOCIATION_CLASS = MAssociationClass.class;
    public static final Object ASSOCIATION_ROLE = MAssociationRole.class;
    public static final Object COLLABORATION    = MCollaboration.class;
    public static final Object DEPENDENCY       = MDependency.class;
    public static final Object EXTEND           = MExtend.class;
    public static final Object GENERALIZATION   = MGeneralization.class;
    public static final Object INCLUDE          = MInclude.class;
    public static final Object LINK             = MLink.class;
    public static final Object PERMISSION       = MPermission.class;
    public static final Object USAGE            = MUsage.class;
    public static final Object TRANSITION       = MTransition.class;

    // Types of node
    public static final Object ACTOR              = MActor.class;
    public static final Object CLASS              = MClass.class;
    public static final Object CLASSIFIER         = MClassifier.class;
    public static final Object CLASSIFIER_ROLE    = MClassifierRole.class;
    public static final Object COMPONENT          = MComponent.class;
    public static final Object COMPONENT_INSTANCE = MComponentInstance.class;
    public static final Object INSTANCE           = MInstance.class;
    public static final Object INTERFACE          = MInterface.class;
    public static final Object NODE               = MNode.class;
    public static final Object NODE_INSTANCE      = MNodeInstance.class;
    public static final Object OBJECT             = MObject.class;
    public static final Object PACKAGE            = MPackage.class;
    public static final Object MODEL              = MModel.class;
    public static final Object SUBSYSTEM          = MSubsystem.class;
    public static final Object STATE              = MState.class;
    public static final Object COMPOSITESTATE     = MCompositeState.class;
    public static final Object STATEVERTEX        = MStateVertex.class;
    public static final Object PSEUDOSTATE        = MPseudostate.class;
    public static final Object FINALSTATE         = MFinalState.class;
    public static final Object USE_CASE           = MUseCase.class;

    // Invisible model elements
    public static final Object ACTION             = MAction.class;
    public static final Object ACTION_EXPRESSION  = MActionExpression.class;
    public static final Object ACTION_STATE       = MActionState.class;
    public static final Object ASSOCIATION_END    = MAssociationEnd.class;
    public static final Object CALL_ACTION        = MCallAction.class;
    public static final Object NAMESPACE          = MNamespace.class;
    public static final Object RECEPTION          = MReception.class;
    public static final Object STEREOTYPE         = MStereotype.class;


    public static final Object ATTRIBUTE          = MAttribute.class;
    public static final Object OPERATION          = MOperation.class;

    public static final Object VISIBILITYKIND     = MVisibilityKind.class;

    public static final Object MODELELEMENT       = MModelElement.class;

    public static final Object INITIAL_PSEUDOSTATEKIND =
        MPseudostateKind.INITIAL;
    public static final Object DEEPHISTORY_PSEUDOSTATEKIND =
        MPseudostateKind.DEEP_HISTORY;
    public static final Object SHALLOWHISTORY_PSEUDOSTATEKIND =
        MPseudostateKind.SHALLOW_HISTORY;
    public static final Object FORK_PSEUDOSTATEKIND =
        MPseudostateKind.FORK;
    public static final Object JOIN_PSEUDOSTATEKIND =
        MPseudostateKind.JOIN;
    public static final Object BRANCH_PSEUDOSTATEKIND =
        MPseudostateKind.BRANCH;

    public static final Object PUBLIC_VISIBILITYKIND =
        MVisibilityKind.PUBLIC;
    public static final Object PRIVATE_VISIBILITYKIND =
        MVisibilityKind.PRIVATE;
    public static final Object PROTECTED_VISIBILITYKIND =
        MVisibilityKind.PROTECTED;


    /** Constructor that forbids instantiation.
     */
    private ModelFacade() {
    }

    ////////////////////////////////////////////////////////////////
    // Recognizer methods for the UML model (in alphabetic order)

    /** Recognizer for Abstraction.
     *
     * @param handle candidate
     * @returns true if handle is an Abstraction
     */
    public static boolean isAAbstraction(Object handle) {
        return handle instanceof MAbstraction;
    }

    /** Recognizer for Action.
     *
     * @param handle candidate
     * @returns true if handle is an Action
     */
    public static boolean isAAction(Object handle) {
        return handle instanceof MAction;
    }

    /**
     * Recognizer for ActionSequence
     * @param handle
     * @return
     */
    public static boolean isAActionSequence(Object handle) {
        return handle instanceof MActionSequence;
    }

    /**
     * Recognizer for Action state
     * @param handle
     * @return
     */
    public static boolean isAActionState(Object handle) {
        return handle instanceof MActionState;
    }

    /** Recognizer for Actor
     *
     * @param handle candidate
     * @returns true if handle is an Actor
     */
    public static boolean isAActor(Object handle) {
        return handle instanceof MActor;
    }


    /** Recognizer for Association.
     *
     * @param handle candidate
     * @returns true if handle is an Association
     */
    public static boolean isAAssociation(Object handle) {
        return handle instanceof MAssociation;
    }

    /** Recognizer for AssociationEnd.
     *
     * @param handle candidate
     * @returns true if handle is an AssociationEnd
     */
    public static boolean isAAssociationEnd(Object handle) {
        return handle instanceof MAssociationEnd;
    }

    /** Recognizer for AssociationRole
     *
     * @param handle candidate
     * @returns true if handle is an AssociationRole
     */
    public static boolean isAAssociationRole(Object handle) {
        return handle instanceof MAssociationRole;
    }

    /** Recognizer for AssociationEndRole
     *
     * @param handle candidate
     * @returns true if handle is an AssociationEndRole
     */
    public static boolean isAAssociationEndRole(Object handle) {
        return handle instanceof MAssociationEndRole;
    }

    /** Recognizer for Attribute
     *
     * @param handle candidate
     * @returns true if handle is an Attribute
     */
    public static boolean isAAttribute(Object handle) {
        return handle instanceof MAttribute;
    }

    /**
     * Recognizer for asynchronisity of an action
     * @param handle
     * @return
     */
    public static boolean isAsynchronous(Object handle) {
        if (handle instanceof MAction) {
            return ((MAction) handle).isAsynchronous();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for abstract classes and operations.
     *
     * @param handle candidate
     * @returns true if handle is abstract.
     */
    public static boolean isAbstract(Object handle) {
        if (handle instanceof MOperation)
            return ((MOperation) handle).isAbstract();
        if (handle instanceof MGeneralizableElement)
            return ((MGeneralizableElement) handle).isAbstract();
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for ActivityGraph
     *
     * @param handle candidate
     * @returns true if handle is ActivityGraph.
     */
    public static boolean isAActivityGraph(Object handle) {
        return handle instanceof MActivityGraph;
    }

    /** Recognizer for bases. A base is an object that is some form of
     *  an element in the model. MBase in Novosoft terms.
     *
     * @param handle candidate
     * @returns true if handle is abstract.
     */
    public static boolean isABase(Object handle) {
        return handle instanceof MBase;
    }

    /** Recognizer for BehavioralFeature
     *
     * @param handle candidate
     * @returns true if handle is a BehavioralFeature
     */
    public static boolean isABehavioralFeature(Object handle) {
        return handle instanceof MBehavioralFeature;
    }

    /** Recognizer for CallAction
     *
     * @param handle candidate
     * @returns true if handle is a CallAction
     */
    public static boolean isACallAction(Object handle) {
        return handle instanceof MCallAction;
    }

    /** Recognizer for CallEvent
     *
     * @param handle candidate
     * @returns true if handle is a CallEvent
     */
    public static boolean isACallEvent(Object handle) {
        return handle instanceof MCallEvent;
    }

    /** Recognizer for Class
     *
     * @param handle candidate
     * @returns true if handle is a Class
     */
    public static boolean isAClass(Object handle) {
        return handle instanceof MClass;
    }

    /** Recognizer for Classifier
     *
     * @param handle candidate
     * @returns true if handle is a Classifier
     */
    public static boolean isAClassifier(Object handle) {
        return handle instanceof MClassifier;
    }

    /** Recognizer for ClassifierRole
     *
     * @param handle candidate
     * @returns true if handle is a ClassifierRole
     */
    public static boolean isAClassifierRole(Object handle) {
        return handle instanceof MClassifierRole;
    }

    /** Recognizer for Comment
     *
     * @param handle candidate
     * @returns true if handle is a Comment
     */
    public static boolean isAComment(Object handle) {
        return handle instanceof MComment;
    }

    /** Recognizer for Collaboration
     *
     * @param handle candidate
     * @returns true if handle is a Collaboration
     */
    public static boolean isACollaboration(Object handle) {
        return handle instanceof MCollaboration;
    }

    /** Recognizer for Component
     *
     * @param handle candidate
     * @returns true if handle is a Component
     */
    public static boolean isAComponent(Object handle) {
        return handle instanceof MComponent;
    }

    /** Recognizer for ComponentInstance
     *
     * @param handle candidate
     * @returns true if handle is a ComponentInstance
     */
    public static boolean isAComponentInstance(Object handle) {
        return handle instanceof MComponentInstance;
    }

    /** Recognizer for Constraint
     *
     * @param handle candidate
     * @returns true if handle is a Constraint
     */
    public static boolean isAConstraint(Object handle) {
        return handle instanceof MConstraint;
    }

    /** Recognizer for CreateAction
     *
     * @param handle candidate
     * @returns true if handle is a CreateAction
     */
    public static boolean isACreateAction(Object handle) {
        return handle instanceof MCreateAction;
    }

    /** Recognizer for DataType
     *
     * @param handle candidate
     * @returns true if handle is a DataType
     */
    public static boolean isADataType(Object handle) {
        return handle instanceof MDataType;
    }

    /** Recognizer for DataValue
     *
     * @param handle candidate
     * @returns true if handle is a DataValue
     */
    public static boolean isADataValue(Object handle) {
        return handle instanceof MDataValue;
    }

    /** Recognizer for Dependency
     *
     * @param handle candidate
     * @returns true if handle is a Dependency
     */
    public static boolean isADependency(Object handle) {
        return handle instanceof MDependency;
    }

    /** Recognizer for DestroyAction
     *
     * @param handle candidate
     * @returns true if handle is a DestroyAction
     */
    public static boolean isADestroyAction(Object handle) {
        return handle instanceof MDestroyAction;
    }

    /** Recognizer for CompositeState
     *
     * @param handle candidate
     * @returns true if handle is a CompositeState
     */
    public static boolean isACompositeState(Object handle) {
        return handle instanceof MCompositeState;
    }

    /** Recognizer for Element
     *
     * @param handle candidate
     * @returns true if handle is an Element
     */
    public static boolean isAElement(Object handle) {
        return handle instanceof MElement;
    }

    /** Recognizer for ElementImport
     *
     * @param handle candidate
     * @returns true if handle is an ElementImport
     */
    public static boolean isAElementImport(Object handle) {
        return handle instanceof MElementImport;
    }

    /** Recognizer for ElementListener
     *
     * @param handle candidate
     * @returns true if handle is an ElementListener
     */
    public static boolean isAElementListener(Object handle) {
        return handle instanceof MElementListener;
    }

    /** Recognizer for ElementResidence
     *
     * @param handle candidate
     * @returns true if handle is an ElementResidence
     */
    public static boolean isAElementResidence(Object handle) {
        return handle instanceof MElementResidence;
    }

    /** Recognizer for Expression
     *
     * @param handle candidate
     * @returns true if handle is an Expression
     */
    public static boolean isAExpression(Object handle) {
        return handle instanceof MExpression;
    }

    /** Recognizer for Extend
     *
     * @param handle candidate
     * @returns true if handle is an Extend
     */
    public static boolean isAExtend(Object handle) {
        return handle instanceof MExtend;
    }


    /** Recognizer for ExtensionPoint
     *
     * @param handle candidate
     * @returns true if handle is an ExtensionPoint
     */
    public static boolean isAExtensionPoint(Object handle) {
        return handle instanceof MExtensionPoint;
    }


    /** Recognizer for Feature
     *
     * @param handle candidate
     * @returns true if handle is a Feature
     */
    public static boolean isAFeature(Object handle) {
        return handle instanceof MFeature;
    }

    /** Recognizer for FinalState
     *
     * @param handle candidate
     * @returns true if handle is a FinalState
     */
    public static boolean isAFinalState(Object handle) {
        return handle instanceof MFinalState;
    }

    /** Recognizer for Flow
     *
     * @param handle candidate
     * @returns true if handle is a Flow
     */
    public static boolean isAFlow(Object handle) {
        return handle instanceof MFlow;
    }

    /** Recognizer for Guard
     *
     * @param handle candidate
     * @returns true if handle is a Guard
     */
    public static boolean isAGuard(Object handle) {
        return handle instanceof MGuard;
    }

    /** Recognizer for GeneralizableElement
     *
     * @param handle candidate
     * @returns true if handle is a GeneralizableElement
     */
    public static boolean isAGeneralizableElement(Object handle) {
        return handle instanceof MGeneralizableElement;
    }

    /** Recognizer for GeneralizableElement
     *
     * @param handle candidate
     * @returns true if handle is a GeneralizableElement
     */
    public static boolean isAGeneralization(Object handle) {
        return handle instanceof MGeneralization;
    }

    /** Recognizer for Include
     *
     * @param handle candidate
     * @returns true if handle is an Include
     */
    public static boolean isAInclude(Object handle) {
        return handle instanceof MInclude;
    }


    /** Recognizer for Instance
     *
     * @param handle candidate
     * @returns true if handle is a Instance
     */
    public static boolean isAInstance(Object handle) {
        return handle instanceof MInstance;
    }


    /** Recognizer for Interaction
     *
     * @param handle candidate
     * @returns true if handle is a Interaction
     */
    public static boolean isAInteraction(Object handle) {
        return handle instanceof MInteraction;
    }

    /** Recognizer for Interface
     *
     * @param handle candidate
     * @returns true if handle is a Interface
     */
    public static boolean isAInterface(Object handle) {
        return handle instanceof MInterface;
    }

    /** Recognizer for Link
     *
     * @param handle candidate
     * @returns true if handle is a Link
     */
    public static boolean isALink(Object handle) {
        return handle instanceof MLink;
    }

    /** Recognizer for Message
     *
     * @param handle candidate
     * @returns true if handle is a Method
     */
    public static boolean isAMessage(Object handle) {
        return handle instanceof MMessage;
    }

    /** Recognizer for Method
     *
     * @param handle candidate
     * @returns true if handle is a Method
     */
    public static boolean isAMethod(Object handle) {
        return handle instanceof MMethod;
    }

    /** Recognizer for Model
     *
     * @param handle candidate
     * @returns true if handle is a Model
     */
    public static boolean isAModel(Object handle) {
        return handle instanceof MModel;
    }

    /** Recognizer for ModelElement
     *
     * @param handle candidate
     * @returns true if handle is a ModelElement
     */
    public static boolean isAModelElement(Object handle) {
        return handle instanceof MModelElement;
    }

    /** Recognizer for Multiplicity
     *
     * @param handle candidate
     * @returns true if handle is a Multiplicity
     */
    public static boolean isAMultiplicity(Object handle) {
        return handle instanceof MMultiplicity;
    }

    /** Recognizer for Namespace
     *
     * @param handle candidate
     * @returns true if handle is a Namespace
     */
    public static boolean isANamespace(Object handle) {
        return handle instanceof MNamespace;
    }

    /** Recognizer for a Node
     *
     * @param handle candidate
     * @returns true if handle is a Node
     */
    public static boolean isANode(Object handle) {
        return handle instanceof MNode;
    }

    /** Recognizer for a NodeInstance
     *
     * @param handle candidate
     * @returns true if handle is a NodeInstance
     */
    public static boolean isANodeInstance(Object handle) {
        return handle instanceof MNodeInstance;
    }

    /**
     * Recognizer for Operation
     *
     * @param handle candidate
     * @returns true if handle is an Operation
     */
    public static boolean isAOperation(Object handle) {
        return handle instanceof MOperation;
    }

    /**
     * Recognizer for Object
     *
     * @param handle candidate
     * @returns true if handle is an Object
     */
    public static boolean isAObject(Object handle) {
        return handle instanceof MObject;
    }

    /** Recognizer for Parameter
     *
     * @param handle candidate
     * @returns true if handle is a Parameter
     */
    public static boolean isAParameter(Object handle) {
        return handle instanceof MParameter;
    }

    /**
     * Recognizer for Permission
     *
     * @param handle candidate
     * @returns true if handle is an Permission
     */
    public static boolean isAPermission(Object handle) {
        return handle instanceof MPermission;
    }

    /** Recognizer for Package
     *
     * @param handle candidate
     * @returns true if handle is a Package
     */
    public static boolean isAPackage(Object handle) {
        return handle instanceof MPackage;
    }

    /** Recognizer for Pseudostate
     *
     * @param handle candidate
     * @returns true if handle is a Pseudostate
     */
    public static boolean isAPseudostate(Object handle) {
        return handle instanceof MPseudostate;
    }

    /** Recognizer for PseudostateKind
     *
     * @param handle candidate
     * @returns true if handle is a PseudostateKind
     */
    public static boolean isAPseudostateKind(Object handle) {
        return handle instanceof MPseudostateKind;
    }
    
    // TODO - Do we need this as well as getKind - I think not
    public static Object getPseudostateKind(Object handle) {
        if (handle instanceof MPseudostate) {
            return ((MPseudostate) handle).getKind();
        }
        throw new IllegalArgumentException("Unrecognized handle " + handle);
    }

    /**
     * Returns the receiver object of a stimulus
     * @param handle
     * @return
     */
    public static Object getReceiver(Object handle) {
        if (handle instanceof MStimulus) {
            return ((MStimulus) handle).getReceiver();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }
    
    public static Object getKind(Object handle) {
        if (handle instanceof MPseudostate) {
            return ((MPseudostate) handle).getKind();
        }
        if (handle instanceof MParameter) {
            return ((MParameter) handle).getKind();
        }
        throw new IllegalArgumentException("Unrecognized handle " + handle);
    }


    /** check whether two pseudostatekinds are equal/of the same type.
     */
    public static boolean equalsPseudostateKind(Object ps1, Object ps2) {
        return ((MPseudostateKind) ps1).equals(ps2);
    }

    /** Recognizer for Reception
     *
     * @param handle candidate
     * @returns true if handle is a Reception
     */
    public static boolean isAReception(Object handle) {
        return handle instanceof MReception;
    }

    /** Recognizer for Returnaction
     *
     * @param handle candidate
     * @returns true if handle is a returnaction
     */
    public static boolean isAReturnAction(Object handle) {
	return handle instanceof MReturnAction;
    }

    /** Recognizer for Relationship
     *
     * @param handle candidate
     * @returns true if handle is a Relationship
     */
    public static boolean isARelationship(Object handle) {
        return handle instanceof MRelationship;
    }

    /** Recognizer for SendAction
     *
     * @param handle candidate
     * @returns true if handle is a SendAction
     */
    public static boolean isASendAction(Object handle) {
        return handle instanceof MSendAction;
    }

    /** Recognizer for Signal
     *
     * @param handle candidate
     * @returns true if handle is a Signal
     */
    public static boolean isASignal(Object handle) {
        return handle instanceof MSignal;
    }

    /** Recognizer for StateMachine
     *
     * @param handle candidate
     * @returns true if handle is a StateMachine
     */
    public static boolean isAStateMachine(Object handle) {
        return handle instanceof MStateMachine;
    }

    /** Recognizer for stimulus
     *
     * @param handle candidate
     * @returns true if handle is a stimulus
     */
    public static boolean isAStimulus(Object handle) {
        return handle instanceof MStimulus;
    }

    /** Recognizer for StateVertex
     *
     * @param handle candidate
     * @returns true if handle is a StateVertex
     */
    public static boolean isAStateVertex(Object handle) {
        return handle instanceof MStateVertex;
    }

    /** Recognizer for Stereotype
     *
     * @param handle candidate
     * @returns true if handle is a Stereotype
     */
    public static boolean isAStereotype(Object handle) {
        return handle instanceof MStereotype;
    }

    /** Recognizer for StructuralFeature
     *
     * @param handle candidate
     * @returns true if handle is a StructuralFeature
     */
    public static boolean isAStructuralFeature(Object handle) {
        return handle instanceof MStructuralFeature;
    }

    /** Recognizer for State
     *
     * @param handle candidate
     * @returns true if handle is a State
     */
    public static boolean isAState(Object handle) {
        return handle instanceof MState;
    }

    /** Recognizer for Subsystem
     *
     * @param handle candidate
     * @returns true if handle is a Subsystem
     */
    public static boolean isASubsystem(Object handle) {
        return handle instanceof MSubsystem;
    }

    /** Recognizer for TaggedValue
     *
     * @param handle candidate
     * @returns true if handle is a TaggedValue
     */
    public static boolean isATaggedValue(Object handle) {
        return handle instanceof MTaggedValue;
    }


    /** Recognizer for Transition
     *
     * @param handle candidate
     * @returns true if handle is a Transition
     */
    public static boolean isATransition(Object handle) {
        return handle instanceof MTransition;
    }

    /** Recognizer for Usage
     *
     * @param handle candidate
     * @returns true if handle is a Usage
     */
    public static boolean isAUsage(Object handle) {
        return handle instanceof MUsage;
    }

    /** Recognizer for a Use Case
     *
     * @param handle candidate
     * @returns true if handle is a Transition
     */
    public static boolean isAUseCase(Object handle) {
        return handle instanceof MUseCase;
    }

    /** Recognizer for VisibilityKind
     *
     * @param handle candidate
     * @returns true if handle is a VisibilityKind
     */
    public static boolean isAVisibilityKind(Object handle) {
        return handle instanceof MVisibilityKind;
    }

    /** Recognizer for attributes that are changeable
     *
     * @param handle candidate
     * @returns true if handle is changeable
     */
    public static boolean isChangeable(Object handle) {
        if (handle != null && handle instanceof MAttribute) {
	    MChangeableKind changeability =
		((MAttribute) handle).getChangeability();
            return MChangeableKind.CHANGEABLE.equals(changeability);

        } else if (handle != null && handle instanceof MAssociationEnd) {
	    MChangeableKind changeability =
		((MAssociationEnd) handle).getChangeability();
            return MChangeableKind.CHANGEABLE.equals(changeability);
        }
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for attributes with classifier scope.
     *
     * @param handle candidate
     * @returns true if handle has classifier scope.
     */
    public static boolean isClassifierScope(Object handle) {
        if (handle instanceof MAttribute) {
            MAttribute a = (MAttribute) handle;
            return MScopeKind.CLASSIFIER.equals(a.getOwnerScope());
        }
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for constructor.
     *
     * @param handle candidate
     * @returns true if handle is a constructor.
     */
    public static boolean isConstructor(Object handle) {
	if (isAOperation(handle)) {
	    if (ExtensionMechanismsHelper.getHelper()
		.isStereotypeInh(getStereoType(handle),
				 "create",
				 "BehavioralFeature"))
		return true;
	}
	if (isAMethod(handle)) {
	    if (ExtensionMechanismsHelper.getHelper()
		.isStereotypeInh(getStereoType(CoreHelper.getHelper()
					       .getSpecification(handle)),
				 "create",
				 "BehavioralFeature"))
		return true;
	}
	return false;
    }

    /**
     * Returns true if a given associationend is a composite.
     * @param handle
     * @return boolean
     */
    public static boolean isComposite(Object handle) {
        if (isAAssociationEnd(handle)) {
            boolean composite = false;
            MAssociationEnd end = (MAssociationEnd) handle;
            if (end.getAggregation() != null
                && end.getAggregation().equals(MAggregationKind.COMPOSITE))
                composite = true;
            return composite;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for attributes that are initialized.
     *
     * @param handle candidate
     * @param true if the attribute is initialized.
     */
    public static boolean isInitialized(Object handle) {
        if (handle instanceof MAttribute) {
            MExpression init = ((MAttribute) handle).getInitialValue();

            if (init != null
                && init.getBody() != null
                && init.getBody().trim().length() > 0)
                return true;
            return false;
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for attributes with instance scope.
     *
     * @param handle candidate
     * @returns true if handle has instance scope.
     */
    public static boolean isInstanceScope(Object handle) {
        if (handle instanceof MFeature) {
            MFeature a = (MFeature) handle;
            return MScopeKind.INSTANCE.equals(a.getOwnerScope());
        }
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for leafs
     *
     * @param handle candidate GeneralizableElement
     * @returns true if handle is a leaf
     */
    public static boolean isLeaf(Object handle) {

        if (handle instanceof MGeneralizableElement) {
            return ((MGeneralizableElement) handle).isLeaf();
        }
        if (handle instanceof MOperation) {
            return ((MOperation) handle).isLeaf();
        }
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for Navigable elements
     *
     * @param handle candidate
     * @returns true if handle is navigable
     */
    public static boolean isNavigable(Object handle) {
        if (handle instanceof MAssociationEnd) {
            return ((MAssociationEnd) handle).isNavigable();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for primary objects.
     * A primary object is an object that is created by the parser or
     * by a user.
     * Object that are created when importing some other object are not.
     *
     * @param handle candidate
     * @returns true if primary object.
     */
    public static boolean isPrimaryObject(Object handle) {
        if (handle instanceof MModelElement) {
            MModelElement element = (MModelElement) handle;
            for (Iterator i = element.getTaggedValues().iterator();
		 i.hasNext();
		 ) {
                MTaggedValue tv = (MTaggedValue) i.next();
                if ((MMUtil.GENERATED_TAG).equals(tv.getTag())) {
                    return false;
                }
            }
            return true;
        }
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for attributes with private
     *
     * @param handle candidate
     * @returns true if handle has private
     */
    public static boolean isPrivate(Object handle) {
        if (handle instanceof MModelElement) {
            MModelElement element = (MModelElement) handle;
            return MVisibilityKind.PRIVATE.equals(element.getVisibility());
        }
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for attributes with public
     *
     * @param handle candidate
     * @returns true if handle has public
     */
    public static boolean isPublic(Object handle) {
        if (handle instanceof MModelElement) {
            MModelElement element = (MModelElement) handle;
            return MVisibilityKind.PUBLIC.equals(element.getVisibility());
        }
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Recognizer for MBehaviouralFeature's that are queries.
     */
    public static boolean isQuery(Object handle){
        
        if (!(handle instanceof MBehavioralFeature)) {
            throw new IllegalArgumentException();
        }
        
        return ((MBehavioralFeature)handle).isQuery();
    }
    
    /** Recognizer for attributes with protected
     *
     * @param handle candidate
     * @returns true if handle has protected
     */
    public static boolean isProtected(Object handle) {
        if (handle instanceof MModelElement) {
            MModelElement element = (MModelElement) handle;
            return MVisibilityKind.PROTECTED.equals(element.getVisibility());
        }
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for realize
     *
     * @param handle candidate
     * @returns true if handle has a realize stereotype
     */
    public static boolean isRealize(Object handle) {
        return isStereotype(handle, "realize");
    }

    /** Recognizer for return
     *
     * @param handle candidate parameter
     * @returns true if handle is a return parameter.
     */
    public static boolean isReturn(Object handle) {
        if (handle instanceof MParameter) {
            MParameter p = (MParameter) handle;
            return MParameterDirectionKind.RETURN.equals(p.getKind());
            
        }
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Recognizer for singleton.
     *
     * @param handle candidate
     * @returns true if handle is a singleton.
     */
    public static boolean isSingleton(Object handle) {
        return isStereotype(handle, "singleton");
    }

    /** Recognizer for a specific stereotype.
     *
     * @param handle candidate
     * @param stereotype a string that is the stereotype name.
     * @return true if handle is a singleton.
     * @deprecated As of ArgoUml version 0.13.5, {@link
     * org.argouml.model.uml.foundation.extensionmechanisms.ExtensionMechanismsHelper#isStereotype(Object,String,String)}
     *             should be used instead. Since this should only ever
     *             be used together with predefined stereotypes the
     *             base class can *be found in the UML 1.3
     *             specification.
     */
    public static boolean isStereotype(Object handle, String stereotypename) {
        if (handle instanceof MModelElement) {
            MModelElement element = (MModelElement) handle;
            MStereotype meSt = element.getStereotype();

            if (meSt == null)
                return false;

            String name = meSt.getName();
            if (name == null)
                return false;

            return name.equalsIgnoreCase(stereotypename);
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    public static boolean isTop(Object handle) {
        return isACompositeState(handle)
            && ((MCompositeState) handle).getStateMachine() != null;
    }

    /** Recognizer for type.
     *
     * @param handle candidate
     * @returns true if handle is a type.
     */
    public static boolean isType(Object handle) {
        return isStereotype(handle, "type");
    }

    /** Recognizer for utility.
     *
     * @param handle candidate
     * @returns true if handle is a utility.
     */
    public static boolean isUtility(Object handle) {
        return isStereotype(handle, "utility");
    }

    ////////////////////////////////////////////////////////////////
    // Recognizer methods for the diagrams (in alphabetic order)

    /** Recognizer for Diagram.
     *
     * @param handle candidate
     * @returns true if handle is a diagram.
     */
    public static boolean isADiagram(Object handle) {
        return handle instanceof Diagram;
    }

    ////////////////////////////////////////////////////////////////
    // Getters for the UML model (in alphabetic order)

    /**
     * Returns the association end between some classifier and some associaton.
     * @param type
     * @param assoc
     * @return association end
     */
    public static Object getAssociation(Object end) {
        
        if (end instanceof MAssociationEnd) {
            return ((MAssociationEnd)end).getAssociation();
        }
        throw new IllegalArgumentException("Unrecognized object " + end);
    }
    
    /**
     * Returns the association end between some classifier and some associaton.
     * @param type
     * @param assoc
     * @return association end
     */
    public static Object getAssociationEnd(Object type, Object assoc) {
        if (type == null
            || assoc == null
            || !(type instanceof MClassifier)
            || !(assoc instanceof MAssociation))
            return null;
        Iterator it = ((MClassifier) type).getAssociationEnds().iterator();
        while (it.hasNext()) {
            MAssociationEnd end = (MAssociationEnd) it.next();
            if (((MAssociation) assoc).getConnections().contains(end))
                return end;
        }
        return null;
    }

    /** The list of Association Ends
     *
     * @param handle the object that we get the association ends from.
     * @return Iterator with association ends.
     */
    public static Collection getAssociationEnds(Object handle) {
        if (handle instanceof MClassifier) {
            Collection endc = ((MClassifier) handle).getAssociationEnds();
            return endc;
        }

        //...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** The list of association roles
     *
     * @param handle the object that we get the association roles from.
     * @return Collection of association roles.
     */
    public static Collection getAssociationRoles(Object handle) {
        if (handle instanceof MAssociation) {
            return ((MAssociation) handle).getAssociationRoles();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** The list of Attributes.
     *
     * @param handle classifier to examine.
     * @return iterator with attributes.
     */
    public static Collection getAttributes(Object handle) {
        if (handle instanceof MClassifier) {
            MClassifier c = (MClassifier) handle;
            // TODO: We are converting back and forth between collections and
            // iterators. I (Linus) prefer iterators.
            //return getStructuralFeatures(c).iterator();
            //...But I (thn) got CVS conflicts, so:
            return getStructuralFeatures(c);
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * The baseclass of some stereotype
     * @param handle the stereotype
     * @return the baseclass
     */
    public static Object getBaseClass(Object handle) {
        if (isAStereotype(handle)) {
            return ((MStereotype) handle).getBaseClass();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * The base of some model element
     * @param handle the model element
     * @return the base
     */
    public static Object getBase(Object handle) {
        if (handle instanceof MAssociationEndRole) {
            return ((MAssociationEndRole) handle).getBase();
        } else if (handle instanceof MAssociationRole) {
            return ((MAssociationRole) handle).getBase();
        } else if (handle instanceof MExtend) {
            return ((MExtend) handle).getBase();
        } else if (handle instanceof MInclude) {
            return ((MInclude) handle).getBase();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }
    
    
    /** Get the bases of a classifier role.
     *
     *
     * @param handle classifier role.
     * @return the bases.
     */
    public static Collection getBases(Object handle) {
        if (handle instanceof MClassifierRole)
            return ((MClassifierRole) handle).getBases();
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the behaviors of a Modelelement.
     *
     *
     * @param handle modelelement to examine.
     * @return the behaviors.
     */
    public static Collection getBehaviors(Object handle) {
        if (isAModelElement(handle))
            return ((MModelElement) handle).getBehaviors();
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the body of an Expression.
     *
     *
     * @param handle expression.
     * @return the body.
     */
    public static Object getBody(Object handle) {
        if (handle instanceof MExpression)
            return ((MExpression) handle).getBody();
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the child of a generalization.
     *
     * TODO: Check that the concepts parent and child exist in the UML model.
     *
     * @param handle generalization.
     * @return the child.
     */
    public static Object getChild(Object handle) {
        if (handle instanceof MGeneralization) {
            return ((MGeneralization) handle).getChild();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Get the children of some generalizable element
     *
     * @param handle to the generalizable element.
     * @return a collection with all children.
     */
    public static Collection getChildren(Object handle) {
        if (isAGeneralizableElement(handle)) {
            return ((MGeneralizableElement) handle).getChildren();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Gets the clients of some dependency
     * @param handle the dependency
     * @return the clients of the dependency
     */
    public static Collection getClients(Object handle) {
        if (isADependency(handle)) {
            return ((MDependency) handle).getClients();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Get the client dependencies of some classifier
     *
     * @param handle to the classifier.
     * @return an iterator with all client dependencies.
     */
    public static Collection getClientDependencies(Object handle) {
        if (isAModelElement(handle)) {
            Collection c = ((MModelElement) handle).getClientDependencies();
            return c;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the concurrency of an operation.
     *
     * @param o operation.
     * @return the concurrency.
     */
    public static short getConcurrency(Object o) {
        if (o != null && o instanceof MOperation) {
            return ((MOperation) o).getConcurrency()
                == MCallConcurrencyKind.GUARDED
                ? GUARDED
                : SEQUENTIAL;
        }
        throw new IllegalArgumentException("Unrecognized object " + o);
    }

    /** The list of Connections or AssociationEnds to an Association.
     *
     * @param handle to the association.
     * @return a Collection with all connections.
     */
    public static Collection getConnections(Object handle) {
        if (handle instanceof MAssociation) {
            return ((MAssociation) handle).getConnections();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Determine if a model element contains a connection
     *
     * @param handle to the association.
     * @return true if the model element contains a connection
     */
    public boolean containsConnection(Object element, Object connection) {
        if (element instanceof MAssociation) {
            return ((MAssociation) element).getConnections().contains(connection);
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + element);
    }

    /** Count the number of Connections or AssociationEnds to an Association.
     *
     * @param handle to the association.
     * @return an Iterator with all connections.
     */
    public static int getConnectionCount(Object handle) {
        if (handle instanceof MAssociation) {
            return ((MAssociation) handle).getConnections().size();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the effect of some transition
     * @param handle
     * @return
     */
    public static Object getEffect(Object handle) {
        if (handle instanceof MTransition) {
            return ((MTransition) handle).getEffect();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the residences of an element.
     *
     * @param handle the model element that we are getting the residences of
     * @returns the residence collection
     */
    public static Collection getElementResidences(Object handle) {
        if (handle instanceof MModelElement)
            return ((MModelElement) handle).getElementResidences();
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the entry action to a state
     * @param handle
     * @return
     */
    public static Object getEntry(Object handle) {
        if (handle instanceof MState) {
            return ((MState) handle).getEntry();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the exit action to a state
     * @param handle
     * @return
     */
    public static Object getExit(Object handle) {
        if (handle instanceof MState) {
            return ((MState) handle).getExit();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** The list of Features from a Classifier.
     *
     * @param handle Classifier to retrieve from.
     * @return Collection with Features
     */
    public static Collection getFeatures(Object handle) {
        if (handle != null && handle instanceof MClassifier)
            return ((MClassifier) handle).getFeatures();
        return new ArrayList();
    }

    /**
     * Gets the generalization between two generalizable elements.
     * Returns null if there is none.
     * @param child
     * @param parent
     * @return The generalization
     */
    public static Object getGeneralization(Object child, Object parent) {
        if (child == null
            || parent == null
            || !(child instanceof MGeneralizableElement)
            || !(parent instanceof MGeneralizableElement))
            return null;
        Iterator it = getGeneralizations(child);
        while (it.hasNext()) {
            MGeneralization gen = (MGeneralization) it.next();
            if (gen.getParent() == parent) {
                return gen;
            }
        }
        return null;
    }

    /** The list of Generalizations from a GeneralizableElement.
     *
     * @param handle GeneralizableElement to retrieve from.
     * @return Iterator with Generalizations
     */
    public static Iterator getGeneralizations(Object handle) {
        if (handle instanceof MGeneralizableElement) {
            MGeneralizableElement ge = (MGeneralizableElement) handle;
            return ge.getGeneralizations().iterator();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Gets the guard for some given transition.
     * @param handle
     * @return Object
     */
    public static Object getGuard(Object handle) {
        if (isATransition(handle)) {
            return ((MTransition) handle).getGuard();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the incoming transitions for some statevertex
     * @param handle
     * @return Collection
     */
    public static Collection getIncomings(Object handle) {
        if (isAStateVertex(handle)) {
            return ((MStateVertex) handle).getIncomings();
        }
        throw new IllegalArgumentException(
					   "Unrecognized object " + handle);
    }

    /**
     * Returns the interaction for some message
     * @param handle
     * @return
     */
    public static Object getInteraction(Object handle) {
        if (handle instanceof MMessage) {
            return ((MMessage) handle).getInteraction();
        }
        throw new IllegalArgumentException(
					   "Unrecognized object " + handle);
    }

    /**
     * Returns the messages belonging to some interaction
     * @param handle
     * @return Collection
     */
    public static Collection getMessages(Object handle) {
        if (isAInteraction(handle)) {
            return ((MInteraction) handle).getMessages();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Get the model of some model element
     *
     * @param handle to the model element.
     * @return model for the model element.
     */
    public static Object getModel(Object handle) {
        if (isAModelElement(handle)) {
            MModel m = ((MModelElement) handle).getModel();
            return m;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * @param elemImport an Element Import.
     */
    public static Object getModelElement(Object elemImport){
        
        if(!(elemImport instanceof MElementImport))
            throw new IllegalArgumentException();
        
        return ((MElementImport) elemImport).getModelElement();
    }
    
    /** Get the Multiplicity from an AssociationEnd.
     *
     * @param handle Classifier to retrieve from.
     * @return Collection with Features
     */
    public static Object getMultiplicity(Object handle) {
        if (!(handle instanceof MAssociationEnd))
            throw new IllegalArgumentException("Unrecognized object " + handle);
        return ((MAssociationEnd) handle).getMultiplicity();
    }

    /** Get the comments of an element.
     *
     * @param handle the model element that we are getting the comments of
     * @returns the comment (or null)
     */
    public static Collection getComments(Object handle) {
        if (handle instanceof MModelElement)
            return ((MModelElement) handle).getComments();
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the comments of an element.
     *
     * @param handle the model element that we are getting the comments of
     * @returns the comment (or null)
     */
    public static Collection getCollaborations(Object handle) {
        if (handle instanceof MOperation)
            return ((MOperation) handle).getCollaborations();
        if (handle instanceof MClassifier)
            return ((MClassifier) handle).getCollaborations();
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Add a new comment to a model element
     * @param element the element to which the comment is to be added
     * @param comment the comment for the model element
     */
    public static void addComment(Object element, Object comment) {
        if (element instanceof MModelElement && comment instanceof MComment) {
            ((MModelElement) element).addComment((MComment)comment);
        }
        throw new IllegalArgumentException("Unrecognized object " + element);
    }
    
    public static Collection getConstraints(Object handle) {
        if (handle instanceof MModelElement) {
            return ((MModelElement) handle).getConstraints();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }
    
    /**
     * Returns the container for the given modelelement. The container is the
     * owner of the modelelement. It will be null for elements that don't have
     * an owner. All elements except for the root element in a project should
     * have an owner. The root element is allways a model.
     * <p> In  the future, this function could return the container of Figs too.
     * </p>
     * @param handle
     * @return Object
     */
    public static Object getContainer(Object handle) {
        if (handle instanceof MBase) {
            return ((MBase) handle).getModelElementContainer();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the context of some given statemachine or the context
     * of some given interaction
     * @param handle the statemachine or the interaction
     * @return the context of the statemachine or interaction or null
     * if the statemachine or interaction doesn't have a context.
     */
    public static Object getContext(Object handle) {
        if (isAStateMachine(handle)) {
            return ((MStateMachine) handle).getContext();
        }
        if (isAInteraction(handle)) {
            return ((MInteraction) handle).getContext();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the context of some given statemachine or the context
     * of some given interaction
     * @param handle the statemachine or the interaction
     * @return the context of the statemachine or interaction or null
     * if the statemachine or interaction doesn't have a context.
     */
    public static Collection getDeploymentLocations(Object handle) {
        if (isAComponent(handle)) {
            return ((MComponent) handle).getDeploymentLocations();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the dispatchaction of a stimulus.
     *
     * @param handle the stimulus that we are getting the dispatchaction of
     * @returns the dispatchaction (or null)
     */
    public static Object getDispatchAction(Object handle) {
        if (handle instanceof MStimulus) {
            return ((MStimulus) handle).getDispatchAction();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }
   
    /**
     * Returns the do activity action of a state
     * @param handle
     * @return
     */
    public static Object getDoActivity(Object handle) {
        if (handle instanceof MState) {
            return ((MState) handle).getDoActivity();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Gets a location of some extension point.
     * @param extension point
     * @returns the location
     */
    public static String getLocation(Object handle) {
        if (handle instanceof MExtensionPoint) {
            return ((MExtensionPoint) handle).getLocation();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the namespace of an element.
     *
     * @param handle the model element that we are getting the namespace of
     * @returns the namespace (or null)
     */
    public static Object getNamespace(Object handle) {
        if (handle instanceof MModelElement)
            return ((MModelElement) handle).getNamespace();
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the node instance of a component instance.
     *
     * @param handle the model element that we are getting the node instance of
     * @returns the node instance
     */
    public static Object getNodeInstance(Object handle) {
        if (handle instanceof MComponentInstance)
            return ((MComponentInstance) handle).getNodeInstance();
        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** The list of operations
     *
     * @param handle classifier to examine.
     * @return Collection with operations.
     */
    public static Collection getOperations(Object handle) {
        if (handle instanceof MClassifier) {
            MClassifier c = (MClassifier) handle;

            return getOperations(c);
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** The list of Operations of this classifier and all inherited.
     *
     * @param handle classifier to examine.
     * @return Iterator with operations.
     */
    public static Iterator getOperationsInh(Object handle) {
        if (handle instanceof MClassifier) {
            MClassifier c = (MClassifier) handle;

            // TODO: We are converting back and forth between collections and
            // iterators. I (Linus) prefer iterators.
            return CoreHelper.getHelper().getOperationsInh(c).iterator();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the opposite end of an association end.
     * @param handle
     * @return Object the opposite end.
     */
    public static Object getOppositeEnd(Object handle) {
        if (handle instanceof MAssociationEnd) {
            return ((MAssociationEnd) handle).getOppositeEnd();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Returns the list of Transitions outgoing from the given stateVertex.
     *
     * @param statevertex
     * @return Collection
     */
    public static Collection getOutgoings(Object handle) {
        if (ModelFacade.isAStateVertex(handle)) {
            return ((MStateVertex) handle).getOutgoings();
        }
        throw new IllegalArgumentException(
					   "Unrecognized object " + handle);
    }

    /** The list of Associations Ends connected to this association end
     *
     * @param handle association end to start from
     * @returns Iterator with all connected association ends.
     */
    public static Collection getOtherAssociationEnds(Object handle) {
        if (handle instanceof MAssociationEnd) {
            MAssociation a = ((MAssociationEnd) handle).getAssociation();

            if (a == null)
                return emptyCollection();

            Collection allEnds = a.getConnections();
            if (allEnds == null)
                return emptyCollection();

            // TODO: An Iterator filter would be nice here instead of the
            // mucking around with the Collection.
            allEnds = new ArrayList(allEnds);
            allEnds.remove(handle);
            return allEnds;
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** The list of owned elements of the the package.
     *
     * @param handle package to retrieve from.
     * @return Iterator with operations
     */
    public static Collection getOwnedElements(Object handle) {
        if (handle instanceof MNamespace) {
            return ((MNamespace) handle).getOwnedElements();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Determine if the passed parameter has a RETURN direction kind
     */
    public static boolean hasReturnParameterDirectionKind(Object o) {
        MParameter parameter = (MParameter)o;
        return (parameter.getKind().equals(MParameterDirectionKind.RETURN));
    }

    /** Get a parameter of a behavioral feature.
     *
     * @param handle behavioral feature to retrieve from
     * @param n parameter number
     * @return parameter.
     */
    public static Object getParameter(Object handle, int n) {
        if (handle instanceof MBehavioralFeature)
            return ((MBehavioralFeature) handle).getParameter(n);

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the parameters of a behavioral feature.
     *
     * @param handle operation to retrieve from
     * @return Iterator with operations.
     */
    public static Collection getParameters(Object handle) {
        if (handle instanceof MBehavioralFeature) {
            return ((MBehavioralFeature) handle).getParameters();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** Get the parent of a generalization.
     *
     * TODO: Check that the concepts parent and child exist in the UML model.
     *
     * @param handle generalization.
     * @return the parent.
     */
    public static Object getParent(Object handle) {
        if (handle instanceof MGeneralization) {
            return ((MGeneralization) handle).getParent();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** returns the receptions of a signal
     * @param handle
     * @return receptions
     */
    public static Collection getReceptions(Object handle) {
        if (handle instanceof MSignal) {
            return ((MSignal) handle).getReceptions();
        }
        throw new IllegalArgumentException("Unrecognized handle: + handle");
    }

    /**
     * Returns the recurense iteration expression of an action
     * @param handle
     * @return
     */
    public static Object getRecurrence(Object handle) {
        if (handle instanceof MAction) {
            return ((MAction) handle).getRecurrence();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the script belonging to a given action
     * @param handle
     * @return
     */
    public static Object getScript(Object handle) {
        if (handle instanceof MAction) {
            return ((MAction) handle).getScript();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the sender object of a stimulus
     * @param handle
     * @return
     */
    public static Object getSender(Object handle) {
        if (handle instanceof MStimulus) {
            return ((MStimulus) handle).getSender();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }
    
    /**
     * Returns a collection with all residents belonging to the given
     * node.
     * @param handle
     * @return Collection
     */
    public static Collection getResidents(Object handle) {
        if (isANode(handle)) {
            return ((MNode) handle).getResidents();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Gets the source for some given transitions.
     * @param handle
     * @return Object
     */
    public static Object getSource(Object handle) {
        if (isATransition(handle)) {
            return ((MTransition) handle).getSource();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the sourceflows of a model element
     * @param handle
     * @return a collection of sourceflows
     */
    public static Collection getSourceFlows(Object handle) {
        if (handle instanceof MModelElement) {
            return ((MModelElement) handle).getSourceFlows();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** The list of Specializations from a GeneralizableElement.
     *
     * @param handle GeneralizableElement to retrieve from.
     * @return Iterator with Specializations.
     */
    public static Iterator getSpecializations(Object handle) {
        if (handle instanceof MGeneralizableElement) {
            MGeneralizableElement ge = (MGeneralizableElement) handle;
            return ge.getSpecializations().iterator();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the state machine belonging to some given state
     * @param handle
     * @return Object
     */
    public static Object getStateMachine(Object handle) {
        if (handle instanceof MState) {
            return ((MState) handle).getStateMachine();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the stereotype belonging to some given modelelement
     * @param handle
     * @return Object
     */
    public static Object getStereoType(Object handle) {
        if (isAModelElement(handle)) {
            return ((MModelElement) handle).getStereotype();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the stereotype belonging to some given modelelement
     * @param handle
     * @return Object
     */
    public static Object getStereotype(Object handle) {
        if (isAModelElement(handle)) {
            return ((MModelElement) handle).getStereotype();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the stimuli belonging to some given link
     * @param handle
     * @return Object
     */
    public static Collection getStimuli(Object handle) {
        if (isALink(handle)) {
            return ((MLink) handle).getStimuli();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }
    
    /**
     * Returns a collection with all subvertices belonging to the given
     * composite state.
     * @param handle
     * @return Collection
     */
    public static Collection getSubvertices(Object handle) {
        if (isACompositeState(handle)) {
            return ((MCompositeState) handle).getSubvertices();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** The list of SupplierDependencies from a ModelElement.
     *
     * @param handle model element.
     * @returns Iterator with the supplier dependencies.
     */
    public static Collection getSupplierDependencies(Object handle) {
        if (handle instanceof MModelElement) {
            MModelElement me = (MModelElement) handle;
            return me.getSupplierDependencies();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** The type of an attribute
     *
     * @param handle the attribute
     * @returns the type
     */
    public static Object getType(Object handle) {
        if (handle instanceof MStructuralFeature) {
            return ((MAttribute) handle).getType();
        }
        if (handle instanceof MAssociationEnd) {
            return ((MAssociationEnd) handle).getType();
        }
        if (handle instanceof MParameter) {
            return ((MParameter) handle).getType();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the target of some transition
     * @param handle
     * @return Object
     */
    public static Object getTarget(Object handle) {
        if (isATransition(handle)) {
            return ((MTransition) handle).getTarget();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the target scope of some model element
     * @param handle
     * @return Object
     */
    public static Object getTargetScope(Object handle) {
        if (handle instanceof MStructuralFeature) {
            return ((MStructuralFeature) handle).getTargetScope();
        }
        if (handle instanceof MAssociationEnd) {
            return ((MAssociationEnd) handle).getTargetScope();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the targetflows of a model element
     * @param handle
     * @return a collection of targetflows
     */
    public static Collection getTargetFlows(Object handle) {
        if (handle instanceof MModelElement) {
            return ((MModelElement) handle).getTargetFlows();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the upper bound of the multiplicity of the given handle (an
     * associationend).
     * @param handle
     * @return int
     */
    public static int getUpper(Object handle) {
        if (isAAssociationEnd(handle)) {
            int upper = 0;
            MAssociationEnd end = (MAssociationEnd) handle;
            if (end.getMultiplicity() != null)
                upper = end.getMultiplicity().getUpper();
            return upper;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the upper bound of the multiplicity of the given handle (an
     * associationend).
     * @param handle
     * @return int
     */
    public static int getLower(Object handle) {
        if (isAAssociationEnd(handle)) {
            int lower = 0;
            MAssociationEnd end = (MAssociationEnd) handle;
            if (end.getMultiplicity() != null)
                lower = end.getMultiplicity().getLower();
            return lower;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }
    
    /**
     * Returns the transitions belonging to the given handle. The handle can be
     * a statemachine or a composite state. If it's a statemachine the
     * transitions will be given back belonging to that statemachine. If it's a
     * compositestate the internal transitions of that compositestate will be
     * given back.
     * @param handle
     * @return Collection
     */
    public static Collection getTransitions(Object handle) {
        if (isAStateMachine(handle)) {
            return ((MStateMachine) handle).getTransitions();
        } else if (isACompositeState(handle)) {
            return ((MCompositeState) handle).getInternalTransitions();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /** This method returns all attributes of a given Classifier.
     *
     * @param classifier the classifier you want to have the attributes for
     * @return a collection of the attributes
     */
    public static Collection getStructuralFeatures(Object classifier) {
        Collection result = new ArrayList();
        if (ModelFacade.isAClassifier(classifier)) {
            MClassifier mclassifier = (MClassifier) classifier;

            Iterator features = mclassifier.getFeatures().iterator();
            while (features.hasNext()) {
                MFeature feature = (MFeature) features.next();
                if (ModelFacade.isAStructuralFeature(feature))
                    result.add(feature);
            }
        }
        return result;
    }

    /** This method returns all operations of a given Classifier
     *
     * @param classifier the classifier you want to have the operations for
     * @return a collection of the operations
     */
    public static Collection getOperations(MClassifier mclassifier) {
        Collection result = new ArrayList();
        Iterator features = mclassifier.getFeatures().iterator();
        while (features.hasNext()) {
            MFeature feature = (MFeature) features.next();
            if (ModelFacade.isAOperation(feature))
                result.add(feature);
        }
        return result;
    }

    /**
     * Returns all Interfaces of which this class is a realization.
     * @param cls  the class you want to have the interfaces for
     * @return a collection of the Interfaces
     */
    public static Collection getSpecifications(Object cls) {
        Collection result = new Vector();
        if (cls instanceof MClassifier) {
            Collection deps = ((MClassifier) cls).getClientDependencies();
            Iterator depIterator = deps.iterator();
            while (depIterator.hasNext()) {
                MDependency dep = (MDependency) depIterator.next();
                if ((dep instanceof MAbstraction)
                    && dep.getStereotype() != null
                    && dep.getStereotype().getName() != null
                    && dep.getStereotype().getName().equals("realize")) {
                    MInterface i = (MInterface) dep.getSuppliers().toArray()[0];
                    result.add(i);
                }
            }
        }
        return result;
    }

    /**
     * Returns the suppliers of an abstraction.
     * @param abstraction
     * @return a collection of the suppliers
     */
    public static Collection getSuppliers(Object handle) {
        if (handle instanceof MAbstraction) {
            return ((MAbstraction) handle).getSuppliers();
		}
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the action belonging to some message
     * @param handle
     * @return
     */
    public static Object getAction(Object handle) {
        if (handle instanceof MMessage) {
            return ((MMessage) handle).getAction();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the activator belonging to some message
     * @param handle
     * @return
     */
    public static Object getActivator(Object handle) {
        if (handle instanceof MMessage) {
            return ((MMessage) handle).getActivator();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns the actual arguments for a given action.
     * @param handle
     * @return
     */
    public static Collection getActualArguments(Object handle) {
        if (handle instanceof MAction) {
            return ((MAction) handle).getActualArguments();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns an addition for a given inlcude.
     * @param handle
     * @return
     */
    public static Object getAddition(Object handle) {
        if (handle instanceof MInclude) {
            return ((MInclude) handle).getAddition();
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Returns all associated classes for some given classifier. Returns an
     * empty collection if the given argument o is not a classifier. The given
     * parameter is included in the returned collection if it has a self-
     * referencing association.
     * @param o
     * @return Collection
     */
    public static Collection getAssociatedClasses(Object o) {
        Collection col = new ArrayList();
        if (o instanceof MClassifier) {
            MClassifier classifier = (MClassifier) o;
            Collection ends = classifier.getAssociationEnds();
            Iterator it = ends.iterator();
            Set associations = new HashSet();
            while (it.hasNext()) {
		MAssociationEnd ae = (MAssociationEnd) it.next();
                associations.add(ae.getAssociation());
            }
            Collection otherEnds = new ArrayList();
            it = associations.iterator();
            while (it.hasNext()) {
                otherEnds.addAll(((MAssociation) it.next()).getConnections());
            }
            otherEnds.removeAll(ends);
            it = otherEnds.iterator();
            while (it.hasNext()) {
                col.add(((MAssociationEnd) it.next()).getType());
            }
        }
        return col;
    }

    ////////////////////////////////////////////////////////////////
    // Common getters

    /** The name of a model element or some diagram part.
     *
     * @param handle that points out the object.
     * @returns the name
     */
    public static String getName(Object handle) {
        if (handle instanceof MModelElement) {
            MModelElement me = (MModelElement) handle;

            return me.getName();
        }

        if (handle instanceof Diagram) {
            Diagram d = (Diagram) handle;

            return d.getName();
        }

        // ...
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
       Return the owner of a feature.
       @param feature
       @return classifier
    */
    public static Object getOwner(Object f) {
        if (f != null && f instanceof MFeature) {
            return ((MFeature) f).getOwner();
        }
        throw new IllegalArgumentException("Unrecognized object " + f);
    }

    /**
       Return the tagged values iterator of a model element.

       @param element The tagged values belong to this.
       @return The tagged values iterator
    */
    public static Iterator getTaggedValues(Object modelElement) {
        if (modelElement != null && modelElement instanceof MModelElement) {
            return ((MModelElement) modelElement).getTaggedValues().iterator();
        }
        return null;
    }

    /**
       Return the tagged value with a specific tag.

       @param element The tagged value belongs to this.
       @param name The tag.
       @return The found tag, null if not found
    */
    public static Object getTaggedValue(Object modelElement, String name) {
        if (modelElement != null && modelElement instanceof MModelElement) {
	    MModelElement me = ((MModelElement) modelElement);
            for (Iterator i = me.getTaggedValues().iterator();
		 i.hasNext();
		 ) {
                MTaggedValue tv = (MTaggedValue) i.next();
                if (tv.getTag().equals(name))
                    return tv;
            }
        }
        return null;
    }


    /**
       Return the key (tag) of some tagged value.

       @param tv The tagged value.
       @return The found value, null if not found
    */
    public static String getTagOfTag(Object tv) {
        if (tv != null && tv instanceof MTaggedValue) {
            return ((MTaggedValue) tv).getTag();
        }
        return null;
    }

    /**
       Return the value of some tagged value.

       @param tv The tagged value.
       @return The found value, null if not found
    */
    public static String getValueOfTag(Object tv) {
        if (tv != null && tv instanceof MTaggedValue) {
            return ((MTaggedValue) tv).getValue();
        }
        return null;
    }


    /**
     *  Return the UUID of this element
     *  @param base base element (MBase type)
     *  @return UUID
     */
    public static String getUUID(Object base) {
        if (isABase(base)) {
            return ((MBase) base).getUUID();
        }
        //
        throw new IllegalArgumentException("Unrecognized object " + base);
    }

    /**
     *  Return the visibility of this element
     *  @param element an nsuml model element
     *  @return visibility
     */
    public static Object getVisibility(Object element) {
        if (element instanceof MModelElement) {
            return ((MModelElement)element).getVisibility();
        }
        //
        throw new IllegalArgumentException("Unrecognized object " + element);
    }

    ////////////////////////////////////////////////////////////////
    // Other querying methods

    /**
     * Returns a named object in the given object by calling it's lookup method.
     * @param namespace
     * @param name of the model element
     * @return found object, null otherwise
     */
    public static Object lookupIn(Object o, String name) {
        if (o instanceof MModel)
            return ((MModel) o).lookup(name);
        if (o instanceof MNamespace)
            return ((MNamespace) o).lookup(name);
        if (o instanceof MClassifier)
            return ((MClassifier) o).lookup(name);
        return null;
    }

    ////////////////////////////////////////////////////////////////
    // Model modifying methods

    /**
     * Adds a feature to some classifier.
     * @param classifier
     * @param feature
     */
    public static void addFeature(Object cls, Object f) {
        if (cls != null
            && f != null
            && cls instanceof MClassifier
            && f instanceof MFeature) {
            ((MClassifier) cls).addFeature((MFeature) f);
        }
    }

    /**
     * Adds a method to some operation and copies the op's attributes
     * to the method.
     * @param operation
     * @param method
     */
    public static void addMethod(Object o, Object m) {
        if (o != null
            && m != null
            && o instanceof MOperation
            && m instanceof MMethod) {
            ((MMethod) m).setVisibility(((MOperation) o).getVisibility());
            ((MMethod) m).setOwnerScope(((MOperation) o).getOwnerScope());
            ((MOperation) o).addMethod((MMethod) m);
        }
    }

    /**
     * Adds a model element to some namespace.
     * @param ns namespace
     * @param me model element
     */
    public static void addOwnedElement(Object ns, Object me) {
        if (ns != null
            && ns instanceof MNamespace
            && me != null
            && me instanceof MModelElement) {
            ((MNamespace) ns).addOwnedElement((MModelElement) me);
        }
    }

    /**
     * Adds a supplier classifier to some abstraction.
     * @param a abstraction
     * @param cls supplier classifier
     */
    public static void addSupplier(Object a, Object cls) {
        if (a != null
            && cls != null
            && a instanceof MAbstraction
            && cls instanceof MClassifier) {
            ((MAbstraction) a).addSupplier((MClassifier) cls);
        }
    }

    /**
     * Adds a supplier dependency to some modelelement
     * @param supplier the supplier
     * @param dependency the dependency
     */
    public static void addSupplierDependency(Object supplier,
					     Object dependency)
    {
        if (isAModelElement(supplier) && isADependency(dependency)) {
	    MModelElement me = (MModelElement) supplier;
	    me.addSupplierDependency((MDependency) dependency);
        }
    }

    /** This method adds a classifier to a classifier role.
     *
     * @param classifier role
     * @param classifier
     */
    public static void addBase(Object o, Object c) {
        if (o != null
            && c != null
            && o instanceof MClassifierRole
            && c instanceof MClassifier) {
            ((MClassifierRole) o).addBase((MClassifier) c);
        }
    }

    /**
     * Adds a client classifier to some abstraction.
     * @param a abstraction
     * @param cls client classifier
     */
    public static void addClient(Object a, Object cls) {
        if (a != null
            && cls != null
            && a instanceof MAbstraction
            && cls instanceof MClassifier)
	{
            ((MAbstraction) a).addClient((MClassifier) cls);
        }
    }

    /**
     * Adds a client dependency to some modelelement
     * @param handle the modelelement
     * @param dependency the dependency
     */
    public static void addClientDependency(Object handle, Object dependency) {
        if (handle != null
	    && dependency != null
	    && isAModelElement(handle)
	    && isADependency(dependency))
	{
	    MModelElement me = (MModelElement) handle;
	    me.addClientDependency((MDependency) dependency);
        }
    }

    /** This method removes a classifier from a classifier role.
     *
     * @param classifier role
     * @param classifier
     */
    public static void removeBase(Object o, Object c) {
        if (o != null
            && c != null
            && o instanceof MClassifierRole
            && c instanceof MClassifier) {
            ((MClassifierRole) o).removeBase((MClassifier) c);
        }
    }

    /** This method removes a dependency from a model element.
     *
     * @param model element
     * @param dependency
     */
    public static void removeClientDependency(Object o, Object dep) {
        if (o != null
            && dep != null
            && o instanceof MModelElement
            && dep instanceof MDependency) {
            ((MModelElement) o).removeClientDependency((MDependency) dep);
        }
    }

    /** This method removes a feature from a classifier.
     *
     * @param classifier
     * @param feature
     */
    public static void removeFeature(Object cls, Object feature) {
        if (cls != null
            && feature != null
            && cls instanceof MClassifier
            && feature instanceof MFeature) {
            ((MClassifier) cls).removeFeature((MFeature) feature);
        }
    }

    /** This method removes an extension point from a use case.
     *
     * @param use case
     * @param extension point
     */
    public static void removeExtensionPoint(Object uc, Object ep) {
        if (uc != null
            && ep != null
            && uc instanceof MUseCase
            && ep instanceof MExtensionPoint) {
            ((MUseCase) uc).removeExtensionPoint((MExtensionPoint) ep);
        }
    }

    /**
     * Removes a owned modelelement from a namespace
     * @param handle
     * @param value
     */
    public static void removeOwnedElement(Object handle, Object value) {
        if (handle != null
            && value != null
            && handle instanceof MNamespace
            && value instanceof MModelElement) {
            ((MNamespace) handle).removeOwnedElement((MModelElement) value);
        }
        throw new IllegalArgumentException("Unrecognized object " + handle
					   + " or " + value);
    }

    /** This method removes a parameter from an operation.
     *
     * @param operation
     * @param parameter
     */
    public static void removeParameter(Object o, Object p) {
        if (o != null
            && p != null
            && o instanceof MOperation
            && p instanceof MParameter) {
            ((MOperation) o).removeParameter((MParameter) p);
        }
    }

    /**
     * Sets a body of some method.
     * @param method
     * @param expression
     */
    public static void setBody(Object m, Object expr) {
        if (m != null
            && m instanceof MMethod
            && (expr == null || expr instanceof MProcedureExpression)) {
            ((MMethod) m).setBody((MProcedureExpression) expr);
        }
    }

    /**
     * Sets a default value of some parameter.
     * @param parameter
     * @param expression
     */
    public static void setDefaultValue(Object p, Object expr) {
        if (p != null
            && p instanceof MParameter
            && (expr == null || expr instanceof MExpression)) {
            ((MParameter) p).setDefaultValue((MExpression) expr);
        }
    }

    /**
     * Sets an initial value of some attribute.
     * @param attribute
     * @param expression
     */
    public static void setInitialValue(Object at, Object expr) {
        if (at != null
            && at instanceof MAttribute
            && (expr == null || expr instanceof MExpression)) {
            ((MAttribute) at).setInitialValue((MExpression) expr);
        }
    }

    /**
     * Sets a location of some extension point.
     * @param extension point
     * @param location
     */
    public static void setLocation(Object ep, String loc) {
        if (ep != null && ep instanceof MExtensionPoint) {
            ((MExtensionPoint) ep).setLocation(loc);
        }
    }

    /**
     * <p>Sets the container that owns the handle. This must be set
     * correctly so every modelelement except the root model does have
     * an owner. Otherwise the saving/loading will fail.</p>
     *
     * <p><b>Warning: when changing the implementation of this method
     * be warned that the sequence of the if then else tree DOES
     * matter.</b> Most notabely, do not move the setNamespace method
     * any level up in the tree.</p>
     *
     * <p><b>Warning: the implementation does not support setting the
     * owner of actions.</b> Use setState1 etc. on action for that
     * goal</p>
     *
     * @param handle The modelelement that must be added to the container
     * @param container The owning modelelement
     * @exception IllegalArgumentException when the handle or
     * container is null or if the handle cannot be added to the
     * container.
     */
    public static void setModelElementContainer(Object handle,
						Object container)
        throws IllegalArgumentException
    {
        if (handle == null || container == null) {
            throw new IllegalArgumentException("Neither the modelelement to be "
					       + "added as the container "
					       + "nor the one to which "
					       + "the modelelement is added "
					       + "may be null");
        }
        if (handle instanceof MPartition
	    && container instanceof MActivityGraph) {
            ((MPartition) handle).setActivityGraph((MActivityGraph) container);
        } else if (handle instanceof MConstraint
		   && container instanceof MStereotype) {
	    MConstraint c = (MConstraint) handle;
	    c.setConstrainedElement2((MStereotype) container);
        } else if (handle instanceof MInteraction
		   && container instanceof MCollaboration) {
            ((MInteraction) handle).setContext((MCollaboration) container);
        } else if (handle instanceof MElementResidence
		   && container instanceof MComponent) {
	    MElementResidence er = (MElementResidence) handle;
	    er.setImplementationLocation((MComponent) container);
        } else if (handle instanceof MAttributeLink
		   && container instanceof MInstance) {
            ((MAttributeLink) handle).setInstance((MInstance) container);
        } else if (handle instanceof MMessage
		   && container instanceof MInteraction) {
            ((MMessage) handle).setInteraction((MInteraction) container);
        } else if (handle instanceof MLinkEnd && container instanceof MLink) {
            ((MLinkEnd) handle).setLink((MLink) container);
        } else if (handle instanceof MAttributeLink
		   && container instanceof MLinkEnd) {
            ((MAttributeLink) handle).setLinkEnd((MLinkEnd) container);
        } else if (handle instanceof MTaggedValue
		   && container instanceof MStereotype) {
            ((MTaggedValue) handle).setStereotype((MStereotype) container);
        } else if (handle instanceof MTaggedValue
		   && container instanceof MModelElement) {
            ((MTaggedValue) handle).setModelElement((MModelElement) container);
        } else if (handle instanceof MStateVertex
		   && container instanceof MCompositeState) {
            ((MStateVertex) handle).setContainer((MCompositeState) container);
        } else if (handle instanceof MElementImport
		   && container instanceof MPackage) {
            ((MElementImport) handle).setPackage((MPackage) container);
        } else if (handle instanceof MTransition
		   && container instanceof MState) {
            ((MTransition) handle).setState((MState) container);
        } else if (handle instanceof MState
		   && container instanceof MStateMachine) {
            ((MState) handle).setStateMachine((MStateMachine) container);
        } else if (handle instanceof MTransition
		   && container instanceof MStateMachine) {
            ((MTransition) handle).setStateMachine((MStateMachine) container);
        } else if (handle instanceof MAction
		   && container instanceof MTransition) {
            ((MAction) handle).setTransition((MTransition) container);
        } else if (handle instanceof MGuard
		   && container instanceof MTransition) {
            ((MGuard) handle).setTransition((MTransition) container);
        } else if (handle instanceof MModelElement
		   && container instanceof MNamespace) {
            ((MModelElement) handle).setNamespace((MNamespace) container);
        } else {
            throw new IllegalArgumentException("Object "
					       + handle.toString()
					       + " cannot be owned by "
					       + container.toString());
        }
    }

    /**
     * Sets a multiplicity of some attribute or association end.
     * @param attribute or association end
     * @param multiplicity as string OR multiplicity object
     */
    public static void setMultiplicity(Object o, Object mult) {
        // FIXME: the implementation is ugly, because I have no spec at hand...
        if (o == null)
            return;
        if (mult != null && !(mult instanceof MMultiplicity)) {
			if (mult instanceof String) {
                mult = ("1_N".equals(mult)) ? MMultiplicity.M1_N : MMultiplicity.M1_1;
			} else
                return;
		}
        if (o instanceof MAttribute) {
            ((MAttribute) o).setMultiplicity((MMultiplicity)mult);
        } else if (o instanceof MAssociationEnd) {
            ((MAssociationEnd) o).setMultiplicity((MMultiplicity)mult);
        }
    }

    /**
     * Sets the classifiers of some instance.
     * @param instance
     * @param classifier vector
     */
    public static void setClassifiers(Object o, Vector v) {
        if (o != null && o instanceof MInstance) {
            ((MInstance) o).setClassifiers(v);
        }
    }

    /**
     * Sets a name of some modelelement.
     * @param model element
     * @param name
     */
    public static void setName(Object o, String name) {
        if (o != null && o instanceof MModelElement) {
            ((MModelElement) o).setName(name);
        }
    }

    /**
     * Sets a namespace of some modelelement.
     * @param model element
     * @param namespace
     */
    public static void setNamespace(Object o, Object ns) {
        if (o != null
            && o instanceof MModelElement
            && (ns == null || ns instanceof MNamespace)) {
            ((MModelElement) o).setNamespace((MNamespace) ns);
        }
    }

    /**
     * Sets the navigability of some association end.
     * @param association end
     * @param navigability flag
     */
    public static void setNavigable(Object o, boolean flag) {
        if (o != null && o instanceof MAssociationEnd) {
            ((MAssociationEnd) o).setNavigable(flag);
        }
    }

    /**
     * Set the visibility of some modelelement.
     * @param model element
     * @param visibility
     */
    public static void setVisibility(Object o, short v) {
        if (o != null && o instanceof MModelElement) {
            if (v == ACC_PRIVATE) {
                ((MModelElement) o).setVisibility(MVisibilityKind.PRIVATE);
            } else if (v == ACC_PROTECTED) {
                ((MModelElement) o).setVisibility(MVisibilityKind.PROTECTED);
            } else if (v == ACC_PUBLIC) {
                ((MModelElement) o).setVisibility(MVisibilityKind.PUBLIC);
            }
        }
    }

    /**
     * Set the owner scope of some feature.
     * @param feature
     * @param owner scope
     */
    public static void setOwnerScope(Object f, short os) {
        if (f != null && f instanceof MFeature) {
            if (os == CLASSIFIER_SCOPE) {
                ((MFeature) f).setOwnerScope(MScopeKind.CLASSIFIER);
            } else if (os == INSTANCE_SCOPE) {
                ((MFeature) f).setOwnerScope(MScopeKind.INSTANCE);
            }
        }
    }

    /**
     * Set the target scope of some association end.
     * @param association end
     * @param target scope
     */
    public static void setTargetScope(Object ae, short ts) {
        if (ae != null && ae instanceof MAssociationEnd) {
            if (ts == CLASSIFIER_SCOPE) {
                ((MAssociationEnd) ae).setTargetScope(MScopeKind.CLASSIFIER);
            } else if (ts == INSTANCE_SCOPE) {
                ((MAssociationEnd) ae).setTargetScope(MScopeKind.INSTANCE);
            }
        }
    }

    /**
     * Set the concurrency of some operation.
     * @param operation
     * @param concurrency
     */
    public static void setConcurrency(Object o, short c) {
        if (o instanceof MOperation) {
	    MOperation oper = (MOperation) o;
            if (c == GUARDED) {
                oper.setConcurrency(MCallConcurrencyKind.GUARDED);
            } else if (c == SEQUENTIAL) {
                oper.setConcurrency(MCallConcurrencyKind.SEQUENTIAL);
            }
        }
    }

    /**
     * Sets the dispatch action for some stimulus
     * @param handle
     * @param value
     */
    public static void setDispatchAction(Object handle, Object value) {
        if (handle instanceof MStimulus
            && (value == null || value instanceof MAction)) {
            ((MStimulus) handle).setDispatchAction((MAction) value);
            return;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle
					   + " or " + value);
    }

    /**
     * Sets the do activity of a state
     * @param handle
     * @param value
     */
    public static void setDoActivity(Object handle, Object value) {
        if (handle instanceof MState
            && (value == null || value instanceof MAction)) {
            ((MState) handle).setDoActivity((MAction) value);
            return;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle
					   + " or " + value);
    }

    /**
     * Sets the effect of some transition
     * @param handle
     * @param value
     */
    public static void setEffect(Object handle, Object value) {
        if (handle instanceof MTransition
            && (value == null || value instanceof MAction)) {
            ((MTransition) handle).setEffect((MAction) value);
            return;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle
					   + " or " + value);
    }

    /**
     * Sets the entry action of some state
     * @param handle
     * @param value
     */
    public static void setEntry(Object handle, Object value) {
        if (handle instanceof MState
            && (value == null || value instanceof MAction)) {
            ((MState) handle).setEntry((MAction) value);
            return;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle
					   + " or " + value);
    }

    /**
     * Sets the exit action of some state
     * @param handle
     * @param value
     */
    public static void setExit(Object handle, Object value) {
        if (handle instanceof MState
            && (value == null || value instanceof MAction)) {
            ((MState) handle).setExit((MAction) value);
            return;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle
					   + " or " + value);
    }

    /**
     * Sets the features of some model element.
     * @param element the model element to set features to
     * @param features the list of features
     */
    public static void setFeatures(Object element, Collection features) {
        if (element != null && element instanceof MClassifier && features instanceof List) {
            ((MClassifier)element).setFeatures((List)features);
        }
    }

    /**
     * Set the changeability of some feature.
     * @param feature
     * @param changeability flag
     */
    public static void setChangeable(Object o, boolean flag) {
        // FIXME: the implementation is ugly, because I have no spec at hand...
        if (o == null)
            return;
        if (o instanceof MStructuralFeature) {
            if (flag)
		((MStructuralFeature) o).setChangeability(MChangeableKind.CHANGEABLE);
            else
		((MStructuralFeature) o).setChangeability(MChangeableKind.FROZEN);
        } else if (o instanceof MAssociationEnd) {
	    MAssociationEnd ae = (MAssociationEnd) o;
            if (flag)
                ae.setChangeability(MChangeableKind.CHANGEABLE);
            else
		ae.setChangeability(MChangeableKind.FROZEN);
        }
    }


    /**
     * Sets if of some classifier is abstract.
     * @param classifier
     * @param flag
     */
    public static void setAbstract(Object o, boolean flag) {
        if (o != null) {
            if (o instanceof MClassifier)
		((MClassifier) o).setAbstract(flag);
            else if (o instanceof MOperation)
		((MOperation) o).setAbstract(flag);
        }
    }

    /**
     * Sets the action to a message
     * @param message
     * @param action
     */
    public static void setAction(Object message, Object action) {
        if (message instanceof MMessage
            && (action == null || action instanceof MAction)) {
            ((MMessage) message).setAction((MAction) action);
            return;
        }
        throw new IllegalArgumentException("Unrecognized object " + message
					   + " or " + action);
    }

    /**
     * Sets the asynchronous property of an action
     * @param handle the action
     * @param value the value to alter the asynchronous property to
     */
    public static void setAsynchronous(Object handle, boolean value) {
        if (handle instanceof MAction) {
            ((MAction) handle).setAsynchronous(value);
            return;
        }
        throw new IllegalArgumentException("Unrecognized object " + handle);
    }

    /**
     * Sets if of some classifier is a leaf.
     * @param classifier
     * @param flag
     */
    public static void setLeaf(Object o, boolean flag) {
        if (o != null && o instanceof MClassifier) {
            ((MClassifier) o).setLeaf(flag);
        }
    }

    /**
     * Sets if of some classifier is a root.
     * @param classifier
     * @param flag
     */
    public static void setRoot(Object o, boolean flag) {
        if (o != null && o instanceof MClassifier) {
            ((MClassifier) o).setRoot(flag);
        }
    }

    /**
     * Set some parameters kind to 'in'.
     * @param parameter
     */
    public static void setKindToIn(Object p) {
        if (p != null && p instanceof MParameter) {
            ((MParameter) p).setKind(MParameterDirectionKind.IN);
        }
    }

    /**
     * Set some parameters kind to 'in/out'.
     * @param parameter
     */
    public static void setKindToInOut(Object p) {
        if (p != null && p instanceof MParameter) {
            ((MParameter) p).setKind(MParameterDirectionKind.INOUT);
        }
    }

    /**
     * Set some parameters kind to 'out'.
     * @param parameter
     */
    public static void setKindToOut(Object p) {
        if (p != null && p instanceof MParameter) {
            ((MParameter) p).setKind(MParameterDirectionKind.OUT);
        }
    }

    /**
     * Set some parameters kind to 'return'.
     * @param parameter
     */
    public static void setKindToReturn(Object p) {
        if (p != null && p instanceof MParameter) {
            ((MParameter) p).setKind(MParameterDirectionKind.RETURN);
        }
    }

    /**
     * Sets the query flag of a behavioral feature.
     * @param behavioral feature
     * @param flag
     */
    public static void setQuery(Object bf, boolean flag) {
        if (bf instanceof MBehavioralFeature) {
            ((MBehavioralFeature) bf).setQuery(flag);
        }
    }

    /**
     * Sets the type of some parameter.
     * @param parameter
     * @param type
     */
    public static void setType(Object parameter, Object type) {
        if (parameter != null && (type == null || type instanceof MClassifier)) {
            if (parameter instanceof MParameter)
		((MParameter) parameter).setType((MClassifier) type);
            else if (parameter instanceof MAssociationEnd)
		((MAssociationEnd) parameter).setType((MClassifier) type);
            else if (parameter instanceof MAttribute)
		((MAttribute) parameter).setType((MClassifier) type);
        }
    }

    /**
     *  Return the UUID of this element
     *  @param base base element (MBase type)
     *  @return UUID
     */
    public static void setUUID(Object base, String uuid) {
        if (isABase(base)) {
            ((MBase) base).setUUID(uuid);
        }
        //
        throw new IllegalArgumentException("Unrecognized object " + base);
    }
    
    /**
     * Sets a tagged value of some modelelement.
     * @param model element
     * @param tag
     * @param value
     */
    public static void setTaggedValue(Object o, String tag, String value) {
        if (o != null && o instanceof MModelElement) {
            MTaggedValue tv = MFactory.getDefaultFactory().createTaggedValue();
            tv.setModelElement((MModelElement) o);
            tv.setTag(tag);
            tv.setValue(value);
        }
    }

    /**
     * Sets a value of some taggedValue.
     * @param taggedValue
     * @param value
     */
    public static void setValueOfTag(Object tv, String value) {
        if (tv != null && tv instanceof MTaggedValue) {
            ((MTaggedValue) tv).setValue(value);
        }
    }

    /**
     * Sets a state machine of some state.
     * @param state
     * @param state machine
     */
    public static void setStateMachine(Object st, Object stm) {
        if (st != null && st instanceof MState
            && (stm == null || stm instanceof MStateMachine)) {
            ((MState) st).setStateMachine((MStateMachine) stm);
        }
    }

    /**
     * Sets the stereotype of some modelelement. The method also copies a
     * stereotype that is not a part of the current model to the current model.
     * @param m model element
     * @param stereo stereotype
     */
    public static void setStereotype(Object m, Object stereo) {
        if (m != null && m instanceof MModelElement) {
            MModelElement me = (MModelElement) m;
            if (stereo != null
                && stereo instanceof MStereotype
                && me.getModel() != ((MStereotype) stereo).getModel()) {
                ((MStereotype) stereo).setNamespace(me.getModel());
            }
            if (stereo == null || stereo instanceof MStereotype)
                me.setStereotype((MStereotype) stereo);
        }
    }

    /**
     * Adds a constraint to some model element.
     * @param me model element
     * @param mc constraint
     */
    public static void addConstraint(Object me, Object mc) {
        if (me != null
            && me instanceof MModelElement
            && mc != null
            && mc instanceof MConstraint) {
            ((MModelElement) me).addConstraint((MConstraint) mc);
        }
    }

    /** getUMLClassName returns the name of the UML Model class, e.g. it
     *  it will return Class for an object of type MClass.
     * @param handle MBase
     * @return classname of modelelement
     */
    public static String getUMLClassName(Object handle) {
        if (handle instanceof MBase) {
            return ((MBase) handle).getUMLClassName();
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////
    // Convenience methods

    /** The empty set.
     *
     * @returns an empty iterator.
     */
    private static Iterator emptyIterator() {
        return Collections.EMPTY_SET.iterator();
    }

    private static Collection emptyCollection() {
        return Collections.EMPTY_LIST;
    }
}
