package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingMeasurementUnitException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingMeasurementUnitException(String message) {
        super(message);
    }
}
