<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Content Stylesheet for Struts User's Guide -->
<!-- $Id: userGuide.xsl,v 1.14 2003/08/29 14:27:25 husted Exp $ -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  version="1.0">


  <!-- Output method -->
  <xsl:output method="html"
            encoding="iso-8859-1"
              indent="no"/>


  <!-- Defined parameters (overrideable) -->
  <xsl:param    name="home-href"         select="'http://jakarta.apache.org/'"/>
  <xsl:param    name="home-logo"         select="'/images/jakarta-logo.gif'"/>
  <xsl:param    name="home-name"         select="'The Jakarta Project'" />
  <xsl:param    name="printer-logo"      select="'/images/printer.gif'"/>
  <xsl:param    name="printer-name"      select="'Print-Friendly Version'"/>
  <xsl:param    name="powered-logo"      select="'/images/struts-power.gif'"/>
  <xsl:param    name="powered-name"      select="'Powered by Struts'"/>
  <xsl:param    name="project-href"      select="'http://jakarta.apache.org/struts/'"/>
  <xsl:param    name="project-logo"      select="'/images/struts.gif'"/>
  <xsl:param    name="project-menu"      select="'menu'"/>
  <xsl:param    name="project-name"      select="'Struts Framework'"/>
  <xsl:param    name="relative-path"     select="'.'"/>


  <!-- Defined variables (non-overrideable) -->
  <xsl:variable name="body-bg"           select="'#ffffff'"/>
  <xsl:variable name="body-fg"           select="'#000000'"/>
  <xsl:variable name="body-link"         select="'#023264'"/>
  <xsl:variable name="banner-bg"         select="'#023264'"/>
  <xsl:variable name="banner-fg"         select="'#ffffff'"/>


  <!-- Process an entire document into an HTML page -->
  <xsl:template match="document">
    <xsl:variable name="project"
                select="document('../userGuide/project.xml')/project"/>
    <html>
    <head>

    <xsl:for-each select="properties/author">
    <xsl:variable name="author">
      <xsl:value-of select="."/>
    </xsl:variable>
    <meta name="author" content="{$author}"/>
    </xsl:for-each>

    <!-- <link rel="stylesheet" type="text/css" href="default.css"/> -->
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
    </head>

    <body bgcolor="{$body-bg}" text="{$body-fg}" link="{$body-link}"
          alink="{$body-link}" vlink="{$body-link}">
 
    <table border="0" width="100%" cellspacing="5">

      <tr><td colspan="2">

        <xsl:comment>
          JAKARTA LOGO
        </xsl:comment>
        <xsl:variable name="alt">
          <xsl:value-of select="$home-name"/>
        </xsl:variable>
        <xsl:variable name="href">
          <xsl:value-of select="$home-href"/>
        </xsl:variable>
        <xsl:variable name="src">
          <xsl:value-of select="$relative-path"/><xsl:value-of select="$home-logo"/>
        </xsl:variable>
        <a href="{$href}">
        <img src="{$src}" align="left" alt="{$alt}" border="0"/>
        </a>

        <xsl:comment>
          STRUTS LOGO
        </xsl:comment>
        <xsl:variable name="alt">
          <xsl:value-of select="$project-name"/>
        </xsl:variable>
        <xsl:variable name="href">
          <xsl:value-of select="$project-href"/>
        </xsl:variable>
        <xsl:variable name="src">
          <xsl:value-of select="$relative-path"/><xsl:value-of select="$project-logo"/>
        </xsl:variable>
        <a href="{$href}">
        <img src="{$src}" align="right" alt="{$alt}" border="0"/>
        </a>

      </td></tr>

      <tr><td colspan="2">
        <hr/>
      </td></tr>

      <xsl:if test="$project-menu = 'menu'">
        <tr><td colspan="2" align="center">
          <xsl:variable name="alt">
            <xsl:value-of select="$printer-name"/>
          </xsl:variable>
          <xsl:variable name="url">
            <xsl:value-of select="/document/@url"/>
          </xsl:variable>
          <xsl:variable name="src">
            <xsl:value-of select="$relative-path"/><xsl:value-of select="$printer-logo"/>
          </xsl:variable>
          <a href="printer/{$url}">
            <img src="{$src}" alt="{$alt}" border="0"/>
            <xsl:value-of select="$printer-name"/>
          </a>
        </td></tr>
      </xsl:if>

      <tr>
        <xsl:if test="$project-menu = 'menu'">
          <td width="120" valign="top">
            <xsl:apply-templates select="$project"/>
          </td>
        </xsl:if>

        <td valign="top">
          <xsl:apply-templates select="body"/>
        </td>
      </tr>

      <tr><td colspan="2">
        <hr/>
      </td></tr>

      <tr><td colspan="2">
        <div align="center"><font color="{$body-link}" size="-1"><em>
        Copyright (c) 2000-2003, Apache Software Foundation - <a href="http://nagoya.apache.org/wiki/apachewiki.cgi?StrutsDocComments">Comments?</a>
        </em></font></div>

          <xsl:variable name="alt">
            <xsl:value-of select="$powered-name"/>
          </xsl:variable>
          <xsl:variable name="src">
            <xsl:value-of select="$relative-path"/><xsl:value-of select="$powered-logo"/>
          </xsl:variable>
          <img src="{$src}" alt="{$alt}" align="right" border="0"/>
      </td></tr>

    </table>
    </body>
    </html>

  </xsl:template>


  <!-- Process the project element for the navigation bar -->
  <xsl:template match="project">
    <xsl:apply-templates/>
  </xsl:template>


  <!-- Process an entire chapter (assumes one chapter per page) -->
  <xsl:template match="chapter">
    <xsl:element name="a">
      <xsl:attribute name="name">
        <xsl:value-of select="@href" />
      </xsl:attribute>
    </xsl:element>
    <table border="0" cellspacing="5" cellpadding="5" width="100%">
      <tr><td bgcolor="{$banner-bg}">
        <font color="{$banner-fg}" face="arial,helvetica,sanserif" size="+1">
          <strong><xsl:value-of select="@name"/></strong>
        </font>
      </td></tr>
      <tr><td><p>Contributors: </p><ul>
    <xsl:for-each select="/document/properties/author">
    <li><xsl:value-of select="."/></li>
    </xsl:for-each>
    </ul>
      </td></tr>
    </table>
    <xsl:apply-templates select="section" />
  </xsl:template>


  <!-- Process a menu for the navigation bar -->
  <xsl:template match="menu">
    <table border="0" cellspacing="5">
      <tr>
        <th colspan="2" align="left">
          <font color="{$body-link}"><strong>
            <xsl:value-of select="@name"/>
          </strong></font>
        </th>
      </tr>
      <xsl:apply-templates/>
    </table>
  </xsl:template>


  <!-- Process a menu item for the navigation bar -->
  <xsl:template match="item">
    <tr>
      <td align="center" width="15"></td>
      <td>
        <font size="-1">
        <xsl:variable name="href">
          <xsl:value-of select="@href"/>
        </xsl:variable>
        <a href="{$href}"><xsl:value-of select="@name"/></a>
        </font>
      </td>
    </tr>
  </xsl:template>


  <!-- Process a documentation section -->
  <xsl:template match="section">
    <xsl:choose>
      <xsl:when test="@href">
        <xsl:variable name="href">
          <xsl:value-of select="@href"/>
        </xsl:variable>
        <a name="{$href}"></a>
      </xsl:when>
    </xsl:choose>
    <table border="0" cellspacing="5" cellpadding="5" width="100%">
      <tr><td bgcolor="{$banner-bg}">
        <font color="{$banner-fg}" face="arial,helvetica,sanserif" size="+1">
          <strong><xsl:value-of select="@name"/></strong>
        </font>
      </td></tr>
      <tr><td>
        <blockquote>
          <xsl:apply-templates/>
        </blockquote>
      </td></tr>
    </table>
  </xsl:template>


  <!-- Process a tag library section -->
  <xsl:template match="taglib">
    <table border="0" cellspacing="5" cellpadding="5" width="98%">
      <tr><td bgcolor="{$banner-bg}">
        <font color="{$banner-fg}" face="arial,helvetica,sanserif" size="+1">
          <strong><xsl:value-of select="display-name"/></strong>
        </font>
      </td></tr>
      <tr><td>
        <blockquote>
          <xsl:apply-templates select="info"/>
        </blockquote>
      </td></tr>
      <tr><td>
        <blockquote>
          <table border="1" cellspacing="2" cellpadding="2">
            <tr>
              <th width="15%"><a name="index"></a>Tag Name</th>
              <th>Description</th>
            </tr>
            <xsl:for-each select="tag">
              <xsl:sort select="name"/>
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
        </blockquote>
      </td></tr>
    </table>
    <xsl:apply-templates select="tag">
      <xsl:sort select="name"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- Process an individual tag -->
  <xsl:template match="tag">
    <xsl:variable name="name">
      <xsl:value-of select="name"/>
    </xsl:variable>
    <a name="{$name}"></a>
    <table border="0" cellspacing="2" cellpadding="2">
      <tr><td bgcolor="{$banner-bg}">
        <font color="{$banner-fg}" face="arial,helvetica,sanserif">
          <strong><xsl:value-of select="name"/></strong> -
          <xsl:value-of select="summary"/>
        </font>
      </td></tr>
      <tr><td>
        <blockquote>
          <xsl:if test="deprecated">
            <p><font color="red"><strong>DEPRECATED: </strong></font>
            <xsl:value-of select="deprecated"/></p>
          </xsl:if>
          <xsl:if test="since">
            <p>Since:  <xsl:value-of select="since"/></p>
          </xsl:if>
          <xsl:apply-templates select="info"/>
        </blockquote>
      </td></tr>
      <xsl:if test="not(@document-attributes)">
        <xsl:call-template name="document-tag-attributes" />
      </xsl:if>
      <xsl:if test="@document-attributes='true'">
        <xsl:call-template name="document-tag-attributes" />
      </xsl:if>
    </table>
    <p><a href="#index">Back to top</a></p>
  </xsl:template>


  <!-- Create the table of documentation for a tag -->
  <xsl:template name="document-tag-attributes">
    <tr><td>
      <blockquote>
        <table border="1" cellspacing="2" cellpadding="2">
          <tr>
            <th width="15%">Attribute Name</th>
            <th>Description</th>
          </tr>
          <xsl:for-each select="attribute">
            <xsl:sort select="name"/>
            <tr>
              <td align="center">
                <xsl:value-of select="name"/>
              </td>
              <td>
                <xsl:if test="deprecated">
                  <p><font color="red"><strong>DEPRECATED: </strong></font>
                  <xsl:value-of select="deprecated"/></p>
                </xsl:if>
                <xsl:if test="since">
                  <p>Since:  <xsl:value-of select="since"/></p>
                </xsl:if>
                <xsl:apply-templates select="info"/>
                <xsl:variable name="required">
                  <xsl:value-of select="required"/>
                </xsl:variable>
                <xsl:if test="default">
                  [<xsl:value-of select="default"/>]
                </xsl:if>
                <xsl:if test="required='true'">
                  (REQUIRED)
                </xsl:if>
                <xsl:if test="rtexprvalue='true'">
                  (RT EXPR)
                </xsl:if>
              </td>
            </tr>
          </xsl:for-each>
        </table>
      </blockquote>
    </td></tr>
  </xsl:template>


  <!-- Process everything else by just passing it through -->
  <xsl:template match="*|@*">
    <xsl:copy>
      <xsl:apply-templates select="@*|*|text()"/>
    </xsl:copy>
  </xsl:template>


</xsl:stylesheet>
