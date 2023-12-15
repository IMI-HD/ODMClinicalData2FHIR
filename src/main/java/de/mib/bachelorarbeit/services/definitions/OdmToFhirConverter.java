package de.mib.bachelorarbeit.services.definitions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;
import odm.ODM;

public interface OdmToFhirConverter {

    String getTestRessource();

    String clinicalDataToQuestionnaireResponse(ODM odm,
                                               String language,
                                               String linkToQuestionnaire)
            throws ClinicalDataToQuestionnaireResponseException;
}
