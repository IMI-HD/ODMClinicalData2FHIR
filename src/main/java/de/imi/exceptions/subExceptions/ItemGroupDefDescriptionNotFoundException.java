package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemGroupDefDescriptionNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ItemGroupDefDescriptionNotFoundException(String message) {
        super(message);
    }
}
