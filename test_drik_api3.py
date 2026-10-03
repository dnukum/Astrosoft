from jhora.panchanga import drik
from jhora.panchanga.drik import swe

jd = swe.julday(2026, 7, 4, 12.0)
p = drik.Place('Houston', 29.7604, -95.3698, -5.0)

sr = drik.sunrise(jd, p)
sn = drik.sunset(jd, p)
nak = drik.nakshatra(jd, p)
ti = drik.tithi(jd, p)
print(f"Sunrise JD: {sr[0]}")
print(f"Sunset JD: {sn[0]}")
print(f"Nakshatra: {nak}")
print(f"Tithi: {ti}")
