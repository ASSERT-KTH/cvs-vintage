/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.ejb.plugins.cmp.bridge.CMPFieldBridge;

import org.jboss.ejb.plugins.cmp.ejbql.ASTAbs;
import org.jboss.ejb.plugins.cmp.ejbql.ASTAbstractSchema;
import org.jboss.ejb.plugins.cmp.ejbql.ASTAnd;
import org.jboss.ejb.plugins.cmp.ejbql.ASTApproximateNumericLiteral;
import org.jboss.ejb.plugins.cmp.ejbql.ASTArithmeticComparison;
import org.jboss.ejb.plugins.cmp.ejbql.ASTArithmeticParenthetical;
import org.jboss.ejb.plugins.cmp.ejbql.ASTBetween;
import org.jboss.ejb.plugins.cmp.ejbql.ASTBooleanComparison;
import org.jboss.ejb.plugins.cmp.ejbql.ASTBooleanLiteral;
import org.jboss.ejb.plugins.cmp.ejbql.ASTCollectionMemberDeclaration;
import org.jboss.ejb.plugins.cmp.ejbql.ASTComparison;
import org.jboss.ejb.plugins.cmp.ejbql.ASTConcat;
import org.jboss.ejb.plugins.cmp.ejbql.ASTConditionalParenthetical;
import org.jboss.ejb.plugins.cmp.ejbql.ASTDatetimeComparison;
import org.jboss.ejb.plugins.cmp.ejbql.ASTEJBQL;
import org.jboss.ejb.plugins.cmp.ejbql.ASTEntityComparison;
import org.jboss.ejb.plugins.cmp.ejbql.ASTExactNumericLiteral;
import org.jboss.ejb.plugins.cmp.ejbql.ASTFrom;
import org.jboss.ejb.plugins.cmp.ejbql.ASTIdentifier;
import org.jboss.ejb.plugins.cmp.ejbql.ASTIn;
import org.jboss.ejb.plugins.cmp.ejbql.ASTIsEmpty;
import org.jboss.ejb.plugins.cmp.ejbql.ASTLCase;
import org.jboss.ejb.plugins.cmp.ejbql.ASTLength;
import org.jboss.ejb.plugins.cmp.ejbql.ASTLike;
import org.jboss.ejb.plugins.cmp.ejbql.ASTLocate;
import org.jboss.ejb.plugins.cmp.ejbql.ASTMemberOf;
import org.jboss.ejb.plugins.cmp.ejbql.ASTMultDiv;
import org.jboss.ejb.plugins.cmp.ejbql.ASTNegation;
import org.jboss.ejb.plugins.cmp.ejbql.ASTNot;
import org.jboss.ejb.plugins.cmp.ejbql.ASTNullComparison;
import org.jboss.ejb.plugins.cmp.ejbql.ASTOr;
import org.jboss.ejb.plugins.cmp.ejbql.ASTOrderBy;
import org.jboss.ejb.plugins.cmp.ejbql.ASTOrderByPath;
import org.jboss.ejb.plugins.cmp.ejbql.ASTParameter;
import org.jboss.ejb.plugins.cmp.ejbql.ASTPath;
import org.jboss.ejb.plugins.cmp.ejbql.ASTPlusMinus;
import org.jboss.ejb.plugins.cmp.ejbql.ASTRangeVariableDeclaration;
import org.jboss.ejb.plugins.cmp.ejbql.ASTSelect;
import org.jboss.ejb.plugins.cmp.ejbql.ASTSqrt;
import org.jboss.ejb.plugins.cmp.ejbql.ASTStringComparison;
import org.jboss.ejb.plugins.cmp.ejbql.ASTStringLiteral;
import org.jboss.ejb.plugins.cmp.ejbql.ASTStringParenthetical;
import org.jboss.ejb.plugins.cmp.ejbql.ASTSubstring;
import org.jboss.ejb.plugins.cmp.ejbql.ASTUCase;
import org.jboss.ejb.plugins.cmp.ejbql.ASTValueClassComparison;
import org.jboss.ejb.plugins.cmp.ejbql.ASTWhere;
import org.jboss.ejb.plugins.cmp.ejbql.SimpleNode;

import org.jboss.ejb.plugins.cmp.ejbql.BasicVisitor;
import org.jboss.ejb.plugins.cmp.ejbql.BlockStringBuffer;
import org.jboss.ejb.plugins.cmp.ejbql.Catalog;
import org.jboss.ejb.plugins.cmp.ejbql.EJBQLParser;
import org.jboss.ejb.plugins.cmp.ejbql.JBossQLParser;
import org.jboss.ejb.plugins.cmp.ejbql.JBossQLParserVisitor;
import org.jboss.ejb.plugins.cmp.ejbql.EJBQLTypes;
import org.jboss.ejb.plugins.cmp.ejbql.Node;

import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMPFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;

/**
 * Compiles EJB-QL and JBossQL into SQL.
 *
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @version $Revision: 1.9 $
 */
public class JDBCEJBQLCompiler extends BasicVisitor {

   // input objects
   private final Catalog catalog;
   private Class returnType;
   private Class[] parameterTypes;
   private JDBCReadAheadMetaData readAhead;

   // alias info
   private AliasManager aliasManager;

   // join info
   private Set declaredPaths = new HashSet();
   private Set joinPaths = new HashSet();
   private Map collectionMemberJoinPaths = new HashMap();
   private Map leftJoinPaths = new HashMap();

   // mapping metadata
   private JDBCTypeMappingMetaData typeMapping;
   private JDBCTypeFactory typeFactory;
   private boolean subquerySupported = false;
   
   // output objects
   private String sql;
   private Object selectObject;
   private List inputParameters = new ArrayList();

   public JDBCEJBQLCompiler(Catalog catalog) {
      this.catalog = catalog;
   }
   
   public void compileEJBQL(
         String ejbql,
         Class returnType,
         Class[] parameterTypes,
         JDBCReadAheadMetaData readAhead) throws Exception {

      // reset all state variables
      reset();

      // set input arguemts
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
      this.readAhead = readAhead;

      // get the parser   
      EJBQLParser parser = new EJBQLParser(new StringReader(""));
      
      try {
         // parse the ejbql into an abstract sytax tree
         ASTEJBQL ejbqlNode;
         ejbqlNode = parser.parse(catalog, parameterTypes, ejbql);

         // translate to sql
         sql = ejbqlNode.jjtAccept(this, new BlockStringBuffer()).toString();
      } catch(Exception e) {
         // if there is a problem reset the state before exiting
         reset();
         throw e;
      } catch(Error e) {
         // lame javacc lexer throws Errors
         reset();
         throw e;
      }
   }

   public void compileJBossQL(
         String ejbql,
         Class returnType,
         Class[] parameterTypes,
         JDBCReadAheadMetaData readAhead) throws Exception {

      // reset all state variables
      reset();

      // set input arguemts
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
      this.readAhead = readAhead;

      // get the parser   
      JBossQLParser parser = new JBossQLParser(new StringReader(""));
      
      try {
         // parse the ejbql into an abstract sytax tree
         ASTEJBQL ejbqlNode;
         ejbqlNode = parser.parse(catalog, parameterTypes, ejbql);

         // translate to sql
         sql = ejbqlNode.jjtAccept(this, new BlockStringBuffer()).toString();
      } catch(Exception e) {
         // if there is a problem reset the state before exiting
         reset();
         throw e;
      } catch(Error e) {
         // lame javacc lexer throws Errors
         reset();
         throw e;
      }
   }

   private void reset() {
      returnType = null;
      parameterTypes = null;
      readAhead = null;
      inputParameters = new ArrayList();
      declaredPaths = new HashSet();
      joinPaths = new HashSet();
      collectionMemberJoinPaths = new HashMap();
      leftJoinPaths = new HashMap();
      selectObject = null;
      typeFactory = null;
      typeMapping = null;
      aliasManager = null;
      subquerySupported = true;
   }

   public String getSQL() {
      return sql;
   }

   public boolean isSelectEntity() {
      return selectObject instanceof JDBCEntityBridge;
   }

   public JDBCEntityBridge getSelectEntity() {
      return (JDBCEntityBridge)selectObject;
   }

   public boolean isSelectField() {
      return selectObject instanceof JDBCCMPFieldBridge;
   }

   public JDBCCMPFieldBridge getSelectField() {
      return (JDBCCMPFieldBridge)selectObject;
   }

   public List getInputParameters() {
      return inputParameters;
   }

   public Object visit(SimpleNode node, Object data) {
      throw new RuntimeException("Internal error: Found unknown node type in " +
            "EJB-QL abstract syntax tree: node=" + node);
   }

   private void setTypeFactory(JDBCTypeFactory typeFactory) {
      this.typeFactory = typeFactory;
      this.typeMapping = typeFactory.getTypeMapping();
      aliasManager = new AliasManager(
            typeMapping.getAliasHeaderPrefix(),
            typeMapping.getAliasHeaderSuffix(),
            typeMapping.getAliasMaxLength());
      subquerySupported = typeMapping.isSubquerySupported();
   }

   private Class getParameterType(int index) {
      int zeroBasedIndex = index - 1;
      Class[] params = parameterTypes;
      if(zeroBasedIndex < params.length) {
         return params[zeroBasedIndex];
      }
      return null;
   }

   // verify that parameter is the same type as the entity
   private void verifyParameterEntityType(
         int number,
         JDBCEntityBridge entity) {

      Class parameterType = getParameterType(number);
      Class remoteClass = entity.getMetaData().getRemoteClass();
      Class localClass = entity.getMetaData().getLocalClass();
      if((localClass==null || 
               !localClass.isAssignableFrom(parameterType)) &&
         (remoteClass==null || 
               !remoteClass.isAssignableFrom(parameterType))) {

         throw new IllegalStateException("Only like types can be " +
               "compared: from entity=" + entity.getEntityName() + 
               " to parameter type=" + parameterType);
      }
   }

   private void compareEntity(
         boolean not,
         Node fromNode,
         Node toNode,
         BlockStringBuffer buf) {

      buf.append("(");
      if(not) {
         buf.append("NOT(");
      }

      String fromAlias;
      JDBCEntityBridge fromEntity;
      ASTPath fromPath = (ASTPath)fromNode;
      joinPaths.add(fromPath);
      fromAlias = aliasManager.getAlias(fromPath.getPath());
      fromEntity = (JDBCEntityBridge)fromPath.getEntity();
      
      if(toNode instanceof ASTParameter) {
         ASTParameter toParam = (ASTParameter)toNode;

         // can only compare like kind entities
         verifyParameterEntityType(toParam.number, fromEntity);

         inputParameters.addAll(QueryParameter.createParameters(
                  toParam.number - 1,
                  fromEntity));

         buf.append(SQLUtil.getWhereClause(
                  fromEntity.getPrimaryKeyFields(), fromAlias));   
      } else {
         String toAlias;
         JDBCEntityBridge toEntity;
         ASTPath toPath = (ASTPath)toNode;
         joinPaths.add(toPath);
         toAlias = aliasManager.getAlias(toPath.getPath());
         toEntity = (JDBCEntityBridge)toPath.getEntity();

         // can only compare like kind entities
         if(!fromEntity.equals(toEntity)) {
            throw new IllegalStateException("Only like types can be " +
                  "compared: from entity=" + fromEntity.getEntityName() + 
                  " to entity="+toEntity.getEntityName());
         }

         buf.append(SQLUtil.getSelfCompareWhereClause(
               fromEntity.getPrimaryKeyFields(), 
               fromAlias, 
               toAlias));   
      }   

      if(not) {
         buf.append(")");
      }
      buf.append(")");
   }

   public void notExistsClause(ASTPath path, BlockStringBuffer buf) {
      if(!path.isCMRField()) {
         throw new IllegalArgumentException("path must be a cmr field");
      }

      JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)path.getCMRField();
      JDBCEntityBridge parentEntity = cmrField.getEntity();
      String parentAlias = aliasManager.getAlias(
            (String)path.getPath(path.size()-2));

      // if exists is not supported we use a left join and is null
      if(!subquerySupported) {
         
         // add the path to the list of paths to left join
         Set joins = (Set)leftJoinPaths.get(path.getPath(path.size()-2));
         if(joins == null) {
            joins = new HashSet();
            leftJoinPaths.put(path.getPath(path.size()-2), joins);
         }
         joins.add(path);

         if(cmrField.getRelationMetaData().isForeignKeyMappingStyle()) {
            JDBCEntityBridge childEntity = 
                  (JDBCEntityBridge)cmrField.getRelatedEntity();
            String childAlias = aliasManager.getAlias(path.getPath());

            buf.append(SQLUtil.getIsNullClause(
                  false, childEntity.getPrimaryKeyFields(), childAlias));
            
         } else {

            String relationTableAlias = 
                  aliasManager.getRelationTableAlias(path.getPath());

            buf.append(SQLUtil.getIsNullClause(
                  false, cmrField.getTableKeyFields(), relationTableAlias));
         }
         return;
      }
      
      buf.append("NOT EXISTS (");

      if(cmrField.getRelationMetaData().isForeignKeyMappingStyle()) {
         JDBCEntityBridge childEntity = 
               (JDBCEntityBridge)cmrField.getRelatedEntity();
         String childAlias = aliasManager.getAlias(path.getPath());

         buf.append("SELECT ");
         buf.append(SQLUtil.getColumnNamesClause(
               childEntity.getPrimaryKeyFields(), childAlias));
      
         buf.append(" FROM ");
         buf.append(childEntity.getTableName()).append(" ").append(childAlias);

         buf.append(" WHERE ");
         buf.append(SQLUtil.getJoinClause(cmrField, parentAlias, childAlias));
         
      } else {

         String relationTableAlias = 
               aliasManager.getRelationTableAlias(path.getPath());
         buf.append("SELECT ");
         buf.append(SQLUtil.getColumnNamesClause(
               cmrField.getTableKeyFields(), relationTableAlias));
      
         buf.append(" FROM ");
         buf.append(cmrField.getTableName());
         buf.append(" ");
         buf.append(relationTableAlias);

         buf.append(" WHERE ");
         buf.append(SQLUtil.getRelationTableJoinClause(
                  cmrField, 
                  parentAlias, 
                  relationTableAlias));
      }
               
      buf.append(")");
   }
   
   public Object visit(ASTEJBQL node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      Node selectNode = node.jjtGetChild(0);
      Node fromNode = node.jjtGetChild(1);
      Node whereNode = null;
      Node orderByNode = null;

      if(node.jjtGetNumChildren() == 3) {
         Node temp = node.jjtGetChild(2);
         if(temp instanceof ASTWhere) {
            whereNode = temp;
         } else if(temp instanceof ASTOrderBy) {
            orderByNode = temp;
         } 
      } else if(node.jjtGetNumChildren() == 4) {
         whereNode = node.jjtGetChild(2);
         orderByNode = node.jjtGetChild(3);
      }

      // translate select and add it to the buffer
      selectNode.jjtAccept(this, buf);

      // translate where and save results to append later
      BlockStringBuffer where = new BlockStringBuffer();
      if(whereNode != null)  {
         whereNode.jjtAccept(this, where);
      }

      // translate order by and save results to append later
      BlockStringBuffer orderBy = new BlockStringBuffer();
      if(orderByNode != null)  {
         orderByNode.jjtAccept(this, orderBy);
      }

      // translate from and add it to the buffer
      buf.append(" ");
      fromNode.jjtAccept(this, buf);

      // get theta joins
      BlockStringBuffer thetaJoin = new BlockStringBuffer();
      createThetaJoin(thetaJoin);

      // add the where clause
      if(where.length() != 0 && thetaJoin.length() != 0) {
         buf.append(" WHERE (").append(where);
         buf.append(") AND (").append(thetaJoin).append(")");
      } else if(where.length() != 0) {
         buf.append(" WHERE ").append(where);
      } else if(thetaJoin.length() != 0) {
         buf.append(" WHERE ").append(thetaJoin);
      }

      // add the orderBy clause
      if(orderBy.length() != 0) {
         buf.append(" ").append(orderBy);
      }

      return buf;
   }

   public Object visit(ASTFrom node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      buf.append("FROM ");
      for(int i=0; i < node.jjtGetNumChildren(); i++) {
         if(i > 0) {
            buf.append(", ");
         }
         node.jjtGetChild(i).jjtAccept(this, buf);
      }

      // add all the additional path tables
      for(Iterator iter = joinPaths.iterator(); iter.hasNext(); ) {
         ASTPath path = (ASTPath)iter.next();
         for(int i=0; i < path.size(); i++) {
            declareTables(path, i, buf);
         }
      }

      // get all the left joined paths 
      Set allLeftJoins = new HashSet();
      for(Iterator iter = leftJoinPaths.values().iterator(); iter.hasNext(); ) {
         allLeftJoins.addAll((Set)iter.next());
      }

      // add all parent paths for left joins
      for(Iterator iter = allLeftJoins.iterator(); iter.hasNext(); ) {
         ASTPath path = (ASTPath)iter.next();
         // don't declare the last one as the first path was left joined
         for(int i=0; i < path.size()-1; i++) {
            declareTables(path, i, buf);
         }
      }

      return buf;
   }

   private void declareTables(ASTPath path, int i, BlockStringBuffer buf) {
      if(!path.isCMRField(i) || declaredPaths.contains(path.getPath(i))) {
         return;
      }

      JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)path.getCMRField(i);
      JDBCEntityBridge entity = (JDBCEntityBridge)path.getEntity(i);

      buf.append(", ");
      buf.append(entity.getTableName());
      buf.append(" ");
      buf.append(aliasManager.getAlias(path.getPath(i)));
      leftJoins(path.getPath(i), buf);

      if(cmrField.getRelationMetaData().isTableMappingStyle()) {
         String relationTableAlias = 
               aliasManager.getRelationTableAlias(path.getPath(i));
         buf.append(", ");
         buf.append(cmrField.getTableName());
         buf.append(" ");
         buf.append(relationTableAlias);
      }

      declaredPaths.add(path.getPath(i));
   }

   private void leftJoins(String parentPath, BlockStringBuffer buf) {
      Set paths = (Set)leftJoinPaths.get(parentPath);
      if(subquerySupported || paths == null) {
         return;
      }
         
      for(Iterator iter = paths.iterator(); iter.hasNext(); ) {
         ASTPath path = (ASTPath)iter.next();

         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)path.getCMRField();
         JDBCEntityBridge parentEntity = cmrField.getEntity();
         String parentAlias = aliasManager.getAlias(parentPath);

         if(cmrField.getRelationMetaData().isForeignKeyMappingStyle()) {
            JDBCEntityBridge childEntity = 
                  (JDBCEntityBridge)cmrField.getRelatedEntity();
            String childAlias = aliasManager.getAlias(path.getPath());

            buf.append(" LEFT JOIN ");
            buf.append(childEntity.getTableName());
            buf.append(" ");
            buf.append(childAlias);

            buf.append(" ON ");
            buf.append(SQLUtil.getJoinClause(
                     cmrField,
                     parentAlias,
                     childAlias));
         
         } else {
   
            String relationTableAlias = 
                  aliasManager.getRelationTableAlias(path.getPath());
      
            buf.append(" LEFT JOIN ");
            buf.append(cmrField.getTableName());
            buf.append(" ");
            buf.append(relationTableAlias);

            buf.append(" ON ");
            buf.append(SQLUtil.getRelationTableJoinClause(
                     cmrField, 
                     parentAlias, 
                     relationTableAlias));

         }
      }
   }

   private void createThetaJoin(BlockStringBuffer buf) {
      Set joinedAliases = new HashSet();

      // add all the additional path tables
      for(Iterator iter = joinPaths.iterator(); iter.hasNext(); ) {
         ASTPath path = (ASTPath)iter.next();
         for(int i=0; i < path.size(); i++) {
            createThetaJoin(path, i, joinedAliases, buf);
         }
      }

      // add all the collection member path tables
      for(Iterator iter = collectionMemberJoinPaths.keySet().iterator(); 
            iter.hasNext(); ) {
         
         String childAlias = (String)iter.next();
         ASTPath path = (ASTPath)collectionMemberJoinPaths.get(childAlias);
         
         // join the memeber path
         createThetaJoin(path, path.size()-1, joinedAliases, childAlias, buf);

         // join the memeber path parents
         for(int i=0; i < path.size()-1; i++) {
            createThetaJoin(path, i, joinedAliases, buf);
         }
      }

      // get all the left joined paths 
      Set allLeftJoins = new HashSet();
      for(Iterator iter = leftJoinPaths.values().iterator(); iter.hasNext(); ) {
         allLeftJoins.addAll((Set)iter.next());
      }

      // add all parent paths for left joins
      for(Iterator iter = allLeftJoins.iterator(); iter.hasNext(); ) {
         ASTPath path = (ASTPath)iter.next();
         // don't declare the last one as the first path was left joined
         for(int i=0; i < path.size()-1; i++) {
            createThetaJoin(path, i, joinedAliases, buf);
         }
      }
   }
            
   private void createThetaJoin(
         ASTPath path,
         int i,
         Set joinedAliases,
         BlockStringBuffer buf) {

      String childAlias = aliasManager.getAlias(path.getPath(i));
      createThetaJoin(path, i, joinedAliases, childAlias, buf); 
   }

   private void createThetaJoin(
         ASTPath path,
         int i,
         Set joinedAliases,
         String childAlias,
         BlockStringBuffer buf) {

      if(!path.isCMRField(i) || joinedAliases.contains(childAlias)) {
         return;
      }

      JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)path.getCMRField(i);
      String parentAlias = aliasManager.getAlias(path.getPath(i-1));

      if(joinedAliases.size() > 0) {
         buf.append(" AND ");
      }

      if(cmrField.getRelationMetaData().isForeignKeyMappingStyle()) {
         buf.append(SQLUtil.getJoinClause(
               cmrField,
               parentAlias,
               childAlias));
      } else {
         String relationTableAlias = 
               aliasManager.getRelationTableAlias(path.getPath(i));
            
         // parent to relation table
         buf.append(SQLUtil.getRelationTableJoinClause(
               cmrField, 
               parentAlias, 
               relationTableAlias));

         buf.append(" AND ");

         // child to relation table
         buf.append(SQLUtil.getRelationTableJoinClause(
               cmrField.getRelatedCMRField(), 
               childAlias,
               relationTableAlias));
      }
      
      joinedAliases.add(childAlias);
   }


   public Object visit(ASTCollectionMemberDeclaration node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      // first arg is a collection valued path 
      ASTPath path = (ASTPath)node.jjtGetChild(0);

      // add this path to the list of declared paths
      declaredPaths.add(path.getPath());

      // get the entity at the end of this path
      JDBCEntityBridge entity = (JDBCEntityBridge)path.getEntity();

      // second arg is the identifier
      ASTIdentifier id = (ASTIdentifier)node.jjtGetChild(1);

      // get the alias
      String alias = aliasManager.getAlias(id.identifier);

      // add this path to the list of join paths so parent paths will be joined
      collectionMemberJoinPaths.put(alias, path);

      // declare the alias mapping
      aliasManager.addAlias(path.getPath(), alias);

      buf.append(entity.getTableName());
      buf.append(" ");
      buf.append(alias);
      leftJoins(path.getPath(), buf);

      // add the relation-table
      JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)path.getCMRField();
      if(cmrField.getRelationMetaData().isTableMappingStyle()) {
         String relationTableAlias = 
               aliasManager.getRelationTableAlias(path.getPath());
         buf.append(", ");
         buf.append(cmrField.getTableName());
         buf.append(" ");
         buf.append(relationTableAlias);
      }

      return buf;
   }

   public Object visit(ASTRangeVariableDeclaration node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      ASTAbstractSchema schema = (ASTAbstractSchema)node.jjtGetChild(0);
      JDBCEntityBridge entity = (JDBCEntityBridge)schema.entity;
      ASTIdentifier id = (ASTIdentifier)node.jjtGetChild(1);

      buf.append(entity.getTableName());
      buf.append(" ");
      buf.append(aliasManager.getAlias(id.identifier));
      leftJoins(id.identifier, buf);

      return buf;
   }

   public Object visit(ASTSelect node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      buf.append("SELECT ");
      if(node.distinct || returnType.equals(Set.class)) {
         buf.append("DISTINCT ");
      }

      ASTPath path = (ASTPath)node.jjtGetChild(0);
      if(path.isCMPField()) {

         // set the select object
         JDBCCMPFieldBridge selectField = 
               (JDBCCMPFieldBridge)path.getCMPField();
         setTypeFactory(selectField.getManager().getJDBCTypeFactory());
         selectObject = selectField;

         path.jjtAccept(this, buf);
      } else {
         JDBCEntityBridge selectEntity = (JDBCEntityBridge)path.getEntity();

         // set the select object
         setTypeFactory(selectEntity.getManager().getJDBCTypeFactory());
         selectObject = selectEntity;

         joinPaths.add(path);
         String alias = aliasManager.getAlias(path.getPath());

         // get a list of all fields to be loaded
         List loadFields = new ArrayList();
         loadFields.addAll(selectEntity.getPrimaryKeyFields());
         if(readAhead.isOnFind()) {
            String eagerLoadGroupName = readAhead.getEagerLoadGroup();
            loadFields.addAll(selectEntity.getLoadGroup(eagerLoadGroupName));
         }
         // get the identifier for this field
         buf.append(SQLUtil.getColumnNamesClause(loadFields, alias));
      }
      return buf;
   }

   /** Generates where clause without the "WHERE" keyword. */
   public Object visit(ASTWhere node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;
      node.jjtGetChild(0).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTNullComparison node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;
      ASTPath path = (ASTPath)node.jjtGetChild(0);

      if(path.isCMRField()) {
         JDBCCMRFieldBridge cmrField = (JDBCCMRFieldBridge)path.getCMRField();
         if(cmrField.getRelationMetaData().isTableMappingStyle()) {
            notExistsClause(path, buf);
            return buf;
         }
      }

      String alias = aliasManager.getAlias((String)path.getPath(path.size()-2));
      JDBCFieldBridge field = (JDBCFieldBridge)path.getField();
      buf.append(SQLUtil.getIsNullClause(node.not, field, alias));
      return buf;
   }

   public Object visit(ASTIsEmpty node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;
      ASTPath path = (ASTPath)node.jjtGetChild(0);

      // IS NOT EMPTY is handled automatically by the from clause
      if(node.not) {
         joinPaths.add(path);
         buf.append(typeMapping.getTrueMapping());
      } else {
         notExistsClause(path, buf);
      }
      return buf;
   }

   /** Compare entity */
   public Object visit(ASTMemberOf node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      // regular (not NOT) member of is just a simple entity compare
      if(!node.not) {
         // send the args backwards because compre expects that if a 
         // parameter is present it is the second argument.
         compareEntity(
               false,
               node.jjtGetChild(1),
               node.jjtGetChild(0),
               buf);
         return buf;
      }
            
      // setup compare to vars first, so we can compre types in from vars
      ASTPath toPath = (ASTPath)node.jjtGetChild(1);

      JDBCCMRFieldBridge toCMRField = (JDBCCMRFieldBridge)toPath.getCMRField();
      
      JDBCEntityBridge toParentEntity = 
            (JDBCEntityBridge)toPath.getEntity(toPath.size()-2);
      JDBCEntityBridge toChildEntity = (JDBCEntityBridge)toPath.getEntity();

      String toParentAlias = aliasManager.getAlias(
            (String)toPath.getPath(toPath.size()-2));
      String toChildAlias = aliasManager.getAlias(toPath.getPath());
      String relationTableAlias = null;
      if(toCMRField.getRelationMetaData().isTableMappingStyle()) {
         relationTableAlias = 
               aliasManager.getRelationTableAlias(toPath.getPath());
      }
   
      // setup from variables
      String fromAlias = null;
      int fromParamNumber = -1;
      if(node.jjtGetChild(0) instanceof ASTParameter) {
         ASTParameter fromParam = (ASTParameter)node.jjtGetChild(0);

         // can only compare like kind entities
         verifyParameterEntityType(fromParam.number, toChildEntity);
         
         fromParamNumber = fromParam.number;
      } else {
         ASTPath fromPath = (ASTPath)node.jjtGetChild(0);
            
         JDBCEntityBridge fromEntity = (JDBCEntityBridge)fromPath.getEntity();
         fromAlias = aliasManager.getAlias((String)fromPath.getPath());
               
         // can only compare like kind entities
         if(!fromEntity.equals(toChildEntity)) {
            throw new IllegalStateException("Only like types can be " +
                  "compared: from entity=" + fromEntity.getEntityName() + 
                  " to entity=" + toChildEntity.getEntityName());
         }
      }
      
      // add the path to the list of paths to left join
      Set joins = (Set)leftJoinPaths.get(toPath.getPath(toPath.size()-2));
      if(joins == null) {
         joins = new HashSet();
         leftJoinPaths.put(toPath.getPath(toPath.size()-2), joins);
      }
      joins.add(toPath);

      // first part makes toChild not in toParent.child
      if(!subquerySupported) {
         // subquery not supported; use a left join and is null
         buf.append("(");
         
         if(relationTableAlias == null) {
            buf.append(SQLUtil.getIsNullClause(
                  false, toChildEntity.getPrimaryKeyFields(), toChildAlias));
         } else {
            buf.append(SQLUtil.getIsNullClause(
                  false, toCMRField.getTableKeyFields(), relationTableAlias));
         }
      } else {
         // subquery supported; use not exists
         buf.append("NOT EXISTS (");
      
         if(relationTableAlias == null) {
            buf.append("SELECT ");
            buf.append(SQLUtil.getColumnNamesClause(
                  toChildEntity.getPrimaryKeyFields(), toChildAlias));
      
            buf.append(" FROM ");
            buf.append(toChildEntity.getTableName());
            buf.append(" ");
            buf.append(toChildAlias);

            buf.append(" WHERE ");
            buf.append(SQLUtil.getJoinClause(
                  toCMRField,
                  toParentAlias,
                  toChildAlias));
         } else {
            buf.append("SELECT ");
            buf.append(SQLUtil.getColumnNamesClause(
                  toCMRField.getRelatedCMRField().getTableKeyFields(),
                  relationTableAlias));
      
            buf.append(" FROM ");
            buf.append(toCMRField.getTableName());
            buf.append(" ");
            buf.append(relationTableAlias);

            buf.append(" WHERE ");
            buf.append(SQLUtil.getRelationTableJoinClause(
                     toCMRField, 
                     toParentAlias, 
                     relationTableAlias));
         }
      }
               
      buf.append(" AND ");

      // second part makes fromNode equal toChild
      if(fromAlias != null) {
         JDBCCMPFieldBridge toChildField;
         JDBCCMPFieldBridge fromField;

         // compre pk to pk
         if(relationTableAlias == null) {
            buf.append(SQLUtil.getSelfCompareWhereClause(
                  toChildEntity.getPrimaryKeyFields(),
                  toChildAlias,
                  fromAlias));
         } else {
            buf.append(SQLUtil.getRelationTableJoinClause(
                  toCMRField.getRelatedCMRField(),
                  fromAlias,
                  relationTableAlias));
         }
      } else {
         // add the parameters
         inputParameters.addAll(QueryParameter.createParameters(
               fromParamNumber - 1,
               toChildEntity));
      
          // compare pk to parameter
         if(relationTableAlias == null) {
            buf.append(SQLUtil.getWhereClause(
                  toChildEntity.getPrimaryKeyFields(),
                  toChildAlias));
         } else {
            buf.append(SQLUtil.getWhereClause(
                  toCMRField.getRelatedCMRField().getTableKeyFields(),
                  relationTableAlias));
         }
      }
      
      buf.append(")");
      
      return buf;
   }

   public Object visit(ASTValueClassComparison node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      boolean not = (node.opp == "<>");
      buf.append("(");
      if(not) {
         buf.append("NOT(");
      }

      // setup the from path
      ASTPath fromPath = (ASTPath)node.jjtGetChild(0);
      joinPaths.add(fromPath);
      String fromAlias = aliasManager.getAlias(
            (String)fromPath.getPath(fromPath.size()-2));
      JDBCCMPFieldBridge fromCMPField = 
            (JDBCCMPFieldBridge)fromPath.getCMPField();

      Node toNode = node.jjtGetChild(1);
      if(toNode instanceof ASTParameter) {
         ASTParameter toParam = (ASTParameter)toNode;

         // can only compare like kind entities
         Class parameterType = getParameterType(toParam.number);
         if(!(fromCMPField.getFieldType().equals(parameterType))) {
            throw new IllegalStateException("Only like types can be " +
                  "compared: from CMP field=" + fromCMPField.getFieldType() +
                  " to parameter=" + parameterType);
         }

         inputParameters.addAll(QueryParameter.createParameters(
                  toParam.number - 1,
                  fromCMPField));

         buf.append(SQLUtil.getWhereClause(fromCMPField, fromAlias));   
      } else {
         ASTPath toPath = (ASTPath)toNode;
         joinPaths.add(toPath);
         String toAlias = aliasManager.getAlias(
               (String)toPath.getPath(toPath.size()-2));
         JDBCCMPFieldBridge toCMPField = 
               (JDBCCMPFieldBridge)toPath.getCMPField();

         // can only compare like kind entities
         if(!(fromCMPField.getFieldType().equals(toCMPField.getFieldType()))) {
            throw new IllegalStateException("Only like types can be " +
                  "compared: from CMP field=" + fromCMPField.getFieldType() +
                  " to CMP field=" + toCMPField.getFieldType());
         }
 
         buf.append(SQLUtil.getSelfCompareWhereClause(
               fromCMPField,
               fromAlias, 
               toAlias));   
      }   

      if(not) {
         buf.append(")");
      }
      buf.append(")");

      return buf;
   }

   /** compreEntity(arg0, arg1) */
   public Object visit(ASTEntityComparison node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;
      Node arg0 = node.jjtGetChild(0);
      Node arg1 = node.jjtGetChild(1);
      if(node.opp == "<>") {
         compareEntity(true, arg0, arg1, buf);
      } else {
         compareEntity(false, arg0, arg1, buf);
      }
      return buf;
   }

   /** Type-mapping function translation */
   public Object visit(ASTConcat node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      JDBCFunctionMappingMetaData function =
            typeMapping.getFunctionMapping("concat");
      String[] args = new String[] {
         node.jjtGetChild(0).jjtAccept(
               this, new BlockStringBuffer()).toString(),
         node.jjtGetChild(1).jjtAccept(
               this, new BlockStringBuffer()).toString(),
      }; 
      buf.append(function.getFunctionSql(args));

      return buf;
   }

   /** Type-mapping function translation */
   public Object visit(ASTSubstring node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      JDBCFunctionMappingMetaData function =
            typeMapping.getFunctionMapping("substring");
      String[] args = new String[] {
         node.jjtGetChild(0).jjtAccept(
               this, new BlockStringBuffer()).toString(),
         node.jjtGetChild(1).jjtAccept(
               this, new BlockStringBuffer()).toString(),
         node.jjtGetChild(2).jjtAccept(
               this, new BlockStringBuffer()).toString()
      }; 
      buf.append(function.getFunctionSql(args));

      return buf;
   }

   /** Type-mapping function translation */
   public Object visit(ASTLCase node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      JDBCFunctionMappingMetaData function =
            typeMapping.getFunctionMapping("lcase");
      String[] args = new String[] {
         node.jjtGetChild(0).jjtAccept(
               this, new BlockStringBuffer()).toString(),
      }; 
      buf.append(function.getFunctionSql(args));

      return buf;
   }

   /** Type-mapping function translation */
   public Object visit(ASTUCase node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      JDBCFunctionMappingMetaData function =
            typeMapping.getFunctionMapping("ucase");
      String[] args = new String[] {
         node.jjtGetChild(0).jjtAccept(
               this, new BlockStringBuffer()).toString(),
      }; 
      buf.append(function.getFunctionSql(args));

      return buf;
   }

   /** Type-mapping function translation */
   public Object visit(ASTLength node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      JDBCFunctionMappingMetaData function =
            typeMapping.getFunctionMapping("length");
      String[] args = new String[] {
         node.jjtGetChild(0).jjtAccept(
               this, new BlockStringBuffer()).toString()
      }; 
      buf.append(function.getFunctionSql(args));

      return buf;
   }

   /** Type-mapping function translation */
   public Object visit(ASTLocate node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      JDBCFunctionMappingMetaData function =
            typeMapping.getFunctionMapping("locate");
      String[] args = new String[3];
      args[0] = node.jjtGetChild(0).jjtAccept(
            this, new BlockStringBuffer()).toString();
      args[1] = node.jjtGetChild(1).jjtAccept(
            this, new BlockStringBuffer()).toString();
      if(node.jjtGetNumChildren()==3) {
         args[2] = node.jjtGetChild(2).jjtAccept(
               this, new BlockStringBuffer()).toString();
      } else {
         args[2] = "1";
      }
      buf.append(function.getFunctionSql(args));

      return buf;
   }

   /** Type-mapping function translation */
   public Object visit(ASTAbs node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      JDBCFunctionMappingMetaData function =
            typeMapping.getFunctionMapping("abs");
      String[] args = new String[] {
         node.jjtGetChild(0).jjtAccept(
               this, new BlockStringBuffer()).toString()
      }; 
      buf.append(function.getFunctionSql(args));

      return buf;
   }

   /** Type-mapping function translation */
   public Object visit(ASTSqrt node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;

      JDBCFunctionMappingMetaData function =
            typeMapping.getFunctionMapping("sqrt");
      String[] args = new String[] {
         node.jjtGetChild(0).jjtAccept(
               this, new BlockStringBuffer()).toString()
      }; 
      buf.append(function.getFunctionSql(args));

      return buf;
   }

   /** tableAlias.columnName */
   public Object visit(ASTPath node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;
      if(!node.isCMPField()) {
         throw new IllegalStateException("Can only visit cmp valued path " +
               "node. Should have been handled at a higher level.");
      }

      // make sure this is mapped to a single column
      switch(node.type) {
         case EJBQLTypes.ENTITY_TYPE:
         case EJBQLTypes.VALUE_CLASS_TYPE:
         case EJBQLTypes.UNKNOWN_TYPE:
            throw new IllegalStateException("Can not visit multi-column path " +
                  "node. Should have been handled at a higher level.");
      }
 
      joinPaths.add(node);
      JDBCCMPFieldBridge cmpField = (JDBCCMPFieldBridge)node.getCMPField();
      String alias = aliasManager.getAlias((String)node.getPath(node.size()-2));
      buf.append(SQLUtil.getColumnNamesClause(cmpField, alias));
      return buf;
   }

   public Object visit(ASTAbstractSchema node, Object data) {
      throw new IllegalStateException("Can not visit abstract schema node. " +
            "Should have been handled at a higher level.");
   }

   /** ? */
   public Object visit(ASTParameter node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;
      Class type = getParameterType(node.number);

      // make sure this is mapped to a single column
      int ejbqlType = EJBQLTypes.getEJBQLType(type);
      if(ejbqlType == EJBQLTypes.ENTITY_TYPE ||
            ejbqlType == EJBQLTypes.VALUE_CLASS_TYPE ||
            ejbqlType == EJBQLTypes.UNKNOWN_TYPE) {
         throw new IllegalStateException("Can not visit multi-column " +
               "parameter node. Should have been handled at a higher level.");
      }
      
      QueryParameter param = new QueryParameter(
               node.number - 1,
               false, // isPrimaryKeyParameter
               null, // field
               null, // parameter
               typeFactory.getJDBCTypeForJavaType(type));
      inputParameters.add(param);
      buf.append("?");
      return buf;
   }

   /** typeMapping.get<True/False>Mapping() */
   public Object visit(ASTBooleanLiteral node, Object data) {
      BlockStringBuffer buf = (BlockStringBuffer)data;
      if(node.value) {
         buf.append(typeMapping.getTrueMapping());
      } else {
         buf.append(typeMapping.getFalseMapping());
      }
      return data;
   }
}
