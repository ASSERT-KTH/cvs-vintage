// Copyright (c) 1996-2001 The Regents of the University of California. All
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


package org.argouml.uml.cognitive;
import java.util.*;
import org.argouml.util.*;

/**
 *   This class is the default member of a resource bundle that
 *   provides strings for UML related critiques and check lists
 *
 *   @author Curt Arnold
 *   @since 0.9
 *   @see java.util.ResourceBundle
 *   @see UMLCognitiveResourceBundle_de
 *   @see org.argouml.util.CheckResourceBundle
 *   @see org.argouml.uml.cognitive.critics.CrUML
 */
public class UMLCognitiveResourceBundle_de extends ListResourceBundle {

    private static final Object[][] _contents = {
        { "CrAssocNameConflict_head" ,
	        "L\u00f6sen Sie den Konflikt bei den Assoziationsnamen auf" },
        { "CrAssocNameConflict_desc" ,
	        "Jedes Element eines Namensraums mu\u00df einen eindeutigen Namen haben. \n\nKlare und unzweideutige Namensgebung ist ein Schl\u00fcssel zur Codegenerierung, Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs. \n\nUm dieses Problem zu beheben, benutzen Sie den \"Next\" Button, oder selektieren Sie die Elemente und benutzen Sie die \"Eigenschaften\" Registerkarte um ihre Namen zu \u00e4ndern." },
        { "CrAttrNameConflict_head" ,
                "\u00c4ndern Sie den Attributnamen um einen Konflikt zu vermeiden."},
        { "CrAttrNameConflict_desc" ,
	        "Attribute m\u00fcssen unterschiedliche Namen haben.  Dieses Problem kann auch durch vererbte Attribute entstehen. \n\nKlare und eindeutige Namen sind ein Schl\u00fcssel zur Codegenerierung und der Erstellung eines verst\u00e4ndlichen und wartbaren Entwurfs.\n\nUm dieses Problem zu l\u00f6sen benutzen Sie den \"Next\" Button, oder selektieren Sie eines der betreffenden Attribute dieser Klasse und \u00e4ndern Sie seinen Namen."},
        { "CrOperNameConflict_head" ,
	        "\u00c4ndern Sie Namen oder Signaturen in <ocl>self</ocl>" },
        { "CrOperNameConflict_desc" ,
	        "Zwei Operationen haben exakt die gleiche Signatur. Operationen m\u00fcssen unterschiedliche Signaturen haben. Eine Signatur ist die Kombination des Namens und der Parametertypen der Operation. \n\nDas Vermeiden von identischen Signaturen ist ein Schl\u00fcssel zur Codegenerierung und zur Erstellung eines verst\u00e4ndlichen und wartbaren Entwurfs.\n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie eine der betreffenden Operationen dieser Klasse uund \u00e4ndern Sie deren Namen." },
        { "CrCircularAssocClass_head" ,
	        "Ringf\u00f6rmige Assoziation" },
        { "CrCircularAssocClass_desc" ,
	        "Assoziierte Klassen k\u00f6nnen keine Rolle beinhalten, welche sich wiederum direkt auf die Klasse bezieht" },
        { "CrCircularInheritance_head" ,
                "Remove <ocl>self</ocl>'s Circular Inheritance" },
        { "CrCircularInheritance_desc" ,
                "Inheritances relationships cannot have cycles. \n\nA legal class inheritance hierarchy is needed for code generation and the correctness of the design. \n\nTo fix this, use the \"Next>\" button, or manually select one of the  generalization arrows in the cycle and remove it." },
        { "CrCircularComposition_head" ,
                "Remove Circular Composition" },
        { "CrCircularComposition_desc" ,
                "Composition relationships (black diamonds) cannot have cycles. \n\nA legal aggregation inheritance hierarchy is needed for code generation and the correctness of the design. \n\nTo fix this, use the \"Next>\" button, or manually select one of the  associations in the cycle and remove it or change its aggregation to something other than composite." },
        { "CrCrossNamespaceAssoc_head" ,
                "Aggregate Role in N-way MAssociation" },
        { "CrCrossNamespaceAssoc_desc" ,
	        "Jede Klasse oder MInterface, welche Teil einer Assoziation ist, sollte im Namensraum der Assoziation sein.\n" },
        { "CrDupParamName_head" ,
	        "Parametername kommt doppelt vor" },
        { "CrDupParamName_desc" ,
	        "Jeder Parameter einer Operation mu\u00df einen eindeutigen Namen haben. \n\nKlare und unzweideutige Namensgebung ist erforderlich zur Codegenerierung und zum Erreichen eines klaren und wartbaren Entwurfs.\n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder \u00e4ndern Sie den Namen eines der Parameter von Hand." },
        { "CrDupRoleNames_head" ,
	        "\u00c4ndern Sie den <ocl>self</ocl> Rollennamen" },
        { "CrDupRoleNames_desc" ,
	        "Assoziation <ocl>self</ocl> hat zwei Rollen mit widerspr\u00fcchlichen Namen. \n\nKlare und unzweideutige Namensgebung ist ein Schl\u00fcssel zur Codegenerierung und der Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie <ocl>self</ocl> und benutzen Sie die 'Eigenschaften' Registerkarte um die Rollennamen zu \u00e4ndern." },
        { "CrFinalSubclassed_head" ,
	        "Entfernen Sie das 'final' Schl\u00fcsselwort oder entfernen Sie die Unterklassen" },
        { "CrFinalSubclassed_desc" ,
	        "In Java bedeutet das 'final' Schl\u00fcsselwort, dass eine Klasse keine Unterklassen haben soll. Diese Klasse ist als 'final' markiert und hat Unterklassen.\n\nEine gut durchdachte Klassenhierachie, welche potentielle Erweiterungen unterst\u00fctzt, ist ein wichtiger Teil eines verst\u00e4ndlichen und wartbaren Entwurfs.\n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button, selektieren sie die Klasse und \u00e4ndern Sie die Basisklasse, oder selektieren Sie die Basisklasse und benutzen Sie die 'Eigenschaften' Registerkarte um das 'final' Schl\u00fcsselwort zu entfernen."},
        { "CrIllegalGeneralization_head" ,
	        "Unerlaubte MGeneralisierung" },
        { "CrIllegalGeneralization_desc" ,
	        "Modellelemente k\u00f6nnen nur von Elementen des gleichen Typs erben. \n\nEine g\u00fcltige Vererbungshierachie ist erforderlich zur Codegenerierung und zur Korrektheit des Entwurfs. \n\nUm dieses Problem zu beheben, benutzen Sie den \"Next\" Button oder selektieren Sie den Generalisierungspfeil per Hand um ihn zu entfernen." },
        { "CrAlreadyRealizes_head" ,
	        "Entfernen Sie die unn\u00f6tige Realisierung von <ocl>self</ocl>" },
        { "CrAlreadyRealizes_desc" ,
	        "Die selektierte Klassen implementiert bereits indirekt das Interface {item.extra}. Es gibt keinen Grund es nochmals direkt zu implementieren. \n\nDen Entwurf zu vereinfachen ist immer eine gute Idee. Sie m\u00f6chten vielleicht diesen \"To Do\" Eintrag l\u00f6schen, falls Sie es offensichtlich machen wollen, da\u00df die selektierte Klasse dieses MInterface implementiert.\n\nUm dieses Problem zu beheben selektieren Sie die Realisierung (die punktierte Linie mit der weissen, dreieckigen, Pfeilspitze) und dr\u00fccken Sie die 'Entfernen' Taste." },
        { "CrInterfaceAllPublic_head" ,
	        "Operationen in einem Interface m\u00fcssen 'public' sein" },
        { "CrInterfaceAllPublic_desc" ,
	        "Interfaces sind dazu gedacht, die Menge der Operationen zu spezifizieren, welche von anderen Klassen implementiert werden m\u00fcssen. Dazu m\u00fcssen sie 'public' sein. \n\nEine wohldurchdachte Sammlung von Interfaces ist ein guter Weg die potentiellen Erweiterungen eines Klassen-Frameworks zu spezifizieren. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button, oder selektieren Sie die Operationen des Interfaces per Hand und benutzen Sie die 'Eigenschaften' Registerkarte um Sie 'public' zu machen." },
        { "CrInterfaceOperOnly_head" ,
	        "Interfaces d\u00fcrfen nur Operationen haben" },
        { "CrInterfaceOperOnly_desc" ,
	        "Interfaces sind dazu gedacht die Menge der Operationen zu spezifizieren, welche von anderen Klassen implementiert werden m\u00fcssen. Sie implementieren diese Operationen nicht selbst und k\u00f6nnen keine Attribute haben. \n\nEine wohldurchdachte Sammlung von Interfaces ist ein guter Weg die potentiellen Erweiterungen eines Klassen-Frameworks zu definieren. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie das Interface und benutzen Sie die 'Eigenschaften' Registerkarte um alle Attribute zu entfernen." },
        { "CrMultipleAgg_head" ,
	        "Doppelt vorkommende Aggregationsrolle"	},
        { "CrMultipleAgg_desc" ,
                "Only one role of an MAssociation can be aggregate or composite.\n\nA clear and consistent is-part-of hierarchy is a key to design clarity, \nmanagable object storage, and the implementation of recursive methods.\nTo fix this, select the MAssociation and set some of its role \naggregations to None." },
        { "CrNWayAgg_head" ,
                "Aggregate Role in N-way MAssociation" },
        { "CrNWayAgg_desc" ,
                "Three-way (or more) Associations can not have aggregate ends.\n\nA clear and consistent is-part-of hierarchy is a key to design clarity, \nmanagable object storage, and the implementation of recursive methods.\nTo fix this, use the \"Next>\" button, or manually select the MAssociation \nand set all of its role aggregations to None." },
        { "CrNavFromInterface_head" ,
                "Remove Navigation from MInterface <ocl>self</ocl>" },
        { "CrNavFromInterface_desc" ,
                "Associations involving an MInterface can be not be naviagable in the direction from the MInterface.  This is because interfaces do contain only operation declarations and cannot hold pointers to other objects.\n\nThis part of the design should be changed before you can generate code from this design.  If you do generate code before fixing this problem, the code will not match the design.\n\nTo fix this, select the MAssociation and use the \"Properties\" tab to uncheck Navigable for the end touching the MInterface.  The MAssociation should then appear with an stick arrowhead pointed away from the MInterface." },
        { "CrUnnavigableAssoc_head" ,
                "Make <ocl>self</ocl> Navigable" },
        { "CrUnnavigableAssoc_desc" ,
                "The MAssociation <ocl>self</ocl> is not navigable in any direction. All associations should be navigable at least one way.\n\nSetting the navigablility of associations allows your code to access data by following pointers. \n\nTo fix this, select association \"<ocl>self</ocl>\" in the diagram or navigator panel and click the properties tab.  Then use the checkboxes at the bottom of the properties panel to turn on navigablility. " },
        { "CrNameConflictAC_head" ,
	        "Rollenname widerspricht Klassenmerkmal" },
        { "CrNameConflictAC_desc" , 
	        "Der Rollenname einer Klasse in einer Assoziation darf nicht in Konflikt mit den Namen von Klassenmerkmalen (z.B. Klassenvariablen) stehen.\n" },
        { "CrMissingClassName_head" ,
	        "W\u00e4hlen Sie einen Namen" },
        { "CrMissingClassName_desc" ,
	        "Jede Klasse und jedes Interface innerhalb eines Pakets mu\u00df einen Namen haben. \n\nKlare und unzweideutige Namensgebung ist ein Schl\u00fcssel zur Codegenerierung, sowie zur Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button und benutzen Sie die 'Eigenschaften' Registerkarte um einen Namen zu vergeben." },
        { "CrMissingAttrName_head" ,
	        "W\u00e4hlen Sie einen Namen" },
        { "CrMissingAttrName_desc" ,
	        "Jedes Attribut mu\u00df einen Namen haben. \n\nKlare und unzweideutige Namensgebung ist ein Schl\u00fcssel zur Codegenerierung, der Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie das Attribut per Hand und benutzen Sie die 'Eigenschaften' Registerkarte um ihm einen Namen zu geben" },
        { "CrMissingOperName_head" ,
	        "W\u00e4hlen Sie einen Namen" },
        { "CrMissingOperName_desc" ,
	        "Jede Operation mu\u00df einen Namen haben. \n\nKlare und unzweideutige Namensgebung ist ein Schl\u00fcssel zur Codegenerierung, Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie die Operation per Hand und benutzen Sie die 'Eigenschaften' Registerkarte um ihr einen Namen zu geben" },
        { "CrMissingStateName_head" ,
	        "W\u00e4hlen Sie einen Namen" },
        { "CrMissingStateName_desc" ,
	        "Jeder Zustand in einer Statemachine sollte einen Namen haben. \n\nKlare und unzweideutige Namensgebung ist ein Schl\u00fcssel zur Verst\u00e4ndlichkeit und wartbarkeit des Entwurfs. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button, selektieren Sie den Zustand per Hand und benutzen Sie die 'Eigenschaften' Registerkarte um ihm einen Namen zu geben, oder selektieren Sie den Zustand und tippen Sie einen Namen." },
        { "CrNoInstanceVariables_head" ,
	        "F\u00fcgen Sie eine Instanzvariable zu <ocl>self</ocl> hinzu" },
        { "CrNoInstanceVariables_desc" ,
                "Sie haben noch keine Instanzvariablen f\u00fcr <ocl>self</ocl> definiert. Normalerweise haben Klassen Instanzvariablen, die Zustandsinformationen der einzelnen Instanzen halten. Klassen, die nur statische Klassenvariablen und statische Methoden beinhalten, sollten den Stereotyp <<utility>> tragen.\n\nUm dieses Problem zu beheben, dr\u00fccken Sie den \"Next\" Button oder f\u00fcgen sie Attribute \u00fcber das Men\u00fc hinzu." },
        { "CrNoAssociations_head" ,
	        "F\u00fcgen Sie Assoziationen zu <ocl>self</ocl> hinzu" },
        { "CrNoAssociations_desc" ,
	        "Sie haben bis jetzt keine Assoziationen f\u00fcr <ocl>self</ocl> spezifiziert. Normalerweise sind Klassen, Aktoren und Anwendungsf\u00e4lle mit anderen assoziiert. \n\nDas Definieren von Assoziationen zwischen Objekten ist ein wichtiger Teil Ihres Entwurfs. \n\nUm dieses Problem zu beheben dr\u00fccken Sie den \"Next\" Button oder f\u00fcgen Sie die Assoziationen per Hand hinzu. Dazu klicken Sie auf das 'Assoziation' Icon im Toolbar und erzeugen Sie die Assoziationen von und zu <ocl>self</ocl> per Drag und Drop." },
        { "CrNonAggDataType_head" ,
                "Wrap MDataType" },
        { "CrNonAggDataType_desc" ,
                "DataTypes are not full classes and cannot be associated with classes, unless the MDataType is part of a composite (black diamond) aggregation. \n\nGood OO design depends on careful choices about which entities to represent as full objects and which to represent as attributes of objects.\n\nTo fix this, use the \"Next>\" button, or manually replace the MDataType with a full class or change the association aggregation to containmentby a full class.\n" },
        { "CrOppEndConflict_head" ,
	        "Benennen Sie die Assozationsrollen um" },
        { "CrOppEndConflict_desc" ,
	        "Zwei Rollen von <ocl>self</ocl> haben den gleichen Namen. Rollen m\u00fcssen unterschiedliche Namen haben. Dies ist eventuell verursacht durch ein ererbtes Attribut. \n\nKlare und unzweideutige Namensgebung ist ein Schl\u00fcssel zur Codegenerierung und zum Erstellen eines verst\u00e4ndlichen und wartbaren Entwurfs.\n\nUm dieses Problem zu beheben dr\u00fccken Sie den \"Next\" Button oder selektieren Sie die Rolle am anderen Ende der Assoziation dieser Klasse und \u00e4ndern Sie ihren Namen." },
        { "CrParamTypeNotImported_head" ,
	        "Importieren Sie den Parametertyp in die Klasse" },
        { "CrParamTypeNotImported_desc" ,
	        "Der Typ eines jeden Operationsparameter mu\u00df sichtbar sein und in die Klasse importiert werden, welche die Operation beinhaltet.\n\nDas Importieren von Klassen ist erforderlich f\u00fcr die Codegenerierung. Eine gute Aufteilung von Klassen in Pakete ist ein Schl\u00fcssel zu einem verst\u00e4ndlichen Entwurf.\n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder f\u00fcgen Sie der betreffenden Klasse per Hand eine 'import' Anweisung hinzu." },
        { "CrSignatureConflict_head" ,
	        "L\u00f6sen Sie den Konflikt bei den Signaturen auf." },
        { "CrSignatureConflict_desc" ,
	        "Zwei Operationen von <ocl>self</ocl> haben die gleiche Signatur. Eine Signatur besteht aus dem Namen der Operation und der Anzahl und den Typen der Parameter.\n\nOperationen m\u00fcssen unterschiedliche Signaturen haben, damit Code generiert wird, welcher compiliert werden kann.\n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder klicken Sie auf eine der betreffenden Operationen in der Navigator Registerkarte und benutzen Sie die 'Eigenschaften' Registerkarte um den Namen oder die Parameter zu \u00e4ndern." },
        { "CrUselessAbstract_head" ,
                "Define Concrete (Sub)Class" },
        { "CrUselessAbstract_desc" ,
                "<ocl>self</ocl> can never influence the running system because it can never have any instances, and none of its subclasses can have instances either. \n\nTo fix this problem: (1) define concrete subclasses that implement the interface of this class; or (2) make <ocl>self</ocl> or one of its existing subclasses concrete." },
        { "CrUselessInterface_head" ,
	        "Definieren Sie eine Klasse um <ocl>self</ocl> zu implementieren" },
        { "CrUselessInterface_desc" ,
                "<ocl>self</ocl> kann nicht genutzt werden, weil es keine Klasse gibt, die dieses Interface implementiert.\n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder nutzen Sie den \"Rapid-Button\" am unteren Rand des Diagrammelements." },
        { "CrDisambigClassName_head" ,
	        "W\u00e4hlen Sie einen eindeutigen Namen f\u00fcr <ocl>self</ocl>" },
        { "CrDisambigClassName_desc" ,
	        "Jede Klasse und jedes Interface innerhalb eines Pakets m\u00fcssen einen eindeutigen Namen haben. Es gibt zumindest zwei Elemente in diesem Paket mit dem Namen \"<ocl>self</ocl>\".\n\nKlare und unzweideutige Namensgebung ist ein Schl\u00fcssel zur Codegenerierung, Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie eine der betreffenden Klassen und benutzen Sie die 'Eigenschaften' Registerkarte um ihren Namen zu \u00e4ndern" },
        { "CrDisambigStateName_head" ,
	        "W\u00e4hlen Sie einen eindeutigen Namen f\u00fcr <ocl>self</ocl>" },
        { "CrDisambigStateName_desc" ,
                "Every state within a state machine must have a unique name. There are at least two states in this machine named \"<ocl>self</ocl>\".\n\nClear and unambiguous naming is key to code generation and the understandability and maintainability of the design. \n\nTo fix this, use the \"Next>\" button, or manually select one of the conflicting states and use the \"Properties\" tab to change their names." },
        { "CrConflictingComposites_head" ,
                "Remove Conflicting Composite Associations" },
        { "CrConflictingComposites_desc" ,
                "A composite (black diamond) role of an association indicates that instances of that class contain instances of the associated classes. Since each instance can only be contained in one other object, each object can be the 'part' in at most one is-part-of relationship.\n\nGood OO design depends on building good is-part-of relationships.\n\nTo fix this, use the \"Next>\" button, or manually change one association to have multiplicity to 0..1 or 1..1, or another kind of aggregation (e.g., a white diamond is less strict), or remove one of the associations" },
        { "CrTooManyAssoc_head" ,
                "Reduce Associations on <ocl>self</ocl>" },
        { "CrTooManyAssoc_desc" ,
                "There are too many Associations on class <ocl>self</ocl>.  Whenever one class becomes too central to the design it may become a maintenance bottleneck that must be updated frequently. \n\nDefining the associations between objects is an important part of your design. \n\nTo fix this, press the \"Next>\" button, or remove associations manually by clicking on an association in the navigator pane or diagram and presing the \"Del\" key. " },
        { "CrTooManyAttr_head" ,
	        "Reduzieren Sie die Anzahl der Attribute in <ocl>self</ocl>" },
        { "CrTooManyAttr_desc" ,
                "There are too many Attributes on class <ocl>self</ocl>.  Whenever one class becomes too central to the design it may become a maintenance bottleneck that must be updated frequently. \n\nDefining the attributes of objects is an important part of your design. \n\nTo fix this, press the \"Next>\" button, or remove attributes manually by double-clicking on the attribute compartment of the  highlighted class in the diagram and removing the line of text for an attribute. " },
        { "CrTooManyOper_head" ,
	        "Reduzieren Sie die Anzahl der Operationen in <ocl>self</ocl>" },
        { "CrTooManyOper_desc" ,
                "There are too many Operations on class <ocl>self</ocl>.  Whenever one class becomes too central to the design it may become a maintenance bottleneck that must be updated frequently. \n\nDefining the operations of objects is an important part of your design. \n\nTo fix this, press the \"Next>\" button, or remove attributes manually by double-clicking on the operation compartment of the  highlighted class in the diagram and removing the line of text for an operation. " },
        { "CrTooManyStates_head" ,
                "Reduce States in machine <ocl>self</ocl>" },
        { "CrTooManyStates_desc" ,
                "There are too many States in <ocl>self</ocl>.  If one state machine has too many states it may become very difficult for humans to understand. \n\nDefining an understandable set of states is an important part of your design. \n\nTo fix this, press the \"Next>\" button, or remove states manually by clicking on a states in the navigator pane or diagram and presing the \"Del\" key.  Or you can nest states..." },
        { "CrTooManyTransitions_head" ,
                "Reduce Transitions on <ocl>self</ocl>" },
        { "CrTooManyTransitions_desc" ,
                "There are too many Transitions on state <ocl>self</ocl>.  Whenever one state becomes too central to the machine it may become a maintenance bottleneck that must be updated frequently. \n\nDefining the transitions between states is an important part of your design. \n\nTo fix this, press the \"Next>\" button, or remove transitions manually by clicking on a transition in the navigator pane or diagram and presing the \"Del\" key. " },
        { "CrTooManyClasses_head" ,
	        "Reduzieren Sie die Anzahl der Klassen im Diagramm <ocl>self</ocl>" },
        { "CrTooManyClasses_desc" ,
                "There are too many classes in <ocl>self</ocl>.  If one class diagram has too many classes it may become very difficult for humans to understand. \n\nDefining an understandable set of class diagrams is an important part of your design. \n\nTo fix this, press the \"Next>\" button, or remove classes manually by clicking on a class in the navigator pane or diagram and presing the \"Del\" key.  Or you can make a new diagram..." },
        { "CrNoTransitions_head" ,
	        "F\u00fcgen Sie \u00dcberg\u00e4nge zu <ocl>self</ocl hinzu" },
        { "CrNoTransitions_desc" ,
	        "Der Zustand <ocl>self</ocl> hat keine ein- oder ausgehenden \u00dcberg\u00e4nge. Normalerweise haben Zust\u00e4nde ein- und ausgehende Zustands\u00fcberg\u00e4nge. \n\nDie Definition von vollst\u00e4ndigen Zust\u00e4nden und \u00dcberg\u00e4ngen ist erforderlich um das Verhalten Ihres Entwurfs vollst\u00e4ndig zu beschreiben. Um dieses Problem zu beheben dr\u00fccken Sie den \"Next\" Button oder f\u00fcgen Sie die \u00dcberg\u00e4nge per Hand hinzu. Dazu klicken Sie auf das \u00dcbergangs Icon im Toolbar und erzeugen Sie die \u00dcberg\u00e4nge von und zu <ocl>self</ocl> per Drag und Drop." },
        { "CrNoIncomingTransitions_head" ,
	        "F\u00fcgen Sie die eingehenden \u00dcberg\u00e4nge zu <ocl>self</ocl> hinzu" },	
        { "CrNoIncomingTransitions_desc" ,
                "MState <ocl>self</ocl> has no incoming transitions. Normally states have both incoming and outgoing transitions. \n\nDefining complete state transitions is needed to complete the behavioral specification part of your design. Without incoming transitions, this state can never be reached.\n\nTo fix this, press the \"Next>\" button, or add transitions manually by clicking on transition tool in the tool bar and dragging from another state to <ocl>self</ocl>. " },
        { "CrNoOutgoingTransitions_head" ,
	        "F\u00fcgen Sie die ausgehenden \u00dcberg\u00e4nge zu <ocl>self</ocl> hinzu" },
        { "CrNoOutgoingTransitions_desc" ,
                "MState <ocl>self</ocl> has no Outgoing transitions. Normally states have both incoming and outgoing transitions. \n\nDefining complete state transitions is needed to complete the behavioral specification part of your design.  Without outgoing transitions, this state is a \"dead\" state that can naver be exited.\n\nTo fix this, press the \"Next>\" button, or add transitions manually by clicking on transition tool in the tool bar and dragging from another state to <ocl>self</ocl>. " },
        { "CrMultipleInitialStates_head" ,
                "Remove Extra Initial States" },
        { "CrMultipleInitialStates_desc" ,
                "There are multiple, ambiguous initial states in this machine. Normally each state machine or composite state has one initial state. \n\nDefining unabiguous states is needed to complete the behavioral specification part of your design.\n\nTo fix this, press the \"Next>\" button, or add manually select one of the extra initial states and remove it. " },
        { "CrNoInitialState_head" ,
	        "F\u00fcgen Sie einen MAnfangszustand hinzu" },
        { "CrNoInitialState_desc" ,
                "There is no initial state in this machine or composite state. Normally each state machine or composite state has one initial state. \n\nDefining unabiguous states is needed to complete the behavioral specification part of your design.\n\nTo fix this, press the \"Next>\" button, or add manually select initial state from the tool bar and place it in the diagram. " },
        { "CrNoTriggerOrGuard_head" ,
	        "F\u00fcgen Sie einen Ausl\u00f6ser oder eine Bedingung zum \u00dcbergang hinzu" },
        { "CrNoTriggerOrGuard_desc" ,
                "The highlighted Transition is incomplete because it has no trigger or guard condition.  Triggers are events that cause a transition to be taken.  Guard conditions must be true for the transition to be taken.  If only a guard is used, the transition is taken when the condition becomes true.\n\nThis problem must be resolved to complete the state machine.\n\nTo fix this, select the Transition and use the \"Properties\" tab, or select the Transition and type some text of the form:\nTRIGGER [GUARD] / ACTION\nWhere TRIGGER is an event name, GUARD is a boolean expression, and ACTION is an action to be performed when the MTransition is taken.  All three parts are optional." },
        { "CrNoGuard_head" ,
	        "F\u00fcgen Sie eine Bedingung zum \u00dcbergang hinzu" },
        { "CrNoGuard_desc" ,
                "The highlighted Transisition is incomplete because it has no guard condition.  MGuard conditions must be true for the transition to be taken.  If only a guard is used, the transition is taken when the condition becomes true.\n\nThis problem must be resolved to complete the state machine.\n\nTo fix this, select the MTransition and use the \"Properties\" tab, or select the MTransition and type some text of the form:\n[GUARD]\nWhere GUARD is a boolean expression." },
        { "CrInvalidFork_head" ,
                "Change Fork Transitions" },
        { "CrInvalidFork_desc" ,
                "This fork state has an invalid number of transitions. Normally fork states have one incoming and two or more outgoing transitions. \n\nDefining correct state transitions is needed to complete the  behavioral specification part of your design.  \n\nTo fix this, press the \"Next>\" button, or remove transitions  manually by clicking on transition in the diagram and pressing the Delete key. " },
        { "CrInvalidJoin_head" ,
                "Change Join Transitions" },
        { "CrInvalidJoin_desc" ,
                "This join state has an invalid number of transitions. Normally join states have two or more incoming and one outgoing transitions. \n\nDefining correct state transitions is needed to complete the  behavioral specification part of your design.  \n\nTo fix this, press the \"Next>\" button, or remove transitions  manually by clicking on transition in the diagram and pressing the Delete key. " },
        { "CrInvalidBranch_head" ,
                "Change Branch Transitions" },
        { "CrInvalidBranch_desc" ,
                "This branch state has an invalid number of transitions. Normally branch states have one incoming and two or more outgoing transitions. \n\nDefining correct state transitions is needed to complete the  behavioral specification part of your design.  \n\nTo fix this, press the \"Next>\" button, or remove transitions  manually by clicking on transition in the diagram and pressing the Delete key, or add transitions using the transition tool. " },
        { "CrEmptyPackage_head" ,
	        "F\u00fcgen Sie Elemente zum Paket <ocl>self</ocl> hinzu" },
        { "CrEmptyPackage_desc" ,
                "You have not yet put anything in package <ocl>self</ocl>. Normally packages contain groups of related classes.\n\nDefining and using packages is key to making a maintainable design. \n\nTo fix this, select package <ocl>self</ocl> in the navigator panel and add  diagrams or model elements such as classes or use cases. " },
        { "CrNoOperations_head" ,
	        "F\u00fcgen Sie Operationen zu <ocl>self</ocl> hinzu" },
        { "CrNoOperations_desc" ,
                "You have not yet specified operations for <ocl>self</ocl>. Normally classes provide operations that define their behavior. \n\nDefining operations is needed to complete the behavioral specification part of your design. \n\nTo fix this, press the \"Next>\" button, or add operations manually by clicking on <ocl>self</ocl> in the navigator pane and using the Create menu to make a new operations. " },
        { "CrConstructorNeeded_head" ,
	        "F\u00fcgen Sie einen Konstruktor zu <ocl>self</ocl> hinzu" },
        { "CrConstructorNeeded_desc" ,
                "You have not yet defined a constructor for class <ocl>self</ocl>. Constructors initialize new instances such that their attributes have valid values.  This class probably needs a constructor because not all of its attributes have initial values. \n\nDefining good constructors is key to establishing class invariants, and class invariants are a powerful aid in writing solid code. \n\nTo fix this, press the \"Next>\" button, or add a constructor manually by clicking on <ocl>self</ocl> in the navigator pane and using the Create menu to make a new constructor. " },
        { "CrNameConfusion_head" ,
	        "\u00c4ndern Sie den Namen um Verwirrung zu vermeiden" },
        { "CrNameConfusion_desc" ,
                "Names should be clearly distinct from each other. These two names are so close to each other that readers might be confused.\n\nClear and unambiguous naming is key to code generation and the understandability and maintainability of the design. \n\nTo fix this, use the \"Next>\" button, or manually select the elements and use the Properties tab to change their names.  Avoid names that differ from other names only in capitalization, or use of underscore characters, or by only one character." },
        { "CrMergeClasses_head" ,
	        "Sie sollten erw\u00e4gen die Klassen zusammenzufassen" },
        { "CrMergeClasses_desc" ,
                "The highlighted class, <ocl>self</ocl>, only participates in one association and that association is one-to-one with another class.  Since instances of these two classes must always be created together and destroyed together, combining these classes might simplify your design without loss of any representation power.  However, you may find the combined class too large and complex, in which case separating them is usually better.\n\nOrganizing classes to manage complexity of the design is always important, especially when the design is already complex. \n\nTo fix this, click on the \"Next>\" button, or manually add the attribues and operations of the highlighted class to the other class, then remove the highlighted class from the project. " },
        { "CrSubclassReference_head" ,
                "Remove Reference to Specific Subclass" },
        { "CrSubclassReference_desc" ,
                "Class <ocl>self</ocl> has a reference to one of it's subclasses. Normally all subclasses should be treated \"equally\" by the superclass.  This allows for addition of new subclasses without modification to the superclass. \n\nDefining the associations between objects is an important part of your design.  Some patterns of associations are easier to maintain than others, depending on the natre of future changes. \n\nTo fix this, press the \"Next>\" button, or remove the association  manually by clicking on it in the diagram and pressing Delete. " },
        { "CrComponentWithoutNode_head" ,
	        "Komponenten sind normalerweise innerhalb von Knoten" },
        { "CrComponentWithoutNode_desc" ,
                "There are nodes in the diagram. So you have got a real\n deployment-diagram, and in deployment-diagrams components\n normally resides on nodes." },
        { "CrCompInstanceWithoutNode_head" ,
	        "Instanzen von Komponenten sind normalerweise innerhalb von Knoten" },
        { "CrCompInstanceWithoutNode_desc" ,
                "There are node-instances in the Diagram. So you have got a real\n deployment-diagram, and in deployment-diagrams Component-instances\n normally resides on node-instances." },
        { "CrClassWithoutComponent_head" ,
	        "Klassen sind normalerweise innerhalb von Komponenten" },
        { "CrClassWithoutComponent_desc" ,
	        " In Verteilungsdiagrammen sind Klassen normalerweise innerhalb von Komponenten" },
        { "CrInterfaceWithoutComponent_head" ,
	        "Interfaces sind normalerweise innerhalb von Komponenten" },
        { "CrInterfaceWithoutComponent_desc" ,
	        " In Verteilungsdiagrammen sind Interfaces normalerweise innerhalb von Komponenten" },
        { "CrObjectWithoutComponent_head" ,
	        "Objekte sind normalerweise innerhalb von Komponenten" },
        { "CrObjectWithoutComponent_desc" ,
	        " In Verteilungsdiagrammen sind Objekte normalerweise innerhalb von Komponenten oder Instanzen von Komponenten" },
        { "CrNodeInsideElement_head" ,
                "Nodes normally have no enclosers" },
        { "CrNodeInsideElement_desc" ,
                " Nodes normally are not inside other Elements. They represent\n run-time physical objects with a processing resource, generally having\n at least a memory and often processing capability as well." },
        { "CrNodeInstanceInsideElement_head" ,
                "NodeInstances normally have no enclosers" },
        { "CrNodeInstanceInsideElement_desc" ,
                " NodeInstances normally are not inside other Elements. They represent\n run-time physical objects with a processing resource, generally having\n at least a memory and often processing capability as well." },
        { "CrWrongLinkEnds_head" ,
                "LinkEnds have not the same locations" },
        { "CrWrongLinkEnds_desc" ,
                " In deployment-diagrams objects can reside either on components\n or on component-instances. So it is not possible to have two objects\n connected with a Link, while one object resides on an component and\n an the other obejct on a component-instance.\n\n\n To fix this remove one object of the two connected objects from its location to an element that has the\n same type as the location of the other object" },
        { "CrInstanceWithoutClassifier_head" ,
                "Set classifier" },
        { "CrInstanceWithoutClassifier_desc" ,
                " Instances have a classifier" },
        { "CrCallWithoutReturn_head" ,
                "Missing return-actions" },
        { "CrCallWithoutReturn_desc" ,
                "Every call- or send-action requires a return-action,\n but this Link has no return-action.\n" },
        { "CrReturnWithoutCall_head" ,
                "Missing call(send)-action" },
        { "CrReturnWithoutCall_desc" ,
                "Every return-action requires a call- or send-action,\n but this Link has no corresponding call- or send-action.\n" },
        { "CrLinkWithoutStimulus_head" ,
                "No Stimuli on these links" },
        { "CrLinkWithoutStimulus_desc" ,
                "In sequence-diagrams a sender-object sends stimuli\nto a receiving object over a link. The link is only the communication-\nconnection, so a stimulus is needed." },
        { "CrSeqInstanceWithoutClassifier_head" ,
                "Set classifier" },
        { "CrSeqInstanceWithoutClassifier_desc" ,
                " Instances have a classifier" },
        { "CrStimulusWithWrongPosition_head" ,
                "Wrong position of these stimuli" },
        { "CrStimulusWithWrongPosition_desc" ,
                "In sequence-diagrams the sender-side oh the communication-connections oh these\nstimuli are connected at the beginning of an activation. To be a sender an object must\nhave a focus-of-control first." },
        { "CrUnconventionalOperName_head" ,
	        "W\u00e4hlen Sie einen besseren Namen f\u00fcr die Operation" },
        { "CrUnconventionalOperName_desc" ,
	        "Normalerweise beginnen Namen von Operationen mit einem Kleinbuchstaben. Der Name '<ocl>self</ocl>' ist ungew\u00f6hnlich da er das nicht tut.\n\nDas Einhalten von sinnvollen Konventionen zur Namensgebung hilft die Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs zu verbessern. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie <ocl>self</ocl> per Hand und benutzen Sie die 'Eigenschaften' Registerkarte um einen anderen Namen zu vergeben." },
        { "CrUnconventionalAttrName_head" ,
	        "W\u00e4hlen Sie einen besseren Namen f\u00fcr das Attribut" },
        { "CrUnconventionalAttrName_desc" ,
	        "Normalerweise beginnen Namen von Attributen mit einem Kleinbuchstaben. Der Name '<ocl>self</ocl>' ist ungew\u00f6hnlich da er das nicht tut.\n\nDas Einhalten von sinnvollen Konventionen zur Namensgebung hilft die Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs zu verbessern. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie <ocl>self</ocl> per Hand und benutzen Sie die 'Eigenschaften' Registerkarte um einen anderen Namen zu vergeben." },
        { "CrUnconventionalClassName_head" ,
	        "Schreiben Sie den Klassennamen <ocl>self</ocl> gro\u00df" },
        { "CrUnconventionalClassName_desc" ,
	        "Normalerweise beginnen Klassennamen mit einem Gro\u00dfbuchstaben. Der Name '<ocl>self</ocl>' ist ungew\u00f6hnlich, da er nicht mit einem Gro\u00dfbuchstaben beginnt.\n\nDas Einhalten von sinnvollen Konventionen zur Namensgebung hilft die Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs zu verbessern. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie <ocl>self</ocl> per Hand und benutzen Sie die 'Eigenschaften' Registerkarte um einen anderen Namen zu vergeben." },
        { "CrUnconventionalPackName_head" ,
	        "W\u00e4hlen Sie einen anderen Paketnamen f\u00fcr <ocl>self</ocl>" },
        { "CrUnconventionalPackName_desc" ,
	        "Normalerweise werden Paketnamen durchg\u00e4ngig klein geschrieben mit Punkten, welche \"geschachtelte\" Pakete anzeigen. Der Name '<ocl>self</ocl>' ist ungew\u00f6hnlich, da er nicht aus kleinen Buchstaben und Punkten besteht.\n\nDas Einhalten von sinnvollen Konventionen zur Namensgebung hilft die Verst\u00e4ndlichkeit und Wartbarkeit des Entwurfs zu verbessern. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder selektieren Sie <ocl>self</ocl> per Hand und benutzen Sie die 'Eigenschaften' Registerkarte, um einen anderen Namen zu vergeben." },
        { "CrClassMustBeAbstract_head" ,
                "Class Must be Abstract" },
        { "CrClassMustBeAbstract_desc" ,
                "Classes that include or inherit abstract methods from base classes or interfaces must be marked Abstract.\n\nDeciding which classes are abstract or concrete is a key part of class hierarchy design.\n\nTo fix this, use the \"Next>\" button, or manually select the class and use the properties tab to add the Abstract keyword, or manually override each abstract operation that is inherited from a base class or interface." },
        { "CrReservedName_head" ,
                "Change <ocl>self</ocl> to a Non-Reserved Word" },
        { "CrReservedName_desc" ,
                "\"<ocl>self</ocl>\" is a reserver word or very close to one.  The names of model elements must not conflict with reserved words of programming languages or UML.\n\nUsing legal names is needed to generate compilable code. \n\nTo fix this, use the \"Next>\" button, or manually select the highlighted element and use the Properties tab to give it a different name." },
        { "CrMultipleInheritance_head" ,
	        "Benutzen Sie bei Mehrfachvererbung Interfaces" },
        { "CrMultipleInheritance_desc" ,
	        "<ocl>self</ocl> hat mehrere Basisklassen, aber Java unterst\u00fctzt keine Mehrfachvererbung. Sie m\u00fcssen stattdessen Interfaces benutzen. \n\nDiese \u00c4nderung ist erforderlich bevor Sie Java Code generieren k\u00f6nnen.\n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button oder entfernen Sie (1) eine der Basisklassen per Hand, definieren Sie (2) optional ein neues Interface mit den selben Methodenk\u00f6pfen, f\u00fcgen Sie es (3) als Interface zu <ocl>self</ocl> hinzu, und bewegen Sie (4) die Methodenr\u00fcmpfe der alten Basisklasse in <ocl>self</ocl>." },
        { "CrIllegalName_head" ,
	        "W\u00e4hlen Sie einen erlaubten Namen" },
        { "CrIllegalName_desc" ,
	        "Die Namen von Modellelementen m\u00fcssen aus Folgen von Buchstaben, Ziffern und Unterstrichen bestehen. Sie d\u00fcrfen keine Satzzeichen enthalten.\n\nZur Generierung von compilierbarem Code sind g\u00fcltige Namen erforderlich. \n\nUm dieses Problem zu beheben benutzen Sie den \"Next\" Button um das hervorgehobene Element zu selektieren und benutzen Sie die 'Eigenschaften' Registerkarte um einen anderen Namen zu vergeben." },
        { "CrConsiderSingleton_head" ,
	        "Sie sollten erw\u00e4gen das 'Singleton' Entwurfsmuster zu benutzen" },
        { "CrConsiderSingleton_desc" ,
                "This class has no attributes or associations that are navigable away from instances of this class.  This means that every instance of this class will be equal() to every other instance, since there will be no instance variables to differentiate them. If this not your intent, you should define some attributes or associations that will represent differences bewteen instances. If there are no attributes or associations that differentiate instances, the you shoudld consider having exatly one instance of this class, as in the Singleton Pattern.\n\nDefining the multiplicity of instances is needed to complete the information representation part of your design.  Using the Singleton Pattern can save time and memory space.\n\nTo automatically apply the Singleton Pattern, press the \"Next>\" button; or manually (1) mark the class with the Singlton stereotype, (2) add a static variable that holds one instance of this class, (3) and make all constructors private.\n\nTo learn more about the Singleton Pattern, press the MoreInfo icon." },
        { "CrSingletonViolated_head" ,
                "Singleton MStereotype Violated" },
        { "CrSingletonViolated_desc" ,
                "This class is marked with the Singleton stereotype, but it does not satisfy the constraints imposed on singletons.  A singleton class can have at most one instance.  This means that the class must have (1) a static variable holding the instance, (2) only private constructors so that new instances cannot be made by other code, and (3) there must be at least one constructor to override the default constructor.\n\nWhenever you mark a class with a stereotype, the class should satisfy all constraints of the stereotype.  This is an important part of making a self-consistent and understangle design. Using the Singleton Pattern can save time and memory space.\n\nIf you no longer want this class to be a Singleton, remove the Singleton stereotype by clicking on the class and deleting Singleton from the Props tab. \nTo automatically apply the Singleton Pattern, press the \"Next>\" button; or manually (1) mark the class with the Singlton stereotype, (2) add a static variable that holds one instance of this class, (3) and make all constructors private.\n\nTo learn more about the Singleton Pattern, press the MoreInfo icon." },
        { "CrNodesOverlap_head" ,
                "Clean Up Diagram" },
        { "CrNodesOverlap_desc" ,
                "Some of the objects in this diagram overlap and obscure each other. This may hide important information and make it difficult for humans to understand. A neat appearance may also make your diagrams more influencial on other designers, implementors, and decision makers.\n\nConstructing an understandable set of class diagrams is an important part of your design. \n\nTo fix this, move the highlighted nodes in the digragm." },
        { "CrZeroLengthEdge_head" ,
                "Make Edge More Visible" },
        { "CrZeroLengthEdge_desc" ,
                "This edge is too small to see easily. This may hide important information and make it difficult for humans to understand. A neat appearance may also make your diagrams more influencial on other designers, implementors, and decision makers.\n\nConstructing an understandable set of diagrams is an important part of your design. \n\nTo fix this, move one or more nodes so that the highlighted edges will be longer, or click in the center of the edge and drag to make a new vertex." },
        //
        //   these phrases should be localized here
        //      not in the following check list section
        { "Naming", "Namensgebung" },
        { "Encoding", "Kodierung" },
        { "Value", "Wert" },
        { "Location", "Ort" },
        { "Updates", "Updates" },
        { "General", "Allgemein" },
        { "Actions" , "Aktionen" },
        { "Transitions", "Zustands\u00fcberg\u00e4nge" },
        { "Structure", "Struktur" },
        { "Trigger", "Ausl\u00f6ser" },
        { "MGuard", "Bedingung" },
        //
        //   The following blocks define the UML related
        //      Checklists.  The key is the name of
        //      the non-deprecated implmenting class,
        //      the value is an array of categories which
        //      are each an array of Strings.  The first
        //      string in each category is the name of the
        //      category and should not be localized here
        //      but should be in the immediate preceeding
        //      section
        //
        { "ChClass",
            new String[][] {
                new String[] { "Naming",
		  "Beschreibt der Name '<ocl>self</ocl>' die Klasse auf klare Weise?",
		  "Ist '<ocl>self</ocl>' ein Hauptwort oder ein Hauptsatz?",
		  "K\u00f6nnte der Name '<ocl>self</ocl>' mi\u00dfinterpretiert werden oder etwas anderes bedeuten?"
                },
                new String[] { "Encoding",
		  "Sollte <ocl>self</ocl> eine eigene Klasse sein oder ein einfaches Attribut einer anderen Klasse?",
		  "Macht <ocl>self</ocl> genau eine Sache, und diese ordentlich?",
		  "K\u00f6nnte <ocl>self</ocl> in zwei oder mehrere Klassen unterteilt werden?"
                },
                new String[] { "Value",
		  "Haben alle Attribute von <ocl>self</ocl> sinnvolle Startwerte?",
		  "K\u00f6nnten Sie eine Invariante f\u00fcr diese Klasse schreiben?",
		  "Etablieren alle Konstruktoren die Invariante dieser Klasse?",
		  "Stellen alle Operationen sicher, dass die Invariante der Klasse erhalten bleibt?"
                },
                new String[] { "Location",
		  "K\u00f6nnte <ocl>self</ocl> auch an einer anderen Stelle in der Klassenhierachie definiert werden?",
		  "Ist geplant, da\u00df <ocl>self</ocl> Unterklassen haben soll?",
		  "K\u00f6nnte <ocl>self</ocl> aus dem Modell entfernt werden?", 
		  "Gibt es eine andere Klasse in dem Modell, welche \u00fcberarbeitet oder entfernt werden sollte, weil sie den gleichen Zweck wie <ocl>self</ocl> erf\u00fcllt?"
                },
                new String[] { "Updates",
		  "Aus welchen Gr\u00fcnden wird eine Instanz von <ocl>self</ocl> aktualisiert?",
		  "Gibt es ein anderes Objekt, welches aktualisiert werden mu\u00df wenn <ocl>self</ocl> aktualisiert wird?"
                }
            }
        },
        { "ChAttribute",
            new String[][] {
                new String[] { "Naming",
		  "Beschreibt der Name '<ocl>self</ocl>' das Attribut auf klare Weise?",
		  "Ist '<ocl>self</ocl>' ein Hauptwort oder ein Hauptsatz?",
		  "K\u00f6nnte der Name '<ocl>self</ocl>' mi\u00dfinterpretiert werden oder etwas anderes bedeuten?"
                },
                new String[] { "Encoding",
		  "Ist der Typ <ocl>self.type</ocl> zu beschr\u00e4nkt um alle sinnvollen Werte von <ocl>self</ocl> enthalten?",
		  "Erlaubt der Typ <ocl>self.type</ocl> Werte, welche niemals korrekt f\u00fcr <ocl>self</ocl> sein k\u00f6nnen?",
		  "K\u00f6nnte <ocl>self</ocl> mit einem anderen Attribut von <ocl>self.owner</ocl> zusammengefasst werden (z.B. {owner.structuralFeature})?",
		  "K\u00f6nnte <ocl>self</ocl> in zwei oder mehrere Teile zerlegt werden (z.B. kann eine Telefonnummer in Vorwahl und Rufnummer zerlegt werden)?",
		  "K\u00f6nnte <ocl>self</ocl> aus anderen Attributen errechnet werden, anstatt gespeichert zu werden?"
                },
                new String[] { "Value",
		  "Sollte  <ocl>self</ocl> einen Start- (oder Default-) Wert haben?",
		  "Ist der Startwert von <ocl>self.initialValue</ocl> korrekt?",
		  "K\u00f6nnten Sie einen verst\u00e4ndlichen Ausdruck schreiben, welcher \u00fcberpr\u00fcft ob <ocl>self</ocl> korrekt ist?"
                },
                new String[] { "Location",
		  "K\u00f6nnte <ocl>self</ocl> in einer anderen Klasse definiert werden, welche mit <ocl>self.owner</ocl> assoziiert ist?",
		  "K\u00f6nnte <ocl>self</ocl> in der Vererbungshierachie nach oben bewegt werden um auch auf owner.name und andere Klassen angewendet zu werden?", 
		  "Kann <ocl>self</ocl> auf alle Instanzen der Klasse <ocl>self.owner</ocl>, inklusive der Unterklassen, angewendet werden?",
	          "K\u00f6nnte <ocl>self</ocl> aus dem Modell entfernt werden?",
	          "Gibt es ein anderes Attribut in dem Modell, welches \u00fcberarbeitet oder entfernt werden sollte, weil es den gleichen Zweck wie <ocl>self</ocl> erf\u00fcllt?"
                },
                new String[] { "Updates",
		  "Aus welchen Gr\u00fcnden wird <ocl>self</ocl> upgedated?",
		  "Gibt es ein anderes Attribut, welches upgedated werden muss, wenn <ocl>self</ocl> upgedated wird?",
		  "Gibt es eine Methode welche aufgerufen werden sollte, wenn <ocl>self</ocl> upgedated wird?",
	          "Gibt es eine Methode welche aufgerufen werden sollte, wenn <ocl>self</ocl> einen bestimmten Wert erh\u00e4lt?"
                }
            }
        },
        { "ChOperation",
            new String[][] {
                new String[] { "Naming",
		  "Beschreibt der Name '<ocl>self</ocl>' die Operation auf klare Weise?",
		  "Ist '<ocl>self</ocl>' ein T\u00e4tigkeitswort oder ein Hauptsatz?", 
		  "K\u00f6nnte der Name '<ocl>self</ocl>' mi\u00dfinterpretiert werden oder etwas anderes bedeuten?",
		  "Macht <ocl>self</ocl> eine Sache und diese ordentlich?"
                },
                new String[] { "Encoding",
		  "Ist der R\u00fcckgabewert '<ocl>self.returnType</ocl>' zu beschr\u00e4nkt um alle m\u00f6glichen R\u00fcckgabewert von <ocl>self</ocl> zu enthalten?",
		  "Erlaubt der Typ '<ocl>self.returnType</ocl>' R\u00fcckgabewerte, welche niemals korrekt sein k\u00f6nnen?",
		  "K\u00f6nnte <ocl>self</ocl> mit einer anderen Operation von <ocl>self.owner</ocl> zusammengelegt werden (z.B. <ocl sep=', '>self.owner.behavioralFeature</ocl>)?",
		  "K\u00f6nnte <ocl>self</ocl> in zwei oder mehrere Teile zerlegt werden (z.B. Preprocessing, Hauptteil und Postprocessing)?",
		  "K\u00f6nnte <ocl>self</ocl> durch eine Folge von Aufrufen einfacherer Operationen ersetzt werden?",
		  "K\u00f6nnte <ocl>self</ocl> mit anderen Operationen zusammengelegt werden um die Anzahl von Aufrufen zu reduzieren?"
                },
                new String[] { "Value",
		  "Kann <ocl>self</ocl> alle m\u00f6glichen Eingaben verarbeiten?",
		  "Gibt es spezielle Eingaben, die separat behandelt werden m\u00fcssen?",
		  "K\u00f6nnten Sie einen verst\u00e4ndlichen Ausdruck schreiben, welcher die Argumente von <ocl>self</ocl> auf Korrektheit \u00fcberpr\u00fcft?",
		  "K\u00f6nnen Sie die Vorbedingungen von <ocl>self</ocl> definieren?",
		  "K\u00f6nnen Sie die Nachbedingungen von <ocl>self</ocl> definieren?",
		  "Wie wird sich <ocl>self</ocl> verhalten, wenn die Vorbedingungen verletzt sind?",
		  "Wie wird sich <ocl>self</ocl> verhalten, wenn die Nachbedingungen verletzt sind?"
                },
                new String[] { "Location",
		  "K\u00f6nnte <ocl>self</ocl> in einer anderen Klasse definiert werden, welche mit <ocl>self.owner</ocl> assoziiert ist?",       
		  "K\u00f6nnte <ocl>self</ocl> in der Vererbungshierachie nach oben bewegt werden um auch auf <ocl>self.owner</ocl> und andere Klassen angewendet zu werden?",
                  "Kann <ocl>self</ocl> auf alle Instanzen der Klasse <ocl>self.owner</ocl>, inklusive der Unterklassen, angewendet werden?",
	          "K\u00f6nnte <ocl>self</ocl> aus dem Modell entfernt werden?",
	          "Gibt es eine andere Operation in dem Modell, welche \u00fcberarbeitet oder entfernt werden sollte, weil sie den gleichen Zweck wie <ocl>self</ocl> erf\u00fcllt?"
                }
            }
        },
        { "ChAssociation",
            new String[][] {
                new String[] { "Naming",
		  "Beschreibt der Name '<ocl>self</ocl>' die Klasse auf klare Weise?",
		  "Ist '<ocl>self</ocl>' ein Hauptwort oder ein Hauptsatz?",
		  "K\u00f6nnte der Name '<ocl>self</ocl>' mi\u00dfinterpretiert werden oder etwas anderes bedeuten?"
                },
                new String[] { "Encoding",
		  "Sollte <ocl>self</ocl> eine eigene Klasse sein, oder ein einfaches Attribut einer anderen Klasse?",
		  "Macht <ocl>self</ocl> genau eine Sache, und diese ordentlich?",
		  "K\u00f6nnte <ocl>self</ocl> in zwei oder mehrere Klassen unterteilt werden?"
                },
                new String[] { "Value",
                  "Haben alle Attribute von <ocl>self</ocl> sinnvolle Startwerte?",
		  "K\u00f6nnten Sie eine Invariante f\u00fcr diese Klasse schreiben?",
		  "Etablieren alle Konstruktoren die Invariante dieser Klasse?",
		  "Stellen alle Operationen sicher, dass die Invariante der Klasse erhalten bleibt?"
                },
                new String[] { "Location",
                  "K\u00f6nnte <ocl>self</ocl> auch an einer anderen Stelle in der Klassenhierachie definiert werden?",
		  "Ist geplant, da\u00df <ocl>self</ocl> Unterklassen haben soll?",
		  "K\u00f6nnte <ocl>self</ocl> aus dem Modell entfernt werden?", 
		  "Gibt es eine andere Klasse in dem Modell, welche \u00fcberarbeitet oder entfernt werden sollte, weil sie den gleichen Zweck wie <ocl>self</ocl> erf\u00fcllt?"
                },
                new String[] { "Updates",
                  "Aus welchen Gr\u00fcnden wird eine Instanz von <ocl>self</ocl> upgedatet?",
		  "Gibt es ein anderes Objekt, welches upgedatet werden mu\u00df wenn <ocl>self</ocl> upgedatet wird?"
                }
            }
        },
        { "ChInterface",
            new String[][] {
                new String[] { "Naming",
                  "Beschreibt der Name '<ocl>self</ocl>' die Klasse auf klare Weise?",
		  "Ist '<ocl>self</ocl>' ein Hauptwort oder ein Hauptsatz?",
		  "K\u00f6nnte der Name '<ocl>self</ocl>' mi\u00dfinterpretiert werden oder etwas anderes bedeuten?"
                },
                new String[] { "Encoding",
        	  "Sollte <ocl>self</ocl> eine eigene Klasse sein, oder ein einfaches Attribut einer anderen Klasse?",
		  "Macht <ocl>self</ocl> genau eine Sache, und diese ordentlich?",
		  "K\u00f6nnte <ocl>self</ocl> in zwei oder mehrere Klassen unterteilt werden?"
                },
                new String[] { "Value",
                  "Haben alle Attribute von <ocl>self</ocl> sinnvolle Startwerte?",
	          "K\u00f6nnten Sie eine Invariante f\u00fcr diese Klasse schreiben?",
		  "Etablieren alle Konstruktoren die Invariante dieser Klasse?",
		  "Stellen alle Operationen sicher, dass die Invariante der Klasse erhalten bleibt?"
                },
                new String[] { "Location",
                  "K\u00f6nnte <ocl>self</ocl> auch an einer anderen Stelle in der Klassenhierachie definiert werden?",
		  "Ist geplant, da\u00df <ocl>self</ocl> Unterklassen haben soll?",
		  "K\u00f6nnte <ocl>self</ocl> aus dem Modell entfernt werden?", 
		  "Gibt es eine andere Klasse in dem Modell, welche \u00fcberarbeitet oder entfernt werden sollte, weil sie den gleichen Zweck wie <ocl>self</ocl> erf\u00fcllt?"
                },
                new String[] { "Updates",
		  "Aus welchen Gr\u00fcnden wird eine Instanz von <ocl>self</ocl> aktualisiert?",
		  "Gibt es ein anderes Objekt, welches aktualisiert werden mu\u00df wenn <ocl>self</ocl> aktualisiert wird?"
                }
            }
        },
        { "ChInstance",
            new String[][] {
                new String[] { "General",
                  "Does this instance <ocl>self</ocl> clearly describe the instance?"
                },
                new String[] { "Naming",
                  "Does the name '<ocl>self</ocl>' clearly describe the instance?",
                  "Does '<ocl>self</ocl>' denote a state rather than an activity?",
                  "Could the name '<ocl>self</ocl>' be misinterpreted to mean something else?"
                },
                new String[] { "Structure",
                  "Should <ocl>self</ocl> be its own state or could it be merged with another state?",
                  "Does <ocl>self</ocl> do exactly one thing and do it well?",
                  "Could <ocl>self</ocl> be broken down into two or more states?",
                  "Could you write a characteristic equation for <ocl>self</ocl>?",
                  "Does <ocl>self</ocl> belong in this state machine or another?",
                  "Should <ocl>self</ocl> be be an initial state?",
                  "Is some state in another machine exclusive with <ocl>self</ocl>?"
                },
                new String[] { "Actions",
                  "What action should be preformed on entry into <ocl>self</ocl>?",
                  "Should some attribute be updated on entry into <ocl>self</ocl>?",
                  "What action should be preformed on exit from <ocl>self</ocl>?",
                  "Should some attribute be updated on exit from <ocl>self</ocl>?",
                  "What action should be preformed while in <ocl>self</ocl>?",
                  "Do state-actions maintain <ocl>self</ocl> as the current state?"
                },
                new String[] { "Transitions",
                  "Should there be another transition into <ocl>self</ocl>?",
                  "Can all the transitions into <ocl>self</ocl> be used?",
                  "Could some incoming transitions be combined?",
                  "Should there be another transition out of <ocl>self</ocl>?",
                  "Can all the transitions out of <ocl>self</ocl> be used?",
                  "Is each outgoing transition exclusive?",
                  "Could some outgoing transitions be combined?"
                }
            }
        },
        { "ChLink",
            new String[][] {
                new String[] { "Naming",
                  "Beschreibt der Name '<ocl>self</ocl>' die Klasse auf klare Weise?",
		  "Ist '<ocl>self</ocl>' ein Hauptwort oder ein Hauptsatz?",
		  "K\u00f6nnte der Name '<ocl>self</ocl>' mi\u00dfinterpretiert werden oder etwas anderes bedeuten?"
                },
                new String[] { "Encoding",
                  "Sollte <ocl>self</ocl> eine eigene Klasse sein, oder ein einfaches Attribut einer anderen Klasse?",
		  "Macht <ocl>self</ocl> genau eine Sache, und diese ordentlich?",
		  "K\u00f6nnte <ocl>self</ocl> in zwei oder mehrere Klassen unterteilt werden?"
                },
                new String[] { "Value",
		  "Haben alle Attribute von <ocl>self</ocl> sinnvolle Startwerte?",
		  "K\u00f6nnten Sie eine Invariante f\u00fcr diese Klasse schreiben?",
		  "Etablieren alle Konstruktoren die Invariante dieser Klasse?",
		  "Stellen alle Operationen sicher, dass die Invariante der Klasse erhalten bleibt?"
                },
                new String[] { "Location",
		  "K\u00f6nnte <ocl>self</ocl> auch an einer anderen Stelle in der Klassenhierachie definiert werden?",
		  "Ist geplant, da\u00df <ocl>self</ocl> Unterklassen haben soll?",
		  "K\u00f6nnte <ocl>self</ocl> aus dem Modell entfernt werden?", 
		  "Gibt es eine andere Klasse in dem Modell, welche \u00fcberarbeitet oder entfernt werden sollte, weil sie den gleichen Zweck wie <ocl>self</ocl> erf\u00fcllt?"
                },
                new String[] { "Updates",
		  "Aus welchen Gr\u00fcnden wird eine Instanz von <ocl>self</ocl> aktualisiert?",
		  "Gibt es ein anderes Objekt, welches aktualisiert werden mu\u00df wenn <ocl>self</ocl> aktualisiert wird?"
                }
            }
        },
        { "ChState",
            new String[][] {
                new String[] { "Naming",
                  "Does the name '<ocl>self</ocl>' clearly describe the state?",
                  "Does '<ocl>self</ocl>' denote a state rather than an activity?",
                  "Could the name '<ocl>self</ocl>' be misinterpreted to mean something else?"
                },
                new String[] { "Structure",
                  "Should <ocl>self</ocl> be its own state or could it be merged with another state?",
                  "Does <ocl>self</ocl> do exactly one thing and do it well?",
                  "Could <ocl>self</ocl> be broken down into two or more states?",
                  "Could you write a characteristic equation for <ocl>self</ocl>?",
                  "Does <ocl>self</ocl> belong in this state machine or another?",
                  "Should <ocl>self</ocl> be be an initial state?",
                  "Is some state in another machine exclusive with <ocl>self</ocl>?"
                },
                new String[] { "Actions",
                  "What action should be preformed on entry into <ocl>self</ocl>?",
                  "Should some attribute be updated on entry into <ocl>self</ocl>?",
                  "What action should be preformed on exit from <ocl>self</ocl>?",
                  "Should some attribute be updated on exit from <ocl>self</ocl>?",
                  "What action should be preformed while in <ocl>self</ocl>?",
                  "Do state-actions maintain <ocl>self</ocl> as the current state?"
                },
                new String[] { "Transitions",
                  "Should there be another transition into <ocl>self</ocl>?",
                  "Can all the transitions into <ocl>self</ocl> be used?",
                  "Could some incoming transitions be combined?",
                  "Should there be another transition out of <ocl>self</ocl>?",
                  "Can all the transitions out of <ocl>self</ocl> be used?",
                  "Is each outgoing transition exclusive?",
                  "Could some outgoing transitions be combined?"
                }
            }
        },
        { "ChTransition",
            new String[][] {
                new String[] { "Structure",
                  "Should this transition start at a different source?",
                  "Should this transition end at a different destination?",
                  "Should there be another transition \"like\" this one?",
                  "Is another transition unneeded because of this one?"
                },
                new String[] { "Trigger",
                  "Does this transition need a trigger?",
                  "Does the trigger happen too often?",
                  "Does the trigger happen too rarely?"
                },
                new String[] { "MGuard",
                  "Could this transition be taken too often?",
                  "Is this transition's condition too restrictive?",
                  "Could it be broken down into two or more transitions?"
                },
                new String[] { "Actions",
                  "Should this transition have an action?",
                  "Should this transition's action be an exit action?",
                  "Should this transition's action be an entry action?",
                  "Is the precondition of the action always met?",
                  "Is the action's postcondition consistent with the destination?"
                }
            }
        },
        { "ChUseCase",
            new String[][] {
                new String[] { "Naming",
		  "Beschreibt der Name '<ocl>self</ocl>' den Anwendungsfall auf klare Weise?",       
		  "Ist '<ocl>self</ocl>' ein Hauptwort oder ein Hauptsatz?",
		  "K\u00f6nnte der Name '<ocl>self</ocl>' mi\u00dfinterpretiert werden oder etwas anderes bedeuten?"
                },
                new String[] { "Encoding",
		  "Sollte <ocl>self</ocl> ein eigener Anwendungsfall sein, oder ein einfaches Attribut eines anderen?",
		  "Macht <ocl>self</ocl> genau eine Sache, und diese ordentlich?",       
		  "K\u00f6nnte <ocl>self</ocl> in zwei oder mehrere Anwendungsf\u00e4lle unterteilt werden?"
                },
                new String[] { "Value",
		  "Haben alle Attribute von <ocl>self</ocl> sinnvolle Startwerte?",
			       "K\u00f6nnten Sie eine Invariante f\u00fcr diesen Anwendungsfall schreiben?" /* , */
			       /* "Etablieren alle Konstruktoren die Invariante dieser Klasse?", 
				  "Stellen alle Operationen sicher, dass die Invariante der Klasse erhalten bleibt?" */
                },
                new String[] { "Location",
			       /*"K\u00f6nnte <ocl>self</ocl> auch an einer anderen Stelle in der Klassenhierachie definiert werden?",       
				 "Ist geplant, da\u00df <ocl>self</ocl> Unterklassen haben soll?", */
		  "K\u00f6nnte <ocl>self</ocl> aus dem Modell entfernt werden?" /*, 
									     "Gibt es eine andere Klasse in dem Modell, welche \u00fcberarbeitet oder entfernt werden sollte, weil sie den gleichen Zweck wie <ocl>self</ocl> erf\u00fcllt?" */
                },
                new String[] { "Updates",
		  "Aus welchen Gr\u00fcnden wird eine Instanz von <ocl>self</ocl> aktualisiert?",       
		  "Gibt es ein anderes Objekt, welches aktualisiert werden mu\u00df wenn <ocl>self</ocl> aktualisiert wird?"
                }
            }
        },
        { "ChActor",
            new String[][] {
                new String[] { "Naming",
		  "Beschreibt der Name '<ocl>self</ocl>' den Aktor auf klare Weise?",
		  "Ist '<ocl>self</ocl>' ein Hauptwort oder ein Hauptsatz?",
		  "K\u00f6nnte der Name '<ocl>self</ocl>' mi\u00dfinterpretiert werden oder etwas anderes bedeuten?"
                },
                new String[] { "Encoding", /*
					     "Sollte <ocl>self</ocl> eine eigene Klasse sein, oder ein einfaches Attribut einer anderen Klasse?", */
		  "Macht <ocl>self</ocl> genau eine Sache, und diese ordentlich?",
		  "K\u00f6nnte <ocl>self</ocl> in zwei oder mehrere Aktoren unterteilt werden?"
                },
                new String[] { "Value" /* ,
		  "Haben alle Attribute von <ocl>self</ocl> sinnvolle Startwerte?",
        	  "K\u00f6nnten Sie eine Invariante f\u00fcr diese Klasse schreiben?",
		  "Etablieren alle Konstruktoren die Invariante dieser Klasse?",
		  "Stellen alle Operationen sicher, dass die Invariante der Klasse erhalten bleibt?" */
                },
                new String[] { "Location",
			       /* "K\u00f6nnte <ocl>self</ocl> auch an einer anderen Stelle in der Klassenhierachie definiert werden?",
				  "Ist geplant, da\u00df <ocl>self</ocl> Unterklassen haben soll?", */
		  "K\u00f6nnte <ocl>self</ocl> aus dem Modell entfernt werden?", 
		  "Gibt es einen anderen Aktoren in dem Modell, welcher \u00fcberarbeitet oder entfernt werden sollte, weil er den gleichen Zweck wie <ocl>self</ocl> erf\u00fcllt?"
                },
                new String[] { "Updates",
		  "Aus welchen Gr\u00fcnden wird eine Instanz von <ocl>self</ocl> aktualisiert?",
		  "Gibt es ein anderes Objekt, welches aktualisiert werden mu\u00df wenn <ocl>self</ocl> aktualisiert wird?"
                }
            }
        }
    };

    public Object[][] getContents() {
        return _contents;
    }
}
