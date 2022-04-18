package com.cloudinary.test.rules;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Objects;

public class RetryRule implements TestRule {
    private int retryCount;
    private int delay;

    public RetryRule(int retryCount, int delay) {
        this.retryCount = retryCount;
        this.delay = delay;
    }

    public RetryRule() {
        this.retryCount = 3;
        this.delay = 3;
    }

    public Statement apply(Statement base, Description description) {
        return statement(base, description);
    }

    private Statement statement(final Statement base, final Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                Throwable caughtThrowable = null;
                for (int i = 0; i < retryCount; i++) {
                    Thread.sleep(delay * 1000);
                    try {
                        base.evaluate();
                        return;
                    } catch (Throwable t) {
                        caughtThrowable = t;
                        System.err.println(description.getDisplayName() + ": run " + (i + 1) + " failed.");
                    }
                }
                System.err.println(description.getDisplayName() + ": Giving up after " + retryCount + " failures.");
                throw Objects.requireNonNull(caughtThrowable);
            }
        };
    }
}
