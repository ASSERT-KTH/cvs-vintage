package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.EJBVerifier20
 * Copyright (C) 2000  Juha Lindfors
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * This package and its source code is available at www.jboss.org
 * $Id: EJBVerifier20.java,v 1.10 2002/03/05 18:44:24 ejort Exp $
 */


// standard imports
import java.util.Iterator;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;


// non-standard class dependencies
import org.jboss.verifier.factory.DefaultEventFactory;

import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.EntityMetaData;



/**
 * EJB 2.0 bean verifier.
 *
 * @author 	Juha Lindfors   (jplindfo@helsinki.fi)
 * @version $Revision: 1.10 $
 * @since  	JDK 1.3
 */
public class EJBVerifier20 extends AbstractVerifier {

    /*
     * Constructor
     */
    public EJBVerifier20(VerificationContext context) {
        super(context, new DefaultEventFactory());
    }

    
/*
 ***********************************************************************
 *
 *    IMPLEMENTS VERIFICATION STRATEGY INTERFACE
 *
 ***********************************************************************
 */
    
    public void checkSession(SessionMetaData session)
    {
       System.out.println("WARNING: EJBVerifier2.0 Session verification not implemented");        
    }            

    public void checkEntity(EntityMetaData entity)
    {
       System.out.println("WARNING: EJBVerifier2.0 Entity verification not implemented");        
    }        
        
    public void checkMessageBean(BeanMetaData bean)
    {
       System.out.println("WARNING: EJBVerifier2.0 Message verification not implemented");        
    }

    public boolean isCreateMethod(Method m) 
    {
        return m.getName().startsWith(CREATE_METHOD);
    }

    public boolean isEjbCreateMethod(Method m) 
    {
        return m.getName().startsWith(EJB_CREATE_METHOD);
    }
}

