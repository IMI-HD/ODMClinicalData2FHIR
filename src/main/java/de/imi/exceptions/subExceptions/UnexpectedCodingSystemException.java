package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class UnexpectedCodingSystemException extends ClinicalDataToQuestionnaireResponseException {
    public UnexpectedCodingSystemException(String message) {
        super(message);
    }
}
