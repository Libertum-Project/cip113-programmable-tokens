package org.cardanofoundation.cip113.model;

/**
 * Response body for POST /issuance/compute-policy-id.
 *
 * @param policyId    hex policy ID (= scriptHash) of the parameterised
 *                    issuance_mint script. The orchestrator persists
 *                    this on the offering doc.
 * @param scriptHash  same as policyId on Cardano — included for clarity
 *                    so callers don't need to know the equivalence.
 */
public record ComputeIssuancePolicyIdResponse(
        String policyId,
        String scriptHash
) {
}
