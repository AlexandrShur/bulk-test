package bulk;

import bulk.dto.BulkResponse;
import bulk.dto.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.ByteStreams;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.TestPropertySource;
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

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations="classpath:test.properties")
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
        MockHttpServletRequestBuilder msb = post(urlTemplate)
                .content(toJsonString(roles))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON);

        // Act
        ResultActions resultActions = mvc.perform(msb);

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
        List<Role> roles = generateRoles(200000);
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
        List<Role> roles = generateRoles(20000);
        byte[] compressed = compress(toJsonString(roles));
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Content-Type", MediaType.APPLICATION_JSON.toString());
        requestHeaders.set("Content-Encoding", "gzip");
        HttpEntity<?> requestEntity = new HttpEntity<>(compressed, requestHeaders);

        // Act
        ResponseEntity<BulkResponse> entity = this.restTemplate.exchange("http://localhost:" + port+CONTRACT_BASE_URI_WILCO, HttpMethod.POST,
                requestEntity, BulkResponse.class);

        // Assert
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(entity.getBody().getAcceptedRoles().size() == 1);
    }

    @Test
    public void testDecompression() throws Exception {

        // Arrange
        List<Role> roles = generateRoles(20000);
        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.set("Content-Type", MediaType.APPLICATION_JSON.toString());
        requestHeaders.set("Accept-Encoding", "gzip");
        HttpEntity<?> requestEntity = new HttpEntity<>(toJsonString(roles), requestHeaders);

        // Act
        ResponseEntity<byte[]> entity = this.restTemplate.exchange("http://localhost:" + port+CONTRACT_BASE_URI_WILCO, HttpMethod.POST,
                requestEntity, byte[].class);

        // Assert
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        String decompressedResponse = deCompress(entity.getBody());
        System.out.println(decompressedResponse);
        assertThat(decompressedResponse.contains("acceptedRoles")).isEqualTo(true);
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
    private byte[] compress(String source) throws IOException {
        if (source == null || source.isEmpty()) {
            return null;
        }

        ByteArrayInputStream sourceStream = new ByteArrayInputStream(source.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream(source.length() / 2);
        OutputStream compressor = new GZIPOutputStream(outputStream);

        try {
            ByteStreams.copy(sourceStream, compressor);
        } finally {
            compressor.close();
        }
        byte[] compressed = outputStream.toByteArray();
        outputStream.close();
        return compressed;
    }

    /**
     * Decompress source.
     *
     * @param source source to decompress.
     * @return decompressed response.
     */
    private String deCompress(byte[] source) throws IOException {
        String deCompressedResponse = new String();
        if (source == null || source.length == 0) {
            return deCompressedResponse;
        }

        ByteArrayInputStream sourceStream = new ByteArrayInputStream(source);
        InputStream deCompressor = new GZIPInputStream(sourceStream);
        InputStreamReader reader = new InputStreamReader(deCompressor);
        BufferedReader in = new BufferedReader(reader);

        String readed;
        while ((readed = in.readLine()) != null) {
            deCompressedResponse += readed;
            System.out.println(deCompressedResponse);
        }
        deCompressor.close();
        return deCompressedResponse;
    }

    /**
     * Generate roles.
     *
     * @param listSize size of role list to return.
     * @return generated role list.
     */
    private List<Role> generateRoles(int listSize) {
        List<Role> roles = new ArrayList<>();
        int nameRand;
        int appRand;
        for (int i = 0; i < listSize; i++) {
            nameRand = (int)(Math.random() * 3 + 1);
            appRand = (int)(Math.random() * 3 + 1);
            roles.add(new Role(0, nameRand + "AccName", "Some description", appRand + "Acc"));
        }
        return roles;
    }
}
