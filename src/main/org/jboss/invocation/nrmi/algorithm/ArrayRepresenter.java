/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation.nrmi.algorithm;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;

/**
 * Creates a linear map representation of a serializable parameter
 * @author <a href="mailto:tilevich@cc.gatech.edu">Eli Tilevich</a>
 * @version $Revision: 1.1 $
*/
public class ArrayRepresenter
{

public static class DoNothingOutputStream extends OutputStream
{
public DoNothingOutputStream () throws IOException {}
public void write(int b) throws IOException {}
}
	
private class ArrayRepresenterStream extends ObjectOutputStream {

public ArrayRepresenterStream (OutputStream out)
		throws IOException {
super(out);
enableReplaceObject(true);
}
	 
protected Object replaceObject (Object obj) throws IOException {
onEntry (obj);
return obj;
}
}	
	
private Object _obj;
private ArrayList _aObjects = new ArrayList ();
public ArrayRepresenter (Object obj) {
_obj = obj;
}
	
public void onEntry (Object obj) {
if (obj instanceof java.io.Serializable)
	_aObjects.add (obj);
}
	
public Object [] getRepresentation () {
try {
DoNothingOutputStream os = new DoNothingOutputStream();
ObjectOutputStream outStream = new ArrayRepresenterStream(os);
outStream.writeObject(_obj);
} catch (IOException e) {
	e.printStackTrace ();	
}
return _aObjects.toArray ();
}
}