package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ItemDataNotFoundException(String message) {
        super(message);
    }
}
