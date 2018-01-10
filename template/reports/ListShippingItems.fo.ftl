<#--
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
-->

<#escape x as x?xml>
<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
    <fo:layout-master-set>
      <fo:simple-page-master master-name="main" page-height="11in" page-width="8.5in"
        margin-top="0.5in" margin-bottom="1in" margin-left=".5in" margin-right="1in">
          <fo:region-body margin-top="1in"/>
          <fo:region-before extent="1in"/>
          <fo:region-after extent="1in"/>
      </fo:simple-page-master>
    </fo:layout-master-set>


  
  <fo:page-sequence master-reference="main">
  	<fo:static-content flow-name="xsl-region-before">
			<fo:block font-size="26pt" font-weight="bold" text-align="center">
				${uiLabelMap.ShippingDate} - ${shipment.estimatedShipDate}
			</fo:block>
	</fo:static-content>
	<fo:static-content flow-name="xsl-region-after">
			<fo:block font-size="26pt" font-weight="bold" text-align="center">
				${uiLabelMap.BoxTotal} - ${totBoxNumber}
				${uiLabelMap.PalletTotal} - ${totPalletNumber}
			</fo:block>
	</fo:static-content>
	<fo:flow flow-name="xsl-region-body" font-family="Helvetica">
            <fo:block>
              <fo:table width="100%" table-layout="fixed">
                
                <fo:table-header text-align="center" background-color="silver">
					<fo:table-row>
						<fo:table-cell padding="1mm" border-width="1mm" border-style="solid">
							<fo:block font-weight="bold">${uiLabelMap.ProductName}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="1mm" border-style="solid">
							<fo:block font-weight="bold">${uiLabelMap.OrderPallettization}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="1mm" border-style="solid">
							<fo:block font-weight="bold">${uiLabelMap.OrderIsBoxOrPallet}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="1mm" border-style="solid">
							<fo:block font-weight="bold">${uiLabelMap.boxNumber}</fo:block>
						</fo:table-cell>
						<fo:table-cell padding="1mm" border-width="1mm" border-style="solid">
							<fo:block font-weight="bold">${uiLabelMap.Quantity}</fo:block>
						</fo:table-cell>
					</fo:table-row>
				</fo:table-header>
                <fo:table-body>
                <#list listIt as item>
                  <fo:table-row>
                    <fo:table-cell border-style="solid">
                      <fo:block>
                        ${item.productName}
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell border-style="solid">
                      <fo:block>
                        ${item.pallet}
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell border-style="solid">
                      <fo:block>
                        ${item.isBoxOrPallet}
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell border-style="solid">
                      <fo:block>
                        ${item.boxNumber}
                      </fo:block>
                    </fo:table-cell>
                    <fo:table-cell border-style="solid">
                      <fo:block>
                        ${item.quantity}
                      </fo:block>
                    </fo:table-cell>
                    
                  </fo:table-row>
                 </#list>
                </fo:table-body>
              </fo:table>
            </fo:block>
	</fo:flow>

  </fo:page-sequence>


</fo:root>
</#escape>