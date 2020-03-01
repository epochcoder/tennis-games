package com.epochcoder.games.tennis;

import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Getter
@ToString
@RequiredArgsConstructor
public class GameDay {

    private final LocalDate matchDate;
    private final List<Match> matchList;

    public static List<GameDay> fromMatches(final TemporalUnit temporalUnit, final int courts, final List<Match> matches) {
        final AtomicInteger counter = new AtomicInteger();
        final LocalDate today = getFirstWeekend();

        final int amountOfGames = IntMath.divide(matches.size(), courts, RoundingMode.UP);
        final int partitionSize = IntMath.divide(matches.size(), amountOfGames, RoundingMode.UP);

        return Lists.partition(matches, partitionSize).stream()
                .map(matchPartition -> new GameDay(today.plus(counter.addAndGet(1), temporalUnit), matchPartition))
                .collect(Collectors.toList());
    }

    public static LocalDate getFirstWeekend() {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            date = date.plus(1, ChronoUnit.DAYS);
        }

        return date;
    }
}
