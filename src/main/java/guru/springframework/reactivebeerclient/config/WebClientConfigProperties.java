package guru.springframework.reactivebeerclient.config;

public class WebClientConfigProperties {

    public static final String BASE_URL = "http://api.springframework.guru";

    public static final String BEER_V2_URL = "/api/v1/beer";
    public static final String BEER_V2_URL_BY_BEERID = "/api/v1/beer/{beerId}";
    public static final String BEER_V2_URL_BY_UPC = "/api/v1/beerUpc/{upc}";
}
