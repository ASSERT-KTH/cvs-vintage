/*
 * @(#) JUnicastServerRef.java	1.0 02/07/15
 *
 * Copyright (C) 2002 - INRIA (www.inria.fr)
 *
 * CAROL: Common Architecture for RMI ObjectWeb Layer
 *
 * This library is developed inside the ObjectWeb Consortium,
 * http://www.objectweb.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 * 
 *
 */
package org.objectweb.carol.rmi.multi;

//java import
import java.util.Enumeration;
import java.util.Hashtable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

//javax import
import javax.rmi.CORBA.PortableRemoteObjectDelegate;

//carol import 
import org.objectweb.carol.util.multi.ProtocolCurrent;
import org.objectweb.carol.util.configuration.TraceCarol;

/*
 * Class <code>MultiPRODelegate</code> This is a proxy for multi orb portable remote object delegate 
 * reference this class with the systeme property :
 * java -Djavax.rmi.CORBA.PortableRemoteObjectClass=org.objectweb.carol.rmi.multi.MultiPRODelegate ...
 * for the moment this class is only for one orb
 * This class parse the communication.xml file for initilization 
 * 
 * @author  Guillaume Riviere (Guillaume.Riviere@inrialpes.fr)
 * @version 1.0, 15/07/2002  
 */
public class MultiPRODelegate implements PortableRemoteObjectDelegate {
    

    /**
     * Static boolean for initialization
     */
    private static boolean init = false;

    /**
     * Standard Hashtable PortableRemoteObjectDelegates
     */
    private static Hashtable activesProtocols = null;

    /**
     * Current Protocol
     */
     private static ProtocolCurrent pcur = null; 

    /*
     * constructor for this PortableRemoteObjectDelegateProxy
     */
    public MultiPRODelegate() throws RemoteException {
	if (TraceCarol.isDebugRmiCarol()) {
            TraceCarol.debugRmiCarol("MultiPRODelegate.MultiPRODelegate()");
        }
	try {
	    initProtocols();
	} catch (Exception e) {
	    String msg = "MultiPRODelegate, init protocols fail";
	    TraceCarol.error(msg,e);
	    throw new RemoteException(msg);
	}
    }
    
    /**
     * Export a Remote Object on all available protocols 
     * @param Remote object to export
     * @exception RemoteException exporting remote object problem 
     */
    public void exportObject(Remote obj) throws RemoteException {
	if (TraceCarol.isDebugRmiCarol()) {
	    TraceCarol.debugRmiCarol("MultiPRODelegate.exportObject(" +simpleClass(obj.getClass().getName())
				     +" obj)");
	}
	try {
	    if (!init) {		
		initProtocols();
	    }
	    for (Enumeration e = activesProtocols.elements(); e.hasMoreElements() ;) {
		((PortableRemoteObjectDelegate)e.nextElement()).exportObject(obj);
	    }	    
	} catch (Exception e) {
	    String msg = "MultiPRODelegate.exportObject(Remote obj) fail";
	    TraceCarol.error(msg,e);
	    throw new RemoteException(msg);	    
	}
    }

    
    /**
     * Method for unexport object on all available protocols
     * @param Remote obj object to unexport 
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public void unexportObject(Remote obj) throws NoSuchObjectException {
	if (TraceCarol.isDebugRmiCarol()) {
	    TraceCarol.debugRmiCarol("MultiPRODelegate.unexportObject(" +simpleClass(obj.getClass().getName())
				     +" obj)");
	}
	try {
	    if (!init) {		
		initProtocols();
	    }
	    for (Enumeration e = activesProtocols.elements(); e.hasMoreElements() ;) {
		((PortableRemoteObjectDelegate)e.nextElement()).unexportObject(obj);
	    }	    
	} catch (Exception e) {
	    String msg = "MultiPRODelegate.unexportObject(Remote obj) fail";
	    TraceCarol.error(msg,e);
	    throw new NoSuchObjectException(msg);	    
	}	
    }

    /**
     * Connection method all available protocols
     * @param target a remote object;
     * @param source another remote object; 
     * @exception RemoteException if the connection fail
     */ 
    public void connect(Remote target,Remote source) throws RemoteException {
	if (TraceCarol.isDebugRmiCarol()) {
	    TraceCarol.debugRmiCarol("MultiPRODelegate.connect("+simpleClass(target.getClass().getName()) + " target,"
				     +simpleClass(source.getClass().getName())
				     +" source)");
	}
	try {
	    if (!init) {		
		initProtocols();
	    }
	    for (Enumeration e = activesProtocols.elements(); e.hasMoreElements() ;) {
		((PortableRemoteObjectDelegate)e.nextElement()).connect(target, source);
	    }	    
	} catch (Exception e) {
	    String msg = "MultiPRODelegate.connect(Remote target,Remote source) fail";
	    TraceCarol.error(msg,e);
	    throw new RemoteException(msg);	    
	}
    }

	
    /**
     * Narrow method on the default protocol
     * @param Remote obj the object to narrow 
     * @param Class newClass the expected type of the result  
     * @return an object of type newClass
     * @exception ClassCastException if the obj class is not compatible with a newClass cast 
     */
    public Object narrow(Object obj, Class newClass ) throws ClassCastException {
	if (TraceCarol.isDebugRmiCarol()) {
            TraceCarol.debugRmiCarol("MultiPRODelegate.narrow("+simpleClass(obj.getClass().getName()) + " obj,"
				     +simpleClass(newClass.getName())
				     +" class)/rmi name=\""+ pcur.getCurrentRMIName()+"\"");
        }
	try {
	    if (init) {
		return pcur.getCurrentPortableRemoteObject().narrow(obj, newClass);
	    } else {
		initProtocols();
		return pcur.getCurrentPortableRemoteObject().narrow(obj, newClass);
	    }
	} catch (Exception e) {
	    String msg = "MultiPRODelegate.narrow(Object obj, Class newClass) fail";
	    TraceCarol.error(msg,e);
	    throw new ClassCastException (msg);
	}
    }

    /**
     * To stub method on the iiop protocol
     * @return the stub object    
     * @param Remote object to unexport    
     * @exception NoSuchObjectException if the object is not currently exported
     */
    public Remote toStub(Remote obj) throws NoSuchObjectException {
	if (TraceCarol.isDebugRmiCarol()) {
            TraceCarol.debugRmiCarol("MultiPRODelegate.toStub(" +simpleClass(obj.getClass().getName())
				     +" obj)/rmi name=\"" +pcur.getCurrentRMIName()+"\"");
        }
	try {
	    if (init) {
		return pcur.getCurrentPortableRemoteObject().toStub(obj);
	    } else {
		initProtocols();
		return pcur.getCurrentPortableRemoteObject().toStub(obj);
	    }
	} catch (Exception e) {
	    String msg = "MultiPRODelegate.toStub(Remote obj) fail";
	    TraceCarol.error(msg,e);
	    throw new NoSuchObjectException(msg);
	}	
    }


    /**
     * Private method for orb intanciation
     */
    private void initProtocols() throws Exception {
	if (TraceCarol.isDebugRmiCarol()) {
            TraceCarol.debugRmiCarol("MultiPRODelegate.initProtocols()");
	}
	pcur = ProtocolCurrent.getCurrent();
	activesProtocols = pcur.getPortableRemoteObjectHashtable();
	init = true;
    }

    /**
     * Just the name of the class without the package
     */
    private String simpleClass(String c) {
	return c.substring(c.lastIndexOf('.') +1);
    }
}
