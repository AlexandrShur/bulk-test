package bulk;

import bulk.dto.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

import java.util.ArrayList;
import java.util.List;


@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BulkServiceApplicationTests {


    @Autowired
    Database database;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mvc;

    public static final String CONTRACT_BASE_URI_WILCO = AppRolesResource.CONTRACT_BASE_URI.replace("{tenant}", "Wilco") + "/_bulk";


    @Before
    public void setUp() throws Exception {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
        database.resetDatabase();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void doubleRoles_forNoUniqueRoles_FailedRoles() throws Exception {

        // Arrange
        String urlTemplate =  CONTRACT_BASE_URI_WILCO;
        List<Role> roles = new ArrayList<>();
        int failedRoleCount = 1;
        int acceptedRoleCount = 1;
        roles.add(new Role(0, "SomeName", "Some desciption", "Some"));
        roles.add(new Role(0, "SomeName", "Some desciption", "Some"));
        MockHttpServletRequestBuilder msb = post(urlTemplate)
                .content(toJsonString(roles))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        // Act
        ResultActions resultActions = mvc.perform(msb);

        System.out.println("reponse: " + resultActions.andReturn().getResponse().getContentAsString());

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.rejectedRoles", hasSize(failedRoleCount)))
                .andExpect(jsonPath("$.acceptedRoles", hasSize(acceptedRoleCount)))
                .andExpect(jsonPath("$.acceptedRoles.[0].id", is(1)))
                .andExpect(jsonPath("$.rejectedRoles.[0].role.id", is(0)))
                .andExpect(jsonPath("$.rejectedRoles.[0].status", is("Failed")));
    }

    @Test
    public void failedRole_forRolesDidntPassValidation_FailedRoles() throws Exception {

        // Arrange
        String urlTemplate = CONTRACT_BASE_URI_WILCO;
        List<Role> roles = new ArrayList<>();
        int failedRoleCount = 2;
        roles.add(new Role(0, "NomeName", "Some desciption", ""));
        roles.add(new Role(0, "", "Some desciption", "same"));
        MockHttpServletRequestBuilder msb = post(urlTemplate)
                .content(toJsonString(roles))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        // Act
        ResultActions resultActions = mvc.perform(msb);

        System.out.println("reponse: " + resultActions.andReturn().getResponse().getContentAsString());

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.rejectedRoles", hasSize(failedRoleCount)))
                .andExpect(jsonPath("$.rejectedRoles.[0].role.id", is(0)))
                .andExpect(jsonPath("$.rejectedRoles.[0].status", is("Failed")))
                .andExpect(jsonPath("$.rejectedRoles.[1].role.id", is(0)))
                .andExpect(jsonPath("$.rejectedRoles.[1].status", is("Failed")));

    }

    @Test
    public void acceptedRoles_AllCorrectRoles_FailedRoles() throws Exception {

        // Arrange
        String urlTemplate = CONTRACT_BASE_URI_WILCO;
        List<Role> roles = new ArrayList<>();
        int acceptedRoleCount = 1;
        roles.add(new Role(0, "AccName", "Some desciption", "Acc"));
        MockHttpServletRequestBuilder msb = post(urlTemplate)
                .content(toJsonString(roles))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        // Act
        ResultActions resultActions = mvc.perform(msb);

        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.acceptedRoles", hasSize(acceptedRoleCount)))
                .andExpect(jsonPath("$.acceptedRoles.[0].id", is(1)));
    }

    @Test
    public void largeRequestRole_RolesToValidate_HttpStatusOk() throws Exception {

        // Arrange
        String urlTemplate =  CONTRACT_BASE_URI_WILCO;
        List<Role> roles = new ArrayList<>();
        int nameRand;
        int appRand;
        int maxListSize = 200000;
        for (int i = 0; i < maxListSize; i++) {
            nameRand = (int)(Math.random() * 3 + 1);
            appRand = (int)(Math.random() * 3 + 1);
            roles.add(new Role(0, nameRand + "AccName", "Some description", appRand + "Acc"));
        }
        MockHttpServletRequestBuilder msb = post(urlTemplate)
                .content(toJsonString(roles))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        // Act
        ResultActions resultActions = mvc.perform(msb);

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
