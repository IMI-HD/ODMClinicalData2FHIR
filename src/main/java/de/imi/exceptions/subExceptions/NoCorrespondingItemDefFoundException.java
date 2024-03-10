package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingItemDefFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingItemDefFoundException(String message) {
        super(message);
    }
}
