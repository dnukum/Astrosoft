package app.astrosoft.beans;

import java.util.Date;
import app.astrosoft.consts.Nakshathra;
import app.astrosoft.consts.Rasi;

public class BalamRowBean {
    private Date startTime;
    private Date endTime;
    private Nakshathra transitNakshathra;
    private Rasi transitRasi;

    public BalamRowBean(Date startTime, Date endTime, Nakshathra transitNakshathra, Rasi transitRasi) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.transitNakshathra = transitNakshathra;
        this.transitRasi = transitRasi;
    }

    public Date getStartTime() { return startTime; }
    public Date getEndTime() { return endTime; }
    public Nakshathra getTransitNakshathra() { return transitNakshathra; }
    public Rasi getTransitRasi() { return transitRasi; }
}
