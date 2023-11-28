package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemGroupDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ItemGroupDataNotFoundException(String message) {
        super(message);
    }
}
