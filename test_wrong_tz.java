import app.astrosoft.util.AstroUtil;

public class test_wrong_tz {
    public static void main(String[] args) {
        double sriseHoustonWithSeattleTZ = AstroUtil.getSunRise(2026, 9, 29, -95.35, 29.75, -7.0);
        System.out.println("Houston with Seattle TZ Sunrise: " + AstroUtil.timeFormat(sriseHoustonWithSeattleTZ, false));
    }
}
