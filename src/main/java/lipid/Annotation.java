package lipid;

import java.util.*;
import adduct.Adduct;
import adduct.AdductList;
import static adduct.Adduct.*;
import java.util.*;

/**
 * Class to represent the annotation over a lipid
 */
public class Annotation {

    private final Lipid lipid;
    private final double mz;
    private final double intensity; // intensity of the most abundant peak in the groupedPeaks
    private final double rtMin;
    private String adduct;
    private final Set<Peak> groupedSignals;
    private int score;
    private int totalScoresApplied;
    private Ionization ionization;

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     */

    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, Ionization ionization) {
        this(lipid, mz, intensity, retentionTime, ionization, Collections.emptySet());
    }

    /**
     * @param lipid
     * @param mz
     * @param intensity
     * @param retentionTime
     * @param groupedSignals
     */
    public Annotation(Lipid lipid, double mz, double intensity, double retentionTime, Ionization ionization, Set<Peak> groupedSignals) {
        this.lipid = lipid;
        this.mz = mz;
        this.rtMin = retentionTime;
        this.intensity = intensity;
        this.groupedSignals = new TreeSet<>(groupedSignals);
        this.score = 0;
        this.totalScoresApplied = 0;
        this.ionization = ionization;
        this.adduct = detectAdduct(this.groupedSignals);
    }

    public Lipid getLipid() {
        return lipid;
    }

    public double getMz() {
        return mz;
    }

    public double getRtMin() {
        return rtMin;
    }

    public String getAdduct() {
        return adduct;
    }

    public void setAdduct(String adduct) {
        this.adduct = adduct;
    }

    public double getIntensity() {
        return intensity;
    }

    public Ionization getIonizationMode() {
        return ionization;
    }

    public Set<Peak> getGroupedSignals() {
        return Collections.unmodifiableSet(groupedSignals);
    }


    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    // !CHECK Take into account that the score should be normalized between -1 and 1
    public void addScore(int delta) {
        this.score += delta;
        this.totalScoresApplied++;
    }

    /**
     * @return The normalized score between 0 and 1 that consists on the final number divided into the times that the rule
     * has been applied.
     */
    public double getNormalizedScore() {
        return (double) this.score / this.totalScoresApplied;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Annotation)) return false;
        Annotation that = (Annotation) o;
        return Double.compare(that.mz, mz) == 0 &&
                Double.compare(that.rtMin, rtMin) == 0 &&
                Objects.equals(lipid, that.lipid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lipid, mz, rtMin);
    }

    @Override
    public String toString() {
        return String.format("Annotation(%s, mz=%.4f, RT=%.2f, adduct=%s, intensity=%.1f, score=%d)",
                lipid.getName(), mz, rtMin, adduct, intensity, score);
    }

    private static final int TOLERANCE_PPM = 10;

    public String detectAdduct(Set<Peak> groupedSignals) {
        if (groupedSignals == null || groupedSignals.size() < 1) return "unknown";

        Map<String, Double> adductMap = ionization == Ionization.POSITIVE
                ? AdductList.MAPMZPOSITIVEADDUCTS
                : AdductList.MAPMZNEGATIVEADDUCTS;

        String bestAdduct = "unknown";
        double bestPpm = Double.MAX_VALUE;

        List<Peak> peaks = new ArrayList<>(groupedSignals);

        for (String adduct : adductMap.keySet()) {
            for (Peak peak : peaks) {
                // Calcular masa monoisotópica desde ese aducto y mz del pico
                Double monoMass = Adduct.getMonoisotopicMassFromMZ(peak.getMz(), adduct);
                if (monoMass == null) continue;

                // Obtener mz esperado con ese aducto
                Double expectedMz = Adduct.getMZFromMonoisotopicMass(monoMass, adduct);
                if (expectedMz == null) continue;

                // Calcular error en ppm comparando el mz calculado vs el mz de esta anotación
                double ppmError = Adduct.calculatePPMIncrement(this.mz, expectedMz);
                System.out.printf("mz=%.5f | adduct=%-15s | expectedMz=%.5f | ppm=%.2f\n", this.mz, adduct, expectedMz, ppmError);

                if (ppmError < TOLERANCE_PPM && ppmError < bestPpm) {
                    bestPpm = ppmError;
                    bestAdduct = adduct;
                }
            }
        }
        return bestAdduct;
    }
}
