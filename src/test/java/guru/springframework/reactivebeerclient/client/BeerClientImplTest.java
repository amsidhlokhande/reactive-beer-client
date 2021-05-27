package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.config.WebClientConfig;
import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import guru.springframework.reactivebeerclient.model.v2.BeerStyleEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.*;

class BeerClientImplTest {

    private BeerClient beerClient;

    @BeforeEach
    void setUp() {
        this.beerClient = new BeerClientImpl(new WebClientConfig().getWebClient());
    }

    @Test
    void getBeerById() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        BeerDto beerDto = beerPagedListMono.block().getContent().get(0);
        BeerDto beerById = this.beerClient.getBeerById(beerDto.getId()).block();
        assertNotNull(beerById);
        assertEquals(beerDto.getId(), beerById.getId());
        assertEquals(beerDto.getUpc(), beerById.getUpc());
    }

    @Test
    void listBeers() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();
        assertNotNull(beerPagedList);
        assertEquals(beerPagedList.stream().count(), 25);
    }

    @Test
    void listBeersPageSize10() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(1, 10, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();
        assertNotNull(beerPagedList);
        assertEquals(beerPagedList.stream().count(), 10);
    }

    @Test
    void listBeersPageSize5() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(2, 5, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();
        assertNotNull(beerPagedList);
        assertEquals(beerPagedList.stream().count(), 5);
    }

    @Test
    void createBeer() {
        BeerDto kingFisher = BeerDto.builder().beerName("KingFisher")
                .beerStyle(BeerStyleEnum.SAISON.name())
                .price(new BigDecimal("240.50"))
                .upc("354354879832").build();
        Mono<ResponseEntity<Void>> beerMono = beerClient.createBeer(kingFisher);
        ResponseEntity responseEntity = beerMono.block();
        assertEquals(responseEntity.getStatusCodeValue(), CREATED.value());
    }

    @Test
    void updateBeer() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        BeerDto beerDto = beerPagedListMono.block().getContent().get(0);
        BeerDto updatedBeerDto = BeerDto.builder().beerName("Radda")
                .beerStyle(beerDto.getBeerStyle())
                .upc(beerDto.getUpc())
                .price(beerDto.getPrice())
                .createdDate(beerDto.getCreatedDate())
                .lastUpdatedDate(beerDto.getLastUpdatedDate())
                .quantityOnHand(beerDto.getQuantityOnHand())
                .build();
        Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.updateBeer(beerDto.getId(), updatedBeerDto);
        assertEquals(responseEntityMono.block().getStatusCodeValue(), NO_CONTENT.value());
    }

    @Test
    void deleteBeerById() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        BeerDto beerDto = beerPagedListMono.block().getContent().get(0);
        Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.deleteBeerById(beerDto.getId());
        assertEquals(responseEntityMono.block().getStatusCodeValue(), NO_CONTENT.value());
    }

    @Test
    void deleteBeerByIdNotFound() {
        Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.deleteBeerById(UUID.randomUUID());
        Assertions.assertThrows(WebClientResponseException.class, () -> {
            ResponseEntity<Void> responseEntity = responseEntityMono.block();
            assertEquals(responseEntity.getStatusCode(), NOT_FOUND);
        });
    }

    @Test
    void deleteBeerByIdHandleException() {
        Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.deleteBeerById(UUID.randomUUID());
        ResponseEntity<Void> responseEntity = responseEntityMono.onErrorResume(throwable -> {
            if (throwable instanceof WebClientResponseException) {
                WebClientResponseException webClientResponseException = (WebClientResponseException) throwable;
                return Mono.just(ResponseEntity.status(webClientResponseException.getStatusCode()).build());
            } else {
                throw new RuntimeException(throwable);
            }
        }).block();

        assertEquals(responseEntity.getStatusCode(), NOT_FOUND);
    }

    @Test
    void getBeerByUPC() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        BeerDto beerDto = beerPagedListMono.block().getContent().get(0);
        BeerDto beerByUPC = this.beerClient.getBeerByUPC(beerDto.getUpc()).block();
        assertNotNull(beerByUPC);
        assertEquals(beerDto.getUpc(), beerByUPC.getUpc());
    }

    @Test
    void getBeerByIdWithFunctionalWay() throws InterruptedException {
        AtomicReference<String> beerName = new AtomicReference<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        beerPagedListMono.map(beerDtos -> beerDtos.getContent().get(0))
                .map(beerDto -> beerDto.getId())
                .map(uuid -> beerClient.getBeerById(uuid))
                .flatMap(beerDtoMono -> beerDtoMono).subscribe(beerDto -> {
            assertNotNull(beerDto);
            beerName.set(beerDto.getBeerName());
            countDownLatch.countDown();
        });
        countDownLatch.await();
        assertNotNull(beerName.get());
    }

}