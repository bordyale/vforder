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

<#if orderHeader?has_content>
    <div class="screenlet">
        <div class="screenlet-title-bar">
            <ul>
                <li class="h3">&nbsp;${uiLabelMap.OrderOrderItems}</li>
            </ul>
            <br class="clear" />
        </div>
        <div class="screenlet-body">
            <table class="order-items basic-table" cellspacing='0'>
                <tr valign="bottom" class="header-row">
                    <td width="30%">${uiLabelMap.ProductProduct}</td>
                    <!--<td width="33%">${uiLabelMap.CommonStatus}</td>-->
                    <td width="5%">${uiLabelMap.OrderQuantity}</td>
                    <!--<td width="10%" align="right">${uiLabelMap.OrderUnitList}</td>
                    <td width="10%" align="right">${uiLabelMap.OrderAdjustments}</td>
                    <td width="10%" align="right">${uiLabelMap.OrderSubTotal}</td>-->
                    <td width="2%">&nbsp;</td>
                </tr>
                <#if !orderItemList?has_content>
                    <tr>
                        <td colspan="7">
                            <font color="red">${uiLabelMap.checkhelper_sales_order_lines_lookup_failed}</font>
                        </td>
                    </tr>
                <#else>
                    <#assign itemClass = "2">
                    <#list orderItemList as orderItem>
                        <#assign orderItemContentWrapper = Static["org.apache.ofbiz.order.order.OrderContentWrapper"].makeOrderContentWrapper(orderItem, request)>
                        <#assign orderItemShipGrpInvResList = orderReadHelper.getOrderItemShipGrpInvResList(orderItem)>
                        <#if orderHeader.orderTypeId == "SALES_ORDER"><#assign pickedQty = orderReadHelper.getItemPickedQuantityBd(orderItem)></#if>
                        <tr<#if itemClass == "1"> class="alternate-row"</#if>>
                            <#assign orderItemType = orderItem.getRelatedOne("OrderItemType", false)!>
                            <#assign productId = orderItem.productId!>
                            <#if productId?? && productId == "shoppingcart.CommentLine">
                                <td colspan="7" valign="top" class="label"> &gt;&gt; ${orderItem.itemDescription}</td>
                            <#else>
                                <td colspan="1">
                                    <div class="order-item-description">
                                        <#if orderItem.supplierProductId?has_content>
                                            ${orderItem.supplierProductId} - ${orderItem.itemDescription!}
                                        <#elseif productId??>
                                            ${orderItem.productId?default("N/A")} - ${orderItem.itemDescription!}
                                            <#if (product.salesDiscontinuationDate)?? && Static["org.apache.ofbiz.base.util.UtilDateTime"].nowTimestamp().after(product.salesDiscontinuationDate)>
                                                <br />
                                                <span style="color: red;">${uiLabelMap.OrderItemDiscontinued}: ${Static["org.apache.ofbiz.base.util.UtilFormatOut"].formatDateTime(product.salesDiscontinuationDate, "", locale, timeZone)!}</span>
                                            </#if>
                                        <#elseif orderItemType??>
                                            ${orderItemType.description} - ${orderItem.itemDescription!}
                                        <#else>
                                            ${orderItem.itemDescription!}
                                        </#if>
                                        <#assign orderItemAttributes = orderItem.getRelated("OrderItemAttribute", null, null, false)/>
                                        <#if orderItemAttributes?has_content>
                                            <ul>
                                            <#list orderItemAttributes as orderItemAttribute>
                                                <li>
                                                    ${orderItemAttribute.attrName} : ${orderItemAttribute.attrValue}
                                                </li>
                                            </#list>
                                            </ul>
                                        </#if>
                                    </div>
                                    <!--<div style="float:right;">
                                        <#assign downloadContents = delegator.findByAnd("OrderItemAndProductContentInfo", {
                                                                                        "orderId" : orderItem.orderId, 
                                                                                        "orderItemSeqId" : orderItem.orderItemSeqId, 
                                                                                        "productContentTypeId" : "DIGITAL_DOWNLOAD", 
                                                                                        "statusId" : "ITEM_COMPLETED"},
                                                                                        null,
                                                                                        false)/>
                                        <#if downloadContents?has_content>
                                            <#list downloadContents as downloadContent>
                                                <a href="/content/control/ViewSimpleContent?contentId=${downloadContent.contentId}" class="buttontext" target="_blank">${uiLabelMap.ContentDownload}</a>&nbsp;
                                            </#list>
                                        </#if>
                                        <a href="/catalog/control/EditProduct?productId=${productId}${StringUtil.wrapString(externalKeyParam)}" class="buttontext" target="_blank">${uiLabelMap.ProductCatalog}</a>
                                        <a href="/ecommerce/control/product?product_id=${productId}" class="buttontext" target="_blank">${uiLabelMap.OrderEcommerce}</a>
                                        <#if orderItemContentWrapper.get("IMAGE_URL", "url")?has_content>
                                            <a href="<@ofbizUrl>viewimage?orderId=${orderId}&amp;orderItemSeqId=${orderItem.orderItemSeqId}&amp;orderContentTypeId=IMAGE_URL</@ofbizUrl>"
                                               target="_orderImage" class="buttontext">${uiLabelMap.OrderViewImage}</a>
                                        </#if>
                                    </div>-->
                                </td>
                                <td>${orderItem.quantity?default(0)?string.number}</td>
                            </#if>
                        </tr>
                         <!--<tr<#if itemClass == "1"> class="alternate-row"</#if>>
                            <#if productId?? && productId == "shoppingcart.CommentLine">
                                <td colspan="7" valign="top" class="label"> &gt;&gt; ${orderItem.itemDescription}</td>
                            <#else>
                                <td valign="top">
                                    <#if productId?has_content>
                                        <#assign product = orderItem.getRelatedOne("Product", true)>
                                    </#if>
                                   <#if productId??>
                                        <#-- INVENTORY -->
                                        <#if (orderHeader.statusId != "ORDER_COMPLETED") && availableToPromiseMap?? && quantityOnHandMap?? && availableToPromiseMap.get(productId)?? && quantityOnHandMap.get(productId)??>
                                            <#assign quantityToProduce = 0>
                                            <#assign atpQuantity = availableToPromiseMap.get(productId)?default(0)>
                                            <#assign qohQuantity = quantityOnHandMap.get(productId)?default(0)>
                                            <#assign mktgPkgATP = mktgPkgATPMap.get(productId)?default(0)>
                                            <#assign mktgPkgQOH = mktgPkgQOHMap.get(productId)?default(0)>
                                            <#assign requiredQuantity = requiredProductQuantityMap.get(productId)?default(0)>
                                            <#assign onOrderQuantity = onOrderProductQuantityMap.get(productId)?default(0)>
                                            <#assign inProductionQuantity = productionProductQuantityMap.get(productId)?default(0)>
                                            <#assign unplannedQuantity = requiredQuantity - qohQuantity - inProductionQuantity - onOrderQuantity - mktgPkgQOH>
                                            <#if unplannedQuantity < 0><#assign unplannedQuantity = 0></#if>
                                            <div class="screenlet order-item-inventory">
                                                <div class="screenlet-body">
                                                    <table cellspacing="0" cellpadding="0" border="0">
                                                        <tr>
                                                            <td style="text-align: right; padding-bottom: 10px;">
                                                                <a class="buttontext"
                                                                   href="/catalog/control/EditProductInventoryItems?productId=${productId}&amp;showAllFacilities=Y${StringUtil.wrapString(externalKeyParam)}"
                                                                   target="_blank">${uiLabelMap.ProductInventory}</a>
                                                            </td>
                                                            <td>&nbsp;</td>
                                                        </tr>
                                                        <tr>
                                                            <td>${uiLabelMap.OrderRequiredForSO}</td>
                                                            <td style="padding-left: 15px; text-align: left;">${requiredQuantity}</td>
                                                        </tr>
                                                        <#if availableToPromiseByFacilityMap?? && quantityOnHandByFacilityMap?? && quantityOnHandByFacilityMap.get(productId)?? && availableToPromiseByFacilityMap.get(productId)??>
                                                            <#assign atpQuantityByFacility = availableToPromiseByFacilityMap.get(productId)?default(0)>
                                                            <#assign qohQuantityByFacility = quantityOnHandByFacilityMap.get(productId)?default(0)>
                                                            <tr>
                                                                <td>
                                                                    ${uiLabelMap.ProductInInventory} [${facility.facilityName!}] ${uiLabelMap.ProductQoh}
                                                                </td>
                                                                <td style="padding-left: 15px; text-align: left;">
                                                                    ${qohQuantityByFacility} (${uiLabelMap.ProductAtp}: ${atpQuantityByFacility})
                                                                </td>
                                                            </tr>
                                                        </#if>
                                                        <tr>
                                                            <td>
                                                                ${uiLabelMap.ProductInInventory} [${uiLabelMap.CommonAll} ${uiLabelMap.ProductFacilities}] ${uiLabelMap.ProductQoh}
                                                            </td>
                                                            <td style="padding-left: 15px; text-align: left;">
                                                                ${qohQuantity} (${uiLabelMap.ProductAtp}: ${atpQuantity})
                                                            </td>
                                                        </tr>
                                                        <#if (product?has_content) && (product.productTypeId?has_content) && Static["org.apache.ofbiz.entity.util.EntityTypeUtil"].hasParentType(delegator, "ProductType", "productTypeId", product.productTypeId, "parentTypeId", "MARKETING_PKG")>
                                                            <tr>
                                                                <td>${uiLabelMap.ProductMarketingPackageQOH}</td>
                                                                <td style="padding-left: 15px; text-align: left;">
                                                                    ${mktgPkgQOH} (${uiLabelMap.ProductAtp}: ${mktgPkgATP})
                                                                </td>
                                                            </tr>
                                                        </#if>
                                                        <tr>
                                                            <td>${uiLabelMap.OrderOnOrder}</td>
                                                            <td style="padding-left: 15px; text-align: left;">${onOrderQuantity}</td>
                                                        </tr>
                                                        <tr>
                                                            <td>${uiLabelMap.OrderInProduction}</td>
                                                            <td style="padding-left: 15px; text-align: left;">${inProductionQuantity}</td>
                                                        </tr>
                                                        <tr>
                                                            <td>${uiLabelMap.OrderUnplanned}</td>
                                                            <td style="padding-left: 15px; text-align: left;">${unplannedQuantity}</td>
                                                        </tr>
                                                    </table>
                                                </div>
                                            </div>
                                        </#if>
                                    </#if>
                                </td>
                                <#-- now show status details per line item -->
                                <#assign currentItemStatus = orderItem.getRelatedOne("StatusItem", false)>
                                <!--<td colspan="1" valign="top">
                                    <div class="screenlet order-item-status-list<#if currentItemStatus.statusCode?has_content> ${currentItemStatus.statusCode}</#if>">
                                        <div class="screenlet-body">
                                            <div class="current-status">
                                                <span class="label">${uiLabelMap.CommonCurrent}</span>&nbsp;${currentItemStatus.get("description",locale)?default(currentItemStatus.statusId)}
                                            </div>
                                            <#if ("ITEM_CREATED" == (currentItemStatus.statusId) && "ORDER_APPROVED" == (orderHeader.statusId)) && security.hasEntityPermission("ORDERMGR", "_UPDATE", session)>
                                                <div>
                                                    <a href="javascript:document.OrderApproveOrderItem_${orderItem.orderItemSeqId?default("")}.submit()" class="buttontext">${uiLabelMap.OrderApproveItem}</a>
                                                    <form name="OrderApproveOrderItem_${orderItem.orderItemSeqId?default("")}" method="post" action="<@ofbizUrl>changeOrderItemStatus</@ofbizUrl>">
                                                        <input type="hidden" name="statusId" value="ITEM_APPROVED"/>
                                                        <input type="hidden" name="orderId" value="${orderId!}"/>
                                                        <input type="hidden" name="orderItemSeqId" value="${orderItem.orderItemSeqId!}"/>
                                                    </form>
                                                </div>
                                            </#if>
                                            <#assign orderItemStatuses = orderReadHelper.getOrderItemStatuses(orderItem)>
                                            <#list orderItemStatuses as orderItemStatus>
                                                <#assign loopStatusItem = orderItemStatus.getRelatedOne("StatusItem", false)>
                                                <#if orderItemStatus.statusDatetime?has_content>${Static["org.apache.ofbiz.base.util.UtilFormatOut"].formatDateTime(orderItemStatus.statusDatetime, "", locale, timeZone)!}&nbsp;&nbsp;</#if>${loopStatusItem.get("description",locale)?default(orderItemStatus.statusId)}
                                            </#list>
                                        </div>
                                    </div>
                                    <#assign returns = orderItem.getRelated("ReturnItem", null, null, false)!>
                                    <#if returns?has_content>
                                        <#list returns as returnItem>
                                            <#assign returnHeader = returnItem.getRelatedOne("ReturnHeader", false)>
                                            <#if returnHeader.statusId != "RETURN_CANCELLED">
                                                <font color="red">${uiLabelMap.OrderReturned}</font>
                                                ${uiLabelMap.CommonNbr}<a href="<@ofbizUrl>returnMain?returnId=${returnItem.returnId}</@ofbizUrl>" class="buttontext">${returnItem.returnId}</a>
                                            </#if>
                                        </#list>
                                    </#if>
                                </td>-->
                                <#-- QUANTITY -->
                                 
                                <!--
                                <td align="right" valign="top" nowrap="nowrap">
                                    <div class="screenlet order-item-quantity">
                                        <div class="screenlet-body">
                                            <table>
                                                <tr valign="top">
                                                    <#assign shippedQuantity = orderReadHelper.getItemShippedQuantity(orderItem)>
                                                    <#assign shipmentReceipts = delegator.findByAnd("ShipmentReceipt", {"orderId" : orderHeader.getString("orderId"), "orderItemSeqId" : orderItem.orderItemSeqId}, null, false)/>
                                                    <#assign totalReceived = 0.0>
                                                    <#if shipmentReceipts?? && shipmentReceipts?has_content>
                                                        <#list shipmentReceipts as shipmentReceipt>
                                                            <#if shipmentReceipt.quantityAccepted?? && shipmentReceipt.quantityAccepted?has_content>
                                                                <#assign  quantityAccepted = shipmentReceipt.quantityAccepted>
                                                                <#assign totalReceived = quantityAccepted + totalReceived>
                                                            </#if>
                                                            <#if shipmentReceipt.quantityRejected?? && shipmentReceipt.quantityRejected?has_content>
                                                                <#assign  quantityRejected = shipmentReceipt.quantityRejected>
                                                                <#assign totalReceived = quantityRejected + totalReceived>
                                                            </#if>
                                                        </#list>
                                                    </#if>
                                                    <#if product.productTypeId == "SERVICE" && currentItemStatus.statusId == "ITEM_COMPLETED">
                                                        <#assign shippedQuantity = orderItem.quantity?default(0)/>
                                                        <#assign totalReceived = orderItem.quantity?default(0)>
                                                    </#if>
                                                    <#if orderHeader.orderTypeId == "PURCHASE_ORDER">
                                                        <#assign remainingQuantity = ((orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0)) - totalReceived?double)>
                                                    <#else>
                                                        <#assign remainingQuantity = ((orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0)) - shippedQuantity?double)>
                                                    </#if>
                                                    <#-- to compute shortfall amount, sum up the orderItemShipGrpInvRes.quantityNotAvailable -->
                                                    <#assign shortfalledQuantity = 0/>
                                                    <#list orderItemShipGrpInvResList as orderItemShipGrpInvRes>
                                                        <#if (orderItemShipGrpInvRes.quantityNotAvailable?has_content && orderItemShipGrpInvRes.quantityNotAvailable > 0)>
                                                            <#assign shortfalledQuantity = shortfalledQuantity + orderItemShipGrpInvRes.quantityNotAvailable/>
                                                        </#if>
                                                    </#list>
                                                    <td><b>${uiLabelMap.OrderOrdered}</b></td>
                                                    <td>${orderItem.quantity?default(0)?string.number}</td>
                                                    <td><b>${uiLabelMap.OrderShipRequest}</b></td>
                                                    <td>${orderReadHelper.getItemReservedQuantity(orderItem)}</td>
                                                </tr>
                                                <tr valign="top">
                                                    <td><b>${uiLabelMap.OrderCancelled}</b></td>
                                                    <td>${orderItem.cancelQuantity?default(0)?string.number}</td>
                                                    <#if orderHeader.orderTypeId == "SALES_ORDER">
                                                        <#if pickedQty gt 0 && orderHeader.statusId == "ORDER_APPROVED">
                                                            <td><font color="red"><b>${uiLabelMap.OrderQtyPicked}</b></font></td>
                                                            <td><font color="red">${pickedQty?default(0)?string.number}</font></td>
                                                        <#else>
                                                            <td><b>${uiLabelMap.OrderQtyPicked}</b></td>
                                                            <td>${pickedQty?default(0)?string.number}</td>
                                                        </#if>
                                                    <#else>
                                                        <td>&nbsp;</td>
                                                        <td>&nbsp;</td>
                                                    </#if>
                                                </tr>
                                                <tr valign="top">
                                                    <td><b>${uiLabelMap.OrderRemaining}</b></td>
                                                    <td>${remainingQuantity}</td>
                                                    <#if orderHeader.orderTypeId == "PURCHASE_ORDER">
                                                        <td><b>${uiLabelMap.OrderPlannedInReceive}</b></td>
                                                        <td>${totalReceived}</td>
                                                    <#else>
                                                        <td><b>${uiLabelMap.OrderQtyShipped}</b></td>
                                                        <td>${shippedQuantity}</td>
                                                    </#if>
                                                </tr>
                                                <tr valign="top">
                                                    <td><b>${uiLabelMap.OrderShortfalled}</b></td>
                                                    <td>${shortfalledQuantity}</td>
                                                    <td><b>${uiLabelMap.OrderOutstanding}</b></td>
                                                    <td>
                                                        <#-- Make sure digital goods without shipments don't always remainn "outstanding": if item is completed, it must have no outstanding quantity.  -->
                                                        <#if (orderItem.statusId?has_content) && (orderItem.statusId == "ITEM_COMPLETED")>
                                                            0
                                                        <#elseif orderHeader.orderTypeId == "PURCHASE_ORDER">
                                                            ${(orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0)) - totalReceived?double}
                                                        <#elseif orderHeader.orderTypeId == "SALES_ORDER">
                                                            ${(orderItem.quantity?default(0) - orderItem.cancelQuantity?default(0)) - shippedQuantity?double}
                                                        </#if>
                                                    </td>
                                                </tr>
                                                <tr valign="top">
                                                    <td><b>${uiLabelMap.OrderInvoiced}</b></td>
                                                    <td>${orderReadHelper.getOrderItemInvoicedQuantity(orderItem)}</td>
                                                    <td><b>${uiLabelMap.OrderReturned}</b></td>
                                                    <td>${returnQuantityMap.get(orderItem.orderItemSeqId)?default(0)}</td>
                                                </tr>
                                            </table>
                                        </div>
                                    </div>
                                </td>-->
                                
                                <td>&nbsp;</td>
                            </#if>
                        </tr>-->
                       
                        
                        <#-- now show adjustment details per line item -->
                        
                        
                    
                       
                        
                       
                        
                       
                       
                        
                     
                        
                        <#if orderItem.comments?has_content>
                          <tr<#if itemClass == "1"> class="alternate-row"</#if>>
                            <td>&nbsp;</td>
                            <td>
                              <div class= "screenlet">
                                <div class = "screenlet-body">
                                  <table>
                                    <tr>
                                      <td align="right">
                                        <span class="label">${uiLabelMap.CommonComments}</span>
                                      </td>
                                      <td align="">
                                        <span class="label">${uiLabelMap.CommonCurrent}:</span>&nbsp;${orderItem.comments}
                                        <#assign orderItemSeqId = orderItem.orderItemSeqId!>
                                        <#if comments?has_content>
                                          <hr/>
                                          <#list comments as comm>
                                            <#if comm.orderItemSeqId?has_content && orderItemSeqId?has_content && comm.orderItemSeqId == orderItemSeqId>
                                              <#if comm.changeComments?has_content>
                                                <div>
                                                ${(comm.changeComments)!}
                                                &nbsp;
                                                <#if comm.changeDatetime?has_content>${Static["org.apache.ofbiz.base.util.UtilFormatOut"].formatDateTime(comm.changeDatetime, "", locale, timeZone)?default("0000-00-00 00:00:00")}</#if>  &nbsp;  ${uiLabelMap.CommonBy} -  [${(comm.changeUserLogin)!}]
                                                </div>
                                              </#if>
                                            </#if>
                                          </#list>
                                        </#if>
                                      </td>
                                    </tr>
                                  </table>
                                </div>
                              </div>
                            </td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                            <td></td>
                          </tr>
                        </#if>
                        <#if itemClass == "2">
                            <#assign itemClass = "1">
                        <#else>
                            <#assign itemClass = "2">
                        </#if>
                    </#list>
                </#if>
                <tr><td colspan="7"><hr /></td></tr>
                
                
            </table>
        </div>
    </div>
</#if>
