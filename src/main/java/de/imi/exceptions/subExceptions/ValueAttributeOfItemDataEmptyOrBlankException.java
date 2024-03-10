package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ValueAttributeOfItemDataEmptyOrBlankException extends ClinicalDataToQuestionnaireResponseException {
    public ValueAttributeOfItemDataEmptyOrBlankException(String message) {
        super(message);
    }
}
