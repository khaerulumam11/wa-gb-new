package com.whatsapp.chattema.data.models

import android.os.Parcelable
import com.android.billingclient.api.Purchase
import com.whatsapp.chattema.extensions.utils.purchaseStateToText
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DetailedPurchaseRecord(
    val sku: String? = null,
    val productId: String? = null,
    val developerPayload: String? = null,
    val autoRenewing: Boolean? = null,
    val acknowledged: Boolean? = null,
    val orderId: String? = null,
    val packageName: String? = null,
    val purchaseState: Int = Purchase.PurchaseState.UNSPECIFIED_STATE,
    val purchaseStateText: String = purchaseStateToText(purchaseState),
    val purchaseTime: Long? = null,
    val purchaseToken: String? = null,
    val signature: String? = null,
    val originalJson: String? = null,
    val isAsync: Boolean? = null
) : Parcelable