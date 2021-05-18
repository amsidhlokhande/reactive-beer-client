package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.config.WebClientConfig;
import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import guru.springframework.reactivebeerclient.model.v2.BeerStyleEnum;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

class BeerClientImplTest {

    private BeerClient beerClient;

    @BeforeEach
    void setUp() {
        this.beerClient = new BeerClientImpl(new WebClientConfig().getWebClient());
    }

    @Test
    void getBeerById() {
    }

    @Test
    void listBeers() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();
        Assertions.assertNotNull(beerPagedList);
        Assertions.assertTrue(beerPagedList.stream().count() == 25);

    }

    @Test
    void listBeersPageSize10() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(1, 10, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();
        Assertions.assertNotNull(beerPagedList);
        Assertions.assertTrue(beerPagedList.stream().count() == 10);

    }

    @Test
    void listBeersPageSize5() {
        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(2, 5, null, null, null);
        BeerPagedList beerPagedList = beerPagedListMono.block();
        Assertions.assertNotNull(beerPagedList);
        Assertions.assertTrue(beerPagedList.stream().count() == 5);

    }

    @Test
    void createBeer() {
        BeerDto kingFisher = BeerDto.builder().beerName("KingFisher")
                .beerStyle(BeerStyleEnum.SAISON.name())
                .price(new BigDecimal("240.50"))
                .upc("354354879832").build();
        Mono<ResponseEntity<Void>> beerMono = beerClient.createBeer(kingFisher);
        ResponseEntity responseEntity = beerMono.block();
        Assertions.assertTrue(responseEntity.getStatusCodeValue() == HttpStatus.CREATED.value());

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
        Assertions.assertTrue(responseEntityMono.block().getStatusCodeValue() == HttpStatus.NO_CONTENT.value());
    }

    @Test
    void deleteBeerById() {

        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        BeerDto beerDto = beerPagedListMono.block().getContent().get(0);
        Mono<ResponseEntity<Void>> responseEntityMono = this.beerClient.deleteBeerById(beerDto.getId());
        Assertions.assertTrue(responseEntityMono.block().getStatusCodeValue() == HttpStatus.NO_CONTENT.value());
    }

    @Test
    void getBeerByUPC() {

        Mono<BeerPagedList> beerPagedListMono = this.beerClient.listBeers(null, null, null, null, null);
        BeerDto beerDto = beerPagedListMono.block().getContent().get(0);
        BeerDto beerByUPC = this.beerClient.getBeerByUPC(beerDto.getUpc()).block();
        Assertions.assertNotNull(beerByUPC);
        Assertions.assertTrue(beerDto.getUpc().equals(beerByUPC.getUpc()));

    }

}