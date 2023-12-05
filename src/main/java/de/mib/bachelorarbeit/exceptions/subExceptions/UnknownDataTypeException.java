package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class UnknownDataTypeException extends ClinicalDataToQuestionnaireResponseException {
    public UnknownDataTypeException(String message) {
        super(message);
    }
}
