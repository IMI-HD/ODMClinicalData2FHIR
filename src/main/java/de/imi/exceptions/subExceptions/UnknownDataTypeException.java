package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class UnknownDataTypeException extends ClinicalDataToQuestionnaireResponseException {
    public UnknownDataTypeException(String message) {
        super(message);
    }
}
