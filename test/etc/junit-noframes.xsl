<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
	xmlns:lxslt="http://xml.apache.org/xslt"
	xmlns:stringutils="xalan://org.apache.tools.ant.util.StringUtils">
<xsl:output method="html" indent="yes" encoding="US-ASCII"
  doctype-public="-//W3C//DTD HTML 4.01 Transitional//EN" />
<xsl:decimal-format decimal-separator="." grouping-separator="," />
<!--
   Copyright 2001-2004 The Apache Software Foundation

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 -->

<!--

 Sample stylesheet to be used with Ant JUnitReport output.

 It creates a non-framed report that can be useful to send via
 e-mail or such.

 @author Stephane Bailliez <a href="mailto:sbailliez@apache.org"/>
 @author Erik Hatcher <a href="mailto:ehatcher@apache.org"/>
 Customized for Carol

-->
<xsl:template match="testsuites">
    <html>
     <head>
      <title>Test Results</title>
      <style type="text/css">
      body {
        background:#ffffff;
        font:normal 68% verdana,arial,helvetica;
        color:#000000;
      }

      table tr td, table tr th {
          font-size: 68%;
      }

      table.details tr th{
        color: #FFFFFF;
        font-weight: bold;
        text-align:left;
        background:#433C7B;
      }

      table.details tr td{
        background:#CCC;
      }

      p {
        line-height:1.5em;
        margin-top:0.5em; margin-bottom:1.0em;
      }

      a
      {
      color: #3F3975;
      background-color: transparent;
      text-decoration: underline;
      }

      a:visited
      {
      color: #9898CB;
      background-color: transparent;
      text-decoration: underline;
      }

      a:hover
      {
      color: #E06611;
      background-color: transparent;
      text-decoration: underline;
      }


      a:active
      {
      color: #FFFFFF;
      background-color: #E06611;
      text-decoration: underline;
      }


      h1
      {
      color: #E06611;
      font-family: Arial, Helvetica, sans-serif;
      font-size: 22px;
      line-height: 27px;
      font-weight: bold;
      border-color: #99C;
      border-width: 0 0 4px 0;
      border-style: none none solid none;
      margin: 10px 0px 5px 0px;
      }

      h2
      {
      color: #99C;
      font-family: Arial, Helvetica, sans-serif;
      font-size: 20px;
      line-height: 27px;
      font-weight: normal;
      border-color: #E06611;
      border-width: 0 0 4px 0;
      border-style: none none solid none;
      margin: 10px 0px 5px 0px;
      }

      h3
      {
      color: #E06611;
      font-family: Arial, Helvetica, sans-serif;
      font-size: 16px;
      line-height: 27px;
      font-weight: bold;
      border-color: #E8EAF0;
      border-width: 0 0 2px 0;
      border-style: none none solid none;
      margin: 10px 0px 5px 0px;
      }

      h4
      {
      color: #99C;
      font-family: Arial, Helvetica, sans-serif;
      font-size: 13px;
      line-height: 24px;
      font-weight: bold;
      border-color: #E8EAF0;
      border-width: 0 0 2px 0;
      border-style: none none solid none;
      margin: 10px 0px 5px 0px;
      }

      h5
      {
      color: #E06611;
      font-family: Arial, Helvetica, sans-serif;
      font-size: 14px;
      line-height: 24px;
      font-weight: normal;
      border-color: #E8EAF0;
      border-width: 0 0 2px 0;
      border-style: none none solid none;
      margin: 10px 0px 5px 0px;
      }

      h6
      {
      color: #99C;
      font-family: Arial, Helvetica, sans-serif;
      font-size: 14px;
      line-height: 22px;
      font-weight: normal;
      border-color: #E8EAF0;
      border-width: 0 0 2px 0;
      border-style: none none solid none;
      margin: 10px 0px 5px 0px;
      }

      .Error {
        font-weight:bold; color:red;
      }
      .Failure {
      font-weight:bold; color:purple;
      }
      .Properties {
      text-align:right;
      }
      </style>
        </head>
        <body>
            <a name="top"></a>
            <xsl:call-template name="pageHeader"/>

            <!-- Environment Part -->
            <xsl:call-template name="environment"/>

            <!-- Summary part -->
            <xsl:call-template name="summary"/>
            <hr size="1" width="95%" align="left"/>
            <!-- Package List part -->
            <xsl:call-template name="packagelist"/>
            <hr size="1" width="95%" align="left"/>

            <!-- For each package create its part -->
            <xsl:call-template name="packages"/>
            <hr size="1" width="95%" align="left"/>

            <!-- For each class create the  part -->
            <xsl:call-template name="classes"/>

        </body>
    </html>
</xsl:template>





<!-- Environment Part -->
<!--
<xsl:template name="environment">
<h2>Environment</h2>
  <xsl:apply-templates select="testsuite" mode="env"/>
</xsl:template>
 -->

<xsl:template name="environment" >
  <h2>Environment</h2>
  <table width="95%" cellspacing="2" cellpadding="5" border="0" class="details">
  <xsl:variable name="carolver" select="testsuite[1]/properties/property[@name='carol.version']/@value"/>
  <xsl:variable name="jdkver" select="testsuite[1]/properties/property[@name='java.version']/@value"/>
  <xsl:variable name="jdkvendor" select="testsuite[1]/properties/property[@name='java.vendor']/@value"/>
  <xsl:variable name="antver" select="testsuite[1]/properties/property[@name='ant.version']/@value"/>
  <xsl:variable name="osname" select="testsuite[1]/properties/property[@name='os.name']/@value"/>
  <xsl:variable name="osversion" select="testsuite[1]/properties/property[@name='os.version']/@value"/>
  <xsl:variable name="osarch" select="testsuite[1]/properties/property[@name='os.arch']/@value"/>
  <xsl:variable name="user" select="testsuite[1]/properties/property[@name='user.name']/@value"/>
  <xsl:variable name="date" select="testsuite[1]/properties/property[@name='dateofday']/@value"/>
  <tr valign="top">
  <th>Carol Version</th><td> <xsl:value-of select="$carolver" /> </td>
  </tr>
  <tr valign="top">
  <th>Version JDK</th><td> <xsl:value-of select="concat($jdkver,'  (', $jdkvendor,')')" /> </td>
  </tr>
  <tr valign="top">
  <th>Version ANT</th><td> <xsl:value-of select="$antver" /> </td>
  </tr>
  <tr valign="top">
  <th>System</th><td> <xsl:value-of select="concat($osname , '  (', $osversion , '/', $osarch, ')')" /> </td>
  </tr>
  <tr valign="top">
  <th>Author</th><td> <xsl:value-of select="$user" /> </td>
  </tr>
  <tr valign="top">
  <th>Date</th><td> <xsl:value-of select="$date" /> </td>
  </tr>
  </table>

</xsl:template>




    <!-- ================================================================== -->
    <!-- Write a list of all packages with an hyperlink to the anchor of    -->
    <!-- of the package name.                                               -->
    <!-- ================================================================== -->
    <xsl:template name="packagelist">
        <h2>Packages</h2>
        Note: package statistics are not computed recursively, they only sum up all of its testsuites numbers.
        <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
            <xsl:call-template name="testsuite.test.header"/>
            <!-- list all packages recursively -->
            <xsl:for-each select="./testsuite[not(./@package = preceding-sibling::testsuite/@package)]">
                <xsl:sort select="@package"/>
                <xsl:variable name="testsuites-in-package" select="/testsuites/testsuite[./@package = current()/@package]"/>
                <xsl:variable name="testCount" select="sum($testsuites-in-package/@tests)"/>
                <xsl:variable name="errorCount" select="sum($testsuites-in-package/@errors)"/>
                <xsl:variable name="failureCount" select="sum($testsuites-in-package/@failures)"/>
                <xsl:variable name="timeCount" select="sum($testsuites-in-package/@time)"/>

                <!-- write a summary for the package -->
                <tr valign="top">
                    <!-- set a nice color depending if there is an error/failure -->
                    <xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
                            <xsl:when test="$errorCount &gt; 0">Error</xsl:when>
                        </xsl:choose>
                    </xsl:attribute>
                    <td><a href="#{@package}"><xsl:value-of select="@package"/></a></td>
                    <td><xsl:value-of select="$testCount"/></td>
                    <td><xsl:value-of select="$errorCount"/></td>
                    <td><xsl:value-of select="$failureCount"/></td>
                    <td>
                    <xsl:call-template name="display-time">
                        <xsl:with-param name="value" select="$timeCount"/>
                    </xsl:call-template>
                    </td>
                </tr>
            </xsl:for-each>
        </table>
    </xsl:template>


    <!-- ================================================================== -->
    <!-- Write a package level report                                       -->
    <!-- It creates a table with values from the document:                  -->
    <!-- Name | Tests | Errors | Failures | Time                            -->
    <!-- ================================================================== -->
    <xsl:template name="packages">
        <!-- create an anchor to this package name -->
        <xsl:for-each select="/testsuites/testsuite[not(./@package = preceding-sibling::testsuite/@package)]">
            <xsl:sort select="@package"/>
                <a name="{@package}"></a>
                <h3>Package <xsl:value-of select="@package"/></h3>

                <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
                    <xsl:call-template name="testsuite.test.header"/>

                    <!-- match the testsuites of this package -->
                    <xsl:apply-templates select="/testsuites/testsuite[./@package = current()/@package]" mode="print.test"/>
                </table>
                <a href="#top">Back to top</a>
                <p/>
                <p/>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="classes">
        <xsl:for-each select="testsuite">
            <xsl:sort select="@name"/>
            <!-- create an anchor to this class name -->
            <a name="{@name}"></a>
            <h3>TestCase <xsl:value-of select="@name"/></h3>

            <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
              <xsl:call-template name="testcase.test.header"/>
              <!--
              test can even not be started at all (failure to load the class)
              so report the error directly
              -->
                <xsl:if test="./error">
                    <tr class="Error">
                        <td colspan="4"><xsl:apply-templates select="./error"/></td>
                    </tr>
                </xsl:if>
                <xsl:apply-templates select="./testcase" mode="print.test"/>
            </table>
            <div class="Properties">
                <a>
                    <xsl:attribute name="href">javascript:displayProperties('<xsl:value-of select="@package"/>.<xsl:value-of select="@name"/>');</xsl:attribute>
                    Properties &#187;
                </a>
            </div>
            <p/>

            <a href="#top">Back to top</a>
        </xsl:for-each>
    </xsl:template>

    <xsl:template name="summary">
        <h2>Summary</h2>
        <xsl:variable name="testCount" select="sum(testsuite/@tests)"/>
        <xsl:variable name="errorCount" select="sum(testsuite/@errors)"/>
        <xsl:variable name="failureCount" select="sum(testsuite/@failures)"/>
        <xsl:variable name="timeCount" select="sum(testsuite/@time)"/>
        <xsl:variable name="successRate" select="($testCount - $failureCount - $errorCount) div $testCount"/>
        <table class="details" border="0" cellpadding="5" cellspacing="2" width="95%">
        <tr valign="top">
            <th>Tests</th>
            <th>Failures</th>
            <th>Errors</th>
            <th>Success rate</th>
            <th>Time</th>
        </tr>
        <tr valign="top">
            <xsl:attribute name="class">
                <xsl:choose>
                    <xsl:when test="$failureCount &gt; 0">Failure</xsl:when>
                    <xsl:when test="$errorCount &gt; 0">Error</xsl:when>
                </xsl:choose>
            </xsl:attribute>
            <td><xsl:value-of select="$testCount"/></td>
            <td><xsl:value-of select="$failureCount"/></td>
            <td><xsl:value-of select="$errorCount"/></td>
            <td>
                <xsl:call-template name="display-percent">
                    <xsl:with-param name="value" select="$successRate"/>
                </xsl:call-template>
            </td>
            <td>
                <xsl:call-template name="display-time">
                    <xsl:with-param name="value" select="$timeCount"/>
                </xsl:call-template>
            </td>

        </tr>
        </table>
        <table border="0" width="95%">
        <tr>
        <td style="text-align: justify;">
        Note: <i>failures</i> are anticipated and checked for with assertions while <i>errors</i> are unanticipated.
        </td>
        </tr>
        </table>
    </xsl:template>

  <!--
   Write properties into a JavaScript data structure.
   This is based on the original idea by Erik Hatcher (ehatcher@apache.org)
   -->
  <xsl:template match="properties">
    cur = TestCases['<xsl:value-of select="../@package"/>.<xsl:value-of select="../@name"/>'] = new Array();
    <xsl:for-each select="property">
    <xsl:sort select="@name"/>
        cur['<xsl:value-of select="@name"/>'] = '<xsl:call-template name="JS-escape"><xsl:with-param name="string" select="@value"/></xsl:call-template>';
    </xsl:for-each>
  </xsl:template>

<!-- Page HEADER -->
<xsl:template name="pageHeader">
    <h1>Carol Test Results</h1>
    <table width="100%">
    <tr>
        <td align="left"></td>
        <td align="right">Designed for use with <a href='http://www.junit.org'>JUnit</a> and <a href='http://jakarta.apache.org/ant'>Ant</a>.</td>
    </tr>
    </table>
    <hr size="1"/>
</xsl:template>

<xsl:template match="testsuite" mode="header">
    <tr valign="top">
        <th width="80%">Name</th>
        <th>Tests</th>
        <th>Errors</th>
        <th>Failures</th>
        <th nowrap="nowrap">Time(s)</th>
    </tr>
</xsl:template>

<!-- class header -->
<xsl:template name="testsuite.test.header">
    <tr valign="top">
        <th width="80%">Name</th>
        <th>Tests</th>
        <th>Errors</th>
        <th>Failures</th>
        <th nowrap="nowrap">Time(s)</th>
    </tr>
</xsl:template>

<!-- method header -->
<xsl:template name="testcase.test.header">
    <tr valign="top">
        <th>Name</th>
        <th>Status</th>
        <th width="80%">Type</th>
        <th nowrap="nowrap">Time(s)</th>
    </tr>
</xsl:template>


<!-- class information -->
<xsl:template match="testsuite" mode="print.test">
    <tr valign="top">
        <!-- set a nice color depending if there is an error/failure -->
        <xsl:attribute name="class">
            <xsl:choose>
                <xsl:when test="@failures[.&gt; 0]">Failure</xsl:when>
                <xsl:when test="@errors[.&gt; 0]">Error</xsl:when>
            </xsl:choose>
        </xsl:attribute>

        <!-- print testsuite information -->
        <td><a href="#{@name}"><xsl:value-of select="@name"/></a></td>
        <td><xsl:value-of select="@tests"/></td>
        <td><xsl:value-of select="@errors"/></td>
        <td><xsl:value-of select="@failures"/></td>
        <td>
            <xsl:call-template name="display-time">
                <xsl:with-param name="value" select="@time"/>
            </xsl:call-template>
        </td>
    </tr>
</xsl:template>

<xsl:template match="testcase" mode="print.test">
    <tr valign="top">
        <xsl:attribute name="class">
            <xsl:choose>
                <xsl:when test="failure | error">Error</xsl:when>
            </xsl:choose>
        </xsl:attribute>
        <td><xsl:value-of select="@name"/></td>
        <xsl:choose>
            <xsl:when test="failure">
                <td>Failure</td>
                <td><xsl:apply-templates select="failure"/></td>
            </xsl:when>
            <xsl:when test="error">
                <td>Error</td>
                <td><xsl:apply-templates select="error"/></td>
            </xsl:when>
            <xsl:otherwise>
                <td>Success</td>
                <td></td>
            </xsl:otherwise>
        </xsl:choose>
        <td>
            <xsl:call-template name="display-time">
                <xsl:with-param name="value" select="@time"/>
            </xsl:call-template>
        </td>
    </tr>
</xsl:template>


<xsl:template match="failure">
    <xsl:call-template name="display-failures"/>
</xsl:template>

<xsl:template match="error">
    <xsl:call-template name="display-failures"/>
</xsl:template>

<!-- Style for the error and failure in the tescase template -->
<xsl:template name="display-failures">
    <xsl:choose>
        <xsl:when test="not(@message)">N/A</xsl:when>
        <xsl:otherwise>
            <xsl:value-of select="@message"/>
        </xsl:otherwise>
    </xsl:choose>
    <!-- display the stacktrace -->
    <code>
        <br/><br/>
        <xsl:call-template name="br-replace">
            <xsl:with-param name="word" select="."/>
        </xsl:call-template>
    </code>
    <!-- the later is better but might be problematic for non-21" monitors... -->
    <!--pre><xsl:value-of select="."/></pre-->
</xsl:template>

<xsl:template name="JS-escape">
    <xsl:param name="string"/>
    <xsl:param name="tmp1" select="stringutils:replace(string($string),'\','\\')"/>
    <xsl:param name="tmp2" select="stringutils:replace(string($tmp1),&quot;'&quot;,&quot;\&apos;&quot;)"/>
    <xsl:value-of select="$tmp2"/>
</xsl:template>


<!--
    template that will convert a carriage return into a br tag
    @param word the text from which to convert CR to BR tag
-->
<xsl:template name="br-replace">
    <xsl:param name="word"/>
    <xsl:param name="br"><br/></xsl:param>
    <xsl:value-of select='stringutils:replace(string($word),"&#xA;",$br)'/>
</xsl:template>

<xsl:template name="display-time">
    <xsl:param name="value"/>
    <xsl:value-of select="format-number($value,'0.000')"/>
</xsl:template>

<xsl:template name="display-percent">
    <xsl:param name="value"/>
    <xsl:value-of select="format-number($value,'0.00%')"/>
</xsl:template>

</xsl:stylesheet>

