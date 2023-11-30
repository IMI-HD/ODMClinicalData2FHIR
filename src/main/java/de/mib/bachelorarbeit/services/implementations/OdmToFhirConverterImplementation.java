package de.mib.bachelorarbeit.services.implementations;


import ca.uhn.fhir.context.FhirContext;
import de.mib.bachelorarbeit.exceptions.ClinicalDataToQuestionnaireResponseException;
import de.mib.bachelorarbeit.exceptions.subExceptions.*;
import de.mib.bachelorarbeit.services.definitions.OdmToFhirConverter;
import odm.*;
import org.hl7.fhir.r4.model.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Service
public class OdmToFhirConverterImplementation implements OdmToFhirConverter {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(OdmToFhirConverterImplementation.class);

    private final FhirContext fhirContext = FhirContext.forR4();

    @Override
    public String getTestRessource() {
        Patient patient = new Patient();
        patient.addIdentifier()
                .setSystem("http://example.com/fhir/patient-identifiers")
                .setValue("12345");
        patient.addName()
                .setFamily("Mustermann")
                .addGiven("Max");
        LOGGER.info("Test patient created!");

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeToString(patient);
    }

    @Override
    public String clinicalDataToQuestionnaireResponse(@NotNull ODM odm)
            throws ClinicalDataToQuestionnaireResponseException {
        LOGGER.info("Received ODM in the converter!");
        // FHIR Bundle that will be returned
        Bundle bundle = createBundle();
        // List of all QRS contained in the final Bundle
        List<QuestionnaireResponse> _qrs = new ArrayList<>();
        // List of <ClinicalData> Elements from ODM
        List<ODMcomplexTypeDefinitionClinicalData> _clinicalData;
        // List of <Study> Elements from ODM
        List<ODMcomplexTypeDefinitionStudy> _study;
        // <ClinicalData> Element at index 0 (only this will be converted!)
        ODMcomplexTypeDefinitionClinicalData clinicalData;
        // <SubjectData> Element at index 0 (only this will be converted!)
        ODMcomplexTypeDefinitionSubjectData subjectData;
        // <Study> Element at index 0 (only this will be converted!)
        ODMcomplexTypeDefinitionStudy study;
        // <MetaDataVersion> Element at index 0 of the <Study> (only this will be converted!)
        ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion;

        // validate basic structure of the ODM file

        // check if a <ClinicalData> Element is present!
        if (odm.getClinicalData().isEmpty()) {
            LOGGER.error("<ClinicalData> was empty!");
            throw new ClinicalDataNotFoundException("<ClinicalData> was empty!");
        } else {
            // set <ClinicalData> from ODM
            LOGGER.info("Set <ClinicalData> list!");
            _clinicalData = odm.getClinicalData();
            try {
                clinicalData = _clinicalData.get(0);
                LOGGER.info("<ClinicalData> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <ClinicalData> found at index 0!");
                throw new ClinicalDataNotFoundException("No <ClinicalData> found at index 0!");
            }
        }

        // check if a <SubjectData> Element is present in the selected <ClinicalData>
        if (clinicalData.getSubjectData().isEmpty()) {
            LOGGER.error("<SubjectData> was empty!");
            throw new SubjectDataNotFoundException("<SubjectData> was empty!");
        } else {
            // set <SubjectData> at index 0 from selected <ClinicalData>
            try {
                subjectData = clinicalData.getSubjectData().get(0);
                LOGGER.info("<SubjetData> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <SubjectData> found at index 0!");
                throw new SubjectDataNotFoundException("No <SubjectData> found at index 0!");
            }
        }

        // check if a <Study> Element is present!
        if (odm.getStudy().isEmpty()) {
            LOGGER.error("<Study> was empty!");
            throw new StudyNotFoundException("<Study> was empty!");
        } else {
            // set <Study> from ODM
            LOGGER.info("Set <Study> list!");
            _study = odm.getStudy();
            try {
                study = _study.get(0);
                LOGGER.info("<Study> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <Study> found at index 0!");
                throw new StudyNotFoundException("No <Study> found at index 0!");
            }
        }

        // check if a <MetaDataVersion> from the selected <Study> is present!
        if (study.getMetaDataVersion().isEmpty()) {
            LOGGER.error("<MetaDataVersion> was empty!");
            throw new MetaDataVersionNotFoundException("<MetaDataVersion> was empty!");
        } else {
            // set <MetaDataVersion> at index 0 from selected <Study> from ODM
            try {
                metaDataVersion = study.getMetaDataVersion().get(0);
                LOGGER.info("<MetaDataVersion> field set!");
            } catch (IndexOutOfBoundsException e) {
                LOGGER.error("No <MetaDataVersion> found at index 0!");
                throw new MetaDataVersionNotFoundException("No <MetaDataVersion> found at index 0!");
            }
        }

        LOGGER.info("All data fields set! Looping through ODM");

        // check if <Study> and <ClinicalData> OIDs match
        if (!clinicalData.getStudyOID().equals(study.getOID())) {
            String errorString = String.format("<ClinicalData> StudyOID: %s does not match provided <Study> OID: %s",
                    clinicalData.getStudyOID(), study.getOID());
            LOGGER.error(errorString);
            throw new ObjectIdDontMatchException(errorString);
        }

        // check if <ClinicalData> MetaDataVersionOID match provided <MetaDataVersion> OID
        if (!metaDataVersion.getOID().equals(clinicalData.getMetaDataVersionOID())) {
            String errorString = String.format("<ClinicalData> MetaDataVersionOID: %s does not " +
                                               "match provided <MetaDataVersion> OID: %s",
                    clinicalData.getMetaDataVersionOID(), metaDataVersion.getOID());
            LOGGER.error(errorString);
            throw new ObjectIdDontMatchException(errorString);
        }

        // Loop through all <StudyEventData> => QuestionnaireResponse
        if (subjectData.getStudyEventData().isEmpty()) {
            String error = "No <StudyEventData> Element found in <SubjectData>";
            LOGGER.error(error);
            throw new StudyEventDataNotFoundException(error);
        }

        // List of all <StudyEventData> Elements in one <SubjectData>
        // Each Element will be converted to a Single QuestionnaireResponse
        List<ODMcomplexTypeDefinitionStudyEventData> _studyEventData = subjectData.getStudyEventData();
        // HashMap with key (OID) and Value (linkId) for <FormDef>
        HashMap<String, String> formDefLinkIdMap = new HashMap<>();
        int formCounter = 1;
        for (ODMcomplexTypeDefinitionStudyEventData studyEventData : _studyEventData) {

            // Moved HashMaps to fix Issue #1
            // HashMap with key (OID) and Value (linkId) for <ItemGroupDef>
            HashMap<String, String> itemGroupDefLinkIdMap = new HashMap<>();
            // HashMap with key (OID) and Value (linkId) for <ItemDef>
            HashMap<String, String> itemDefLinkIdMap = new HashMap<>();

            LOGGER.info(
                    String.format("Converting <StudyEventData> with OID: %s",
                            studyEventData.getStudyEventOID())
            );
            // QuestionnaireResponse
            QuestionnaireResponse qrs = createQuestionnaireResponseBase();
            // Create root Element (linkId: 1, no "name")
            QuestionnaireResponse.QuestionnaireResponseItemComponent root = qrs.addItem();
            root.setLinkId("1");
            //List of <StudyEventDef> from <MetaDataVersion>
            List<ODMcomplexTypeDefinitionStudyEventDef> _studyEvents
                    = metaDataVersion.getStudyEventDef();
            LOGGER.info(
                    String.format("Searching for corresponding <StudyEventDef> in <MetaDataVersion>" +
                                  " for <StudyEventData> with OID: %s",
                            studyEventData.getStudyEventOID()));
            // Search for corresponding <StudyEventDef> in the <MetaDataVersion>
            Optional<ODMcomplexTypeDefinitionStudyEventDef> studyEventDef = _studyEvents.stream()
                    .filter(obj -> studyEventData.getStudyEventOID().equals(obj.getOID()))
                    .findFirst();
            if (studyEventDef.isPresent()) {
                LOGGER.info(
                        String.format("<StudyEventDef> with OID: %s found!",
                                studyEventDef.get().getOID()));
            } else {
                String error = String.format("No corresponding <StudyEventDef> with OID: %s found!",
                        studyEventData.getStudyEventOID());
                LOGGER.error(error);
                throw new NoCorrespondingStudyEventDefFoundException(error);
            }

            // List of <FormData> => linkId under the root (1.x)
            List<ODMcomplexTypeDefinitionFormData> _formData = studyEventData.getFormData();
            if (_formData.isEmpty()) {
                String error = String.format("No <FormData> found in <StudyEventData> with OID: %s",
                        studyEventData.getStudyEventOID());
                LOGGER.error(error);
                throw new FormDataNotFoundException(error);
            } else {
                LOGGER.info("<FormData> found in <StudyEventData>");
            }

            // <FormDef> list from <MetaDataVersion> this list
            // will be searched for each <FormData>
            List<ODMcomplexTypeDefinitionFormDef> _formDef = metaDataVersion.getFormDef();

            // Loop through all <FormData> Elements
            for (int i = 0; i < _formData.size(); i++) {

                // Current <FormData> Element
                ODMcomplexTypeDefinitionFormData formData = _formData.get(i);

                // Search for corresponding <FormDef> in the <MetaDataVersion>
                LOGGER.info(
                        String.format("Searching for corresponding " +
                                      "<FormDef> with OID: %s in <MetaDataVersion>",
                                formData.getFormOID()));
                Optional<ODMcomplexTypeDefinitionFormDef> formDef = _formDef.stream()
                        .filter(obj -> formData.getFormOID().equals(obj.getOID()))
                        .findFirst();

                if (formDef.isPresent()) {
                    LOGGER.info(
                            String.format("<FormDef> with OID: %s found!",
                                    formDef.get().getOID()));
                } else {
                    String error = String.format("No corresponding <FormDef> found for <FormData> with OID: %s",
                            formData.getFormOID());
                    LOGGER.error(error);
                    throw new NoCorrespondingFormDefFoundException(error);
                }

                // Create QRS Item one level under the root
                QuestionnaireResponse.QuestionnaireResponseItemComponent item_1_x = root.addItem();
                LOGGER.info(
                        String.format("Converting <FormData> with OID: %s",
                                formData.getFormOID()));


                // Set text of the item
                // WARNING MAY PRODUCE INDEX OUT OF BOUNDS EXCEPTION
                //ToDo: insert language check via parameter
                ODMcomplexTypeDefinitionDescription formDescription = formDef.get().getDescription();
                if (formDescription.getTranslatedText().isEmpty()) {
                    String error = String.format(
                            "No <TranslatedText> in <Description> found for <FormDef> with OID: %s!",
                            formDef.get().getOID()
                    );
                    LOGGER.error(error);
                    throw new FormDescriptionNotFoundException(error);
                } else {
                    LOGGER.info("Set text of linkId 1.x Element!");
                    item_1_x.setText(formDef.get().getDescription().getTranslatedText().get(0).getValue());
                }

                // Check if form is in HashMap if not => add
                // ToDo: remove duplicated code if it works
                if (formDefLinkIdMap.containsKey(formData.getFormOID())) {
                    LOGGER.info(String.format(
                            "LinkId for <FormData> with OID: %s found",
                            formData.getFormOID()
                    ));
                    // Set linkId from HashMap
                    item_1_x.setLinkId(String.format("1.%s", formDefLinkIdMap.get(formData.getFormOID())));
                } else {
                    LOGGER.info(String.format(
                            "Generate linkId for <FormData> with OID: %s",
                            formData.getFormOID()
                    ));
                    // Set append Value in HashMap
                    formDefLinkIdMap.put(formData.getFormOID(), String.valueOf(formCounter));
                    // Get append Value in HashMap
                    item_1_x.setLinkId(String.format("1.%s", formDefLinkIdMap.get(formData.getFormOID())));
                    // increase form counter
                    formCounter++;
                }

                // List of <ItemGroupData> in <FormData>
                List<ODMcomplexTypeDefinitionItemGroupData> _itemGroupData =
                        formData.getItemGroupData();
                if (_itemGroupData.isEmpty()) {
                    String error = String.format("No <ItemGroupData> found in <FormData> with OID: %s",
                            formData.getFormOID());
                    LOGGER.error(error);
                    throw new ItemGroupDataNotFoundException(error);
                } else {
                    LOGGER.info("<ItemGroupData> found in <FormData>");
                }

                // <ItemGroupDef> list from <MetaDataVersion> list
                // will be searched for each <ItemGroupData>
                List<ODMcomplexTypeDefinitionItemGroupDef> _itemGroupDef = metaDataVersion.getItemGroupDef();

                // Loop through all <ItemGroupData>
                for (int k = 0; k < _itemGroupData.size(); k++) {
                    // Current <ItemGroupData> Element
                    ODMcomplexTypeDefinitionItemGroupData itemGroupData = _itemGroupData.get(k);

                    // Search for corresponding <ItemGroupDef> in <MetaDataVersion>
                    LOGGER.info(
                            String.format("Searching for corresponding <ItemGroupDef>" +
                                          " with OID: %s in <MetaDataVersion",
                                    itemGroupData.getItemGroupOID()));
                    Optional<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDef = _itemGroupDef.stream()
                            .filter(
                                    obj -> itemGroupData.getItemGroupOID()
                                            .equals(obj.getOID()))
                            .findFirst();

                    if (itemGroupDef.isPresent()) {
                        LOGGER.info(
                                String.format("<ItemGroupDef> with OID: %s found!",
                                        itemGroupDef.get().getOID()));
                    } else {
                        String error = String.format("No corresponding <ItemGroupDef> found for " +
                                                     "<ItemGroupData> with OID: %s",
                                itemGroupData.getItemGroupOID());
                        LOGGER.error(error);
                        throw new NoCorrespondingItemGroupDefFoundException(error);
                    }

                    // Create QRS Item one level under the Forms (1.x.x)
                    QuestionnaireResponse.QuestionnaireResponseItemComponent item_1_x_x = item_1_x.addItem();
                    LOGGER.info(
                            String.format("Converting <ItemGroupData> with OID: %s",
                                    itemGroupData.getItemGroupOID()));

                    // Set text of the item
                    // WARNING MAY PRODUCE INDEX OUT OF BOUNDS EXCEPTION
                    //ToDo: insert language check via parameter
                    ODMcomplexTypeDefinitionDescription itemGroupDescription = itemGroupDef.get().getDescription();
                    if (itemGroupDescription.getTranslatedText().isEmpty()) {
                        String error = String.format(
                                "No <TranslatedText> in <Description> found for <ItemGroupDef> with OID: %s!",
                                itemGroupDef.get().getOID()
                        );
                        LOGGER.error(error);
                        throw new ItemGroupDefDescriptionNotFoundException(error);
                    } else {
                        LOGGER.info("Set text of linkId 1.x.x Element!");
                        item_1_x_x.setText(itemGroupDescription.getTranslatedText().get(0).getValue());
                    }

                    // Check if <ItemGroup> is in HashMap if not => add
                    // ToDo: remove duplicated code if it works
                    if (itemGroupDefLinkIdMap.containsKey(itemGroupData.getItemGroupOID())) {
                        LOGGER.info(String.format(
                                "LinkId for <ItemGroupData> with OID: %s found",
                                itemGroupData.getItemGroupOID()
                        ));
                        // Set linkId from HashMap
                        item_1_x_x.setLinkId(String.format("1.%s.%s",
                                formDefLinkIdMap.get(formData.getFormOID()),
                                itemGroupDefLinkIdMap.get(itemGroupData.getItemGroupOID())));
                    } else {
                        LOGGER.info(String.format(
                                "Generate linkId for <ItemGroupData> with OID: %s",
                                itemGroupData.getItemGroupOID()
                        ));
                        // Set append Value in HashMap
                        int linkId = (k + 1);
                        itemGroupDefLinkIdMap.put(itemGroupData.getItemGroupOID(), String.valueOf(linkId));
                        // Get append Value in HashMap
                        item_1_x_x.setLinkId(String.format("1.%s.%s",
                                formDefLinkIdMap.get(formData.getFormOID()),
                                itemGroupDefLinkIdMap.get(itemGroupData.getItemGroupOID())));
                    }

                    // List of <ItemData> in <ItemGroupData>
                    List<ODMcomplexTypeDefinitionItemData> _itemData =
                            itemGroupData.getItemDataGroup();
                    if (_itemData.isEmpty()) {
                        String error = String.format("No <ItemData> found in <ItemGroupData> with OID: %s",
                                itemGroupData.getItemGroupOID());
                        LOGGER.error(error);
                        throw new ItemDataNotFoundException(error);
                    } else {
                        LOGGER.info("<ItemData> found in <ItemGroupData>");
                    }

                    // <ItemDef> list from <MetaDataVersion> list
                    // will be searched for each <ItemData>
                    List<ODMcomplexTypeDefinitionItemDef> _itemDef = metaDataVersion.getItemDef();

                    // Loop through all <ItemData>
                    for (int t = 0; t < _itemData.size(); t++) {

                        // Current <ItemData> Element
                        ODMcomplexTypeDefinitionItemData itemData = _itemData.get(t);

                        // Search for corresponding <ItemDef> in <MetaDataVersion>
                        LOGGER.info(
                                String.format("Searching for corresponding <ItemDef>" +
                                              " with OID: %s in <MetaDataVersion>",
                                        itemData.getItemOID()));
                        Optional<ODMcomplexTypeDefinitionItemDef> itemDef = _itemDef.stream()
                                .filter(
                                        obj -> itemData.getItemOID()
                                                .equals(obj.getOID()))
                                .findFirst();

                        // Check if <ItemDef> was found
                        if (itemDef.isPresent()) {
                            LOGGER.info(
                                    String.format("<ItemDef> with OID: %s found!",
                                            itemDef.get().getOID()));
                        } else {
                            String error = String.format("No corresponding <ItemDef> found for " +
                                                         "<ItemData> with OID: %s",
                                    itemData.getItemOID());
                            LOGGER.error(error);
                            throw new NoCorrespondingItemDefFoundException(error);
                        }

                        // Create QRS Item one level under the ItemGroup (1.x.x.x)
                        QuestionnaireResponse.QuestionnaireResponseItemComponent item_1_x_x_x = item_1_x_x.addItem();
                        LOGGER.info(
                                String.format("Converting <ItemData> with OID: %s",
                                        itemData.getItemOID()));

                        // Set text of the item
                        // WARNING MAY PRODUCE INDEX OUT OF BOUNDS EXCEPTION
                        //ToDo: insert language check via parameter
                        ODMcomplexTypeDefinitionQuestion itemQuestion = itemDef.get().getQuestion();
                        if (itemQuestion.getTranslatedText().isEmpty()) {
                            String error = String.format(
                                    "No <TranslatedText> in <Question> found for <ItemDef> with OID: %s!",
                                    itemDef.get().getOID()
                            );
                            LOGGER.error(error);
                            throw new ItemGroupDefDescriptionNotFoundException(error);
                        } else {
                            LOGGER.info("Set text of linkId 1.x.x.x Element!");
                            item_1_x_x_x.setText(itemQuestion.getTranslatedText().get(0).getValue());
                        }

                        // Check if <Item> is in HashMap if not => add
                        // ToDo: remove duplicated code if it works
                        if (itemDefLinkIdMap.containsKey(itemData.getItemOID())) {
                            LOGGER.info(String.format(
                                    "LinkId for <ItemData> with OID: %s found",
                                    itemData.getItemOID()
                            ));
                            // Set linkId from HashMap
                            item_1_x_x_x.setLinkId(String.format("1.%s.%s.%s",
                                    formDefLinkIdMap.get(formData.getFormOID()),
                                    itemGroupDefLinkIdMap.get(itemGroupData.getItemGroupOID()),
                                    itemDefLinkIdMap.get(itemData.getItemOID())));
                        } else {
                            LOGGER.info(String.format(
                                    "Generate linkId for <ItemData> with OID: %s",
                                    itemData.getItemOID()
                            ));
                            // Set append Value in HashMap
                            int linkId = (t + 1);
                            itemDefLinkIdMap.put(itemData.getItemOID(), String.valueOf(linkId));
                            // Get append Value in HashMap
                            item_1_x_x_x.setLinkId(String.format("1.%s.%s.%s",
                                    formDefLinkIdMap.get(formData.getFormOID()),
                                    itemGroupDefLinkIdMap.get(itemGroupData.getItemGroupOID()),
                                    itemDefLinkIdMap.get(itemData.getItemOID())));
                        }

                        if (itemDef.get().getAlias().isEmpty()) {
                            String error = String.format("No <Alias> found in <ItemDef> with OID: %s",
                                    itemDef.get().getOID());
                            LOGGER.error(error);
                            throw new NoAliasElementFound(error);
                        }

                        // Check if Item is coded
                        if (itemDef.get().getCodeListRef() != null) {
                            LOGGER.info(String.format(
                                    "<CodeListRef> found for <ItemDef> with OID: %s!",
                                    itemDef.get().getOID()
                            ));
                            ODMcomplexTypeDefinitionCodeListRef codeListRef = itemDef.get().getCodeListRef();
                            // List of <CodeList> from <MetaDataVersion>
                            List<ODMcomplexTypeDefinitionCodeList> _codeList = metaDataVersion.getCodeList();
                            // Search the list for corresponding <CodeList> from given <CodeListRef>
                            if (_codeList.isEmpty()) {
                                String error = "<CodeList> was empty!";
                                LOGGER.error(error);
                                throw new CodeListWasEmptyException(error);
                            }

                            LOGGER.info(
                                    String.format("Searching for <CodeList> with OID: %s",
                                            itemDef.get().getCodeListRef().getCodeListOID()));
                            // <CodeList> with matching OID of <CodeListRef> from <ItemDef>
                            Optional<ODMcomplexTypeDefinitionCodeList> codeList = _codeList.stream()
                                    .filter(obj ->
                                            itemDef.get().getCodeListRef().getCodeListOID().equals(obj.getOID()))
                                    .findFirst();

                            if (codeList.isPresent()) {
                                LOGGER.info(
                                        String.format("Corresponding <CodeList> found with OID: %s",
                                                codeList.get().getOID())
                                );
                                // List of <CodeListItems> inside the <CodeList>
                                List<ODMcomplexTypeDefinitionCodeListItem> _codeListItem =
                                        codeList.get().getCodeListItem();
                                LOGGER.info(
                                        String.format("Searching for <CodeListItem> with CodedValue=%s",
                                                itemData.getValue())
                                );
                                if (_codeListItem.isEmpty()) {
                                    String error = "<CodeList> was empty!";
                                    LOGGER.error(error);
                                    throw new NoCodeListItemsException(error);
                                }
                                // Search for matching CodedValue
                                Optional<ODMcomplexTypeDefinitionCodeListItem> codeListItem =
                                        _codeListItem.stream()
                                                .filter(obj -> itemData.getValue().equals(obj.getCodedValue()))
                                                .findFirst();

                                if (codeListItem.isPresent()) {
                                    // Set coded Value as answer
                                    // WARNING may produce IndexOutOfBoundsException
                                    // ToDo: parameterize language selection in order to prevent IndexOutOfBounds
                                    item_1_x_x_x.addAnswer()
                                            .setValue(
                                                    new Coding(
                                                            "http://loinc.org",
                                                            itemDef.get().getAlias().get(0).getName(),
                                                            codeListItem.get().getDecode()
                                                                    .getTranslatedText().get(0).getValue()
                                                    )
                                            );
                                } else {
                                    String error = String.format("No matching <CodeListItem> with" +
                                                                 " CodedValue=%s found!",
                                            itemData.getValue()
                                    );
                                    LOGGER.error(error);
                                    throw new NoMatchingCodeListItemException(error);
                                }

                            } else {
                                String error = String.format("No corresponding <CodeList> found for <CodeListRef>" +
                                                             "with OID: %s",
                                        itemDef.get().getCodeListRef().getCodeListOID());
                                LOGGER.error(error);
                                throw new NoCorrespondingCodeListException(error);
                            }

                        } else {
                            LOGGER.info(String.format(
                                    "No coding for <ItemDef> with OID: %s found!",
                                    itemDef.get().getOID()
                            ));
                            // Insert answer[] into item
                            // WARNING may produce IndexOutOfBoundsException
                            // ToDo: generalize here and fix IndexOutOfBounds Alias
                            item_1_x_x_x.addAnswer()
                                    .setValue(
                                            new Coding(
                                                    "http://loinc.org",
                                                    itemDef.get().getAlias().get(0).getName(),
                                                    itemData.getValue()
                                            )
                                    );
                        }

                    }

                }
            }

            // Add finished QuestionnaireResponse to the _qrs List
            LOGGER.info("Add QuestionnaireResponse to List!");
            _qrs.add(qrs);

        }

        // Add all QuestionnaireResponse Resources from the List the Bundle
        if (_qrs.isEmpty()) {
            String error = "No QuestionnaireResponse was found in the List!";
            LOGGER.error(error);
            throw new NoQuestionnaireResponseFoundException(error);
        }

        for (QuestionnaireResponse qrs : _qrs) {
            bundle.addEntry().setResource(qrs);
        }

        return fhirContext.newJsonParser().setPrettyPrint(true).encodeToString(bundle);
    }

    private Bundle createBundle() {
        Bundle bundle = new Bundle();
        bundle.setType(Bundle.BundleType.COLLECTION);
        return bundle;
    }

    private QuestionnaireResponse createQuestionnaireResponseBase() {
        QuestionnaireResponse qrs = new QuestionnaireResponse();
        qrs.setQuestionnaire("http://link_to_questionnaire");
        qrs.setStatus(QuestionnaireResponse.QuestionnaireResponseStatus.COMPLETED);
        return qrs;
    }


    @Override
    public void printClinicalData(ODM odm) {
        LOGGER.info("Received ODM in converter service");
        List<ODMcomplexTypeDefinitionClinicalData> _clinicalData = odm.getClinicalData();
        List<ODMcomplexTypeDefinitionStudy> _study = odm.getStudy();
        ODMcomplexTypeDefinitionStudy study = null;
        ODMcomplexTypeDefinitionMetaDataVersion metaDataVersion = null;
        if (_study.isEmpty()) {
            LOGGER.warn("No <Study> was found!");
        } else {
            LOGGER.info("Only <Study> at index 0 is picked!");
            study = _study.get(0);
            if (study.getMetaDataVersion().isEmpty()) {
                LOGGER.warn("No <MetaDataVersion> was found in <Study>");
            } else {
                LOGGER.info("Only <MetaDataVersion> at index 0 is picked!");
                metaDataVersion = study.getMetaDataVersion().get(0);
            }
        }
        // <ClinicalData> => FHIR Bundle
        if (!_clinicalData.isEmpty()) {
            LOGGER.info("Only <ClinicalData> element at index 0 is picked!");
            ODMcomplexTypeDefinitionClinicalData clinicalData = _clinicalData.get(0);
            List<ODMcomplexTypeDefinitionSubjectData> _subjectData = clinicalData.getSubjectData();
            if (!_subjectData.isEmpty()) {
                LOGGER.info("Only <SubjectData> from Patient at index 0 is picked!");
                ODMcomplexTypeDefinitionSubjectData subjectData = _subjectData.get(0);
                List<ODMcomplexTypeDefinitionStudyEventData> _studyEventData = subjectData.getStudyEventData();
                if (!_studyEventData.isEmpty()) {
                    LOGGER.info(String.format("Detected amount of <StudyEventData>: %d", _studyEventData.size()));
                    // Loop through all <StudyEventData> in <SubjectData> => QuestionnaireResponse
                    for (ODMcomplexTypeDefinitionStudyEventData studyEventData : _studyEventData) {
                        LOGGER.info(String.format("Looking into <StudyEventData> with OID: %s",
                                studyEventData.getStudyEventOID()));
                        if (metaDataVersion == null) {
                            LOGGER.warn("No <StudyEventDef> cloud be found because <MetaDataVersion> at index 0 was null!");
                        } else {
                            //List of <StudyEventDef> from <MetaDataVersion>
                            List<ODMcomplexTypeDefinitionStudyEventDef> _studyEvents
                                    = metaDataVersion.getStudyEventDef();
                            // search in the <StudyEventDef> list for the first definition
                            // with same OID as the <StudyEventData>
                            LOGGER.info(
                                    String.format("Searching for corresponding <StudyEventDef> in <MetaDataVersion>" +
                                                  " for <StudyEventData> with OID: %s",
                                            studyEventData.getStudyEventOID()));
                            Optional<ODMcomplexTypeDefinitionStudyEventDef> studyEventDef = _studyEvents.stream()
                                    .filter(obj -> studyEventData.getStudyEventOID().equals(obj.getOID()))
                                    .findFirst();
                            // <StudyEventDef> found!
                            if (studyEventDef.isPresent()) {
                                LOGGER.info(
                                        String.format("<StudyEventDef> with OID: %s found!",
                                                studyEventDef.get().getOID()));
                                // List of <FormData> in <StudyEventData> => 1.x
                                // <FormData> => link.id 1.x under the root Level in QRS
                                List<ODMcomplexTypeDefinitionFormData> _formData = studyEventData.getFormData();
                                if (!_formData.isEmpty()) {
                                    LOGGER.info("<FormData> found!");
                                    // <FormDef> list from <MetaDataVersion> this list
                                    // will be searched for each <FormData>
                                    List<ODMcomplexTypeDefinitionFormDef> _formDef = metaDataVersion.getFormDef();
                                    for (ODMcomplexTypeDefinitionFormData formData : _formData) {
                                        LOGGER.info(
                                                String.format("Looking into <FormData> with OID: %s",
                                                        formData.getFormOID()));
                                        // Search for corresponding <FormDef> in <MetaDataVersion>
                                        LOGGER.info(
                                                String.format("Searching for corresponding " +
                                                              "<FormDef> with OID: %s in <MetaDataVersion>",
                                                        formData.getFormOID()));
                                        Optional<ODMcomplexTypeDefinitionFormDef> formDef = _formDef.stream()
                                                .filter(obj -> formData.getFormOID().equals(obj.getOID()))
                                                .findFirst();
                                        // corresponding <FormDef> for <FormData> found!
                                        if (formDef.isPresent() && !_formDef.isEmpty()) {
                                            LOGGER.info(
                                                    String.format("<FormDef> with OID: %s found!",
                                                            formDef.get().getOID()));
                                            List<ODMcomplexTypeDefinitionItemGroupData> _itemGroupData =
                                                    formData.getItemGroupData();
                                            LOGGER.info(
                                                    String.format("Detected amount of <ItemGroupData> in " +
                                                                  "<FormData> with OID: %s (amount -> %d)",
                                                            formData.getFormOID(), _itemGroupData.size()));
                                            if (!_itemGroupData.isEmpty()) {
                                                // List of <ItemGroupDef> in <MetaDataVersion>
                                                List<ODMcomplexTypeDefinitionItemGroupDef> _itemGroupDef =
                                                        metaDataVersion.getItemGroupDef();
                                                // Loop through all <ItemGroupData>
                                                for (ODMcomplexTypeDefinitionItemGroupData itemGroupData :
                                                        _itemGroupData) {
                                                    LOGGER.info(
                                                            String.format("Looking into <ItemGroupData> with OID: %s",
                                                                    itemGroupData.getItemGroupOID()));
                                                    // Search for corresponding <ItemGroupDef> in <MetaDataVersion>
                                                    LOGGER.info(
                                                            String.format("Searching for corresponding <ItemGroupDef>" +
                                                                          " with OID: %s in <MetaDataVersion",
                                                                    itemGroupData.getItemGroupOID()));
                                                    Optional<ODMcomplexTypeDefinitionItemGroupDef> itemGroupDef =
                                                            _itemGroupDef.stream()
                                                                    .filter(obj -> itemGroupData.getItemGroupOID()
                                                                            .equals(obj.getOID()))
                                                                    .findFirst();
                                                    // Found corresponding <ItemGroupDef>
                                                    if (itemGroupDef.isPresent() && !_itemGroupDef.isEmpty()) {
                                                        LOGGER.info(String.format("<ItemGroupDef> with OID: %s found!",
                                                                itemGroupDef.get().getOID()));
                                                        // List of <ItemData> in the <ItemGroup>
                                                        List<ODMcomplexTypeDefinitionItemData> _itemData =
                                                                itemGroupData.getItemDataGroup();
                                                        LOGGER.info(
                                                                String.format("Detected amount of <ItemData> in " +
                                                                              "<ItemGroupData> with OID: %s (amount -> %d)",
                                                                        itemGroupData.getItemGroupOID(), _itemData.size()));
                                                        if (!_itemData.isEmpty()) {
                                                            //List of all <ItemDef> in <MetaDataVersion>
                                                            List<ODMcomplexTypeDefinitionItemDef> _itemDef =
                                                                    metaDataVersion.getItemDef();
                                                            // Loop through all <ItemData>
                                                            for (ODMcomplexTypeDefinitionItemData itemData :
                                                                    _itemData) {
                                                                LOGGER.info(
                                                                        String.format("Looking into <ItemData> " +
                                                                                      "with OID: %s",
                                                                                itemData.getItemOID())
                                                                );
                                                                // Search for the corresponding <ItemDef>
                                                                // in <MetaDataVersion>
                                                                Optional<ODMcomplexTypeDefinitionItemDef> itemDef =
                                                                        _itemDef.stream()
                                                                                .filter(obj -> itemData.getItemOID()
                                                                                        .equals(obj.getOID()))
                                                                                .findFirst();
                                                                // Found corresponding <ItemDef>
                                                                if (itemDef.isPresent() && !_itemDef.isEmpty()) {
                                                                    // INFO: just testing <Alias> assuming it's always at
                                                                    // index 0
                                                                    try {
                                                                        LOGGER.info(String.format(
                                                                                "ItemValue: %s ; OID: %s ; Alias: %s",
                                                                                itemData.getValue(),
                                                                                itemData.getItemOID(),
                                                                                itemDef.get().getAlias().get(0).getName()
                                                                        ));
                                                                    } catch (IndexOutOfBoundsException e) {
                                                                        LOGGER.warn("No <Alias> in <ItemDef>");
                                                                        LOGGER.info(String.format(
                                                                                "ItemValue: %s ; OID: %s",
                                                                                itemData.getValue(),
                                                                                itemData.getItemOID()
                                                                        ));
                                                                    }
                                                                } else {
                                                                    LOGGER.warn(
                                                                            String.format("No <ItemDef> was found " +
                                                                                          "in <MetaDataVersion> with OID: %s",
                                                                                    itemData.getItemOID()));
                                                                }
                                                            }
                                                        } else {
                                                            LOGGER.info(String.format("No <ItemData> was found " +
                                                                                      "in <ItemGroupData> with OID: %s",
                                                                    itemGroupData.getItemGroupOID()));
                                                        }
                                                    } else {
                                                        LOGGER.warn(
                                                                String.format("No <ItemGroupDef> was found " +
                                                                              "in <MetaDataVersion> with OID: %s",
                                                                        itemGroupData.getItemGroupOID()));
                                                    }
                                                }
                                            } else {
                                                LOGGER.warn(
                                                        String.format("No <ItemGroupData> was found " +
                                                                      "in <FormData> with OID: %s",
                                                                formData.getFormOID()));
                                            }
                                        } else {
                                            // No <FormDef> found in <MetaDataVersion>
                                            LOGGER.warn(
                                                    String.format("No corresponding <FormDef> found for <FormData> with OID: %s",
                                                            formData.getFormOID()));
                                        }
                                    }
                                } else {
                                    LOGGER.warn(
                                            String.format("No <FormData> found in <StudyEventData> with OID: %s",
                                                    studyEventData.getStudyEventOID()));
                                }

                            } else {
                                // <StudyEventDef> not found!
                                LOGGER.warn(
                                        String.format("No corresponding <StudyEventDef> with OID: %s found!",
                                                studyEventData.getStudyEventOID()));
                            }
                        }
                    }
                } else {
                    LOGGER.warn("No <StudyEventData> was found!");
                }
            } else {
                LOGGER.warn("No <SubjectData> was found!");
            }
        } else {
            LOGGER.warn("No <ClinicalData> element was found!");
        }
    }
}
