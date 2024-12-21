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






orderItemShippingItem = select("orderId","statusId","orderHStatusId","orderItemSeqId","quantity","quantityShipped","productId","productName").from("OrderItemShippingItemView").cache(false).queryList()



List<HashMap<String,Object>> hashMaps = new ArrayList<HashMap<String,Object>>()


for (GenericValue entry: orderItemShippingItem){
	Map<String,Object> e = new HashMap<String,Object>()
	e.put("orderItemSeqId",entry.get("orderItemSeqId"))
	e.put("orderId",entry.get("orderId"))
	e.put("productId",entry.get("productId"))
	e.put("productName",entry.get("productName"))
	BigDecimal quantity = entry.get("quantity")
	e.put("quantity",entry.get("quantity"))
	BigDecimal quantityShipped = entry.get("quantityShipped")
	if (quantityShipped ==null){
		quantityShipped = BigDecimal.ZERO
	}

	e.put("quantityShipped",quantityShipped)
	e.put("quantityShippable",quantity.subtract(quantityShipped))
	status = entry.get("orderHStatusId")
	if (!quantity.equals(quantityShipped)){
		if (!status.equals("ORDER_CANCELLED")){
			hashMaps.add(e)
		}
	}
}



context.orderItems2 = hashMaps
