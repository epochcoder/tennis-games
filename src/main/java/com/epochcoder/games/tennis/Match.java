package com.epochcoder.games.tennis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    public static Set<Match> findMatchSets(
            final Set<Team> teams, final Set<Player> players, final Set<Match> matchSet, final int i) {
        final LinkedHashSet<Match> orderedMatchSets = new LinkedHashSet<>();
        final Set<Player> usedPlayers = matchSet.stream()
                .flatMap(m -> m.getPlayers().stream())
                .collect(Collectors.toSet());

        if (usedPlayers.containsAll(players)) {
            orderedMatchSets.addAll(matchSet);
            return orderedMatchSets;
        }

        final List<Team> teamsWithUnusedPlayers = Team.getTeamsWithUnusedPlayers(teams, usedPlayers);
        for (final Team team1 : teamsWithUnusedPlayers) {
            for (final Team team2 : teamsWithUnusedPlayers) {
                if (team1.equals(team2) || team1.hasPlayerFromTeam(team2)) {
                    continue;
                }

                final Match match = new Match(team1, team2);
                final boolean hasMirroredMatch = orderedMatchSets.contains(match) || orderedMatchSets.stream()
                        .anyMatch(possibleMirroredMatch -> possibleMirroredMatch.isMirroredMatch(match));

                if (!hasMirroredMatch) {
                    final LinkedHashSet<Match> subMatches = new LinkedHashSet<>(matchSet);
                    subMatches.add(match);

                    // find rest of the matches remaining for played matches
                    final Set<Match> subMatchSets = findMatchSets(teams, players, subMatches, i + 1);
                    if (!subMatchSets.isEmpty()) {
                        orderedMatchSets.addAll(subMatchSets);
                    }
                }
            }
        }


        return orderedMatchSets;
    }

}
