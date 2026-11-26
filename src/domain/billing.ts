import { PRO_PLANS, type ProPlanId, type ProPlanOption } from '../model/musicoletModels';

export interface BillingPurchaseResult {
  success: boolean;
  planId: ProPlanId;
  receiptToken?: string;
  message: string;
}

export interface BillingRestoreResult {
  restored: boolean;
  activePlan: ProPlanId | null;
  message: string;
}

/**
 * Clean billing abstraction so Google Play Billing (Android) or web checkout
 * can be swapped in without touching UI or store business logic.
 */
export interface BillingProvider {
  readonly providerName: string;
  getAvailablePlans(): ProPlanOption[];
  purchasePlan(planId: ProPlanId): Promise<BillingPurchaseResult>;
  restorePurchases(storedPlan: ProPlanId | null): Promise<BillingRestoreResult>;
}

export class LocalDemoBillingProvider implements BillingProvider {
  readonly providerName = 'Moronto Billing Adapter (Google Play Ready)';

  getAvailablePlans(): ProPlanOption[] {
    return PRO_PLANS;
  }

  async purchasePlan(planId: ProPlanId): Promise<BillingPurchaseResult> {
    const plan = PRO_PLANS.find((p) => p.id === planId) ?? PRO_PLANS[0];
    return {
      success: true,
      planId: plan.id,
      receiptToken: `moronto-pro-${plan.id}-${Date.now()}`,
      message: `Activated ${plan.name} (${plan.priceLabel}). Ads removed & all Pro features unlocked.`,
    };
  }

  async restorePurchases(
    storedPlan: ProPlanId | null,
  ): Promise<BillingRestoreResult> {
    if (storedPlan) {
      const plan = PRO_PLANS.find((p) => p.id === storedPlan);
      return {
        restored: true,
        activePlan: storedPlan,
        message: `Restored ${plan?.name ?? 'Moronto Music Pro'} from local entitlement record.`,
      };
    }
    return {
      restored: false,
      activePlan: null,
      message:
        'No active Pro purchase found on this device. Connect Google Play Billing or activate Monthly / Lifetime above.',
    };
  }
}

export const morontoBillingProvider: BillingProvider =
  new LocalDemoBillingProvider();
