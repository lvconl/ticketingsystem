package ticketingsystem.service;

import ticketingsystem.entity.Seat;
import ticketingsystem.entity.Train;

import java.util.concurrent.ConcurrentLinkedQueue;

public interface ITrainService {
    /**
     * 根据车次号获得空座位，有空坐返回座位实例，否则返回null
     * @param route 车次
     * @param departure 始发站
     * @param arrival 终点站
     * @return 座位实例或null
     */
    Seat getFreeSeatByRoute(int route, int departure, int arrival);

    /**
     * 根据车次，始发站，终点站查询空余座位数量
     * @param route 车次
     * @param departure 始发站
     * @param arrival 终点站
     * @return 空余座位数量
     */
    int getFreeSeatCountByRoute(int route, int departure, int arrival);

    /**
     * 退票方法
     * @param seat 座位实例
     * @param route 车次
     * @param departure 始发站
     * @param arrival 终点站
     * @return 成功返回true，失败返回false
     */
    boolean refundTicket(Seat seat, int route, int departure, int arrival);

    /**
     * 验证正确性
     *
     * 已售出且未退回的票与车座当前状态进行比较
     *
     * @param tickets 已售出且未退回的票
     * @return 正确与否
     */
    boolean verfiy(ConcurrentLinkedQueue<?> tickets);

    public Train[] getTrains();
}
