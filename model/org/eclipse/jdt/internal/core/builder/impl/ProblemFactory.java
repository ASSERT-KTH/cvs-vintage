package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.core.builder.IProblemDetail;
import org.eclipse.jdt.internal.compiler.problem.ProblemIrritants;

import org.eclipse.jdt.internal.compiler.util.CharOperation;
import java.util.*;

/**
 * @see IProblemFactory
 */
public class ProblemFactory implements IProblemFactory {

	protected Locale fLocale;
	protected ResourceBundle fCompilerResources;
	protected String[] fMessageTemplates;

	protected static Hashtable fgFactories = new Hashtable(5);
	
	private final static char[] DOUBLE_QUOTES = "''".toCharArray(); //$NON-NLS-1$
	private final static char[] SINGLE_QUOTE = "'".toCharArray(); //$NON-NLS-1$
	
	/**
	 * Creates a problem factory for the given locale.
	 */
	private ProblemFactory(Locale locale) {
		fLocale = locale;
		fCompilerResources =
			ResourceBundle.getBundle(
				"org.eclipse.jdt.internal.compiler.problem.messages", //$NON-NLS-1$
				locale);
		initializeMessageTemplates();
	}
	/**
	 * @see IProblemFactory
	 */
	public IProblem createProblem(
		char[] originatingFileName,
		int problemId,
		String[] arguments,
		int severity,
		int startPosition,
		int endPosition,
		int lineNumber) {
		String message = getLocalizedMessage(problemId, arguments);
		int sev = (severity & IProblem.Error) != 0 ? IProblemDetail.S_ERROR : 0;
		// SourceEntry is filled in later when problem is actually recorded.
		SourceEntry sEntry = null;
		if (lineNumber == 0)
			lineNumber = -1;
		return new ProblemDetailImpl(
			message,
			problemId,
			sev,
			sEntry,
			startPosition,
			endPosition,
			lineNumber);
	}
	/**
	 * @see IProblemFactory
	 */
	public Locale getLocale() {
		return fLocale;
	}
	/**
	 * @see IProblemFactory
	 */
	public String getLocalizedMessage(int id, String[] problemArguments) {
		StringBuffer output = new StringBuffer(80);
		String message =
			fMessageTemplates[(id & ProblemIrritants.IgnoreCategoriesMask)];
		if (message == null) {
			return "Unable to retrieve the error message for problem id: " //$NON-NLS-1$
				+ id
				+ ". Check compiler resources.";//$NON-NLS-1$
		}
		// for compatibility with MessageFormat which eliminates double quotes in original message
		char[] messageWithNoDoubleQuotes =
			CharOperation.replace(message.toCharArray(), DOUBLE_QUOTES, SINGLE_QUOTE);
		message = new String(messageWithNoDoubleQuotes);
		
		int length = message.length();
		int start = -1, end = length;
		while (true) {
			if ((end = message.indexOf('{', start)) > -1) {
				output.append(message.substring(start + 1, end));
				if ((start = message.indexOf('}', end)) > -1) {
					try {
						output.append(
							problemArguments[Integer.parseInt(message.substring(end + 1, start))]);
					} catch (NumberFormatException nfe) {
						output.append(message.substring(end + 1, start + 1));
					} catch (ArrayIndexOutOfBoundsException e) {
						return "Corrupted compiler resources for problem id: "//$NON-NLS-1$
							+ (id & ProblemIrritants.IgnoreCategoriesMask)
							+ ". Check compiler resources.";//$NON-NLS-1$
					}
				} else {
					output.append(message.substring(end, length));
					break;
				}
			} else {
				output.append(message.substring(start + 1, length));
				break;
			}
		}
		return output.toString();
	}
	/**
	 * Returns the problem factory for the given locale.
	 */
	public static ProblemFactory getProblemFactory(Locale locale) {
		ProblemFactory factory = (ProblemFactory) fgFactories.get(locale);
		if (factory == null) {
			factory = new ProblemFactory(locale);
			fgFactories.put(locale, factory);
		}
		return factory;
	}
/**
 * This method initializes the messageTemplates variable according
 * to the current Locale.
 */
protected void initializeMessageTemplates() {
	fMessageTemplates = new String[500];
	for (int i = 0; i < fMessageTemplates.length; i++) {
		try {
			fMessageTemplates[i] = fCompilerResources.getString(String.valueOf(i)); 
		} catch (MissingResourceException e) {
		}
	}
}
}