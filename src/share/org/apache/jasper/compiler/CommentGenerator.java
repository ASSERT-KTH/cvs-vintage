/*
 *  Copyright 1999-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language 
 */
/*

package org.apache.jasper.compiler;

import org.apache.jasper.JasperException;

public interface CommentGenerator {
    
    /**
     * Generates "start-of the JSP-embedded code block" comment
     *
     * @param out The ServletWriter
     * @param start Start position of the block
     * @param stop End position of the block
     * @exception JasperException
     *
     * @author Mandar Raje [Patch submitted by Yury Kamen]
     */
    void generateStartComment(Generator generator, ServletWriter out, Mark start, Mark stop) throws JasperException;

    /**
     * Generates "end-of the JSP-embedded code block" comment
     *
     * @param out The ServletWriter
     * @param start Start position of the block
     * @param stop End position of the block
     * @exception JasperException 
     */
    void generateEndComment(Generator generator, ServletWriter out, Mark start, Mark stop) throws JasperException;
}
