package com.nhatro.app.viewmodel;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.nhatro.app.data.model.DashboardStats;
import com.nhatro.app.data.model.MonthlyRevenue;
import com.nhatro.app.data.repository.DashboardRepository;
import com.nhatro.app.data.repository.Resource;
import java.util.Calendar;
import java.util.List;

public class DashboardViewModel extends AndroidViewModel {
    private final DashboardRepository repository;

    public static class StatsQuery {
        public final Integer month;
        public final Integer year;

        public StatsQuery(Integer month, Integer year) {
            this.month = month;
            this.year = year;
        }
    }

    private final MutableLiveData<StatsQuery> statsQuery = new MutableLiveData<>();
    private final MutableLiveData<Integer> revenueMonthsQuery = new MutableLiveData<>();

    private final LiveData<Resource<DashboardStats>> stats;
    private final LiveData<Resource<List<MonthlyRevenue>>> monthlyRevenue;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        this.repository = new DashboardRepository(application);

        this.stats = Transformations.switchMap(statsQuery, query -> {
            if (query == null) {
                return repository.getStats();
            }
            return repository.getStats(query.month, query.year);
        });

        this.monthlyRevenue = Transformations.switchMap(revenueMonthsQuery, months -> {
            int m = (months != null && months > 0) ? months : 6;
            return repository.getMonthlyRevenue(m);
        });
    }

    public LiveData<Resource<DashboardStats>> getStats() {
        return stats;
    }

    public LiveData<Resource<List<MonthlyRevenue>>> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void loadStats(int month, int year) {
        statsQuery.setValue(new StatsQuery(month, year));
    }

    public void loadRevenue(int months) {
        revenueMonthsQuery.setValue(months);
    }

    public void refresh() {
        StatsQuery current = statsQuery.getValue();
        if (current != null) {
            statsQuery.setValue(new StatsQuery(current.month, current.year));
        } else {
            Calendar cal = Calendar.getInstance();
            statsQuery.setValue(new StatsQuery(cal.get(Calendar.MONTH) + 1, cal.get(Calendar.YEAR)));
        }

        Integer months = revenueMonthsQuery.getValue();
        revenueMonthsQuery.setValue(months != null ? months : 6);
    }
}
