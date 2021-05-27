package guru.springframework.reactivebeerclient.client;

import guru.springframework.reactivebeerclient.model.BeerDto;
import guru.springframework.reactivebeerclient.model.BeerPagedList;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.UUID;

import static guru.springframework.reactivebeerclient.config.WebClientConfigProperties.*;

/**
 * Created by jt on 3/13/21.
 */
@Service
@AllArgsConstructor
public class BeerClientImpl implements BeerClient {


    private final WebClient webClient;

    @Override
    public Mono<BeerDto> getBeerById(UUID id) {
        return this.webClient.get().uri(uriBuilder -> uriBuilder.path(BEER_V2_URL_BY_BEERID)
                .queryParamIfPresent("id", Optional.of(id))
                .build(id)).retrieve().bodyToMono(BeerDto.class);
    }

    @Override
    public Mono<BeerPagedList> listBeers(Integer pageNumber, Integer pageSize, String beerName, String beerStyle, Boolean showInventoryOnhand) {
        return this.webClient.get().uri(uriBuilder -> uriBuilder.path(BEER_V2_URL)
                .queryParamIfPresent("pageNumber", Optional.ofNullable(pageNumber))
                .queryParamIfPresent("pageSize", Optional.ofNullable(pageSize))
                .queryParamIfPresent("beerName", Optional.ofNullable(beerName))
                .queryParamIfPresent("beerStyle", Optional.ofNullable(beerStyle))
                .queryParamIfPresent("showInventoryOnhand", Optional.ofNullable(showInventoryOnhand))
                .build()).retrieve().bodyToMono(BeerPagedList.class);
    }

    @Override
    public Mono<ResponseEntity<Void>> createBeer(BeerDto beerDto) {
        return webClient.post().uri(BEER_V2_URL).body(BodyInserters.fromValue(beerDto)).retrieve().toBodilessEntity();
    }

    @Override
    public Mono<ResponseEntity<Void>> updateBeer(UUID uuid, BeerDto beerDto) {
        return webClient.put().uri(uriBuilder -> uriBuilder.path(BEER_V2_URL_BY_BEERID).build(uuid))
                .body(BodyInserters.fromValue(beerDto)).retrieve().toBodilessEntity();
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteBeerById(UUID id) {
        return webClient.delete().uri(uriBuilder -> uriBuilder.path(BEER_V2_URL_BY_BEERID).build(id))
                .retrieve().toBodilessEntity();
    }

    @Override
    public Mono<BeerDto> getBeerByUPC(String upc) {
        return webClient.get().uri(uriBuilder -> uriBuilder.path(BEER_V2_URL_BY_UPC).build(upc)).retrieve().bodyToMono(BeerDto.class);
    }
}
