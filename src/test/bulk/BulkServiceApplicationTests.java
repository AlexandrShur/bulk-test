package bulk;

import bulk.dto.BulkResponse;
import bulk.dto.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.BDDAssertions.then;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class BulkServiceApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Test
    public void testDoubleRule() {
        List<Rule> rules = new ArrayList<>();
        int failedRuleCount = 1;
        rules.add(new Rule(0, "SomeName", "Some desciption", "Some"));
        rules.add(new Rule(0, "SomeName", "Some desciption", "Some"));
        ResponseEntity<BulkResponse> entity = this.testRestTemplate.postForEntity("http://localhost:" + this.port + "/api/v1/cds/app-roles/bulk", rules, BulkResponse.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(entity.getBody().getFailedRules());
        then( entity.getBody().getFailedRules()).isEqualTo(failedRuleCount);
    }

    @Test
    public void testFailedRule() {
        List<Rule> rules = new ArrayList<>();
        int failedRuleCount = 2;
        rules.add(new Rule(0, "NomeName", "Some desciption", "Name"));
        rules.add(new Rule(0, "GomeName", "Some desciption", "same"));
        ResponseEntity<BulkResponse> entity = this.testRestTemplate.postForEntity("http://localhost:" + this.port + "/api/v1/cds/app-roles/bulk", rules, BulkResponse.class);
        System.out.println(entity.getBody());
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then( entity.getBody().getFailedRules()).isEqualTo(failedRuleCount);
    }

    @Test
    public void testAllAcceptedRule() {
        List<Rule> rules = new ArrayList<>();
        int acceptedRuleCount = 1;
        rules.add(new Rule(0, "AccName", "Some desciption", "Acc"));
        ResponseEntity<BulkResponse> entity = this.testRestTemplate.postForEntity("http://localhost:" + this.port + "/api/v1/cds/app-roles/bulk", rules, BulkResponse.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        then( entity.getBody().getAcceptedRules()).isEqualTo(acceptedRuleCount);
    }

    @Test
    public void testLargeRequestRule() {
        List<Rule> rules = new ArrayList<>();
        int nameRand;
        int appRand;
        int maxListSize = 200000;
        for (int i = 0; i < maxListSize; i++) {
            nameRand = (int)(Math.random() * 3 + 1);
            appRand = (int)(Math.random() * 3 + 1);
            rules.add(new Rule(0, nameRand + "AccName", "Some description", appRand + "Acc"));
        }
        ResponseEntity<BulkResponse> entity = this.testRestTemplate.postForEntity("http://localhost:" + this.port + "/api/v1/cds/app-roles/bulk", rules, BulkResponse.class);
        then(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
