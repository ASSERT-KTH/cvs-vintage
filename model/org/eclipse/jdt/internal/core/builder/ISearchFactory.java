package org.eclipse.jdt.internal.core.builder;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * <code>ISearchFactory</code> creates <code>ISearch</code> objects which can 
 * search some subset of the image.  These <code>ISearch</code> objects can 
 * be configured with a name, a scope, and a search context.  
 * <p>
 * <ul>
 * <li><b>Name:</b>
 * The name of the search specifies what the search is actually looking for.  
 * It can be the name of a type, a field, a package, a method, or a constructor. 
 * Searches for methods and constructors may also supply parameter and return 
 * type information. Wildcarding is supported.
 * <li><b>Scope:</b>
 * The scope of the search defines the subset of the image that is to be searched.  
 * Including a type in the scope includes all inner types, fields, methods and 
 * constructors of that type in the search.  Inner types also have all of their 
 * inner types, fields, methods and constructors included in the scope of the 
 * search, recursively. Including a package in the scope includes all types contained
 * in the package in the scope and, therefore, all inner types, fields, methods and 
 * constructors. The items in the scope must belong to the same state as the
 * image from which this search engine was obtained. Results of the search will also 
 * be in the same state as the image. If the image is non-state-specific, the
 * items in the scope must be non-state-specific as well.  Furthermore, results
 * found by the search will also be non-state-specific.
 * <li><b>Context:</b>
 * The search context is defined using flags which specify whether the search is 
 * looking for declarations or references. Furthermore, when looking for 
 * references, the search can look either in the principle structure or the 
 * source of each type in the search scope. The flags can be 'OR'ed 
 * together to allow searches for declarations and references.
 * </ul>
 * <p>
 * <b>Example:</b><br>
 * Search for all references to a type named "String" in the scope of the 
 * package "hanoiExample".  Wait for the results, and then print them out.
 * <pre><code>
 * IImage image = dc.getImage();
 * ISearchFactory sf = image.newSearchFactory();
 * IHandle[] scope = {image.getPackageHandle("hanoiExample", false)};
 * ISearch search = 
 * 	sf.searchForType(
 *		"String", 
 *		scope, 
 *		ISearchFactory.SEARCH_FOR_PRINCIPLE_STRUCTURE_REFS | 
 *		ISearchFactory.SEARCH_FOR_SOURCE_REFS);
 * search.run(); // perform the search
 * for (int i = 0; i < search.getItemCount(); i++)
 *	System.out.println(search.getItem(i));
 * </code></pre>
 * @see ISearch
 */
public interface ISearchFactory
{   
	/* Flags for search context */

	/** 
	 * Context flag indicating that declarations are to be searched.
	 * Searches using this flag search over type, inner type,
	 * field, method and constructor declarations.  
	 */
	int SEARCH_FOR_DECLS = 0x01;

	/** 
	 * Context flag indicating that references in the principle structure
	 * are to be searched.  The only references which are contained in the
	 * principle structure are references to types and packages -- in field 
	 * declarations, superclass/superinterface declarations, parameter, exception, 
	 * and method return types.
	 */
	int SEARCH_FOR_PRINCIPLE_STRUCTURE_REFS = 0x02;

	/** 
	 * Context flag indicating that references in the source are to be 
	 * searched. Only references made within the body of methods/constructors,
	 * in static and instance initializers, or in import declarations are 
	 * treated as source references.  References such as superclass names and
	 * parameter, exception and method return types, etc. are found in the 
	 * principle structure.
	 */
	int SEARCH_FOR_SOURCE_REFS = 0x04;

/**
 * Returns an <code>ISearch</code> that will look for fields with a matching
 * name.  
 * <p>
 * <b>Example:</b> Searching for declarations of fields named <code>width</code> 
 *      in package <code>java.awt</code>.
 * <pre><code>
 * IImage image = dc.getImage();
 * ISearchFactory sf = image.newSearchFactory();
 * IHandle[] scope = {image.getPackageHandle("java.awt", false)};
 * ISearch search = 
 *  sf.searchForField(
 *      "width", 
 *      scope, 
 *      ISearchFactory.SEARCH_FOR_DECLS);
 * search.run();
 * </code></pre> 
 * @param fieldName the name of the field to search for, possibly containing 
 *      wildcards ("*")
 * @param scope the packages and types to search in
 * @param context the context flags.  SEARCH_FOR_DECLS, and 
 *      SEARCH_FOR_SOURCE_REFS are valid flags for field searches.
 *      SEARCH_FOR_PRINCIPLE_STRUCTURE_REFS will not have any effect on
 *      the search because there are no field references in the principle
 *      structure.
 *
 *
 * @see ISearch
 */
ISearch searchForField(
		String fieldName, 
		IHandle[] scope,
		int context);
/**
 * Returns an <code>ISearch</code> that will look for methods or constructors 
 * with matching name, parameter types, and return type.  
 * <p>
 * When searching for a constructor, the return type is ignored.
 * <p>
 * <b>Example:</b> Search for declarations of methods named <code>toString</code>
 * whose second argument is <code>int</code>. The search will be conducted 
 * in the scope of the <code>hanoiExample</code> package.
 *  <code><pre>
 * IImage image = dc.getImage();
 * ISearchFactory factory = image.newSearchFactory();
 * IHandle[] scope = {image.getPackageHandle("hanoiExample")};
 * ISearch search = 
 *  factory.searchForMethod(
 *      "toString", 
 *      new String[] {"*", "int"}, 
 *      "*",
 *      scope, 
 *      SEARCH_FOR_DECLS);
 * search.run();
 *  </code></pre>
 * <br>
 * @param methodName the method name to search for, possibly containing wildcards ("*")
 * @param paramTypes the names of parameter types the method being searched for must 
 *      have, possibly containing wildcards. An empty array indicates a method with 
 *      zero parameters.
 * @param returnType the name of the return type the method being 
 *      searched for must have, possibly containing wildcards. A return type 
 *      of "*" effectively ignores the return type.
 * @param scope the packages and types to search in
 * @param context the context flags. SEARCH_FOR_DECLS make sense as a flag for 
 *      this method search. Using SEARCH_FOR_PRINCIPLE_STRUCTURE_REFS will have no effect since methods
 *      cannot be referenced in the principle structure. When searching the source,
 *      SEARCH_FOR_SOURCE_REFS will ignore the return type, and only use the 
 *      number of parameters, not the parameter types.  This is because at the source 
 *      level only the method name and number of parameters are 
 * known.
 * @see ISearch
 */
ISearch searchForMethod(
		String methodName,
		String[] paramTypes,
		String returnType,
		IHandle[] scope,
		int context);
/**
 * Returns an <code>ISearch</code> that will look for methods or constructors
 * with matching name, number of parameters, and return type. A parameter count
 * of -1 indicates that the number of parameters doesn't matter.
 * <p>
 * When searching for a constructor, the return type is ignored.
 * <p>
 * <b>Example 1:</b> Search for declarations and references to methods named <code>add</code> 
 * which have 2 arguments. The search will be conducted in the scope of the <code>hanoiExample</code>
 * package. Note that the principle structure is not searched for references 
 * since it never contains information about references to methods.
 *  <code><pre>
 * IImage image = dc.getImage();
 * ISearchFactory factory = image.newSearchFactory();
 * IHandle[] scope = {image.getPackageHandle("hanoiExample")};
 * ISearch search = 
 *  factory.searchForMethod(
 *      "add", 
 *      2, 
 *      "*",
 *      scope, 
 *      SEARCH_FOR_DECLS | 
 *      SEARCH_FOR_SOURCE_REFS);
 * search.run();
 *  </code></pre>
 * <br>
 * <b>Example 2:</b> Search for references to a constructor for a class named 
 * <code>Disk</code> which takes any number of arguments. The search will be conducted in
 * the scope of the <code>hanoiExample</code> package. Note that the principle structure is not searched for references 
 * since it never contains information about references to methods.
 *  <code><pre>
 * IImage image = dc.getImage();
 * ISearchFactory factory = image.newSearchFactory();
 * IHandle[] scope = {image.getPackageHandle("hanoiExample")};
 * ISearch search = 
 *  factory.searchForMethod(
 *      "Disk",
 *      -1, 
 *      "", // return type ignored for constructors
 *      scope, 
 *      SEARCH_FOR_SOURCE_REFS);
 * search.run();
 *  </code></pre> 
 *
 * @param methodName   the method name to search for, possibly containing wildcards ("*")
 * @param parameterCount the number of parameters the method being 
 *          searched for has. Use -1 if number of parameters doesn't matter
 * @param returnType the name of the return type the method being 
 *      searched for must have, possibly containing wildcards. A return type of
 *      "*" effectively ignores return type.
 * @param scope the packages and types to search in
 * @param context the context flags. SEARCH_FOR_DECLS and 
 *      SEARCH_FOR_SOURCE_REFS make sense as flags for this method search.
 *      SEARCH_FOR_PRINCIPLE_STRUCTURE_REFS will not have any effect since 
 *      principle structures cannot reference methods.
 *
 * @see ISearch
 */
ISearch searchForMethod(
		String methodName,
		int parameterCount, 
		String returnType,
		IHandle[] scope,
		int context);
/**
 * Returns an <code>ISearch</code> that will look for packages with a matching
 * name.
 * <p>
 * <b>Example:</b><br>
 * Searching for all references (principle structure and source) to packages 
 * matching <code>java.aw*</code> in the scope of the <code>hanoiExample</code> 
 * package.
 * <pre><code>
 * IImage image = dc.getImage();
 * ISearchFactory sf = image.newSearchFactory();
 * IHandle[] scope = {image.getPackageHandle("hanoiExample", false)};
 * ISearch search = 
 *  sf.searchForPackage(
 *      "java.aw*", 
 *      scope, 
 *      ISearchFactory.SEARCH_FOR_PRINCIPLE_STRUCTURE_REFS | 
 *      ISearchFactory.SEARCH_FOR_SOURCE_REFS); 
 * search.run();
 * </code></pre>
 *
 * @param packageName the name of the package to search for, possibly
 *      containing wildcards ("*")
 * @param scope the packages and types to search in
 * @param context the context flags. SEARCH_FOR_PRINCIPLE_STRUCTURE_REFS,
 *      and SEARCH_FOR_SOURCE_REFS make sense as flags for package 
 *      searches. Using SEARCH_FOR_DECLS will not have any effect because
 *      packages are not declared anywhere.
 *
 * @see ISearch
 */
ISearch searchForPackage(
		String packageName, 
		IHandle[] scope,
		int context);
/**
 * Returns an <code>ISearch</code> that will look for types with a matching
 * name.
 * <p>
 * <b>Example:</b><br>
 * Searching for all declarations of a type named <code>Post</code> in the scope of 
 * package <code>hanoiExample</code>.
 * <pre><code>
 * IImage image = dc.getImage();
 * ISearchFactory sf = image.newSearchFactory();
 * IHandle[] scope = {image.getPackageHandle("hanoiExample", false)};
 * ISearch search = 
 *  sf.searchForType(
 *      "Post", 
 *      scope, 
 *      ISearchFactory.SEARCH_FOR_DECLS); 
 * search.run();
 * </code></pre>
 * @param typeName   the simple name of the type to search for, possibly
 *      containing wildcards ("*")
 * @param scope the packages and types to search in
 * @param context   the context flags.  SEARCH_FOR_DECLS, SEARCH_FOR_PRINCIPLE_STRUCTURE_REFS,
 *      and SEARCH_FOR_SOURCE_REFS all make sense as flags for type searches.
 *
 * @see ISearch
 */
ISearch searchForType(
		String typeName, 
		IHandle[] scope,
		int context);
}
