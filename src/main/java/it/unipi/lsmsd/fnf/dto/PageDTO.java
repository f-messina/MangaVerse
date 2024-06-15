package it.unipi.lsmsd.fnf.dto;

import it.unipi.lsmsd.fnf.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Data Transfer Object for the Page class.
 * It is used to store a list of objects and the total count of objects in the list.
 * @param <T>       The type of the objects in the list
 */
public class PageDTO<T> {
    private List<T> entries;
    private int totalCount;
    private int totalPages;

    public PageDTO() {
        entries = new ArrayList<>();
    }

    public PageDTO(List<T> entries, int totalCount, Integer totalPages) {
        this.entries = entries;
        this.totalCount = totalCount;
        this.totalPages = totalPages != null ? totalPages : (int) Math.ceil((double) totalCount / Constants.PAGE_SIZE);
    }

    public List<T> getEntries() {
        return entries;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void add(T entry) {
        entries.add(entry);
    }

    public void remove(T entry) {
        entries.remove(entry);
    }

    public void setEntries(List<T> entries) {
        this.entries = entries;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public void setTotalPages(int totalCount) {
        this.totalPages = (int) Math.ceil((double) totalCount / entries.size());
    }

    @Override
    public String toString() {
        return "PageDTO{" +
                "entries=" + entries +
                ", totalCount=" + totalCount +
                ", totalPages=" + totalPages +
                '}';
    }
}
