/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.ofbiz.entity.condition.EntityExpr
import org.apache.ofbiz.entity.condition.EntityFunction
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.condition.EntityFieldValue
import org.apache.ofbiz.entity.condition.EntityConditionList
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.util.EntityUtil
import java.sql.Timestamp

fromDate = parameters.fromDate
thruDate = parameters.thruDate

List searchCond = []
if (fromDate) {
	searchCond.add(EntityCondition.makeCondition("estimatedShipDate", EntityOperator.GREATER_THAN_EQUAL_TO, Timestamp.valueOf(fromDate)))
}
if (thruDate) {
	searchCond.add(EntityCondition.makeCondition("estimatedShipDate", EntityOperator.LESS_THAN_EQUAL_TO, Timestamp.valueOf(thruDate)))
}

orderItemShippingItem = select("estimatedShipDate","orderId","productId","productName","internalName","statusId","quantity","quantityShipped","quantityShippable").from("ShipmentsReport").where(searchCond).cache(false).queryList()

orderItemShippingItem = EntityUtil.orderBy(orderItemShippingItem,  ["estimatedShipDate"])
orderItemShippingItem = EntityUtil.orderBy(orderItemShippingItem,  ["productId"])

List<HashMap<String,Object>> hashMaps = new ArrayList<HashMap<String,Object>>()
for (GenericValue entry: orderItemShippingItem){
	Map<String,Object> e = new HashMap<String,Object>()
	e.put("estimatedShipDate",entry.get("estimatedShipDate"))
	e.put("orderId",entry.get("orderId"))
	e.put("productId",entry.get("productId"))
	e.put("productName",entry.get("productName"))
	BigDecimal quantity = entry.get("quantity")
	e.put("quantity",entry.get("quantity"))
	e.put("quantityShipped",entry.get("quantityShipped"))
	e.put("quantityShippable",entry.get("quantityShippable"))
	status = entry.get("statusId")
	if (!status.equals("ITEM_CANCELLED")){
		hashMaps.add(e)
	}
}

shippingWeight = select("estimatedShipDate","shipmentId","netWeight").from("ShippingWeight").where(searchCond).cache(false).queryList()

shippingWeight = EntityUtil.orderBy(shippingWeight,  ["estimatedShipDate"])

List<HashMap<String,Object>> shipWeights = new ArrayList<HashMap<String,Object>>()
for (GenericValue entry: shippingWeight){
	Map<String,Object> e = new HashMap<String,Object>()
	e.put("estimatedShipDate",entry.get("estimatedShipDate"))
	e.put("shipmentId",entry.get("shipmentId"))
	e.put("netWeight",entry.get("netWeight"))
	shipWeights.add(e)
}

extraShippedProducts = select("estimatedShipDate","shipmentId","productId","productName","internalName","quantity").from("ShippingItemView").where("orderId", null).cache(false).queryList()

extraShippedProducts = EntityUtil.orderBy(extraShippedProducts,  ["productId"])
extraShippedProducts = EntityUtil.orderBy(extraShippedProducts,  ["estimatedShipDate"])

List<HashMap<String,Object>> exShippedPr = new ArrayList<HashMap<String,Object>>()
for (GenericValue entry: extraShippedProducts){
	Map<String,Object> e = new HashMap<String,Object>()
	e.put("estimatedShipDate",entry.get("estimatedShipDate"))
	e.put("shipmentId",entry.get("shipmentId"))
	e.put("productId",entry.get("productId"))
	e.put("productName",entry.get("productName"))
	e.put("quantity",entry.get("quantity"))
	exShippedPr.add(e)
}


context.orderItems = hashMaps
context.shipWeights = shipWeights
context.exShippedPr = exShippedPr