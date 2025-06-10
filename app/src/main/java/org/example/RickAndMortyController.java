
package org.example;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.example.dto.RickAndMortyDTOs.*;
import org.example.dto.RickAndMortyDTOs.CharacterData;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
public class RickAndMortyController {

    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper json = new ObjectMapper();
    private final Random random = new Random();

    private boolean hasValidUrl(String url) {
        return url != null && !url.isBlank() && !url.equalsIgnoreCase("unknown");
    }

    private boolean hasEpisodes(List<String> episodeUrls) {
        return episodeUrls != null && !episodeUrls.isEmpty();
    }

    @GetMapping("/random-character")
    public CompletableFuture<Object> getRandomCharacterData() {
        int randomId = random.nextInt(827) + 1;

        URI characterUri = URI.create("https://rickandmortyapi.com/api/character/" + randomId);

        return fetch(characterUri, CharacterData.class)
                .thenCompose(character -> {
                    // Handle origin
                    CompletableFuture<LocationData> originFuture = hasValidUrl(character.origin().url())
                            ? fetch(URI.create(character.origin().url()), LocationData.class)
                            : CompletableFuture.completedFuture(null);

                    CompletableFuture<LocationData> locationFuture = hasValidUrl(character.location().url())
                            ? fetch(URI.create(character.location().url()), LocationData.class)
                            : CompletableFuture.completedFuture(null);

                    List<String> episodeUrls = character.episode();

                    final List<CompletableFuture<EpisodeData>> episodeFutures = hasEpisodes(episodeUrls)
                            ? episodeUrls.stream()
                                    .filter(this::hasValidUrl)
                                    .map(url -> fetch(URI.create(url), EpisodeData.class))
                                    .toList()
                            : List.of();

                    return CompletableFuture
                            .allOf(originFuture, locationFuture,
                                    CompletableFuture.allOf(episodeFutures.toArray(new CompletableFuture[0])))
                            .thenApply(v -> {
                                Map<String, Object> result = new HashMap<>();

                                result.put("character", character);
                                result.put("origin", originFuture.join());
                                result.put("location", locationFuture.join());
                                result.put("episodes", episodeFutures.stream()
                                        .map(CompletableFuture::join)
                                        .toList());

                                return result;

                            });

                });
    }

    private <T> CompletableFuture<T> fetch(URI uri, Class<T> type) {
        HttpRequest req = HttpRequest.newBuilder(uri).GET().build();
        return http.sendAsync(req, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenApply(body -> {
                    try {
                        return json.readValue(body, type);
                    } catch (Exception e) {
                        throw new RuntimeException("Bad JSON: " + e.getMessage(), e);
                    }
                });
    }
}
