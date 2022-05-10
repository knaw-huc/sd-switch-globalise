<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:js="http://www.w3.org/2005/xpath-functions" xmlns:xs="http://www.w3.org/2001/XMLSchema"
    exclude-result-prefixes="xs" version="2.0">

    <xsl:output method="html"/>

    <xsl:template match="/">
        <html>
            <head>
                <title><xsl:value-of select="js:map/js:string[@key='searchable']" /></title>
            </head>
            <style>
                table,
                td,
                th {
                border: 1px solid;
                border-collapse: collapse;
                }
                td {
                padding: 0 1ex 0 1ex;
                }
            </style>
            <body>
                <table>
                    <tbody>
                        <tr><th>Veld</th><th>Waarde</th></tr>
                        <xsl:apply-templates/>
                    </tbody>
                </table>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="js:string">
        <tr>
            <td>
                <xsl:value-of select="@key"/>
            </td>
            <td>
                <xsl:value-of select="."/>
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="js:number">
        <tr>
            <td>
                <xsl:value-of select="@key"/>
            </td>
            <td>
                <xsl:value-of select="."/>
            </td>
        </tr>
    </xsl:template>

</xsl:stylesheet>
