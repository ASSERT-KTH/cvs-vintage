<?xml version="1.0" encoding="utf-8"?>
<!-- Content Stylesheet for Struts User's Guide -->
<!-- $Id: struts.xsl,v 1.14 2003/09/11 21:31:24 sraeburn Exp $ -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns="http://www.w3.org/1999/xhtml"
                version="1.0">


  <!-- Output method -->
  <!-- omit-xml-declaration="yes"   ensures browsers don't display file in  -->
  <!--                             'quirks' mode.                           -->
  <!-- encoding="utf-8"             xmlvalidate ant task only recognizes    -->
  <!--                              charset if declared in xml declaration. -->
  <!--                              defaults to utf-8 otherwise             -->
  <xsl:output method="xml" 
  	      version="1.0" 
  	      encoding="utf-8" 
  	      omit-xml-declaration="yes" 
  	      doctype-public="-//W3C//DTD XHTML 1.0 Strict//EN" 
  	      doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd" 
  	      indent="yes" 
  	      media-type="text/html"/>


  <!-- Defined parameters (overrideable) -->
  <xsl:param    name="home-logo"         select="'/images/jakarta-logo.gif'"/>
  <xsl:param    name="powered-logo"      select="'/images/struts-power.gif'"/>
  <xsl:param    name="project-logo"      select="'/images/struts.gif'"/>
  <xsl:param    name="project-name"      select="'Struts Framework'"/>
  <xsl:param    name="relative-path"     select="'..'"/>
  <xsl:param    name="css-path"          select="'/struts.css'"/>
  <xsl:param    name="project-path"      select="'../project.xml'"/>
  

  <!-- Import project information document -->
  <xsl:variable name="project"
                select="document($project-path)/project"/>  


  <!-- Process an entire document into an HTML page -->
  <xsl:template match="document">
    <html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
    <xsl:choose>
      <xsl:when test="properties/title">
        <title><xsl:value-of select="properties/title"/></title>
      </xsl:when>
      <xsl:when test="body/title">
        <title><xsl:value-of select="body/title"/></title>
      </xsl:when>
      <xsl:otherwise>
        <title><xsl:value-of select="$project/title"/></title>
      </xsl:otherwise>
    </xsl:choose>

    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />   

    <xsl:for-each select="properties/author">
    <xsl:variable name="author">
      <xsl:value-of select="."/>
    </xsl:variable>
    <meta name="author" content="{$author}"/>
    </xsl:for-each>

    <xsl:variable name="css">
      <xsl:value-of select="$relative-path"/>
      <xsl:value-of select="$css-path"/>
    </xsl:variable>	    
    <link rel="stylesheet" type="text/css" href="{$css}"/>
    </head>

    <body>
         
        <div id="heading">

        <xsl:variable name="src">
          <xsl:value-of select="$relative-path"/><xsl:value-of select="$home-logo"/>
        </xsl:variable>
        <a href="http://jakarta.apache.org/">
            <img src="{$src}" alt="The Jakarta Project" id="jakarta-logo"/>
        </a>

        <xsl:variable name="src">
            <xsl:value-of select="$relative-path"/><xsl:value-of select="$project-logo"/>
        </xsl:variable>
        <a href="http://jakarta.apache.org/struts/">
            <img src="{$src}" alt="Struts Framework" id="struts-logo"/>
        </a>
        </div>

        <div id="content">

        <div id="main">
            <xsl:apply-templates select="body"/>
        </div>

        <div id="menu">
            <xsl:apply-templates select="$project"/>

            <!-- 
                 Output contributors list if the project 'authors' attribute 
                 is true and the document level 'authors' attribute is either
                 true or not specified.
            -->
            <xsl:if test="(boolean($project[@authors='true']) 
                 and not(/document/@authors)) or /document[@authors='true']">
                 
                <!-- Only output contributors if there are any specified for this document -->     
                <xsl:if test="/document/properties/author">            
                <div class="authors">
                <p><strong>Contributors</strong></p>
                  <ul>
	          <xsl:for-each select="/document/properties/author">
	            <li><xsl:value-of select="."/></li>
	          </xsl:for-each>
                  </ul>
                </div>
                </xsl:if>
            </xsl:if>
        </div>
        
        </div>

      <div id="footer">
        <xsl:variable name="src">
          <xsl:value-of select="$relative-path"/><xsl:value-of select="$powered-logo"/>
        </xsl:variable>
        <img src="{$src}" alt="Powered by Struts" id="powered-logo"/>
        Copyright (c) 2000-2003, Apache Software Foundation <span class="noprint">- 
        <a href="http://nagoya.apache.org/wiki/apachewiki.cgi?StrutsDocComments">Comments?</a></span>
      </div>

    </body>
    </html>

  </xsl:template>


  <!-- Process the project element for the navigation bar -->
  <xsl:template match="project">
    <xsl:apply-templates/>
  </xsl:template>


  <!-- Silently skip title element in project.xml -->
  <xsl:template match="title"/> 


  <!-- Process a menu for the navigation bar -->
  <xsl:template match="menu">
    <p><xsl:value-of select="@name"/></p>
    <ul>
       <xsl:apply-templates/>
    </ul>
  </xsl:template>


  <!-- Process a menu item for the navigation bar -->
  <xsl:template match="item">
    <li>
      <xsl:variable name="href">
        <xsl:value-of select="@href"/>
      </xsl:variable>
      <a href="{$href}"><xsl:value-of select="@name"/></a>
    </li>
  </xsl:template>


  <!-- Process a document body -->
  <xsl:template match="body">
    <xsl:apply-templates/>
  </xsl:template>
  

  <!-- Process an entire chapter -->
  <xsl:template match="chapter">
    <xsl:element name="h1">
      <xsl:if test="@href">
        <xsl:attribute name="id">
          <xsl:value-of select="@href" />
        </xsl:attribute> 
      </xsl:if>
      <xsl:value-of select="@name"/>
    </xsl:element>
    <xsl:apply-templates select="section" />
  </xsl:template>


  <!-- Process a documentation section -->
  <xsl:template match="section">
    <xsl:choose>
    <xsl:when test="@name">
    <xsl:element name="h2">
      <xsl:if test="@href">
        <xsl:attribute name="id">
          <xsl:value-of select="@href" />
        </xsl:attribute> 
      </xsl:if>
      <xsl:value-of select="@name"/>
    </xsl:element>
    </xsl:when>
    <xsl:otherwise>
      <hr class="section"/>
    </xsl:otherwise>
    </xsl:choose>
    <div class="indent">
    <xsl:apply-templates />
    </div>
  </xsl:template>


  <!-- Process a documentation subsection -->
  <xsl:template match="subsection">
    <xsl:element name="h3">
      <xsl:if test="@href">
        <xsl:attribute name="id">
          <xsl:value-of select="@href" />
        </xsl:attribute> 
      </xsl:if>
      <xsl:value-of select="@name"/>
    </xsl:element>
    <div class="indent">
    <xsl:apply-templates />
    </div>
  </xsl:template>
  
  
  <!-- Process a tag library section -->
  <xsl:template match="taglib">
       <h2 id="top"><xsl:value-of select="display-name"/></h2>
       <div class="indent">      

       <xsl:apply-templates select="info"/>
       <table class="taglib-summary">
           <thead>
           <tr>
              <th>Tag Name</th>
              <th>Description</th>
            </tr>
            </thead>
            <xsl:for-each select="tag">
              <tr>
                <td align="center">
                  <xsl:variable name="name">
                    <xsl:value-of select="name"/>
                  </xsl:variable>
                  <a href="#{$name}"><xsl:value-of select="name"/></a>
                </td>
                <td>
                  <xsl:value-of select="summary"/>
                </td>
              </tr>
            </xsl:for-each>
          </table>
    </div>
    <xsl:apply-templates select="tag"/>
  </xsl:template>

  <!-- Process an individual tag -->
  <xsl:template match="tag">
    <xsl:variable name="name">
      <xsl:value-of select="name"/>
    </xsl:variable>
    <h3 id="{$name}"><strong><xsl:value-of select="name"/></strong> - <xsl:value-of select="summary"/></h3>
    
    <div class="indent">      

      <xsl:apply-templates select="info"/>

      <xsl:if test="./attribute">
        <table class="tag-attributes">
         <thead>
          <tr>
            <th class="attribute">Attribute Name</th>
            <th>Description</th>
          </tr>
          </thead>
          <xsl:apply-templates select="attribute"/>
        </table>
      </xsl:if>     
      
      </div>
      <p><a href="#top">Back to top</a></p>

  </xsl:template>

  <!-- Create the table of documentation for a tag -->
  <xsl:template match="attribute">
      <tr>
        <td align="center">
          <xsl:value-of select="name"/>
        </td>
        <td>
          <xsl:apply-templates select="info"/>
          <xsl:variable name="required">
            <xsl:value-of select="required"/>
          </xsl:variable>
          <xsl:if test="required='true'">
            [Required]
          </xsl:if>
          <xsl:if test="rtexprvalue='true'">
            [RT Expr]
          </xsl:if>
        </td>
      </tr>
  </xsl:template>


  <!-- Process an tag info section -->
  <xsl:template match="info">
     <xsl:apply-templates/>
  </xsl:template>
  

  <!-- Process a task list section -->
  <xsl:template match="task-list">
    <xsl:element name="h2">
      <xsl:if test="@href">
        <xsl:attribute name="id">
          <xsl:value-of select="@href" />
        </xsl:attribute> 
      </xsl:if>
      <xsl:value-of select="@name"/>
    </xsl:element>

    <div class="indent">
    
    <xsl:apply-templates select="info"/>
    
    <xsl:if test="child::task">
    
    <table class="task-list">
      <thead>
      <tr>
        <th>Description</th>
        <th>Volunteer</th>
      </tr>
      </thead>
      <xsl:apply-templates select="task"/>
     </table>
     </xsl:if>
     </div>
  </xsl:template>


  <!-- Process an individual task (in a TODO list) -->
  <xsl:template match="task">
    <tr>
      <td>
        <xsl:choose>
          <xsl:when test="@name">
            <em><xsl:value-of select="@name"/></em>.
          </xsl:when>
        </xsl:choose>
        <xsl:value-of select="info"/>
      </td>
      <td><xsl:value-of select="assigned"/></td>
    </tr>
  </xsl:template>


  <!-- Process everything else by just passing it through -->
  <xsl:template match="*|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>