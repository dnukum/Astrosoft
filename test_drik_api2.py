from jhora.panchanga import drik
import datetime
import inspect

p = drik.Place('Houston', 29.7604, -95.3698, -5.0)
d = drik.Date(2026, 7, 4)

print("sunrise:", inspect.signature(drik.sunrise))
print("sunrise val:", drik.sunrise(d, p))
print("nakshatra val:", drik.nakshatra(d, p))

