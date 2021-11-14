package com.wzy;

import lombok.extern.slf4j.Slf4j;

/**
 * 两阶段终止模式
 */
@Slf4j(topic = "Test04")
public class Test04 {

    public static void main(String[] args) throws InterruptedException {
        TwoPhaseTermination tpt = new TwoPhaseTermination();
        tpt.start();

        Thread.sleep(3500);
        tpt.stop();
    }

}

@Slf4j(topic = "TwoPhaseTermination")
class TwoPhaseTermination {
    private Thread monitor;

    public void start() {
        monitor = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Thread current = Thread.currentThread();
                    if (current.isInterrupted()) {
                        //处理后事
                        log.info("处理后事");
                        break;
                    } else {
                        try {
                            Thread.sleep(1000);
                            log.info("监控");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            //防止sleep的线程被打断后，无法正常停止。因为调用下面的stop方法可能会打断sleep的线程
                            //interrupted标志位会被置为false，代码无法走到处理后事的那一步
                            //current.interrupt();
                        }
                    }
                }
            }
        });

        monitor.start();
    }

    public void stop() {
        monitor.interrupt();
    }
}