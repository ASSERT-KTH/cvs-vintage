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

import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * Java compilation unit AST node type. This is the type of the root of an AST.
 * <p>
 * The source range for this type of node is ordinarily the entire source file,
 * including leading and trailing whitespace and comments.
 * </p>
 *
 * <pre>
 * CompilationUnit:
 *    [ PackageDeclaration ]
 *    { ImportDeclaration }
 *    { TypeDeclaration | <b>;</b> }
 * </pre>
 * 
 * @since 2.0
 */
public class CompilationUnit extends ASTNode {

	/**
	 * The comment table, or <code>null</code> if none; initially
	 * <code>null</code>.
	 * @since 3.0
	 */
	private Comment[] optionalCommentTable = null;
	
	/**
	 * The package declaration, or <code>null</code> if none; initially
	 * <code>null</code>.
	 */
	private PackageDeclaration optionalPackageDeclaration = null;
	
	/**
	 * The list of import declarations in textual order order; 
	 * initially none (elementType: <code>ImportDeclaration</code>).
	 */
	private ASTNode.NodeList imports =
		new ASTNode.NodeList(false, ImportDeclaration.class);
	
	/**
	 * The list of type declarations in textual order order; 
	 * initially none (elementType: <code>TypeDeclaration</code>)
	 */
	private ASTNode.NodeList types =
		new ASTNode.NodeList(false, TypeDeclaration.class);
	
	/**
	 * Line end table. If <code>lineEndTable[i] == p</code> then the
	 * line number <code>i+1</code> ends at character position 
	 * <code>p</code>. Except for the last line, the positions are that
	 * of the last character of the line delimiter. 
	 * For example, the source string <code>A\nB\nC</code> has
	 * line end table {1, 3} (if \n is one character).
	 */
	private int[] lineEndTable = new int[0];

	/**
	 * Canonical empty list of messages.
	 */
	private static final Message[] EMPTY_MESSAGES = new Message[0];

	/**
	 * Canonical empty list of problems.
	 */
	private static final IProblem[] EMPTY_PROBLEMS = new IProblem[0];

	/**
	 * Messages reported by the compiler during parsing or name resolution.
	 */
	private Message[] messages;
	
	/**
	 * Problems reported by the compiler during parsing or name resolution.
	 */
	private IProblem[] problems = EMPTY_PROBLEMS;
	 
	/**
	 * Sets the line end table for this compilation unit.
	 * If <code>lineEndTable[i] == p</code> then line number <code>i+1</code> 
	 * ends at character position <code>p</code>. Except for the last line, the 
	 * positions are that of (the last character of) the line delimiter.
	 * For example, the source string <code>A\nB\nC</code> has
	 * line end table {1, 3, 4}.
	 * 
	 * @param lineEndtable the line end table
	 */
	void setLineEndTable(int[] lineEndTable) {
		if (lineEndTable == null) {
			throw new NullPointerException();
		}
		modifying();
		this.lineEndTable = lineEndTable;
	}

	/**
	 * Creates a new AST node for a compilation owned by the given AST.
	 * The compilation unit initially has no package declaration, no
	 * import declarations, and no type declarations.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	CompilationUnit(AST ast) {
		super(ast);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return COMPILATION_UNIT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		CompilationUnit result = new CompilationUnit(target);
		// n.b do not copy line number table or messages
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setPackage(
			(PackageDeclaration) ASTNode.copySubtree(target, getPackage()));
		result.imports().addAll(ASTNode.copySubtrees(target, imports()));
		result.types().addAll(ASTNode.copySubtrees(target, types()));
		return result;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		// dispatch to correct overloaded match method
		return matcher.match(this, other);
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void accept0(ASTVisitor visitor) {
		boolean visitChildren = visitor.visit(this);
		if (visitChildren) {
			// visit children in normal left to right reading order
			acceptChild(visitor, getPackage());
			acceptChildren(visitor, imports);
			acceptChildren(visitor, types);
		}
		visitor.endVisit(this);
	}
	
	/**
	 * Returns the node for the package declaration of this compilation 
	 * unit, or <code>null</code> if this compilation unit is in the 
	 * default package.
	 * 
	 * @return the package declaration node, or <code>null</code> if none
	 */ 
	public PackageDeclaration getPackage() {
		return optionalPackageDeclaration;
	}
	
	/**
	 * Sets or clears the package declaration of this compilation unit 
	 * node to the given package declaration node.
	 * 
	 * @param pkgDecl the new package declaration node, or 
	 *   <code>null</code> if this compilation unit does not have a package
	 *   declaration (that is in the default package)
	 * @exception IllegalArgumentException if:
	 * <ul>
	 * <li>the node belongs to a different AST</li>
	 * <li>the node already has a parent</li>
	 * </ul>
	 */ 
	public void setPackage(PackageDeclaration pkgDecl) {
		replaceChild(this.optionalPackageDeclaration, pkgDecl, false);
		this.optionalPackageDeclaration = pkgDecl;
	}

	/**
	 * Returns the live list of nodes for the import declarations of this 
	 * compilation unit, in order of appearance.
	 * 
	 * @return the live list of import declaration nodes
	 *    (elementType: <code>ImportDeclaration</code>)
	 */ 
	public List imports() {
		return imports;
	}
	
	/**
	 * Returns the live list of nodes for the top-level type declarations of this 
	 * compilation unit, in order of appearance.
	 * 
	 * @return the live list of top-level type declaration
	 *    nodes (elementType: <code>TypeDeclaration</code>)
	 */ 
	public List types() {
		return types;
	}

	/**
	 * Finds the corresponding AST node in the given compilation unit from 
	 * which the given binding originated. Returns <code>null</code> if the
	 * binding does not correspond to any node in this compilation unit.
	 * This method always returns <code>null</code> if bindings were not requested
	 * when this AST was built.
	 * <p>
	 * The following table indicates the expected node type for the various
	 * different kinds of bindings:
	 * <ul>
	 * <li></li>
	 * <li>package - a <code>PackageDeclaration</code></li>
	 * <li>class or interface - a <code>TypeDeclaration</code> or a
	 *    <code>AnonymousClassDeclaration</code> (for anonymous classes) </li>
	 * <li>primitive type - none</li>
	 * <li>array type - none</li>
	 * <li>field - a <code>VariableDeclarationFragment</code> in a 
	 *    <code>FieldDeclaration</code> </li>
	 * <li>local variable - a <code>SingleVariableDeclaration</code>, or
	 *    a <code>VariableDeclarationFragment</code> in a 
	 *    <code>VariableDeclarationStatement</code> or 
	 *    <code>VariableDeclarationExpression</code></li>
	 * <li>method - a <code>MethodDeclaration</code> </li>
	 * <li>constructor - a <code>MethodDeclaration</code> </li>
	 * </ul>
	 * </p>
	 * <p>
	 * Each call to <code>AST.parseCompilationUnit</code> with a request for bindings
	 * gives rise to separate universe of binding objects. This method always returns
	 * <code>null</code> when the binding object comes from a different AST.
	 * Use <code>findDeclaringNode(binding.getKey())</code> when the binding comes
	 * from a different AST.
	 * </p>
	 * 
	 * @param binding the binding
	 * @return the corresponding node where the given binding is declared,
	 * or <code>null</code> if the binding does not correspond to a node in this
	 * compilation unit or if bindings were not requested when this AST was built
	 * @see #findDeclaringNode(String)
	 */
	public ASTNode findDeclaringNode(IBinding binding) {
		return getAST().getBindingResolver().findDeclaringNode(binding);
	}

	/**
	 * Finds the corresponding AST node in the given compilation unit from 
	 * which the binding with the given key originated. Returns
	 * <code>null</code> if the corresponding node cannot be determined.
	 * This method always returns <code>null</code> if bindings were not requested
	 * when this AST was built.
	 * <p>
	 * The following table indicates the expected node type for the various
	 * different kinds of binding keys:
	 * <ul>
	 * <li></li>
	 * <li>package - a <code>PackageDeclaration</code></li>
	 * <li>class or interface - a <code>TypeDeclaration</code> or a
	 *    <code>AnonymousClassDeclaration</code> (for anonymous classes) </li>
	 * <li>primitive type - none</li>
	 * <li>array type - none</li>
	 * <li>field - a <code>VariableDeclarationFragment</code> in a 
	 *    <code>FieldDeclaration</code> </li>
	 * <li>local variable - a <code>SingleVariableDeclaration</code>, or
	 *    a <code>VariableDeclarationFragment</code> in a 
	 *    <code>VariableDeclarationStatement</code> or 
	 *    <code>VariableDeclarationExpression</code></li>
	 * <li>method - a <code>MethodDeclaration</code> </li>
	 * <li>constructor - a <code>MethodDeclaration</code> </li>
	 * </ul>
	 * </p>
	 * <p>
	 * Note that as explained in {@link IBinding#getKey() IBinding.getkey}
	 * there may be no keys for finding the declaring node for local variables,
	 * local or anonymous classes, etc.
	 * </p>
	 * 
	 * @param key the binding key, or <code>null</code>
	 * @return the corresponding node where a binding with the given
	 * key is declared, or <code>null</code> if the key is <code>null</code>
	 * or if the key does not correspond to a node in this compilation unit
	 * or if bindings were not requested when this AST was built
	 * @see IBinding#getKey()
	 * @since 2.1
	 */
	public ASTNode findDeclaringNode(String key) {
		return getAST().getBindingResolver().findDeclaringNode(key);
	}
		
	/**
	 * Returns the line number corresponding to the given source character
	 * position in the original source string. The initial line of the 
	 * compilation unit is numbered 1, and each line extends through the
	 * last character of the end-of-line delimiter. The very last line extends
	 * through the end of the source string and has no line delimiter.
	 * For example, the source string <code>class A\n{\n}</code> has 3 lines
	 * corresponding to inclusive character ranges [0,7], [8,9], and [10,10].
	 * Returns 1 for a character position that does not correspond to any
	 * source line, or if no line number information is available for this
	 * compilation unit.
	 * 
	 * @param position a 0-based character position, possibly
	 *   negative or out of range
	 * @return the 1-based line number, or <code>1</code> if the character
	 *    position does not correspond to a source line in the original
	 *    source file or if line number information is not known for this
	 *    compilation unit
	 * @see AST#parseCompilationUnit(char[])
	 */
	public int lineNumber(int position) {
		int length = lineEndTable.length;
		if (length == 0) {
			// no line number info
			return 1;
		}
		int low = 0;
		if (position <= lineEndTable[low]) {
			// position illegal or before the first line delimiter
			return 1;
		}
		// assert position > lineEndTable[low+1]  && low == 0
		int hi = length - 1;
		if (position > lineEndTable[hi]) {
			// position beyond the last line separator
			if (position >= getStartPosition() + getLength()) {
				// this is beyond the end of the source length
				return 1;
			} else {
				return length + 1;
			}
		}
		// assert lineEndTable[low]  < position <= lineEndTable[hi]
		// && low == 0 && hi == length - 1 && low < hi
		
		// binary search line end table
		while (true) {
			// invariant lineEndTable[low] < position <= lineEndTable[hi]
			// && 0 <= low < hi <= length - 1
			// reducing measure hi - low
			if (low + 1 == hi) {
				// assert lineEndTable[low] < position <= lineEndTable[low+1]
				// position is on line low+1 (line number is low+2)
				return low + 2;
			}
			// assert hi - low >= 2, so average is truly in between
			int mid = (low + hi) / 2;
			// assert 0 <= low < mid < hi <= length - 1
			if (position <= lineEndTable[mid]) {
				// assert lineEndTable[low] < position <= lineEndTable[mid]
				// && 0 <= low < mid < hi <= length - 1
				hi = mid;
			} else {
				// position > lineEndTable[mid]
				// assert lineEndTable[mid] < position <= lineEndTable[hi]
				// && 0 <= low < mid < hi <= length - 1
				low = mid;
			}
			// in both cases, invariant reachieved with reduced measure
		}
	}

	/**
	 * Returns the list of messages reported by the compiler during the parsing 
	 * or the type checking of this compilation unit. This list might be a subset of 
	 * errors detected and reported by a Java compiler.
	 * <p>
	 * This list of messages is suitable for simple clients that do little
	 * more than log the messages or display them to the user. Clients that
	 * need further details should call <code>getProblems</code> to get
	 * compiler problem objects.
	 * </p>
	 *
	 * @return the list of messages, possibly empty
	 * @see #getProblems()
	 * @see AST#parseCompilationUnit(char[])
	 */
	public Message[] getMessages() {
		if (this.messages == null) {
			int problemLength = this.problems.length;
			if (problemLength == 0) {
				this.messages = EMPTY_MESSAGES;
			} else {
				this.messages = new Message[problemLength];
				for (int i = 0; i < problemLength; i++) {
					IProblem problem = this.problems[i];
					int start = problem.getSourceStart();
					int end = problem.getSourceEnd();
					messages[i] = new Message(problem.getMessage(), start, end - start + 1);
				}
			}
		}
		return this.messages;
	}

	/**
	 * Returns the list of detailed problem reports noted by the compiler
	 * during the parsing or the type checking of this compilation unit. This
	 * list might be a subset of errors detected and reported by a Java
	 * compiler.
	 * <p>
	 * Simple clients that do little more than log the messages or display
	 * them to the user should probably call <code>getMessages</code> instead.
	 * </p>
	 * 
	 * @return the list of detailed problem objects, possibly empty
	 * @see #getMessages()
	 * @see AST#parseCompilationUnit(char[])
	 * @since 2.1
	 */
	public IProblem[] getProblems() {
		return this.problems;
	}

	/**
	 * Sets the array of problems reported by the compiler during the parsing or
	 * name resolution of this compilation unit.
	 * 
	 * @param problems the list of problems
	 */
	void setProblems(IProblem[] problems) {
		if (problems == null) {
			throw new IllegalArgumentException();
		}
		this.problems = problems;
	}
		
	/**
	 * Returns a list of the comments encountered while parsing
	 * this compilation unit.
	 * <p>
	 * Since the Java language allows comments to appear most anywhere
	 * in the source text, it is problematic to locate comments in relation
	 * to the structure of an AST. The one exception is doc comments 
	 * which, by convention, immediately precede type, field, and
	 * method declarations; these comments are located in the AST
	 * by {@link  BodyDeclaration#getJavadoc() BodyDeclaration.getJavadoc}.
	 * Other comments do not show up in the AST. The table of comments
	 * is provided for clients that need to find the source ranges of
	 * all comments in the original source string. It includes entries
	 * for comments of all kinds (line, block, and doc), arranged in order
	 * of increasing source position. 
	 * </p>
	 * Note on comment parenting: The {@link ASTNode#getParent() getParent()}
	 * of a doc comment associated with a body declaration is the body
	 * declaration node; for these comment nodes
	 * {@link ASTNode#getRoot() getRoot()} will return the compilation unit
	 * (assuming an unmodified AST) reflecting the fact that these nodes
	 * are property located in the AST for the compilation unit.
	 * However, for other comment nodes, {@link ASTNode#getParent() getParent()}
	 * will return <code>null</code>, and {@link ASTNode#getRoot() getRoot()}
	 * will return the comment node itself, indicating that these comment nodes
	 * are not directly connected to the AST for the compilation unit. The 
	 * {@link Comment#getAlternateRoot() Comment.getAlternateRoot}
	 * method provides a way to navigate from a comment to its compilation
	 * unit.
	 * </p>
	 * <p>
	 * A note on visitors: The only comment nodes that will be visited when
	 * visiting a compilation unit are the doc comments parented by body
	 * declarations. To visit all comments in normal reading order, iterate
	 * over the comment table and call {@link ASTNode#accept(ASTVisitor) accept}
	 * on each element.
	 * </p>
	 * <p>
	 * Clients must not modify the array.
	 * </p>
	 * 
	 * @return a list of comments in increasing order of source
	 * start position, or <code>null</code> if comment information
	 * for this compilation unit is not available
	 * @see AST#parseCompilationUnit(char[])
	 * @since 3.0
	 */
	public Comment[] getCommentTable() {
		return this.optionalCommentTable;
	}
	
	/**
	 * Sets the list of the comments encountered while parsing
	 * this compilation unit.
	 * 
	 * @param commentTable a list of comments in increasing order
	 * of source start position, or <code>null</code> if comment
	 * information for this compilation unit is not available
	 * @throw IllegalArgumentException if the comment table is
	 * not in increasing order of source position
	 * @see #getCommentTable()
	 * @see AST#parseCompilationUnit
	 * @since 3.0
	 */
	void setCommentTable(Comment[] commentTable) {
		// double check table to ensure that all comments have
		// source positions and are in strictly increasing order
		if (commentTable != null) {
			int nextAvailablePosition = 0;
			for (int i = 0; i < commentTable.length; i++) {
				Comment comment = commentTable[i];
				if (comment == null) {
					throw new IllegalArgumentException();
				}
				int start = comment.getStartPosition();
				int length = comment.getLength();
				if (start < 0 || length < 0 || start < nextAvailablePosition) {
					throw new IllegalArgumentException();
				}
				nextAvailablePosition = comment.getStartPosition() + comment.getLength();
			}
		}
		this.optionalCommentTable = commentTable;
	}
	
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	void appendDebugString(StringBuffer buffer) {
		buffer.append("CompilationUnit"); //$NON-NLS-1$
		// include the type names
		buffer.append("["); //$NON-NLS-1$
		for (Iterator it = types().iterator(); it.hasNext(); ) {
			TypeDeclaration d = (TypeDeclaration) it.next();
			buffer.append(d.getName().getIdentifier());
			if (it.hasNext()) {
				buffer.append(","); //$NON-NLS-1$
			}
		}
		buffer.append("]"); //$NON-NLS-1$
	}
		
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 7 * 4;
		if (lineEndTable != null) {
			size += HEADERS + 4 * lineEndTable.length;
		}
		if (optionalCommentTable != null) {
			size += HEADERS + 4 * optionalCommentTable.length;
		}
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		int size = memSize();
		if (optionalPackageDeclaration != null) {
			size += getPackage().treeSize();
		}
		size += imports.listSize();
		size += types.listSize();
		// include disconnected comments
		if (optionalCommentTable != null) {
			for (int i = 0; i < optionalCommentTable.length; i++) {
				Comment comment = optionalCommentTable[i];
				if (comment != null && comment.getParent() == null) {
					size += comment.treeSize();
				}
			}
		}
		return size;
	}
}

