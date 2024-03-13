package de.imi.services.definitions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;
import odm.ODM;
import odm.ODMcomplexTypeDefinitionSubjectData;

public interface OdmToFhirConverter {

    String clinicalDataToQuestionnaireResponse(ODM odm,
                                               ODMcomplexTypeDefinitionSubjectData subjectData,
                                               String language,
                                               String linkToQuestionnaire)
            throws ClinicalDataToQuestionnaireResponseException;
}
