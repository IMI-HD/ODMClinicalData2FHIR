package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoIntegerTypeException extends ClinicalDataToQuestionnaireResponseException {
    public NoIntegerTypeException(String message) {
        super(message);
    }
}
