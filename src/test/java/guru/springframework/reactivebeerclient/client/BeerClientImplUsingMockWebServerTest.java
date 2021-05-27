package guru.springframework.reactivebeerclient.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

import static guru.springframework.reactivebeerclient.config.WebClientConfigProperties.BEER_V2_URL_BY_BEERID;
import static guru.springframework.reactivebeerclient.model.v2.BeerStyleEnum.SAISON;
import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

public class BeerClientImplUsingMockWebServerTest {

    private final MockWebServer mockWebServer = new MockWebServer();
    private final BeerClient beerClient = new BeerClientImpl(WebClient.create(mockWebServer.url("/").toString()));

    @AfterEach
    void tearDown() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void testGetBeer() throws JsonProcessingException {
        UUID uuid = UUID.randomUUID();
        BeerDto kingFisher = BeerDto.builder().id(uuid).beerName("KingFisher")
                .beerStyle(SAISON.name())
                .price(new BigDecimal("240.50"))
                .upc("354354879832").build();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(OK.value()).setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .setBody(new ObjectMapper().writeValueAsString(kingFisher)));
        Mono<BeerDto> beerByIdMono = this.beerClient.getBeerById(uuid);
        BeerDto beerById = beerByIdMono.block();
        Assertions.assertNotNull(beerById);
        assertEquals(kingFisher.getId(), beerById.getId());
        assertEquals(kingFisher.getUpc(), beerById.getUpc());
    }

    @Test
    void listBeers() {
        mockWebServer.enqueue(new MockResponse().setResponseCode(200).setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .setBody(getAllBearsResponse()));
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();
        Assertions.assertNotNull(beerPagedList);
        Assertions.assertTrue(beerPagedList.stream().count() > 0);
    }

    @Test
    void createBeer() {
        BeerDto kingFisher = BeerDto.builder().beerName("KingFisher")
                .beerStyle(SAISON.name())
                .price(new BigDecimal("240.50"))
                .upc("354354879832").build();
        mockWebServer.enqueue(new MockResponse().setResponseCode(CREATED.value()).setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));

        Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.createBeer(kingFisher);
        ResponseEntity<Void> responseEntity = responseEntityMono.block();
        assertEquals(nonNull(responseEntity) ? responseEntity.getStatusCodeValue() : "", CREATED.value());
    }

    @Test
    void updateBeer() throws JsonProcessingException {
        BeerDto kingFisher = BeerDto.builder().id(UUID.randomUUID()).beerName("KingFisher")
                .beerStyle(SAISON.name())
                .price(new BigDecimal("240.50"))
                .upc("354354879832").build();
        mockWebServer.url(BEER_V2_URL_BY_BEERID);
        mockWebServer.enqueue(new MockResponse().setResponseCode(OK.value()).setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .setBody("{\n" +
                        "\"id\": \"19cd3637-7911-45bb-bcae-0a1c0e19fd19\",\n" +
                        "\"beerName\": \"Mango Bobs\",\n" +
                        "\"beerStyle\": \"ALE\",\n" +
                        "\"upc\": \"0631234200036\",\n" +
                        "\"price\": 37.06,\n" +
                        "\"quantityOnHand\": 3028,\n" +
                        "\"createdDate\": \"2021-05-16T16:13:27.18Z\",\n" +
                        "\"lastUpdatedDate\": null\n" +
                        "}"));

        Mono<BeerDto> beerById = this.beerClient.getBeerById(kingFisher.getId());
        BeerDto beerDto = beerById.block();
        Assertions.assertNotNull(beerDto);

        BeerDto updatedBeerDto = BeerDto.builder().beerName("Radda")
                .beerStyle(beerDto.getBeerStyle())
                .upc(beerDto.getUpc())
                .price(beerDto.getPrice())
                .createdDate(beerDto.getCreatedDate())
                .lastUpdatedDate(beerDto.getLastUpdatedDate())
                .quantityOnHand(beerDto.getQuantityOnHand())
                .build();
        mockWebServer.enqueue(new MockResponse().setResponseCode(NO_CONTENT.value()).setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.updateBeer(beerDto.getId(), updatedBeerDto);
        assertEquals(responseEntityMono.block().getStatusCodeValue(), NO_CONTENT.value());

    }

    @Test
    public void testGetBeerByUPC() throws JsonProcessingException {
        BeerDto kingFisher = BeerDto.builder().id(UUID.randomUUID()).beerName("KingFisher")
                .beerStyle(SAISON.name())
                .price(new BigDecimal("240.50"))
                .upc("354354879832").build();
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(OK.value()).setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .setBody(new ObjectMapper().writeValueAsString(kingFisher)));
        Mono<BeerDto> beerByIdMono = this.beerClient.getBeerByUPC(kingFisher.getUpc());
        BeerDto beerById = beerByIdMono.block();
        Assertions.assertNotNull(beerById);
        assertEquals(kingFisher.getUpc(), beerById.getUpc());
        assertEquals(kingFisher.getId(), beerById.getId());
    }

    @Test
    public void testDeleteBeerById() {
        mockWebServer.enqueue(new MockResponse()
                .setResponseCode(NO_CONTENT.value()).setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE));
        Mono<ResponseEntity<Void>> responseEntityMono = beerClient.deleteBeerById(UUID.randomUUID());
        ResponseEntity<Void> responseEntity = responseEntityMono.block();
        assertEquals(responseEntity.getStatusCode(), NO_CONTENT);
    }

    private String getAllBearsResponse() {
        return "{\n" +
                "    \"content\": [\n" +
                "        {\n" +
                "            \"id\": \"b0edcc5c-9b47-4b30-b911-8e11df318251\",\n" +
                "            \"beerName\": \"Mango Bobs\",\n" +
                "            \"beerStyle\": \"ALE\",\n" +
                "            \"upc\": \"0631234200036\",\n" +
                "            \"price\": 86.36,\n" +
                "            \"quantityOnHand\": 3884,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"c4eea7af-19cf-433b-a9c5-8663c6b33ffe\",\n" +
                "            \"beerName\": \"Galaxy Cat\",\n" +
                "            \"beerStyle\": \"PALE_ALE\",\n" +
                "            \"upc\": \"9122089364369\",\n" +
                "            \"price\": 34.78,\n" +
                "            \"quantityOnHand\": 4805,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"85ef7127-697e-477a-8850-6ed802d84bbc\",\n" +
                "            \"beerName\": \"No Hammers On The Bar\",\n" +
                "            \"beerStyle\": \"WHEAT\",\n" +
                "            \"upc\": \"0083783375213\",\n" +
                "            \"price\": 60.14,\n" +
                "            \"quantityOnHand\": 773,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"66967a55-6196-49e7-b7f5-b784753c484d\",\n" +
                "            \"beerName\": \"Blessed\",\n" +
                "            \"beerStyle\": \"STOUT\",\n" +
                "            \"upc\": \"4666337557578\",\n" +
                "            \"price\": 76.96,\n" +
                "            \"quantityOnHand\": 4094,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"ea88fe09-006e-45df-aa3b-8cacb3aa2e1e\",\n" +
                "            \"beerName\": \"Adjunct Trail\",\n" +
                "            \"beerStyle\": \"STOUT\",\n" +
                "            \"upc\": \"8380495518610\",\n" +
                "            \"price\": 96.13,\n" +
                "            \"quantityOnHand\": 2079,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"0f49f4ff-318b-4cc3-a1ef-10fe61ccb60d\",\n" +
                "            \"beerName\": \"Very GGGreenn\",\n" +
                "            \"beerStyle\": \"IPA\",\n" +
                "            \"upc\": \"5677465691934\",\n" +
                "            \"price\": 37.47,\n" +
                "            \"quantityOnHand\": 3061,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"26d84092-e979-421c-8efc-819ac8324088\",\n" +
                "            \"beerName\": \"Double Barrel Hunahpu's\",\n" +
                "            \"beerStyle\": \"STOUT\",\n" +
                "            \"upc\": \"5463533082885\",\n" +
                "            \"price\": 32.81,\n" +
                "            \"quantityOnHand\": 3532,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"4ce6a332-38d1-4db1-981d-961dba5de118\",\n" +
                "            \"beerName\": \"Very Hazy\",\n" +
                "            \"beerStyle\": \"IPA\",\n" +
                "            \"upc\": \"5339741428398\",\n" +
                "            \"price\": 3.33,\n" +
                "            \"quantityOnHand\": 1716,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"8cd88531-8ea8-471a-975d-a324f45ce48b\",\n" +
                "            \"beerName\": \"SR-71\",\n" +
                "            \"beerStyle\": \"STOUT\",\n" +
                "            \"upc\": \"1726923962766\",\n" +
                "            \"price\": 72.04,\n" +
                "            \"quantityOnHand\": 1025,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"f5252908-50be-4283-9fd4-dec2a2541377\",\n" +
                "            \"beerName\": \"Pliny the Younger\",\n" +
                "            \"beerStyle\": \"IPA\",\n" +
                "            \"upc\": \"8484957731774\",\n" +
                "            \"price\": 11.39,\n" +
                "            \"quantityOnHand\": 3458,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"b716d721-7c7b-4976-b8b3-729bb455c681\",\n" +
                "            \"beerName\": \"Blessed\",\n" +
                "            \"beerStyle\": \"STOUT\",\n" +
                "            \"upc\": \"6266328524787\",\n" +
                "            \"price\": 70.37,\n" +
                "            \"quantityOnHand\": 3072,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"6484fe43-29b0-4319-ac23-fa9bf20a4738\",\n" +
                "            \"beerName\": \"King Krush\",\n" +
                "            \"beerStyle\": \"IPA\",\n" +
                "            \"upc\": \"7490217802727\",\n" +
                "            \"price\": 83.3,\n" +
                "            \"quantityOnHand\": 4514,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"dca87b4d-50e4-43bf-8708-61ab3abf54a1\",\n" +
                "            \"beerName\": \"PBS Porter\",\n" +
                "            \"beerStyle\": \"PORTER\",\n" +
                "            \"upc\": \"8579613295827\",\n" +
                "            \"price\": 87.3,\n" +
                "            \"quantityOnHand\": 1580,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"4e4c1b98-f3ab-4635-abd0-29ccdddff070\",\n" +
                "            \"beerName\": \"Pinball Porter\",\n" +
                "            \"beerStyle\": \"STOUT\",\n" +
                "            \"upc\": \"2318301340601\",\n" +
                "            \"price\": 14.1,\n" +
                "            \"quantityOnHand\": 4869,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"47ad9b85-a58c-420e-ba5b-57a08897270b\",\n" +
                "            \"beerName\": \"Golden Budda\",\n" +
                "            \"beerStyle\": \"STOUT\",\n" +
                "            \"upc\": \"9401790633828\",\n" +
                "            \"price\": 88.22,\n" +
                "            \"quantityOnHand\": 543,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"96f8d229-8b5c-4ec7-95e4-e6165c9105be\",\n" +
                "            \"beerName\": \"Grand Central Red\",\n" +
                "            \"beerStyle\": \"LAGER\",\n" +
                "            \"upc\": \"4813896316225\",\n" +
                "            \"price\": 93.78,\n" +
                "            \"quantityOnHand\": 2432,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"dd862639-cb3e-473c-b91a-5b00b4092b7b\",\n" +
                "            \"beerName\": \"Pac-Man\",\n" +
                "            \"beerStyle\": \"STOUT\",\n" +
                "            \"upc\": \"3431272499891\",\n" +
                "            \"price\": 57.54,\n" +
                "            \"quantityOnHand\": 4187,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"2c531ea6-e2ea-4c23-adc6-c09c188812e0\",\n" +
                "            \"beerName\": \"Ro Sham Bo\",\n" +
                "            \"beerStyle\": \"IPA\",\n" +
                "            \"upc\": \"2380867498485\",\n" +
                "            \"price\": 3.66,\n" +
                "            \"quantityOnHand\": 891,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"89483d76-8ef9-4c36-b616-9abe9952943f\",\n" +
                "            \"beerName\": \"Summer Wheatly\",\n" +
                "            \"beerStyle\": \"WHEAT\",\n" +
                "            \"upc\": \"4323950503848\",\n" +
                "            \"price\": 63.36,\n" +
                "            \"quantityOnHand\": 4427,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"a98acd80-e177-4ea2-bf45-bfb4899871f1\",\n" +
                "            \"beerName\": \"Java Jill\",\n" +
                "            \"beerStyle\": \"LAGER\",\n" +
                "            \"upc\": \"4006016803570\",\n" +
                "            \"price\": 53.33,\n" +
                "            \"quantityOnHand\": 2144,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"8da6ea86-1768-4f55-b89e-38132b623b7b\",\n" +
                "            \"beerName\": \"Bike Trail Pale\",\n" +
                "            \"beerStyle\": \"PALE_ALE\",\n" +
                "            \"upc\": \"9883012356263\",\n" +
                "            \"price\": 61.94,\n" +
                "            \"quantityOnHand\": 598,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"7551980a-a74c-45c6-8db2-b084f19ea96f\",\n" +
                "            \"beerName\": \"N.Z.P\",\n" +
                "            \"beerStyle\": \"IPA\",\n" +
                "            \"upc\": \"0583668718888\",\n" +
                "            \"price\": 66.63,\n" +
                "            \"quantityOnHand\": 1748,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"c5dfbf10-50de-4336-b790-b46442b3362e\",\n" +
                "            \"beerName\": \"Stawberry Blond\",\n" +
                "            \"beerStyle\": \"WHEAT\",\n" +
                "            \"upc\": \"9006801347604\",\n" +
                "            \"price\": 14.25,\n" +
                "            \"quantityOnHand\": 760,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"16a36954-43a4-409c-a861-50c5de20ebd0\",\n" +
                "            \"beerName\": \"Loco\",\n" +
                "            \"beerStyle\": \"PORTER\",\n" +
                "            \"upc\": \"0610275742736\",\n" +
                "            \"price\": 70.12,\n" +
                "            \"quantityOnHand\": 2261,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        },\n" +
                "        {\n" +
                "            \"id\": \"19d1f460-729c-4709-b434-8a0e4a41a11f\",\n" +
                "            \"beerName\": \"Spocktoberfest\",\n" +
                "            \"beerStyle\": \"STOUT\",\n" +
                "            \"upc\": \"6504219363283\",\n" +
                "            \"price\": 81.94,\n" +
                "            \"quantityOnHand\": 2564,\n" +
                "            \"createdDate\": \"2021-05-17T07:13:27.18Z\",\n" +
                "            \"lastUpdatedDate\": null\n" +
                "        }\n" +
                "    ],\n" +
                "    \"number\": 0,\n" +
                "    \"size\": 25,\n" +
                "    \"totalElements\": 30,\n" +
                "    \"pageable\": {\n" +
                "        \"sort\": {\n" +
                "            \"sorted\": false,\n" +
                "            \"unsorted\": true,\n" +
                "            \"empty\": true\n" +
                "        },\n" +
                "        \"offset\": 0,\n" +
                "        \"pageNumber\": 0,\n" +
                "        \"pageSize\": 25,\n" +
                "        \"paged\": true,\n" +
                "        \"unpaged\": false\n" +
                "    },\n" +
                "    \"last\": false,\n" +
                "    \"totalPages\": 2,\n" +
                "    \"sort\": {\n" +
                "        \"sorted\": false,\n" +
                "        \"unsorted\": true,\n" +
                "        \"empty\": true\n" +
                "    },\n" +
                "    \"first\": true,\n" +
                "    \"numberOfElements\": 25,\n" +
                "    \"empty\": false\n" +
                "}";
    }
}
