/***************************************
 *                                     *
 *  JBoss: The OpenSource J2EE WebOS   *
 *                                     *
 *  Distributable under LGPL license.  *
 *  See terms of license at gnu.org.   *
 *                                     *
 ***************************************/

package org.jboss.cmp.ejbql;

import java.io.StringReader;

import org.jboss.cmp.query.CollectionRelation;
import org.jboss.cmp.query.CrossJoin;
import org.jboss.cmp.query.NamedRelation;
import org.jboss.cmp.query.Path;
import org.jboss.cmp.query.Projection;
import org.jboss.cmp.query.Query;
import org.jboss.cmp.query.RangeRelation;
import org.jboss.cmp.query.Relation;
import org.jboss.cmp.schema.AbstractAssociationEnd;
import org.jboss.cmp.schema.AbstractAttribute;
import org.jboss.cmp.schema.AbstractClass;
import org.jboss.cmp.schema.AbstractSchema;
import org.jboss.cmp.schema.AbstractType;

public class EJBQL20Compiler implements ParserVisitor
{
   private final AbstractSchema schema;
   private final EJBQL20Parser parser;

   public EJBQL20Compiler(AbstractSchema schema)
   {
      this.schema = schema;
      parser = new EJBQL20Parser(new StringReader(""));
   }

   public Query compile(String ejbql) throws ParseException, CompileException
   {
      parser.ReInit(new StringReader(ejbql));
      ASTEJBQL rootNode = parser.EJBQL();

      return (Query) rootNode.jjtAccept(this, null);
   }

   public Object visit(SimpleNode node, Object data)
   {
      throw new IllegalStateException();
   }

   public Object visit(ASTEJBQL node, Object data) throws CompileException
   {
      Query query = new Query();
      // visit FROM first to define all the identification_variables
      ((VisitableNode) node.jjtGetChild(1)).jjtAccept(this, query);

      // now we can get the SELECT list
      ((VisitableNode) node.jjtGetChild(0)).jjtAccept(this, query);

      // and finally the WHERE clause
      if (node.jjtGetNumChildren() > 2)
      {
         ((VisitableNode) node.jjtGetChild(2)).jjtAccept(this, query);
      }
      return query;
   }

   public Object visit(ASTSelect node, Object data) throws CompileException
   {
      Query query = (Query) data;
      Projection projection = new Projection();
      projection.setDistinct(node.distinct);

      VisitableNode n = (VisitableNode) node.jjtGetChild(0);
      if (n instanceof ASTPath)
      {
         Path nav = (Path) n.jjtAccept(this, query);
         if (nav.isCollection())
         {
            throw new CompileException("Cannot SELECT collection, path is:" + nav);
         }
         projection.addChild(nav);
      }
      else if (n instanceof ASTIdentifier)
      {
         ASTIdentifier idNode = (ASTIdentifier) n;
         NamedRelation relation = query.getRelation(idNode.getName());
         if (relation == null)
         {
            throw new CompileException("Unknown identification variable: " + idNode.getName());
         }
         projection.addChild(new Path(relation));
      }
      else
      {
         throw new IllegalStateException();
      }
      query.setProjection(projection);
      return data;
   }

   public Object visit(ASTFrom node, Object data) throws CompileException
   {
      return node.childrenAccept(this, data);
   }

   public Object visit(ASTIdDeclaration node, Object data) throws CompileException
   {
      Query query = (Query) data;
      ASTIdentifier idNode = (ASTIdentifier) node.jjtGetChild(1);
      String id = idNode.getName();
      if (schema.isClassNameInUse(id))
      {
         throw new CompileException("IdentificationVariable " + id + " matches ejb-name or abstract-schema-name");
      }
      if (query.getRelation(id) != null)
      {
         throw new CompileException("IdentificationVariable " + id + " already used");
      }

      NamedRelation relation;
      Node n = node.jjtGetChild(0);
      if (n instanceof ASTIdentifier)
      {
         ASTIdentifier rangeDecl = (ASTIdentifier) n;
         AbstractClass rangeClass = schema.getClassByName(rangeDecl.getName());
         if (rangeClass == null)
         {
            throw new CompileException("Unknown abstract-schema-name: " + rangeDecl.getName());
         }
         relation = new RangeRelation(id, rangeClass);
      }
      else if (n instanceof ASTPath)
      {
         ASTPath pathDecl = (ASTPath) n;
         Path nav = (Path) pathDecl.jjtAccept(this, data);
         if (nav.isCollection() == false)
         {
            throw new CompileException("Path does not end in a collection:" + nav);
         }
         relation = new CollectionRelation(id, nav);
      }
      else
      {
         throw new IllegalStateException();
      }

      query.addAlias(relation);
      Relation left = query.getRelation();
      if (left != null)
      {
         query.setRelation(new CrossJoin(left, relation));
      }
      else
      {
         query.setRelation(relation);
      }
      return data;
   }

   public Object visit(ASTWhere node, Object data)
   {
      return null;
   }

   public Object visit(ASTOr node, Object data)
   {
      return null;
   }

   public Object visit(ASTAnd node, Object data)
   {
      return null;
   }

   public Object visit(ASTNot node, Object data)
   {
      return null;
   }

   public Object visit(ASTCondition node, Object data)
   {
      return null;
   }

   public Object visit(ASTPath node, Object data) throws CompileException
   {
      Query query = (Query) data;
      ASTIdentifier idVarNode = (ASTIdentifier) node.jjtGetChild(0);
      NamedRelation relation = query.getRelation(idVarNode.getName());
      if (relation == null)
      {
         throw new CompileException("Unknown identification-variable: " + idVarNode.getName());
      }
      Path navigation = new Path(relation);
      for (int i = 1; i < node.jjtGetNumChildren(); i++)
      {
         if (i > 1 && navigation.isCollection())
         {
            throw new CompileException("Cannot navigate collection path:" + node.toString(i));
         }
         ASTIdentifier idNode = (ASTIdentifier) node.jjtGetChild(i);
         String name = idNode.getName();
         AbstractType type = navigation.getType();
         if (type instanceof AbstractClass)
         {
            AbstractClass clazz = (AbstractClass) type;
            AbstractAttribute attr = clazz.getAttributeByName(name);
            if (attr != null)
            {
               navigation.addStep(attr);
            }
            else
            {
               AbstractAssociationEnd end = clazz.getAssocationByName(name);
               if (end == null)
               {
                  throw new CompileException("Cannot resolve path:" + node.toString(i + 1));
               }
               navigation.addStep(end);
            }
         }
         else
         {
            throw new CompileException("Cannot navigate path, prefix is:" + node.toString(i + 1));
         }
      }
      return navigation;
   }

   public Object visit(ASTIdentifier node, Object data)
   {
      return null;
   }

   public Object visit(ASTInputParameter node, Object data)
   {
      return null;
   }

   public Object visit(ASTLiteral node, Object data)
   {
      return null;
   }
}
