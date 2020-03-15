package com.epochcoder.games.tennis.domain;

import com.google.common.collect.Lists;
import com.google.common.math.IntMath;
import org.immutables.value.Value;

import javax.annotation.Nullable;
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
import java.util.function.Function;
import java.util.stream.Collectors;

@Value.Immutable
public abstract class GameDay {

    @Nullable
    public abstract LocalDate getDate();

    public abstract List<Match> getMatches();

    public static List<GameDay> buildGameDays(
            final TemporalUnit unit, final LocalDate firstDate,
            final Set<Team> teams, final int courts) {
        final Set<Player> allPlayers = Player.getPlayers(teams);
        final int courtsPlayable = allPlayers.size() / 4;
        if (courts > courtsPlayable) {
            throw new IllegalArgumentException(
                    "Cannot place matches on more than " + courtsPlayable + " courts");
        }

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

        return GameDay.fromMatches(unit, firstDate, courts, matches);
    }

    public static List<GameDay> fromMatches(
            final TemporalUnit temporalUnit, final LocalDate firstDate,
            final int courts, final List<Match> matches) {
        final AtomicInteger counter = new AtomicInteger();
        final LocalDate localDate = firstDate != null ? firstDate : getFirstWeekend();

        final int amountOfGames = IntMath.divide(matches.size(), courts, RoundingMode.UP);
        final int partitionSize = IntMath.divide(matches.size(), amountOfGames, RoundingMode.UP);


        return Lists.partition(matches, partitionSize).stream()
                .map(mapGameDay(temporalUnit, counter, localDate))
                .collect(Collectors.toList());
    }

    private static Function<List<Match>, ImmutableGameDay> mapGameDay(
            final TemporalUnit temporalUnit, final AtomicInteger counter, final LocalDate today) {
        return matchPartition -> {
            ImmutableGameDay.Builder builder = ImmutableGameDay.builder();
            if (temporalUnit != null) {
                builder.date(today.plus(counter.getAndAdd(1), temporalUnit));
            }

            return builder.matches(matchPartition).build();
        };
    }

    public static LocalDate getFirstWeekend() {
        LocalDate date = LocalDate.now();
        while (date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY) {
            date = date.plus(1, ChronoUnit.DAYS);
        }

        return date;
    }
}
