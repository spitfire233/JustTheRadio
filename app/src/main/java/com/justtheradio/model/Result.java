package com.justtheradio.model;

import java.util.Collection;

public abstract class Result {

    private Result() { }

    public boolean isSuccess() {
        return this instanceof Success;
    }

    public static final class Success<C> extends Result{
        private final C response;

        public Success(C data) {
            this.response = data;
        }

        public Success() {
            this(null);
        }

        public C getResponse() {
            return response;
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
