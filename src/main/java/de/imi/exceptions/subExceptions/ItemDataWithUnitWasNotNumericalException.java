package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemDataWithUnitWasNotNumericalException extends ClinicalDataToQuestionnaireResponseException {
    public ItemDataWithUnitWasNotNumericalException(String message) {
        super(message);
    }
}
