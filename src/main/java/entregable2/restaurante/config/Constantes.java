package main.java.entregable2.restaurante.config;

/**
 * Claves y valores por defecto de configuración.
 * Evita strings mágicos y centraliza defaults.
 */
public final class Constantes {

    private Constantes() { }

    // ---- Claves ----
    public static final String KEY_T = "T";
    public static final String KEY_M = "M";
    public static final String KEY_P = "P";
    public static final String KEY_Z = "Z";
    public static final String KEY_C = "C";
    public static final String KEY_Y = "Y";

    public static final String KEY_TP_MIN = "TPmin";
    public static final String KEY_TP_MAX = "TPmax";
    public static final String KEY_TM_MIN = "TMmin";
    public static final String KEY_TM_MAX = "TMmax";
    public static final String KEY_TQ_MIN = "TQmin";
    public static final String KEY_TQ_MAX = "TQmax";
    public static final String KEY_TZ_MIN = "TZmin";
    public static final String KEY_TZ_MAX = "TZmax";
    public static final String KEY_TR_MIN = "TRmin";
    public static final String KEY_TR_MAX = "TRmax";
    public static final String KEY_TL_MIN = "TLmin";
    public static final String KEY_TL_MAX = "TLmax";
    public static final String KEY_TC_MIN = "TCmin";
    public static final String KEY_TC_MAX = "TCMax";
    public static final String KEY_TY_MIN = "TYmin";
    public static final String KEY_TY_MAX = "TYmax";

    // ---- Defaults ----
    public static final int DEF_T = 30;
    public static final int DEF_M = 5;
    public static final int DEF_P = 4;
    public static final int DEF_Z = 2;
    public static final int DEF_C = 2;
    public static final int DEF_Y = 1;

    public static final long DEF_TP_MIN = 500, DEF_TP_MAX = 2000;
    public static final long DEF_TM_MIN = 300, DEF_TM_MAX = 1000;
    public static final long DEF_TQ_MIN = 2000, DEF_TQ_MAX = 5000;
    public static final long DEF_TZ_MIN = 300, DEF_TZ_MAX = 1000;
    public static final long DEF_TR_MIN = 500, DEF_TR_MAX = 1500;
    public static final long DEF_TL_MIN = 500, DEF_TL_MAX = 1500;
    public static final long DEF_TC_MIN = 1000, DEF_TC_MAX = 3000;
    public static final long DEF_TY_MIN = 300, DEF_TY_MAX = 1000;
}