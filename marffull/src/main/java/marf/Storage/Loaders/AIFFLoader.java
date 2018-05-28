package marf.Storage.Loaders;

import java.io.File;

import marf.Storage.Sample;
import marf.Storage.SampleLoader;
import marf.Storage.StorageException;
import marf.util.NotImplementedException;


/**
 * Not Implemented.
 *
 * @author Serguei Mokhov
 * @version $Revision: 1.3 $
 * @since 0.3.0.2
 */
public class AIFFLoader
        extends SampleLoader {
    /**
     * AIFF Loader Constructor.
     */
    public AIFFLoader() {
    }

    /**
     * Not Implemented.
     *
     * @param padSample unused
     * @return nothing
     * @throws NotImplementedException
     * @throws StorageException        never thrown
     */
    public final int readAudioData(double[] padSample) {
        throw new NotImplementedException("readAudioData()");
    }

    /**
     * Not Implemented.
     *
     * @param padSample unused
     * @param piNbrData unused
     * @return nothing
     * @throws NotImplementedException
     * @throws StorageException        never thrown
     */
    public final int writeAudioData(final double[] padSample, final int piNbrData) {
        throw new NotImplementedException("writeAudioData()");
    }

    /**
     * Not Implemented.
     *
     * @param poFile unused
     * @return nothing
     * @throws NotImplementedException
     * @throws StorageException        never thrown
     */
    public Sample loadSample(File poFile) {
        throw new NotImplementedException("loadSample()");
    }

    /**
     * Not Implemented.
     *
     * @param poFile unused
     * @throws NotImplementedException
     * @throws StorageException        never thrown
     */
    public void saveSample(File poFile) {
        throw new NotImplementedException("saveSample()");
    }

    /**
     * Returns source code revision information.
     *
     * @return revision string
     */
    public static String getMARFSourceCodeRevision() {
        return "$Revision: 1.3 $";
    }
}

// EOF
