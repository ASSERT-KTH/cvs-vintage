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
 * $Id: EJBVerifier20.java,v 1.2 2000/06/03 15:24:15 juha Exp $
 */


// standard imports
import java.util.Iterator;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Method;


// non-standard class dependencies
import org.gjt.lindfors.pattern.StrategyContext;

import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationEventFactory;

import com.dreambean.ejx.ejb.Session;
import com.dreambean.ejx.ejb.Entity;



/**
 * EJB 2.0 bean verifier.
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     << OTHER RELATED CLASSES >>
 *
 * @author 	Juha Lindfors
 * @version $Revision: 1.2 $
 * @since  	JDK 1.3
 */
public class EJBVerifier20 implements VerificationStrategy {

    /*
     * [TODO]   The EJB 1.1 verifier and EJB 2.0 verifier are going to share
     *          some common code. These ought to be moved to an abstract
     *          verifier super class.
     */
     
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

    public void checkSessions(Iterator beans) {

        while (beans.hasNext()) { 
            try {
                //checkSession((Session)beans.next());
            }
            catch (ClassCastException e) {
                System.err.println(e);
                // THROW INTERNAL ERROR   
            }
        }
    }            

    public void checkEntities(Iterator beans) {
        
        while (beans.hasNext()) {
            try {
                //checkEntity((Entity)beans.next());
            }
            catch (ClassCastException e) {
                System.err.println(e);
                // THROW INTERNAL ERROR
            }
        }
    }        
        
    public void checkMessageDriven(Iterator beans) {
            
            // NOT IMPLEMENTED YET
    }
        
    
}

