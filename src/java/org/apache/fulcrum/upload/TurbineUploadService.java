package org.apache.fulcrum.upload;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Turbine" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Turbine", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.fulcrum.BaseService;
import org.apache.fulcrum.InitializationException;
import org.apache.fulcrum.ServiceException;

/**
 * <p> This class is an implementation of {@link UploadService}.
 *
 * <p> Files will be stored in temporary disk storage on in memory,
 * depending on request size, and will be available from the {@link
 * org.apache.fulcrum.util.parser.ParameterParser} as {@link
 * org.apache.fulcrum.upload.FileItem}s.
 *
 * <p>This implementation of {@link UploadService} handles multiple
 * files per single html widget, sent using multipar/mixed encoding
 * type, as specified by RFC 1867.  Use {@link
 * org.apache.fulcrum.util.parser.ParameterParser#getFileItems(String)} to
 * acquire an array of {@link
 * org.apache.fulcrum.upload.FileItem}s associated with given
 * html widget.
 *
 * @author <a href="mailto:Rafal.Krzewski@e-point.pl">Rafal Krzewski</a>
 * @author <a href="mailto:dlr@collab.net">Daniel Rall</a>
 * @author <a href="mailto:jvanzyl@apache.org">Jason van Zyl</a>
 * @version $Id: TurbineUploadService.java,v 1.1 2004/10/24 22:12:30 dep4b Exp $
 */
public class TurbineUploadService
    extends BaseService
    implements UploadService
{
    protected Object component;
    private boolean automatic;

    private int sizeThreshold;
    private int sizeMax;

    /**
     * Initializes the service.
     *
     * This method processes the repository path, to make it relative to the
     * web application root, if neccessary
     */
    public void init() throws InitializationException
    {
        String repoPath = getConfiguration().getString(
            UploadService.REPOSITORY_KEY,
            UploadService.REPOSITORY_DEFAULT);

        // test for the existence of the path within the webapp directory.
        // if it does not exist, assume the path was to be used as is.
        String testPath = getRealPath(repoPath);
        File testDir = new File(testPath);
        if ( testDir.exists() ) 
        {
            repoPath = testPath;
        }

        getConfiguration().setProperty(UploadService.REPOSITORY_KEY, repoPath);

        getCategory().debug(
            "Upload Service: REPOSITORY_KEY => " + repoPath);

        FileUpload upload = new FileUpload();

        // remove this when getAutomatic() is removed.
        automatic = getConfiguration().getBoolean(
            UploadService.AUTOMATIC_KEY,
            UploadService.AUTOMATIC_DEFAULT);

        sizeMax = getConfiguration().getInt(
            UploadService.SIZE_MAX_KEY,
            UploadService.SIZE_MAX_DEFAULT);
        upload.setSizeMax(sizeMax);

        sizeThreshold = getConfiguration().getInt(
            UploadService.SIZE_THRESHOLD_KEY,
            UploadService.SIZE_THRESHOLD_DEFAULT);
        upload.setSizeThreshold(sizeThreshold);

        upload.setRepositoryPath( getConfiguration().getString(
            UploadService.REPOSITORY_KEY,
            UploadService.REPOSITORY_DEFAULT) );

        component = upload;
        setInit(true);
    }

    public Object getComponent()
    {
        return component;
    }


    /**
     * Used to determine whether the parseRequest method should be
     * called automatically.  
     * 
     * @deprecated This is left over from coupling with
     * ParameterParser.  ParameterParser (and any other use of the
     * service) should determine for itself whether to call the
     * parseRequest method and not ask the service.
     */
    public boolean getAutomatic() 
    {
        return automatic;
    }
    
    /**
     * The maximum allowed upload size
     */
    public int getSizeMax() 
    {
        return ((FileUpload)getComponent()).getSizeMax();
    }
    

    /**
     * The threshold beyond which files are written directly to disk.
     */
    public int getSizeThreshold()
    {
        return ((FileUpload)getComponent()).getSizeThreshold();
    }

    /**
     * The location used to temporarily store files that are larger
     * than the size threshold.
     */
    public String getRepository()
    {
        return ((FileUpload)getComponent()).getRepositoryPath();
    }

    /**
     * <p>Parses a <a href="http://rf.cx/rfc1867.html">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.</p>
     *
     * @param req The servlet request to be parsed.
     * @param path The location where the files should be stored.
     * @exception ServiceException Problems reading/parsing the
     * request or storing the uploaded file(s).
     */
    public ArrayList parseRequest(HttpServletRequest req, String path)
            throws ServiceException
    {
        try
        {
            return (ArrayList)
                ((FileUpload)getComponent())
                .parseRequest(req, sizeThreshold, sizeMax, path);
        }
        catch (FileUploadException e)
        {
            throw new ServiceException(e);
        }
    }


    /**
     * <p>Parses a <a href="http://rf.cx/rfc1867.html">RFC 1867</a>
     * compliant <code>multipart/form-data</code> stream.</p>
     *
     * @param req The servlet request to be parsed.
     * @param sizeThreshold the max size in bytes to be stored in memory
     * @param sizeMax the maximum allowed upload size in bytes
     * @param path The location where the files should be stored.
     * @exception ServiceException Problems reading/parsing the
     * request or storing the uploaded file(s).
     */
    public List parseRequest(HttpServletRequest req, int sizeThreshold,
                                  int sizeMax, String path)
            throws ServiceException
    {
        try
        {
            return ((FileUpload)getComponent())
                .parseRequest(req, sizeThreshold, sizeMax, path);
        }
        catch (FileUploadException e)
        {
            throw new ServiceException(e);
        }
    }
}
