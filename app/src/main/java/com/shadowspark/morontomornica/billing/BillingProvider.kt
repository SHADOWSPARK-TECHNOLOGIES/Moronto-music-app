package com.shadowspark.morontomornica.billing

enum class ProPlanId(val key: String) {
  MONTHLY("monthly"),
  LIFETIME("lifetime"),
  ;

  companion object {
    fun fromKey(raw: String?): ProPlanId? =
      entries.find { it.key == raw }
  }
}

data class ProPlanOption(
  val id: ProPlanId,
  val name: String,
  val priceNaira: Int,
  val priceLabel: String,
  val cadence: String,
  val badgeText: String,
  val features: List<String>,
)

val PRO_PLANS = listOf(
  ProPlanOption(
    id = ProPlanId.MONTHLY,
    name = "Moronto Music Pro · Monthly",
    priceNaira = 1500,
    priceLabel = "₦1,500",
    cadence = "per month",
    badgeText = "Flexible Plan",
    features = listOf(
      "Zero banner or rewarded ads anywhere",
      "Unlimited access to all 10-band EQ presets",
      "Dual-deck Crossfade (0–12 seconds)",
      "All 4 Studio Themes + Sleep Timer",
    ),
  ),
  ProPlanOption(
    id = ProPlanId.LIFETIME,
    name = "Moronto Music Pro · Lifetime",
    priceNaira = 12000,
    priceLabel = "₦12,000",
    cadence = "one-time unlock",
    badgeText = "Best Value",
    features = listOf(
      "Permanent ad-free listening forever",
      "All 10-band EQ presets (Afrobeats Bass, Amapiano, Gospel, Highlife)",
      "Dual-deck Crossfade (0–12 seconds) & Gapless",
      "All Pro Studio Themes, Sleep Timer & Priority Updates",
    ),
  ),
)

data class BillingPurchaseResult(
  val success: Boolean,
  val planId: ProPlanId,
  val receiptToken: String?,
  val message: String,
)

data class BillingRestoreResult(
  val restored: Boolean,
  val activePlan: ProPlanId?,
  val message: String,
)

/**
 * Modular billing abstraction so Google Play Billing Library can be connected
 * later without changing ViewModel state contracts or UI screens.
 */
interface BillingProvider {
  val providerName: String
  fun getAvailablePlans(): List<ProPlanOption>
  fun purchasePlan(planId: ProPlanId): BillingPurchaseResult
  fun restorePurchases(storedPlan: ProPlanId?): BillingRestoreResult
}

class LocalDemoBillingProvider : BillingProvider {
  override val providerName: String = "Moronto Billing Adapter (Google Play Ready)"

  override fun getAvailablePlans(): List<ProPlanOption> = PRO_PLANS

  override fun purchasePlan(planId: ProPlanId): BillingPurchaseResult {
    val plan = PRO_PLANS.find { it.id == planId } ?: PRO_PLANS.first()
    return BillingPurchaseResult(
      success = true,
      planId = plan.id,
      receiptToken = "moronto-pro-${plan.id.key}-${System.currentTimeMillis()}",
      message = "Activated ${plan.name} (${plan.priceLabel}). Ads removed & all Pro features unlocked.",
    )
  }

  override fun restorePurchases(storedPlan: ProPlanId?): BillingRestoreResult {
    if (storedPlan != null) {
      val plan = PRO_PLANS.find { it.id == storedPlan }
      return BillingRestoreResult(
        restored = true,
        activePlan = storedPlan,
        message = "Restored ${plan?.name ?: "Moronto Music Pro"} from device entitlement record.",
      )
    }
    return BillingRestoreResult(
      restored = false,
      activePlan = null,
      message = "No active Pro purchase found on this device. Connect Google Play Billing or activate Monthly / Lifetime above.",
    )
  }
}

val morontoBillingProvider: BillingProvider = LocalDemoBillingProvider()
