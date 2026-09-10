package backend.service;

import java.util.UUID;
import java.util.List;
import backend.model.VerificationResult;

public interface OrchestrationService {

    // TODO: Implement document processing orchestration with the document pipeline.
    void triggerDocumentProcessing(UUID documentId);

    // TODO: Implement verification orchestration with the verification providers.
    void triggerVerificationChecks(UUID tenderBidId);

    // TODO: Implement compliance evaluation orchestration with the compliance engine.
    void triggerComplianceEvaluation(UUID tenderBidId);

    List<VerificationResult> getVerificationResults(UUID tenderBidId);
}