package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCodeListItemsException extends ClinicalDataToQuestionnaireResponseException {
    public NoCodeListItemsException(String message) {
        super(message);
    }
}
