package edu.upenn.cis.cis455.util;

import java.util.*;

/**
 * AcceptTypeParse: Utilities for type and subtype matching.
 *
 * Functions for
 * Accept Type based Matching.
 */

public class AcceptTypeParse {

    private static final String EMPTY_TYPE = "";
    private static final String WILD_CARD = "*";


    private static AcceptType parseString(String inputAcceptType) {

        String[] tokens = inputAcceptType.split(";");

        String type = null;
        String subType = null;

        String typeDenoter = tokens[0];

        if(typeDenoter.equals("*")) {
            typeDenoter = "*/*";
        }

        String[] types = typeDenoter.split("/");

        if(types.length >= 2) {
            type = types[0];
            subType = types[1];
        } else {
            type = types[0];
            subType = WILD_CARD;
        }

        AcceptType acceptType = new AcceptType();
        acceptType.setType(type);
        acceptType.setSubType(subType);

        for(int index = 1; index < tokens.length; index++) {

            String param = tokens[index];
            String[] equalityParams = param.split("=");
            if(equalityParams.length == 2) {
                String lhs = equalityParams[0];
                String rhs = equalityParams[1];
                acceptType.getParams().put(lhs.trim(), rhs.trim());
                if(lhs.equals("q")) {
                    try{
                        acceptType.setScore(Float.parseFloat(rhs.trim()));
                    } catch (Exception e) {
                        /* ignore it*/
                    }
                }
            }
        }

        return acceptType;
    }

    public static AcceptTypeScoringEntity getAcceptTypeScoringEntity(String type, List<AcceptType> parsedTypes) {

        int currentBestMatchingScore = -1;
        float currentBestQualityScore = 0.0f;

        AcceptType currentType = parseString(type);

        for (AcceptType acceptType: parsedTypes) {

            if(!WILD_CARD.equals(acceptType.getType()) && !WILD_CARD.equals(currentType.getType()) &&
                    !currentType.getType().equals(acceptType.getType())) {
                continue;
            }
            if(!WILD_CARD.equals(acceptType.getSubType()) && !WILD_CARD.equals(currentType.getSubType()) &&
                    !currentType.getSubType().equals(acceptType.getSubType())) {
                continue;
            }

            /*
            Matching the correct types now.
             */

            int paramHit = 0;

            for(Map.Entry entry: currentType.getParams().entrySet()) {
                if(acceptType.getParams().containsKey(entry.getKey()) &&
                        acceptType.getParams().get(entry.getKey()).equals(entry.getValue())) {
                    paramHit += 1;
                }

            }

            int matchingScore = 0;
            if(acceptType.getType().equals(currentType.getType())) {
                matchingScore += 100;
            }
            if(acceptType.getSubType().equals(currentType.getSubType())) {
                matchingScore += 10;
            }
            matchingScore += paramHit;

            if(currentBestMatchingScore < matchingScore) {
                currentBestMatchingScore = matchingScore;
                currentBestQualityScore = acceptType.getScore();
            }

        }

        return new AcceptTypeScoringEntity(currentBestMatchingScore, currentBestQualityScore);

    }

    public static String findBestMatch(String type, List<String> allowedTypes) {

        /*
            Type can contain multiple accept types;
         */

        List<AcceptType> parsedTypes = new ArrayList<>();

        if(allowedTypes.size() == 0) {
            return EMPTY_TYPE;
        }

        String[] validTypes = type.split(",");

        for(String validType: validTypes) {
            AcceptType parsedType = parseString(validType.trim());
            parsedTypes.add(parsedType);
        }

        List<AcceptTypeScoringEntity> scoreObjects = new ArrayList<>();

        for(String allowedType: allowedTypes) {
            AcceptTypeScoringEntity acceptTypeScoringEntity = getAcceptTypeScoringEntity(allowedType, parsedTypes);
            acceptTypeScoringEntity.setAcceptType(allowedType);
            scoreObjects.add(acceptTypeScoringEntity);
        }

        Collections.sort(scoreObjects);
        Collections.reverse(scoreObjects);

        AcceptTypeScoringEntity bestMatch = scoreObjects.get(0);
        if(bestMatch.getQualityScore() == 0.0d) {
            return EMPTY_TYPE;
        }
        return bestMatch.getAcceptType();

    }



    public static class AcceptType {

        private String type;
        private String subType;
        private Map<String, String> params;
        private Float score = 1.0f;

        public AcceptType() {
            params = new HashMap<>();
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSubType() {
            return subType;
        }

        public void setSubType(String subType) {
            this.subType = subType;
        }

        public Map<String, String> getParams() {
            return params;
        }

        public void setParams(Map<String, String> params) {
            this.params = params;
        }

        public Float getScore() {
            return score;
        }

        public void setScore(Float score) {
            this.score = score;
        }
    }

    public static class AcceptTypeScoringEntity implements Comparable<AcceptTypeScoringEntity> {

        private String acceptType;
        private int matchingScore;
        private float qualityScore;

        public AcceptTypeScoringEntity(int matchingScore, float qualityScore) {
            this.matchingScore = matchingScore;
            this.qualityScore = qualityScore;
        }

        public String getAcceptType() {
            return acceptType;
        }

        public void setAcceptType(String acceptType) {
            this.acceptType = acceptType;
        }

        public int getMatchingScore() {
            return matchingScore;
        }

        public void setMatchingScore(int matchingScore) {
            this.matchingScore = matchingScore;
        }

        public float getQualityScore() {
            return qualityScore;
        }

        public void setQualityScore(float qualityScore) {
            this.qualityScore = qualityScore;
        }

        @Override
        public int compareTo(AcceptTypeScoringEntity otherEntity) {
            if (this.getMatchingScore() == otherEntity.getMatchingScore()) {
                if (this.getQualityScore() == otherEntity.getQualityScore()) {
                    return 0;
                } else {
                    return this.getQualityScore() < otherEntity.getQualityScore() ? -1 : 1;
                }
            } else {
                return this.getMatchingScore() < otherEntity.getMatchingScore() ? -1 : 1;
            }
        }
    }



}
