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

package org.tigris.scarab.migration.b15b16;

import java.sql.*;
import java.util.*;

import org.apache.tools.ant.BuildException;
import org.tigris.scarab.migration.JDBCTask;

/**
 * The creation transaction was previously used to retrieve the 
 * created date and creator for an issue.  This makes some queries
 * complex and error prone as well as adding inefficiency to the query.
 * Denormalizing a bit for easier and faster queries.
 * If the columns have not previously been added to SCARAB_ISSUE, this
 * script adds them and populates the columns with data from the 
 * SCARAB_TRANSACTION table. 
 *
 * @author <a href="mailto:jon@collab.net">John McNally</a>
 * @version $Id: DB_1_MoveIssueCreateInfo.java,v 1.8 2004/04/03 22:30:24 pledbrook Exp $
 */
public class DB_1_MoveIssueCreateInfo extends JDBCTask
{
    private static final String CREATE_ISSUE__PK = "1";
    private static final String MOVE_ISSUE__PK = "3";

    public DB_1_MoveIssueCreateInfo()
    {
    }

    public void execute() 
        throws BuildException
    {
        boolean proceed = false;
        try
        {
            // check whether SCARAB_ISSUE already has changes
            Connection conn = null;
            Statement stmt = null;
            try 
            {
                setAutocommit(true);
                conn = getConnection();
                String sql = "SELECT CREATED_TRANS_ID FROM SCARAB_ISSUE";
                stmt = conn.createStatement();
                try 
                {
                    stmt.executeQuery(sql);                    
                }
                catch (SQLException e)
                {
                    proceed = true;
                }                
            } 
            finally 
            {
                close(stmt, conn);
            }

            if (proceed) 
            {
                upgradeScarabIssue();
            }        
        }
        catch (Exception e)
        {
            throw new BuildException(e);
        }

        addNewIndices();
    }

    private void addNewIndices()
    {
        Connection conn = null;
        Statement stmt = null;
        try 
        {
            setAutocommit(true);
            conn = getConnection();
            String sql = "CREATE INDEX IX_ATTACHMENT on SCARAB_ACTIVITY (ATTACHMENT_ID)";
            try 
            {
                stmt = conn.createStatement();
                stmt.execute(sql);
            }
            catch (SQLException e)
            {
                System.out.println("index SCARAB_ACTIVITY.IX_ATTACHMENT was not created.  verify that it already exists.");
            }
            close(stmt, null);
            
            sql = "CREATE INDEX IX_ISSUE_ATTACHTYPE on SCARAB_ATTACHMENT (ISSUE_ID, ATTACHMENT_TYPE_ID)";
            try 
            {
                stmt = conn.createStatement();
                stmt.execute(sql);
            }
            catch (SQLException e)
            {
                System.out.println("index SCARAB_ATTACHMENT.IX_ISSUE_ATTACHTYPE  was not created.  verify that it already exists.");
            }
            close(stmt, null);
            
            sql = "CREATE INDEX IX_DEPEND on SCARAB_ACTIVITY (DEPEND_ID)";
            try 
            {
                stmt = conn.createStatement();
                stmt.execute(sql);
            }
            catch (SQLException e)
            {
                System.out.println("index SCARAB_ACTIVITY.IX_DEPEND was not created.  verify that it already exists.");
            }
        } 
        finally 
        {
            close(stmt, conn);
        }
    }


    private static final String MYSQL = "mysql";
    private static final String POSTGRESQL = "postgres";
    private static final String ORACLE = "oracle";
    private static final String[] supportedDBs = 
        {MYSQL, POSTGRESQL, ORACLE};
    private Map intTypes = new HashMap(3);
    private Map longTypes = new HashMap(3);
    private Map dateTypes = new HashMap(3);

    {
        intTypes.put(MYSQL, "INTEGER");
        intTypes.put(POSTGRESQL, "integer");
        intTypes.put(ORACLE, "NUMBER");
        longTypes.put(MYSQL, "BIGINT");
        longTypes.put(POSTGRESQL, "int8");
        longTypes.put(ORACLE, "NUMBER (20, 0)");
        dateTypes.put(MYSQL, "DATETIME");
        dateTypes.put(POSTGRESQL, "timestamp");
        dateTypes.put(ORACLE, "DATE");
    }

    private String getCanonicalDBProductName(Connection conn)
        throws SQLException, BuildException
    {
        DatabaseMetaData dmd = conn.getMetaData();
        String theVendor = dmd.getDatabaseProductName().toLowerCase();
        String result = null;
        for (int i=0; i<supportedDBs.length && result == null; i++) 
        {
            if (theVendor.indexOf(supportedDBs[i]) >= 0) 
            {
                result = supportedDBs[i];
            }
        }
        if (result == null) 
        {
            throw new BuildException("Unsupported database: " + theVendor);
        }
        
        return result;
    }

    private void upgradeScarabIssue()
        throws SQLException
    {
        Connection conn = null;
        Statement stmt = null;
        try 
        {
            // could try to do this as a transaction for db's that support
            // it, but db's should be backed up prior to migration so taking
            // the easy way out.
            setAutocommit(true);
            conn = getConnection();
            String dbtype = getCanonicalDBProductName(conn);
            String longType = (String)longTypes.get(dbtype);
            //String intType = (String)intTypes.get(dbtype);
            //String dateType = (String)dateTypes.get(dbtype);

            // add the transaction fk to SCARAB_ISSUE
            String sql = "alter table SCARAB_ISSUE add " + 
                "CREATED_TRANS_ID " + longType + " NULL";
            System.out.println(
                "Adding creation info to SCARAB_ISSUE");
            try 
            {
                stmt = conn.createStatement();
                stmt.execute(sql);
            }
            finally
            {
                close(stmt, null);
            }

            // add fk constraint
            sql ="alter table SCARAB_ISSUE add FOREIGN KEY (CREATED_TRANS_ID)"
                + " REFERENCES SCARAB_TRANSACTION(TRANSACTION_ID)";
            try 
            {
                stmt = conn.createStatement();
                stmt.execute(sql);
            }
            finally
            {
                close(stmt, null);
            }

            // get the highest ISSUE_ID 
            long max = -1L;
            sql = "select max(ISSUE_ID) from SCARAB_ISSUE";
            try 
            {
                stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql);                    
                rs.next();
                max = rs.getLong(1);
            }
            finally
            {
                close(stmt, null);
            }
            System.out.println("Updating " + max + " rows in SCARAB_ISSUE");
            System.out.print("...");

            // get create info for 1000 issue records at a time
            // there is still 1 update statement per issue 
            for (long i=1; i<max; i+=1000L) 
            {
                for (Iterator idAndInfo = 
                         getCreateInfoFromInitialActivitySets(i, i+1000, conn)
                         .entrySet().iterator(); idAndInfo.hasNext();) 
                {
                    Map.Entry me = (Map.Entry)idAndInfo.next();
                    //String[] info = (String[])me.getValue();
                    //setCreatedInfo((String)me.getKey(), (String)info[0], 
                    //               (String)info[1], conn);
                    setCreatedInfo((String)me.getKey(), (String)me.getValue(),
                                   conn);
                }
            }
            System.out.print("\ndone.");
        }
        finally
        {
            close(stmt, conn);
        }
    }

    /**
     * The initial activity set from issue creation.
     *
     * @return a <code>ActivitySet</code> value
     * @exception Exception if an error occurs
     */
    private Map getCreateInfoFromInitialActivitySets(long startIssueId, 
                                                     long endIssueId, 
                                                     Connection conn)
        throws SQLException
    {
        Map result = new HashMap(1500);
        Statement stmt = null;
        String sql = "select " + 
            //"a.ISSUE_ID, t.CREATED_DATE, t.CREATED_BY, t.TYPE_ID " +
            "a.ISSUE_ID, t.TRANSACTION_ID, t.TYPE_ID " +
            "FROM SCARAB_TRANSACTION t, SCARAB_ACTIVITY a " +
            "WHERE a.ISSUE_ID >= " + startIssueId + 
            " and a.ISSUE_ID < " + endIssueId +
            " and t.TYPE_ID IN (" + CREATE_ISSUE__PK + "," + MOVE_ISSUE__PK +
            ") and a.TRANSACTION_ID=t.TRANSACTION_ID " +
            "ORDER BY t.TYPE_ID ASC";
            System.out.print(".");
        try 
        {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) 
            {
                String id = rs.getString(1);
                if (!result.containsKey(id)) 
                {
                    //String[] info = {rs.getString(2), rs.getString(3)};
                    result.put(id, rs.getString(2));
                }
            }                
        }
        finally
        {
            close(stmt, null);
        }
        return result;
    }

    //private void setCreatedInfo(String issueId, String date, String userId,
    private void setCreatedInfo(String issueId, String activitySetId,
                                Connection conn)
        throws SQLException
    {
        Statement stmt = null;
        //String sql = "update SCARAB_ISSUE set CREATED_DATE='" + date + 
        //    "', CREATED_USER_ID=" + userId + " where ISSUE_ID=" + issueId;
        String sql = "update SCARAB_ISSUE set CREATED_TRANS_ID=" + 
            activitySetId + " where ISSUE_ID=" + issueId;
        try 
        {        
            stmt = conn.createStatement();
            stmt.executeUpdate(sql);
        } 
        finally 
        {
            close(stmt, null);
        }
    }

    private void close(Statement stmt, Connection conn)
    { 
        if (stmt != null) 
        {
            try 
            {
                stmt.close();
            }
            catch (SQLException ignore) {}
        }
        if (conn != null) 
        {
            try 
            {
                conn.close();
            }
            catch (SQLException ignore) {}
        }
    }
}
