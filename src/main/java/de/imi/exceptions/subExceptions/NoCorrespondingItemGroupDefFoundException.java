package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCorrespondingItemGroupDefFoundException extends ClinicalDataToQuestionnaireResponseException {
    public NoCorrespondingItemGroupDefFoundException(String message) {
        super(message);
    }
}
