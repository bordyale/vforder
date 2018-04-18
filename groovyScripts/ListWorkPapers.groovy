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

import java.util.Map;

import org.apache.ofbiz.base.util.UtilMisc;
import org.apache.ofbiz.entity.condition.EntityExpr
import org.apache.ofbiz.entity.condition.EntityFunction
import org.apache.ofbiz.entity.condition.EntityOperator
import org.apache.ofbiz.entity.condition.EntityFieldValue
import org.apache.ofbiz.entity.condition.EntityConditionList
import org.apache.ofbiz.entity.condition.EntityCondition
import org.apache.ofbiz.entity.GenericValue
import org.apache.ofbiz.entity.util.EntityUtil




orderItemShippingItem = select("productId","internalName","productName","quantity").from("ListShippingItemsSummary").where("shipmentId", shipmentId).cache(false).queryList()
orderItemShippingItem = EntityUtil.orderBy(orderItemShippingItem,  ["productName"])

List<HashMap<String,Object>> hashMaps = new ArrayList<HashMap<String,Object>>()

for (GenericValue entry: orderItemShippingItem){

	productId =entry.get("productId")
	//if there is a main supplier skip.
	supplier = select("productId","partyId").from("SupplierProduct").where("productId",productId,"supplierPrefOrderId", "10_MAIN_SUPPL").cache(false).queryList()
	if (supplier.size()>0){
		continue
	}
	Map<String,Object> e = new HashMap<String,Object>()
	BigDecimal quantity = entry.get("quantity")
	e.put("quantity",quantity)
	String productId = entry.get("productId")
	e.put("internalName",entry.get("internalName"))
	productAssoc = select("paQuantity", "prInternalName", "paProductIdTo").from("VfProductAndAssoc").where("paProductId", productId,"paProductAssocTypeId","MANUF_COMPONENT").cache(false).queryList()
	if (productAssoc != null){
		int assocNum = 0
		List<HashMap<String,Object>> components = new ArrayList<HashMap<String,Object>>()
		for (GenericValue pra: productAssoc){
			Map<String,Object> a = new HashMap<String,Object>()
			a.put("name",pra.get("prInternalName"))
			BigDecimal qty =pra.get("paQuantity")
			//ék 5X5
			if ("10158".equals(pra.get("paProductIdTo"))){
				qty = qty.multiply(quantity)
				qty = qty.setScale(2, BigDecimal.ROUND_HALF_EVEN);
				a.put("quantity",qty + " fm")
			}else{
				a.put("quantity",qty.multiply(quantity).intValue() + " db")
			}
			components.add(a)
			assocNum ++
		}
		e.put("components", components)
		//extra empty row on work paper
		int tableSize = 12
		if (assocNum >= tableSize){
			e.put("emptyRowNum", 0)
		}else{
			e.put("emptyRowNum", tableSize - assocNum)
		}
	}

	hashMaps.add(e)
}


context.listIt = hashMaps


