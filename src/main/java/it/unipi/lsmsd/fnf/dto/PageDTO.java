package it.unipi.lsmsd.fnf.dto;

import it.unipi.lsmsd.fnf.dto.mediaContent.MediaContentDTO;

import java.util.ArrayList;
import java.util.List;

public class PageDTO <T extends MediaContentDTO>{
    private List<T> entries;
    private int totalCount;

    public PageDTO(List<T> entries, int totalCount) {
        this.entries = entries;
        this.totalCount = totalCount;
    }

    public List<T> getEntries() {
        return entries;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void add (T entry) {
        entries.add(entry);
    }

    public void remove (T entry) {
        entries.remove(entry);
    }

    public void setEntries(List<T> entries) {
        this.entries = entries;
    }



    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    @Override
    public String toString() {
        return "PageDTO{" +
                "entries=" + entries +
                ", totalCount=" + totalCount +
                '}';
    }
}
