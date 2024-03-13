package de.imi;


import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class ConverterApiTests {

    // Api mocking
    @Autowired
    private MockMvc mockMvc;

    // Logger
    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(ConverterApiTests.class);

    // Object mapper (JSON)
    private final ObjectMapper objectMapper = new ObjectMapper();

    // XML mapper
    //XmlMapper xmlMapper = new XmlMapper();

    // Fhir context
    private final FhirContext fhirContext = FhirContext.forR4();

    // String mapper
    private final IParser fhirJsonParser = fhirContext.newJsonParser();


    @Test
    @Disabled
    public void testMEDVLymphomaQuestionnaire() throws Exception {

        String xmlContent = "";

        // Load valid XML file from resources
        try {
            LOGGER.info("Loading XML file from resources");
            ClassPathResource xmlFile = new ClassPathResource("testFiles/MEDV Lymphoma/MEDVFormularLymphom.xml");
            xmlContent = new String(Files.readAllBytes(xmlFile.getFile().toPath()));
            LOGGER.info("Successfully read XML file!");
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        //Check if data was read
        if (xmlContent.isEmpty()) {
            fail("XML String was empty!");
        }

        // try for request
        try {
            // Set required headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("questionnaire-language", "de");
            headers.add("questionnaire-link", "http://example.org/fhir/Questionnaire/1");

            // Send XML to Api => expect 200 Code because XML is valid
            ResultActions postRequestGerman = postToApi(
                    xmlContent,
                    headers
            );

            // Check response code and data type
            postRequestGerman
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"));

            // Extract response
            MockHttpServletResponse response = postRequestGerman.andReturn().getResponse();

            // Load validated JSON from resources
            ClassPathResource jsonFile = new ClassPathResource("testFiles/MEDV Lymphoma/MEDVConvertedJsonLymphoma.json");
            String jsonContent = new String(Files.readAllBytes(jsonFile.getFile().toPath()));

            // parse to json objects
            JsonNode jsonApi = objectMapper.readTree(response.getContentAsString());
            JsonNode jsonResources = objectMapper.readTree(jsonContent);

            // test if the two jsons are identical
            assertEquals(jsonApi, jsonResources);

        } catch (Exception e) {
            fail(e.getMessage());
        }


    }

    @Test
    @Disabled
    public void testWHO5Questionnaire() {
        try {
            ClassPathResource xmlFile = new ClassPathResource("testFiles/WHO5 /WHO5WithTestData.xml");
            String xmlContent = new String(Files.readAllBytes(xmlFile.getFile().toPath()));

            if (xmlContent.isEmpty()) {
                fail("XML String was empty!");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("questionnaire-language", "de");
            headers.add("questionnaire-link", "http://link-to-questionnaire");

            // Send XML to Api => expect 200 Code because XML is valid
            ResultActions postRequestGerman = postToApi(
                    xmlContent,
                    headers
            );

            // Check response code and data type
            postRequestGerman
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"));

            // Extract response
            MockHttpServletResponse response = postRequestGerman.andReturn().getResponse();

            // Load validated JSON from resources
            ClassPathResource jsonFile = new ClassPathResource("testFiles/WHO5 /WHO5WithTestData.json");
            String jsonContent = new String(Files.readAllBytes(jsonFile.getFile().toPath()));

            // parse to json objects
            JsonNode jsonApi = objectMapper.readTree(response.getContentAsString());
            JsonNode jsonResources = objectMapper.readTree(jsonContent);

            // test if the two jsons are identical
            assertEquals(jsonApi, jsonResources);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Disabled
    public void testWHO5QuestionnaireRepeating() {
        try {
            ClassPathResource xmlFile = new ClassPathResource("testFiles/WHO5 repeating/WHO5WithTestDataRepeating.xml");
            String xmlContent = new String(Files.readAllBytes(xmlFile.getFile().toPath()));

            if (xmlContent.isEmpty()) {
                fail("XML String was empty!");
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("questionnaire-language", "de");
            headers.add("questionnaire-link", "http://link-to-questionnaire");

            // Send XML to Api => expect 200 Code because XML is valid
            ResultActions postRequestGerman = postToApi(
                    xmlContent,
                    headers
            );

            // Check response code and data type
            postRequestGerman
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"));

            // Extract response
            MockHttpServletResponse response = postRequestGerman.andReturn().getResponse();

            // Load validated JSON from resources
            ClassPathResource jsonFile = new ClassPathResource("testFiles/WHO5 repeating/WHO5WithTestDataRepeating.json");
            String jsonContent = new String(Files.readAllBytes(jsonFile.getFile().toPath()));

            // parse to json objects
            JsonNode jsonApi = objectMapper.readTree(response.getContentAsString());
            JsonNode jsonResources = objectMapper.readTree(jsonContent);

            // test if the two jsons are identical
            assertEquals(jsonApi, jsonResources);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    @Disabled
    public void testLanguageCodeSwitch() {
        try {
            ClassPathResource xmlFile = new ClassPathResource("testFiles/WHO5 repeating/WHO5WithTestDataRepeating.xml");
            String xmlContent = new String(Files.readAllBytes(xmlFile.getFile().toPath()));

            if (xmlContent.isEmpty()) {
                fail("XML String was empty!");
            }

            HttpHeaders headersGerman = new HttpHeaders();
            headersGerman.add("questionnaire-language", "de");
            headersGerman.add("questionnaire-link", "http://link-to-questionnaire");

            HttpHeaders headersEnglish = new HttpHeaders();
            headersEnglish.add("questionnaire-language", "en");
            headersEnglish.add("questionnaire-link", "http://link-to-questionnaire");

            ResultActions postRequestGerman = postToApi(
                    xmlContent,
                    headersGerman
            );

            ResultActions postRequestEnglish = postToApi(
                    xmlContent,
                    headersEnglish
            );

            // Check response code and data type
            postRequestGerman
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"));

            // Check response code and data type
            postRequestEnglish
                    .andExpect(status().isOk())
                    .andExpect(content().contentType("application/json;charset=UTF-8"));

            JsonNode jsonGerman = objectMapper.readTree(
                    postRequestGerman.andReturn().getResponse().getContentAsString());

            JsonNode jsonEnglish = objectMapper.readTree(
                    postRequestEnglish.andReturn().getResponse().getContentAsString());

            assertNotEquals(jsonGerman, jsonEnglish);

        } catch (Exception e) {
            fail(e.getMessage());
        }

    }

    @Test
    @Disabled
    public void testRejectionOfInvalidResources() {
        try {

            // XML Objects
            ClassPathResource xmlFileNoClinicalData =
                    new ClassPathResource("testFiles/Invalid resources/WHO5NoClinicalData.xml");
            ClassPathResource xmlFileNoStudy =
                    new ClassPathResource("testFiles/Invalid resources/WHO5NoStudy.xml");
            ClassPathResource xmlNoMatchingMetaDataV =
                    new ClassPathResource("testFiles/Invalid resources/WHO5NoMatchingMetaDataV.xml");
            ClassPathResource xmlFileMissingElementDef =
                    new ClassPathResource("testFiles/Invalid resources/WHO5MissingElementDef.xml");

            // Data Strings
            String xmlContentNoClinicalData =
                    new String(Files.readAllBytes(xmlFileNoClinicalData.getFile().toPath()));
            String xmlContentNoStudy =
                    new String(Files.readAllBytes(xmlFileNoStudy.getFile().toPath()));
            String xmlContentNoMatchingMetaDataV =
                    new String(Files.readAllBytes(xmlNoMatchingMetaDataV.getFile().toPath()));
            String xmlMissingElementDef =
                    new String(Files.readAllBytes(xmlFileMissingElementDef.getFile().toPath()));

            if (xmlContentNoClinicalData.isEmpty()) {
                fail("XMLNoClinicalData String was empty!");
            }

            if (xmlContentNoStudy.isEmpty()) {
                fail("XMLNoStudy String was empty!");
            }

            if (xmlContentNoMatchingMetaDataV.isEmpty()) {
                fail("XMLNoMatchingMetaDataV was empty!");
            }

            if (xmlMissingElementDef.isEmpty()) {
                fail("XMLMissingElementDef String was empty!");
            }

            // use valid headers
            HttpHeaders headers = new HttpHeaders();
            headers.add("questionnaire-language", "de");
            headers.add("questionnaire-link", "http://link-to-questionnaire");

            ResultActions postRequestNoClinicalData = postToApi(
                    xmlContentNoClinicalData,
                    headers
            );

            ResultActions postRequestNoStudy = postToApi(
                    xmlContentNoStudy,
                    headers
            );

            ResultActions postRequestNoMatchingMetaDataV = postToApi(
                    xmlContentNoMatchingMetaDataV,
                    headers
            );

            ResultActions postRequestMissingElementDef = postToApi(
                    xmlMissingElementDef,
                    headers
            );

            // Check status code (bad request for wrong xml format)
            postRequestNoClinicalData
                    .andExpect(status().is(400));

            postRequestNoStudy
                    .andExpect(status().is(400));

            postRequestNoMatchingMetaDataV
                    .andExpect(status().is(400));

            postRequestMissingElementDef
                    .andExpect(status().is(400));

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    private ResultActions postToApi(String content, HttpHeaders headers) throws Exception {
        return mockMvc.perform(
                post("/converter")
                        .contentType("application/xml;charset=UTF-8")
                        .content(content)
                        .headers(headers)
        );
    }
}
