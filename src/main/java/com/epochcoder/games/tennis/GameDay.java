package com.epochcoder.games.tennis;

import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@ToString
@RequiredArgsConstructor
public class GameDay {

    private final List<Match> matchList;

    public static List<GameDay> toGameDays(final int courts, final List<Match> matches) {
        final int amountOfGames = IntMath.divide(matches.size(), courts, RoundingMode.UP);
        final int partitionSize = IntMath.divide(matches.size(), amountOfGames, RoundingMode.UP);
        return Lists.partition(matches, partitionSize).stream()
                .map(GameDay::new).collect(Collectors.toList());
    }
}
