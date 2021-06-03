package abc;

import org.slf4j.MDC;
import rx.functions.Action0;

import java.util.Map;
import java.util.concurrent.Callable;


public final class ContextAwareFunction<T> implements Action0, Runnable { 
    private final Action0 actualAction;
    private final Callable<T> actualCallable;
    private Map<String, String> mdcContextMap;

    public ContextAwareFunction(Action0 actualAction) {
        this.actualAction = actualAction;
        this.actualCallable = null;
        mdcContextMap = MDC.getCopyOfContextMap();
    }

    public ContextAwareFunction(Runnable runnable) { 
        this.actualAction = () -> runnable.run(); 
        this.actualCallable = null;
        mdcContextMap = MDC.getCopyOfContextMap();
    }

    public ContextAwareFunction(Callable<T> actualCallable) {
        this.actualAction = null;
        this.actualCallable = actualCallable;
        mdcContextMap = MDC.getCopyOfContextMap();
    }

    @Override
    public void call() {
        wrapMDC(actualAction);
    }

    @Override
    public void run() {
        wrapMDC(actualAction);
    }

    public Callable<T> getCallable() {
        return () -> wrapMDC(actualCallable);
    }

    private T wrapMDC(Callable<T> callable) throws Exception { //NOSONAR
        MDC.setContextMap(mdcContextMap);
        try {
            return callable.call();
        } finally {
            MDC.clear();
        }
    }

    private void wrapMDC(Action0 action) {
        MDC.setContextMap(mdcContextMap);
        try {
            action.call();
        } finally {
            MDC.clear();
        }
    }
}
