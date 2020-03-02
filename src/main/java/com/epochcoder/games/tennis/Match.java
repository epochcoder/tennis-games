package com.epochcoder.games.tennis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class Match {

    private final Team team1;
    private final Team team2;

    public boolean hasTeamFromMatch(final Match otherMatch) {
        return this.hasTeam(otherMatch.getTeam1()) || this.hasTeam(otherMatch.getTeam2());
    }

    public boolean hasTeam(final Team team) {
        return (this.getTeam1().equals(team) || this.getTeam1().isMirroredTeam(team))
                || (this.getTeam2().equals(team) || this.getTeam2().isMirroredTeam(team));
    }

    public Set<Team> getTeams() {
        return Set.of(this.team1, this.team2);
    }

    public Set<Player> getPlayers() {
        return this.getTeams().stream()
                .flatMap(t -> t.getPlayers().stream())
                .collect(Collectors.toSet());
    }

    public boolean isMirroredMatch(final Match match) {
        return this.hasTeam(match.getTeam1()) && this.hasTeam(match.getTeam2());
    }

    @Override
    public String toString() {
        return "Match(" +
                this.team1 + " vs " + this.team2 +
                ')';
    }

    /**
     * Finds game permutations where each player gets a chance to play every game
     *
     * @param teams    the possible teams
     * @param players  all players available
     * @param matchSet the current match set
     * @param i        the current iteration
     * @return a list of games playable by all players
     */
    public static List<Game> findGames(
            final Set<Team> teams, final Set<Player> players, final Set<Match> matchSet, final int i) {
        final List<Game> orderedGames = new ArrayList<>();
        final Set<Player> usedPlayers = matchSet.stream()
                .flatMap(m -> m.getPlayers().stream())
                .collect(Collectors.toSet());

        if (usedPlayers.containsAll(players)) {
            orderedGames.add(new Game(matchSet));
            return orderedGames;
        }

        final List<Team> teamsWithUnusedPlayers = Team.getTeamsWithUnusedPlayers(teams, usedPlayers);
        for (final Team team1 : teamsWithUnusedPlayers) {
            for (final Team team2 : teamsWithUnusedPlayers) {
                if (team1.equals(team2) || team1.hasPlayerFromTeam(team2)) {
                    continue;
                }

                final Match match = new Match(team1, team2);
                if (orderedGames.stream().noneMatch(game -> game.hasMatch(match))) {
                    final LinkedHashSet<Match> subMatches = new LinkedHashSet<>(matchSet);
                    subMatches.add(match);

                    final List<Game> games = findGames(teams, players, subMatches, i + 1);
                    if (!games.isEmpty()) {
                        orderedGames.addAll(games);
                    }
                }
            }
        }

        return orderedGames;
    }

    static void checkNextMatches(final List<Match> matches) {
        log.debug("Checking {} matches", matches.size());
        for (int i = 0; i < matches.size(); i++) {
            Match currentMatch = matches.get(i);
            log.debug(i + ":\t" + currentMatch);
            if (i < matches.size() - 1) {
                final Match nextMatch = matches.get(i + 1);
                if (currentMatch.hasTeamFromMatch(nextMatch)) {
                    log.debug("\t\t (next match has team)");
                    throw new IllegalStateException("Matches are not in optimal order!");
                }
            }
        }
    }
}
