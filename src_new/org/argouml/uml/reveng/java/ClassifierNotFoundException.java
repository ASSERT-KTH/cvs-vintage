// $Id: ClassifierNotFoundException.java,v 1.1 2001/05/27 19:24:59 marcus Exp $

package org.argouml.uml.reveng.java;

/**
   This is thrown when a classifier can not be located in the model or
   via the classpath.
*/
class ClassifierNotFoundException extends RuntimeException
{
    public ClassifierNotFoundException(String name)
    {
	super("classifier not found: " + name);
    }
}
	
