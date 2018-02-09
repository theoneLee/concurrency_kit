package version1;

public class ProduceConsumerVersion1 {

    private int i = 1;

    final private Object LOCK = new Object();

    private void produce() {
        synchronized (LOCK) {
            System.out.println("P->" + (i++));
        }
    }

    private void consume() {
        synchronized (LOCK) {
            System.out.println("C->" + i);
        }
    }

    /**
     * 这个会导致生产者线程生产的内容，消费者线程重复消费同一个内容，并且生产者会无止境的生产除非抢占不到锁
     * @param args
     */
    public static void main(String[] args) {

        ProduceConsumerVersion1 pc = new ProduceConsumerVersion1();

        new Thread("P") {
            @Override
            public void run() {
                while (true)
                    pc.produce();
            }
        }.start();

        new Thread("C") {
            @Override
            public void run() {
                while (true)
                    pc.consume();
            }
        }.start();
    }
}

