package com.epochcoder.games.tennis;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
class Player {

    private String name;
    private Gender gender;

    public static List<Player> toPlayers(final Gender gender, final String... names) {
        return Arrays.stream(names)
                .map(name -> new Player(name, gender))
                .sorted(Comparator.comparing(Player::getName))
                .collect(Collectors.toList());
    }
}
