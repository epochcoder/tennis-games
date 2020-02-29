package com.epochcoder.games.tennis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
class Team {

    private final Player player1;
    private final Player player2;

    public String getId() {
        return this.player1.getId() + "-" + this.player2.getId();
    }

    public Set<Player> getPlayers() {
        return Set.of(this.player1, this.player2);
    }

    public boolean hasPlayer(final Player player) {
        return this.player1.equals(player) || this.player2.equals(player);
    }

    public boolean isMirroredTeam(final Team otherTeam) {
        return this.hasPlayer(otherTeam.getPlayer1()) && this.hasPlayer(otherTeam.getPlayer2());
    }

    public boolean hasPlayerFromTeam(final Team otherTeam) {
        return this.hasPlayer(otherTeam.player1) || this.hasPlayer(otherTeam.player2);
    }

    @Override
    public String toString() {
        return "Team(" + this.player1.getName() + "/" + this.player2.getName() + ")";
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

                final Team team = new Team(p1, p2);
                teams.add(team);
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
