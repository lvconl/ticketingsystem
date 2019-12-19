package ticketingsystem;

import ticketingsystem.entity.Train;

import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * 验证正确性
 *
 * @author lyuconl
 *
 */
public class VerfiyUtil {

    public static boolean verfiy(ConcurrentLinkedQueue<?> tickets, Train[] trains) {
        for (Object obj : tickets) {
            Ticket ticket = (Ticket) obj;
            System.out.println(ticket.tid);
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
