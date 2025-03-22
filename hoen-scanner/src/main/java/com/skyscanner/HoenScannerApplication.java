package com.skyscanner;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// Main Application Class
public class HoenScannerApplication extends Application<HoenScannerConfiguration> {

    @Override
    public void run(final HoenScannerConfiguration configuration, final Environment environment) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        // Load data from JSON files
        List<SearchResult> carResults = Arrays.asList(
                mapper.readValue(
                        getClass().getClassLoader().getResource("rental_cars.json"),
                        SearchResult[].class
                )
        );

        List<SearchResult> hotelResults = Arrays.asList(
                mapper.readValue(
                        getClass().getClassLoader().getResource("hotels.json"),
                        SearchResult[].class
                )
        );

        List<SearchResult> searchResults = new ArrayList<>();
        searchResults.addAll(carResults);
        searchResults.addAll(hotelResults);

        final SearchResource searchResource = new SearchResource(searchResults);
        environment.jersey().register(searchResource);
    }

    @Path("/search")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public static class SearchResource {
        private final List<SearchResult> searchResults;

        public SearchResource(List<SearchResult> searchResults) {
            this.searchResults = searchResults;
        }

        @POST
        public List<SearchResult> search(@NotNull @Valid Search search) {
            return searchResults.stream()
                    .filter(result -> result.getCity().equalsIgnoreCase(search.getCity()))
                    .collect(Collectors.toList());
        }
    }

    public static class Search {
        @JsonProperty
        private String city;

        public Search() {}

        public Search(String city) {
            this.city = city;
        }

        public String getCity() {
            return city;
        }
    }

    public static class SearchResult {
        @JsonProperty
        private String city;
        @JsonProperty
        private String kind;
        @JsonProperty
        private String title;

        public SearchResult() {}

        public SearchResult(String city, String title, String kind) {
            this.city = city;
            this.title = title;
            this.kind = kind;
        }

        public String getCity() { return city; }
        public String getKind() { return kind; }
        public String getTitle() { return title; }
    }
}
