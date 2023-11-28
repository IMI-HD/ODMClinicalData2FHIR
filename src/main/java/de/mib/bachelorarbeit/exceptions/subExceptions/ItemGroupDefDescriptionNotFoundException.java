package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class ItemGroupDefDescriptionNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public ItemGroupDefDescriptionNotFoundException(String message) {
        super(message);
    }
}
