package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ClinicalDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ClinicalDataNotFoundException(String message) {
        super(message);
    }
}
