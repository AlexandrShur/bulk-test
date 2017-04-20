package bulk;

import bulk.dto.BulkResponse;
import bulk.dto.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.common.io.ByteStreams;
import com.google.common.io.ByteStreams;
import org.apache.tomcat.util.ExceptionUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.*;
import javax.servlet.descriptor.JspConfigDescriptor;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BulkServiceApplicationTests {

    @Autowired
    Database database;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private TestRestTemplate restTemplate;

    @LocalServerPort
    private int port;

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
        ZipEntry entry = new ZipEntry(toJsonString(roles));
        MockHttpServletRequestBuilder msb = post(urlTemplate)
                .content(entry.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .header("Content-Encoding", "gzip")
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

    @Test
    public void testCompression() throws Exception {

        // Arrange
        String json = "[{\"name\":\"name\",\"description\": \"descitption\",\"application\": \"application\"}]";
        byte[] jsonb = compress(json.getBytes());
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Content-Type", MediaType.APPLICATION_JSON.toString());
        requestHeaders.set("Content-Encoding", "gzip");
        HttpEntity<?> requestEntity = new HttpEntity<>(jsonb, requestHeaders);

        // Act
        ResponseEntity<BulkResponse> entity = this.restTemplate.exchange("http://localhost:" + port+CONTRACT_BASE_URI_WILCO, HttpMethod.POST,
                requestEntity, BulkResponse.class);

        // Assert
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody().getAcceptedRoles().size() == 1);
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

    /**
     * Compress source.
     *
     * @param source source to compress.
     * @return compressed byte array.
     */
    private byte[] compress(byte[] source) throws IOException {
        if (source == null || source.length == 0) {
            return source;
        }

        ByteArrayInputStream sourceStream = new ByteArrayInputStream(source);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.length / 2);
        OutputStream compressor = new GZIPOutputStream(outputStream);

        try {
            ByteStreams.copy(sourceStream, compressor);
        } finally {
            compressor.close();
        }

        return outputStream.toByteArray();
    }
}
