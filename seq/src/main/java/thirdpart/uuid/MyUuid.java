package thirdpart.uuid;

public class MyUuid {

    private static MyUuid instance = new MyUuid();

    public static MyUuid getInstance() {
        return instance;
    }

    private MyUuid() {
    }

    private SnowflakeIdWorker idWorker;

    public void init(long centerId, long workerId) {
        idWorker = new SnowflakeIdWorker(workerId, centerId);
    }

    public long getUuid() {
        return idWorker.nextId();
    }
}
