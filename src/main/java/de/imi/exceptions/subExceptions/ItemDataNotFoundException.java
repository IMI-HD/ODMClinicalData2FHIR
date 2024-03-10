package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ItemDataNotFoundException(String message) {
        super(message);
    }
}
