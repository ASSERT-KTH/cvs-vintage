// $Id: DataTypesFactoryImpl.java,v 1.4 2005/03/19 22:05:08 linus Exp $
// Copyright (c) 1996-2005 The Regents of the University of California. All
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


import java.util.List;

import org.argouml.model.DataTypesFactory;

import ru.novosoft.uml.foundation.data_types.MActionExpression;
import ru.novosoft.uml.foundation.data_types.MArgListsExpression;
import ru.novosoft.uml.foundation.data_types.MBooleanExpression;
import ru.novosoft.uml.foundation.data_types.MExpression;
import ru.novosoft.uml.foundation.data_types.MExpressionEditor;
import ru.novosoft.uml.foundation.data_types.MIterationExpression;
import ru.novosoft.uml.foundation.data_types.MMappingExpression;
import ru.novosoft.uml.foundation.data_types.MMultiplicity;
import ru.novosoft.uml.foundation.data_types.MObjectSetExpression;
import ru.novosoft.uml.foundation.data_types.MProcedureExpression;
import ru.novosoft.uml.foundation.data_types.MTimeExpression;
import ru.novosoft.uml.foundation.data_types.MTypeExpression;

/**
 * Factory to create UML classes for the UML
 * Foundation::DataTypes package.<p>
 *
 * TODO: Change visibility to package after reflection problem solved.
 *
 * @since ARGO0.11.2
 * @author Thierry Lach
 */
public class DataTypesFactoryImpl
	extends AbstractUmlModelFactory
	implements DataTypesFactory {

    /**
     * The model implementation.
     */
    private NSUMLModelImplementation nsmodel;

    /**
     * Don't allow instantiation.
     *
     * @param implementation To get other helpers and factories.
     */
    DataTypesFactoryImpl(NSUMLModelImplementation implementation) {
        nsmodel = implementation;
    }

    /**
     * Create an empty but initialized instance of a UML ActionExpression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML ActionExpression instance.
     */
    public Object createActionExpression(String language,
							      String body) {
        MActionExpression expression = new MActionExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an empty but initialized instance of a UML ArgListsExpression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML ArgListsExpression instance.
     */
    public Object createArgListsExpression(String language,
            				   String body) {
        MArgListsExpression expression =
	    new MArgListsExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an empty but initialized instance of a UML BooleanExpression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML BooleanExpression instance.
     */
    public Object createBooleanExpression(String language,
					  String body) {
        MBooleanExpression expression = new MBooleanExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an UML ExpressionEditor based on a given expression.
     *
     * @param expr Object MExpression the given expression
     * @return an initialized ExpressionEditor instance.
     * @deprecated as of 0.18.beta1 by Linus Tolke.
     *             This is NSUML-implementation creaping out.
     */
    public Object createExpressionEditor(Object expr) {
        MExpressionEditor editor = new MExpressionEditor();
        MExpression expression = (MExpression) expr;
	super.initialize(editor);
        editor.setBody(expression.getBody());
        editor.setLanguage(expression.getLanguage());
	return editor;
    }

    /**
     * Create an empty but initialized instance of a UML Expression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML Expression instance.
     */
    public Object createExpression(String language, String body) {
        MExpression expression = new MExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an empty but initialized instance of a UML IterationExpression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML IterationExpression instance.
     */
    public Object createIterationExpression(String language,
					    String body) {
        MIterationExpression expression =
	    new MIterationExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an empty but initialized instance of a UML MappingExpression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML MappingExpression instance.
     */
    public Object createMappingExpression(String language,
						      String body) {
        MMappingExpression expression = new MMappingExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an empty but initialized instance of a UML ObjectSetExpression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML ObjectSetExpression instance.
     */
    public Object createObjectSetExpression(String language,
							  String body) {
        MObjectSetExpression expression =
	    new MObjectSetExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an empty but initialized instance of a UML ProcedureExpression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML ProcedureExpression instance.
     */
    public Object createProcedureExpression(String language,
							  String body) {
        MProcedureExpression expression =
	    new MProcedureExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an empty but initialized instance of a UML TimeExpression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML TimeExpression instance.
     */
    public Object createTimeExpression(String language, String body) {
        MTimeExpression expression = new MTimeExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an empty but initialized instance of a UML TypeExpression.
     *
     * @param language the language for the expression
     * @param body the body for the expression
     * @return an initialized UML TypeExpression instance.
     */
    public Object createTypeExpression(String language, String body) {
        MTypeExpression expression = new MTypeExpression(language, body);
	super.initialize(expression);
	return expression;
    }

    /**
     * Create an empty but initialized instance of a UML Multiplicity.
     * Quote from the standard:
     * "In the metamodel a MultiplicityRange defines a range of integers.
     * The upper bound of the range cannot be below the lower bound.
     * The lower bound must be a nonnegative integer. The upper bound
     * must be a nonnegative integer or the special value unlimited,
     * which indicates there is no upper bound on the range."
     *
     * @param lower the lower bound of the range
     * @param upper the upper bound of the range
     *        TODO: UnlimitedInteger - which number represents "unlimited?".
     *        Quote from standard: "In the metamodel UnlimitedInteger defines
     *        a data type whose range is the nonnegative integers augmented
     *        by the special value 'unlimited'."
     * @return an initialized UML Multiplicity instance.
     */
    public Object createMultiplicity(int lower, int upper) {
        MMultiplicity multiplicity = new MMultiplicity(lower, upper);
	super.initialize(multiplicity);
	return multiplicity;
    }

    /**
     * Create an empty but initialized instance of a UML Multiplicity.
     *
     * @param range a List containing the range
     * @return an initialized UML Multiplicity instance.
     */
    public Object createMultiplicity(List range) {
        MMultiplicity multiplicity = new MMultiplicity(range);
	super.initialize(multiplicity);
	return multiplicity;
    }

    /**
     * Create an empty but initialized instance of a UML Multiplicity.
     *
     * @param str a String representing the multiplicity
     * @return an initialized UML Multiplicity instance.
     */
    public Object createMultiplicity(String str) {
        MMultiplicity multiplicity = new MMultiplicity(str);
	super.initialize(multiplicity);
	return multiplicity;
    }


}

