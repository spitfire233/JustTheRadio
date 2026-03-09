package com.justtheradio.model;

import java.util.List;

public abstract class Result {
    private Result() { }
    public boolean isSuccess() {
        return (this instanceof Success);
    }
    public static final class Success extends Result{
        private final List<RadioStation> radioStations;
        public Success(List<RadioStation> radioStations) {
            this.radioStations = radioStations;
        }
        public List<RadioStation> getRadioStations() {
            return radioStations;
        }
    }
    public static final class Error extends Result {
        private final String errorMessage;
        public Error(String errorMessage) {
            this.errorMessage = errorMessage;
        }
        public String getErrorMessage() {
            return errorMessage;
        }
    }
}
