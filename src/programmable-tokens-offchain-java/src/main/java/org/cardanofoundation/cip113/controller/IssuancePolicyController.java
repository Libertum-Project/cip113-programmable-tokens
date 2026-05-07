package org.cardanofoundation.cip113.controller;

import com.bloxbean.cardano.client.address.Credential;
import com.bloxbean.cardano.client.util.HexUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.cardanofoundation.cip113.model.ComputeIssuancePolicyIdRequest;
import org.cardanofoundation.cip113.model.ComputeIssuancePolicyIdResponse;
import org.cardanofoundation.cip113.service.FreezeAndSeizeScriptBuilderService;
import org.cardanofoundation.cip113.service.ProtocolBootstrapService;
import org.cardanofoundation.cip113.service.ProtocolScriptBuilderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Pure-compute helper for the off-chain orchestrator.
 *
 * The Libertum CIP-113 substandards orchestrator (Lucid-based) needs
 * the per-token issuance policy ID before it can parameterise the
 * kyc-transfer-logic substandard — but the issuance script is
 * parameterised with the substandard's issuer-admin credential, which
 * lives here in cip113-java's reference impl. Rather than duplicate
 * the parameterisation in TS, we expose this single compute call.
 *
 * No tx is built, no chain side effects — this is `applyParamsToScript`
 * + scriptHash. Safe to call repeatedly.
 *
 * See also: FreezeAndSeizeHandler line ~215 for the equivalent
 * computation done inline during the freeze-and-seize register flow.
 */
@RestController
@RequestMapping("${apiPrefix}/issuance")
@RequiredArgsConstructor
@Slf4j
public class IssuancePolicyController {

    private final ProtocolBootstrapService protocolBootstrapService;
    private final ProtocolScriptBuilderService protocolScriptBuilderService;
    private final FreezeAndSeizeScriptBuilderService fesScriptBuilder;

    @PostMapping("/compute-policy-id")
    public ResponseEntity<?> computePolicyId(@RequestBody ComputeIssuancePolicyIdRequest request) {

        if (request == null || request.adminPubKeyHash() == null || request.adminPubKeyHash().isBlank()) {
            return ResponseEntity.badRequest().body("adminPubKeyHash is required");
        }

        try {
            var protocolParams = (request.protocolTxHash() != null && !request.protocolTxHash().isBlank())
                    ? protocolBootstrapService.getProtocolBootstrapParamsByTxHash(request.protocolTxHash())
                        .orElseThrow(() -> new IllegalArgumentException(
                                "no bootstrap params for txHash=" + request.protocolTxHash()))
                    : protocolBootstrapService.getProtocolBootstrapParams();

            var adminCredential = Credential.fromKey(request.adminPubKeyHash());
            var substandardIssueContract = fesScriptBuilder.buildIssuerAdminScript(adminCredential);

            var issuanceContract = protocolScriptBuilderService
                    .getParameterizedIssuanceMintScript(protocolParams, substandardIssueContract);

            var policyId = issuanceContract.getPolicyId();
            var scriptHash = HexUtil.encodeHexString(issuanceContract.getScriptHash());

            log.info("computed issuance policyId={} for adminPkh={} (protocolTxHash={})",
                    policyId, request.adminPubKeyHash(), protocolParams.txHash());

            return ResponseEntity.ok(new ComputeIssuancePolicyIdResponse(policyId, scriptHash));

        } catch (IllegalArgumentException e) {
            log.warn("compute-policy-id bad request: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("compute-policy-id failed", e);
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
    }
}
