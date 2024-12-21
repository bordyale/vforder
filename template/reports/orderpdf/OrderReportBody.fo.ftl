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
    <#if orderHeader?has_content>
        <fo:table table-layout="fixed" border-spacing="3pt" border-width="1px" border-style="solid" width="95%">
            <fo:table-column column-width="70%"/>
            <fo:table-column column-width="25%" />
           
            <fo:table-header>
                <fo:table-row>
                    <fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid">
                        <fo:block font-weight="bold">${uiLabelMap.OrderProduct}</fo:block>
                    </fo:table-cell>
                   
                    <fo:table-cell text-align="right" padding="1mm" border-width="0.3mm" border-style="solid">
                        <fo:block font-weight="bold">${uiLabelMap.OrderQuantity}</fo:block>
                    </fo:table-cell>
                  
                </fo:table-row>
            </fo:table-header>
            <fo:table-body>
                <#list orderItemList as orderItem>
                    <#assign orderItemType = orderItem.getRelatedOne("OrderItemType", false)!>
                    <#assign productId = orderItem.productId!>
                    <#assign remainingQuantity = (orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0))>
                    <#assign itemAdjustment = Static["org.apache.ofbiz.order.order.OrderReadHelper"].getOrderItemAdjustmentsTotal(orderItem, orderAdjustments, true, false, false)>
                    <#assign internalImageUrl = Static["org.apache.ofbiz.product.imagemanagement.ImageManagementHelper"].getInternalImageUrl(request, productId!)!>
                    <fo:table-row>
                        <fo:table-cell padding="1mm" border-width="0.3mm" border-style="solid">
                            <fo:block>
                                <#if orderItem.supplierProductId?has_content>
                                   <!-- ${orderItem.supplierProductId} - --> ${orderItem.itemDescription!}
                                <#elseif productId??>
                                    ${orderItem.productId?default("N/A")} - ${orderItem.itemDescription!}
                                <#elseif orderItemType??>
                                    ${orderItemType.get("description",locale)} - ${orderItem.itemDescription!}
                                <#else>
                                    ${orderItem.itemDescription!}
                                </#if>
                            </fo:block>
                        </fo:table-cell>
                       
                        <fo:table-cell text-align="right" padding="1mm" border-width="0.3mm" border-style="solid">
                            <fo:block>${remainingQuantity}</fo:block>
                        </fo:table-cell>
                       
                    </fo:table-row>
                    
                </#list>
                
                <#-- summary of order amounts -->
                
                
                
                
                
                <#-- notes -->
                
            </fo:table-body>
        </fo:table>
    </#if>
</#escape>
