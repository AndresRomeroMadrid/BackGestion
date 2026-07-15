package com.example.BackGestion.Util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Valida y formatea RUT chilenos.
 * Acepta como entrada el formato "12345678-9" (con o sin puntos) y
 * produce como salida el formato "12.345.678-9", con el digito
 * verificador siempre en mayuscula cuando corresponde a "K".
 */
public final class RutValidator {

    private static final Pattern RUT_PATTERN = Pattern.compile("^(\\d{1,3}(?:\\.?\\d{3}){1,2})-([0-9kK])$");

    private RutValidator() {
    }

    public static boolean esValido(String rut) {
        if (rut == null) {
            return false;
        }
        Matcher matcher = RUT_PATTERN.matcher(rut.trim());
        if (!matcher.matches()) {
            return false;
        }
        String numero = matcher.group(1).replace(".", "");
        char dv = Character.toUpperCase(matcher.group(2).charAt(0));
        return calcularDv(numero) == dv;
    }

    public static String formatear(String rut) {
        if (!esValido(rut)) {
            throw new IllegalArgumentException("RUT invalido: " + rut);
        }
        Matcher matcher = RUT_PATTERN.matcher(rut.trim());
        matcher.matches();
        String numero = matcher.group(1).replace(".", "");
        char dv = Character.toUpperCase(matcher.group(2).charAt(0));

        StringBuilder conPuntos = new StringBuilder();
        int contador = 0;
        for (int i = numero.length() - 1; i >= 0; i--) {
            conPuntos.insert(0, numero.charAt(i));
            contador++;
            if (contador % 3 == 0 && i != 0) {
                conPuntos.insert(0, '.');
            }
        }
        return conPuntos.append('-').append(dv).toString();
    }

    private static char calcularDv(String numero) {
        int suma = 0;
        int multiplicador = 2;
        for (int i = numero.length() - 1; i >= 0; i--) {
            suma += Character.getNumericValue(numero.charAt(i)) * multiplicador;
            multiplicador = multiplicador == 7 ? 2 : multiplicador + 1;
        }
        int resto = 11 - (suma % 11);
        if (resto == 11) {
            return '0';
        }
        if (resto == 10) {
            return 'K';
        }
        return Character.forDigit(resto, 10);
    }
}
