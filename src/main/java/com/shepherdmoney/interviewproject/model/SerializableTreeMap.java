package com.shepherdmoney.interviewproject.model;

import java.util.TreeMap;
import java.util.SortedMap;
import java.time.LocalDate;
import java.util.Comparator;
import java.io.Serializable;

public class SerializableTreeMap implements Serializable {
    public SerializableTreeMap() {
        this.map = new TreeMap<LocalDate, BalanceHistory>(new DateComparator());
    }
    public TreeMap<LocalDate, BalanceHistory> getMap() {
        return this.map;
    }
    private TreeMap<LocalDate, BalanceHistory> map;



    private class DateComparator implements Comparator<LocalDate>, Serializable {
        @Override
        public int compare(LocalDate date1, LocalDate date2) {
            return date1.compareTo(date2);
        }
    }
}

