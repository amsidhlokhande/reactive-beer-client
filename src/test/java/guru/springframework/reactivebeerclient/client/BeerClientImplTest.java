package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.config.WebClientConfig;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

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
        Assertions.assertTrue(beerPagedList.stream().count() > 0);

    }

    @Test
    void createBeer() {
    }

    @Test
    void updateBeer() {
    }

    @Test
    void deleteBeerById() {
    }

    @Test
    void getBeerByUPC() {
    }
}