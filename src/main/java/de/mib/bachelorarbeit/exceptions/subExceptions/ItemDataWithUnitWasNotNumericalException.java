package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemDataWithUnitWasNotNumericalException extends ClinicalDataToQuestionnaireResponseException {
    public ItemDataWithUnitWasNotNumericalException(String message) {
        super(message);
    }
}
