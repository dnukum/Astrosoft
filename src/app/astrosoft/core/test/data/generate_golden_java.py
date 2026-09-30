import json

with open("panchang_golden_tests.json", "r") as f:
    data = json.load(f)

out = []
out.append("package app.astrosoft.core.test;")
out.append("")
out.append("import java.util.List;")
out.append("import java.util.ArrayList;")
out.append("")
out.append("public class PanchangGoldenData {")
out.append("")
out.append("    public static class TestCase {")
out.append("        public String name;")
out.append("        public double lat;")
out.append("        public double lon;")
out.append("        public String tzId;")
out.append("        public String dateStr;")
out.append("        public String sunrise;")
out.append("        public String sunset;")
out.append("        public Integer nakshatraIdx;")
out.append("        public Integer tithiIdx;")
out.append("        public TestCase(String n, double la, double lo, String t, String d, String sr, String sn, Integer nak, Integer ti) {")
out.append("            name = n; lat = la; lon = lo; tzId = t; dateStr = d; sunrise = sr; sunset = sn; nakshatraIdx = nak; tithiIdx = ti;")
out.append("        }")
out.append("    }")
out.append("")
out.append("    public static List<TestCase> getCases() {")
out.append("        List<TestCase> list = new ArrayList<TestCase>();")

for item in data:
    n = f'"{item["name"]}"'
    la = item["lat"]
    lo = item["lon"]
    t = f'"{item["tz_id"]}"'
    dateStr = f'"{item["date"]}"'
    sunrise = f'"{item["sunrise"]}"' if item["sunrise"] else 'null'
    sunset = f'"{item["sunset"]}"' if item["sunset"] else 'null'
    nak = item["nakshatra_idx"] if item["nakshatra_idx"] is not None else 'null'
    ti = item["tithi_idx"] if item["tithi_idx"] is not None else 'null'
    out.append(f"        list.add(new TestCase({n}, {la}, {lo}, {t}, {dateStr}, {sunrise}, {sunset}, {nak}, {ti}));")

out.append("        return list;")
out.append("    }")
out.append("}")

with open("src/app/astrosoft/core/test/PanchangGoldenData.java", "w") as f:
    f.write("\n".join(out))
