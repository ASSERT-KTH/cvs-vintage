/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

import org.jboss.ejb.plugins.cmp.ejbql.*;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractEntityBridge;
import org.jboss.ejb.plugins.cmp.jdbc.bridge.JDBCAbstractCMRFieldBridge;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCReadAheadMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCTypeMappingMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCRelationMetaData;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCFunctionMappingMetaData;
import org.jboss.ejb.EntityPersistenceStore;
import org.jboss.logging.Logger;

/**
 * Compiles EJB-QL and JBossQL into SQL using OUTER and INNER joins.
 *
 * @author <a href="mailto:alex@jboss.org">Alex Loubyansky</a>
 * @version $Revision: 1.12 $
 */
public final class EJBQLToSQL92Compiler
   implements QLCompiler, JBossQLParserVisitor
{
   private static final Logger log = Logger.getLogger(EJBQLToSQL92Compiler.class);

   // input objects
   private final Catalog catalog;
   private Class returnType;
   private Class[] parameterTypes;
   private JDBCReadAheadMetaData readAhead;

   // alias info
   private AliasManager aliasManager;
   private Map joinPaths = new HashMap();
   private Map identifierToTable = new HashMap();
   private Set joinedAliases = new HashSet();

   // mapping metadata
   private JDBCTypeMappingMetaData typeMapping;
   private JDBCTypeFactory typeFactory;

   // output objects
   private boolean forceDistinct;
   private String sql;
   private int offsetParam;
   private int offsetValue;
   private int limitParam;
   private int limitValue;
   private JDBCEntityPersistenceStore selectManager;
   private Object selectObject;
   private List inputParameters = new ArrayList();

   private List leftJoinCMRList = new ArrayList();
   private StringBuffer onFindCMRJoin;

   private boolean countCompositePk;

   public EJBQLToSQL92Compiler(Catalog catalog)
   {
      this.catalog = catalog;
   }

   public void compileEJBQL(String ejbql, Class returnType, Class[] parameterTypes, JDBCReadAheadMetaData readAhead)
      throws Exception
   {
      // reset all state variables
      reset();

      // set input arguemts
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
      this.readAhead = readAhead;

      // get the parser
      EJBQLParser parser = new EJBQLParser(new StringReader(""));

      try
      {
         // parse the ejbql into an abstract sytax tree
         ASTEJBQL ejbqlNode = parser.parse(catalog, parameterTypes, ejbql);

         // translate to sql
         sql = ejbqlNode.jjtAccept(this, new StringBuffer()).toString();
      }
      catch(Exception e)
      {
         // if there is a problem reset the state before exiting
         reset();
         throw e;
      }
      catch(Error e)
      {
         // lame javacc lexer throws Errors
         reset();
         throw e;
      }
   }

   public void compileJBossQL(String ejbql, Class returnType, Class[] parameterTypes, JDBCReadAheadMetaData readAhead)
      throws Exception
   {
      // reset all state variables
      reset();

      // set input arguemts
      this.returnType = returnType;
      this.parameterTypes = parameterTypes;
      this.readAhead = readAhead;

      // get the parser
      JBossQLParser parser = new JBossQLParser(new StringReader(""));

      try
      {
         // parse the ejbql into an abstract sytax tree
         ASTEJBQL ejbqlNode = parser.parse(catalog, parameterTypes, ejbql);

         // translate to sql
         sql = ejbqlNode.jjtAccept(this, new StringBuffer()).toString();

         if(log.isTraceEnabled())
         {
            log.trace("ejbql: " + ejbql);
            log.trace("sql: " + sql);
         }
      }
      catch(Exception e)
      {
         // if there is a problem reset the state before exiting
         reset();
         throw e;
      }
      catch(Error e)
      {
         // lame javacc lexer throws Errors
         reset();
         throw e;
      }
   }

   public String getSQL()
   {
      return sql;
   }

   public int getOffsetValue()
   {
      return offsetValue;
   }

   public int getOffsetParam()
   {
      return offsetParam;
   }

   public int getLimitValue()
   {
      return limitValue;
   }

   public int getLimitParam()
   {
      return limitParam;
   }

   public boolean isSelectEntity()
   {
      return selectObject instanceof JDBCAbstractEntityBridge;
   }

   public JDBCAbstractEntityBridge getSelectEntity()
   {
      return (JDBCAbstractEntityBridge) selectObject;
   }

   public boolean isSelectField()
   {
      boolean result;
      if(selectObject instanceof JDBCFieldBridge)
      {
         JDBCFieldBridge field = (JDBCFieldBridge) selectObject;
         result = field.isCMPField();
      }
      else
      {
         result = false;
      }
      return result;
   }

   public JDBCFieldBridge getSelectField()
   {
      return (JDBCFieldBridge) selectObject;
   }

   public SelectFunction getSelectFunction()
   {
      return (SelectFunction) selectObject;
   }

   public EntityPersistenceStore getStoreManager()
   {
      return selectManager;
   }

   public List getInputParameters()
   {
      return inputParameters;
   }

   public List getLeftJoinCMRList()
   {
      return leftJoinCMRList;
   }

   public Object visit(SimpleNode node, Object data)
   {
      throw new RuntimeException("Internal error: Found unknown node type in " +
         "EJB-QL abstract syntax tree: node=" + node);
   }

   public Object visit(ASTEJBQL node, Object data)
   {
      Node selectNode = node.jjtGetChild(0);
      Node fromNode = node.jjtGetChild(1);

      // compile selectNode
      StringBuffer selectClause = new StringBuffer(50);
      selectNode.jjtAccept(this, selectClause);

      StringBuffer whereClause = null;
      StringBuffer orderByClause = null;
      for(int i = 2; i < node.jjtGetNumChildren(); ++i)
      {
         Node childNode = node.jjtGetChild(i);
         if(childNode instanceof ASTWhere)
         {
            whereClause = new StringBuffer(20);
            childNode.jjtAccept(this, whereClause);
         }
         else if(childNode instanceof ASTOrderBy)
         {
            orderByClause = new StringBuffer();
            childNode.jjtAccept(this, orderByClause);
         }
         else if(childNode instanceof ASTLimitOffset)
         {
            childNode.jjtAccept(this, null);
         }
      }

      // compile fromNode
      StringBuffer fromClause = new StringBuffer(30);
      fromNode.jjtAccept(this, fromClause);

      // left-join
      for(Iterator iter = identifierToTable.entrySet().iterator(); iter.hasNext();)
      {
         final Map.Entry entry = (Map.Entry) iter.next();
         final String identifier = (String) entry.getKey();
         final String table = (String) entry.getValue();
         final String alias = aliasManager.getAlias(identifier);

         fromClause.append(table).append(' ').append(alias);
         join(alias, fromClause);

         if(iter.hasNext())
         {
            fromClause.append(SQLUtil.COMMA);
         }
      }

      // assemble sql
      StringBuffer sql = (StringBuffer) data;
      if(selectManager.getMetaData().hasRowLocking() && !(selectObject instanceof SelectFunction))
      {
         JDBCFunctionMappingMetaData rowLockingTemplate = typeMapping.getRowLockingTemplate();
         if(rowLockingTemplate == null)
         {
            throw new IllegalStateException("Row locking template is not defined for given mapping: " + typeMapping.getName());
         }

         boolean distinct = ((ASTSelect) selectNode).distinct || returnType == Set.class || forceDistinct;

         Object args[] = new Object[]{
            distinct ? SQLUtil.DISTINCT + selectClause : selectClause.toString(),
            fromClause,
            whereClause == null || whereClause.length() == 0 ? null : whereClause,
            orderByClause == null || orderByClause.length() == 0 ? null : orderByClause
         };
         rowLockingTemplate.getFunctionSql(args, sql);
      }
      else
      {
         sql.append(SQLUtil.SELECT);
         if(((ASTSelect) selectNode).distinct || returnType == Set.class || forceDistinct)
         {
            sql.append(SQLUtil.DISTINCT);
         }
         sql.append(selectClause)
            .append(SQLUtil.FROM)
            .append(fromClause);

         if(whereClause != null && whereClause.length() > 0)
         {
            sql.append(SQLUtil.WHERE).append(whereClause);
         }

         if(orderByClause != null && orderByClause.length() > 0)
         {
            sql.append(SQLUtil.ORDERBY).append(orderByClause);
         }
      }

      if(countCompositePk)
      {
         sql.insert(0, "SELECT COUNT(*) FROM (").append(") t_count");
      }

      return data;
   }

   public Object visit(ASTOrderBy node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < node.jjtGetNumChildren(); i++)
      {
         buf.append(SQLUtil.COMMA);
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTOrderByPath node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      if(node.ascending)
      {
         buf.append(SQLUtil.ASC);
      }
      else
      {
         buf.append(SQLUtil.DESC);
      }
      return data;
   }

   public Object visit(ASTLimitOffset node, Object data)
   {
      int child = 0;
      if(node.hasOffset)
      {
         Node offsetNode = node.jjtGetChild(child++);
         if(offsetNode instanceof ASTParameter)
         {
            ASTParameter param = (ASTParameter) offsetNode;
            Class parameterType = getParameterType(param.number);
            if(int.class != parameterType && Integer.class != parameterType)
            {
               throw new IllegalStateException("OFFSET parameter must be an int");
            }
            offsetParam = param.number;
         }
         else
         {
            ASTExactNumericLiteral param = (ASTExactNumericLiteral) offsetNode;
            offsetValue = (int) param.value;
         }
      }

      if(node.hasLimit)
      {
         Node limitNode = node.jjtGetChild(child);
         if(limitNode instanceof ASTParameter)
         {
            ASTParameter param = (ASTParameter) limitNode;
            Class parameterType = getParameterType(param.number);
            if(int.class != parameterType && Integer.class != parameterType)
            {
               throw new IllegalStateException("LIMIT parameter must be an int");
            }
            limitParam = param.number;
         }
         else
         {
            ASTExactNumericLiteral param = (ASTExactNumericLiteral) limitNode;
            limitValue = (int) param.value;
         }
      }
      return data;
   }

   public Object visit(ASTSelect select, Object data)
   {
      StringBuffer sql = (StringBuffer) data;

      final Node child0 = select.jjtGetChild(0);
      final ASTPath path;
      if(child0 instanceof ASTPath)
      {
         path = (ASTPath) child0;

         if(path.isCMPField())
         {
            // set the select object
            JDBCFieldBridge selectField = (JDBCFieldBridge) path.getCMPField();
            selectManager = selectField.getManager();
            selectObject = selectField;
            setTypeFactory(selectManager.getJDBCTypeFactory());

            // todo inner or left?
            //addLeftJoinPath(path);
            addInnerJoinPath(path);

            String alias = aliasManager.getAlias(path.getPath(path.size() - 2));
            SQLUtil.getColumnNamesClause(selectField, alias, sql);
         }
         else
         {
            JDBCAbstractEntityBridge selectEntity = (JDBCAbstractEntityBridge) path.getEntity();
            selectManager = selectEntity.getManager();
            selectObject = selectEntity;
            setTypeFactory(selectEntity.getManager().getJDBCTypeFactory());

            final String alias = aliasManager.getAlias(path.getPath());
            SQLUtil.getColumnNamesClause(selectEntity.getTableFields(),
               alias,
               sql);

            /*
            if(readAhead.isOnFind())
            {
               String eagerLoadGroupName = readAhead.getEagerLoadGroup();
               boolean[] loadGroupMask = selectEntity.getLoadGroupMask(eagerLoadGroupName);
               SQLUtil.appendColumnNamesClause(
                  selectEntity.getTableFields(),
                  loadGroupMask,
                  alias,
                  sql
               );
            }
            */

            addLeftJoinPath(path);
         }
      }
      else
      {
         // the function should take a path expresion as a parameter
         path = getPathFromChildren(child0);

         if(path == null)
         {
            throw new IllegalStateException("The function in SELECT clause does not contain a path expression.");
         }

         if(path.isCMPField())
         {
            JDBCFieldBridge selectField = (JDBCFieldBridge) path.getCMPField();
            selectManager = selectField.getManager();
            setTypeFactory(selectManager.getJDBCTypeFactory());
         }
         else if(path.isCMRField())
         {
            JDBCFieldBridge cmrField = (JDBCFieldBridge) path.getCMRField();
            selectManager = cmrField.getManager();
            setTypeFactory(selectManager.getJDBCTypeFactory());
            addLeftJoinPath(path);
         }
         else
         {
            final JDBCAbstractEntityBridge entity = (JDBCAbstractEntityBridge) path.getEntity();
            selectManager = entity.getManager();
            setTypeFactory(selectManager.getJDBCTypeFactory());
            addLeftJoinPath(path);
         }

         selectObject = child0;
         child0.jjtAccept(this, data);
      }

      return data;
   }

   public Object visit(ASTWhere node, Object data)
   {
      node.jjtGetChild(0).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTOr node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < node.jjtGetNumChildren(); ++i)
      {
         buf.append(SQLUtil.OR);
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTWhereConditionalTerm node, Object data)
   {
      for(int i = 0; i < node.jjtGetNumChildren(); ++i)
      {
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTAnd node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < node.jjtGetNumChildren(); i++)
      {
         buf.append(SQLUtil.AND);
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTNot node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append(SQLUtil.NOT);
      node.jjtGetChild(0).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTConditionalParenthetical node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append('(');
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(')');
      return data;
   }

   public Object visit(ASTBetween node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      if(node.not)
      {
         buf.append(SQLUtil.NOT);
      }
      buf.append(SQLUtil.BETWEEN);
      node.jjtGetChild(1).jjtAccept(this, data);
      buf.append(SQLUtil.AND);
      node.jjtGetChild(2).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTIn node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      if(node.not)
      {
         buf.append(SQLUtil.NOT);
      }
      buf.append(SQLUtil.IN).append('(');
      node.jjtGetChild(1).jjtAccept(this, data);
      for(int i = 2; i < node.jjtGetNumChildren(); i++)
      {
         buf.append(SQLUtil.COMMA);
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      buf.append(')');
      return data;
   }

   public Object visit(ASTLike node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      if(node.not)
      {
         buf.append(SQLUtil.NOT);
      }
      buf.append(SQLUtil.LIKE);
      node.jjtGetChild(1).jjtAccept(this, data);
      if(node.jjtGetNumChildren() == 3)
      {
         buf.append(SQLUtil.ESCAPE);
         node.jjtGetChild(2).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTNullComparison node, Object data)
   {
      StringBuffer sql = (StringBuffer) data;

      final Node child0 = node.jjtGetChild(0);
      if(child0 instanceof ASTPath)
      {
         ASTPath path = (ASTPath) child0;
         addLeftJoinPath(path);

         JDBCFieldBridge field = (JDBCFieldBridge) path.getField();

         if(field.getJDBCType() == null)
         {
            JDBCAbstractCMRFieldBridge cmrField = (JDBCAbstractCMRFieldBridge) field;
            final JDBCFieldBridge[] keyFields;
            if(cmrField.getMetaData().getRelationMetaData().isTableMappingStyle())
            {
               keyFields = cmrField.getRelatedCMRField().getEntity().getPrimaryKeyFields();
            }
            else
            {
               keyFields = (
                  cmrField.hasForeignKey()
                  ? cmrField.getForeignKeyFields()
                  : cmrField.getRelatedCMRField().getForeignKeyFields()
                  );
            }

            String alias = aliasManager.getAlias(path.getPath());
            SQLUtil.getIsNullClause(node.not, keyFields, alias, sql);
         }
         else
         {
            String alias = aliasManager.getAlias(path.getPath(path.size() - 2));
            SQLUtil.getIsNullClause(node.not, field, alias, sql);
         }
      }
      else if(child0 instanceof ASTParameter)
      {
         ASTParameter param = (ASTParameter) child0;
         Class type = getParameterType(param.number);

         QueryParameter queryParam = new QueryParameter(param.number - 1,
            false, // isPrimaryKeyParameter
            null, // field
            null, // parameter
            typeFactory.getJDBCTypeForJavaType(type));
         inputParameters.add(queryParam);

         sql.append("? IS ");
         if(node.not)
         {
            sql.append(SQLUtil.NOT);
         }
         sql.append(SQLUtil.NULL);
      }
      else
      {
         throw new IllegalStateException("Unexpected node in IS NULL clause: " + node);
      }

      return data;
   }

   public Object visit(ASTIsEmpty node, Object data)
   {
      ASTPath path = (ASTPath) node.jjtGetChild(0);
      if(!path.isCMRField())
      {
         throw new IllegalStateException("IS EMPTY can be applied only to collection valued CMR field.");
      }

      addLeftJoinPath(path);

      StringBuffer sql = (StringBuffer) data;
      JDBCAbstractCMRFieldBridge cmrField = (JDBCAbstractCMRFieldBridge) path.getCMRField();
      JDBCAbstractEntityBridge relatedEntity = (JDBCAbstractEntityBridge) cmrField.getRelatedEntity();
      String alias = aliasManager.getAlias(path.getPath());
      SQLUtil.getIsNullClause(node.not, relatedEntity.getPrimaryKeyFields(), alias, sql);

      return data;
   }

   public Object visit(ASTMemberOf node, Object data)
   {
      Node member = node.jjtGetChild(0);
      ASTPath colPath = (ASTPath) node.jjtGetChild(1);
      String colAlias = aliasManager.getAlias(colPath.getPath());
      JDBCAbstractEntityBridge colEntity = (JDBCAbstractEntityBridge) colPath.getEntity();

      StringBuffer sql = (StringBuffer) data;

      if(node.not)
      {
         sql.append(SQLUtil.NOT);
      }

      sql.append(SQLUtil.EXISTS).append('(').append(SQLUtil.SELECT);

      if(member instanceof ASTParameter)
      {
         ASTParameter toParam = (ASTParameter) member;
         verifyParameterEntityType(toParam.number, colEntity);
         inputParameters.addAll(QueryParameter.createParameters(toParam.number - 1, colEntity));

         String parentAlias = aliasManager.getAlias(colPath.getPath(0));
         String localParentAlias = parentAlias + "_local";
         JDBCAbstractEntityBridge parentEntity = (JDBCAbstractEntityBridge) colPath.getEntity(0);
         SQLUtil.getColumnNamesClause(parentEntity.getPrimaryKeyFields(), localParentAlias, sql);
         sql.append(SQLUtil.FROM)
            .append(parentEntity.getTableName()).append(' ').append(localParentAlias);
         innerJoinPath(colPath, sql);

         sql.append(SQLUtil.WHERE);

         JDBCAbstractEntityBridge col0 = (JDBCAbstractEntityBridge)colPath.getEntity(0);
         SQLUtil.getSelfCompareWhereClause(col0.getPrimaryKeyFields(), parentAlias, localParentAlias, sql);
         sql.append(SQLUtil.AND);
         SQLUtil.getWhereClause(colEntity.getPrimaryKeyFields(), colAlias + "_local", sql);
      }
      else
      {
         ASTPath memberPath = (ASTPath) member;
         JDBCAbstractEntityBridge memberEntity = (JDBCAbstractEntityBridge) memberPath.getEntity();

         if(!memberEntity.equals(colEntity))
         {
            throw new IllegalStateException("Member must be if the same type as the collection, got: member="
               +
               memberEntity.getEntityName()
               + ", collection=" + colEntity.getEntityName());
         }

         String memberAlias = aliasManager.getAlias(memberPath.getPath());

         if(memberPath.size() > 1)
         {
            String parentAlias = aliasManager.getAlias(memberPath.getPath(0)) + "_local";
            JDBCAbstractEntityBridge parentEntity = (JDBCAbstractEntityBridge) memberPath.getEntity(0);
            SQLUtil.getColumnNamesClause(parentEntity.getPrimaryKeyFields(), parentAlias, sql);
            sql.append(SQLUtil.FROM)
               .append(parentEntity.getTableName()).append(' ').append(parentAlias);
            innerJoinPath(memberPath, sql);
            innerJoinPath(colPath, sql);
         }
         else if(colPath.size() > 1)
         {
            String parentAlias = aliasManager.getAlias(colPath.getPath(0)) + "_local";
            JDBCAbstractEntityBridge parentEntity = (JDBCAbstractEntityBridge) colPath.getEntity(0);
            SQLUtil.getColumnNamesClause(parentEntity.getPrimaryKeyFields(), parentAlias, sql);
            sql.append(SQLUtil.FROM)
               .append(parentEntity.getTableName()).append(' ').append(parentAlias);
            innerJoinPath(colPath, sql);
         }
         else
         {
            throw new IllegalStateException(
               "There should be collection valued path expression, not identification variable.");
         }

         sql.append(SQLUtil.WHERE);

         JDBCAbstractEntityBridge member0 = (JDBCAbstractEntityBridge)memberPath.getEntity(0);
         String member0Alias = aliasManager.getAlias(memberPath.getPath(0));
         if(memberPath.size() > 1)
         {
            SQLUtil.getSelfCompareWhereClause(colEntity.getPrimaryKeyFields(),
               memberAlias + "_local",
               colAlias + "_local",
               sql);

            sql.append(SQLUtil.AND);
            SQLUtil.getSelfCompareWhereClause(member0.getPrimaryKeyFields(),
               member0Alias,
               member0Alias + "_local",
               sql);
         }
         else
         {
            SQLUtil.getSelfCompareWhereClause(member0.getPrimaryKeyFields(), memberAlias, colAlias + "_local", sql);
         }
      }

      sql.append(')');

      return data;
   }

   private void innerJoinPath(ASTPath path, StringBuffer sql)
   {
      if(path.size() < 2)
      {
         return;
      }

      String parentAlias = aliasManager.getAlias(path.getPath(0)) + "_local";
      String leftAlias = parentAlias;
      for(int i = 1; i < path.size(); ++i)
      {
         String curPath = path.getPath(i);
         final String joinAlias = aliasManager.getAlias(curPath) + "_local";

         final JDBCAbstractCMRFieldBridge cmrField = (JDBCAbstractCMRFieldBridge) path.getCMRField(i);
         final JDBCAbstractEntityBridge joinEntity = (JDBCAbstractEntityBridge) cmrField.getRelatedEntity();

         JDBCRelationMetaData relation = cmrField.getMetaData().getRelationMetaData();

         String join = " INNER JOIN ";

         if(relation.isTableMappingStyle())
         {
            String relTableAlias = aliasManager.getRelationTableAlias(curPath) + "_local";
            sql.append(join)
               .append(cmrField.getTableName())
               .append(' ')
               .append(relTableAlias)
               .append(" ON ");
            SQLUtil.getRelationTableJoinClause(cmrField, leftAlias, relTableAlias, sql);

            sql.append(join)
               .append(joinEntity.getTableName())
               .append(' ')
               .append(joinAlias)
               .append(" ON ");
            SQLUtil.getRelationTableJoinClause(cmrField.getRelatedCMRField(), joinAlias, relTableAlias, sql);
         }
         else
         {
            sql.append(join)
               .append(joinEntity.getTableName())
               .append(' ')
               .append(joinAlias)
               .append(" ON ");

            SQLUtil.getJoinClause(cmrField, leftAlias, joinAlias, sql);
         }

         leftAlias = joinAlias;
      }
   }

   public Object visit(ASTStringComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(' ').append(node.opp).append(' ');
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTBooleanComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      if(node.jjtGetNumChildren() == 2)
      {
         buf.append(' ').append(node.opp).append(' ');
         node.jjtGetChild(1).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTDatetimeComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(' ').append(node.opp).append(' ');
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTValueClassComparison node, Object data)
   {
      throw new IllegalStateException("Value class comparison is not yet supported.");
   }

   public Object visit(ASTEntityComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      Node arg0 = node.jjtGetChild(0);
      Node arg1 = node.jjtGetChild(1);
      if(node.opp.equals(SQLUtil.NOT_EQUAL))
      {
         compareEntity(true, arg0, arg1, buf);
      }
      else
      {
         compareEntity(false, arg0, arg1, buf);
      }
      return data;
   }

   public Object visit(ASTArithmeticComparison node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(' ').append(node.opp).append(' ');
      node.jjtGetChild(1).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTPlusMinus node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < node.jjtGetNumChildren(); i++)
      {
         buf.append(' ').append(node.opps.get(i - 1)).append(' ');
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTMultDiv node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < node.jjtGetNumChildren(); i++)
      {
         buf.append(' ').append(node.opps.get(i - 1)).append(' ');
         node.jjtGetChild(i).jjtAccept(this, data);
      }
      return data;
   }

   public Object visit(ASTNegation node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append('-');
      node.jjtGetChild(0).jjtAccept(this, data);
      return data;
   }

   public Object visit(ASTArithmeticParenthetical node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append('(');
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(')');
      return data;
   }

   public Object visit(ASTStringParenthetical node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append('(');
      node.jjtGetChild(0).jjtAccept(this, data);
      buf.append(')');
      return data;
   }

   public Object visit(ASTConcat node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.CONCAT);
      Object[] args = childrenToStringArr(2, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTSubstring node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.SUBSTRING);
      Object[] args = childrenToStringArr(3, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTUCase node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.UCASE);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTLCase node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.LCASE);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTLength node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.LENGTH);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTLocate node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.LOCATE);
      Object[] args = new Object[3];
      args[0] = node.jjtGetChild(0).jjtAccept(this, new StringBuffer()).toString();
      args[1] = node.jjtGetChild(1).jjtAccept(this, new StringBuffer()).toString();
      if(node.jjtGetNumChildren() == 3)
      {
         args[2] = node.jjtGetChild(2).jjtAccept(this, new StringBuffer()).toString();
      }
      else
      {
         args[2] = "1";
      }
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTAbs node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.ABS);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTSqrt node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      JDBCFunctionMappingMetaData function = typeMapping.getFunctionMapping(JDBCTypeMappingMetaData.SQRT);
      Object[] args = childrenToStringArr(1, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTMod node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      JDBCFunctionMappingMetaData function = JDBCTypeMappingMetaData.MOD_FUNC;
      Object[] args = childrenToStringArr(2, node);
      function.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTAvg node, Object data)
   {
      node.setResultType(returnType);
      StringBuffer buf = (StringBuffer) data;
      Object[] args = new Object[]{
         node.distinct,
         node.jjtGetChild(0).jjtAccept(this, new StringBuffer()).toString(),
      };
      JDBCTypeMappingMetaData.AVG_FUNC.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTMax node, Object data)
   {
      node.setResultType(returnType);
      StringBuffer buf = (StringBuffer) data;
      Object[] args = new Object[]{
         node.distinct,
         node.jjtGetChild(0).jjtAccept(this, new StringBuffer()).toString(),
      };
      JDBCTypeMappingMetaData.MAX_FUNC.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTMin node, Object data)
   {
      node.setResultType(returnType);
      StringBuffer buf = (StringBuffer) data;
      Object[] args = new Object[]{
         node.distinct,
         node.jjtGetChild(0).jjtAccept(this, new StringBuffer()).toString(),
      };
      JDBCTypeMappingMetaData.MIN_FUNC.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTSum node, Object data)
   {
      node.setResultType(returnType);
      StringBuffer buf = (StringBuffer) data;
      Object[] args = new Object[]{
         node.distinct,
         node.jjtGetChild(0).jjtAccept(this, new StringBuffer()).toString(),
      };
      JDBCTypeMappingMetaData.SUM_FUNC.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTCount node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      node.setResultType(returnType);

      Object args[];
      final ASTPath cntPath = (ASTPath) node.jjtGetChild(0);
      if(cntPath.isCMPField())
      {
         args = new Object[]{node.distinct, node.jjtGetChild(0).jjtAccept(this, new StringBuffer()).toString()};
      }
      else
      {
         JDBCAbstractEntityBridge entity = (JDBCAbstractEntityBridge) cntPath.getEntity();
         final JDBCFieldBridge[] pkFields = entity.getPrimaryKeyFields();
         if(pkFields.length > 1)
         {
            countCompositePk = true;
            forceDistinct = node.distinct.length() > 0;

            addLeftJoinPath(cntPath);

            String alias = aliasManager.getAlias(cntPath.getPath());
            SQLUtil.getColumnNamesClause(entity.getPrimaryKeyFields(),
               alias,
               buf);

            return buf;
         }
         else
         {
            final String alias = aliasManager.getAlias(cntPath.getPath());
            StringBuffer keyColumn = new StringBuffer(20);
            SQLUtil.getColumnNamesClause(pkFields[0], alias, keyColumn);
            args = new Object[]{node.distinct, keyColumn.toString()};
         }
      }

      JDBCTypeMappingMetaData.COUNT_FUNC.getFunctionSql(args, buf);
      return data;
   }

   public Object visit(ASTPath node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      if(!node.isCMPField())
      {
         throw new IllegalStateException("Can only visit cmp valued path node. "
            + "Should have been handled at a higher level.");
      }

      // make sure this is mapped to a single column
      switch(node.type)
      {
         case EJBQLTypes.ENTITY_TYPE:
         case EJBQLTypes.VALUE_CLASS_TYPE:
         case EJBQLTypes.UNKNOWN_TYPE:
            throw new IllegalStateException("Can not visit multi-column path " +
               "node. Should have been handled at a higher level.");
      }

      addLeftJoinPath(node);
      JDBCFieldBridge cmpField = (JDBCFieldBridge) node.getCMPField();
      String alias = aliasManager.getAlias(node.getPath(node.size() - 2));
      SQLUtil.getColumnNamesClause(cmpField, alias, buf);
      return data;
   }

   public Object visit(ASTAbstractSchema node, Object data)
   {
      throw new IllegalStateException("Can not visit abstract schema node. "
         + " Should have been handled at a higher level.");
   }

   public Object visit(ASTIdentifier node, Object data)
   {
      throw new UnsupportedOperationException("Must not visit ASTIdentifier noe.");
   }

   public Object visit(ASTParameter node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      Class type = getParameterType(node.number);

      // make sure this is mapped to a single column
      int ejbqlType = EJBQLTypes.getEJBQLType(type);
      if(ejbqlType == EJBQLTypes.ENTITY_TYPE
         ||
         ejbqlType == EJBQLTypes.VALUE_CLASS_TYPE ||
         ejbqlType == EJBQLTypes.UNKNOWN_TYPE)
      {
         throw new IllegalStateException("Can not visit multi-column " +
            "parameter node. Should have been handled at a higher level.");
      }

      QueryParameter param = new QueryParameter(node.number - 1,
         false, // isPrimaryKeyParameter
         null, // field
         null, // parameter
         typeFactory.getJDBCTypeForJavaType(type));
      inputParameters.add(param);
      buf.append('?');

      return data;
   }

   public Object visit(ASTExactNumericLiteral node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append(node.literal);
      return data;
   }

   public Object visit(ASTApproximateNumericLiteral node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append(node.literal);
      return data;
   }

   public Object visit(ASTStringLiteral node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      buf.append(node.value);
      return data;
   }

   public Object visit(ASTBooleanLiteral node, Object data)
   {
      StringBuffer buf = (StringBuffer) data;
      if(node.value)
      {
         buf.append(typeMapping.getTrueMapping());
      }
      else
      {
         buf.append(typeMapping.getFalseMapping());
      }
      return data;
   }

   public Object visit(ASTFrom from, Object data)
   {
      StringBuffer sql = (StringBuffer) data;
      from.jjtGetChild(0).jjtAccept(this, data);
      for(int i = 1; i < from.jjtGetNumChildren(); ++i)
      {
         from.jjtGetChild(i).jjtAccept(this, data);
      }

      return data;
   }

   public Object visit(ASTCollectionMemberDeclaration node, Object data)
   {
      ASTPath path = (ASTPath) node.jjtGetChild(0);

      // assign the same alias for path and identifier
      ASTIdentifier id = (ASTIdentifier) node.jjtGetChild(1);
      String alias = aliasManager.getAlias(id.identifier);
      aliasManager.addAlias(path.getPath(), alias);

      addInnerJoinPath(path);

      return data;
   }

   public Object visit(ASTRangeVariableDeclaration node, Object data)
   {
      ASTAbstractSchema schema = (ASTAbstractSchema) node.jjtGetChild(0);
      JDBCAbstractEntityBridge entity = (JDBCAbstractEntityBridge) schema.entity;
      ASTIdentifier id = (ASTIdentifier) node.jjtGetChild(1);
      declareTable(id.identifier, entity.getTableName());
      return data;
   }

   // Private

   private void compareEntity(boolean not, Node fromNode, Node toNode, StringBuffer buf)
   {
      buf.append('(');
      if(not)
      {
         buf.append(SQLUtil.NOT).append('(');
      }

      ASTPath fromPath = (ASTPath) fromNode;
      addLeftJoinPath(fromPath);
      String fromAlias = aliasManager.getAlias(fromPath.getPath());
      JDBCAbstractEntityBridge fromEntity = (JDBCAbstractEntityBridge) fromPath.getEntity();

      if(toNode instanceof ASTParameter)
      {
         ASTParameter toParam = (ASTParameter) toNode;

         // can only compare like kind entities
         verifyParameterEntityType(toParam.number, fromEntity);

         inputParameters.addAll(QueryParameter.createParameters(toParam.number - 1, fromEntity));

         SQLUtil.getWhereClause(fromEntity.getPrimaryKeyFields(), fromAlias, buf);
      }
      else
      {
         ASTPath toPath = (ASTPath) toNode;
         addLeftJoinPath(toPath);
         String toAlias = aliasManager.getAlias(toPath.getPath());
         JDBCAbstractEntityBridge toEntity = (JDBCAbstractEntityBridge) toPath.getEntity();

         // can only compare like kind entities
         if(!fromEntity.equals(toEntity))
         {
            throw new IllegalStateException("Only like types can be "
               +
               "compared: from entity="
               +
               fromEntity.getEntityName()
               + " to entity=" + toEntity.getEntityName());
         }

         SQLUtil.getSelfCompareWhereClause(fromEntity.getPrimaryKeyFields(), fromAlias, toAlias, buf);
      }

      if(not)
      {
         buf.append(')');
      }
      buf.append(')');
   }

   private void join(String alias, StringBuffer sql)
   {
      Map paths = (Map) joinPaths.get(alias);
      if(paths == null || paths.isEmpty())
      {
         return;
      }

      for(Iterator iter = paths.values().iterator(); iter.hasNext();)
      {
         String leftAlias = alias;
         ASTPath path = (ASTPath) iter.next();
         for(int i = 1; i < path.size(); ++i)
         {
            if(path.isCMRField(i))
            {
               final String curPath = path.getPath(i);
               final String joinAlias = aliasManager.getAlias(curPath);

               if(joinedAliases.add(joinAlias))
               {
                  final JDBCAbstractCMRFieldBridge cmrField = (JDBCAbstractCMRFieldBridge) path.getCMRField(i);
                  final JDBCAbstractEntityBridge joinEntity = (JDBCAbstractEntityBridge) cmrField.getRelatedEntity();

                  JDBCRelationMetaData relation = cmrField.getMetaData().getRelationMetaData();

                  String join = (path.innerJoin ? " INNER JOIN " : " LEFT OUTER JOIN ");

                  if(relation.isTableMappingStyle())
                  {
                     String relTableAlias = aliasManager.getRelationTableAlias(curPath);
                     sql.append(join)
                        .append(cmrField.getTableName())
                        .append(' ')
                        .append(relTableAlias)
                        .append(" ON ");
                     SQLUtil.getRelationTableJoinClause(cmrField, leftAlias, relTableAlias, sql);

                     sql.append(join)
                        .append(joinEntity.getTableName())
                        .append(' ')
                        .append(joinAlias)
                        .append(" ON ");
                     SQLUtil.getRelationTableJoinClause(cmrField.getRelatedCMRField(), joinAlias, relTableAlias, sql);
                  }
                  else
                  {
                     sql.append(join)
                        .append(joinEntity.getTableName())
                        .append(' ')
                        .append(joinAlias)
                        .append(" ON ");

                     SQLUtil.getJoinClause(cmrField, leftAlias, joinAlias, sql);
                  }

                  join(joinAlias, sql);
               }
               leftAlias = joinAlias;
            }
         }
      }
   }

   private void declareTable(String alias, String table)
   {
      identifierToTable.put(alias, table);
   }

   private void addLeftJoinPath(ASTPath path)
   {
      if(path.size() > 1 && path.isCMRField(1))
      {
         final String identifier = path.getPath(0);
         final String alias = aliasManager.getAlias(identifier);
         Map paths = (Map) joinPaths.get(alias);
         if(paths == null)
         {
            paths = new HashMap();
            joinPaths.put(alias, paths);
         }

         ASTPath oldPath = (ASTPath) paths.put(path, path);
         if(oldPath != null && oldPath.innerJoin)
         {
            path.innerJoin = true;
         }
      }
   }

   private void addInnerJoinPath(ASTPath path)
   {
      if(path.size() > 1 && path.isCMRField(1))
      {
         final String identifier = path.getPath(0);
         final String alias = aliasManager.getAlias(identifier);
         Map paths = (Map) joinPaths.get(alias);
         if(paths == null)
         {
            paths = new HashMap();
            joinPaths.put(alias, paths);
         }

         path.innerJoin = true;
         paths.put(path, path);
      }
   }

   private Object[] childrenToStringArr(int numChildren, Node node)
   {
      Object[] args = new Object[numChildren];
      for(int i = 0; i < numChildren; ++i)
      {
         args[i] = node.jjtGetChild(i).jjtAccept(this, new StringBuffer()).toString();
      }
      return args;
   }

   /**
    * Recursively searches for ASTPath among children.
    *
    * @param selectFunction a node implements SelectFunction
    * @return ASTPath child or null if there was no child of type ASTPath
    */
   private ASTPath getPathFromChildren(Node selectFunction)
   {
      for(int childInd = 0; childInd < selectFunction.jjtGetNumChildren(); ++childInd)
      {
         Node child = selectFunction.jjtGetChild(childInd);
         if(child instanceof ASTPath)
         {
            return (ASTPath) child;
         }
         else if(child instanceof SelectFunction)
         {
            Node path = getPathFromChildren(child);
            if(path != null)
            {
               return (ASTPath) path;
            }
         }
      }
      return null;
   }

   private void setTypeFactory(JDBCTypeFactory typeFactory)
   {
      this.typeFactory = typeFactory;
      this.typeMapping = typeFactory.getTypeMapping();
      aliasManager = new AliasManager(typeMapping.getAliasHeaderPrefix(),
         typeMapping.getAliasHeaderSuffix(),
         typeMapping.getAliasMaxLength());
   }

   private Class getParameterType(int index)
   {
      int zeroBasedIndex = index - 1;
      Class[] params = parameterTypes;
      if(zeroBasedIndex < params.length)
      {
         return params[zeroBasedIndex];
      }
      return null;
   }

   // verify that parameter is the same type as the entity
   private void verifyParameterEntityType(int number, JDBCAbstractEntityBridge entity)
   {
      Class parameterType = getParameterType(number);
      Class remoteClass = entity.getRemoteInterface();
      Class localClass = entity.getLocalInterface();
      if((localClass == null || !localClass.isAssignableFrom(parameterType)) &&
         (remoteClass == null || !remoteClass.isAssignableFrom(parameterType)))
      {
         throw new IllegalStateException("Only like types can be compared: from entity=" +
            entity.getEntityName() + " to parameter type=" + parameterType);
      }
   }

   private void reset()
   {
      returnType = null;
      parameterTypes = null;
      readAhead = null;
      inputParameters.clear();
      selectObject = null;
      selectManager = null;
      typeFactory = null;
      typeMapping = null;
      aliasManager = null;
      forceDistinct = false;
      limitParam = 0;
      limitValue = 0;
      offsetParam = 0;
      offsetValue = 0;
      leftJoinCMRList.clear();
      onFindCMRJoin = null;
      countCompositePk = false;
      joinPaths.clear();
      identifierToTable.clear();
      joinedAliases.clear();
   }
}
