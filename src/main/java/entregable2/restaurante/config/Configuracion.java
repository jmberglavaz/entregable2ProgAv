package main.java.entregable2.restaurante.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Configuración inmutable de la simulación.
 * Se carga una sola vez (holder idiom) y es segura para lectura concurrente.
 */
public final class Configuracion {

    private static final String ARCHIVO = "/config.properties";

    private final int t;
    private final int m, p, z, c, y;
    private final long tpMin, tpMax;
    private final long tmMin, tmMax;
    private final long tqMin, tqMax;
    private final long tzMin, tzMax;
    private final long trMin, trMax;
    private final long tlMin, tlMax;
    private final long tcMin, tcMax;
    private final long tyMin, tyMax;

    private Configuracion(Properties props) {
        this.t = leerEntero(props, Constantes.KEY_T, Constantes.DEF_T);
        this.m = leerEntero(props, Constantes.KEY_M, Constantes.DEF_M);
        this.p = leerEntero(props, Constantes.KEY_P, Constantes.DEF_P);
        this.z = leerEntero(props, Constantes.KEY_Z, Constantes.DEF_Z);
        this.c = leerEntero(props, Constantes.KEY_C, Constantes.DEF_C);
        this.y = leerEntero(props, Constantes.KEY_Y, Constantes.DEF_Y);

        this.tpMin = leerLong(props, Constantes.KEY_TP_MIN, Constantes.DEF_TP_MIN);
        this.tpMax = leerLong(props, Constantes.KEY_TP_MAX, Constantes.DEF_TP_MAX);
        this.tmMin = leerLong(props, Constantes.KEY_TM_MIN, Constantes.DEF_TM_MIN);
        this.tmMax = leerLong(props, Constantes.KEY_TM_MAX, Constantes.DEF_TM_MAX);
        this.tqMin = leerLong(props, Constantes.KEY_TQ_MIN, Constantes.DEF_TQ_MIN);
        this.tqMax = leerLong(props, Constantes.KEY_TQ_MAX, Constantes.DEF_TQ_MAX);
        this.tzMin = leerLong(props, Constantes.KEY_TZ_MIN, Constantes.DEF_TZ_MIN);
        this.tzMax = leerLong(props, Constantes.KEY_TZ_MAX, Constantes.DEF_TZ_MAX);
        this.trMin = leerLong(props, Constantes.KEY_TR_MIN, Constantes.DEF_TR_MIN);
        this.trMax = leerLong(props, Constantes.KEY_TR_MAX, Constantes.DEF_TR_MAX);
        this.tlMin = leerLong(props, Constantes.KEY_TL_MIN, Constantes.DEF_TL_MIN);
        this.tlMax = leerLong(props, Constantes.KEY_TL_MAX, Constantes.DEF_TL_MAX);
        this.tcMin = leerLong(props, Constantes.KEY_TC_MIN, Constantes.DEF_TC_MIN);
        this.tcMax = leerLong(props, Constantes.KEY_TC_MAX, Constantes.DEF_TC_MAX);
        this.tyMin = leerLong(props, Constantes.KEY_TY_MIN, Constantes.DEF_TY_MIN);
        this.tyMax = leerLong(props, Constantes.KEY_TY_MAX, Constantes.DEF_TY_MAX);

        validar();
    }

    // ---- Holder idiom: lazy + thread-safe sin sincronización ----
    private static final class Holder {
        private static final Configuracion INSTANCIA = cargar();
    }

    public static Configuracion get() {
        return Holder.INSTANCIA;
    }

    /** Útil para tests: recarga desde un Properties en memoria. */
    public static Configuracion desde(Properties props) {
        return new Configuracion(props);
    }

    private static Configuracion cargar() {
        Properties props = new Properties();
        try (InputStream in = Configuracion.class.getResourceAsStream(ARCHIVO)) {
            if (in == null) {
                System.err.println("[Config] No se encontró " + ARCHIVO
                        + ". Se usan valores por defecto.");
            } else {
                props.load(in);
            }
        } catch (IOException e) {
            System.err.println("[Config] Error leyendo " + ARCHIVO
                    + ": " + e.getMessage() + ". Se usan defaults.");
        }
        return new Configuracion(props);
    }

    private static int leerEntero(Properties p, String key, int def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            System.err.println("[Config] Valor inválido para " + key
                    + "='" + v + "'. Usando default " + def);
            return def;
        }
    }

    private static long leerLong(Properties p, String key, long def) {
        String v = p.getProperty(key);
        if (v == null) return def;
        try {
            return Long.parseLong(v.trim());
        } catch (NumberFormatException e) {
            System.err.println("[Config] Valor inválido para " + key
                    + "='" + v + "'. Usando default " + def);
            return def;
        }
    }

    private void validar() {
        exigirPositivo("T", t);
        exigirPositivo("M", m);
        exigirPositivo("P", p);
        exigirPositivo("Z", z);
        exigirPositivo("C", c);
        exigirPositivo("Y", y);
        exigirRango("TP", tpMin, tpMax);
        exigirRango("TM", tmMin, tmMax);
        exigirRango("TQ", tqMin, tqMax);
        exigirRango("TZ", tzMin, tzMax);
        exigirRango("TR", trMin, trMax);
        exigirRango("TL", tlMin, tlMax);
        exigirRango("TC", tcMin, tcMax);
        exigirRango("TY", tyMin, tyMax);
    }

    private static void exigirPositivo(String nombre, int valor) {
        if (valor <= 0) {
            throw new IllegalStateException(
                    "[Config] " + nombre + " debe ser > 0 (valor=" + valor + ")");
        }
    }

    private static void exigirRango(String prefijo, long min, long max) {
        if (min < 0 || max < 0 || min > max) {
            throw new IllegalStateException(
                    "[Config] Rango inválido " + prefijo + "min=" + min
                            + " / " + prefijo + "max=" + max);
        }
    }

    // ---- Getters ----
    public int getT() { return t; }
    public int getM() { return m; }
    public int getP() { return p; }
    public int getZ() { return z; }
    public int getC() { return c; }
    public int getY() { return y; }

    public long getTpMin() { return tpMin; }
    public long getTpMax() { return tpMax; }
    public long getTmMin() { return tmMin; }
    public long getTmMax() { return tmMax; }
    public long getTqMin() { return tqMin; }
    public long getTqMax() { return tqMax; }
    public long getTzMin() { return tzMin; }
    public long getTzMax() { return tzMax; }
    public long getTrMin() { return trMin; }
    public long getTrMax() { return trMax; }
    public long getTlMin() { return tlMin; }
    public long getTlMax() { return tlMax; }
    public long getTcMin() { return tcMin; }
    public long getTcMax() { return tcMax; }
    public long getTyMin() { return tyMin; }
    public long getTyMax() { return tyMax; }

    @Override
    public String toString() {
        return "Configuracion{" +
                "T=" + t +
                ", M=" + m + ", P=" + p + ", Z=" + z + ", C=" + c + ", Y=" + y +
                ", TP=[" + tpMin + "," + tpMax + "]" +
                ", TM=[" + tmMin + "," + tmMax + "]" +
                ", TQ=[" + tqMin + "," + tqMax + "]" +
                ", TZ=[" + tzMin + "," + tzMax + "]" +
                ", TR=[" + trMin + "," + trMax + "]" +
                ", TL=[" + tlMin + "," + tlMax + "]" +
                ", TC=[" + tcMin + "," + tcMax + "]" +
                ", TY=[" + tyMin + "," + tyMax + "]" +
                '}';
    }
}