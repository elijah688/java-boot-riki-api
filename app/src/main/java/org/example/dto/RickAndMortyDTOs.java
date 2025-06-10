package org.example.dto;

import java.util.List;

public class RickAndMortyDTOs {

    public record CharacterData(
            int id,
            String name,
            String status,
            String species,
            String type,
            String gender,
            Origin origin,
            Location location,
            List<String> episode,
            String image,
            String url,
            String created) {

        public record Origin(String name, String url) {
        }

        public record Location(String name, String url) {
        }
    }

    public record LocationData(
            int id,
            String name,
            String type,
            String dimension,
            List<String> residents,
            String url,
            String created) {
    }

    public record EpisodeData(
            int id,
            String name,
            String air_date,
            String episode,
            List<String> characters,
            String url,
            String created) {
    }
}
