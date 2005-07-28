// $Id: TestCopyHelper.java,v 1.6 2005/07/28 08:17:05 linus Exp $
// Copyright (c) 2003-2005 The Regents of the University of California. All
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

package org.argouml.model.uml;

import java.util.Iterator;

import org.argouml.model.Model;

import junit.framework.TestCase;

import ru.novosoft.uml.MBase;
import ru.novosoft.uml.foundation.core.MClass;
import ru.novosoft.uml.foundation.core.MClassifier;
import ru.novosoft.uml.foundation.core.MCommentImpl;
import ru.novosoft.uml.foundation.core.MDataType;
import ru.novosoft.uml.foundation.core.MElement;
import ru.novosoft.uml.foundation.core.MGeneralizableElement;
import ru.novosoft.uml.foundation.core.MInterface;
import ru.novosoft.uml.foundation.core.MModelElement;
import ru.novosoft.uml.foundation.core.MNamespace;
import ru.novosoft.uml.foundation.core.MNamespaceImpl;
import ru.novosoft.uml.foundation.data_types.MVisibilityKind;
import ru.novosoft.uml.foundation.extension_mechanisms.MStereotype;
import ru.novosoft.uml.foundation.extension_mechanisms.MTaggedValue;
import ru.novosoft.uml.model_management.MModel;
import ru.novosoft.uml.model_management.MPackage;

/**
 * Non-gui tests of the CopyHelper class.
 */
public class TestCopyHelper extends TestCase {
    /**
     * The model implementation.
     */
    private NSUMLModelImplementation nsmodel;

    /**
     * @see junit.framework.TestCase#TestCase(String)
     */
    public TestCopyHelper(String name) {
	super(name);
	nsmodel = new NSUMLModelImplementation();
    }

    /**
     * Testing the existance of public static members.
     */
    public void compileTestPublicStaticMembers() {
	nsmodel.getCopyHelper();
    }

    /**
     * Testing the existance of public members.
     */
    public void compileTestPublicMembers() {
	CopyHelper h = nsmodel.getCopyHelper();

	h.copy(new MCommentImpl(), new MNamespaceImpl());
    }

    /**
     * Testing the copying of a class.
     */
    public void testCopyClass() {
	MModel m1 = (MModel) Model.getModelManagementFactory().createModel();
	MModel m2 = (MModel) Model.getModelManagementFactory().createModel();
	CopyHelper helper = nsmodel.getCopyHelper();

	Object o;
	MClass c;
	MClass c2;
	MClass k;
	MStereotype st;

	k = (MClass) Model.getCoreFactory().createClass();
	m1.addOwnedElement(k);

	st =
	    (MStereotype) Model.getExtensionMechanismsFactory()
	        .buildStereotype("clsStT", k);
	st.setBaseClass("Class");
	m1.addOwnedElement(st);

	// See if we can copy a class right off
	o = helper.copy(k, m2);
	assertNotNull(o);
	assertTrue(o.getClass() == k.getClass());
	c = (MClass) o;
	assertTrue(c.getNamespace() == m2);
	checkClassCopy(k, c);

	// Change things
	k.setName("TestClass");
	k.setVisibility(MVisibilityKind.PUBLIC);
	k.setSpecification(true);
	k.setTaggedValue("TVKey", "TVValue");
	k.setActive(false);
	c = (MClass) helper.copy(k, m2);
	checkClassCopy(k, c);

	// Assert that the copy isn't modified when the source changes
	k.setName("TestClass2");
	k.setVisibility(MVisibilityKind.PROTECTED);
	k.setSpecification(false);
	k.setTaggedValue("TVKey", "TVNewValue");
	k.setActive(true);
	k.setStereotype(st);
	assertEquals("TestClass", c.getName());
	assertTrue(c.getVisibility() == MVisibilityKind.PUBLIC);
	assertTrue(c.isSpecification());
	assertTrue(!c.isActive());
	assertEquals("TVValue", c.getTaggedValue("TVKey"));

	// See if the other copy was just a lucky shot
	c = (MClass) helper.copy(k, m2);
	checkClassCopy(k, c);

	// See if two copies look like copies of eachother
	c2 = (MClass) helper.copy(k, m2);
	checkClassCopy(c, c2);
    }

    /**
     * Testing the copying of a data type.
     */
    public void testCopyDataType() {
	MModel m1 = (MModel) Model.getModelManagementFactory().createModel();
	MModel m2 = (MModel) Model.getModelManagementFactory().createModel();
	CopyHelper helper = nsmodel.getCopyHelper();

	Object o;
	MDataType c;
	MDataType c2;
	MDataType d;
	MStereotype st;

	d = (MDataType) Model.getCoreFactory().createDataType();
	m1.addOwnedElement(d);

	st =
	    (MStereotype) Model.getExtensionMechanismsFactory()
	        .buildStereotype("dttStT", d);
	st.setBaseClass("DataType");
	m1.addOwnedElement(st);

	// See if we can copy a datatype right off
	o = helper.copy(d, m2);
	assertNotNull(o);
	assertTrue(o.getClass() == d.getClass());
	c = (MDataType) o;
	assertTrue(c.getNamespace() == m2);
	checkDataTypeCopy(d, c);

	// Change things
	d.setName("TestDataType");
	d.setVisibility(MVisibilityKind.PUBLIC);
	d.setSpecification(true);
	d.setTaggedValue("TVKey", "TVValue");
	c = (MDataType) helper.copy(d, m2);
	checkDataTypeCopy(d, c);

	// Assert that the copy isn't modified when the source changes
	d.setName("TestDataType2");
	d.setVisibility(MVisibilityKind.PROTECTED);
	d.setSpecification(false);
	d.setTaggedValue("TVKey", "TVNewValue");
	d.setStereotype(st);
	assertEquals("TestDataType", c.getName());
	assertTrue(c.getVisibility() == MVisibilityKind.PUBLIC);
	assertTrue(c.isSpecification());
	assertEquals("TVValue", c.getTaggedValue("TVKey"));

	// See if the other copy was just a lucky shot
	c = (MDataType) helper.copy(d, m2);
	checkDataTypeCopy(d, c);

	// See if two copies look like copies of eachother
	c2 = (MDataType) helper.copy(d, m2);
	checkDataTypeCopy(c, c2);
    }

    /**
     * Testing the copying of an interface.
     */
    public void testCopyInterface() {
	MModel m1 = (MModel) Model.getModelManagementFactory().createModel();
	MModel m2 = (MModel) Model.getModelManagementFactory().createModel();
	CopyHelper helper = nsmodel.getCopyHelper();

	Object o;
	MInterface c;
	MInterface c2;
	MInterface i;
	MStereotype st;

	i = (MInterface) Model.getCoreFactory().createInterface();
	m1.addOwnedElement(i);

	st =
	    (MStereotype) Model.getExtensionMechanismsFactory()
	        .buildStereotype("intStT", i);
	st.setBaseClass("Interface");
	m1.addOwnedElement(st);

	// See if we can copy an interface right off
	o = helper.copy(i, m2);
	assertNotNull(o);
	assertTrue(o.getClass() == i.getClass());
	c = (MInterface) o;
	assertTrue(c.getNamespace() == m2);
	checkInterfaceCopy(i, c);

	// Change things
	i.setName("TestInterface");
	i.setVisibility(MVisibilityKind.PUBLIC);
	i.setSpecification(true);
	i.setTaggedValue("TVKey", "TVValue");
	c = (MInterface) helper.copy(i, m2);
	checkInterfaceCopy(i, c);

	// Assert that the copy isn't modified when the source changes
	i.setName("TestInterface2");
	i.setVisibility(MVisibilityKind.PROTECTED);
	i.setSpecification(false);
	i.setTaggedValue("TVKey", "TVNewValue");
	i.setStereotype(st);
	assertEquals("TestInterface", c.getName());
	assertTrue(c.getVisibility() == MVisibilityKind.PUBLIC);
	assertTrue(c.isSpecification());
	assertEquals("TVValue", c.getTaggedValue("TVKey"));

	// See if the other copy was just a lucky shot
	c = (MInterface) helper.copy(i, m2);
	checkInterfaceCopy(i, c);

	// See if two copies look like copies of eachother
	c2 = (MInterface) helper.copy(i, m2);
	checkInterfaceCopy(c, c2);
    }

    /**
     * Testing the copying of a package.
     */
    public void testCopyPackage() {
	MModel m1 = (MModel) Model.getModelManagementFactory().createModel();
	MModel m2 = (MModel) Model.getModelManagementFactory().createModel();
	CopyHelper helper = nsmodel.getCopyHelper();

	Object o;
	MPackage c;
	MPackage c2;
	MPackage p;
	MStereotype st;

	p = (MPackage) Model.getModelManagementFactory().createPackage();
	m1.addOwnedElement(p);

	st =
	    (MStereotype) Model.getExtensionMechanismsFactory()
	        .buildStereotype("pkgStT", p);
	st.setBaseClass("Package");
	m1.addOwnedElement(st);

	// See if we can copy a package right off
	o = helper.copy(p, m2);
	assertNotNull(o);
	assertTrue(o.getClass() == p.getClass());
	c = (MPackage) o;
	assertTrue(c.getNamespace() == m2);
	checkPackageCopy(p, c);

	// Change things
	p.setName("TestPackage");
	p.setVisibility(MVisibilityKind.PUBLIC);
	p.setSpecification(true);
	p.setTaggedValue("TVKey", "TVValue");
	c = (MPackage) helper.copy(p, m2);
	checkPackageCopy(p, c);

	// Assert that the copy isn't modified when the source changes
	p.setName("TestPackage2");
	p.setVisibility(MVisibilityKind.PROTECTED);
	p.setSpecification(false);
	p.setTaggedValue("TVKey", "TVNewValue");
	p.setStereotype(st);
	assertEquals("TestPackage", c.getName());
	assertTrue(c.getVisibility() == MVisibilityKind.PUBLIC);
	assertTrue(c.isSpecification());
	assertEquals("TVValue", c.getTaggedValue("TVKey"));

	// See if the other copy was just a lucky shot
	c = (MPackage) helper.copy(p, m2);
	checkPackageCopy(p, c);

	// See if two copies look like copies of eachother
	c2 = (MPackage) helper.copy(p, m2);
	checkPackageCopy(c, c2);
    }

    /**
     * Testing the copying of a stereotype.
     */
    public void testCopyStereotype() {
	MModel m1 = (MModel) Model.getModelManagementFactory().createModel();
	MModel m2 = (MModel) Model.getModelManagementFactory().createModel();
	CopyHelper helper = nsmodel.getCopyHelper();

	Object o;
	MStereotype c;
	MStereotype c2;
	MStereotype s;
	MStereotype st;

	s =
	    (MStereotype) Model.getExtensionMechanismsFactory()
	        .buildStereotype(null, m1);
	m1.addOwnedElement(s);

	st =
	    (MStereotype) Model.getExtensionMechanismsFactory()
	        .buildStereotype("sttStT", m1);
	st.setBaseClass("Stereotype");
	m1.addOwnedElement(st);

	// See if we can copy a stereotype right off
	o = helper.copy(s, m2);
	assertNotNull(o);
	assertTrue(o.getClass() == s.getClass());
	c = (MStereotype) o;
	assertTrue(c.getNamespace() == m2);
	checkStereotypeCopy(s, c);

	// Change things
	s.setName("TestStereotype");
	s.setVisibility(MVisibilityKind.PUBLIC);
	s.setSpecification(true);
	s.setTaggedValue("TVKey", "TVValue");
	s.setBaseClass("ModelElement");
	s.setIcon("Icon1");
	c = (MStereotype) helper.copy(s, m2);
	checkStereotypeCopy(s, c);

	// Assert that the copy isn't modified when the source changes
	s.setName("TestStereotype2");
	s.setVisibility(MVisibilityKind.PROTECTED);
	s.setSpecification(false);
	s.setTaggedValue("TVKey", "TVNewValue");
	s.setStereotype(st);
	s.setBaseClass("ClassifierRole");
	s.setIcon("Icon2");
	assertEquals("TestStereotype", c.getName());
	assertTrue(c.getVisibility() == MVisibilityKind.PUBLIC);
	assertTrue(c.isSpecification());
	assertEquals("ModelElement", c.getBaseClass());
	assertEquals("Icon1", c.getIcon());
	assertEquals("TVValue", c.getTaggedValue("TVKey"));

	// See if the other copy was just a lucky shot
	c = (MStereotype) helper.copy(s, m2);
	checkStereotypeCopy(s, c);

	// See if two copies look like copies of eachother
	c2 = (MStereotype) helper.copy(s, m2);
	checkStereotypeCopy(c, c2);
    }

    private void checkBaseCopy(MBase b1, MBase b2) {
    }

    private void checkClassCopy(MClass c1, MClass c2) {
	checkClassifierCopy(c1, c2);

	assertTrue(c1.isActive() == c2.isActive());
    }

    private void checkClassifierCopy(MClassifier c1, MClassifier c2) {
	checkNamespaceCopy(c1, c2);
	checkGeneralizableElementCopy(c1, c2);
    }

    private void checkDataTypeCopy(MDataType d1, MDataType d2) {
	checkClassifierCopy(d1, d2);
    }

    private void checkElementCopy(MElement e1, MElement e2) {
	checkBaseCopy(e1, e2);
    }

    private void checkGeneralizableElementCopy(
					       MGeneralizableElement e1,
					       MGeneralizableElement e2) {
	checkModelElementCopy(e1, e2);

	assertTrue(e1.isAbstract() == e2.isAbstract());
	assertTrue(e1.isLeaf() == e2.isLeaf());
	assertTrue(e1.isRoot() == e2.isRoot());
    }

    private void checkInterfaceCopy(MInterface i1, MInterface i2) {
	checkClassifierCopy(i1, i2);
    }

    private void checkModelElementCopy(MModelElement e1, MModelElement e2) {
	checkElementCopy(e1, e2);
	if (e1.getName() == null) {
	    assertNull(e2.getName());
	} else {
	    assertEquals(e1.getName(), e2.getName());
	}
	assertTrue(e1.getVisibility() == e2.getVisibility());
	assertTrue(e1.isSpecification() == e2.isSpecification());

	assertTrue(e1.getTaggedValues().size() == e2.getTaggedValues().size());
	Iterator it = e1.getTaggedValues().iterator();
	while (it.hasNext()) {
	    MTaggedValue tv = (MTaggedValue) it.next();
	    if (tv.getValue() == null) {
	        assertNull(e2.getTaggedValue(tv.getTag()));
	    } else {
	        assertEquals(tv.getValue(), e2.getTaggedValue(tv.getTag()));
	    }
	}

	if (e1.getStereotype() == null) {
	    assertNull(e2.getStereotype());
	} else {
	    assertNotNull(e2.getStereotype());
	    assertNotNull(e2.getStereotype().getName());
	    assertEquals(e1.getStereotype().getName(),
			 e2.getStereotype().getName());
	}
    }

    private void checkNamespaceCopy(MNamespace n1, MNamespace n2) {
	checkModelElementCopy(n1, n2);
    }

    private void checkPackageCopy(MPackage p1, MPackage p2) {
	checkNamespaceCopy(p1, p2);
    }

    private void checkStereotypeCopy(MStereotype s1, MStereotype s2) {
	checkGeneralizableElementCopy(s1, s2);
	if (s1.getBaseClass() == null) {
	    assertNull(s2.getBaseClass());
	} else {
	    assertEquals(s1.getBaseClass(), s2.getBaseClass());
	}

	if (s1.getIcon() == null) {
	    assertNull(s2.getIcon());
	} else {
	    assertEquals(s1.getIcon(), s2.getIcon());
	}

	// TODO: constraints
	// TODO: required tags
    }
}
