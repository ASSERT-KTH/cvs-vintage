/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

/**
 * A method binding represents a method or constructor of a class or interface.
 * Method bindings usually correspond directly to method or
 * constructor declarations found in the source code.
 * However, in certain cases of references to a generic method,
 * the method binding may correspond to a copy of a generic method
 * declaration with substitutions for the method's type parameters
 * (for these, <code>getTypeArguments</code> returns a non-empty
 * list, and either <code>isParameterizedMethod</code> or
 * <code>isRawMethod</code> returns <code>true</code>).
 * And in certain cases of references to a method declared in a
 * generic type, the method binding may correspond to a copy of a
 * method declaration with substitutions for the type's type
 * parameters (for these, <code>getTypeArguments</code> returns
 * an empty list, and both <code>isParameterizedMethod</code> and
 * <code>isRawMethod</code> return <code>false</code>).
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see ITypeBinding#getDeclaredMethods()
 * @since 2.0
 */
public interface IMethodBinding extends IBinding {
	
	/**
	 * Returns whether this binding is for a constructor or a method.
	 * 
	 * @return <code>true</code> if this is the binding for a constructor,
	 *    and <code>false</code> if this is the binding for a method
	 */ 
	public boolean isConstructor();

	/**
	 * Returns whether this binding is known to be a compiler-generated 
	 * default constructor. 
	 * <p>
	 * This method returns <code>false</code> for:
	 * <ul>
	 * <li>methods</li>
	 * <li>constructors with more than one parameter</li>
	 * <li>0-argument constructors where the binding information was obtained
	 * from a Java source file containing an explicit 0-argument constructor
	 * declaration</li>
	 * <li>0-argument constructors where the binding information was obtained
	 * from a Java class file (it is not possible to determine from a
	 * class file whether a 0-argument constructor was present in the source
	 * code versus generated automatically by a Java compiler)</li>
	 * </ul>
	 * 
	 * @return <code>true</code> if this is known to be the binding for a 
	 * compiler-generated default constructor, and <code>false</code>
	 * otherwise
	 * @since 3.0
	 */ 
	public boolean isDefaultConstructor();
	
	/**
	 * Returns the name of the method declared in this binding. The method name
	 * is always a simple identifier. The name of a constructor is always the
	 * same as the declared name of its declaring class.
	 * 
	 * @return the name of this method, or the declared name of this
	 *   constructor's declaring class
	 */
	public String getName();
	
	/**
	 * Returns the type binding representing the class or interface
	 * that declares this method or constructor.
	 * 
	 * @return the binding of the class or interface that declares this method
	 *    or constructor
	 */
	public ITypeBinding getDeclaringClass();

	/**
	 * Returns a list of type bindings representing the formal parameter types,
	 * in declaration order, of this method or constructor. Returns an array of
	 * length 0 if this method or constructor does not takes any parameters.
	 * <p>
	 * Note: The result does not include synthetic parameters introduced by
	 * inner class emulation.
	 * </p>
	 * 
	 * @return a (possibly empty) list of type bindings for the formal
	 *   parameters of this method or constructor
	 */
	public ITypeBinding[] getParameterTypes();

	/**
	 * Returns the binding for the return type of this method. Returns the
	 * special primitive <code>void</code> return type for constructors.
	 * 
	 * @return the binding for the return type of this method, or the
	 *    <code>void</code> return type for constructors
	 */
	public ITypeBinding getReturnType();

	/**
	 * Returns a list of type bindings representing the types of the exceptions thrown
	 * by this method or constructor. Returns an array of length 0 if this method
	 * throws no exceptions. The resulting types are in no particular order.
	 * 
	 * @return a list of type bindings for exceptions
	 *   thrown by this method or constructor
	 */
	public ITypeBinding[] getExceptionTypes();
	
	/**
	 * Returns the type parameters of this method or constructor binding.
	 * <p>
	 * Note that type parameters only occur on the binding of the
	 * declaring generic method. Type bindings corresponding to a raw or
	 * parameterized reference to a generic method do not carry type
	 * parameters (they instead have non-empty type arguments
	 * and non-trivial erasure).
	 * </p>
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return the list of binding for the type variables for the type
	 * parameters of this method, or otherwise the empty list
	 * @see ITypeBinding#isTypeVariable()
	 * @since 3.0
	 */
	public ITypeBinding[] getTypeParameters();
	
	/**
	 * Returns whether this method binding represents a declaration of
	 * a generic method.
	 * <p>
	 * Note that type parameters only occur on the binding of the
	 * declaring generic method; e.g., <code>public &lt;T&gt; T identity(T t);</code>.
	 * Method bindings corresponding to a raw or parameterized reference to a generic
	 * method do not carry type parameters (they instead have non-empty type arguments
	 * and non-trivial erasure).
	 * This method is fully equivalent to <code>getTypeParameters().length &gt; 0)</code>.
	 * </p>
	 * <p>
	 * Note that {@link #isGenericMethod()},
	 * {@link #isParameterizedMethod()},
	 * and {@link #isRawMethod()} are mutually exclusive.
	 * </p>
	 * <p>
	 * Note: Support for new language features of the 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return <code>true</code> if this method binding represents a 
	 * declaration of a generic method, and <code>false</code> otherwise
	 * @see #getTypeParameters()
	 * @since 3.1
	 */
	public boolean isGenericMethod();
	
	/**
	 * Returns whether this method binding represents an instance of
	 * a generic method corresponding to a parameterized method reference.
	 * <p>
	 * Note that {@link #isGenericMethod()},
	 * {@link #isParameterizedMethod()},
	 * and {@link #isRawMethod()} are mutually exclusive.
	 * </p>
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return <code>true</code> if this method binding represents a 
	 * an instance of a generic method corresponding to a parameterized
	 * method reference, and <code>false</code> otherwise
	 * @see #getGenericMethod()
	 * @see #getTypeArguments()
	 * @since 3.1
	 */
	public boolean isParameterizedMethod();
	
	/**
	 * Returns the type arguments of this generic method instance, or the
	 * empty list for other method bindings.
	 * <p>
	 * Note that type arguments only occur on a method binding that represents
	 * an instance of a generic method corresponding to a raw or parameterized
	 * reference to a generic method. Do not confuse these with type parameters
	 * which only occur on the method binding corresponding directly to the
	 * declaration of a generic method.
	 * </p> 
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return the list of type bindings for the type arguments used to
	 * instantiate the corrresponding generic method, or otherwise the empty list
	 * @see #getGenericMethod()
	 * @see #isParameterizedMethod()
	 * @see #isRawMethod()
	 * @since 3.1
	 */
	public ITypeBinding[] getTypeArguments();
	
	/**
	 * @since 3.1
	 * @deprecated Use {@link #getMethodDeclaration()} instead.
	 */
	// TODO (jeem) - remove before 3.1M5 (bug 80800)
	public IMethodBinding getErasure();
	
	/**
	 * Returns the binding for the method declaration corresponding to this
	 * method binding. For parameterized methods ({@link #isParameterizedMethod()})
	 * and raw methods ({@link #isRawMethod()}), this method returns the binding
	 * for the corresponding generic method. For other method bindings, this
	 * returns the same binding.
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return the method binding
	 * @since 3.1
	 */
	public IMethodBinding getMethodDeclaration();

	/**
	 * @since 3.1
	 * @deprecated Use {@link #getMethodDeclaration()} instead.
	 */
	// TODO (jeem) - remove before 3.1M5 (bug 80800)
	public IMethodBinding getGenericMethod();
	
	/**
	 * Returns whether this method binding represents an instance of
	 * a generic method corresponding to a raw method reference.
	 * <p>
	 * Note that {@link #isGenericMethod()},
	 * {@link #isParameterizedMethod()},
	 * and {@link #isRawMethod()} are mutually exclusive.
	 * </p>
	 * <p>
	 * Note: Support for new language features proposed for the upcoming 1.5
	 * release of J2SE is tentative and subject to change.
	 * </p>
	 *
	 * @return <code>true</code> if this method binding represents a 
	 * an instance of a generic method corresponding to a raw
	 * method reference, and <code>false</code> otherwise
	 * @see #getGenericMethod()
	 * @see #getTypeArguments()
	 * @since 3.1
	 */
	public boolean isRawMethod();
	
	/**
	 * Returns whether this is a variable arity method.
	 * <p>
	 * Note: Variable arity ("varargs") methods were added in JLS3.
	 * </p>
	 * 
	 * @return <code>true</code> if this is a variable arity method,
	 *    and <code>false</code> otherwise
	 * @since 3.1
	 */ 
	public boolean isVarargs();
	
	/**
	 * Returns whether this method overrides the given method,
	 * as specified in section 6.4.2 of <em>The Java Language 
	 * Specification, Second Edition</em> (JLS2).
	 * 
	 * @param method the method that is possibly overriden
	 * @return <code>true</code> if this method overrides the given method,
	 * and <code>false</code> otherwise
	 * @since 3.1
	 */
	public boolean overrides(IMethodBinding method);
}
