package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ValueAttributeOfItemDataEmptyOrBlankException extends ClinicalDataToQuestionnaireResponseException {
    public ValueAttributeOfItemDataEmptyOrBlankException(String message) {
        super(message);
    }
}
