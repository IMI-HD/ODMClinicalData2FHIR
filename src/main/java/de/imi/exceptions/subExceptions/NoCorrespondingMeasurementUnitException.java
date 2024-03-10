package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingMeasurementUnitException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingMeasurementUnitException(String message) {
        super(message);
    }
}
