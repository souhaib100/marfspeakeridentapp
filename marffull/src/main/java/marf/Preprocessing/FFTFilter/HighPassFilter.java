package marf.Preprocessing.FFTFilter;

import marf.Preprocessing.IPreprocessing;
import marf.Preprocessing.PreprocessingException;
import marf.Storage.Sample;
import marf.util.Debug;


/**
 * <p>HighPassFilter class implements high-pass filtering the FFT Filter.</p>
 * <p>
 * <p>$Id: HighPassFilter.java,v 1.15 2005/12/31 23:17:37 mokhov Exp $</p>
 *
 * @author Serguei Mokhov
 * @version $Revision: 1.15 $
 * @since 0.0.1
 */
public class HighPassFilter
        extends FFTFilter {
    /**
     * For serialization versioning.
     * When adding new members or make other structural
     * changes regenerate this number with the
     * <code>serialver</code> tool that comes with JDK.
     *
     * @since 0.3.0.4
     */
    private static final long serialVersionUID = 6784418884789745008L;

    /**
     * Default constructor for reflective creation of Preprocessing
     * clones. Typically should not be used unless really necessary
     * for the frameworked modules.
     *
     * @since 0.3.0.5
     */
    public HighPassFilter() {
        super();
    }

    /**
     * Preprocessing pipeline constructor.
     *
     * @param poPreprocessing follow up preprocessing module
     * @throws PreprocessingException
     * @since 0.3.0.3
     */
    public HighPassFilter(IPreprocessing poPreprocessing)
            throws PreprocessingException {
        super(poPreprocessing);
    }

    /**
     * HighPassFilter Constructor.
     *
     * @param poSample incoming sample
     * @throws PreprocessingException
     */
    public HighPassFilter(Sample poSample)
            throws PreprocessingException {
        super(poSample);
    }

    /**
     * Stub implementation of <code>removeNoise()</code>.
     *
     * @return <code>false</code>
     * @throws PreprocessingException never thrown
     */
    public final boolean removeNoise() {
        Debug.debug("HighPassFilter.removeNoise()");
        return false;
    }

    /**
     * Stub implementation of <code>removeSilence()</code>.
     *
     * @return <code>false</code>
     * @throws PreprocessingException never thrown
     */
    public final boolean removeSilence() {
        Debug.debug("HighPassFilter.removeSilence()");
        return false;
    }

    /**
     * Stub implementation of <code>cropAudio()</code>.
     *
     * @param pdStartingFrequency unused
     * @param pdEndFrequency      unused
     * @return <code>false</code>
     * @throws PreprocessingException never thrown
     */
    public final boolean cropAudio(double pdStartingFrequency, double pdEndFrequency) {
        Debug.debug("HighPassFilter.cropAudio()");
        return false;
    }

    /**
     * Creates high-pass frequency response coefficients and sets applies
     * them to the frequency response vector.
     *
     * @since 0.3.0
     */
    public void genereateResponseCoefficients() {
        double[] adResponse = new double[DEFAULT_FREQUENCY_RESPONSE_SIZE];

		/*
         * Create a response that drops all frequencies below 2853 Hz
		 * XXX -- Note: 2853Hz = 70 * 128 every 8000Hz
		 */
        for (int i = 0; i < adResponse.length; i++) {
            if (i > 70) {
                adResponse[i] = 1;
            } else {
                adResponse[i] = 0;
            }
        }

        setFrequencyResponse(adResponse);
    }

    /**
     * Returns source code revision information.
     *
     * @return revision string
     * @since 0.3.0.2
     */
    public static String getMARFSourceCodeRevision() {
        return "$Revision: 1.15 $";
    }
}

// EOF
