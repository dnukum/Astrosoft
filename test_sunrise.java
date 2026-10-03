import app.astrosoft.util.AstroUtil;

public class test_sunrise {
    public static void main(String[] args) {
        double sriseSeattle = AstroUtil.getSunRise(2026, 9, 29, -122.0, 47.6, -7.0);
        double sriseHouston = AstroUtil.getSunRise(2026, 9, 29, -95.35, 29.75, -5.0);
        System.out.println("Seattle Sunrise: " + AstroUtil.timeFormat(sriseSeattle, false));
        System.out.println("Houston Sunrise: " + AstroUtil.timeFormat(sriseHouston, false));
    }
}
