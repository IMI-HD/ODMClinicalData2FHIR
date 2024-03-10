package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class UnknownCodingSystemException extends ClinicalDataToQuestionnaireResponseException {
    public UnknownCodingSystemException(String message) {
        super(message);
    }
}
