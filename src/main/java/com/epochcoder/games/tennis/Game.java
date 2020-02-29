package com.epochcoder.games.tennis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

@Getter
@ToString
@RequiredArgsConstructor
public class Game {

    private final Set<Match> matches;

    public boolean hasMatch(final Match match) {
        return this.matches.contains(match) || this.matches.stream()
                .anyMatch(possibleMirroredMatch -> possibleMirroredMatch.isMirroredMatch(match));
    }

    public boolean playedAnyMatch(final Collection<Match> playedMatches) {
        return playedMatches.stream().anyMatch(this.matches::contains);
    }

    public boolean hasTeamFromGame(final Game playerGame) {
        final Set<Team> theseTeams = this.matches.stream()
                .flatMap(match -> match.getTeams().stream())
                .collect(Collectors.toSet());

        final Set<Team> gameTeams = playerGame.getMatches().stream()
                .flatMap(match -> match.getTeams().stream())
                .collect(Collectors.toSet());

        return theseTeams.stream().anyMatch(gameTeams::contains);
    }

    /**
     * Place games in such a way as to avoid exact same matches played and ensuring teams don't play again after each other
     *
     * @param games      a random list of games to be played
     * @param lastPlayed a circular array of the last games played
     * @param i          the current iteration
     * @return all matches in the order they should be played
     */
    public static List<Match> placeMatches(final List<Game> games, final Queue<Game> lastPlayed, final int i) {
        final Queue<Game> queue = new ArrayBlockingQueue<>(games.size(), true, games);
        final List<Match> matches = new ArrayList<>();
        final List<Game> remaining = new LinkedList<>();

        boolean foundAtLeastOne = false;
        while (!queue.isEmpty()) {
            final Game currentGame = queue.poll();

            // make sure we haven't played it
            if (!currentGame.playedAnyMatch(matches)) {
                // make sure this game has not been played last time
                if (lastPlayed.isEmpty() || lastPlayed.stream()
                        .noneMatch(lp -> lp.hasTeamFromGame(currentGame))) {
                    // add them in order
                    matches.addAll(currentGame.getMatches());
                    lastPlayed.offer(currentGame);

                    foundAtLeastOne = true;
                    continue;
                }
            }

            remaining.add(currentGame);
        }

        if (!foundAtLeastOne) {
            System.err.println("Could not place " + remaining.size() + " matches");
            matches.addAll(remaining.stream()
                    .flatMap(g -> g.getMatches().stream())
                    .collect(Collectors.toList()));
        } else {
            if (!remaining.isEmpty()) {
                matches.addAll(placeMatches(remaining, lastPlayed, i + 1));
            }
        }

        return matches;
    }

    public static List<Game> orderGames(final List<Game> listOfGames) {
        final Game[] games = listOfGames.toArray(new Game[0]);
        for (int current = 0; current < games.length; current++) {
            final Game currentGame = games[current];

            if (current < (games.length - 1)) {
                final int next = current + 1;
                while (currentGame.hasTeamFromGame(games[next])) {
                    final int possible = findGameForNext(games, current, next);
                    if (possible == -1) {
                        System.err.println("Cannot find optimal match!");
                        break;
                    }

                    final Game temp = games[next];
                    games[next] = games[possible];
                    games[possible] = temp;
                }
            }
        }

        return List.of(games);
    }

    static int findGameForNext(final Game[] matches, int current, int next) {
        for (int possible = 1; possible < matches.length - 1; possible++) {
            if (possible != current && possible != next) {
                Game currentGame = matches[current];
                Game possibleGame = matches[possible];
                Game nextGame = matches[next];

                // is a possibility for switch
                if (!possibleGame.hasTeamFromGame(currentGame) && !possibleGame.hasTeamFromGame(nextGame)) {
                    if (!matches[possible - 1].hasTeamFromGame(nextGame) && !matches[possible + 1].hasTeamFromGame(nextGame)) {
                        return possible;
                    }
                }
            }
        }

        return -1;
    }
}
