package org.cardanofoundation.cip113.model;

/**
 * Request body for POST /issuance/compute-policy-id.
 *
 * Used by the off-chain orchestrator (Libertum CIP-113 substandards) to
 * derive the per-token issuance policy ID before the registry-insert +
 * sentinel-mint phase. The orchestrator needs the policy ID to
 * parameterise the kyc-transfer-logic substandard, which has to know
 * the token policy at script-build time.
 *
 * Mirrors the parameterisation done internally by FreezeAndSeizeHandler
 * around line 215 — but exposed as a pure compute call (no tx, no
 * on-chain side effects).
 *
 * @param adminPubKeyHash hex PKH of the issuer custodian — fed into
 *                        FreezeAndSeizeScriptBuilderService.buildIssuerAdminScript
 *                        as the substandard's issuer-admin credential.
 *                        REQUIRED.
 * @param protocolTxHash  optional bootstrap-tx hash, picks a specific
 *                        protocol params snapshot. Defaults to the
 *                        latest if null.
 */
public record ComputeIssuancePolicyIdRequest(
        String adminPubKeyHash,
        String protocolTxHash
) {
}
