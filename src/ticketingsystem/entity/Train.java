package ticketingsystem.entity;

import java.util.concurrent.locks.ReentrantLock;

/**
 * 列车类
 *
 * @author lyuconl
 */
public class Train {
    private boolean[][] coachs;
    private int seatNum;
    private int coachNum;
    private int stationNum;
    private ReentrantLock[] lock;

    public Train(int seatNum, int coachNum, int stationNum) {
        coachs = new boolean[seatNum * coachNum + 1][stationNum + 1];
        this.seatNum = seatNum;
        this.coachNum = coachNum;
        this.stationNum = stationNum;
        lock = new ReentrantLock[seatNum * coachNum + 1];

        for (int i = 0; i < lock.length; i++) {
            lock[i] = new ReentrantLock();
        }
    }

    public boolean[][] getCoachs() {
        return coachs;
    }

    public void setCoachs(boolean[][] coachs) {
        this.coachs = coachs;
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

    public int getStationNum() {
        return stationNum;
    }

    public void setStationNum(int stationNum) {
        this.stationNum = stationNum;
    }

    public ReentrantLock[] getLock() {
        return lock;
    }

    public void setLock(ReentrantLock[] lock) {
        this.lock = lock;
    }
}
