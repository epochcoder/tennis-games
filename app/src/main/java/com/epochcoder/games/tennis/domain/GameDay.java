package com.epochcoder.games.tennis.domain;

import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import org.immutables.value.Value;

import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class GameDay {

    public abstract LocalDate getDate();

    public abstract List<Match> getMatches();

    public static List<GameDay> buildGameDays(final TemporalUnit unit, final Set<Team> teams, final int courts) {
        final Set<Player> allPlayers = Player.getPlayers(teams);

        // get games letting each player play
        final List<Game> games = Game.findGames(
                Collections.unmodifiableSet(teams),
                Collections.unmodifiableSet(allPlayers),
                new LinkedHashSet<>(0),
                0);

        if (games.isEmpty()) {
            throw new IllegalStateException("Could not generate games");
        }

        // possible to shuffle since our sets are complete
        Collections.shuffle(games);

        final List<Match> matches = Match.teamBasedMatchOrdering(games, new ArrayList<>(teams));
        Match.checkNextMatches(matches);

        return GameDay.fromMatches(unit, courts, matches);
    }

    public static List<GameDay> fromMatches(final TemporalUnit temporalUnit, final int courts, final List<Match> matches) {
        final AtomicInteger counter = new AtomicInteger();
        final LocalDate today = getFirstWeekend();

        final int amountOfGames = IntMath.divide(matches.size(), courts, RoundingMode.UP);
        final int partitionSize = IntMath.divide(matches.size(), amountOfGames, RoundingMode.UP);

        return Lists.partition(matches, partitionSize).stream()
                .map(matchPartition -> ImmutableGameDay.builder()
                        .date(today.plus(counter.addAndGet(1), temporalUnit))
                        .matches(matchPartition)
                        .build())
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
