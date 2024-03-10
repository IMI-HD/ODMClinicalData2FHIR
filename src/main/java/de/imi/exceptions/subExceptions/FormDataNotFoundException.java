package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class FormDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public FormDataNotFoundException(String message) {
        super(message);
    }
}
