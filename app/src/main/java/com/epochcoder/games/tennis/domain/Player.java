package com.epochcoder.games.tennis.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class Player {

    private final String name;
    private final Gender gender;

    public static List<Player> toPlayers(final Gender gender, final String... names) {
        return Arrays.stream(names)
                .map(name -> new Player(name, gender))
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toUnmodifiableList());
    }

    public static Set<Player> getPlayers(final Set<Team> teams) {
        return teams.stream()
                .flatMap(t -> t.getPlayers().stream())
                .collect(Collectors.toSet());
    }
}
