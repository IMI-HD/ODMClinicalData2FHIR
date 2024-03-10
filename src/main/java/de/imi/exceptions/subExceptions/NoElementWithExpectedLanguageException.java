package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoElementWithExpectedLanguageException extends ClinicalDataToQuestionnaireResponseException {
    public NoElementWithExpectedLanguageException(String message) {
        super(message);
    }
}
