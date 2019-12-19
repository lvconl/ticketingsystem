package ticketingsystem.entity;

public class Seat {
    private int seatNum;
    private int coachNum;

    public Seat() {}

    public Seat(int seatNum, int coachNum) {
        this.seatNum = seatNum;
        this.coachNum = coachNum;
    }

    public int getSeatNum() {
        return seatNum;
    }

    public void setSeatNum(int seatNum) {
        this.seatNum = seatNum;
    }

    public int getCoachNum() {
        return coachNum;
    }

    public void setCoachNum(int coachNum) {
        this.coachNum = coachNum;
    }
}
