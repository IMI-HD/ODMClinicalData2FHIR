package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class UnknownBoolCodingException extends ClinicalDataToQuestionnaireResponseException {
    public UnknownBoolCodingException(String message) {
        super(message);
    }
}
