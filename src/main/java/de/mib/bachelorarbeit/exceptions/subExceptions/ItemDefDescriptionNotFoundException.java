package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemDefDescriptionNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ItemDefDescriptionNotFoundException(String message) {
        super(message);
    }
}
