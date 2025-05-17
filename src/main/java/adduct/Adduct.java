package adduct;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Adduct {

    private static int extractCharge(String adduct) {
        if(adduct == null) return 1;
        Pattern pattern = Pattern.compile("\\[(\\d*)M[+-][^\\]]+\\](\\d*)([+-])");
        Matcher matcher = pattern.matcher(adduct);
        if(matcher.matches()){
            String chargeStr = matcher.group(2);
            return(chargeStr == null || chargeStr.isEmpty()) ? 1 : Integer.parseInt(chargeStr);
        }
        return 1;
    }
    private static int extractMultimer(String adduct) {
        if(adduct == null) return 1;
        Pattern pattern = Pattern.compile("\\[(\\d*)M[+-][^\\]]+\\](\\d*)([+-])");
        Matcher matcher = pattern.matcher(adduct);
        if(matcher.matches()){
            String multimerStr = matcher.group(1);
            return(multimerStr == null || multimerStr.isEmpty()) ? 1 : Integer.parseInt(multimerStr);
        }
        return 1;
    }

    /**
     * Calculate the mass to search depending on the adduct hypothesis
     *
     * @param mz mz
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return the monoisotopic mass of the experimental mass mz with the adduct @param adduct
     */
    public static Double getMonoisotopicMassFromMZ(Double mz, String adduct) {
        /*
        if Adduct is single charge the formula is M = m/z +- adductMass. Charge is 1 so it does not affect
        if Adduct is double or triple charged the formula is M = ( mz +- adductMass ) * charge
        if adduct is a dimer or multimer the formula is M =  (mz +- adductMass) / numberOfMultimer
        return monoisotopicMass;
         */
        if(mz == null || adduct == null || adduct.isEmpty()) {
            return null;
        }
        int charge = extractCharge(adduct);
        int multimer = extractMultimer(adduct);
        Double adductMass = 0.0;
        if (AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adduct)) {
            adductMass = -AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
        } else if (AdductList.MAPMZNEGATIVEADDUCTS.containsKey(adduct)) {
            adductMass = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
        }
        System.out.println("adduct" + adduct + ", charge: " + charge + ", multimer: " + multimer);
        return multimer * (mz * charge + adductMass); //(mz * charge + adductMass) / multimer;
    }

    /**
     * Calculate the mz of a monoisotopic mass with the corresponding adduct
     *
     * @param monoisotopicMass
     * @param adduct adduct name ([M+H]+, [2M+H]+, [M+2H]2+, etc..)
     *
     * @return
     */
    public static Double getMZFromMonoisotopicMass(Double monoisotopicMass, String adduct) {

        /*
        if Adduct is single charge the formula is m/z = M +- adductMass. Charge is 1 so it does not affect
        if Adduct is double or triple charged the formula is mz = M/charge +- adductMass
        if adduct is a dimer or multimer the formula is mz = M * numberOfMultimer +- adductMass
        return monoisotopicMass;
         */

        if(monoisotopicMass == null || adduct == null || adduct.isEmpty()){
            return null;
        }
        int charge = extractCharge(adduct);
        int multimer = extractMultimer(adduct);
        Double adductMass = 0.0;
        if (AdductList.MAPMZPOSITIVEADDUCTS.containsKey(adduct)) {
            adductMass = -AdductList.MAPMZPOSITIVEADDUCTS.get(adduct);
        } else if (AdductList.MAPMZNEGATIVEADDUCTS.containsKey(adduct)) {
            adductMass = AdductList.MAPMZNEGATIVEADDUCTS.get(adduct);
        }
        return ((monoisotopicMass / multimer) - adductMass) / Math.abs(charge);//((monoisotopicMass * multimer) - adductMass) / charge;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass    Mass measured by MS
     * @param theoreticalMass Theoretical mass of the compound
     */
    public static int calculatePPMIncrement(Double experimentalMass, Double theoreticalMass) {
        int ppmIncrement;
        ppmIncrement = (int) Math.round(Math.abs((experimentalMass - theoreticalMass) * 1000000 / theoreticalMass));
        return ppmIncrement;
    }

    /**
     * Returns the ppm difference between measured mass and theoretical mass
     *
     * @param experimentalMass    Mass measured by MS
     * @param ppm ppm of tolerance
     */
    public static double calculateDeltaPPM(Double experimentalMass, int ppm) {
        double deltaPPM;
        deltaPPM =  Math.round(Math.abs((experimentalMass * ppm) / 1000000));
        return deltaPPM;
    }
}
