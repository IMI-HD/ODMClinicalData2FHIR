package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class UnknownBoolCodingException extends ClinicalDataToQuestionnaireResponseException {
    public UnknownBoolCodingException(String message) {
        super(message);
    }
}
