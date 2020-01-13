package ticketingsystem;

import ticketingsystem.entity.Seat;
import ticketingsystem.entity.Train;
import ticketingsystem.service.ITrainService;
import ticketingsystem.service.impl.TrainServiceImpl;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

public class TicketingDS implements TicketingSystem {

    /**
     * 车票id
     */
    private AtomicLong ticketId = new AtomicLong(1);

    private int routeNum;
    private int coachNum;
    private int seatNum;
    private int stationNum;
    private int threadNum;

    private ITrainService trainService;

    public TicketingDS (int routeNum, int coachNum, int seatNum, int stationNum, int threadNum) {
        this.routeNum = routeNum;
        this.coachNum = coachNum;
        this.seatNum = seatNum;
        this.stationNum = stationNum;
        this.threadNum = threadNum;
        trainService = new TrainServiceImpl(routeNum, coachNum, seatNum, stationNum);
    }

    @Override
    public Ticket buyTicket(String passenger, int route, int departure, int arrival) {
        Ticket ticket = null;
        Seat seat = trainService.getFreeSeatByRoute(route, departure, arrival);
        if (seat != null) {
            ticket = new Ticket();
            ticket.tid = ticketId.getAndIncrement();
            ticket.passenger = passenger;
            ticket.route = route;
            ticket.coach = seat.getCoachNum();
            ticket.seat = seat.getSeatNum();
            ticket.departure = departure;
            ticket.arrival = arrival;
        }
        return ticket;
    }

    @Override
    public int inquiry(int route, int departure, int arrival) {
        return trainService.getFreeSeatCountByRoute(route, departure, arrival);
    }

    @Override
    public boolean refundTicket(Ticket ticket) {
        // 检查车票是否合法，不合法返回false
//        if (ticket == null || ticket.route >= routeNum || ticket.departure >= stationNum || ticket.arrival >= stationNum) {
//            return false;
//        }
        Seat seat = new Seat(ticket.seat, ticket.coach);
        return trainService.refundTicket(seat, ticket.route, ticket.departure, ticket.arrival);
    }

    public boolean verfiy(ConcurrentLinkedQueue<Ticket> tickets) {
        Train[] trains = trainService.getTrains();
        for (Ticket ticket : tickets) {
            Train train = trains[ticket.route];
            for (int i = ticket.departure; i < ticket.arrival; i++) {
                if (!train.getCoachs()[ticket.seat][i]) {
                    return false;
                }
            }
        }
        return true;
    }
}
