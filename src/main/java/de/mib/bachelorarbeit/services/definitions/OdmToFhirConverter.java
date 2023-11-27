package de.mib.bachelorarbeit.services.definitions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;
import odm.ODM;

public interface OdmToFhirConverter {

    String getTestRessource();

    void printClinicalData(ODM odm);

    String clinicalDataToQuestionnaireResponse(ODM odm) throws ClinicalDataToQuestionnaireResponseException;
}
