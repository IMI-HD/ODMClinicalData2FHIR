package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class UnexpectedCodingSystemException extends ClinicalDataToQuestionnaireResponseException {
    public UnexpectedCodingSystemException(String message) {
        super(message);
    }
}
