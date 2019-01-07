package tools.performance;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class CPUSampler {

    private final List<String> included;
    private static final CPUSampler instance = new CPUSampler();
    private long interval;
    private SamplerThread sampler;
    private final Map<StackTrace, Integer> recorded;
    private int totalSamples;

    public CPUSampler() {
        this.included = new LinkedList();

        this.interval = 5L;
        this.sampler = null;
        this.recorded = new HashMap();
        this.totalSamples = 0;
    }

    public static CPUSampler getInstance() {
        return instance;
    }

    public void setInterval(long millis) {
        this.interval = millis;
    }

    public void addIncluded(String include) {
        for (String alreadyIncluded : this.included) {
            if (include.startsWith(alreadyIncluded)) {
                return;
            }
        }
        this.included.add(include);
    }

    public void reset() {
        this.recorded.clear();
        this.totalSamples = 0;
    }

    public void start() {
        if (this.sampler == null) {
            this.sampler = new SamplerThread();
            this.sampler.start();
        }
    }

    public void stop() {
        if (this.sampler != null) {
            this.sampler.stop();
            this.sampler = null;
        }
    }

    public SampledStacktraces getTopConsumers() {
        List ret = new ArrayList();
        Set<Entry<StackTrace, Integer>> entrySet = this.recorded.entrySet();
        for (Map.Entry entry : entrySet) {
            ret.add(new StacktraceWithCount(((Integer) entry.getValue()), (StackTrace) entry.getKey()));
        }
        Collections.sort(ret);
        return new SampledStacktraces(ret, this.totalSamples);
    }

    public void save(Writer writer, int minInvocations, int topMethods) throws IOException {
        SampledStacktraces topConsumers = getTopConsumers();
        StringBuilder builder = new StringBuilder();
        builder.append("Top Methods:\r\n");
        for (int i = 0; (i < topMethods) && (i < topConsumers.getTopConsumers().size()); i++) {
            builder.append(((StacktraceWithCount) topConsumers.getTopConsumers().get(i)).toString(topConsumers.getTotalInvocations(), 1));
        }
        builder.append("\r\nStack Traces:\r\n");
        writer.write(builder.toString());
        writer.write(topConsumers.toString(minInvocations));
        writer.flush();
    }

    private void consumeStackTraces(Map<Thread, StackTraceElement[]> traces) {
        for (Map.Entry trace : traces.entrySet()) {
            int relevant = findRelevantElement((StackTraceElement[]) trace.getValue());
            if (relevant != -1) {
                StackTrace st = new StackTrace((StackTraceElement[]) trace.getValue(), relevant, ((Thread) trace.getKey()).getState());
                Integer i = this.recorded.get(st);
                this.totalSamples += 1;
                if (i == null) {
                    this.recorded.put(st, 1);
                } else {
                    this.recorded.put(st, i + 1);
                }
            }
        }
    }

    private int findRelevantElement(StackTraceElement[] trace) {
        if (trace.length == 0) {
            return -1;
        }
        if (this.included.isEmpty()) {
            return 0;
        }
        int firstIncluded = -1;
        for (String myIncluded : this.included) {
            for (int i = 0; i < trace.length; i++) {
                StackTraceElement ste = trace[i];
                if ((!ste.getClassName().startsWith(myIncluded)) || ((i >= firstIncluded) && (firstIncluded != -1))) {
                    continue;
                }
                firstIncluded = i;
                break;
            }

        }

        if ((firstIncluded >= 0) && (trace[firstIncluded].getClassName().equals("tools.performance.CPUSampler$SamplerThread"))) {
            return -1;
        }
        return firstIncluded;
    }

    public static class SampledStacktraces {

        List<CPUSampler.StacktraceWithCount> topConsumers;
        int totalInvocations;

        public SampledStacktraces(List<CPUSampler.StacktraceWithCount> topConsumers, int totalInvocations) {
            this.topConsumers = topConsumers;
            this.totalInvocations = totalInvocations;
        }

        public List<CPUSampler.StacktraceWithCount> getTopConsumers() {
            return this.topConsumers;
        }

        public int getTotalInvocations() {
            return this.totalInvocations;
        }

        @Override
        public String toString() {
            return toString(0);
        }

        public String toString(int minInvocation) {
            StringBuilder ret = new StringBuilder();
            for (CPUSampler.StacktraceWithCount swc : this.topConsumers) {
                if (swc.getCount() >= minInvocation) {
                    ret.append(swc.toString(this.totalInvocations, 2147483647));
                    ret.append("\r\n");
                }
            }
            return ret.toString();
        }
    }

    public static class StacktraceWithCount
            implements Comparable<StacktraceWithCount> {

        private final int count;
        private final CPUSampler.StackTrace trace;

        public StacktraceWithCount(int count, CPUSampler.StackTrace trace) {
            this.count = count;
            this.trace = trace;
        }

        public int getCount() {
            return this.count;
        }

        public StackTraceElement[] getTrace() {
            return this.trace.getTrace();
        }

        @Override
        public int compareTo(StacktraceWithCount o) {
            return -Integer.valueOf(this.count).compareTo(o.count);
        }

        @Override
        public boolean equals(Object oth) {
            if (!(oth instanceof StacktraceWithCount)) {
                return false;
            }
            StacktraceWithCount o = (StacktraceWithCount) oth;
            return this.count == o.count;
        }

        @Override
        public String toString() {
            return this.count + " Sampled Invocations\r\n" + this.trace.toString();
        }

        private double getPercentage(int total) {
            return Math.round(this.count / total * 10000.0D) / 100.0D;
        }

        public String toString(int totalInvoations, int traceLength) {
            return this.count + "/" + totalInvoations + " Sampled Invocations (" + getPercentage(totalInvoations) + "%) " + this.trace.toString(traceLength);
        }
    }

    private class SamplerThread
            implements Runnable {

        private boolean running = false;
        private boolean shouldRun = false;
        private Thread rthread;

        private SamplerThread() {
        }

        public void start() {
            if (!this.running) {
                this.shouldRun = true;
                this.rthread = new Thread(this, "CPU Sampling Thread");
                this.rthread.start();
                this.running = true;
            }
        }

        public void stop() {
            this.shouldRun = false;
            this.rthread.interrupt();
            try {
                this.rthread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            while (this.shouldRun) {
                CPUSampler.this.consumeStackTraces(Thread.getAllStackTraces());
                try {
                    Thread.sleep(CPUSampler.this.interval);
                } catch (InterruptedException e) {
                    return;
                }
            }
        }
    }

    private static class StackTrace {

        private final StackTraceElement[] trace;
        private final Thread.State state;

        public StackTrace(StackTraceElement[] trace, int startAt, Thread.State state) {
            this.state = state;
            if (startAt == 0) {
                this.trace = trace;
            } else {
                this.trace = new StackTraceElement[trace.length - startAt];
                System.arraycopy(trace, startAt, this.trace, 0, this.trace.length);
            }
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof StackTrace)) {
                return false;
            }
            StackTrace other = (StackTrace) obj;
            if (other.trace.length != this.trace.length) {
                return false;
            }
            if (other.state != this.state) {
                return false;
            }
            for (int i = 0; i < this.trace.length; i++) {
                if (!this.trace[i].equals(other.trace[i])) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public int hashCode() {
            int ret = 13 * this.trace.length + this.state.hashCode();
            for (StackTraceElement ste : this.trace) {
                ret ^= ste.hashCode();
            }
            return ret;
        }

        public StackTraceElement[] getTrace() {
            return this.trace;
        }

        @Override
        public String toString() {
            return toString(-1);
        }

        public String toString(int traceLength) {
            StringBuilder ret = new StringBuilder("State: ");
            ret.append(this.state.name());
            if (traceLength > 1) {
                ret.append("\r\n");
            } else {
                ret.append(" ");
            }
            int i = 0;
            for (StackTraceElement ste : this.trace) {
                i++;
                if (i > traceLength) {
                    break;
                }
                ret.append(ste.getClassName());
                ret.append("#");
                ret.append(ste.getMethodName());
                ret.append(" (Line: ");
                ret.append(ste.getLineNumber());
                ret.append(")\r\n");
            }
            return ret.toString();
        }
    }
}
