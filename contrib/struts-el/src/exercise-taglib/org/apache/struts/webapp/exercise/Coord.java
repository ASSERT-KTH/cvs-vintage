/*
 * $Header: /tmp/cvs-vintage/struts/contrib/struts-el/src/exercise-taglib/org/apache/struts/webapp/exercise/Coord.java,v 1.2 2004/03/14 07:15:06 sraeburn Exp $
 * $Revision: 1.2 $
 * $Date: 2004/03/14 07:15:06 $
 *
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.struts.webapp.exercise;

/**
 * Simple bean to use for testing indexed tags.
 */
public class Coord implements java.io.Serializable
{
    private int   x;
    private int   y;
    
    public Coord() {}
    
    public Coord(int x, int y)
    {
        this.x = x;
        this.y = y;
    }
    
    public  int   getX() { return (x); }
    public  int   getY() { return (y); }

    public  void  setX(int x) { this.x = x; }
    public  void  setY(int y) { this.y = y; }

    public  String   toString()
    { return ("Coord[" + "x=" + x + ";y=" + y + "]"); }
}
