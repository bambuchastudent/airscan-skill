package dev.airscan.worker;

/** Independent worker process entry point. No network, crawler or job protocol. */
public final class WorkerBootstrap {
    private WorkerBootstrap() {}

    public static void main(String[] args) {
        System.out.println("airscan-worker-bootstrap ready");
    }
}
