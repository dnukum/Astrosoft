import json
import datetime
from jhora.panchanga import drik
from jhora.panchanga.drik import swe

def hour_to_str(h):
    hours = int(h)
    minutes = int((h - hours) * 60)
    seconds = int((((h - hours) * 60) - minutes) * 60)
    return f"{hours:02d}:{minutes:02d}:{seconds:02d}"

locations = [
    {"name": "Sammamish", "lat": 47.6101, "lon": -122.0435, "tz": -8.0, "tz_id": "America/Los_Angeles"},
    {"name": "Houston", "lat": 29.7604, "lon": -95.3698, "tz": -6.0, "tz_id": "America/Chicago"},
    {"name": "New_York", "lat": 40.7128, "lon": -74.0060, "tz": -5.0, "tz_id": "America/New_York"},
    {"name": "Chennai", "lat": 13.0827, "lon": 80.2707, "tz": 5.5, "tz_id": "Asia/Kolkata"},
    {"name": "London", "lat": 51.5074, "lon": -0.1278, "tz": 0.0, "tz_id": "Europe/London"},
    {"name": "Phoenix", "lat": 33.4484, "lon": -112.0740, "tz": -7.0, "tz_id": "US/Arizona"},
    {"name": "Sydney", "lat": -33.8688, "lon": 151.2093, "tz": 10.0, "tz_id": "Australia/Sydney"},
    {"name": "Kathmandu", "lat": 27.7172, "lon": 85.3240, "tz": 5.75, "tz_id": "Asia/Kathmandu"},
    {"name": "Anchorage", "lat": 61.2181, "lon": -149.9003, "tz": -9.0, "tz_id": "America/Anchorage"},
    {"name": "Punta_Arenas", "lat": -53.1500, "lon": -70.9167, "tz": -3.0, "tz_id": "America/Punta_Arenas"},
    {"name": "Quito", "lat": -0.1807, "lon": -78.4678, "tz": -5.0, "tz_id": "America/Guayaquil"}
]

dates = [
    (2026, 1, 15),
    (2026, 5, 15),
    (2026, 8, 15),
    (2026, 10, 15),
    (2026, 3, 8),
    (2026, 11, 1),
    (2026, 4, 5),
    (2026, 10, 4),
    (2026, 6, 21),
    (2026, 12, 21),
    (2024, 2, 29)
]

results = []
for loc in locations:
    p = drik.Place(loc["name"], loc["lat"], loc["lon"], loc["tz"])
    for d in dates:
        jd = swe.julday(d[0], d[1], d[2], 12.0, swe.GREG_CAL)
        
        try:
            sr = drik.sunrise(jd, p)
            sunrise_str = hour_to_str(sr[0]) if sr else None
        except: sunrise_str = None
            
        try:
            sn = drik.sunset(jd, p)
            sunset_str = hour_to_str(sn[0]) if sn else None
        except: sunset_str = None
            
        try:
            nak = drik.nakshatra(jd, p)
            nak_idx = nak[0]
        except: nak_idx = None

        try:
            ti = drik.tithi(jd, p)
            ti_idx = ti[0]
        except: ti_idx = None
            
        results.append({
            "name": loc["name"],
            "lat": loc["lat"],
            "lon": loc["lon"],
            "tz_id": loc["tz_id"],
            "date": f"{d[0]}-{d[1]:02d}-{d[2]:02d}",
            "sunrise": sunrise_str,
            "sunset": sunset_str,
            "nakshatra_idx": nak_idx,
            "tithi_idx": ti_idx
        })

with open("panchang_golden_tests.json", "w") as f:
    json.dump(results, f, indent=4)
