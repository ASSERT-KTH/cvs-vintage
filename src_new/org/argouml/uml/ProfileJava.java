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
import ru.novosoft.uml.foundation.core.*;
import ru.novosoft.uml.foundation.extension_mechanisms.*;
import ru.novosoft.uml.model_management.*;
import ru.novosoft.uml.behavior.state_machines.*;
import ru.novosoft.uml.behavior.activity_graphs.*;
import ru.novosoft.uml.behavior.collaborations.*;
import ru.novosoft.uml.foundation.data_types.*;

import java.util.*;

/**
 *   This class implements the abstract class Profile for use in modelling
 *   Java language projects.  Eventually, this class may be replaced by
 *   a configurable profile.
 *
 *   @author Curt Arnold
 */
public class ProfileJava extends Profile {
    java.util.List _modelElement;
    java.util.List _stereotypes;
    java.util.List _classifiers;
    ProfileClassifier _defaultClassifier;
    ProfileClassifier _voidClassifier;
    //java.util.List _tags;
    
    public ProfileJava() {
        _modelElement = new ArrayList(21);
        _stereotypes = new ArrayList(21);
        //_tags = new ArrayList(21);
        
        _modelElement.add(MPermission.class);
        _stereotypes.add(new String[] {"access","friend","import"});

        _modelElement.add(MAssociationEnd.class);
        _stereotypes.add(new String[] {"association","global","local","parameter","self"});

        _modelElement.add(MFlow.class);
        _stereotypes.add(new String[] {"become", "copy", "create"});

        _modelElement.add(MUsage.class);
        _stereotypes.add(new String[] { "call","instantiate","send" });

        _modelElement.add(MBehavioralFeature.class);
        _stereotypes.add(new String[] { "create", "destroy" });

        _modelElement.add(MCallEvent.class);
        _stereotypes.add(new String[] { "create", "destroy" });

        _modelElement.add(MAbstraction.class);
        _stereotypes.add(new String[] {"derive","realize","refine","trace"});

        _modelElement.add(MComponent.class);
        _stereotypes.add( new String[] {"document","executable","file","library","table"});

        _modelElement.add(MPackage.class);
        _stereotypes.add(new String[] {"facade","framework","metamodel","stub","systemModel","topLevel","use-case system","analysis system","analysis package","use-case package","analysis service package"});

        _modelElement.add(MGeneralization.class);
        _stereotypes.add(new String[] {"implementation"});

        _modelElement.add(MClass.class);
        _stereotypes.add(new String[] {"implementationClass","type","boundary","entity","control","worker","case worker","internal worker","entity"});

        _modelElement.add(MAssociation.class);
        _stereotypes.add(new String[] {"implicit","communicate","subscribe"});
        //_tags.add(new String[] {"persistence","persistent"});

        _modelElement.add(MConstraint.class);
        _stereotypes.add(new String[] { "invariant","postcondition","precondition" });

        _modelElement.add(MClassifier.class);
        _stereotypes.add(new String[] {"metaclass","powertype","process","thread","utility"});
        //_tags.add(new String[] {"persistence","semantics"});
        
        
        
        _modelElement.add(MAttribute.class);
        _stereotypes.add(null);
        //_tags.add(new String[] {"persistence"});
        
        _modelElement.add(MComment.class);
        _stereotypes.add(new String[] {"requirement","responsibility"});
        
        _modelElement.add(MOperation.class);
        _stereotypes.add(null);
        //_tags.add(new String[] {"semantics"});
        
        _modelElement.add(MObjectFlowState.class);
        _stereotypes.add(new String[] {"signalflow"});
        
        _modelElement.add(MModel.class);
        _stereotypes.add(new String[] {"use-case model","analysis model","design model","implementation model","object model"});
        
        _modelElement.add(MSubsystem.class);
        _stereotypes.add(new String[] {"design system","implementation system","implementation subsystem","design service subsystem","object system","organization unit","work unit"});
        
        _modelElement.add(MCollaboration.class);
        _stereotypes.add(new String[] {"use-case realization"});
        
        String[] javaLang = {"java","lang" };
        String[] javaUtil = {"java","util" };
        String[] javaMath = {"java","math" };
        _defaultClassifier = new ProfileClassifier(MClass.class,MClassImpl.class,"Object",javaLang);
        _classifiers = new ArrayList(27);
        _classifiers.add(_defaultClassifier);

        _classifiers.add(new ProfileClassifier(MDataType.class,MDataTypeImpl.class,"char",javaLang));
        _classifiers.add(new ProfileClassifier(MDataType.class,MDataTypeImpl.class,"byte",javaLang));
        _classifiers.add(new ProfileClassifier(MDataType.class,MDataTypeImpl.class,"short",javaLang));
        _classifiers.add(new ProfileClassifier(MDataType.class,MDataTypeImpl.class,"int",javaLang));
        _classifiers.add(new ProfileClassifier(MDataType.class,MDataTypeImpl.class,"long",javaLang));
        _classifiers.add(new ProfileClassifier(MDataType.class,MDataTypeImpl.class,"float",javaLang));
        _classifiers.add(new ProfileClassifier(MDataType.class,MDataTypeImpl.class,"double",javaLang));

        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Char",javaLang));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Byte",javaLang));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Short",javaLang));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Integer",javaLang));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Long",javaLang));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Float",javaLang));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Double",javaLang));
        
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"String",javaLang));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Object",javaLang));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Vector",javaUtil));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"BigDecimal",javaMath));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"BigInteger",javaMath));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Date",javaUtil));
        _classifiers.add(new ProfileClassifier(MClass.class,MClassImpl.class,"Time",javaUtil));
        
        _classifiers.add(new ProfileClassifier(MInterface.class,MInterfaceImpl.class,"Collection",javaUtil));
        _classifiers.add(new ProfileClassifier(MInterface.class,MInterfaceImpl.class,"Iterator",javaUtil));
        _classifiers.add(new ProfileClassifier(MInterface.class,MInterfaceImpl.class,"List",javaUtil));
        _classifiers.add(new ProfileClassifier(MInterface.class,MInterfaceImpl.class,"Set",javaUtil));
        _classifiers.add(new ProfileClassifier(MInterface.class,MInterfaceImpl.class,"SortedSet",javaUtil));
        
        _voidClassifier = new ProfileClassifier(MClassifier.class,null,"void",null);
    }
    
    public String formatElement(MModelElement element,MNamespace namespace) {
        String value = null;
        if(element == null) {
            value = "";
        }
        else {
            MNamespace elementNs = element.getNamespace();
            if(elementNs == namespace) {
                value = element.getName();
                if(value == null || value.length() == 0) {
                    value = defaultName(element,namespace);
                }
            }
            else {
                StringBuffer buffer = new StringBuffer();
                String pathSep = getPathSeparator();
                buildPath(buffer,element,pathSep);
                value = buffer.toString();
            }
        }
        return value;
    }
    
    protected String defaultAssocEndName(MAssociationEnd assocEnd,MNamespace namespace) {
        String name = null;
        MClassifier type = assocEnd.getType();
        if(type != null) {
            name = formatElement(type,namespace);
        }
        else {
            name = "unknown type";
        }
        MMultiplicity mult = assocEnd.getMultiplicity();
        if(mult != null) {
            StringBuffer buf = new StringBuffer(name);
            buf.append("[");
            buf.append(Integer.toString(mult.getLower()));
            buf.append("..");
            int upper = mult.getUpper();
            if(upper >= 0) {
                buf.append(Integer.toString(upper));
            }
            else {
                buf.append("*");
            }
            buf.append("]");
            name = buf.toString();
        }
        return name;
    }
    
    protected String defaultAssocName(MAssociation assoc,MNamespace ns) {
        StringBuffer buf = new StringBuffer();
        Iterator iter = assoc.getConnections().iterator();
        for(int i = 0;iter.hasNext();i++) {
            if(i != 0) {
                buf.append("-");
            }
            buf.append(defaultAssocEndName((MAssociationEnd) iter.next(),ns));
        }
        return buf.toString();
    }
            
        
    
    protected String defaultName(MModelElement element,MNamespace namespace) {
        String name = null;
        if(element instanceof MAssociationEnd) {
            name = defaultAssocEndName((MAssociationEnd) element,namespace);
        }
        else {
            if(element instanceof MAssociation) {
                name = defaultAssocName((MAssociation) element,namespace);
            }
            name = "anon";
        }
        return name;
    }
    
    protected String getPathSeparator() {
        return ".";
    }
    
    private void buildPath(StringBuffer buffer,MModelElement element,String pathSep) {
        if(element != null) {
            MNamespace parent = element.getNamespace();
            if(parent != null && parent != element) {
                buildPath(buffer,parent,pathSep);
                buffer.append(pathSep);
            }
            String name = element.getName();
            if(name == null || name.length() == 0) {
                name = defaultName(element,null);
            }
            buffer.append(name);
        }
    }
           
    protected String getElementSeparator() {
        return ", ";
    }
    
    protected String getEmptyCollection() {
        return "[empty]";
    }
    
    public String formatCollection(Iterator iter,MNamespace namespace) {
        String value = null;
        if(iter.hasNext()) {
            StringBuffer buffer = new StringBuffer();
            String elementSep = getElementSeparator();
            Object obj = null;
            for(int i = 0; iter.hasNext(); i++) {
                if(i > 0) {
                    buffer.append(elementSep);
                }
                obj = iter.next();
                if(obj instanceof MModelElement) {
                    buffer.append(formatElement((MModelElement) obj,namespace));
                }
                else {
                    buffer.append(obj.toString());
                }
            }
            value = buffer.toString();
        }
        else {
            value = getEmptyCollection();
        }
        return value;
    }
    
    public void addBuiltinClassifiers(MModel model,Class classifierType,Set classifierSet,boolean addVoid)
    {
        Iterator iter = _classifiers.iterator();
        while(iter.hasNext()) {
            ProfileClassifier profileClass = (ProfileClassifier) iter.next();
            if(classifierType.isAssignableFrom(profileClass.getClassInterface())) {
                if(model == null || !profileClass.exists(model)) {
                    classifierSet.add(profileClass);
                }
            }
        }
        if(addVoid) {
            classifierSet.add(_voidClassifier);
        }
    }
    
    public ProfileClassifier getDefaultClassifier() {
        return _defaultClassifier;
    }
    
    public ProfileClassifier getVoidClassifier() {
        return _voidClassifier;
    }
    
    public MClassifier constructBuiltinClassifier(MModel model,String name) {
        return null;
    }
    
    public Vector getInitialValues(MClassifier type) {
        Vector initialValues = null;
        if(type instanceof MDataType) {
            String name = type.getName();
            if(name.equals("boolean")) {
                initialValues = new Vector(2);
                initialValues.add("true");
                initialValues.add("false");
            }
            else {
                if(name.equals("byte")) {
                    initialValues = new Vector(2);
                    initialValues.add("0x00");
                    initialValues.add("0xFF");
                }
                else {
                    if(name.equals("short") || name.equals("long") || name.equals("int")) {
                        initialValues = new Vector(3);
                        initialValues.add("-1");
                        initialValues.add("0");
                        initialValues.add("1");
                    }
                    else {
                        if(name.equals("float") || name.equals("double")) {
                            initialValues = new Vector(7);
                            initialValues.add("-1");
                            initialValues.add("-0");
                            initialValues.add("0");
                            initialValues.add("1");
                            if(name.equals("float")) {
                                initialValues.add("Float.NaN");
                                initialValues.add("Float.POSITIVE_INFINITY");
                                initialValues.add("Float.NEGATIVE_INFINITY");
                            }
                            else {
                                initialValues.add("Double.NaN");
                                initialValues.add("Double.POSITIVE_INFINITY");
                                initialValues.add("Double.NEGATIVE_INFINITY");
                            }
                        }
                        else {
                            Collection supers = type.getGeneralizations();
                            if(supers != null) {
                                Iterator iter = supers.iterator();
                                while(iter.hasNext() && initialValues == null) {
                                    MClassifier superType = (MClassifier) iter.next();
                                    if(superType != type) {
                                        initialValues = getInitialValues(superType);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return initialValues;
    }

    // javadoc'd in Profile
    public boolean isAppropriateStereotype(Class targetClass,MStereotype stereotype)
    {
        boolean isAppropriate = false;
        String stereoName = stereotype.getName();
        if(stereoName != null && stereoName.length() > 0) {
            int voteFor = 0;
            int voteAgainst = 0;
            Iterator elementClassIter = _modelElement.iterator();
            Iterator stereoIter = _stereotypes.iterator();
            Class elementClass;
            String[] stereotypes;
            while(elementClassIter.hasNext()) {
                boolean match = false;
                stereotypes = (String[]) stereoIter.next();
                elementClass = (Class) elementClassIter.next();
                if(stereotypes != null) {
                    for(int i = 0; i < stereotypes.length; i++) {
                        if(stereoName.equals(stereotypes[i])) {
                            match = true;
                            break;
                        }
                    }
                }
                //
                //  if the stereotype matched, see if this class is a match
                if(match) {
                    if(elementClass.isAssignableFrom(targetClass)) {
                        voteFor++;
                    }
                    else {
                        voteAgainst++;
                    }
                }
            }            
            if(voteFor > 0 || voteAgainst == 0) {
                isAppropriate = true;
            }
        }
                    
        return isAppropriate;
    }
    
    //  javadoc'd in Profile
    public void addWellKnownStereotypes(Class targetClass,Set stereotypes)
    {
        Iterator modelElementIter = _modelElement.iterator();
        Iterator stereotypesIter = _stereotypes.iterator();
        Class modelElement;
        String[] stereotypeNames;
        while(modelElementIter.hasNext()) {
            modelElement = (Class) modelElementIter.next();
            stereotypeNames = (String[]) stereotypesIter.next();
            if(stereotypeNames != null && modelElement.isAssignableFrom(targetClass)) {
                for(int i = 0; i < stereotypeNames.length; i++) {
                    stereotypes.add(stereotypeNames[i]);
                }
            }
        }
    }
    
    public MStereotype constructWellKnownStereotype(MModel model,String name)
    {
        return null;
    }
    
    
}