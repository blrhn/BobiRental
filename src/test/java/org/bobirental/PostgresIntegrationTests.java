package org.bobirental;

import org.bobirental.client.ClientRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledInNativeImage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.DockerClientFactory;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = { "spring.docker.compose.skip.in-tests=false",
        "spring.docker.compose.start.arguments=--force-recreate,--renew-anon-volumes,postgres" })
@ActiveProfiles("postgres")
@DisabledInNativeImage
public class PostgresIntegrationTests {
    @LocalServerPort
    private int port;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RestTemplateBuilder restTemplateBuilder;

    @BeforeAll
    static void available() {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(), "Docker unavailable");
    }

    @Test
    void testClientDetails() {
        RestTemplate template = restTemplateBuilder.rootUri("http://localhost:" + port).build();
        ResponseEntity<String> result = template.exchange(RequestEntity.get("/clients/1").build(), String.class);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void testFindAll() throws Exception {
        clientRepository.findAll();
    }
}
