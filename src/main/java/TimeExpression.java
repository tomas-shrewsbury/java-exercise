import static java.time.temporal.TemporalAdjusters.dayOfWeekInMonth;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;
import java.util.function.Function;

class TimeExpression {
    public Function<LocalDate, Boolean> isRecurring;

    public TimeExpression(Function<LocalDate, Boolean> predicate) {
        this.isRecurring = predicate;
    }

    public boolean isRecurringOn(LocalDate aDate) {
        return isRecurring.apply(aDate);
    }

    public static TimeExpression on(LocalDate initialDate) {
        return new TimeExpression(NoRecurringTimeExpressions.isRecurringOn(initialDate));
    }

    public static TimeExpression dailyEveryFromOnwards(TimeInterval anAmountOfDays, LocalDate initialDate) {
        return new TimeExpression(RecurringTimeExpression.isRecurringOn(initialDate, anAmountOfDays));
    }

    public static TimeExpression monthlyEveryOnFromOnwards(TimeInterval anAmountOfMonths, int aDayInMonth, YearMonth anYear) {
        LocalDate initialDate = anYear.atDay(aDayInMonth);
        return new TimeExpression(RecurringTimeExpression.isRecurringOn(initialDate, anAmountOfMonths));
    }

    public static TimeExpression monthlyEveryOnOfFromOnwards(TimeInterval anAmountOfMonths, DayOfWeek aDayOfWeek, int aWeekInMonth, YearMonth anYear) {
        LocalDate initialDate = anYear.atDay(1).with(dayOfWeekInMonth(aWeekInMonth, aDayOfWeek));
        return new TimeExpression(DayInMonthTimeExpression.isRecurringOn(aDayOfWeek.getValue(), aWeekInMonth, initialDate));
    }

    public static TimeExpression yearlyEveryOnFromOnwards(TimeInterval anAmountOfYears, MonthDay aMonthDay, int anYear) {
        LocalDate initialDate = aMonthDay.atYear(anYear);
        return new TimeExpression(RecurringTimeExpression.isRecurringOn(initialDate, anAmountOfYears));
    }
}

class NoRecurringTimeExpressions {
    public static Function<LocalDate, Boolean> isRecurringOn(LocalDate initialDate) {
        return (LocalDate date) -> date.equals(initialDate);
    }
}

class RecurringTimeExpression {
    private static boolean matchesInterval(LocalDate initialDate, LocalDate date, TimeInterval timeInterval) {
        long diff = timeInterval.getTimeUnit().between(initialDate, date);
        return diff != 0 && (diff % timeInterval.getAmount() == 0);
    }

    public static Function<LocalDate, Boolean> isRecurringOn(LocalDate initialDate, TimeInterval timeInterval) {
        return (LocalDate date) -> date.equals(initialDate)
                    || date.isAfter(initialDate) && matchesInterval(initialDate, date, timeInterval);
    }
}

class DayInMonthTimeExpression {
    private static boolean dayMatches(LocalDate aDate, int dayIndex) {
        return aDate.getDayOfWeek().getValue() == dayIndex;
    }
    private static boolean weekMatches(LocalDate aDate, int weekNumber) {
        if (weekNumber > 0)
            return weekFromStartMatches(aDate, weekNumber);
        else
            return weekFromEndMatches(aDate, weekNumber);
    }
    private static boolean weekFromStartMatches (LocalDate aDate, int weekNumber) {
        return weekInMonth(aDate.getDayOfMonth()) == weekNumber;
    }
    private static boolean weekFromEndMatches (LocalDate aDate, int weekNumber) {
        int daysFromMonthEnd = daysLeftInMonth(aDate) + 1;
        return weekInMonth(daysFromMonthEnd) == Math.abs(weekNumber);
    }
    private static int weekInMonth (int dayNumber) {
        return (dayNumber / 7) + 1;
    }
    private static int daysLeftInMonth (LocalDate aDate) {
        return YearMonth.of(aDate.getYear(), aDate.getMonth()).lengthOfMonth() - aDate.getDayOfMonth();
    }

    public static Function<LocalDate, Boolean> isRecurringOn(int dayIndex, int weekNumber, LocalDate initialDate) {
        return (LocalDate date) -> date.equals(initialDate)
                    || date.isAfter(initialDate) && dayMatches(date, dayIndex) && weekMatches(date, weekNumber);
    }
}
