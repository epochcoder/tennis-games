package com.epochcoder.games.tennis.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import java.util.Set;
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
        return this.hasTeamFromGame(new Game(new HashSet<>(playedMatches)));
    }

    public boolean hasTeamFromGame(final Game playerGame) {
        final Set<Team> theseTeams = this.getTeams();
        final Set<Team> thoseTeams = playerGame.getTeams();

        return theseTeams.stream().anyMatch(thoseTeams::contains);
    }

    public static List<Match> teamBasedMatchOrdering(final List<Game> games, final List<Team> teams) {
        final List<Match> matches = new ArrayList<>();
        final List<Match> roundMatches = new ArrayList<>();
        final LinkedList<Team> remainingTeams = new LinkedList<>();
        final Set<Team> lastRoundTeams = new HashSet<>();

        Collections.shuffle(teams);

        final Map<Team, Integer> playCount = new LinkedHashMap<>();
        teams.forEach(t -> playCount.putIfAbsent(t, 0));

        while (!games.isEmpty()) {
            final Comparator<Team> byPlayCount = Comparator.comparing(playCount::get);

            // get a set of fresh leastPlayedTeam if we ran out
            if (remainingTeams.isEmpty()) {
                remainingTeams.addAll(teams);
                remainingTeams.sort(byPlayCount);

                roundMatches.clear();
            }

            final Team leastPlayedTeam = remainingTeams.poll();
            final Optional<Game> optionalGame = findGameForTeam(
                    leastPlayedTeam, lastRoundTeams, roundMatches, games);

            optionalGame.ifPresent(game -> {
                // update teams matches
                matches.addAll(game.getMatches());
                // update round matches
                roundMatches.addAll(game.getMatches());

                // update last round teams
                lastRoundTeams.clear();
                lastRoundTeams.addAll(game.getTeams());

                // update play counts for each leastPlayedTeam
                game.getTeams().forEach(playedTeam -> playCount.merge(playedTeam, 1, Integer::sum));

                // remove game from set and update remainingTeams teams
                remainingTeams.removeIf(game.getTeams()::contains);
                games.remove(game);

                // sort remainingTeams for next iteration based on play counts
                remainingTeams.sort(byPlayCount);
            });

            if (remainingTeams.isEmpty() && roundMatches.isEmpty()) {
                // infinite loop detection when nothing could be found
                log.debug("No possible match found at {} remaining games", games.size());
                break;
            }
        }

        return matches;
    }

    private static Optional<Game> findGameForTeam(
            final Team team, final Set<Team> lastRoundTeams,
            final List<Match> roundMatches, final List<Game> games) {
        return games.stream()
                // get a match for the currently selected team
                .filter(game -> game.getTeams().stream().anyMatch(team::equals))
                // cannot have a match that played before in this round
                .filter(game -> game.didNotPlayAnyMatch(roundMatches))
                // the game matches cannot have any teams that have played this round
                .filter(game -> !game.hasTeamFromMatches(roundMatches))
                // the game does not have any of the teams that played last round
                .filter(game -> game.getTeams().stream().noneMatch(lastRoundTeams::contains))
                .findFirst();
    }
}
