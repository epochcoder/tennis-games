package com.epochcoder.games.tennis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

@Slf4j
@Getter
@ToString
@RequiredArgsConstructor
public class Game {

    private final Set<Match> matches;

    public Set<Team> getTeams() {
        return this.matches.stream()
                .flatMap(match -> match.getTeams().stream())
                .collect(Collectors.toSet());
    }

    public boolean hasMatch(final Match match) {
        return this.matches.contains(match) || this.matches.stream()
                .anyMatch(possibleMirroredMatch -> possibleMirroredMatch.isMirroredMatch(match));
    }

    public boolean didNotPlayAnyMatch(final Collection<Match> playedMatches) {
        return playedMatches.stream().noneMatch(this.matches::contains);
    }

    public boolean hasTeamFromMatches(final Collection<Match> playedMatches) {
        return hasTeamFromGame(new Game(new HashSet<>(playedMatches)));
    }

    public boolean hasTeamFromGame(final Game playerGame) {
        final Set<Team> theseTeams = this.getTeams();
        final Set<Team> thoseTeams = playerGame.getTeams();

        return theseTeams.stream().anyMatch(thoseTeams::contains);
    }

    public static List<Match> teamBasedMatchOrdering(final List<Game> games, final List<Team> teams) {
        final List<Match> matches = new ArrayList<>();
        final List<Match> roundMatches = new ArrayList<>();
        final LinkedList<Team> remaining = new LinkedList<>();
        final Set<Team> lastRoundTeams = new HashSet<>();

        Collections.shuffle(teams);

        final Map<Team, Integer> playCount = new LinkedHashMap<>();
        teams.forEach(t -> playCount.putIfAbsent(t, 0));

        // while we have games left to play
        while (!games.isEmpty()) {
            // get a set of fresh team if we ran out
            if (remaining.isEmpty()) {
                remaining.addAll(teams);
                remaining.sort(Comparator.comparing(playCount::get));

                roundMatches.clear(); // TODO: (maybe not remove that last of the added)
                System.out.println();
            }

            final Team team = remaining.poll();
            System.out.println("Finding match for team: " + team + " (" + playCount.get(team) + ") games remaining: " + games.size());
            Optional<Game> first = games.stream()
                    // get a match for the currently selected team
                    .filter(game -> game.getMatches().stream().anyMatch(match -> match.hasTeam(team)))
                    // cannot have a match that played before in this round
                    .filter(game -> game.didNotPlayAnyMatch(roundMatches))
                    // the game matches cannot have any teams that have played this round
                    .filter(game -> !game.hasTeamFromMatches(roundMatches))
                    // and the game does not have any of the teams that played last round
                    .filter(game -> game.getTeams().stream().noneMatch(lastRoundTeams::contains))
                    .findFirst();

            first
                    .ifPresent(game -> {
                        // new round, clear last rounf
                        // update teams matches
                        matches.addAll(game.getMatches());
                        // update round matches
                        roundMatches.addAll(game.getMatches());

                        System.out.println("\tFound matches:");
                        game.getMatches().forEach(c -> {
                            System.out.println("\t\t" + c);
                        });

                        // update last round teams
                        lastRoundTeams.clear();
                        lastRoundTeams.addAll(game.getTeams());

                        // update play counts
                        game.getTeams().forEach(playedTeam -> {
                            playCount.merge(playedTeam, 1, Integer::sum);
                        });

                        // remove game and remaining teams
                        games.remove(game);
                        remaining.removeIf(game.getTeams()::contains);
                        System.out.println("\tRemaining teams: " + remaining.size());

                        // sort remaining for next iteration based on play counts
                        remaining.sort(Comparator.comparing(playCount::get));
                        remaining.forEach(r -> {
                            System.out.println("\t\t" + r + ": " + playCount.get(r));
                        });

                        System.out.println("\t\t\tNext is: " + remaining.peek());
                        System.out.println("\t\t\tLast round teams are: " + lastRoundTeams);
                    });

            if (first.isEmpty()) {
                System.out.println("\tDid not find match!");
            }
        }
        System.out.println("found all matches");

        return matches;
    }

    /**
     * Place games in such a way as to avoid exact same matches played and ensuring teams don't play again after each other
     *
     * @param games       a random list of games to be played
     * @param lastPlayed  a circular array of the last games played
     * @param keepInvalid keep matches which could not mbe placed correctly?
     * @param i           the current iteration
     * @return all matches in the order they should be played
     */
    public static List<Match> placeMatches(final List<Game> games, final Queue<Game> lastPlayed, final boolean keepInvalid, final int i) {
        final Queue<Game> queue = new ArrayBlockingQueue<>(games.size(), true, games);
        final List<Match> matches = new ArrayList<>();
        final List<Game> remaining = new LinkedList<>();

        boolean foundAtLeastOne = false;
        while (!queue.isEmpty()) {
            final Game currentGame = queue.poll();

            // make sure we haven't played it
            if (currentGame.didNotPlayAnyMatch(matches)) {
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
            if (keepInvalid) {
                log.debug("Could not place {} matches", remaining.size());
                matches.addAll(remaining.stream()
                        .flatMap(g -> g.getMatches().stream())
                        .collect(Collectors.toList()));
            }
        } else if (!remaining.isEmpty()) {
            matches.addAll(placeMatches(remaining, lastPlayed, keepInvalid, i + 1));
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
                        log.debug("Cannot find optimal match for {}!", games[next]);
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
