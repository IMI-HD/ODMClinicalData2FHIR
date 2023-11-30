package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class CodeListWasEmptyException extends ClinicalDataToQuestionnaireResponseException {
    public CodeListWasEmptyException(String message) {
        super(message);
    }
}
