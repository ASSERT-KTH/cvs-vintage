package org.jboss.verifier.strategy;

/*
 * Class org.jboss.verifier.strategy.AbstractVerifier
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
 * $Id: AbstractVerifier.java,v 1.1 2000/06/03 21:43:55 juha Exp $
 */

// standard imports
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


// non-standard class dependencies


/**
 * Abstract superclass for verifiers containing a bunch of useful methods.
 *
 * For more detailed documentation, refer to the
 * <a href="" << INSERT DOC LINK HERE >> </a>
 *
 * @see     org.jboss.verifier.strategy.VerificationStrategy
 *
 * @author 	Juha Lindfors (jplindfo@helsinki.fi)
 * @version $Revision: 1.1 $
 * @since  	JDK 1.3
 */
public abstract class AbstractVerifier implements VerificationStrategy {

    /*
     * checks if a class's member (method, constructor or field) has a 'static'
     * modifier.
     */
    public boolean isStaticMember(Member member) {
        
        if (member.getModifiers() == Modifier.STATIC)
            return true;
            
        return false;
    }
    
    /*
     * checks if a class's member (method, constructor or field) has a 'final'
     * modifier.
     */
    public boolean isFinalMember(Member member) {
        
        if (member.getModifiers() == Modifier.FINAL)
            return true;
            
        return false;
    }
    
    /*
     * checks if a method has a void return type
     */
    public boolean hasVoidReturnType(Method method) {
        
        return (method.getReturnType() == Void.TYPE);
    }
    
}


