import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.Condition;

/**
 * 使用自定义AQS实现的锁实现一个生产消费模型
 */
public class ProducerConsumerModel {

    final static BasicLock lock = new BasicLock();
    final static Condition notFull = lock.newCondition();
    final static Condition notEmpty = lock.newCondition();

    final static Queue<String> queue = new LinkedBlockingQueue<String>();
    final static int queueSize = 10;


    public static void main(String[] args) {
        Thread producer = new Thread(new Runnable() {
            @Override
            public void run() {

                while (true){
                    lock.lock();
                    try{
                        while(queue.size()==queueSize){
                            notEmpty.await();
                        }

                        queue.add("test");

                        notFull.signalAll();
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        lock.unlock();
                    }
                }


            }
        });

        Thread consumer = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    lock.lock();
                    try {
                        while (queue.size()==0){
                            notFull.await();
                        }
                        String s = queue.poll();
                        System.out.println(s);
                        notEmpty.signalAll();
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        lock.unlock();
                    }

                }
            }
        });

        producer.start();
        consumer.start();

    }



}
