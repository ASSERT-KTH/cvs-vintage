/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.InternalCompletionProposal;

/**
 * Completion proposal.
 * <p>
 * In typical usage, the user working in a Java code editor issues
 * a code assist command. This command results in a call to
 * <code>ICodeAssist.codeComplete(position, completionRequestor)</code>
 * passing the current position in the source code. The code assist
 * engine analyzes the code in the buffer, determines what kind of
 * Java language construct is at that position, and proposes ways
 * to complete that construct. These proposals are instances of
 * the class <code>CompletionProposal</code>. These proposals,
 * perhaps after sorting and filtering, are presented to the user
 * to make a choice.
 * </p>
 * <p>
 * The proposal is as follows: insert
 * the {@linkplain #getCompletion() completion string} into the
 * source file buffer, replacing the characters between 
 * {@linkplain #getReplaceStart() the start}
 * and {@linkplain #getReplaceEnd() end}. The string
 * can be arbitrary; for example, it might include not only the 
 * name of a method but a set of parentheses. Moreover, the source
 * range may include source positions before or after the source
 * position where <code>ICodeAssist.codeComplete</code> was invoked.
 * The rest of the information associated with the proposal is
 * to provide context that may help a user to choose from among
 * competing proposals.
 * </p>
 * <p>
 * The completion engine creates instances of this class; it is not
 * intended to be used by other clients.
 * </p>
 * 
 * @see ICodeAssist#codeComplete(int, CompletionRequestor)
 * @since 3.0
 */
public final class CompletionProposal extends InternalCompletionProposal {
	private boolean updateCompletion = false;

	/**
	 * Completion is a declaration of an anonymous class.
	 * This kind of completion might occur in a context like
	 * <code>"new List^;"</code> and complete it to
	 * <code>"new List() {}"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getDeclarationSignature()} -
	 * the type signature of the type being implemented or subclassed
	 * </li>
	 * <li>{@link #getDeclarationKey()} -
	 * the type unique key of the type being implemented or subclassed
	 * </li>
	 * <li>{@link #getSignature()} -
	 * the method signature of the constructor that is referenced
	 * </li>
	 * <li>{@link #getKey()} -
	 * the method unique key of the constructor that is referenced
	 * if the declaring type is not an interface
	 * </li>
	 * <li>{@link #getFlags()} -
	 * the modifiers flags of the constructor that is referenced
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
	 */
	public static final int ANONYMOUS_CLASS_DECLARATION = 1;

	/**
	 * Completion is a reference to a field.
	 * This kind of completion might occur in a context like
	 * <code>"this.ref^ = 0;"</code> and complete it to
	 * <code>"this.refcount = 0;"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getDeclarationSignature()} -
	 * the type signature of the type that declares the field that is referenced
	 * </li>
	 * <li>{@link #getFlags()} -
	 * the modifiers flags (including ACC_ENUM) of the field that is referenced
	 * </li>
	 * <li>{@link #getName()} -
	 * the simple name of the field that is referenced
	 * </li>
	 * <li>{@link #getSignature()} -
	 * the type signature of the field's type (as opposed to the
	 * signature of the type in which the referenced field
	 * is declared)
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
	 */
	public static final int FIELD_REF = 2;

	/**
	 * Completion is a keyword.
	 * This kind of completion might occur in a context like
	 * <code>"public cl^ Foo {}"</code> and complete it to
	 * <code>"public class Foo {}"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getName()} -
	 * the keyword token
	 * </li>
	 * <li>{@link #getFlags()} -
	 * the corresponding modifier flags if the keyword is a modifier
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
	 */
	public static final int KEYWORD = 3;

	/**
	 * Completion is a reference to a label.
	 * This kind of completion might occur in a context like
	 * <code>"break lo^;"</code> and complete it to
	 * <code>"break loop;"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getName()} -
	 * the simple name of the label that is referenced
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
	 */
	public static final int LABEL_REF = 4;

	/**
	 * Completion is a reference to a local variable.
	 * This kind of completion might occur in a context like
	 * <code>"ke^ = 4;"</code> and complete it to
	 * <code>"keys = 4;"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getFlags()} -
	 * the modifiers flags of the local variable that is referenced
	 * </li>
	 * <li>{@link #getName()} -
	 * the simple name of the local variable that is referenced
	 * </li>
	 * <li>{@link #getSignature()} -
	 * the type signature of the local variable's type
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
	 */
	public static final int LOCAL_VARIABLE_REF = 5;

	/**
	 * Completion is a reference to a method.
	 * This kind of completion might occur in a context like
	 * <code>"System.out.pr^();"</code> and complete it to
	 * <code>""System.out.println();"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getDeclarationSignature()} -
	 * the type signature of the type that declares the method that is referenced
	 * </li>
	 * <li>{@link #getFlags()} -
	 * the modifiers flags of the method that is referenced
	 * </li>
	 * <li>{@link #getName()} -
	 * the simple name of the method that is referenced
	 * </li>
	 * <li>{@link #getSignature()} -
	 * the method signature of the method that is referenced
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
	 */
	public static final int METHOD_REF = 6;

	/**
	 * Completion is a declaration of a method.
	 * This kind of completion might occur in a context like
	 * <code>"new List() {si^};"</code> and complete it to
	 * <code>"new List() {public int size() {} };"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getDeclarationSignature()} -
	 * the type signature of the type that declares the
	 * method that is being overridden or implemented
	 * </li>
	 * <li>{@link #getDeclarationKey()} -
	 * the unique of the type that declares the
	 * method that is being overridden or implemented
	 * </li>
	 * <li>{@link #getName()} -
	 * the simple name of the method that is being overridden
	 * or implemented
	 * </li>
	 * <li>{@link #getSignature()} -
	 * the method signature of the method that is being
	 * overridden or implemented
	 * </li>
	 * <li>{@link #getKey()} -
	 * the method unique key of the method that is being
	 * overridden or implemented
	 * </li>
	 * <li>{@link #getFlags()} -
	 * the modifiers flags of the method that is being
	 * overridden or implemented
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
	 */
	public static final int METHOD_DECLARATION = 7;

	/**
	 * Completion is a reference to a package.
	 * This kind of completion might occur in a context like
	 * <code>"import java.u^.*;"</code> and complete it to
	 * <code>"import java.util.*;"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getDeclarationSignature()} -
	 * the dot-based package name of the package that is referenced
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
	 */
	public static final int PACKAGE_REF = 8;

	/**
	 * Completion is a reference to a type. Any kind of type
	 * is allowed, including primitive types, reference types,
	 * array types, parameterized types, and type variables.
	 * This kind of completion might occur in a context like
	 * <code>"public static Str^ key;"</code> and complete it to
	 * <code>"public static String key;"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getDeclarationSignature()} -
	 * the dot-based package name of the package that contains
	 * the type that is referenced
	 * </li>
	 * <li>{@link #getSignature()} -
	 * the type signature of the type that is referenced
	 * </li>
	 * <li>{@link #getFlags()} -
	 * the modifiers flags (including Flags.AccInterface, AccEnum,
	 * and AccAnnotation) of the type that is referenced
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
	 */
	public static final int TYPE_REF = 9;

	/**
	 * Completion is a declaration of a variable (locals, parameters,
	 * fields, etc.).
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getName()} -
	 * the simple name of the variable being declared
	 * </li>
	 * <li>{@link #getSignature()} -
	 * the type signature of the type of the variable
	 * being declared
	 * </li>
	 * <li>{@link #getFlags()} -
	 * the modifiers flags of the variable being declared
	 * </li>
	 * </ul>
	 * </p>
	 * @see #getKind()
	 */
	public static final int VARIABLE_DECLARATION = 10;

	/**
	 * Completion is a declaration of a new potential method.
	 * This kind of completion might occur in a context like
	 * <code>"new List() {si^};"</code> and complete it to
	 * <code>"new List() {public int si() {} };"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getDeclarationSignature()} -
	 * the type signature of the type that declares the
	 * method that is being created
	 * </li>
	 * <li>{@link #getName()} -
	 * the simple name of the method that is being created
	 * </li>
	 * <li>{@link #getSignature()} -
	 * the method signature of the method that is being
	 * created
	 * </li>
	 * <li>{@link #getFlags()} -
	 * the modifiers flags of the method that is being
	 * created
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
     * @since 3.1
	 */
	public static final int POTENTIAL_METHOD_DECLARATION = 11;
	
	/**
	 * Completion is a reference to a method name.
	 * This kind of completion might occur in a context like
	 * <code>"import p.X.fo^"</code> and complete it to
	 * <code>"import p.X.foo;"</code>.
	 * <p>
	 * The following additional context information is available
	 * for this kind of completion proposal at little extra cost:
	 * <ul>
	 * <li>{@link #getDeclarationSignature()} -
	 * the type signature of the type that declares the method that is referenced
	 * </li>
	 * <li>{@link #getFlags()} -
	 * the modifiers flags of the method that is referenced
	 * </li>
	 * <li>{@link #getName()} -
	 * the simple name of the method that is referenced
	 * </li>
	 * <li>{@link #getSignature()} -
	 * the method signature of the method that is referenced
	 * </li>
	 * </ul>
	 * </p>
	 * 
	 * @see #getKind()
     * @since 3.1
	 */
	public static final int METHOD_NAME_REFERENCE = 12;
	
	/**
	 * Kind of completion request.
	 */
	private int completionKind;
	
	/**
	 * Offset in original buffer where ICodeAssist.codeComplete() was
	 * requested.
	 */
	private int completionLocation;
	
	/**
	 * Start position (inclusive) of source range in original buffer 
	 * containing the relevant token
	 * defaults to empty subrange at [0,0).
	 */
	private int tokenStart = 0;
	
	/**
	 * End position (exclusive) of source range in original buffer 
	 * containing the relevant token;
	 * defaults to empty subrange at [0,0).
	 */
	private int tokenEnd = 0;
	
	/**
	 * Completion string; defaults to empty string.
	 */
	private char[] completion = CharOperation.NO_CHAR;
	
	/**
	 * Start position (inclusive) of source range in original buffer 
	 * to be replaced by completion string; 
	 * defaults to empty subrange at [0,0).
	 */
	private int replaceStart = 0;
	
	/**
	 * End position (exclusive) of source range in original buffer 
	 * to be replaced by completion string;
	 * defaults to empty subrange at [0,0).
	 */
	private int replaceEnd = 0;
	
	/**
	 * Relevance rating; positive; higher means better;
	 * defaults to minimum rating.
	 */
	private int relevance = 1;
	
	/**
	 * Signature of the relevant package or type declaration
	 * in the context, or <code>null</code> if none.
	 * Defaults to null.
	 */
	private char[] declarationSignature = null;
	
	/**
	 * Unique key of the relevant package or type declaration
	 * in the context, or <code>null</code> if none.
	 * Defaults to null.
	 */
	private char[] declarationKey = null;
	
	/**
	 * Simple name of the method, field,
	 * member, or variable relevant in the context, or
	 * <code>null</code> if none.
	 * Defaults to null.
	 */
	private char[] name = null;
	
	/**
	 * Signature of the method, field type, member type,
	 * relevant in the context, or <code>null</code> if none.
	 * Defaults to null.
	 */
	private char[] signature = null;
	
	/**
	 * Unique of the method, field type, member type,
	 * relevant in the context, or <code>null</code> if none.
	 * Defaults to null.
	 */
	private char[] key = null;
	
	/**
	 * Modifier flags relevant in the context, or
	 * <code>Flags.AccDefault</code> if none.
	 * Defaults to <code>Flags.AccDefault</code>.
	 */
	private int flags = Flags.AccDefault;
	
	/**
	 * Parameter names (for method completions), or
	 * <code>null</code> if none. Lazily computed.
	 * Defaults to <code>null</code>.
	 */
	private char[][] parameterNames = null;
	
	/**
	 * Indicates whether parameter names have been computed.
	 */
	private boolean parameterNamesComputed = false;
	
	/**
	 * Creates a basic completion proposal. All instance
	 * field have plausible default values unless otherwise noted.
	 * <p>
	 * Note that the constructors for this class are internal to the
	 * Java model implementation. Clients cannot directly create
	 * CompletionProposal objects.
	 * </p>
	 * 
	 * @param kind one of the kind constants declared on this class
	 * @param completionOffset original offset of code completion request
	 * @return a new completion proposal
	 */
	public static CompletionProposal create(int kind, int completionOffset) {
		return new CompletionProposal(kind, completionOffset);
	}
	
	/**
	 * Creates a basic completion proposal. All instance
	 * field have plausible default values unless otherwise noted.
	 * <p>
	 * Note that the constructors for this class are internal to the
	 * Java model implementation. Clients cannot directly create
	 * CompletionProposal objects.
	 * </p>
	 * 
	 * @param kind one of the kind constants declared on this class
	 * @param completionLocation original offset of code completion request
	 */
	CompletionProposal(int kind, int completionLocation) {
		if ((kind < CompletionProposal.ANONYMOUS_CLASS_DECLARATION)
				|| (kind > CompletionProposal.METHOD_NAME_REFERENCE)) {
			throw new IllegalArgumentException();
		}
		if (this.completion == null || completionLocation < 0) {
			throw new IllegalArgumentException();
		}
		this.completionKind = kind;
		this.completionLocation = completionLocation;
	}
	
	/**
	 * Returns the kind of completion being proposed.
	 * <p>
	 * The set of different kinds of completion proposals is
	 * expected to change over time. It is strongly recommended
	 * that clients do <b>not</b> assume that the kind is one of the
	 * ones they know about, and code defensively for the
	 * possibility of unexpected future growth.
	 * </p>
	 * 
	 * @return the kind; one of the kind constants
	 * declared on this class, or possibly a kind unknown
	 * to the caller
	 */
	public int getKind() {
		return this.completionKind;
	}
	
	/**
	 * Returns the character index in the source file buffer
	 * where source completion was requested (the 
	 * <code>offset</code>parameter to
	 * <code>ICodeAssist.codeComplete</code>.
	 * 
	 * @return character index in source file buffer
	 * @see ICodeAssist#codeComplete(int,CompletionRequestor)
	 */
	public int getCompletionLocation() {
		return this.completionLocation;
	}
	
	/**
	 * Returns the character index of the start of the
	 * subrange in the source file buffer containing the
	 * relevant token being completed. This
	 * token is either the identifier or Java language keyword
	 * under, or immediately preceding, the original request 
	 * offset. If the original request offset is not within
	 * or immediately after an identifier or keyword, then the
	 * position returned is original request offset and the
	 * token range is empty.
	 * 
	 * @return character index of token start position (inclusive)
	 */
	public int getTokenStart() {
		return this.tokenStart;
	}
	
	/**
	 * Returns the character index of the end (exclusive) of the subrange
	 * in the source file buffer containing the
	 * relevant token. When there is no relevant token, the
	 * range is empty
	 * (<code>getEndToken() == getStartToken()</code>).
	 * 
	 * @return character index of token end position (exclusive)
	 */
	public int getTokenEnd() {
		return this.tokenEnd;
	}
	
	/**
	 * Sets the character indices of the subrange in the
	 * source file buffer containing the relevant token being
	 * completed. This token is either the identifier or
	 * Java language keyword under, or immediately preceding,
	 * the original request offset. If the original request
	 * offset is not within or immediately after an identifier
	 * or keyword, then the source range begins at original
	 * request offset and is empty.
	 * <p>
	 * If not set, defaults to empty subrange at [0,0).
	 * </p>
	 * 
	 * @param startIndex character index of token start position (inclusive)
	 * @param endIndex character index of token end position (exclusive)
	 */
	public void setTokenRange(int startIndex, int endIndex) {
		if (startIndex < 0 || endIndex < startIndex) {
			throw new IllegalArgumentException();
		}
		this.tokenStart = startIndex;
		this.tokenEnd = endIndex;
	}
	
	/**
	 * Returns the proposed sequence of characters to insert into the
	 * source file buffer, replacing the characters at the specified
	 * source range. The string can be arbitrary; for example, it might
	 * include not only the name of a method but a set of parentheses.
	 * <p>
	 * The client must not modify the array returned.
	 * </p>
	 * 
	 * @return the completion string
	 */
	public char[] getCompletion() {
		if(this.completionKind == METHOD_DECLARATION) {
			this.findParameterNames(null);
			if(this.updateCompletion) {
				this.updateCompletion = false;
				
				if(this.parameterNames != null) {
					int length = this.parameterNames.length;
					StringBuffer completionBuffer = new StringBuffer(this.completion.length);
						
					int start = 0;
					int end = CharOperation.indexOf('%', this.completion);
	
					completionBuffer.append(CharOperation.subarray(this.completion, start, end));
					
					for(int i = 0 ; i < length ; i++){
						completionBuffer.append(this.parameterNames[i]);
						start = end + 1;
						end = CharOperation.indexOf('%', this.completion, start);
						if(end > -1){
							completionBuffer.append(CharOperation.subarray(this.completion, start, end));
						} else {
							completionBuffer.append(CharOperation.subarray(this.completion, start, this.completion.length));
						}
					}
					int nameLength = completionBuffer.length();
					this.completion = new char[nameLength];
					completionBuffer.getChars(0, nameLength, this.completion, 0);
				}
			}
		}
		return this.completion;
	}
	
	/**
	 * Sets the proposed sequence of characters to insert into the
	 * source file buffer, replacing the characters at the specified
	 * source range. The string can be arbitrary; for example, it might
	 * include not only the name of a method but a set of parentheses.
	 * <p>
	 * If not set, defaults to an empty character array.
	 * </p>
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param completion the completion string
	 */
	public void setCompletion(char[] completion) {
		this.completion = completion;
	}
	
	/**
	 * Returns the character index of the start of the
	 * subrange in the source file buffer to be replaced
	 * by the completion string. If the subrange is empty
	 * (<code>getReplaceEnd() == getReplaceStart()</code>),
	 * the completion string is to be inserted at this
	 * index.
	 * <p>
	 * Note that while the token subrange is precisely 
	 * specified, the replacement range is loosely
	 * constrained and may not bear any direct relation
	 * to the original request offset. For example,
	 * it would be possible for a type completion to 
	 * propose inserting an import declaration at the
	 * top of the compilation unit; or the completion
	 * might include trailing parentheses and
	 * punctuation for a method completion.
	 * </p>
	 * 
	 * @return replacement start position (inclusive)
	 */
	public int getReplaceStart() {
		return this.replaceStart;
	}
	
	/**
	 * Returns the character index of the end of the
	 * subrange in the source file buffer to be replaced
	 * by the completion string. If the subrange is empty
	 * (<code>getReplaceEnd() == getReplaceStart()</code>),
	 * the completion string is to be inserted at this
	 * index.
	 * 
	 * @return replacement end position (exclusive)
	 */
	public int getReplaceEnd() {
		return this.replaceEnd;
	}
	
	/**
	 * Sets the character indices of the subrange in the
	 * source file buffer to be replaced by the completion
	 * string. If the subrange is empty
	 * (<code>startIndex == endIndex</code>),
	 * the completion string is to be inserted at this
	 * index.
	 * <p>
	 * If not set, defaults to empty subrange at [0,0).
	 * </p>
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param startIndex character index of replacement start position (inclusive)
	 * @param endIndex character index of replacement end position (exclusive)
	 */
	public void setReplaceRange(int startIndex, int endIndex) {
		if (startIndex < 0 || endIndex < startIndex) {
			throw new IllegalArgumentException();
		}
		this.replaceStart = startIndex;
		this.replaceEnd = endIndex;
	}
	
	/**
	 * Returns the relative relevance rating of this proposal.
	 * 
	 * @return relevance rating of this proposal; ratings are positive; higher means better
	 */
	public int getRelevance() {
		return this.relevance;
	}
	
	/**
	 * Sets the relative relevance rating of this proposal.
	 * <p>
	 * If not set, defaults to the lowest possible rating (1).
	 * </p>
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param rating relevance rating of this proposal; ratings are positive; higher means better
	 */
	public void setRelevance(int rating) {
		if (rating <= 0) {
			throw new IllegalArgumentException();
		}
		this.relevance = rating;
	}
	
	/**
	 * Returns the type signature or package name of the relevant
	 * declaration in the context, or <code>null</code> if none.
	 * <p>
	 * This field is available for the following kinds of
	 * completion proposals:
	 * <ul>
	 * <li><code>ANONYMOUS_CLASS_DECLARATION</code> - type signature
	 * of the type that is being subclassed or implemented</li>
	 * 	<li><code>FIELD_REF</code> - type signature
	 * of the type that declares the field that is referenced</li>
	 * 	<li><code>METHOD_REF</code> - type signature
	 * of the type that declares the method that is referenced</li>
	 * 	<li><code>METHOD_DECLARATION</code> - type signature
	 * of the type that declares the method that is being
	 * implemented or overridden</li>
	 * 	<li><code>PACKAGE_REF</code> - dot-based package 
	 * name of the package that is referenced</li>
	 * 	<li><code>TYPE_REF</code> - dot-based package 
	 * name of the package containing the type that is referenced</li>
	 *  <li><code>POTENTIAL_METHOD_DECLARATION</code> - type signature
	 * of the type that declares the method that is being created</li>
	 * </ul>
	 * For kinds of completion proposals, this method returns
	 * <code>null</code>. Clients must not modify the array
	 * returned.
	 * </p>
	 * 
	 * @return a type signature or a package name (depending
	 * on the kind of completion), or <code>null</code> if none
	 * @see Signature
	 */
	public char[] getDeclarationSignature() {
		return this.declarationSignature;
	}
	
	/**
	 * Returns the key of the relevant
	 * declaration in the context, or <code>null</code> if none.
	 * <p>
	 * This field is available for the following kinds of
	 * completion proposals:
	 * <ul>
	 * <li><code>ANONYMOUS_CLASS_DECLARATION</code> - key
	 * of the type that is being subclassed or implemented</li>
	 * 	<li><code>METHOD_DECLARATION</code> - key
	 * of the type that declares the method that is being
	 * implemented or overridden</li>
	 * </ul>
	 * For kinds of completion proposals, this method returns
	 * <code>null</code>. Clients must not modify the array
	 * returned.
	 * </p>
	 * 
	 * @return a key, or <code>null</code> if none
	 * @see org.eclipse.jdt.core.dom.ASTParser#createASTs(ICompilationUnit[], String[], org.eclipse.jdt.core.dom.ASTRequestor, IProgressMonitor)
     * @since 3.1
	 */
	public char[] getDeclarationKey() {
		return this.declarationKey;
	}
	
	/**
	 * Sets the type or package signature of the relevant
	 * declaration in the context, or <code>null</code> if none.
	 * <p>
	 * If not set, defaults to none.
	 * </p>
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param signature the type or package signature, or
	 * <code>null</code> if none
	 */
	public void setDeclarationSignature(char[] signature) {
		this.declarationSignature = signature;
	}
	
	/**
	 * Sets the type or package key of the relevant
	 * declaration in the context, or <code>null</code> if none.
	 * <p>
	 * If not set, defaults to none.
	 * </p>
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param key the type or package key, or
	 * <code>null</code> if none
     * @since 3.1
	 */
	public void setDeclarationKey(char[] key) {
		this.declarationKey = key;
	}
	
	/**
	 * Returns the simple name of the method, field,
	 * member, or variable relevant in the context, or
	 * <code>null</code> if none.
	 * <p>
	 * This field is available for the following kinds of
	 * completion proposals:
	 * <ul>
	 * 	<li><code>FIELD_REF</code> - the name of the field</li>
	 * 	<li><code>KEYWORD</code> - the keyword</li>
	 * 	<li><code>LABEL_REF</code> - the name of the label</li>
	 * 	<li><code>LOCAL_VARIABLE_REF</code> - the name of the local variable</li>
	 * 	<li><code>METHOD_REF</code> - the name of the method</li>
	 * 	<li><code>METHOD_DECLARATION</code> - the name of the method</li>
	 * 	<li><code>VARIABLE_DECLARATION</code> - the name of the variable</li>
	 *  <li><code>POTENTIAL_METHOD_DECLARATION</code> - the name of the method</li>
	 * </ul>
	 * For kinds of completion proposals, this method returns
	 * <code>null</code>. Clients must not modify the array
	 * returned.
	 * </p>
	 * 
	 * @return the keyword, field, method, local variable, or member
	 * name, or <code>null</code> if none
	 */
	public char[] getName() {
		return this.name;
	}
	
	
	/**
	 * Sets the simple name of the method, field,
	 * member, or variable relevant in the context, or
	 * <code>null</code> if none.
	 * <p>
	 * If not set, defaults to none.
	 * </p>
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param name the keyword, field, method, local variable,
	 * or member name, or <code>null</code> if none
	 */
	public void setName(char[] name) {
		this.name = name;
	}
	
	/**
	 * Returns the signature of the method or type
	 * relevant in the context, or <code>null</code> if none.
	 * <p>
	 * This field is available for the following kinds of
	 * completion proposals:
	 * <ul>
	 * <li><code>ANONYMOUS_CLASS_DECLARATION</code> - method signature
	 * of the constructor that is being invoked</li>
	 * 	<li><code>FIELD_REF</code> - the type signature
	 * of the referenced field's type</li>
	 * 	<li><code>LOCAL_VARIABLE_REF</code> - the type signature
	 * of the referenced local variable's type</li>
	 * 	<li><code>METHOD_REF</code> - method signature
	 * of the method that is referenced</li>
	 * 	<li><code>METHOD_DECLARATION</code> - method signature
	 * of the method that is being implemented or overridden</li>
	 * 	<li><code>TYPE_REF</code> - type signature
	 * of the type that is referenced</li>
	 * 	<li><code>VARIABLE_DECLARATION</code> - the type signature
	 * of the type of the variable being declared</li>
	 *  <li><code>POTENTIAL_METHOD_DECLARATION</code> - method signature
	 * of the method that is being created</li>
	 * </ul>
	 * For kinds of completion proposals, this method returns
	 * <code>null</code>. Clients must not modify the array
	 * returned.
	 * </p>
	 * 
	 * @return the signature, or <code>null</code> if none
	 * @see Signature
	 */
	public char[] getSignature() {
		return this.signature;
	}
	
	/**
	 * Returns the key relevant in the context,
	 * or <code>null</code> if none.
	 * <p>
	 * This field is available for the following kinds of
	 * completion proposals:
	 * <ul>
	 * <li><code>ANONYMOUS_CLASS_DECLARATION</code> - method key
	 * of the constructor that is being invoked, or <code>null</code> if
	 * the declaring type is an interface</li>
	 * 	<li><code>METHOD_DECLARATION</code> - method key
	 * of the method that is being implemented or overridden</li>
	 * </ul>
	 * For kinds of completion proposals, this method returns
	 * <code>null</code>. Clients must not modify the array
	 * returned.
	 * </p>
	 * 
	 * @return the key, or <code>null</code> if none
	 * @see org.eclipse.jdt.core.dom.ASTParser#createASTs(ICompilationUnit[], String[], org.eclipse.jdt.core.dom.ASTRequestor, IProgressMonitor)
     * @since 3.1
	 */
	public char[] getKey() {
		return this.key;
	}
	
//	/**
//	 * Returns the package name of the relevant
//	 * declaration in the context, or <code>null</code> if none.
//	 * <p>
//	 * This field is available for the following kinds of
//	 * completion proposals:
//	 * <ul>
//	 * <li><code>ANONYMOUS_CLASS_DECLARATION</code> - the dot-based package name
//	 * of the type that is being subclassed or implemented</li>
//	 * 	<li><code>FIELD_REF</code> - the dot-based package name
//	 * of the type that declares the field that is referenced</li>
//	 * 	<li><code>METHOD_REF</code> - the dot-based package name
//	 * of the type that declares the method that is referenced</li>
//	 * 	<li><code>METHOD_DECLARATION</code> - the dot-based package name
//	 * of the type that declares the method that is being
//	 * implemented or overridden</li>
//	 * </ul>
//	 * For kinds of completion proposals, this method returns
//	 * <code>null</code>. Clients must not modify the array
//	 * returned.
//	 * </p>
//	 * 
//	 * @return the dot-based package name, or
//	 * <code>null</code> if none
//	 * @see #getDeclarationSignature()
//	 * @see #getSignature()
//	 * 
//	 * @since 3.1
//	 */
//	public char[] getDeclarationPackageName() {
//		return this.declarationPackageName;
//	}
//	
//	/**
//	 * Returns the type name of the relevant
//	 * declaration in the context without the package fragment,
//	 * or <code>null</code> if none. 
//	 * <p>
//	 * This field is available for the following kinds of
//	 * completion proposals:
//	 * <ul>
//	 * <li><code>ANONYMOUS_CLASS_DECLARATION</code> - the dot-based type name
//	 * of the type that is being subclassed or implemented</li>
//	 * 	<li><code>FIELD_REF</code> - the dot-based type name
//	 * of the type that declares the field that is referenced
//	 * or an anonymous type instanciation ("new X(){}") if it is an anonymous type</li>
//	 * 	<li><code>METHOD_REF</code> - the dot-based type name
//	 * of the type that declares the method that is referenced
//	 * or an anonymous type instanciation ("new X(){}") if it is an anonymous type</li>
//	 * 	<li><code>METHOD_DECLARATION</code> - the dot-based type name
//	 * of the type that declares the method that is being
//	 * implemented or overridden</li>
//	 * </ul>
//	 * For kinds of completion proposals, this method returns
//	 * <code>null</code>. Clients must not modify the array
//	 * returned.
//	 * </p>
//	 * 
//	 * @return the dot-based package name, or
//	 * <code>null</code> if none
//	 * @see #getDeclarationSignature()
//	 * @see #getSignature()
//	 * 
//	 * @since 3.1
//	 */
//	public char[] getDeclarationTypeName() {
//		return this.declarationTypeName;
//	}
//	
//	/**
//	 * Returns the package name of the method or type
//	 * relevant in the context, or <code>null</code> if none.
//	 * <p>
//	 * This field is available for the following kinds of
//	 * completion proposals:
//	 * <ul>
//	 * 	<li><code>FIELD_REF</code> - the dot-based package name
//	 * of the referenced field's type</li>
//	 * 	<li><code>LOCAL_VARIABLE_REF</code> - the dot-based package name
//	 * of the referenced local variable's type</li>
//	 * 	<li><code>METHOD_REF</code> -  the dot-based package name
//	 * of the return type of the method that is referenced</li>
//	 * 	<li><code>METHOD_DECLARATION</code> - the dot-based package name
//	 * of the return type of the method that is being implemented
//	 * or overridden</li>
//	 * 	<li><code>PACKAGE_REF</code> - the dot-based package name
//	 * of the package that is referenced</li>
//	 * 	<li><code>TYPE_REF</code> - the dot-based package name
//	 * of the type that is referenced</li>
//	 * 	<li><code>VARIABLE_DECLARATION</code> - the dot-based package name
//	 * of the type of the variable being declared</li>
//	 * </ul>
//	 * For kinds of completion proposals, this method returns
//	 * <code>null</code>. Clients must not modify the array
//	 * returned.
//	 * </p>
//	 * 
//	 * @return the package name, or <code>null</code> if none
//	 * 
//	 * @see #getDeclarationSignature()
//	 * @see #getSignature()
//	 * 
//	 * @since 3.1
//	 */
//	public char[] getPackageName() {
//		return this.packageName;
//	}
//	
//	/**
//	 * Returns the type name without the package fragment of the method or type
//	 * relevant in the context, or <code>null</code> if none.
//	 * <p>
//	 * This field is available for the following kinds of
//	 * completion proposals:
//	 * <ul>
//	 * 	<li><code>FIELD_REF</code> - the dot-based type name
//	 * of the referenced field's type</li>
//	 * 	<li><code>LOCAL_VARIABLE_REF</code> - the dot-based type name
//	 * of the referenced local variable's type</li>
//	 * 	<li><code>METHOD_REF</code> -  the dot-based type name
//	 * of the return type of the method that is referenced</li>
//	 * 	<li><code>METHOD_DECLARATION</code> - the dot-based type name
//	 * of the return type of the method that is being implemented
//	 * or overridden</li>
//	 * 	<li><code>TYPE_REF</code> - the dot-based type name
//	 * of the type that is referenced</li>
//	 * 	<li><code>VARIABLE_DECLARATION</code> - the dot-based package name
//	 * of the type of the variable being declared</li>
//	 * </ul>
//	 * For kinds of completion proposals, this method returns
//	 * <code>null</code>. Clients must not modify the array
//	 * returned.
//	 * </p>
//	 * 
//	 * @return the package name, or <code>null</code> if none
//	 * 
//	 * @see #getDeclarationSignature()
//	 * @see #getSignature()
//	 * 
//	 * @since 3.1
//	 */
//	public char[] getTypeName() {
//		return this.typeName;
//	}
//	
//	/**
//	 * Returns the parameter package names of the method 
//	 * relevant in the context, or <code>null</code> if none.
//	 * <p>
//	 * This field is available for the following kinds of
//	 * completion proposals:
//	 * <ul>
//	 * 	<li><code>ANONYMOUS_CLASS_DECLARATION</code> - parameter package names
//	 * of the constructor that is being invoked</li>
//	 * 	<li><code>METHOD_REF</code> - parameter package names
//	 * of the method that is referenced</li>
//	 * 	<li><code>METHOD_DECLARATION</code> - parameter package names
//	 * of the method that is being implemented or overridden</li>
//	 * </ul>
//	 * For kinds of completion proposals, this method returns
//	 * <code>null</code>. Clients must not modify the array
//	 * returned.
//	 * </p>
//	 * 
//	 * @return the package name, or <code>null</code> if none
//	 * 
//	 * @see #getDeclarationSignature()
//	 * @see #getSignature()
//	 * 
//	 * @since 3.1
//	 */
//	public char[][] getParameterPackageNames() {
//		return this.parameterPackageNames;
//	}
//	
//	/**
//	 * Returns the parameter type names without teh package fragment of
//	 * the method relevant in the context, or <code>null</code> if none.
//	 * <p>
//	 * This field is available for the following kinds of
//	 * completion proposals:
//	 * <ul>
//	 * 	<li><code>ANONYMOUS_CLASS_DECLARATION</code> - parameter type names
//	 * of the constructor that is being invoked</li>
//	 * 	<li><code>METHOD_REF</code> - parameter type names
//	 * of the method that is referenced</li>
//	 * 	<li><code>METHOD_DECLARATION</code> - parameter type names
//	 * of the method that is being implemented or overridden</li>
//	 * </ul>
//	 * For kinds of completion proposals, this method returns
//	 * <code>null</code>. Clients must not modify the array
//	 * returned.
//	 * </p>
//	 * 
//	 * @return the package name, or <code>null</code> if none
//	 * 
//	 * @see #getDeclarationSignature()
//	 * @see #getSignature()
//	 * 
//	 * @since 3.1
//	 */
//	public char[][] getParameterTypeNames() {
//		return this.parameterTypeNames;
//	}
	
	/**
	 * Sets the signature of the method, field type, member type,
	 * relevant in the context, or <code>null</code> if none.
	 * <p>
	 * If not set, defaults to none.
	 * </p>
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param signature the signature, or <code>null</code> if none
	 */
	public void setSignature(char[] signature) {
		this.signature = signature;
	}
	
	/**
	 * Sets the key of the method, field type, member type,
	 * relevant in the context, or <code>null</code> if none.
	 * <p>
	 * If not set, defaults to none.
	 * </p>
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param key the key, or <code>null</code> if none
     * @since 3.1
	 */
	public void setKey(char[] key) {
		this.key = key;
	}
	
	/**
	 * Returns the modifier flags relevant in the context, or
	 * <code>Flags.AccDefault</code> if none.
	 * <p>
	 * This field is available for the following kinds of
	 * completion proposals:
	 * <ul>
	 * <li><code>ANONYMOUS_CLASS_DECLARATION</code> - modifier flags
	 * of the constructor that is referenced</li>
	 * 	<li><code>FIELD_REF</code> - modifier flags
	 * of the field that is referenced; 
	 * <code>Flags.AccEnum</code> can be used to recognize
	 * references to enum constants
	 * </li>
	 * 	<li><code>KEYWORD</code> - modifier flag
	 * corrresponding to the modifier keyword</li>
	 * 	<li><code>LOCAL_VARIABLE_REF</code> - modifier flags
	 * of the local variable that is referenced</li>
	 * 	<li><code>METHOD_REF</code> - modifier flags
	 * of the method that is referenced;
	 * <code>Flags.AccAnnotation</code> can be used to recognize
	 * references to annotation type members
	 * </li>
	 * 	<li><code>METHOD_DECLARATION</code> - modifier flags
	 * for the method that is being implemented or overridden</li>
	 * 	<li><code>TYPE_REF</code> - modifier flags
	 * of the type that is referenced; <code>Flags.AccInterface</code>
	 * can be used to recognize references to interfaces, 
	 * <code>Flags.AccEnum</code> enum types,
	 * and <code>Flags.AccAnnotation</code> annotation types
	 * </li>
	 * 	<li><code>VARIABLE_DECLARATION</code> - modifier flags
	 * for the variable being declared</li>
	 * 	<li><code>POTENTIAL_METHOD_DECLARATION</code> - modifier flags
	 * for the method that is being created</li>
	 * </ul>
	 * For other kinds of completion proposals, this method returns
	 * <code>Flags.AccDefault</code>.
	 * </p>
	 * 
	 * @return the modifier flags, or
	 * <code>Flags.AccDefault</code> if none
	 * @see Flags
	 */
	public int getFlags() {
		return this.flags;
	}
	
	/**
	 * Sets the modifier flags relevant in the context.
	 * <p>
	 * If not set, defaults to none.
	 * </p>
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param flags the modifier flags, or
	 * <code>Flags.AccDefault</code> if none
	 */
	public void setFlags(int flags) {
		this.flags = flags;
	}
	
	/**
	 * Finds the method parameter names.
	 * This information is relevant to method reference (and
	 * method declaration proposals). Returns <code>null</code>
	 * if not available or not relevant.
	 * <p>
	 * The client must not modify the array returned.
	 * </p>
	 * <p>
	 * <b>Note that this is an expensive thing to compute, which may require
	 * parsing Java source files, etc. Use sparingly.
	 * </p>
	 * 
	 * @param monitor the progress monitor, or <code>null</code> if none
	 * @return the parameter names, or <code>null</code> if none
	 * or not available or not relevant
	 */
	public char[][] findParameterNames(IProgressMonitor monitor) {
		if (!this.parameterNamesComputed) {
			this.parameterNamesComputed = true;
			
			switch(this.completionKind) {
				case ANONYMOUS_CLASS_DECLARATION:
					this.parameterNames =  this.findMethodParameterNames(
							this.declarationPackageName,
							this.declarationTypeName,
							CharOperation.lastSegment(this.declarationTypeName, '.'),
							this.parameterPackageNames,
							this.parameterTypeNames);
					break;
				case METHOD_REF:
					this.parameterNames =  this.findMethodParameterNames(
							this.declarationPackageName,
							this.declarationTypeName,
							this.name,
							this.parameterPackageNames,
							this.parameterTypeNames);
					//this.parameterNames = this.findMethodParameterNames(
					//		this.declarationSignature,
					//		this.name,
					//		Signature.getParameterTypes(this.getSignature()));
					break;
				case METHOD_DECLARATION:
					this.parameterNames =  this.findMethodParameterNames(
							this.declarationPackageName,
							this.declarationTypeName,
							this.name,
							this.parameterPackageNames,
							this.parameterTypeNames);
					//char[][] parameterTypes = Signature.getParameterTypes(this.getSignature();
					///this.parameterNames = this.findMethodParameterNames(
					//		this.declarationSignature,
					//		this.name,
					//		parameterTypes);
					if(this.parameterNames != null) {
						this.updateCompletion = true;
					}
					break;
			}			
		}
		return this.parameterNames;
	}
	
	/**
	 * Sets the method parameter names.
	 * This information is relevant to method reference (and
	 * method declaration proposals).
	 * <p>
	 * The completion engine creates instances of this class and sets
	 * its properties; this method is not intended to be used by other clients.
	 * </p>
	 * 
	 * @param parameterNames the parameter names, or <code>null</code> if none
	 */
	public void setParameterNames(char[][] parameterNames) {
		this.parameterNames = parameterNames;
		this.parameterNamesComputed = true;
	}
}
