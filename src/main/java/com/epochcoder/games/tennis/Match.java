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

    public String getId() {
        return this.team1.getId() + "/" + this.getTeam2().getId();
    }

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
        return getTeams().stream()
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

    public static List<AllPlayerGame> findGames(
            final Set<Team> teams, final Set<Player> players, final Set<Match> matchSet, final int i) {
        final List<AllPlayerGame> orderedGames = new ArrayList<>();
        final Set<Player> usedPlayers = matchSet.stream()
                .flatMap(m -> m.getPlayers().stream())
                .collect(Collectors.toSet());

        if (usedPlayers.containsAll(players)) {
            orderedGames.add(new AllPlayerGame(matchSet));
            return orderedGames;
        }

        final List<Team> teamsWithUnusedPlayers = Team.getTeamsWithUnusedPlayers(teams, usedPlayers);
        for (final Team team1 : teamsWithUnusedPlayers) {
            for (final Team team2 : teamsWithUnusedPlayers) {
                if (team1.equals(team2) || team1.hasPlayerFromTeam(team2)) {
                    continue;
                }

                final Match match = new Match(team1, team2);
                if (orderedGames.stream().noneMatch(allPlayerGame -> allPlayerGame.hasMatch(match))) {
                    final LinkedHashSet<Match> subMatches = new LinkedHashSet<>(matchSet);
                    subMatches.add(match);

                    final List<AllPlayerGame> games = findGames(teams, players, subMatches, i + 1);
                    if (!games.isEmpty()) {
                        orderedGames.addAll(games);
                    }
                }
            }
        }

        return orderedGames;
    }

}
