package com.epochcoder.games.tennis.domain;

import org.immutables.value.Value;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class Player {

    public abstract String getName();

    public abstract PlayerGroup getPlayerGroup();

    public static List<Player> toPlayers(final PlayerGroup playerGroup, final String... names) {
        return Arrays.stream(names)
                .map(name -> ImmutablePlayer.builder()
                        .playerGroup(playerGroup)
                        .name(name)
                        .build())
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toUnmodifiableList());
    }

    public static Set<Player> getPlayers(final Set<Team> teams) {
        return teams.stream()
                .flatMap(t -> t.getPlayers().stream())
                .collect(Collectors.toSet());
    }
}
