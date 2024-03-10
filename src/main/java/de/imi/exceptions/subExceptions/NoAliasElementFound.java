package de.imi.exceptions.subExceptions;

import de.imi.exceptions.ClinicalDataToQuestionnaireResponseException;

public class NoAliasElementFound extends ClinicalDataToQuestionnaireResponseException {
    public NoAliasElementFound(String message) {
        super(message);
    }
}
