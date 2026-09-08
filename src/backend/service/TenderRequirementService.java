package backend.service;

import backend.model.TenderRequirement;
import backend.storage.TenderRequirementStorage;

import java.util.List;
import java.util.UUID;

public class TenderRequirementService {

    private final TenderRequirementStorage requirementStorage;
    private final TenderService tenderService;


    public TenderRequirementService(
            TenderRequirementStorage requirementStorage,
            TenderService tenderService
    ) {
        this.requirementStorage = requirementStorage;
        this.tenderService = tenderService;
    }


    public TenderRequirement createRequirement(
            UUID tenderId,
            String requirement,
            boolean mandatory
    ) {

        // Check if the Tender exists
        tenderService.getTenderById(tenderId);

        TenderRequirement tenderRequirement =
                new TenderRequirement(
                        tenderId,
                        requirement,
                        mandatory
                );

        requirementStorage.saveRequirement(
                tenderRequirement
        );

        return tenderRequirement;
    }


    public TenderRequirement getRequirementById(
            UUID id
    ) {

        TenderRequirement requirement =
                requirementStorage
                        .findRequirementById(id);

        if (requirement == null) {

            throw new IllegalArgumentException(
                    "Tender requirement not found"
            );
        }

        return requirement;
    }


    public List<TenderRequirement> getAllRequirements() {

        return requirementStorage.getAllRequirements();
    }


    public List<TenderRequirement> getRequirementsByTenderId(
            UUID tenderId
    ) {

        // Check if the Tender exists
        tenderService.getTenderById(tenderId);

        return requirementStorage
                .getRequirementsByTenderId(tenderId);
    }


    public void updateRequirement(
            UUID id,
            String requirement,
            boolean mandatory
    ) {

        TenderRequirement tenderRequirement =
                getRequirementById(id);

        tenderRequirement.updateRequirement(
                requirement,
                mandatory
        );
    }


    public void deleteRequirement(
            UUID id
    ) {

        // Check if requirement exists first
        getRequirementById(id);

        requirementStorage.deleteRequirement(id);
    }
}