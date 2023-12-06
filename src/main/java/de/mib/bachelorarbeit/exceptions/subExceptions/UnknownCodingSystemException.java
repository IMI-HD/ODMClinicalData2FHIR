package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class UnknownCodingSystemException extends ClinicalDataToQuestionnaireResponseException {
    public UnknownCodingSystemException(String message) {
        super(message);
    }
}
