/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.nrmi.algorithm;

/**
 * A utility callback to print the layout of an object
 * @author <a href="mailto:tilevich@cc.gatech.edu">Eli Tilevich</a>
 * @version $Revision: 1.1 $
 * */
public class ObjectPrinterCallback implements DFObjectWalker.Callback
{
public void onNull () {
	System.out.println ("null");
}

public void onRepeatedObject (Object obj) {
	System.out.println ("The object graph has cycles - found " + obj + " again");	
}

public void onEntry (Object obj) {
	System.out.println ("Walking object " + obj);
}
	
public void onExit (Object obj) {
	System.out.println ("Finished walking object " + obj);
}

public void onBooleanField (boolean field) {
	System.out.println ("Encountered field " + field);		
}

public void onByteField (byte field) {
	System.out.println ("Encountered field " + field);			
}

public void onCharField (char field) {
	System.out.println ("Encountered field " + field);			
}

public void onShortField (short field) {
	System.out.println ("Encountered field " + field);			
}

public void onIntField (int field) {
	System.out.println ("Encountered field " + field);			
}	

public void onLongField (long field) {
	System.out.println ("Encountered field " + field);			
}

public void onFloatField (float field) {
	System.out.println ("Encountered field " + field);			
}

public void onDoubleField (double field) {
	System.out.println ("Encountered field " + field);			
}

public void onPrimitiveArray (Object arrayField) {
	System.out.println ("Encountered primitive array field " + arrayField);			
}

public void onObjectArray (Object arrayField) {
	System.out.println ("Encountered objecdt array field " + arrayField);			
}	

}
