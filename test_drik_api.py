from jhora.panchanga import drik
import datetime

# Create a place
# Let's see what args Place takes
import inspect
print(inspect.signature(drik.Place))

# Create a place for Houston
# Usually Place(latitude, longitude, timezone)
p = drik.Place(29.7604, -95.3698, -5.0)

d = drik.Date(2026, 7, 4)
print(inspect.signature(drik.sunrise))

sr = drik.sunrise(d, p)
print(sr)

