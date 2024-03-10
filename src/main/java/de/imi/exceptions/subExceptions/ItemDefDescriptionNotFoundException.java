package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemDefDescriptionNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ItemDefDescriptionNotFoundException(String message) {
        super(message);
    }
}
