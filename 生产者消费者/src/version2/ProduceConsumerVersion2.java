package version2;

import java.util.stream.Stream;

/**
 * 该demo可展示wait和notify作用和内部机制，以及waiting状态和blocked状态和runnable状态的转化
 */
public class ProduceConsumerVersion2 {

    private int i = 0;

    final private Object LOCK = new Object();

    private volatile boolean isProduced = false;

    public void produce() {
        synchronized (LOCK) {
            if (isProduced) {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                i++;
                System.out.println("P->" + i);
                LOCK.notify();
                isProduced = true;
            }
        }
    }

    public void consume() {
        synchronized (LOCK) {
            if (isProduced) {
                System.out.println("C->" + i);
                LOCK.notify();
                isProduced = false;
            } else {
                try {
                    LOCK.wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {


        ProduceConsumerVersion2 pc = new ProduceConsumerVersion2();

        //todo 单生产者，单消费者，缓冲区大小为1时，可正常运作
        new Thread("P1") {
            @Override
            public void run() {
                while (true)
                    pc.produce();
            }
        }.start();
        new Thread("C1") {
            @Override
            public void run() {
                while (true)
                    pc.consume();
            }
        }.start();

        //todo 多生产者，多消费者，缓冲区大小为1时，会出现全部线程都进入waiting状态，导致假死（注意：此时不是处于死锁）
//        Stream.of("P1", "P2").forEach(n ->
//                new Thread(n) {
//                    @Override
//                    public void run() {
//                        while (true)
//                            pc.produce();
//                    }
//                }.start()
//        );
//        Stream.of("C1", "C2").forEach(n ->
//                new Thread(n) {
//                    @Override
//                    public void run() {
//                        while (true)
//                            pc.consume();
//                    }
//                }.start()
//        );
    }
}