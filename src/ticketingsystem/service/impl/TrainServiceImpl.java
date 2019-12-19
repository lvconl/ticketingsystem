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
        boolean finded = false;
        for (int i = 1; i <= coachNum; i++) {
            for (int j = ((i - 1) * seatNum + 1); j <= totalSeatNum; j++) {
                if (isEmptySeat(trains[route], j, departure, arrival)) {
                    trains[route].getLock()[j].lock();
                    try {
                        if (isEmptySeat(trains[route], j, departure, arrival)) {
                            seat = new Seat(j, i);
                            setSeatNonEmpty(trains[route], j, departure, arrival);
                            finded = true;
                            break;
                        }
                    } finally {
                        trains[route].getLock()[j].unlock();
                    }
                }
            }
            if (finded) {
                break;
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
        if (seat.getSeatNum() >= totalSeatNum || seat.getCoachNum() >= coachNum) {
            return false;
        }
        trains[route].getLock()[seat.getSeatNum()].lock();
        try {
            for (int i = departure; i < arrival; i++) {
                trains[route].getCoachs()[seat.getSeatNum()][i] = false;
            }
        } finally {
            trains[route].getLock()[seat.getSeatNum()].unlock();
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
