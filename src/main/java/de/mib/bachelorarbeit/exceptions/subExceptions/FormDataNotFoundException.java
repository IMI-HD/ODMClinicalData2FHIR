package de.mib.bachelorarbeit.exceptions.subExceptions;

import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;

public class FormDataNotFoundException extends ClinicalDataToQuestionnaireResponseException {
    public FormDataNotFoundException(String message) {
        super(message);
    }
}
