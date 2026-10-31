#!/usr/bin/env python3
"""
Step 4 Generator: Probes PyJHora ephemeris across dates/times to build synthetic test cases
for the Astrosoft Planetary Positions status-suffix test matrix (Step 3).

Outputs: src/app/astrosoft/ui/test/resources/planet_status_cases.json
"""

import json
import math
import os
import sys
from datetime import datetime, timedelta

# Ensure PyJHora is on sys.path
PYJHORA_SRC = "/Users/mukunds100/Dev/PyJHora/src"
if PYJHORA_SRC not in sys.path:
    sys.path.insert(0, PYJHORA_SRC)

import swisseph as swe
from jhora import const, utils
from jhora.panchanga import drik
from jhora.horoscope.chart import charts

RASI_NAMES = [
    "Mesha", "Vrishabha", "Mithuna", "Kataka",
    "Simha", "Kanya", "Thula", "Vrichika",
    "Dhanus", "Makara", "Kumbha", "Meena",
]

PLANET_NAMES = [
    "Sun", "Moon", "Mars", "Mercury",
    "Jupiter", "Venus", "Saturn", "Rahu", "Ketu",
]

# Whole-sign dignity tables per B.V. Raman (Graha and Bhava Balas, Table II) & Planet.java
EXALTED_SIGN = {0: 0, 1: 1, 2: 9, 3: 5, 4: 3, 5: 11, 6: 6}
DEBILITATED_SIGN = {0: 6, 1: 7, 2: 3, 3: 11, 4: 9, 5: 5, 6: 0}
MOOLATRIKONA_SIGN = {0: 4, 1: 1, 2: 0, 3: 5, 4: 8, 5: 6, 6: 10}
OWN_SIGNS = {
    0: {4},
    1: {3},
    2: {0, 7},
    3: {2, 5},
    4: {8, 11},
    5: {1, 6},
    6: {9, 10},
}

# B.V. Raman degree-range Moolatrikona (for comparison metadata)
RAMAN_MT_DEGREE_RANGE = {
    0: (4, 0.0, 20.0),    # Sun: Leo 0-20
    1: (1, 3.0, 30.0),    # Moon: Taurus 3-30 (after 0-3 Exaltation)
    2: (0, 0.0, 12.0),    # Mars: Aries 0-12
    3: (5, 15.0, 20.0),   # Mercury: Virgo 15-20 (after 0-15 Exaltation)
    4: (8, 0.0, 10.0),    # Jupiter: Sagittarius 0-10
    5: (6, 0.0, 15.0),    # Venus: Libra 0-15
    6: (10, 0.0, 20.0),   # Saturn: Aquarius 0-20
}

# Combustion orbs (degrees from Sun)
# 1) Authoritative B.V. Raman / Surya Siddhanta orbs
RAMAN_COMBUST_ORB = {
    1: (12.0, 12.0),  # Moon
    2: (17.0, 17.0),  # Mars
    3: (14.0, 12.0),  # Mercury (direct, retro)
    4: (11.0, 11.0),  # Jupiter
    5: (10.0, 8.0),   # Venus (direct, retro)
    6: (15.0, 15.0),  # Saturn
}

# 2) Astrosoft PlanetaryInfo.java:132-153 orbs (now aligned with B.V. Raman)
ASTROSOFT_COMBUST_ORB = dict(RAMAN_COMBUST_ORB)

DEFAULT_PLACE = drik.Place("SyntheticVille", 13.0827, 80.2707, 5.5)


def set_mode(ayanamsa_name):
    if ayanamsa_name == "RAMAN":
        drik.set_ayanamsa_mode("RAMAN")
    else:
        drik.set_ayanamsa_mode("LAHIRI")
    # Match Astrosoft's standard apparent geocentric sidereal flags (AstroConsts.iflag):
    # SweConst.SEFLG_SWIEPH | SweConst.SEFLG_SPEED | SweConst.SEFLG_SIDEREAL
    drik.PLANET_FLAGS = swe.FLG_SWIEPH | swe.FLG_SIDEREAL | swe.FLG_SPEED


def hand_computed_navamsa_sign(d1_sign, lon_in_sign):
    """
    Independent hand-computed Parashari Navamsa sign formula:
    Fire (0,4,8) -> Mesha (0)
    Earth (1,5,9) -> Makara (9)
    Air (2,6,10) -> Thula (6)
    Water (3,7,11) -> Kataka (3)
    Pada index k = floor(lon_in_sign / (30/9)) in 0..8
    """
    if d1_sign in (0, 4, 8):
        seed = 0
    elif d1_sign in (1, 5, 9):
        seed = 9
    elif d1_sign in (2, 6, 10):
        seed = 6
    else:
        seed = 3
    pada = int(lon_in_sign // (30.0 / 9.0))
    if pada >= 9:
        pada = 8
    if pada < 0:
        pada = 0
    return (seed + pada) % 12


def angular_diff(lon1, lon2):
    d = abs(lon1 - lon2) % 360.0
    return 360.0 - d if d > 180.0 else d


def whole_sign_dignity(p_id, sign):
    if p_id not in range(7):
        return None
    if EXALTED_SIGN[p_id] == sign:
        return "Ex"
    if DEBILITATED_SIGN[p_id] == sign:
        return "Db"
    if MOOLATRIKONA_SIGN[p_id] == sign:
        return "Mt"
    if sign in OWN_SIGNS[p_id]:
        return "Own"
    return None


def format_suffix(name, retro, combust, dignity_code, vargottama):
    tokens = []
    if retro:
        tokens.append("R")
    if combust:
        tokens.append("C")
    if dignity_code:
        tokens.append(dignity_code)
    if vargottama:
        tokens.append("V")
    return f"{name} ({','.join(tokens)})" if tokens else name


def evaluate_instant(dt, place=DEFAULT_PLACE, ayanamsa="LAHARI"):
    """
    Evaluates a datetime using PyJHora and cross-checks against the independent hand formula.
    Note: dt must have second == 0 so Horoscope(name, d, m, y, hr, min, place) in Java matches.
    """
    set_mode(ayanamsa)
    dob = drik.Date(dt.year, dt.month, dt.day)
    tob = (dt.hour, dt.minute, dt.second)
    jd = utils.julian_day_number(dob, tob)

    rasi_pos = charts.rasi_chart(jd, place)
    d9_pos = charts.navamsa_chart(rasi_pos)
    retro_list = drik.planets_in_retrograde(jd, place)
    pyjhora_combust_buggy = charts.planets_in_combustion(rasi_pos, use_absolute_longitude=True)

    # Extract Lagna + 9 planets
    # rasi_pos format: [['L', [sign, lon]], [0, [sign, lon]], ..., [8, [sign, lon]]]
    pos_map = {}
    for key, (sign, lon) in rasi_pos:
        pos_map[key] = (int(sign), float(lon))

    d9_map = {}
    for key, (sign, lon) in d9_pos:
        d9_map[key] = int(sign)

    sun_sign, sun_lon_in_sign = pos_map[0]
    sun_abs = sun_sign * 30.0 + sun_lon_in_sign

    planets_data = {}
    navamsa_disagreements = []

    for p_id in range(9):
        p_name = PLANET_NAMES[p_id]
        sign, lon = pos_map[p_id]
        abs_lon = sign * 30.0 + lon
        d9_pyjhora = d9_map[p_id]
        d9_hand = hand_computed_navamsa_sign(sign, lon)
        if d9_pyjhora != d9_hand:
            navamsa_disagreements.append((p_name, sign, lon, d9_pyjhora, d9_hand))

        # Classical 7 only for R, C, dignity, V
        is_retro = (p_id in range(2, 7)) and (p_id in retro_list)
        sun_dist = angular_diff(abs_lon, sun_abs) if p_id != 0 else 0.0

        # Combustion under B.V. Raman vs Astrosoft current
        if p_id in RAMAN_COMBUST_ORB:
            orb_dir, orb_ret = RAMAN_COMBUST_ORB[p_id]
            orb_raman = orb_ret if is_retro else orb_dir
            combust_raman = sun_dist <= orb_raman
        else:
            combust_raman = False

        if p_id in ASTROSOFT_COMBUST_ORB:
            orb_dir_a, orb_ret_a = ASTROSOFT_COMBUST_ORB[p_id]
            orb_astro = orb_ret_a if is_retro else orb_dir_a
            combust_astro = (orb_astro > 0.0) and (sun_dist <= orb_astro)
        else:
            combust_astro = False

        dignity_code = whole_sign_dignity(p_id, sign)
        is_vargottama = (p_id in range(7)) and (sign == d9_hand)

        expected_raman = format_suffix(p_name, is_retro, combust_raman, dignity_code, is_vargottama)
        expected_astro_compact = expected_raman
        expected_astro_table = expected_raman

        planets_data[p_name] = {
            "sign": RASI_NAMES[sign],
            "sign_index": sign,
            "degree_in_sign": round(lon, 6),
            "abs_longitude": round(abs_lon, 6),
            "sun_distance_deg": round(sun_dist, 6),
            "navamsa_sign": RASI_NAMES[d9_pyjhora],
            "navamsa_sign_hand": RASI_NAMES[d9_hand],
            "R": is_retro,
            "C_raman": combust_raman,
            "C_astrosoft": combust_astro,
            "C_pyjhora_raw": (p_id in pyjhora_combust_buggy),
            "dignity": dignity_code if dignity_code else "NONE",
            "V": is_vargottama,
            "expected_suffix": expected_raman,
            "expected_suffix_astrosoft_compact": expected_astro_compact,
            "expected_suffix_astrosoft_table": expected_astro_table,
        }

    # Ascendant
    asc_sign, asc_lon = pos_map["L"]
    asc_d9_pyjhora = d9_map["L"]
    asc_d9_hand = hand_computed_navamsa_sign(asc_sign, asc_lon)
    if asc_d9_pyjhora != asc_d9_hand:
        navamsa_disagreements.append(("Ascendant", asc_sign, asc_lon, asc_d9_pyjhora, asc_d9_hand))

    planets_data["Ascendant"] = {
        "sign": RASI_NAMES[asc_sign],
        "sign_index": asc_sign,
        "degree_in_sign": round(asc_lon, 6),
        "abs_longitude": round(asc_sign * 30.0 + asc_lon, 6),
        "sun_distance_deg": round(angular_diff(asc_sign * 30.0 + asc_lon, sun_abs), 6),
        "navamsa_sign": RASI_NAMES[asc_d9_pyjhora],
        "navamsa_sign_hand": RASI_NAMES[asc_d9_hand],
        "R": False,
        "C_raman": False,
        "C_astrosoft": False,
        "C_pyjhora_raw": False,
        "dignity": "NONE",
        "V": False,
        "expected_suffix": "Ascendant",
        "expected_suffix_astrosoft_compact": "Ascendant",
        "expected_suffix_astrosoft_table": "Ascendant",
    }

    return planets_data, navamsa_disagreements


def fast_lon_speed(jd_utc, swe_planet):
    set_mode("LAHARI")
    res, _ = swe.calc_ut(jd_utc, swe_planet, drik.PLANET_FLAGS)
    return res[0] % 360.0, res[3]


def dt_to_jd_utc(dt, tz_hours=5.5):
    dob = drik.Date(dt.year, dt.month, dt.day)
    tob = (dt.hour, dt.minute, dt.second)
    jd_local = utils.julian_day_number(dob, tob)
    return jd_local - tz_hours / 24.0


def find_minute_crossing(start_dt, max_days, eval_fn, target_val, step_hours=6):
    """
    Finds a minute-resolution datetime where eval_fn(dt) crosses target_val.
    Returns (dt_before, dt_after) at 1-minute resolution.
    """
    cur = start_dt
    prev_dt = cur
    prev_v = eval_fn(cur) - target_val
    steps = int((max_days * 24) / step_hours)
    for _ in range(steps):
        nxt = prev_dt + timedelta(hours=step_hours)
        v = eval_fn(nxt) - target_val
        # Check for sign change (ignoring 360 wrap jumps > 90 deg)
        if prev_v * v <= 0 and abs(prev_v - v) < 45.0:
            # Binary search down to 1-minute resolution
            lo = prev_dt
            hi = nxt
            while (hi - lo).total_seconds() > 60:
                mid_sec = int((hi - lo).total_seconds() // 120) * 60
                if mid_sec == 0:
                    break
                mid = lo + timedelta(seconds=mid_sec)
                mv = eval_fn(mid) - target_val
                if prev_v * mv <= 0:
                    hi = mid
                else:
                    lo = mid
                    prev_v = mv
            return lo, hi
        prev_dt = nxt
        prev_v = v
    return None, None


def find_elongation_crossing(
    start_dt, max_days, swe_planet, target_orb, require_retro=None, step_hours=6
):
    """
    Finds a pair of integer-minute datetimes bracketing angular_diff(planet, Sun) == target_orb,
    with optional retrograde filter. Ensures one is inside [target_orb - 0.05, target_orb - 0.002]
    and the other is outside [target_orb + 0.002, target_orb + 0.05].
    """
    cur = start_dt
    steps = int((max_days * 24) / step_hours)

    def state(dt):
        jd_utc = dt_to_jd_utc(dt)
        p_lon, p_spd = fast_lon_speed(jd_utc, swe_planet)
        s_lon, _ = fast_lon_speed(jd_utc, swe.SUN)
        return angular_diff(p_lon, s_lon), (p_spd < 0)

    prev_dt = cur
    prev_dist, prev_ret = state(cur)
    for _ in range(steps):
        nxt = prev_dt + timedelta(hours=step_hours)
        dist, ret = state(nxt)
        if (prev_dist - target_orb) * (dist - target_orb) <= 0 and abs(prev_dist - dist) < 20.0:
            if require_retro is None or (ret == require_retro and prev_ret == require_retro):
                # Scan minute by minute between prev_dt and nxt via bisection
                lo, hi = prev_dt, nxt
                while (hi - lo).total_seconds() > 60:
                    mid = lo + timedelta(seconds=int((hi - lo).total_seconds() // 120) * 60)
                    mdist, _ = state(mid)
                    if (prev_dist - target_orb) * (mdist - target_orb) <= 0:
                        hi = mid
                    else:
                        lo = mid
                        prev_dist = mdist
                # Walk a few minutes around lo/hi to get clean ~1 arcmin (0.0167 deg) margin
                inside_dt = None
                outside_dt = None
                for offset_min in range(-60, 61):
                    cand = lo + timedelta(minutes=offset_min)
                    cdist, cret = state(cand)
                    if require_retro is not None and cret != require_retro:
                        continue
                    diff = cdist - target_orb
                    if -0.035 <= diff <= -0.010 and inside_dt is None:
                        inside_dt = cand
                    if 0.010 <= diff <= 0.035 and outside_dt is None:
                        outside_dt = cand
                if inside_dt and outside_dt:
                    return inside_dt, outside_dt
        prev_dt = nxt
        prev_dist, prev_ret = dist, ret
    return None, None


def find_planet_longitude_window(
    start_dt, max_days, swe_planet, target_sign, deg_min, deg_max,
    require_retro=None, require_not_combust_orb=None, step_hours=12
):
    cur = start_dt
    steps = int((max_days * 24) / step_hours)
    for _ in range(steps):
        jd_utc = dt_to_jd_utc(cur)
        p_lon, p_spd = fast_lon_speed(jd_utc, swe_planet)
        sign = int(p_lon // 30.0)
        deg = p_lon % 30.0
        if sign == target_sign and deg_min <= deg <= deg_max:
            if require_retro is None or ((p_spd < 0) == require_retro):
                if require_not_combust_orb is not None:
                    s_lon, _ = fast_lon_speed(jd_utc, swe.SUN)
                    if angular_diff(p_lon, s_lon) <= require_not_combust_orb:
                        cur += timedelta(hours=step_hours)
                        continue
                return cur
        cur += timedelta(hours=step_hours)
    return None


def build_all_cases():
    set_mode("LAHARI")
    cases = []
    all_disagreements = []
    case_counter = 1

    def add_case(target_state, focus_planet, dt, ayanamsa="LAHARI", description=""):
        nonlocal case_counter
        if dt is None:
            raise RuntimeError(f"Failed to find date/time for target_state={target_state}")
        pdata, disagreements = evaluate_instant(dt, DEFAULT_PLACE, ayanamsa)
        if disagreements:
            all_disagreements.extend(disagreements)
        cid = f"CASE_{case_counter:02d}"
        cname = f"SyntheticChart_{case_counter:02d}"
        case_counter += 1
        cases.append({
            "id": cid,
            "target_state": target_state,
            "focus_planet": focus_planet,
            "description": description,
            "name": cname,
            "date": {"year": dt.year, "month": dt.month, "day": dt.day},
            "time": {"hour": dt.hour, "minute": dt.minute, "second": dt.second},
            "timezone": DEFAULT_PLACE.timezone,
            "place_name": "SyntheticVille",
            "latitude": DEFAULT_PLACE.latitude,
            "longitude": DEFAULT_PLACE.longitude,
            "ayanamsa": ayanamsa,
            "planets": pdata,
        })

    base_dt = datetime(2024, 1, 1, 0, 0, 0)

    # 1. Sign boundary (Sun crossing Pisces 29:59 -> Aries 00:00)
    def sun_dist_to_aries(dt):
        lon, _ = fast_lon_speed(dt_to_jd_utc(dt), swe.SUN)
        return lon - 360.0 if lon > 180.0 else lon

    lo, hi = find_minute_crossing(datetime(2025, 4, 10, 0, 0, 0), 10, sun_dist_to_aries, 0.0, step_hours=2)
    # Walk back/forward 1 min to ensure safety against Moshier vs Swieph 1-arcsec jitter
    dt_sign_before = lo - timedelta(minutes=1)
    dt_sign_after = hi + timedelta(minutes=1)
    add_case("sign_boundary_before_29_59", "Sun", dt_sign_before, "LAHARI",
             "Sun at 29:59 Meena just before entering Mesha")
    add_case("sign_boundary_after_00_00", "Sun", dt_sign_after, "LAHARI",
             "Sun at 00:00 Mesha (Ex,V) just after crossing sign boundary")

    # 2. Navamsa boundaries: 3:20 (3.3333 deg) and 6:40 (6.6667 deg) in Aries for Sun
    lo320, hi320 = find_minute_crossing(datetime(2025, 4, 15, 0, 0, 0), 10, sun_dist_to_aries, 10.0 / 3.0, step_hours=2)
    add_case("navamsa_boundary_under_3_20", "Sun", lo320 - timedelta(minutes=1), "LAHARI",
             "Sun in Mesha just under 3:20 (1st Navamsa = Mesha => Ex,V)")
    add_case("navamsa_boundary_over_3_20", "Sun", hi320 + timedelta(minutes=1), "LAHARI",
             "Sun in Mesha just over 3:20 (2nd Navamsa = Vrishabha => Ex only)")

    lo640, hi640 = find_minute_crossing(datetime(2025, 4, 18, 0, 0, 0), 10, sun_dist_to_aries, 20.0 / 3.0, step_hours=2)
    add_case("navamsa_boundary_under_6_40", "Sun", lo640 - timedelta(minutes=1), "LAHARI",
             "Sun in Mesha just under 6:40 (2nd Navamsa = Vrishabha)")
    add_case("navamsa_boundary_over_6_40", "Sun", hi640 + timedelta(minutes=1), "LAHARI",
             "Sun in Mesha just over 6:40 (3rd Navamsa = Mithuna)")

    # 3. Vargottama in movable, fixed, and dual signs
    # Movable: Mars in Capricorn (9) 1.0..2.5 deg (non-combust) -> Ex,V
    dt_v_mov = find_planet_longitude_window(base_dt, 800, swe.MARS, 9, 1.0, 2.5, False, 20.0)
    add_case("vargottama_movable", "Mars", dt_v_mov, "LAHARI",
             "Mars in Makara (movable) 1st pada (0..3:20) => Ex,V")

    # Fixed: Sun in Leo (4) 14.0..16.0 deg -> Mt,V
    dt_v_fix = find_planet_longitude_window(base_dt, 400, swe.SUN, 4, 14.0, 16.0)
    add_case("vargottama_fixed", "Sun", dt_v_fix, "LAHARI",
             "Sun in Simha (fixed) 5th pada (13:20..16:40) => Mt,V")

    # Dual: Jupiter in Sagittarius (8) 27.0..29.0 deg -> Mt,V
    dt_v_dual = find_planet_longitude_window(datetime(2019, 1, 1, 0, 0, 0), 800, swe.JUPITER, 8, 27.0, 29.0, False, 20.0)
    add_case("vargottama_dual", "Jupiter", dt_v_dual, "LAHARI",
             "Jupiter in Dhanus (dual) 9th pada (26:40..30:00) => Mt,V")

    # 4. Moolatrikona degree edges (all 7 classical planets)
    # Sun Leo 20 deg
    dt_sun_mt1 = find_planet_longitude_window(base_dt, 400, swe.SUN, 4, 19.3, 19.8)
    dt_sun_mt2 = find_planet_longitude_window(base_dt, 400, swe.SUN, 4, 20.2, 20.8)
    add_case("mt_edge_sun_leo_under_20", "Sun", dt_sun_mt1, "LAHARI", "Sun in Simha < 20 deg")
    add_case("mt_edge_sun_leo_over_20", "Sun", dt_sun_mt2, "LAHARI", "Sun in Simha > 20 deg (whole-sign Mt)")

    # Moon Taurus <3, 3..4, >4 deg (non-combust)
    dt_mo_mt1 = find_planet_longitude_window(base_dt, 60, swe.MOON, 1, 2.2, 2.8, None, 20.0, step_hours=1)
    dt_mo_mt2 = find_planet_longitude_window(base_dt, 60, swe.MOON, 1, 3.2, 3.8, None, 20.0, step_hours=1)
    dt_mo_mt3 = find_planet_longitude_window(base_dt, 60, swe.MOON, 1, 4.2, 4.8, None, 20.0, step_hours=1)
    add_case("mt_edge_moon_taurus_under_3", "Moon", dt_mo_mt1, "LAHARI", "Moon in Vrishabha < 3 deg (Ex)")
    add_case("mt_edge_moon_taurus_3_to_4", "Moon", dt_mo_mt2, "LAHARI", "Moon in Vrishabha 3..4 deg (whole-sign Ex)")
    add_case("mt_edge_moon_taurus_over_4", "Moon", dt_mo_mt3, "LAHARI", "Moon in Vrishabha > 4 deg (whole-sign Ex)")

    # Mars Aries 12 deg
    dt_ma_mt1 = find_planet_longitude_window(base_dt, 700, swe.MARS, 0, 11.2, 11.8, False, 20.0)
    dt_ma_mt2 = find_planet_longitude_window(base_dt, 700, swe.MARS, 0, 12.2, 12.8, False, 20.0)
    add_case("mt_edge_mars_aries_under_12", "Mars", dt_ma_mt1, "LAHARI", "Mars in Mesha < 12 deg (Mt)")
    add_case("mt_edge_mars_aries_over_12", "Mars", dt_ma_mt2, "LAHARI", "Mars in Mesha > 12 deg (whole-sign Mt)")

    # Mercury Virgo <16, 16..20, >20 deg (non-combust, direct)
    dt_me_mt1 = find_planet_longitude_window(datetime(2020, 1, 1, 0, 0, 0), 2500, swe.MERCURY, 5, 15.1, 15.9, False, 14.2, step_hours=2)
    dt_me_mt2 = find_planet_longitude_window(datetime(2020, 1, 1, 0, 0, 0), 2500, swe.MERCURY, 5, 16.5, 19.5, False, 14.2, step_hours=2)
    dt_me_mt3 = find_planet_longitude_window(datetime(2020, 1, 1, 0, 0, 0), 2500, swe.MERCURY, 5, 20.5, 23.0, False, 14.2, step_hours=2)
    add_case("mt_edge_mercury_virgo_15_to_16", "Mercury", dt_me_mt1, "LAHARI", "Mercury in Kanya 15..16 deg (whole-sign Ex)")
    add_case("mt_edge_mercury_virgo_16_to_20", "Mercury", dt_me_mt2, "LAHARI", "Mercury in Kanya 16..20 deg (whole-sign Ex)")
    add_case("mt_edge_mercury_virgo_over_20", "Mercury", dt_me_mt3, "LAHARI", "Mercury in Kanya > 20 deg (whole-sign Ex)")

    # Jupiter Sagittarius 10 deg (non-combust)
    dt_ju_mt1 = find_planet_longitude_window(datetime(2005, 1, 1, 0, 0, 0), 7000, swe.JUPITER, 8, 8.0, 9.9, False, 12.0, step_hours=12)
    dt_ju_mt2 = find_planet_longitude_window(datetime(2005, 1, 1, 0, 0, 0), 7000, swe.JUPITER, 8, 10.1, 25.0, False, 12.0, step_hours=12)
    add_case("mt_edge_jupiter_sag_under_10", "Jupiter", dt_ju_mt1, "LAHARI", "Jupiter in Dhanus < 10 deg (Mt)")
    add_case("mt_edge_jupiter_sag_over_10", "Jupiter", dt_ju_mt2, "LAHARI", "Jupiter in Dhanus > 10 deg (whole-sign Mt)")

    # Venus Libra 15 deg
    dt_ve_mt1 = find_planet_longitude_window(datetime(2020, 1, 1, 0, 0, 0), 2000, swe.VENUS, 6, 14.1, 14.9, False, 10.5, step_hours=2)
    dt_ve_mt2 = find_planet_longitude_window(datetime(2020, 1, 1, 0, 0, 0), 2000, swe.VENUS, 6, 15.1, 15.9, False, 10.5, step_hours=2)
    add_case("mt_edge_venus_libra_under_15", "Venus", dt_ve_mt1, "LAHARI", "Venus in Thula < 15 deg (Mt)")
    add_case("mt_edge_venus_libra_over_15", "Venus", dt_ve_mt2, "LAHARI", "Venus in Thula > 15 deg (whole-sign Mt)")

    # Saturn Aquarius 20 deg
    dt_sa_mt1 = find_planet_longitude_window(datetime(2020, 1, 1, 0, 0, 0), 3000, swe.SATURN, 10, 19.1, 19.9, False, 16.0, step_hours=6)
    dt_sa_mt2 = find_planet_longitude_window(datetime(2020, 1, 1, 0, 0, 0), 3000, swe.SATURN, 10, 20.1, 20.9, False, 16.0, step_hours=6)
    add_case("mt_edge_saturn_aq_under_20", "Saturn", dt_sa_mt1, "LAHARI", "Saturn in Kumbha < 20 deg (Mt)")
    add_case("mt_edge_saturn_aq_over_20", "Saturn", dt_sa_mt2, "LAHARI", "Saturn in Kumbha > 20 deg (whole-sign Mt)")

    # 5. Combined suffixes: R+Ex, R+Db, C+Own, R+C+Db+V
    # R+Ex: Jupiter retrograde in Cancer (2014-2015)
    dt_r_ex = find_planet_longitude_window(datetime(2014, 12, 15, 0, 0, 0), 120, swe.JUPITER, 3, 5.0, 28.0, True, 20.0)
    add_case("combo_R_Ex", "Jupiter", dt_r_ex, "LAHARI", "Jupiter retrograde in Kataka => Jupiter (R,Ex)")

    # R+Db: Jupiter retrograde in Capricorn (2009) outside 1st pada (so R,Db without V)
    dt_r_db = find_planet_longitude_window(datetime(2009, 6, 1, 0, 0, 0), 150, swe.JUPITER, 9, 3.5, 29.5, True, 20.0)
    add_case("combo_R_Db", "Jupiter", dt_r_db, "LAHARI", "Jupiter retrograde in Makara => Jupiter (R,Db)")

    # C+Own: Jupiter combust in Pisces (March/April 2023)
    dt_c_own = None
    cur = datetime(2023, 3, 20, 0, 0, 0)
    for _ in range(60):
        jd_utc = dt_to_jd_utc(cur)
        j_lon, j_spd = fast_lon_speed(jd_utc, swe.JUPITER)
        s_lon, _ = fast_lon_speed(jd_utc, swe.SUN)
        if int(j_lon // 30.0) == 11 and j_spd > 0 and angular_diff(j_lon, s_lon) < 8.0 and (j_lon % 30.0) < 26.5:
            dt_c_own = cur
            break
        cur += timedelta(hours=12)
    add_case("combo_C_Own", "Jupiter", dt_c_own, "LAHARI", "Jupiter combust in Meena => Jupiter (C,Own)")

    # R+C+Db+V: Mercury retrograde + combust in Pisces 9th pada (26:40..30:00)
    dt_rcdbv = None
    cur = datetime(2000, 1, 1, 0, 0, 0)
    for _ in range(365 * 30 * 4):
        if cur.month in (3, 4):
            jd_utc = dt_to_jd_utc(cur)
            m_lon, m_spd = fast_lon_speed(jd_utc, swe.MERCURY)
            if int(m_lon // 30.0) == 11 and (m_lon % 30.0) >= 26.75 and m_spd < 0:
                s_lon, _ = fast_lon_speed(jd_utc, swe.SUN)
                if angular_diff(m_lon, s_lon) <= 11.5:
                    dt_rcdbv = cur
                    break
        cur += timedelta(hours=6)
    add_case("combo_R_C_Db_V", "Mercury", dt_rcdbv, "LAHARI",
             "Mercury retrograde, combust, debilitated in Meena 9th pada => Mercury (R,C,Db,V)")

    # 6. Moon Own in Cancer (non-combust, non-Vargottama)
    dt_mo_own = find_planet_longitude_window(base_dt, 60, swe.MOON, 3, 5.0, 12.0, None, 30.0, step_hours=2)
    add_case("moon_own_cancer", "Moon", dt_mo_own, "LAHARI", "Moon in Kataka => Moon (Own)")

    # 7. Rahu/Ketu & Ascendant in Vargottama padas and PyJHora exaltation signs (must have NO suffix)
    # Find Rahu in Taurus (1) 13:20..16:40 (so Ketu is in Scorpio 13:20..16:40, both fixed 5th pada = Vargottama)
    dt_rk = None
    cur = datetime(2020, 9, 1, 0, 0, 0)
    for _ in range(600):
        jd_utc = dt_to_jd_utc(cur)
        r_lon, _ = fast_lon_speed(jd_utc, swe.TRUE_NODE)
        if int(r_lon // 30.0) == 1 and 13.5 <= (r_lon % 30.0) <= 16.5:
            # Find minute of this day where Ascendant is in Aries 0..3:20 (Vargottama)
            for m in range(0, 1440, 2):
                cand = cur + timedelta(minutes=m)
                pdata, _ = evaluate_instant(cand, DEFAULT_PLACE, "LAHARI")
                if pdata["Ascendant"]["sign_index"] == 0 and pdata["Ascendant"]["degree_in_sign"] < 3.2:
                    dt_rk = cand
                    break
            if dt_rk:
                break
        cur += timedelta(days=1)
    add_case("rahu_ketu_asc_no_suffix", "Rahu", dt_rk, "LAHARI",
             "Rahu in Vrishabha 5th pada, Ketu in Vrichika 5th pada, Ascendant in Mesha 1st pada => all plain")

    # 8. Combustion limits +/- 1 minute of arc per planet
    combust_specs = [
        ("combust_moon_12", "Moon", swe.MOON, 12.0, None, 1),
        ("combust_mars_17", "Mars", swe.MARS, 17.0, False, 6),
        ("combust_mercury_direct_14", "Mercury", swe.MERCURY, 14.0, False, 2),
        ("combust_mercury_retro_12", "Mercury", swe.MERCURY, 12.0, True, 2),
        ("combust_jupiter_11", "Jupiter", swe.JUPITER, 11.0, False, 6),
        ("combust_venus_direct_10", "Venus", swe.VENUS, 10.0, False, 2),
        ("combust_venus_retro_8", "Venus", swe.VENUS, 8.0, True, 2),
        ("combust_saturn_raman_15", "Saturn", swe.SATURN, 15.0, False, 6),
        ("combust_saturn_astrosoft_5", "Saturn", swe.SATURN, 5.0, False, 6),
    ]
    for prefix, pname, swe_p, orb, req_ret, step_h in combust_specs:
        in_dt, out_dt = find_elongation_crossing(base_dt, 1200, swe_p, orb, req_ret, step_hours=step_h)
        add_case(f"{prefix}_inside", pname, in_dt, "LAHARI",
                 f"{pname} just inside {orb} deg combustion limit")
        add_case(f"{prefix}_outside", pname, out_dt, "LAHARI",
                 f"{pname} just outside {orb} deg combustion limit")

    # 9. Retrograde + non-combust in the window between retro orb and direct orb
    # Mercury retro at 12.5..13.5 deg from Sun (combust if direct, NOT combust because retro orb=12)
    dt_me_ret_gap = None
    cur = base_dt
    for _ in range(365 * 8):
        jd_utc = dt_to_jd_utc(cur)
        m_lon, m_spd = fast_lon_speed(jd_utc, swe.MERCURY)
        if m_spd < 0:
            s_lon, _ = fast_lon_speed(jd_utc, swe.SUN)
            dist = angular_diff(m_lon, s_lon)
            if 12.3 <= dist <= 13.7:
                dt_me_ret_gap = cur
                break
        cur += timedelta(hours=3)
    add_case("retro_mercury_between_12_and_14", "Mercury", dt_me_ret_gap, "LAHARI",
             "Mercury retrograde at 12..14 deg from Sun => R only (not C)")

    # Venus retro at 8.3..9.7 deg from Sun (combust if direct, NOT combust because retro orb=8)
    dt_ve_ret_gap = None
    cur = base_dt
    for _ in range(365 * 4 * 8):
        jd_utc = dt_to_jd_utc(cur)
        v_lon, v_spd = fast_lon_speed(jd_utc, swe.VENUS)
        if v_spd < 0:
            s_lon, _ = fast_lon_speed(jd_utc, swe.SUN)
            dist = angular_diff(v_lon, s_lon)
            if 8.3 <= dist <= 9.7:
                dt_ve_ret_gap = cur
                break
        cur += timedelta(hours=3)
    add_case("retro_venus_between_8_and_10", "Venus", dt_ve_ret_gap, "LAHARI",
             "Venus retrograde at 8..10 deg from Sun => R only (not C)")

    # 10. Chart with NO statuses (all 7 classical planets plain)
    dt_plain = None
    cur = datetime(2000, 1, 1, 0, 0, 0)
    for _ in range(365 * 30 * 4):
        jd_utc = dt_to_jd_utc(cur)
        ok = True
        s_lon, _ = fast_lon_speed(jd_utc, swe.SUN)
        for p_id, swe_p in enumerate([swe.SUN, swe.MOON, swe.MARS, swe.MERCURY, swe.JUPITER, swe.VENUS, swe.SATURN]):
            lon, spd = fast_lon_speed(jd_utc, swe_p)
            sign = int(lon // 30.0)
            deg = lon % 30.0
            if p_id >= 2 and spd < 0:
                ok = False
                break
            if whole_sign_dignity(p_id, sign) is not None:
                ok = False
                break
            if hand_computed_navamsa_sign(sign, deg) == sign:
                ok = False
                break
            if p_id >= 1:
                orb_d, _ = RAMAN_COMBUST_ORB[p_id]
                if angular_diff(lon, s_lon) <= orb_d + 0.3:
                    ok = False
                    break
        if ok:
            dt_plain = cur
            break
        cur += timedelta(hours=6)
    add_case("all_planets_plain_no_status", "Sun", dt_plain, "LAHARI",
             "All 7 classical planets direct, non-combust, neutral dignity, non-Vargottama")

    # 11. Ayanamsa Lahiri vs Raman near a sign edge
    # When Sun is at ~29:15 Pisces in Lahiri (~24.2 deg ayanamsa), in Raman (~22.77 deg ayanamsa)
    # Sun is at ~00:38 Aries (Ex,V)!
    dt_aya = find_planet_longitude_window(datetime(2025, 4, 12, 0, 0, 0), 5, swe.SUN, 11, 29.1, 29.5, step_hours=1)
    add_case("ayanamsa_edge_lahiri", "Sun", dt_aya, "LAHARI",
             "Sun at ~29.2 deg Meena under Lahiri => plain Sun")
    add_case("ayanamsa_edge_raman", "Sun", dt_aya, "RAMAN",
             "Same instant under Raman ayanamsa => Sun in Mesha 1st pada => Sun (Ex,V)")

    return cases, all_disagreements


def main():
    cases, disagreements = build_all_cases()
    out_dir = "/Users/mukunds100/Dev/Astrosoft-intellij/src/app/astrosoft/ui/test/resources"
    os.makedirs(out_dir, exist_ok=True)
    out_path = os.path.join(out_dir, "planet_status_cases.json")

    payload = {
        "generator": "tools/gen_status_cases.py",
        "total_cases": len(cases),
        "navamsa_crosscheck_disagreements": len(disagreements),
        "cases": cases,
    }
    with open(out_path, "w", encoding="utf-8") as f:
        json.dump(payload, f, indent=2)

    print(f"Generated {len(cases)} cases -> {out_path}")
    print(f"Independent Navamsa cross-check disagreements vs PyJHora: {len(disagreements)}")
    missing = [c["target_state"] for c in cases if c["date"]["year"] is None]
    print(f"Targets not found: {len(missing)}")

    # Print summary table of cases
    for c in cases:
        fp = c["focus_planet"]
        pinfo = c["planets"][fp]
        d = c["date"]
        t = c["time"]
        print(
            f"  {c['id']} | {c['target_state']:36s} | {d['year']:04d}-{d['month']:02d}-{d['day']:02d} "
            f"{t['hour']:02d}:{t['minute']:02d} ({c['ayanamsa']:6s}) | "
            f"{fp:8s} {pinfo['sign']:9s} {pinfo['degree_in_sign']:6.3f}° D9={pinfo['navamsa_sign']:9s} "
            f"ΔSun={pinfo['sun_distance_deg']:6.2f}° | Raman='{pinfo['expected_suffix']}' "
            f"| AstroCompact='{pinfo['expected_suffix_astrosoft_compact']}' "
            f"| AstroTable='{pinfo['expected_suffix_astrosoft_table']}'"
        )


if __name__ == "__main__":
    main()
