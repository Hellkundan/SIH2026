package backend.service;

import backend.model.TenderRequirement;
import backend.repository.TenderRequirementRepository;

import java.util.List;
import java.util.UUID;

public class TenderRequirementService {

    private final TenderRequirementRepository tenderRequirementRepository;
    private final TenderService tenderService;


    public TenderRequirementService(
            TenderRequirementRepository tenderRequirementRepository,
            TenderService tenderService
    ) {
        this.tenderRequirementRepository = tenderRequirementRepository;
        this.tenderService = tenderService;
    }


    public TenderRequirement createRequirement(
            UUID tenderId,
            String requirement,
            boolean mandatory
    ) {

        tenderService.getTenderById(tenderId);

        TenderRequirement tenderRequirement =
                new TenderRequirement(
                        tenderId,
                        requirement,
                        mandatory
                );

        return tenderRequirementRepository.save(tenderRequirement);
    }


    public TenderRequirement getRequirementById(UUID id) {

        return tenderRequirementRepository
                .findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Tender requirement not found"
                ));
    }


    public List<TenderRequirement> getAllRequirements() {

        return tenderRequirementRepository.findAll();
    }


    public List<TenderRequirement> getRequirementsByTenderId(UUID tenderId) {

        tenderService.getTenderById(tenderId);

        return tenderRequirementRepository.findByTenderId(tenderId);
    }


    public void updateRequirement(
            UUID id,
            String requirement,
            boolean mandatory
    ) {

        TenderRequirement tenderRequirement = getRequirementById(id);

        tenderRequirement.updateRequirement(
                requirement,
                mandatory
        );

        tenderRequirementRepository.save(tenderRequirement);
    }


    public void deleteRequirement(UUID id) {

        getRequirementById(id);

        tenderRequirementRepository.deleteById(id);
    }
}