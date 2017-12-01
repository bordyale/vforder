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

shippingId = request.getParameter("shippingId") ?: ""
orderId = request.getParameter("orderId") ?: ""



orderItems = select("orderId","orderItemSeqId","productId","quantity").from("OrderItem").where("orderId", orderId).cache(true).queryList()

shippingOrderItems = select("orderId","orderItemSeqId","productId","quantity").from("OrderItem").where("orderId", orderId).cache(true).queryList()


List<HashMap<String,Object>> hashMaps = new ArrayList<HashMap<String,Object>>()

for (GenericValue entry: orderItems){
	Map<String,Object> e = new HashMap<String,Object>()
	e.put("orderId",entry.get("orderId"))
	e.put("productId",entry.get("productId"))
	e.put("quantity",entry.get("quantity"))
	e.put("numero","45")
	hashMaps.add(e)
}



context.orderItems = hashMaps
