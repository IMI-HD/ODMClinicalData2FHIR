package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoMatchingCodeListItemException extends ClinicalDataToQuestionnaireResponseException {
    public NoMatchingCodeListItemException(String message) {
        super(message);
    }
}
