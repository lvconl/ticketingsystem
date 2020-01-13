package ticketingsystem.service.impl;

import ticketingsystem.VerfiyUtil;
import ticketingsystem.entity.Seat;
import ticketingsystem.entity.Train;
import ticketingsystem.service.ITrainService;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 列车业务方法实现类
 *
 * @author lyuconl
 */
public class TrainServiceImpl implements ITrainService {

    private int routeNum;
    private int seatNum;
    private int coachNum;
    private int stationNum;
    private int totalSeatNum;
    private Train[] trains;

    public TrainServiceImpl(int routeNum, int seatNum, int coachNum, int stationNum) {
        this.routeNum = routeNum;
        this.seatNum = seatNum;
        this.coachNum = coachNum;
        this.stationNum = stationNum;
        this.totalSeatNum = seatNum * coachNum;

        trains = new Train[routeNum + 1];
        for (int i = 0; i < trains.length; i++) {
            trains[i] = new Train(seatNum, coachNum, stationNum);
        }
    }

    @Override
    public Seat getFreeSeatByRoute(int route, int departure, int arrival) {
        Seat seat = null;
//        boolean finded = false;
//        for (int i = 1; i <= totalSeatNum; i++) {
//            if (isEmptySeat(trains[route], i, departure, arrival)) {
//                trains[route].getLock()[i].lock();
//                try {
//                    if (isEmptySeat(trains[route], i, departure, arrival)) {
//                        seat = new Seat(i % seatNum + 1, i % seatNum == 0 ?  (i / seatNum) : (i / seatNum + 1));
//                        setSeatNonEmpty(trains[route], i, departure, arrival);
//                        finded = true;
//                        break;
//                    }
//                } finally {
//                    trains[route].getLock()[i].unlock();
//                }
//            }
//        }
        for (int i = 1; i <= coachNum; i++) {
            for (int j = 1; j <= seatNum; j++) {
                int seatNumber = (i - 1) * seatNum + j;
                if (isEmptySeat(trains[route], seatNumber, departure, arrival)) {
                    trains[route].getLock()[seatNumber].lock();
                    try {
                        if (isEmptySeat(trains[route], seatNumber, departure, arrival)) {
                            seat = new Seat(j, i);
                            setSeatNonEmpty(trains[route], seatNumber, departure, arrival);
                            break;
                        }
                    } finally {
                        trains[route].getLock()[seatNumber].unlock();
                    }
                }
            }
        }
        return seat;
    }

    @Override
    public int getFreeSeatCountByRoute(int route, int departure, int arrival) {
        int result = 0;
        for (int i = 1; i <= totalSeatNum; i++) {
            if (isEmptySeat(trains[route], i, departure, arrival)) {
                result++;
            }
        }
        return result;
    }

    @Override
    public boolean refundTicket(Seat seat, int route, int departure, int arrival) {
//        if (seat.getSeatNum() >= totalSeatNum || seat.getCoachNum() >= coachNum) {
//            return false;
//        }
        int seatNumber = (seat.getCoachNum() - 1) * coachNum + seat.getSeatNum();
        trains[route].getLock()[seatNumber].lock();
        try {
            for (int i = departure; i < arrival; i++) {
                trains[route].getCoachs()[seatNumber][i] = false;
            }
        } finally {
            trains[route].getLock()[seatNumber].unlock();
        }
        return true;
    }

    @Override
    public boolean verfiy(ConcurrentLinkedQueue<?> tickets) {
        return VerfiyUtil.verfiy(tickets, trains);
    }

    /**
     * 根据座位号，始发站，终点站判断该座位是否为空
     *
     * @param seatNum 座位号
     * @param departure 始发站编号
     * @param arrival 终点站编号
     * @return 座位状态，空座返回true，非空返回false
     */
    private boolean isEmptySeat(Train train, int seatNum, int departure, int arrival) {
        for (int i = departure; i < arrival; i++) {
            if (train.getCoachs()[seatNum][i]) {
                return false;
            }
        }
        return true;
    }

    /**
     * 将座位置为非空
     *
     * @param train 车次
     * @param seatNum 座位号
     * @param departure 始发站
     * @param arrival 终点站
     */
    private void setSeatNonEmpty(Train train, int seatNum, int departure, int arrival) {
        for (int i = departure; i < arrival; i++) {
            train.getCoachs()[seatNum][i] = true;
        }
    }

    @Override
    public Train[] getTrains() {
        return trains;
    }
}
