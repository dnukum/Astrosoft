<?xml version="1.0" encoding="utf-8"?>
<xsl:stylesheet version="1.1" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">	

<xsl:import href="page-layouts.xsl" />
<xsl:import href="compactibility-info-template.xsl"/>
<xsl:import href="compactibility-chart-template.xsl"/>
<xsl:import href="kuta-template.xsl"/>
<xsl:import href="dosha-template.xsl"/>
<xsl:import href="info-template.xsl"/>
<xsl:import href="chart-template.xsl"/>
<xsl:import href="header-footer-template.xsl" />
<xsl:import href="table-header-row-template.xsl" />
<xsl:import href="table-row-template.xsl" />


	<xsl:output method="xml" version="1.0" omit-xml-declaration="no" indent="yes"/>
	
	<xsl:template match="Compatibility | Compactibility">

		<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">

			<xsl:call-template name="page-layouts" />
			
			<fo:page-sequence master-reference="SimplePage">
			
				<xsl:call-template name="header-footer" />

				<fo:flow flow-name="xsl-region-body">
					
					<fo:block>
					
							
							<fo:table width="500pt" table-layout="fixed" >
								<fo:table-body> <fo:table-row> <fo:table-cell padding-top="5" padding-after="5">
									<fo:block text-align="center">
							
											<xsl:apply-templates select="Boy/Horoscope-Information"  > 
												<xsl:with-param name="title"  select="'Boy'"/>
											</xsl:apply-templates>
											
									</fo:block>
								</fo:table-cell>
								<fo:table-cell padding-top="5" padding-after="5">
									<fo:block text-align="center">
									
											<xsl:apply-templates select="Girl/Horoscope-Information"  > 
												<xsl:with-param name="title"  select="'Girl'"/>
											</xsl:apply-templates>
											
									</fo:block>
								</fo:table-cell>
								</fo:table-row></fo:table-body>
							</fo:table>
							
							<xsl:apply-templates select="Kuta-Analysis" /> 
							
					</fo:block>
					
				</fo:flow>

			</fo:page-sequence>
			
			<xsl:if test="@hasHoroscope = 'true'" >
			
				<fo:page-sequence master-reference="SimplePage">
				
					<xsl:call-template name="header-footer" />
	
					<fo:flow flow-name="xsl-region-body">
						
						<fo:block >
							
							<xsl:call-template name="compactibility-chart-template" />
	
						</fo:block>
	
					</fo:flow>
	
				</fo:page-sequence>
				
				<fo:page-sequence master-reference="SimplePage">
				
					<xsl:call-template name="header-footer" />
	
					<fo:flow flow-name="xsl-region-body">
						
						<fo:block >
							
							<xsl:apply-templates select="Dosha-Analysis" /> 
	
						</fo:block>
	
					</fo:flow>
	
				</fo:page-sequence>
						
			</xsl:if>

			<xsl:if test="Note-Line">
				<fo:page-sequence master-reference="SimplePage">
					<xsl:call-template name="header-footer" />
					<fo:flow flow-name="xsl-region-body">
						<fo:block font-size="14pt" font-family="Helvetica" font-weight="bold" space-before="10pt" space-after="12pt" color="#000080" border-bottom="1pt solid #000080" padding-bottom="4pt">
							Notes &amp; Observations
						</fo:block>
						<xsl:for-each select="Note-Line">
							<fo:block font-size="10pt" font-family="Helvetica" line-height="15pt" space-after="8pt">
								<xsl:value-of select="." />
							</fo:block>
						</xsl:for-each>
					</fo:flow>
				</fo:page-sequence>
			</xsl:if>

		</fo:root>
	</xsl:template>
 
</xsl:stylesheet>