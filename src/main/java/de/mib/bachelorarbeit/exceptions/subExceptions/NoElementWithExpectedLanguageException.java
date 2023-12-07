package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoElementWithExpectedLanguageException extends ClinicalDataToQuestionnaireResponseException {
    public NoElementWithExpectedLanguageException(String message) {
        super(message);
    }
}
