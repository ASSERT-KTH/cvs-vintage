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
 * $Id: EJBVerifier20.java,v 1.6 2000/08/12 00:42:13 salborini Exp $
 */


// standard imports
import java.util.Iterator;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;


// non-standard class dependencies
import org.gjt.lindfors.pattern.StrategyContext;

import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.factory.VerificationEventFactory;

import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.EntityMetaData;



/**
 * EJB 2.0 bean verifier.
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author 	Juha Lindfors   (jplindfo@helsinki.fi)
 * @version $Revision: 1.6 $
 * @since  	JDK 1.3
 */
public class EJBVerifier20 extends AbstractVerifier {

    private VerificationContext context      = null; 


    /*
     * Constructor
     */
    public EJBVerifier20(VerificationContext context) {
        this.context = context;
    }

    
    /*
     ***********************************************************************
     *
     *    IMPLEMENTS VERIFICATION STRATEGY INTERFACE
     *
     ***********************************************************************
     */
    
    public StrategyContext getContext() {
        return context;
    }

    public void checkSession(SessionMetaData session) {
            
            // NOT IMPLEMENTED YET
    }            

    public void checkEntity(EntityMetaData entity) {
            
            // NOT IMPLEMENTED YET
    }        
        
    public void checkMessageDriven(BeanMetaData bean) {
            
            // NOT IMPLEMENTED YET
    }
        
    
}

