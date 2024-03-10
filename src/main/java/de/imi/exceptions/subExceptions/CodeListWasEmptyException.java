package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class CodeListWasEmptyException extends ClinicalDataToQuestionnaireResponseException {
    public CodeListWasEmptyException(String message) {
        super(message);
    }
}
