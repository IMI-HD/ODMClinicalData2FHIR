package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoCodeListItemsException extends ClinicalDataToQuestionnaireResponseException {
    public NoCodeListItemsException(String message) {
        super(message);
    }
}
