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

package org.eclipse.jdt.core.dom;

import java.util.List;

/**
 * AST node for a tag within a doc comment.
 * <pre>
 * TagElement:
 *     [ <b>@</b> Identifier ] { DocElement }
 * DocElement:
 *     TextElement
 *     Name
 *     MethodRef
 *     MemberRef
 *     <b>{</b> TagElement <b>}<b>
 * </pre>
 * 
 * @see Javadoc
 * @since 3.0
 */
public final class TagElement extends ASTNode implements IDocElement {

	/**
	 * The tag name, or null if none; defaults to null.
	 */
	private String optionalTagName = null;
	
	/**
	 * The list of doc elements (element type: <code>IDocElement</code>). 
	 * Defaults to an empty list.
	 */
	private ASTNode.NodeList fragments = 
		new ASTNode.NodeList(true, IDocElement.class);

	/**
	 * Creates a new AST node for a tag element owned by the given AST.
	 * The new node has no name and an empty list of fragments.
	 * <p>
	 * N.B. This constructor is package-private; all subclasses must be 
	 * declared in the same package; clients are unable to declare 
	 * additional subclasses.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	TagElement(AST ast) {
		super(ast);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	public int getNodeType() {
		return TAG_ELEMENT;
	}

	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	ASTNode clone(AST target) {
		TagElement result = new TagElement(target);
		result.setSourceRange(this.getStartPosition(), this.getLength());
		result.setTagName(getTagName());
		result.fragments().addAll(ASTNode.copySubtrees(target, fragments()));
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
			acceptChildren(visitor, fragments);
		}
		visitor.endVisit(this);
	}

	/**
	 * Returns this node's tag name, or <code>null</code> if none.
	 * For top level doc tags such as parameter tags, the tag name
     * includes the "@" character ("@param").
	 * For inline doc tags such as link tags, the tag name
     * includes the "@" character ("@link").
     * The tag name may also be <code>null</code>; this is used to
     * represent the material at the start of a doc comment preceding
     * the first explicit tag.
     *
	 * @return the tag name, or <code>null</code> if none
	 */ 
	public String getTagName() {
		return this.optionalTagName;
	}
	
	/**
	 * Sets the tag name of this node to the given value.
	 * For top level doc tags such as parameter tags, the tag name
	 * includes the "@" character ("@param").
	 * For inline doc tags such as link tags, the tag name
	 * includes the "@" character ("@link").
	 * The tag name may also be <code>null</code>; this is used to
	 * represent the material at the start of a doc comment preceding
	 * the first explicit tag.
	 *
	 * @param name the tag name, or <code>null</code> if none
	 */ 
	public void setTagName(String tagName) {
		modifying();
		this.optionalTagName = tagName;
	}
		
	/**
	 * Returns the live list of fragments in this tag element. 
	 * <p>
	 * The fragments cover everything following the tag name
	 * (or everything if there is no tag name), and generally omit
	 * embedded line breaks (and leading whitespace on new lines,
	 * including any leading "&ast;"). {@link TagElement TagElement}
	 * nodes are used to represent tag elements (e.g., "@link")
	 * nested within this tag element. 
	 * </p>
	 * <p>
	 * Here are some typical examples:
	 * <ul>
	 * <li>"@see Foo#bar()" - TagElement with tag name "@see";
	 * fragments() contains a single MethodRef node</li>
	 * <li>"@deprected Use {@link #foo foo} instead." - 
	 * TagElement with tag name "@deprecated";
	 * 3 fragments: TextElement ("Use "),
	 * TagElement (for "@link #foo foo"),
	 * TextElement (" instead.")</li>
	 * <li>"@param args the program arguments" -
	 * TagElement with tag name "@param";
	 * 2 fragments: SimpleName ("args"), TextElement
	 * (" the program arguments")</li>
	 * </ul>
	 * The use of Name, MethodRef, and MemberRef nodes within
	 * tag elements allows these fragments to be queried for
	 * binding information.
	 * </p>
	 * <p>
	 * Adding and removing nodes from this list affects this node
	 * dynamically. The nodes in this list may be of various
	 * types, including {@link TextElement TextElement}, 
	 * {@link TagElement TagElement}, {@link Name Name}, 
	 * {@link MemberRef MemberRef}, and {@link MethodRef MethodRef}.
	 * Clients should assume that the list of types may grow in
	 * the future, and write their code to deal with unexpected
	 * nodes types. However, attempts to add a non-proscribed type
	 * of node will trigger an exception.
	 * 
	 * @return the live list of doc elements in this tag element
	 */ 
	public List fragments() {
		return fragments;
	}

	/**
	 * Returns whether this tag element is nested within another
	 * tag element. Nested tag elements appears enclosed in 
	 * "{" and "}"; certain doc tags, including "@link" and
	 * "@linkplain" are only meaningful as nested tags.
	 * Top-level (i.e., non-nested) doc tags begin on a new line;
	 * certain doc tags, including "@param" and
	 * "@see" are only meaningful as top-level tags.
	 * <p>
	 * This convenience methods checks to see whether the parent
	 * of this node is of type {@link TagElement TagElement).
	 * </p>
	 * 
	 * @return <code>true</code> if this node is a nested tag element,
	 * and false if this node is either parented by a doc comment node
	 * ({@link Javadoc Javadoc}), or is unparented
	 */
	public boolean isNested() {
		return (getParent() instanceof TagElement);
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int memSize() {
		int size = BASE_NODE_SIZE + 2 * 4 + stringSize(optionalTagName);
		return size;
	}
	
	/* (omit javadoc for this method)
	 * Method declared on ASTNode.
	 */
	int treeSize() {
		return memSize() + fragments.listSize();
	}
}
