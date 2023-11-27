package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ClinicalDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ClinicalDataNotFoundException(String message) {
        super(message);
    }
}
