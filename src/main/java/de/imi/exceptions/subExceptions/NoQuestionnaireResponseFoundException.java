package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoQuestionnaireResponseFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoQuestionnaireResponseFoundException(String message) {
        super(message);
    }
}
