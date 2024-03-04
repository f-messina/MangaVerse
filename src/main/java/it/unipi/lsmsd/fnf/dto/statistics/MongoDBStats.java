package it.unipi.lsmsd.fnf.dto.statistics;

import java.util.Map;

public class MongoDBStats {
    public Map<String, Double> getAvgRatingByCriteria() {
        return avgRatingByCriteria;
    }

    public void setAvgRatingByCriteria(Map<String, Double> avgRatingByCriteria) {
        this.avgRatingByCriteria = avgRatingByCriteria;
    }

    Map<String, Double> avgRatingByCriteria;



}
