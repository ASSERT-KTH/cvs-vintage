package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.EJBVerifier20
 * Copyright (C) 2000  Juha Lindfors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This package and its source code is available at www.jboss.org
 * $Id: EJBVerifier20.java,v 1.8 2000/11/05 19:02:36 juha Exp $
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
 * @version $Revision: 1.8 $
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
    
    public void checkSession(SessionMetaData session) {
            
            // NOT IMPLEMENTED YET
    }            

    public void checkEntity(EntityMetaData entity) {
            
            // NOT IMPLEMENTED YET
    }        
        
    public void checkMessageBean(BeanMetaData bean) {
            
            // NOT IMPLEMENTED YET
    }
        
    
}

