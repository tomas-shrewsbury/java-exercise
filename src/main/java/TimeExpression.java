import static java.time.temporal.TemporalAdjusters.dayOfWeekInMonth;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.MonthDay;
import java.time.YearMonth;

abstract class TimeExpression {
    public LocalDate initialDate;

    public TimeExpression(LocalDate initialDate) {
        this.initialDate = initialDate;
    }

    public abstract boolean isRecurringOn (LocalDate theDate);

    public static TimeExpression on(LocalDate aDate) {
        return new NoRecurringTimeExpression(aDate);
    }

    public static TimeExpression dailyEveryFromOnwards(TimeInterval anAmountOfDays, LocalDate aDate) {
        return new RecurringTimeExpression(aDate, anAmountOfDays);
    }

    public static TimeExpression monthlyEveryOnFromOnwards(TimeInterval anAmountOfMonths, int aDayInMonth, YearMonth anYear) {
        LocalDate aDate = anYear.atDay(aDayInMonth);
        return new RecurringTimeExpression(aDate, anAmountOfMonths);
    }

    public static TimeExpression monthlyEveryOnOfFromOnwards(TimeInterval anAmountOfMonths, DayOfWeek aDayOfWeek, int aWeekInMonth, YearMonth anYear) {
        LocalDate aDate = anYear.atDay(1).with(dayOfWeekInMonth(aWeekInMonth, aDayOfWeek));
        return new DayInMonthTimeExpression(anAmountOfMonths, aDayOfWeek.getValue(), aWeekInMonth, aDate);
    }

    public static TimeExpression yearlyEveryOnFromOnwards(TimeInterval anAmountOfYears, MonthDay aMonthDay, int anYear) {
        LocalDate aDate = aMonthDay.atYear(anYear);
        return new RecurringTimeExpression(aDate, anAmountOfYears);
    }

    public boolean isDateEqual(LocalDate aDate) {
        return this.initialDate.equals(aDate);
    }

    public boolean isDateAfter(LocalDate date) {
        return date.isAfter(this.initialDate);
    }
}

class NoRecurringTimeExpression extends TimeExpression {
    public NoRecurringTimeExpression(LocalDate initialDate) {
        super(initialDate);
    }

    @Override
    public boolean isRecurringOn(LocalDate aDate) {
        return this.isDateEqual(aDate);
    }
}

class RecurringTimeExpression extends TimeExpression {
    public TimeInterval timeInterval;

    public RecurringTimeExpression(LocalDate initialDate, TimeInterval timeInterval) {
        super(initialDate);
        this.timeInterval = timeInterval;
    }

    @Override
    public boolean isRecurringOn(LocalDate aDate) {
        long diff = this.timeInterval.getTimeUnit().between(this.initialDate, aDate);

        return this.isDateEqual(aDate)
                || this.isDateAfter(aDate) && diff != 0 && (diff % this.timeInterval.getAmount() == 0);
    }
}

class DayInMonthTimeExpression extends RecurringTimeExpression {
    private int weekNumber;
    private int dayIndex;

    public DayInMonthTimeExpression(TimeInterval anAmountOfMonths, int dayIndex, int weekNumber, LocalDate initialDate) {
        super(initialDate, anAmountOfMonths);
        this.dayIndex = dayIndex;
        this.weekNumber = weekNumber;
    };

    @Override
    public boolean isRecurringOn (LocalDate aDate) {
        return this.isDateEqual(aDate) || this.isDateAfter(aDate) && dayMatches(aDate) && weekMatches(aDate);
    }
    private boolean dayMatches (LocalDate aDate) {
        return aDate.getDayOfWeek().getValue() == dayIndex;
    }
    private boolean weekMatches (LocalDate aDate) {
        if (weekNumber > 0)
            return weekFromStartMatches(aDate);
        else
            return weekFromEndMatches(aDate);
    }
    private boolean weekFromStartMatches (LocalDate aDate) {
        return this.weekInMonth(aDate.getDayOfMonth()) == weekNumber;
    }
    private boolean weekFromEndMatches (LocalDate aDate) {
        int daysFromMonthEnd = daysLeftInMonth(aDate) + 1;
        return weekInMonth(daysFromMonthEnd) == Math.abs(weekNumber);
    }
    private int weekInMonth (int dayNumber) {
        return (dayNumber / 7) + 1;
    }
    private int daysLeftInMonth (LocalDate aDate) {
        return YearMonth.of(aDate.getYear(), aDate.getMonth()).lengthOfMonth() - aDate.getDayOfMonth();
    }
}
