package com.epochcoder.games.tennis.domain;

import org.immutables.value.Value.Immutable;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Immutable
public abstract class Team {

    public abstract Player getPlayerA();

    public abstract Player getPlayerB();

    public Set<Player> getPlayers() {
        return Set.of(this.getPlayerA(), this.getPlayerB());
    }

    public boolean hasPlayer(final Player player) {
        return this.getPlayers().contains(player);
    }

    public boolean isMirroredTeam(final Team otherTeam) {
        return this.hasPlayer(otherTeam.getPlayerA()) && this.hasPlayer(otherTeam.getPlayerB());
    }

    public boolean hasPlayerFromTeam(final Team otherTeam) {
        return this.hasPlayer(otherTeam.getPlayerA()) || this.hasPlayer(otherTeam.getPlayerB());
    }

    @Override
    public String toString() {
        return "Team(" + this.getPlayerA().getName() + "/" + this.getPlayerB().getName() + ")";
    }

    public static Set<Team> makeTeams(final List<Player> set1, final List<Player> set2) {
        if (set1 == null || set2 == null
                || (set1.size() + set2.size()) % 4 != 0) {
            throw new IllegalArgumentException("Null input or players not in groups of 4");
        }

        final Set<Team> teams = new LinkedHashSet<>();
        for (final Player p1 : set1) {
            for (final Player p2 : set2) {
                if (p1.equals(p2)) {
                    throw new IllegalArgumentException("Same player in both sets");
                }

                teams.add(ImmutableTeam.builder().playerA(p1).playerB(p2).build());
            }
        }

        return teams;
    }

    public static List<Team> getTeamsWithUnusedPlayers(final Set<Team> teams, final Set<Player> usedPlayers) {
        return teams.stream()
                .filter(t -> usedPlayers.stream().noneMatch(t.getPlayers()::contains))
                .collect(Collectors.toList());
    }

}
