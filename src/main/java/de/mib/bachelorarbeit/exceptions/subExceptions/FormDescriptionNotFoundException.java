package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class FormDescriptionNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public FormDescriptionNotFoundException(String message) {
        super(message);
    }
}
