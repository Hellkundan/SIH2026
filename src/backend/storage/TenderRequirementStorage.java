package backend.storage;

import backend.model.TenderRequirement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TenderRequirementStorage {

    private final HashMap<UUID, TenderRequirement> requirements =
            new HashMap<>();


    public void saveRequirement(
            TenderRequirement requirement
    ) {

        requirements.put(
                requirement.getId(),
                requirement
        );
    }


    public TenderRequirement findRequirementById(
            UUID id
    ) {

        return requirements.get(id);
    }


    public List<TenderRequirement> getAllRequirements() {

        return new ArrayList<>(
                requirements.values()
        );
    }


    public List<TenderRequirement> getRequirementsByTenderId(
            UUID tenderId
    ) {

        List<TenderRequirement> tenderRequirements =
                new ArrayList<>();

        for (TenderRequirement requirement :
                requirements.values()) {

            if (requirement.getTenderId()
                    .equals(tenderId)) {

                tenderRequirements.add(
                        requirement
                );
            }
        }

        return tenderRequirements;
    }


    public void deleteRequirement(
            UUID id
    ) {

        requirements.remove(id);
    }
}