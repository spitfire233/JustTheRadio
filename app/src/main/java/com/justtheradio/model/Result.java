package com.justtheradio.model;

/**
 * This class represents the outcome of an operation that can either succeed or fail.
 */
public abstract class Result {
    /**
     * Empty private constructor for {@link Result}.
     */
    private Result() { }
    /**
     * This method tells whether the operation succeeded
     * @return true if this is a {@link Success}, false if it is a {@link Failure}
     */
    public boolean isSuccess() {
        return this instanceof Success;
    }
    /**
     * Represents a successful outcome of an operation
     * @param <C> The type of the data returned by the operation
     */
    public static final class Success<C> extends Result{
        /**
         * The data produced by the operation
         */
        private final C data;
        /**
         * Constructor for {@link Success}
         * @param data The data produced by the operation
         */
        public Success(C data) {
            this.data = data;
        }
        /**
         * Constructor for {@link Success}. Sets the data produced by the operation to {@code null}
         */
        public Success() {
            this(null);
        }
        /**
         * Getter for the data produced by the operation
         * @return The data produced by the operation
         */
        public C getData() {
            return data;
        }
    }
    /**
     * Represents a failed outcome for an operation
     */
    public static final class Failure extends Result {
        /**
         * The error message produced by the operation
         */
        private final String message;
        /**
         * Constructor for {@link Failure}
         * @param message The error message produced by the operation
         */
        public Failure(String message) {
            this.message = message;
        }
        /**
         * Getter for the error message produced by the operation
         * @return The error message produced by the operation
         */
        public String getMessage() {
            return message;
        }
    }
}
