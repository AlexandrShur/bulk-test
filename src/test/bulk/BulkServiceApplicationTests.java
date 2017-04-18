package bulk;

import bulk.dto.Rule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BulkServiceApplicationTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    public static final String CONTRACT_BASE_URI_WILCO = AppRolesResource.CONTRACT_BASE_URI.replace("{tenant}", "Wilco") + "/_bulk";


    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void doubleRules_forNoUniqueRules_FailedRules() throws Exception {

        // Arrange
        String urlTemplate =  CONTRACT_BASE_URI_WILCO;
        List<Rule> rules = new ArrayList<>();
        int failedRuleCount = 1;
        int acceptedRuleCount = 1;
        rules.add(new Rule(0, "SomeName", "Some desciption", "Some"));
        rules.add(new Rule(0, "SomeName", "Some desciption", "Some"));

        // Act
        ResultActions resultActions = mvc.perform(post(urlTemplate)
                .content(toJsonString(rules))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.failedRules", is(failedRuleCount)))
                .andExpect(jsonPath("$.acceptedRules", is(acceptedRuleCount)));
    }

    @Test
    public void failedRule_forRulesDidntPassValidation_FailedRules() throws Exception {

        // Arrange
        String urlTemplate = CONTRACT_BASE_URI_WILCO;
        List<Rule> rules = new ArrayList<>();
        int failedRuleCount = 2;
        rules.add(new Rule(0, "NomeName", "Some desciption", "Name"));
        rules.add(new Rule(0, "GomeName", "Some desciption", "same"));

        // Act
        ResultActions resultActions = mvc.perform(post(urlTemplate)
                .content(toJsonString(rules))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.failedRules", is(failedRuleCount)));
    }

    @Test
    public void acceptedRules_AllCorrectRules_FailedRules() throws Exception {

        // Arrange
        String urlTemplate = CONTRACT_BASE_URI_WILCO;
        List<Rule> rules = new ArrayList<>();
        int acceptedRuleCount = 1;
        rules.add(new Rule(0, "AccName", "Some desciption", "Acc"));

        // Act
        ResultActions resultActions = mvc.perform(post(urlTemplate)
                .content(toJsonString(rules))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptedRules", is(acceptedRuleCount)));
    }

    @Test
    public void largeRequestRule_RulesToValidate_HttpStatusOk() throws Exception {

        // Arrange
        String urlTemplate =  CONTRACT_BASE_URI_WILCO;
        List<Rule> rules = new ArrayList<>();
        int nameRand;
        int appRand;
        int maxListSize = 200000;
        for (int i = 0; i < maxListSize; i++) {
            nameRand = (int)(Math.random() * 3 + 1);
            appRand = (int)(Math.random() * 3 + 1);
            rules.add(new Rule(0, nameRand + "AccName", "Some description", appRand + "Acc"));
        }

        // Act
        ResultActions resultActions = mvc.perform(post(urlTemplate)
                .content(toJsonString(rules))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON));

        // Assert
        resultActions.andExpect(status().isOk());
    }

    /**
     * Convert {@link Object} type to JSON {@link String}.
     *
     * @param object object to convert.
     * @return JSON {@link String}
     */
    public static String toJsonString(final Object object) {
        try {
            final ObjectMapper objectMapper = new ObjectMapper();
            final String json = objectMapper.writeValueAsString(object);
            return json;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
