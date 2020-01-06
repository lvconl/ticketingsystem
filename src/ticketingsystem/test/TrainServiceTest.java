package ticketingsystem.test;

import ticketingsystem.entity.Seat;
import ticketingsystem.service.ITrainService;
import ticketingsystem.service.impl.TrainServiceImpl;

import java.util.ArrayList;

/**
 * 列车业务方法类
 *
 * @author lyuconl
 *
 */
public class TrainServiceTest {
    private static ITrainService trainService = new TrainServiceImpl(5, 100, 8, 10);
    public static void main(String[] args) {
        ArrayList<Seat> allSeat = new ArrayList<>();
        Seat seat = null;
        int route = 3;

        for (int i = 0; i < 300; i++) {
            seat = trainService.getFreeSeatByRoute(route, 2, 3);
            allSeat.add(seat);
        }


        System.out.println(trainService.getFreeSeatCountByRoute(route, 2, 3));

        seat = trainService.getFreeSeatByRoute(route, 2, 3);
        System.out.println(seat.getCoachNum());
        System.out.println(trainService.getFreeSeatCountByRoute(route, 2, 3));
    }
}
