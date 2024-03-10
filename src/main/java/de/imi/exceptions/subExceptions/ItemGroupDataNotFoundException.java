package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemGroupDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ItemGroupDataNotFoundException(String message) {
        super(message);
    }
}
