package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoMatchingCodeListItemException extends ClinicalDataToQuestionnaireResponseException {
    public NoMatchingCodeListItemException(String message) {
        super(message);
    }
}
