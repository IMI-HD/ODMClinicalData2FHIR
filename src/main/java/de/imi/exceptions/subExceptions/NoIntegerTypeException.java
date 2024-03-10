package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoIntegerTypeException extends ClinicalDataToQuestionnaireResponseException {
    public NoIntegerTypeException(String message) {
        super(message);
    }
}
