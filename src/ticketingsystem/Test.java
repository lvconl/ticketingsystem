package ticketingsystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 测试类
 * @author 吕从雷
 */
public class Test {

    private static final int TOTAL_CALL_NUM = 100000;

    private static AtomicInteger counter = new AtomicInteger(0);

    /**
     * 向线程池提交的任务，即票务代理
     */
    private static class TicketTask implements Runnable {

        /**
         * 票务方法
         */
        private final TicketingDS tds;

        /**
         * 车次数，用于测试时随机生成购票车次
         */
        private final int routeNum;

        /**
         * 车站树，用于测试时随机生成始发站和终点站
         */
        private final int stationNum;

        /**
         * 总的调用数，即该线程总调用方法数
         */
        private final int totalNum;

        /**
         * 方法调用次数计数器
         * 用于记录某个线程调用查询、购票、退票的次数
         */
        private final AtomicInteger[] callCounters;

        /**
         * 方法执行时间计数器
         * 用于记录某个线程调用某个方法所使用时间
         */
        private final AtomicLong[] callTimers;

        /**
         * 用于存放已购的车票，执行退票时随机从中抽取车票执行退票操作
         */
        private final Collection<Ticket> allTickets;

        TicketTask(TicketingDS tds, int routeNum, int stationNum, int totalNum, AtomicInteger[] callCounters,
                    AtomicLong[] callTimers, Collection<Ticket> allTickets) {
            this.tds = tds;
            this.routeNum = routeNum;
            this.stationNum = stationNum;
            this.totalNum = totalNum;
            this.callCounters = callCounters;
            this.callTimers = callTimers;
            this.allTickets = allTickets;
        }

        @Override
        public void run() {
            ArrayList<Ticket> tickets = new ArrayList<>();

            // 随机数生成器，用于下列操作中随机生成操作类型，车次，始发站，终点站等信息
            Random random = new Random();
            long startTime, endTime;
            int type, route, departure, arrival;
            int laveCount; /* 余票数量，减轻for循环带来的内存消耗 */
            int index; /* 退票索引 */
            Ticket ticket;

            for (int i = 0; i < totalNum; i++) {
                counter.getAndIncrement();

                startTime = System.currentTimeMillis();

                type = random.nextInt(100);

                if (type < 80) {
                    type = 0;
                } else if (type < 95) {
                    type = 1;
                } else {
                    type = 2;
                }

                if (type == 0) { /* 查询操作 */
                    /* 随机生成车次、始发站、终点站， 为简单起见，各种操作均从小端到大端操作 */
                    route = random.nextInt(routeNum) + 1;
                    departure = random.nextInt(stationNum - 1) + 1;
                    arrival = departure + random.nextInt(stationNum - departure) + 1;

                    /* 防止始发站和终点站重合 */
                    while (departure == arrival) {
                        departure = random.nextInt(stationNum - 1);
                        arrival = departure + random.nextInt(stationNum - departure);
                    }

                    laveCount = tds.inquiry(route, departure, arrival);
                    //System.out.println("route " + route + ": " + departure + "-->" + arrival + ",free seat : " + laveCount);
                } else if (type == 1) { /* 购票操作 */
                    String passenger = "passenger" + i;
                    route = random.nextInt(routeNum) + 1;
                    departure = random.nextInt(stationNum - 1) + 1;
                    arrival = departure + random.nextInt(stationNum - departure) + 1;

                    /* 防止始发站和终点站重合 */
                    while (departure == arrival) {
                        departure = random.nextInt(stationNum - 1);
                        arrival = departure + random.nextInt(stationNum - departure);
                    }
                    ticket = tds.buyTicket(passenger, route, departure, arrival);
                    if (ticket == null) {
                        //System.out.println("route " + route + ": " + departure + "-->" + arrival + ",passenger" + passenger + ", buy ticket failed.");
                    } else {
//                        System.out.println(String.format("route %d: %d --> %d, passenger: %s, buy ticket success.",
//                                route, departure, arrival, passenger));
                        //System.out.println("route " + route + ": " + departure + "-->" + arrival + ",passenger" + passenger + ", buy ticket success");
                        tickets.add(ticket);
                        allTickets.add(ticket);
                    }
                } else { /* 退票操作 */
                    if (!tickets.isEmpty()) {
                        index = random.nextInt(tickets.size());
                        ticket = tickets.remove(index);
                        allTickets.remove(ticket);
                        if (ticket != null) {
                            if (tds.refundTicket(ticket)) {
//                                System.out.println(String.format("route %d: %d --> %d, passenger: %s. refund ticket, the id: %d",
//                                                        ticket.route, ticket.departure, ticket.arrival,
//                                                        ticket.passenger, ticket.tid));
                                //System.out.println("refund ticket, the id: " + ticket.tid);
                            } else {
//                                System.out.println(String.format("route %d: %d --> %d, passenger: %s. refund ticket failed, the id: %d",
//                                            ticket.route, ticket.departure, ticket.arrival,
//                                            ticket.passenger, ticket.tid));
                                //System.out.println("refund ticket failed, the id: " + ticket.tid);
                            }
                        }
                    }
                }
                endTime = System.currentTimeMillis();
                callCounters[type].incrementAndGet();
                callTimers[type].addAndGet(endTime - startTime);
            }

//            for (int i = 0; i < totalNum; i++) {
//                startTime = System.currentTimeMillis();
//                type = random.nextInt(100);
//                if (type < 60) {
//                    // 查询操作
//                    type = 0;
//                } else if (type < 90) {
//                    // 购票操作
//                    type = 1;
//                } else {
//                    // 退票操作
//                    type = 2;
//                }
//
//                if (type == 0) { /* 查询操作 */
//                    /* 随机生成车次，始发站，终点站，为简单起见，各种操作均从小端到大端操作 */
//                    route = random.nextInt(routeNum);
//                    departure = random.nextInt(stationNum - 1);
//                    arrival = departure + random.nextInt(stationNum - departure);
//                    /* 防止出发地和终点站重复 */
//                    while (departure == arrival) {
//                        departure = random.nextInt(stationNum - 1);
//                        arrival = departure + random.nextInt(stationNum - departure);
//                    }
//                    laveCount = tds.inquiry(route, departure, arrival);
//                    System.out.println(String.format("route-%d: %d --> %d, laveCount: %d", route, departure, arrival, laveCount));
//                }
//                if (type == 1) { /* 购票操作 */
//                    StringBuffer passenger = new StringBuffer("passenger").append(i);
//                    route = random.nextInt(routeNum);
//                    departure = random.nextInt(stationNum);
//                    arrival = departure + random.nextInt(stationNum - departure);
//                    /* 防止出发地和终点站重复 */
//                    while (departure == arrival) {
//                        departure = random.nextInt(stationNum - 1);
//                        arrival = departure + random.nextInt(stationNum - departure);
//                    }
//
//                    ticket = tds.buyTicket(passenger.toString(), route, departure, arrival);
//                    if (ticket == null) { /* 出票失败 */
//                        System.out.println(String.format("route-%d: %d --> %d, passenger: %s. sold out!", route, departure, arrival, passenger));
//                    } else {
//                        System.out.println(String.format("route-%d: %d --> %d, passenger: %s. success buy ticket", route, departure, arrival, passenger));
//                    }
//                    tickets.add(ticket);
//                    allTickets.add(ticket);
//                }
//                if (type == 2) { /* 退票操作 */
//                    if (!tickets.isEmpty()) { /* 当前有票可退 */
//                        while (true) {
//                            index = random.nextInt(tickets.size()); /* 根据当前票的数量，随机抽取一张票执行退票操作 */
//                            ticket = tickets.remove(index);
//                            if (ticket != null) {
//                                if (!tds.refundTicket(ticket)) {
//                                    System.out.println(String.format("route-%d: %d --> %d, passenger: %s. failed refund.",
//                                                        ticket.getRoute(), ticket.getDeparture(), ticket.getArrival(), ticket.getPassenger()));
//                                } else {
//                                    System.out.println(String.format("route-%d: %d --> %d, passenger: %s. success refund.",
//                                            ticket.getRoute(), ticket.getDeparture(), ticket.getArrival(), ticket.getPassenger()));
//                                }
//                                break;
//                            }
//                        }
//                    }
//                }
//                endTime = System.currentTimeMillis();
//                callCounters[type].incrementAndGet();
//                callTimers[type].addAndGet(endTime - startTime);
//            }
        }
    }

    /**
     * 多线程测试
     * @param prefix 测试类型前缀
     * @param routeNum 车次数
     * @param coachNum 车厢数
     * @param seatNum 座位数
     * @param stationNum 车站数
     * @param threadNum 并发的线程数
     * @param totalCallNum 每个线程调用的方法总数
     */
    private static void multiThreadTest(String prefix, int routeNum, int coachNum, int seatNum,
                                         int stationNum, int threadNum, int totalCallNum) throws InterruptedException {
        System.out.println(String.format("current test--%s: %d thread, %d call", prefix, threadNum, totalCallNum));

        long startTime = System.currentTimeMillis();

        final TicketingDS tds = new TicketingDS(routeNum, coachNum, seatNum, stationNum, threadNum);

        ExecutorService executorService = Executors.newFixedThreadPool(threadNum);

        /**
         * 存放所有线程的调用次数，执行时间
         */
        final ArrayList<AtomicInteger[]> callCounterList = new ArrayList<>(threadNum);
        final ArrayList<AtomicLong[]> callTimerList = new ArrayList<>(threadNum);

        final ConcurrentLinkedQueue<Ticket> allTicket = new ConcurrentLinkedQueue<>();

        long allThreadStartTime = System.currentTimeMillis();

        for (int i= 0; i < threadNum; i++) {
            AtomicInteger[] callCounters = new AtomicInteger[3];
            AtomicLong[] callTimers = new AtomicLong[3];
            for (int j = 0; j < 3; j++) {
                callCounters[j] = new AtomicInteger(0);
                callTimers[j] = new AtomicLong(0);
            }
            callCounterList.add(callCounters);
            callTimerList.add(callTimers);
            executorService.submit(new TicketTask(tds, routeNum, stationNum, totalCallNum, callCounters, callTimers, allTicket));
        }

        executorService.shutdown();

        int minutes = 0;
        while (!executorService.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.println(String.format("%s: waiting %d s...", prefix, minutes++));
        }

        long allThreadStopTime = System.currentTimeMillis();
        long throughTime = allThreadStopTime - allThreadStartTime;

        /* 存放单个线程调用每个方法总数以及调用每个方法所耗费的时间 */
        int[] singleThreadCallNumArr = new int[3];
        long[] singleThreadCallTimeArr = new long[3];

        /* 存放本次测试所有线程对每个方法调用次数的综合以及每个方法所耗费的时间的总和 */
        int[] totalThreadCallNumArr = new int[3];
        long[] totalThreadCallTimeArr = new long[3];

        for (int i = 0; i < callCounterList.size(); i++) {
            AtomicInteger[] callCounter = callCounterList.get(i);
            AtomicLong[] callTimer = callTimerList.get(i);
            for (int j = 0; j < 3; j++) {
                singleThreadCallNumArr[j] = callCounter[j].get();
                singleThreadCallTimeArr[j] = callTimer[j].get();
                totalThreadCallNumArr[j] += singleThreadCallNumArr[j];
                totalThreadCallTimeArr[j] += singleThreadCallTimeArr[j];
            }
//            System.out.println(String.format("%s: %d with: %d inqiry %dms, %d buy %dms, %d refund %dms",
//                                prefix, i, singleThreadCallNumArr[0], singleThreadCallTimeArr[0],
//                                singleThreadCallNumArr[1], singleThreadCallTimeArr[1],
//                                singleThreadCallNumArr[2], singleThreadCallTimeArr[2]));
        }
        System.out.println("correct: " + tds.verfiy(allTicket));
        System.out.println("current test result:");
        System.out.println(String.format("%d inquiry %dms, %d buy %dms, %d refund %dms",
                            totalThreadCallNumArr[0], totalThreadCallTimeArr[0],
                            totalThreadCallNumArr[1], totalThreadCallTimeArr[1],
                            totalThreadCallNumArr[2], totalThreadCallTimeArr[2]));
        System.out.println("inquiry avg " + ((double)totalThreadCallTimeArr[0] / totalThreadCallNumArr[0]) +
                "ms, buy avg " + ((double)totalThreadCallTimeArr[1] / totalThreadCallNumArr[1]) +
                "ms, refund avg " + ((double)totalThreadCallTimeArr[2] / totalThreadCallNumArr[2]) + "ms");

        System.out.println("throughput: " + (((double)threadNum * totalCallNum) / throughTime));
        System.out.println("total method call:" + counter.getAndSet(0));

        System.out.println(String.format("[%d, %d, %d, %d, %d, %d, %.17f, %.17f, %.17f, %.17f]",
                totalThreadCallNumArr[0], totalThreadCallTimeArr[0],
                totalThreadCallNumArr[1], totalThreadCallTimeArr[1],
                totalThreadCallNumArr[2], totalThreadCallTimeArr[2],
                ((double)totalThreadCallTimeArr[0] / totalThreadCallNumArr[0]),
                ((double)totalThreadCallTimeArr[1] / totalThreadCallNumArr[1]),
                ((double)totalThreadCallTimeArr[2] / totalThreadCallNumArr[2]),
                (((double)threadNum * totalCallNum) / throughTime)));

        System.out.println("----------------------");
    }

    public static void main(String[] args) throws InterruptedException {
        final int defaultRouteNum = 20;
        final int defaultCoachNum = 15;
        final int defaultSeatNum = 100;
        final int defaultStationNum = 10;
        final int defaultThreadNum = 4;
        final int defaultTotalCallNum = 100000;

        int coreNumber = Runtime.getRuntime().availableProcessors();

        int[] threadNumbers = {4, 8, 16, 32, 64, 96};

        multiThreadTest("test", defaultRouteNum, defaultCoachNum, defaultSeatNum,
                        defaultStationNum, defaultThreadNum, defaultTotalCallNum);
//        for (int i = 0; i < threadNumbers.length; i++) {
//            multiThreadTest("thread" + threadNumbers[i], defaultRouteNum, defaultCoachNum, defaultSeatNum,
//                    defaultStationNum, threadNumbers[i], defaultTotalCallNum);
//        }
    }
}
