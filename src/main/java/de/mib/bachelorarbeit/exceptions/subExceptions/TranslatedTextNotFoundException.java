package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class TranslatedTextNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public TranslatedTextNotFoundException(String message) {
        super(message);
    }
}
