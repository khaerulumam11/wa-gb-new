package com.whatsapp.chattema.extensions.utils

import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.google.gson.Gson
import com.whatsapp.chattema.data.models.DetailedPurchaseRecord
import com.whatsapp.chattema.data.models.InternalDetailedPurchaseRecord
import com.whatsapp.chattema.data.models.PseudoDetailedPurchaseRecord

fun Purchase.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        val pseudoDetailedRecord =
            Gson().fromJson(originalJson, PseudoDetailedPurchaseRecord::class.java)
        val internalDetailedRecord = InternalDetailedPurchaseRecord(pseudoDetailedRecord, this)
        Gson().fromJson(internalDetailedRecord.toJSONString(0), DetailedPurchaseRecord::class.java)
    } catch (e: Exception) {
        null
    }

fun PurchaseHistoryRecord.asDetailedPurchase(): DetailedPurchaseRecord? =
    try {
        val purchase = Purchase(originalJson, signature)
        val pseudoDetailedRecord =
            Gson().fromJson(originalJson, PseudoDetailedPurchaseRecord::class.java)
        val internalDetailedRecord =
            InternalDetailedPurchaseRecord(pseudoDetailedRecord, purchase, true)
        Gson().fromJson(internalDetailedRecord.toJSONString(0), DetailedPurchaseRecord::class.java)
    } catch (e: Exception) {
        null
    }

internal fun purchaseStateToText(@Purchase.PurchaseState purchaseState: Int): String =
    when (purchaseState) {
        Purchase.PurchaseState.PENDING -> "Pending"
        Purchase.PurchaseState.PURCHASED -> "Purchased"
        else -> "Unspecified"
    }