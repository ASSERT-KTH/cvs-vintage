package org.tigris.scarab.util.word;

/* ================================================================
 * Copyright (c) 2000-2002 CollabNet.  All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if
 * any, must include the following acknowlegement: "This product includes
 * software developed by Collab.Net <http://www.Collab.Net/>."
 * Alternately, this acknowlegement may appear in the software itself, if
 * and wherever such third-party acknowlegements normally appear.
 * 
 * 4. The hosted project names must not be used to endorse or promote
 * products derived from this software without prior written
 * permission. For written permission, please contact info@collab.net.
 * 
 * 5. Products derived from this software may not use the "Tigris" or 
 * "Scarab" names nor may "Tigris" or "Scarab" appear in their names without 
 * prior written permission of Collab.Net.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL COLLAB.NET OR ITS CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER
 * IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many
 * individuals on behalf of Collab.Net.
 */ 

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.torque.TorqueException;
import org.tigris.scarab.om.Module;
import org.tigris.scarab.om.RModuleIssueType;

    public class QueryResult
    {
        /** the search that created this QueryResult */
        private final IssueSearch search;

        QueryResult(IssueSearch search)
        {
            this.search = search;
        }

        String issueId;

        /**
         * Get the IssueId value.
         * @return the IssueId value.
         */
        public String getIssueId()
        {
            return issueId;
        }

        /**
         * Set the IssueId value.
         * @param newIssueId The new IssueId value.
         */
        public void setIssueId(String newIssueId)
        {
            this.issueId = newIssueId;
        }

        String idPrefix;

        /**
         * Get the IdPrefix value.
         * @return the IdPrefix value.
         */
        public String getIdPrefix()
        {
            return idPrefix;
        }

        /**
         * Set the IdPrefix value.
         * @param newIdPrefix The new IdPrefix value.
         */
        public void setIdPrefix(String newIdPrefix)
        {
            this.idPrefix = newIdPrefix;
        }

        String idCount;

        /**
         * Get the IdCount value.
         * @return the IdCount value.
         */
        public String getIdCount()
        {
            return idCount;
        }

        /**
         * Set the IdCount value.
         * @param newIdCount The new IdCount value.
         */
        public void setIdCount(String newIdCount)
        {
            this.idCount = newIdCount;
        }

        private String uniqueId;
        public String getUniqueId()
        {
            if (uniqueId == null) 
            {
                uniqueId = getIdPrefix() + getIdCount();
            }
            
            return uniqueId;
        }

        List attributeValues;

        /**
         * Get the AttributeValues value.
         * @return the AttributeValues value.
         */
        public List getAttributeValues()
        {
            return attributeValues;
        }

        /**
         * Get the AttributeValues value.
         * @return the AttributeValues value.
         */
        public List getAttributeValuesAsCSV()
        {
            List result = null;
            if (attributeValues != null) 
            {
                result = new ArrayList(attributeValues.size());
                for (Iterator i = attributeValues.iterator(); i.hasNext();) 
                {
                    String csv = null;
                    List multiVal = (List)i.next();
                    if (multiVal.size() == 1) 
                    {
                        csv = (String)multiVal.get(0);    
                        if (csv == null) 
                        {
                            csv = "";
                        }
                    }
                    else 
                    {
                        StringBuffer sb = new StringBuffer();
                        boolean addComma = false;
                        for (Iterator j = multiVal.iterator(); j.hasNext();) 
                        {
                            if (addComma) 
                            {
                                sb.append(", ");
                            }
                            else 
                            {
                                addComma = true;
                            }
                            
                            sb.append(j.next().toString());
                        }
                        csv = sb.toString();
                    }
                    result.add(csv);
                }
            }
            
            return result;
        }

        /**
         * Set the AttributeValues value.
         * @param newAttributeValues The new AttributeValues value.
         */
        public void setAttributeValues(List newAttributeValues)
        {
            this.attributeValues = newAttributeValues;
        }
        
        Integer moduleId;

        /**
         * Get the ModuleId value.
         * @return the ModuleId value.
         */
        public Integer getModuleId()
        {
            return moduleId;
        }

        /**
         * Set the ModuleId value.
         * @param newModuleId The new ModuleId value.
         */
        public void setModuleId(Integer newModuleId)
        {
            this.moduleId = newModuleId;
        }

        public Module getModule()
            throws TorqueException
        {
            return search.getModule(moduleId);
        }

        Integer issueTypeId;

        /**
         * Get the IssueTypeId value.
         * @return the IssueTypeId value.
         */
        public Integer getIssueTypeId()
        {
            return issueTypeId;
        }

        /**
         * Set the IssueTypeId value.
         * @param newIssueTypeId The new IssueTypeId value.
         */
        public void setIssueTypeId(Integer newIssueTypeId)
        {
            this.issueTypeId = newIssueTypeId;
        }

        public RModuleIssueType getRModuleIssueType()
            throws TorqueException
        {
            return search.getRModuleIssueType(moduleId, issueTypeId);
        }
    }
