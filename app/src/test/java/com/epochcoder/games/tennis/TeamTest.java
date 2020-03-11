package com.epochcoder.games.tennis;

import com.epochcoder.games.tennis.domain.ImmutablePlayer;
import com.epochcoder.games.tennis.domain.ImmutableTeam;
import com.epochcoder.games.tennis.domain.Player;
import com.epochcoder.games.tennis.domain.PlayerGroup;
import com.epochcoder.games.tennis.domain.Team;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TeamTest {

    @Test
    void equalsTest() {
        final Player player1 = this.createPlayer(PlayerGroup.A);
        final Player player2 = this.createPlayer(PlayerGroup.B);

        final Team team = createTeam(player1, player2);
        final Team same = createTeam(player1, player2);
        final Team swapped = createTeam(player2, player1);
        assertEquals(team, same);
        assertNotEquals(team, swapped);
    }

    @Test
    void hasPlayer() {
        final Player player1 = this.createPlayer(PlayerGroup.A);
        final Player player2 = this.createPlayer(PlayerGroup.B);

        final Team team = createTeam(player1, player2);
        assertTrue(team.hasPlayer(player1));
        assertTrue(team.hasPlayer(player2));
        assertFalse(team.hasPlayer(this.createPlayer(PlayerGroup.B)));
    }

    @Test
    void isMirroredTeam() {
        final Player player1 = this.createPlayer(PlayerGroup.A);
        final Player player2 = this.createPlayer(PlayerGroup.B);
        final Player player3 = this.createPlayer(PlayerGroup.B);

        final Team team = createTeam(player1, player2);
        final Team swapped = createTeam(player2, player1);
        final Team sameButNewPlayer = createTeam(player2, player3);

        assertTrue(team.isMirroredTeam(swapped));
        assertTrue(swapped.isMirroredTeam(team));
        assertFalse(swapped.isMirroredTeam(sameButNewPlayer));
        assertFalse(sameButNewPlayer.isMirroredTeam(swapped));
    }

    @Test
    void hasPlayerFromTeam() {
        final Player player1 = this.createPlayer(PlayerGroup.A);
        final Player player2 = this.createPlayer(PlayerGroup.B);
        final Player player3 = this.createPlayer(PlayerGroup.B);
        final Player player4 = this.createPlayer(PlayerGroup.A);

        final Team team = createTeam(player1, player2);
        final Team swapped = createTeam(player2, player1);
        final Team sameButNewPlayer = createTeam(player2, player3);
        final Team noMatch = createTeam(player4, player3);

        assertTrue(team.hasPlayerFromTeam(swapped));
        assertTrue(swapped.hasPlayerFromTeam(team));
        assertTrue(team.hasPlayerFromTeam(sameButNewPlayer));
        assertTrue(sameButNewPlayer.hasPlayerFromTeam(team));

        assertTrue(noMatch.hasPlayerFromTeam(sameButNewPlayer));
        assertFalse(noMatch.hasPlayerFromTeam(team));
        assertFalse(noMatch.hasPlayerFromTeam(swapped));
    }

    private Player createPlayer(final PlayerGroup playerGroup) {
        return ImmutablePlayer.builder()
                .name(UUID.randomUUID().toString())
                .playerGroup(playerGroup)
                .build();
    }

    private Team createTeam(final Player p1, final Player p2) {
        return ImmutableTeam.builder()
                .playerA(p1)
                .playerB(p2)
                .build();
    }
}
