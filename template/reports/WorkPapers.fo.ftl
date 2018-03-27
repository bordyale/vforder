
<#--
Licensed to the Apache Software Foundation (ASF) under one
or more
contributor license agreements. See the NOTICE file
distributed with this
work for additional information
regarding copyright ownership. The ASF
licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the
License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable
law or agreed to in writing,
software distributed under the License is
distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific
language governing permissions and limitations
under the License.
-->

<#escape x as x?xml>
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
	<fo:layout-master-set>
		<fo:simple-page-master master-name="main"
			page-height="11in" page-width="8.5in" margin-top="0.5in"
			margin-bottom="1in" margin-left=".5in" margin-right="1in">
			<fo:region-body margin-top="1in" />
			<fo:region-before extent="1in" />
			<fo:region-after extent="1in" />
		</fo:simple-page-master>
	</fo:layout-master-set>

	<#list listIt as item>
	<fo:page-sequence master-reference="main">
		<fo:static-content flow-name="xsl-region-before">

			<fo:table width="100%" table-layout="fixed">
				<fo:table-column border-width="1px" border-style="solid" />
				<fo:table-column border-width="1px" border-style="solid" />
				<fo:table-column border-width="1px" border-style="solid" />
				<fo:table-body>
					<fo:table-row>
						<fo:table-cell>
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell>
							<fo:block>

							</fo:block>
						</fo:table-cell>
						<fo:table-cell border-style="solid" border-width="0.1mm">
							<fo:block>
								${uiLabelMap.Datum}
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<fo:table-row>
						<fo:table-cell>
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell>
							<fo:block text-align="center">
								${uiLabelMap.WorkPapersTitle}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell border-style="solid" border-width="0.1mm">
							<fo:block>
								${uiLabelMap.Datum}
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<fo:table-row>
						<fo:table-cell>
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell>
							<fo:block>

							</fo:block>
						</fo:table-cell>
						<fo:table-cell border-style="solid" border-width="0.1mm">
							<fo:block>
								${uiLabelMap.Datum}
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
				</fo:table-body>
			</fo:table>
			





		</fo:static-content>
		<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
		
			<fo:table width="100%" table-layout="fixed">
				<fo:table-column border-width="1px" border-style="solid" />
				<fo:table-column border-width="1px" border-style="solid" />
				<fo:table-column border-width="1px" border-style="solid" />
				<fo:table-body>
					<fo:table-row>
						<fo:table-cell>
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell>
							<fo:block>

							</fo:block>
						</fo:table-cell>
						<fo:table-cell border-style="solid" border-width="0.1mm">
							<fo:block>
								${uiLabelMap.Datum}
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<fo:table-row>
						<fo:table-cell>
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell>
							<fo:block text-align="center">
								${uiLabelMap.WorkPapersTitle}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell border-style="solid" border-width="0.1mm">
							<fo:block>
								${uiLabelMap.Datum}
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					<fo:table-row>
						<fo:table-cell>
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell>
							<fo:block>

							</fo:block>
						</fo:table-cell>
						<fo:table-cell border-style="solid" border-width="0.1mm">
							<fo:block>
								${uiLabelMap.Datum}
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
				</fo:table-body>
			</fo:table>
			
			
			<fo:table space-before="0.5cm" width="100%" table-layout="fixed">
				<fo:table-column column-width="50%" border-width="1px" border-style="solid" />
				<fo:table-column border-width="1px" border-style="solid" />
				<fo:table-body>
					<fo:table-row>
						<fo:table-cell text-align="center">
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						
						<fo:table-cell text-align="center">
							<fo:block>
								${uiLabelMap.Datum}
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					
				</fo:table-body>
			</fo:table>
			<fo:table width="100%" table-layout="fixed">
				<fo:table-column column-width="23%" border-width="1px" border-style="solid" />
				<fo:table-column column-width="12%" border-width="1px" border-style="solid" />
				<fo:table-column column-width="15%" border-width="1px" border-style="solid" />
				<fo:table-column column-width="14%" border-width="1px" border-style="solid" />
				<fo:table-column column-width="12%" border-width="1px" border-style="solid" />
				<fo:table-column column-width="12%" border-width="1px" border-style="solid" />
				<fo:table-column column-width="12%" border-width="1px" border-style="solid" />
				<fo:table-body>
					<fo:table-row>
						<fo:table-cell text-align="center">
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell text-align="center">
							<fo:block>
								${uiLabelMap.Datum}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell text-align="center">
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell text-align="center">
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell text-align="center">
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell text-align="center">
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
						<fo:table-cell text-align="center">
							<fo:block>
								${uiLabelMap.Company}
							</fo:block>
						</fo:table-cell>
					</fo:table-row>
					
				</fo:table-body>
			</fo:table>



			<fo:block text-align="left" font-weight="bold" font-size="60pt">
				${item.internalName} - ${item.components}
			</fo:block>





		</fo:flow>

	</fo:page-sequence>
	</#list>


</fo:root>
</#escape>
