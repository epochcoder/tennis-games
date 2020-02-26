package com.epochcoder.games.tennis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
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

    public boolean hasPlayer(final Player player) {
        return getPlayers().contains(player);
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
}
