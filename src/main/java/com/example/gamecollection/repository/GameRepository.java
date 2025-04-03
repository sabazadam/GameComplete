package com.example.gamecollection.repository;

import com.example.gamecollection.model.Game;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository class for managing game data and handling JSON import/export.
 */
public class GameRepository {
    private List<Game> games;
    private final ObjectMapper objectMapper;

    public GameRepository() {
        this.games = new ArrayList<>();
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * Imports games from a JSON file.
     *
     * @param filePath Path to the JSON file
     * @return List of imported games
     * @throws IOException If file cannot be read or parsed
     */
    public List<Game> importGamesFromJson(String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            games = objectMapper.readValue(file, new TypeReference<List<Game>>() {});
            return new ArrayList<>(games);
        }
        return new ArrayList<>();
    }

    /**
     * Exports the current list of games to a JSON file.
     *
     * @param filePath Path where to save the JSON file
     * @throws IOException If file cannot be written
     */
    public void exportGamesToJson(String filePath) throws IOException {
        objectMapper.writeValue(new File(filePath), games);
    }

    /**
     * Gets all games in the repository.
     *
     * @return List of all games
     */
    public List<Game> getAllGames() {
        return new ArrayList<>(games);
    }

    /**
     * Adds a new game to the repository.
     *
     * @param game Game to add
     * @return true if game was added, false if it already exists
     */
    public boolean addGame(Game game) {
        if (game != null && !games.contains(game)) {
            return games.add(game);
        }
        return false;
    }

    /**
     * Updates an existing game in the repository.
     *
     * @param game Updated game
     * @return true if game was updated, false if it doesn't exist
     */
    public boolean updateGame(Game game) {
        if (game != null) {
            for (int i = 0; i < games.size(); i++) {
                if (games.get(i).equals(game)) {
                    games.set(i, game);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Deletes a game from the repository.
     *
     * @param game Game to delete
     * @return true if game was deleted, false if it doesn't exist
     */
    public boolean deleteGame(Game game) {
        return games.remove(game);
    }

    /**
     * Searches for games matching the query across all fields.
     *
     * @param query Search query
     * @return List of games matching the query
     */
    public List<Game> searchGames(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new ArrayList<>(games);
        }

        String normalizedQuery = query.toLowerCase().trim();
        return games.stream()
                .filter(game -> containsQuery(game, normalizedQuery))
                .collect(Collectors.toList());
    }

    /**
     * Filters games by tags.
     *
     * @param tags List of tags to filter by
     * @return List of games containing all specified tags
     */
    public List<Game> filterGamesByTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return new ArrayList<>(games);
        }

        List<String> normalizedTags = tags.stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        return games.stream()
                .filter(game -> containsAllTags(game, normalizedTags))
                .collect(Collectors.toList());
    }

    /**
     * Checks if a game contains the query in any of its fields.
     */
    private boolean containsQuery(Game game, String query) {
        if (containsInField(game.getTitle(), query)) return true;
        if (containsInField(game.getDeveloper(), query)) return true;
        if (containsInField(game.getPublisher(), query)) return true;
        if (containsInField(game.getFormat(), query)) return true;
        if (containsInField(game.getLanguage(), query)) return true;
        if (containsInField(game.getSteamId(), query)) return true;
        
        if (game.getReleaseYear() != null && 
            String.valueOf(game.getReleaseYear()).contains(query)) return true;
        if (game.getPlaytime() != null && 
            String.valueOf(game.getPlaytime()).contains(query)) return true;
        if (game.getRating() != null && 
            String.valueOf(game.getRating()).contains(query)) return true;
        
        if (containsInList(game.getGenres(), query)) return true;
        if (containsInList(game.getPlatforms(), query)) return true;
        if (containsInList(game.getTranslators(), query)) return true;
        if (containsInList(game.getTags(), query)) return true;
        
        return false;
    }

    private boolean containsInField(String field, String query) {
        return field != null && field.toLowerCase().contains(query);
    }

    private boolean containsInList(List<String> list, String query) {
        if (list == null) return false;
        return list.stream()
                .anyMatch(item -> item != null && item.toLowerCase().contains(query));
    }

    /**
     * Checks if a game contains all specified tags.
     */
    private boolean containsAllTags(Game game, List<String> tags) {
        if (game.getTags() == null) return false;
        
        List<String> gameTags = game.getTags().stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());
                
        return gameTags.containsAll(tags);
    }
}