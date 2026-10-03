import app.astrosoft.ui.comp.PlaceChooser;
import app.astrosoft.beans.Place;

public class test_getplace {
    public static void main(String[] args) {
        PlaceChooser chooser = new PlaceChooser();
        Place p = chooser.getSelectedPlace();
        System.out.println(p.city() + " " + p.latitude() + " " + p.longitude() + " tz: " + p.astrosoftTimeZone());
    }
}
