package de.imi.services.definitions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;
import odm.ODM;

public interface OdmToFhirConverter {

    String clinicalDataToQuestionnaireResponse(ODM odm,
                                               String language,
                                               String linkToQuestionnaire)
            throws ClinicalDataToQuestionnaireResponseException;
}
