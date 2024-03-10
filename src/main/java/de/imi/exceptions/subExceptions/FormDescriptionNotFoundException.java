package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class FormDescriptionNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public FormDescriptionNotFoundException(String message) {
        super(message);
    }
}
