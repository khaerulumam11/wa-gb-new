package com.whatsapp.chattema.data.listeners

import com.android.billingclient.api.SkuDetails
import com.whatsapp.chattema.data.models.DetailedPurchaseRecord

interface BillingProcessesListener {
    fun onBillingClientReady() {}
    fun onBillingClientDisconnected() {}
    fun onInAppSkuDetailsListUpdated(skuDetailsList: List<SkuDetails>) {}
    fun onSubscriptionsSkuDetailsListUpdated(skuDetailsList: List<SkuDetails>) {}
    fun onInAppPurchasesHistoryUpdated(inAppPurchasesHistory: List<DetailedPurchaseRecord>) {}
    fun onSubscriptionsPurchasesHistoryUpdated(subscriptionsPurchasesHistory: List<DetailedPurchaseRecord>) {}
    fun onSkuPurchaseSuccess(purchase: DetailedPurchaseRecord? = null)
    fun onSkuPurchaseError(purchase: DetailedPurchaseRecord? = null)
}