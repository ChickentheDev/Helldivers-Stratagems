package dev.chickenthedev.stratagems;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.chickenthedev.stratagems.Direction.DOWN;
import static dev.chickenthedev.stratagems.Direction.LEFT;
import static dev.chickenthedev.stratagems.Direction.RIGHT;
import static dev.chickenthedev.stratagems.Direction.UP;

// Every stratagem in Helldivers 2, with its real in-game arrow code,
// base cooldown, and call-in time.
public class StratagemRegistry {

    private static final Map<String, Stratagem> STRATAGEMS = new LinkedHashMap<>();

    static {
        // ---- Orbital Strikes ----
        register(new Stratagem("orbital_precision_strike", "Orbital Precision Strike", "Offensive",
                80, 3, RIGHT, RIGHT, UP));
        register(new Stratagem("orbital_gatling_barrage", "Orbital Gatling Barrage", "Offensive",
                70, 2, RIGHT, DOWN, LEFT, UP, UP));
        register(new Stratagem("orbital_gas_strike", "Orbital Gas Strike", "Offensive",
                75, 3, RIGHT, RIGHT, DOWN, RIGHT));
        register(new Stratagem("orbital_120mm", "Orbital 120MM HE Barrage", "Offensive",
                180, 6, RIGHT, RIGHT, DOWN, LEFT, RIGHT, DOWN));
        register(new Stratagem("orbital_airburst", "Orbital Airburst Strike", "Offensive",
                100, 3, RIGHT, RIGHT, RIGHT));
        register(new Stratagem("orbital_smoke", "Orbital Smoke Strike", "Offensive",
                75, 3, RIGHT, RIGHT, DOWN, UP));
        register(new Stratagem("orbital_ems", "Orbital EMS Strike", "Offensive",
                75, 3, RIGHT, RIGHT, LEFT, DOWN));
        register(new Stratagem("orbital_380mm", "Orbital 380MM HE Barrage", "Offensive",
                240, 7, RIGHT, DOWN, UP, UP, LEFT, DOWN, DOWN));
        register(new Stratagem("orbital_walking", "Orbital Walking Barrage", "Offensive",
                240, 4, RIGHT, DOWN, RIGHT, DOWN, RIGHT, DOWN));
        register(new Stratagem("orbital_laser", "Orbital Laser", "Offensive",
                300, 1, RIGHT, DOWN, UP, RIGHT, DOWN));
        register(new Stratagem("orbital_napalm", "Orbital Napalm Barrage", "Offensive",
                240, 6, RIGHT, RIGHT, DOWN, LEFT, RIGHT, UP));
        register(new Stratagem("orbital_railcannon", "Orbital Railcannon Strike", "Offensive",
                180, 1, RIGHT, UP, DOWN, DOWN, RIGHT));

        // ---- Eagle Strikes ----
        register(new Stratagem("eagle_strafing", "Eagle Strafing Run", "Offensive",
                15, 2, UP, RIGHT, RIGHT));
        register(new Stratagem("eagle_airstrike", "Eagle Airstrike", "Offensive",
                15, 4, UP, RIGHT, DOWN, RIGHT));
        register(new Stratagem("eagle_cluster", "Eagle Cluster Bomb", "Offensive",
                15, 4, UP, RIGHT, DOWN, DOWN, RIGHT));
        register(new Stratagem("eagle_smoke", "Eagle Smoke Strike", "Offensive",
                15, 4, UP, RIGHT, UP, DOWN));
        register(new Stratagem("eagle_napalm", "Eagle Napalm Airstrike", "Offensive",
                15, 4, UP, RIGHT, DOWN, UP));
        register(new Stratagem("eagle_rockets", "Eagle 110MM Rocket Pods", "Offensive",
                15, 3, UP, RIGHT, UP, LEFT));
        register(new Stratagem("eagle_500kg", "Eagle 500KG Bomb", "Offensive",
                15, 3, UP, RIGHT, DOWN, DOWN, DOWN));
        register(new Stratagem("eagle_gas", "Eagle Gas Airstrike", "Offensive",
                15, 3, UP, RIGHT, LEFT, RIGHT));

        // ---- Support Weapons ----
        register(new Stratagem("w_mg43", "MG-43 Machine Gun", "Supply",
                480, 5, DOWN, LEFT, DOWN, UP, RIGHT));
        register(new Stratagem("w_eat17", "EAT-17 Expendable Anti-Tank", "Supply",
                70, 5, DOWN, DOWN, LEFT, UP, RIGHT));
        register(new Stratagem("w_stalwart", "M-105 Stalwart", "Supply",
                480, 5, DOWN, LEFT, DOWN, UP, UP, LEFT));
        register(new Stratagem("w_las98", "LAS-98 Laser Cannon", "Supply",
                480, 5, DOWN, LEFT, DOWN, UP, LEFT));
        register(new Stratagem("w_apw1", "APW-1 Anti-Materiel Rifle", "Supply",
                480, 5, DOWN, LEFT, RIGHT, UP, DOWN));
        register(new Stratagem("w_gr8", "GR-8 Recoilless Rifle", "Supply",
                480, 5, DOWN, LEFT, RIGHT, RIGHT, LEFT));
        register(new Stratagem("w_gl21", "GL-21 Grenade Launcher", "Supply",
                480, 5, DOWN, LEFT, UP, LEFT, DOWN));
        register(new Stratagem("w_flam40", "FLAM-40 Flamethrower", "Supply",
                480, 5, DOWN, LEFT, UP, DOWN, UP));
        register(new Stratagem("w_mg206", "MG-206 Heavy Machine Gun", "Supply",
                480, 5, DOWN, LEFT, UP, DOWN, DOWN));
        register(new Stratagem("w_ac8", "AC-8 Autocannon", "Supply",
                480, 5, DOWN, LEFT, DOWN, UP, UP, RIGHT));
        register(new Stratagem("w_arc3", "ARC-3 Arc Thrower", "Supply",
                480, 5, DOWN, RIGHT, DOWN, UP, LEFT, LEFT));
        register(new Stratagem("w_las99", "LAS-99 Quasar Cannon", "Supply",
                480, 5, DOWN, DOWN, UP, LEFT, RIGHT));
        register(new Stratagem("w_rl77", "RL-77 Airburst Rocket Launcher", "Supply",
                480, 5, DOWN, UP, UP, LEFT, RIGHT));
        register(new Stratagem("w_mls4x", "MLS-4X Commando", "Supply",
                120, 5, DOWN, LEFT, UP, DOWN, RIGHT));
        register(new Stratagem("w_faf14", "FAF-14 Spear", "Supply",
                480, 5, DOWN, DOWN, UP, DOWN, DOWN));
        register(new Stratagem("w_rs422", "RS-422 Railgun", "Supply",
                480, 5, DOWN, RIGHT, DOWN, UP, LEFT, RIGHT));
        register(new Stratagem("w_wasp", "StA-X3 W.A.S.P. Launcher", "Supply",
                480, 8, DOWN, DOWN, UP, DOWN, RIGHT));
        register(new Stratagem("w_cqc20", "CQC-20 Breaching Hammer", "Supply",
                480, 5, DOWN, LEFT, RIGHT, LEFT, UP));
        register(new Stratagem("w_plas45", "PLAS-45 Epoch", "Supply",
                480, 5, DOWN, LEFT, UP, LEFT, RIGHT));
        register(new Stratagem("w_mgx42", "MGX-42 Bullet Storm", "Supply",
                70, 5, DOWN, LEFT, DOWN, RIGHT, UP, LEFT));
        register(new Stratagem("w_s11", "S-11 Speargun", "Supply",
                480, 5, DOWN, RIGHT, DOWN, LEFT, UP, RIGHT));
        register(new Stratagem("w_cqc9", "CQC-9 Defoliation Tool", "Supply",
                480, 5, DOWN, LEFT, RIGHT, RIGHT, DOWN));
        register(new Stratagem("w_gl52", "GL-52 De-Escalator", "Supply",
                480, 5, DOWN, RIGHT, UP, LEFT, RIGHT));
        register(new Stratagem("w_eat700", "EAT-700 Expendable Napalm", "Supply",
                140, 2, DOWN, DOWN, LEFT, UP, LEFT));
        register(new Stratagem("w_tx41", "TX-41 Sterilizer", "Supply",
                480, 5, DOWN, LEFT, UP, DOWN, LEFT));
        register(new Stratagem("w_eat411", "EAT-411 Leveller", "Supply",
                140, 2, DOWN, DOWN, LEFT, UP, DOWN));
        register(new Stratagem("w_gl28", "GL-28 Belt-Fed Grenade Launcher", "Supply",
                480, 5, DOWN, LEFT, UP, LEFT, UP, UP));
        register(new Stratagem("w_c4", "B/MD C4 Pack", "Supply",
                480, 5, DOWN, RIGHT, UP, UP, RIGHT, UP));
        register(new Stratagem("w_ms11", "MS-11 Solo Silo", "Supply",
                180, 5, DOWN, UP, RIGHT, DOWN, DOWN));
        register(new Stratagem("w_cremator", "B/FLAM-80 Cremator", "Supply",
                480, 5, DOWN, DOWN, RIGHT, DOWN, UP, UP));
        register(new Stratagem("w_m1000", "M-1000 Maxigun", "Supply",
                480, 5, DOWN, LEFT, RIGHT, DOWN, UP, UP));
        register(new Stratagem("w_flag", "CQC-1 One True Flag", "Supply",
                480, 5, DOWN, LEFT, RIGHT, RIGHT, UP));
        register(new Stratagem("w_meltagun", "40-K Meltagun", "Supply",
                480, 5, DOWN, LEFT, UP, LEFT, LEFT, DOWN));

        // ---- Backpacks ----
        register(new Stratagem("p_b1", "B-1 Supply Pack", "Supply",
                480, 10, DOWN, LEFT, DOWN, UP, UP, DOWN));
        register(new Stratagem("p_lift850", "LIFT-850 Jump Pack", "Supply",
                480, 10, DOWN, UP, UP, DOWN, UP));
        register(new Stratagem("p_sh20", "SH-20 Ballistic Shield Backpack", "Supply",
                240, 10, DOWN, LEFT, DOWN, DOWN, UP, LEFT));
        register(new Stratagem("p_ar23", "AX/AR-23 Guard Dog", "Supply",
                480, 10, DOWN, UP, LEFT, UP, RIGHT, DOWN));
        register(new Stratagem("p_las5", "AX/LAS-5 Guard Dog Rover", "Supply",
                480, 10, DOWN, UP, LEFT, UP, RIGHT, RIGHT));
        register(new Stratagem("p_sh32", "SH-32 Shield Generator Pack", "Supply",
                480, 10, DOWN, UP, LEFT, RIGHT, LEFT, RIGHT));
        register(new Stratagem("p_sh51", "SH-51 Directional Shield", "Supply",
                300, 10, DOWN, UP, LEFT, RIGHT, UP, UP));
        register(new Stratagem("p_flam75", "AX/FLAM-75 Hot Dog", "Supply",
                480, 10, DOWN, UP, LEFT, UP, LEFT, LEFT));
        register(new Stratagem("p_b100", "B-100 Portable Hellbomb", "Supply",
                300, 10, DOWN, RIGHT, UP, UP, UP));
        register(new Stratagem("p_k9", "AX/ARC-3 K-9", "Supply",
                480, 10, DOWN, UP, LEFT, UP, RIGHT, LEFT));
        register(new Stratagem("p_lift860", "LIFT-860 Hover Pack", "Supply",
                480, 10, DOWN, UP, UP, DOWN, LEFT, RIGHT));
        register(new Stratagem("p_tx13", "AX/TX-13 Dog Breath", "Supply",
                480, 10, DOWN, UP, LEFT, UP, RIGHT, UP));
        register(new Stratagem("p_lift182", "LIFT-182 Warp Pack", "Supply",
                480, 10, DOWN, LEFT, RIGHT, DOWN, LEFT, RIGHT));

        // ---- Vehicles ----
        register(new Stratagem("v_exo45", "EXO-45 Patriot Exosuit", "Supply",
                420, 10, LEFT, DOWN, RIGHT, UP, LEFT, DOWN, DOWN));
        register(new Stratagem("v_exo49", "EXO-49 Emancipator Exosuit", "Supply",
                420, 10, LEFT, DOWN, RIGHT, UP, LEFT, DOWN, UP));
        register(new Stratagem("v_exo51", "EXO-51 Lumberer Exosuit", "Supply",
                420, 10, LEFT, DOWN, RIGHT, UP, RIGHT, LEFT, UP));
        register(new Stratagem("v_exo55", "EXO-55 Breakthrough Exosuit", "Supply",
                420, 10, LEFT, DOWN, RIGHT, LEFT, RIGHT, DOWN, UP));
        register(new Stratagem("v_m102", "M-102 Gunner FRV", "Supply",
                480, 10, LEFT, DOWN, RIGHT, DOWN, RIGHT, DOWN, UP));
        register(new Stratagem("v_m103", "M-103 Supply FRV", "Supply",
                480, 10, LEFT, DOWN, LEFT, LEFT, DOWN, UP, RIGHT));
        register(new Stratagem("v_m104", "M-104 Incinerator FRV", "Supply",
                480, 10, LEFT, DOWN, RIGHT, LEFT, DOWN, UP, UP));
        register(new Stratagem("v_td110", "TD-110 Maelstrom", "Supply",
                780, 10, LEFT, DOWN, RIGHT, DOWN, LEFT, DOWN, UP, LEFT, RIGHT));
        register(new Stratagem("v_td220", "TD-220 Bastion", "Supply",
                780, 10, LEFT, DOWN, RIGHT, DOWN, LEFT, DOWN, UP, DOWN, UP));

        // ---- Sentries ----
        register(new Stratagem("s_mg", "A/MG-43 Machine Gun Sentry", "Defensive",
                90, 5, DOWN, UP, RIGHT, RIGHT, UP));
        register(new Stratagem("s_gatling", "A/G-16 Gatling Sentry", "Defensive",
                150, 5, DOWN, UP, RIGHT, LEFT));
        register(new Stratagem("s_autocannon", "A/AC-8 Autocannon Sentry", "Defensive",
                150, 5, DOWN, UP, RIGHT, UP, LEFT, UP));
        register(new Stratagem("s_mortar", "A/M-12 Mortar Sentry", "Defensive",
                180, 5, DOWN, UP, RIGHT, RIGHT, DOWN));
        register(new Stratagem("s_rocket", "A/MLS-4X Rocket Sentry", "Defensive",
                150, 0, DOWN, UP, RIGHT, RIGHT, LEFT));
        register(new Stratagem("s_tesla", "A/ARC-3 Tesla Tower", "Defensive",
                120, 5, DOWN, UP, RIGHT, UP, LEFT, RIGHT));
        register(new Stratagem("s_ems", "A/M-23 EMS Mortar Sentry", "Defensive",
                180, 5, DOWN, UP, RIGHT, DOWN, RIGHT));
        register(new Stratagem("s_laser", "A/LAS-98 Laser Sentry", "Defensive",
                150, 5, DOWN, UP, RIGHT, DOWN, UP, RIGHT));
        register(new Stratagem("s_flame", "E/FLAM-40 Flame Sentry", "Defensive",
                100, 5, DOWN, UP, RIGHT, DOWN, UP, UP));
        register(new Stratagem("s_gas", "A/GM-17 Gas Mortar Sentry", "Defensive",
                180, 5, DOWN, UP, RIGHT, DOWN, LEFT));

        // ---- Emplacements ----
        register(new Stratagem("e_mines_ap", "MD-6 Anti-Personnel Minefield", "Defensive",
                120, 3, DOWN, LEFT, UP, RIGHT));
        register(new Stratagem("e_mines_fire", "MD-I4 Incendiary Mines", "Defensive",
                120, 3, DOWN, LEFT, LEFT, DOWN));
        register(new Stratagem("e_mines_at", "MD-17 Anti-Tank Mines", "Defensive",
                120, 3, DOWN, LEFT, UP, UP));
        register(new Stratagem("e_shield", "FX-12 Shield Generator Relay", "Defensive",
                90, 0, DOWN, DOWN, LEFT, RIGHT, LEFT, RIGHT));
        register(new Stratagem("e_hmg", "E/MG-101 HMG Emplacement", "Defensive",
                180, 3, DOWN, UP, LEFT, RIGHT, RIGHT, LEFT));
        register(new Stratagem("e_grenadier", "E/GL-21 Grenadier Battlement", "Defensive",
                120, 3, DOWN, RIGHT, DOWN, LEFT, RIGHT));
        register(new Stratagem("e_mines_gas", "MD-8 Gas Mines", "Defensive",
                120, 3, DOWN, LEFT, LEFT, RIGHT));
        register(new Stratagem("e_at", "E/AT-12 Anti-Tank Emplacement", "Defensive",
                180, 3, DOWN, UP, LEFT, RIGHT, RIGHT, RIGHT));

        // ---- Mission ----
        register(new Stratagem("m_reinforce", "Reinforce", "Mission",
                120, 5, UP, DOWN, RIGHT, LEFT, UP));
        register(new Stratagem("m_resupply", "Resupply", "Mission",
                180, 12, DOWN, DOWN, UP, RIGHT));
        register(new Stratagem("m_sos", "SOS Beacon", "Mission",
                120, 5, UP, DOWN, RIGHT, UP));
        register(new Stratagem("m_hellbomb", "NUX-223 Hellbomb", "Mission",
                30, 6, DOWN, UP, LEFT, DOWN, UP, RIGHT, DOWN, UP));
        register(new Stratagem("m_rearm", "Eagle Rearm", "Mission",
                120, 0, UP, UP, LEFT, UP, RIGHT));
        register(new Stratagem("m_seaf", "SEAF Artillery", "Mission",
                180, 5, RIGHT, UP, UP, DOWN));
        register(new Stratagem("m_flag", "Super Earth Flag", "Mission",
                300, 5, DOWN, UP, DOWN, UP));
    }

    private static void register(Stratagem stratagem) {
        STRATAGEMS.put(stratagem.id, stratagem);
    }

    public static Stratagem get(String id) {
        return STRATAGEMS.get(id);
    }

    public static Collection<Stratagem> all() {
        return STRATAGEMS.values();
    }

    public static Stratagem findFullMatch(List<Direction> input) {
        for (Stratagem s : STRATAGEMS.values()) {
            if (s.matches(input)) return s;
        }
        return null;
    }

    public static boolean hasAnyPrefix(List<Direction> input) {
        for (Stratagem s : STRATAGEMS.values()) {
            if (s.hasPrefix(input)) return true;
        }
        return false;
    }
}
