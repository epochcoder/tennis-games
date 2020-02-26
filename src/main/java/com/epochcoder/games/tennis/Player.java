package com.epochcoder.games.tennis;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
class Player {

    private final String name;
    private final Gender gender;

    public static List<Player> toPlayers(final Gender gender, final String... names) {
        return Arrays.stream(names)
                .map(name -> new Player(name, gender))
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toUnmodifiableList());
    }
}
