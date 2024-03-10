package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingCodeListException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingCodeListException(String message) {
        super(message);
    }
}
