/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Modifier node.
 * <pre>
 * Modifier:
 *    <b>public</b>
 *    <b>protected</b>
 *    <b>private</b>
 *    <b>static</b>
 *    <b>abstract</b>
 *    <b>final</b>
 *    <b>native</b>
 *    <b>synchronized</b>
 *    <b>transient</b>
 *    <b>volatile</b>
 *    <b>strictfp</b>
 * </pre>
 * <p>
 * The numeric values of these flags match the ones for class
 * files as described in the Java Virtual Machine Specification.
 * Note that Java model class {@link org.eclipse.jdt.core.Flags} also
 * provides the same constants as this class.
 * </p>
 * 
 * @since 2.0
 */
public final class Modifier extends ASTNode implements IExtendedModifier {

	/**
	 * Modifier constant (bit mask, value 0) indicating no modifiers.
	 * @since 2.0
	 */
	public static final int NONE = 0x0000;

	/**
	 * "public" modifier constant (bit mask).
	 * Applicable to types, methods, constructors, and fields.
	 * @since 2.0
	 */
	public static final int PUBLIC = 0x0001;

	/**
	 * "private" modifier constant (bit mask).
	 * Applicable to types, methods, constructors, and fields.
	 * @since 2.0
	 */
	public static final int PRIVATE = 0x0002;

	/**
	 * "protected" modifier constant (bit mask).
	 * Applicable to types, methods, constructors, and fields.
	 * @since 2.0
	 */
	public static final int PROTECTED = 0x0004;

	/**
	 * "static" modifier constant (bit mask).
	 * Applicable to types, methods, fields, and initializers.
	 * @since 2.0
	 */
	public static final int STATIC = 0x0008;

	/**
	 * "final" modifier constant (bit mask).
	 * Applicable to types, methods, fields, and variables.
	 * @since 2.0
	 */
	public static final int FINAL = 0x0010;

	/**
	 * "synchronized" modifier constant (bit mask).
	 * Applicable only to methods.
	 * @since 2.0
	 */
	public static final int SYNCHRONIZED = 0x0020;

	/**
	 * "volatile" modifier constant (bit mask).
	 * Applicable only to fields.
	 * @since 2.0
	 */
	public static final int VOLATILE = 0x0040;

	/**
	 * "transient" modifier constant (bit mask).
	 * Applicable only to fields.
	 * @since 2.0
	 */
	public static final int TRANSIENT = 0x0080;

	/**
	 * "native" modifier constant (bit mask).
	 * Applicable only to methods.
	 * @since 2.0
	 */
	public static final int NATIVE = 0x0100;

	/**
	 * "abstract" modifier constant (bit mask).
	 * Applicable to types and methods.
	 * @since 2.0
	 */
	public static final int ABSTRACT = 0x0400;

	/**
	 * "strictfp" modifier constant (bit mask).
	 * Applicable to types and methods.
	 * @since 2.0
	 */
	public static final int STRICTFP = 0x0800;

	/**
	 * Returns whether the given flags includes the "public" modifier.
	 * Applicable to types, methods, constructors, and fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>PUBLIC</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isPublic(int flags) {
		return (flags & PUBLIC) != 0;
	}

	/**
	 * Returns whether the given flags includes the "private" modifier.
	 * Applicable to types, methods, constructors, and fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>PRIVATE</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isPrivate(int flags) {
		return (flags & PRIVATE) != 0;
	}

	/**
	 * Returns whether the given flags includes the "protected" modifier.
	 * Applicable to types, methods, constructors, and fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>PROTECTED</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isProtected(int flags) {
		return (flags & PROTECTED) != 0;
	}

	/**
	 * Returns whether the given flags includes the "static" modifier.
	 * Applicable to types, methods, fields, and initializers.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>STATIC</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isStatic(int flags) {
		return (flags & STATIC) != 0;
	}

	/**
	 * Returns whether the given flags includes the "final" modifier.
	 * Applicable to types, methods, fields, and variables.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>FINAL</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isFinal(int flags) {
		return (flags & FINAL) != 0;
	}

	/**
	 * Returns whether the given flags includes the "synchronized" modifier.
	 * Applicable only to methods.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>SYNCHRONIZED</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isSynchronized(int flags) {
		return (flags & SYNCHRONIZED) != 0;
	}

	/**
	 * Returns whether the given flags includes the "volatile" modifier.
	 * Applicable only to fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>VOLATILE</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isVolatile(int flags) {
		return (flags & VOLATILE) != 0;
	}

	/**
	 * Returns whether the given flags includes the "transient" modifier.
	 * Applicable only to fields.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>TRANSIENT</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isTransient(int flags) {
		return (flags & TRANSIENT) != 0;
	}

	/**
	 * Returns whether the given flags includes the "native" modifier.
	 * Applicable only to methods.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>NATIVE</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isNative(int flags) {
		return (flags & NATIVE) != 0;
	}

	/**
	 * Returns whether the given flags includes the "abstract" modifier.
	 * Applicable to types and methods.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>ABSTRACT</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isAbstract(int flags) {
		return (flags & ABSTRACT) != 0;
	}

	/**
	 * Returns whether the given flags includes the "strictfp" modifier.
	 * Applicable to types and methods.
	 * 
	 * @param flags the modifier flags
	 * @return <code>true</code> if the <code>STRICTFP</code> bit is
	 *   set, and <code>false</code> otherwise
	 * @since 2.0
	 */
	public static boolean isStrictfp(int flags) {
		return (flags & STRICTFP) != 0;
	}
	
	/**
 	 * Modifier keywords (typesafe enumeration).
 	 * @since 3.0
	 */
	public static class ModifierKeyword {
	
		/** "public" modifier with flag value {@link Modifier#PUBLIC}. */
		public static final ModifierKeyword PUBLIC_KEYWORD = new ModifierKeyword("public", PUBLIC);//$NON-NLS-1$
		
		/** "protected" modifier with flag value {@link Modifier#PROTECTED}. */
		public static final ModifierKeyword PROTECTED_KEYWORD = new ModifierKeyword("protected", PROTECTED);//$NON-NLS-1$
		
		/** "private" modifier with flag value {@link Modifier#PRIVATE}. */
		public static final ModifierKeyword PRIVATE_KEYWORD = new ModifierKeyword("private", PRIVATE);//$NON-NLS-1$
		
		/** "static" modifier with flag value {@link Modifier#STATIC}. */
		public static final ModifierKeyword STATIC_KEYWORD = new ModifierKeyword("static", STATIC);//$NON-NLS-1$
		
		/** "abstract" modifier with flag value {@link Modifier#ABSTRACT}. */
		public static final ModifierKeyword ABSTRACT_KEYWORD = new ModifierKeyword("abstract", ABSTRACT);//$NON-NLS-1$
		
		/** "final" modifier with flag value {@link Modifier#FINAL}. */
		public static final ModifierKeyword FINAL_KEYWORD = new ModifierKeyword("final", FINAL);//$NON-NLS-1$
		
		/** "native" modifier with flag value {@link Modifier#NATIVE}. */
		public static final ModifierKeyword NATIVE_KEYWORD = new ModifierKeyword("native", NATIVE);//$NON-NLS-1$
		
		/** "synchronized" modifier with flag value {@link Modifier#SYNCHRONIZED}. */
		public static final ModifierKeyword SYNCHRONIZED_KEYWORD = new ModifierKeyword("synchronized", SYNCHRONIZED);//$NON-NLS-1$
		
		/** "transient" modifier with flag value {@link Modifier#TRANSIENT}. */
		public static final ModifierKeyword TRANSIENT_KEYWORD = new ModifierKeyword("transient", TRANSIENT);//$NON-NLS-1$
		
		/** "volatile" modifier with flag value {@link Modifier#VOLATILE}. */
		public static final ModifierKeyword VOLATILE_KEYWORD = new ModifierKeyword("volatile", VOLATILE);//$NON-NLS-1$
		
		/** "strictfp" modifier with flag value {@link Modifier#STRICTFP}. */
		public static final ModifierKeyword STRICTFP_KEYWORD = new ModifierKeyword("strictfp", STRICTFP);//$NON-NLS-1$
		
		/**
		 * Map from token to operator (key type: <code>String</code>;
		 * value type: <code>Operator</code>).
		 */
		private static final Map KEYWORDS;
		static {
			KEYWORDS = new HashMap(20);
			ModifierKeyword[] ops = {
					PUBLIC_KEYWORD,
					PROTECTED_KEYWORD,
					PRIVATE_KEYWORD,
					STATIC_KEYWORD,
					ABSTRACT_KEYWORD,
					FINAL_KEYWORD,
					NATIVE_KEYWORD,
					SYNCHRONIZED_KEYWORD,
					TRANSIENT_KEYWORD,
					VOLATILE_KEYWORD,
					STRICTFP_KEYWORD
				};
			for (int i = 0; i < ops.length; i++) {
				KEYWORDS.put(ops[i].toString(), ops[i]);
			}
		}

		/**
		 * The keyword modifier string.
		 */
		private String keyword;
		
		/**
		 * The flag value for the modifier.
		 */
		private int flagValue;
		
		/**
		 * Creates a new modifier with the given keyword.
		 * <p>
		 * Note: this constructor is private. The only instances
		 * ever created are the ones for the standard modifiers.
		 * </p>
		 * 
		 * @param keyword the character sequence for the modifier
		 * @param flagValue flag value as described in the Java Virtual Machine Specification
		 */
		private ModifierKeyword(String keyword, int flagValue) {
			this.keyword = keyword;
			this.flagValue = flagValue;
		}
		
		/**
		 * Returns the keyword for the modifier.
		 * 
		 * @return the keyword for the modifier
		 * @see #toKeyword(String)
		 */
		public String toString() {
			return this.keyword;
		}
		
		/**
		 * Returns the modifier corresponding to the given string,
		 * or <code>null</code> if none.
		 * <p>
		 * <code>toKeyword</code> is the converse of <code>toString</code>:
		 * that is, <code>ModifierKind.toKeyword(k.toString()) == k</code> for 
		 * all modifier keywords <code>k</code>.
		 * </p>
		 * 
		 * @param keyword the lowercase string name for the modifier
		 * @return the modifier keyword, or <code>null</code> if none
		 * @see #toString()
		 */
		public static ModifierKeyword toKeyword(String keyword) {
			return (ModifierKeyword) KEYWORDS.get(keyword);
		}
		
		/**
		 * Returns the modifier flag value corresponding to this modifier keyword.
		 * These flag values are as described in the Java Virtual Machine Specification.
		 * 
		 * @return one of the <code>Modifier</code> constants
		 * @see #fromFlagValue(int)
		 */ 
		public int toFlagValue() {
			return this.flagValue;
		}

		/**
		 * Returns the modifier corresponding to the given single-bit flag value,
		 * or <code>null</code> if none or if more than one bit is set.
		 * <p>
		 * <code>fromFlagValue</code> is the converse of <code>toFlagValue</code>:
		 * that is, <code>ModifierKind.fromFlagValue(k.toFlagValue()) == k</code> for 
		 * all modifier keywords <code>k</code>.
		 * </p>
		 * 
		 * @param flagValue the single-bit flag value for the modifier
		 * @return the modifier keyword, or <code>null</code> if none
		 * @see #toFlagValue()
		 */
		public static ModifierKeyword fromFlagValue(int flagValue) {
			for (Iterator it = KEYWORDS.values().iterator(); it.hasNext(); ) {
				ModifierKeyword k = (ModifierKeyword) it.next();
				if (k.toFlagValue() == flagValue) {
					return k;
				}
			}
			return null;
		}
	}
	
	/**
	 * The "keyword" structural property of this node type.
	 * @since 3.0
	 */
	public static final SimplePropertyDescriptor KEYWORD_PROPERTY = 
		new SimplePropertyDescriptor(Modifier.class, "keyword", Modifier.ModifierKeyword.class, MANDATORY); //$NON-NLS-1$
	
	/**
	 * A list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor}),
	 * or null if uninitialized.
	 */
	private static final List PROPERTY_DESCRIPTORS;
	
	static {
		List properyList = new ArrayList(2);
		createPropertyList(Modifier.class, properyList);
		addProperty(KEYWORD_PROPERTY, properyList);
		PROPERTY_DESCRIPTORS = reapPropertyList(properyList);
	}

	/**
	 * Returns a list of structural property descriptors for this node type.
	 * Clients must not modify the result.
	 * 
	 * @param apiLevel the API level; one of the
	 * <code>AST.JLS&ast;</code> constants

	 * @return a list of property descriptors (element type: 
	 * {@link StructuralPropertyDescriptor})
	 * @since 3.0
	 */
	public static List propertyDescriptors(int apiLevel) {
		return PROPERTY_DESCRIPTORS;
	}
			
	/**
	 * The modifier keyword; defaults to an unspecified modifier.
	 * @since 3.0
	 */
	private ModifierKeyword modifierKeyword = ModifierKeyword.PUBLIC_KEYWORD;

	/**
	 * Creates a new unparented modifier node owned by the given AST.
	 * By default, the node has unspecified (but legal) modifier.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 * @since 3.0
	 */
	Modifier(AST ast) {
		super(ast);
	    unsupportedIn2();
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final List internalStructuralPropertiesForType(int apiLevel) {
		return propertyDescriptors(apiLevel);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	final Object internalGetSetObjectProperty(SimplePropertyDescriptor property, boolean get, Object value) {
		if (property == KEYWORD_PROPERTY) {
			if (get) {
				return getKeyword();
			} else {
				setKeyword((ModifierKeyword) value);
				return null;
			}
		}
		// allow default implementation to flag the error
		return super.internalGetSetObjectProperty(property, get, value);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	final int getNodeType0() {
		return MODIFIER;
	}

	/**
	 * @see IExtendedModifier#isModifier()
	 */ 
	public boolean isModifier() {
		return true;
	}
	
	/**
	 * @see IExtendedModifier#isAnnotation()
	 */ 
	public boolean isAnnotation() {
		return false;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	ASTNode clone0(AST target) {
		Modifier result = new Modifier(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setKeyword(getKeyword());
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	final boolean subtreeMatch0(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	void accept0(ASTVisitor visitor) {
		visitor.visit(this);
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the modifier keyword of this modifier node.
	 * 
	 * @return the modifier keyword
	 * @since 3.0
	 */ 
	public ModifierKeyword getKeyword() {
		return this.modifierKeyword;
	}

	/**
	 * Sets the modifier keyword of this modifier node.
	 * 
	 * @param modifierKeyord the modifier keyword 
	 * @exception IllegalArgumentException if the argument is <code>null</code>
	 * @since 3.0
	 */ 
	public void setKeyword(ModifierKeyword modifierKeyord) {
		if (modifierKeyord == null) {
			throw new IllegalArgumentException();
		}
		preValueChange(KEYWORD_PROPERTY);
		this.modifierKeyword = modifierKeyord;
		postValueChange(KEYWORD_PROPERTY);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	int memSize() {
		// treat ModifierKeyword as free
		return BASE_NODE_SIZE + 1 * 4;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 * @since 3.0
	 */
	int treeSize() {
		return memSize();
	}
}
