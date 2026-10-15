package com.justtheradio.model;

public abstract class Result {

    private Result() { }

    public boolean isSuccess() {
        return this instanceof Success;
    }

    public static final class Success<C> extends Result{
        private final C data;

        public Success(C data) {
            this.data = data;
        }

        public Success() {
            this(null);
        }

        public C getData() {
            return data;
        }
    }

    public static final class Failure extends Result {
        private final String message;

        public Failure(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

    }


}
