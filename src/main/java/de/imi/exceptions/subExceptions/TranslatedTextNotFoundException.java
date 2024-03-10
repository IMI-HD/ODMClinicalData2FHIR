package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class TranslatedTextNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public TranslatedTextNotFoundException(String message) {
        super(message);
    }
}
