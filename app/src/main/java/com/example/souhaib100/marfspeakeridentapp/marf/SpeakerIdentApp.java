package com.example.souhaib100.marfspeakeridentapp.marf;



import com.example.souhaib100.marfspeakeridentapp.Variables;

import java.io.File;
import java.util.Date;

import marf.MARF;
import marf.Storage.ModuleParams;
import marf.Storage.TrainingSet;
import marf.util.Debug;
import marf.util.MARFException;
import marf.util.OptionProcessor;


/**
 * <p>Identifies a speaker independently of text, based on the MARF framework,
 * <a href="http://marf.sf.net">http://marf.sf.net</a>.
 * </p>
 *
 * $Id: SpeakerIdentApp.java,v 1.70 2009/03/03 12:06:54 mokhov Exp $
 *
 * @author Serguei Mokhov
 * @author Stephen Sinclair
 * @author Ian Clement
 * @author Dimitrios Nicolacopoulos
 * @author The MARF Research and Development Group
 *
 * @version 0.3.0, $Revision: 1.70 $
 * @since 0.0.1
 */
public class SpeakerIdentApp
{
	public static String path = "";

	/*
	 * ----------------
	 * Apps. Versioning
	 * ----------------
	 */

	/**
	 * Current major version of the application.
	 */
	public static final int MAJOR_VERSION = 0;

	/**
	 * Current minor version of the application.
	 */
	public static final int MINOR_VERSION = 3;

	/**
	 * Current revision of the application.
	 */
	public static final int REVISION      = 0;

	
	/*
	 * ----------------------------------
	 * Defaults
	 * ----------------------------------
	 */

	/**
	 * Default file name extension for sample files; case-insensitive.
	 * @since 0.3.0.6
	 */
	public static final String DEFAULT_SAMPLE_FILE_EXTENSION = "wav";
	//public static final String DEFAULT_SAMPLE_FILE_EXTENSION = "txt";

	/*
	 * ----------------------------------
	 * Major and Misc Options Enumeration
	 * ----------------------------------
	 */

	/**
	 * Numeric equivalent of the option <code>--train</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_TRAIN = 0;

	/**
	 * Numeric equivalent of the option <code>--ident</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_IDENT = 1;

	/**
	 * Numeric equivalent of the option <code>--stats</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_STATS = 2;

	/**
	 * Numeric equivalent of the option <code>--reset</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_RESET = 3;

	/**
	 * Numeric equivalent of the option <code>--version</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_VERSION = 4;

	/**
	 * Numeric equivalent of the option <code>--help</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_HELP_LONG = 5;

	/**
	 * Numeric equivalent of the option <code>-h</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_HELP_SHORT = 6;

	/**
	 * Numeric equivalent of the option <code>-debug</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_DEBUG = 7;

	/**
	 * Numeric equivalent of the option <code>-graph</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_GRAPH = 8;

	/**
	 * Numeric equivalent of the option <code>-spectrogram</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_SPECTROGRAM = 9;

	/**
	 * Numeric equivalent of the option <code>&lt;speaker ID&gt;</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_EXPECTED_SPEAKER_ID = 10;

	/**
	 * Numeric equivalent of the option <code>--batch-ident</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_BATCH_IDENT = 11;

	/**
	 * Numeric equivalent of the option <code>--single-train</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_SINGLE_TRAIN = 12;

	/**
	 * Numeric equivalent of the option <code>&lt;sample-file-or-directory-name&gt;</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_DIR_OR_FILE = 13;

	/**
	 * Numeric equivalent of the option <code>--best-score</code>.
	 * @since 0.3.0.5
	 */
	public static final int OPT_BEST_SCORE = 14;
	
	/**
	 * Numeric equivalent of the option <code>--gui</code> to have a GUI.
	 * @since 0.3.0.6
	 */
	public static final int OPT_GUI = 15;

	/**
	 * Numeric equivalent of the option <code>-noise</code> to enable noise removal.
	 * @since 0.3.0.6
	 */
	public static final int OPT_NOISE_REMOVAL = 16;

	/**
	 * Numeric equivalent of the option <code>-silence</code> to enable silence removal.
	 * @since 0.3.0.6
	 */
	public static final int OPT_SILENCE_REMOVAL = 17;


	/*
	 * ---------------------
	 * State Data Structures
	 * ---------------------
	 */

	/**
	 * Instance of the database of speakers to track stats per
	 * configuration for all speakers.
	 * @since 0.3.0.5
	 */
	protected static SpeakersIdentDb soDBPerConfig;

	/**
	 * Instance of the database of speakers to track stats per
	 * speaker for all configuration.
	 * @since 0.3.0.6
	 */
	protected static SpeakersIdentDb soDBPerSpeaker;

//	protected static SpeakersIdentDb soDBPerConfig = new SpeakersIdentDb("languages.txt");
//	protected static SpeakersIdentDb soDBPerSpeaker = new SpeakersIdentDb("languages.txt");

	/**
	 * Instance of the option processing utility.
	 * @since 0.3.0.5
	 */
	protected static OptionProcessor soGetOpt = new OptionProcessor();

	/*
	 * -----------------
	 * Static State Init
	 * -----------------
	 */

	static
	{
		// Main options
		soGetOpt.addValidOption(OPT_TRAIN, "--train");
		soGetOpt.addValidOption(OPT_SINGLE_TRAIN, "--single-train");
		soGetOpt.addValidOption(OPT_IDENT, "--ident");
		soGetOpt.addValidOption(OPT_BATCH_IDENT, "--batch-ident");
		soGetOpt.addValidOption(OPT_STATS, "--stats");
		soGetOpt.addValidOption(OPT_BEST_SCORE, "--best-score");
		soGetOpt.addValidOption(OPT_VERSION, "--version");
		soGetOpt.addValidOption(OPT_RESET, "--reset");
		soGetOpt.addValidOption(OPT_HELP_LONG, "--help");
		soGetOpt.addValidOption(OPT_HELP_SHORT, "-h");
		soGetOpt.addValidOption(OPT_GUI, "--gui");

		// Sample Loading
		soGetOpt.addValidOption(MARF.WAV, "-wav");
//		soGetOpt.addValidOption(MARF.TEXT, "-text");

		// Preprocessing
		soGetOpt.addValidOption(OPT_NOISE_REMOVAL, "-noise");
		soGetOpt.addValidOption(OPT_SILENCE_REMOVAL, "-silence");

		soGetOpt.addValidOption(MARF.DUMMY, "-norm");
		soGetOpt.addValidOption(MARF.HIGH_FREQUENCY_BOOST_FFT_FILTER, "-boost");
		soGetOpt.addValidOption(MARF.HIGH_PASS_FFT_FILTER, "-high");
		soGetOpt.addValidOption(MARF.LOW_PASS_FFT_FILTER, "-low");
		soGetOpt.addValidOption(MARF.BANDPASS_FFT_FILTER, "-band");
//		soGetOpt.addValidOption(MARF.BAND_STOP_FFT_FILTER, "-bandstop");
		soGetOpt.addValidOption(MARF.HIGH_PASS_BOOST_FILTER, "-highpassboost");
		soGetOpt.addValidOption(MARF.RAW, "-raw");
		soGetOpt.addValidOption(MARF.ENDPOINT, "-endp");
//		soGetOpt.addValidOption(MARF.HIGH_PASS_CFE_FILTER, "-highcfe");
//		soGetOpt.addValidOption(MARF.LOW_PASS_CFE_FILTER, "-lowcfe");
//		soGetOpt.addValidOption(MARF.BAND_PASS_CFE_FILTER, "-bandcfe");
//		soGetOpt.addValidOption(MARF.BAND_STOP_CFE_FILTER, "-bandstopcfe");

		// Feature extraction
		soGetOpt.addValidOption(MARF.FFT, "-fft");
		soGetOpt.addValidOption(MARF.LPC, "-lpc");
		soGetOpt.addValidOption(MARF.RANDOM_FEATURE_EXTRACTION, "-randfe");
		soGetOpt.addValidOption(MARF.MIN_MAX_AMPLITUDES, "-minmax");
		soGetOpt.addValidOption(MARF.FEATURE_EXTRACTION_AGGREGATOR, "-aggr");
		soGetOpt.addValidOption(MARF.F0, "-f0");
		soGetOpt.addValidOption(MARF.SEGMENTATION, "-segm");
		soGetOpt.addValidOption(MARF.CEPSTRAL, "-cepstral");

		// Classification
		soGetOpt.addValidOption(MARF.NEURAL_NETWORK, "-nn");
		soGetOpt.addValidOption(MARF.EUCLIDEAN_DISTANCE, "-eucl");
		soGetOpt.addValidOption(MARF.CHEBYSHEV_DISTANCE, "-cheb");
		soGetOpt.addValidOption(MARF.MINKOWSKI_DISTANCE, "-mink");
		soGetOpt.addValidOption(MARF.MAHALANOBIS_DISTANCE, "-mah");
		soGetOpt.addValidOption(MARF.RANDOM_CLASSIFICATION, "-randcl");
		soGetOpt.addValidOption(MARF.DIFF_DISTANCE, "-diff");
//		soGetOpt.addValidOption(MARF.ZIPFS_LAW, "-zipf");
		soGetOpt.addValidOption(MARF.MARKOV, "-markov");
//		soGetOpt.addValidOption(MARF.HAMMING_DISTANCE, "-hamming");
//		soGetOpt.addValidOption(MARF.COSINE_SIMILARITY_MEASURE, "-cos");

		// Misc
		soGetOpt.addValidOption(OPT_SPECTROGRAM, "-spectrogram");
		soGetOpt.addValidOption(OPT_DEBUG, "-debug");
		soGetOpt.addValidOption(OPT_GRAPH, "-graph");
	}
	
	
	/**
	 * Main body.
	 * @param argv command-line arguments
	 */
	public static final void main(String[] argv)
	{
		try
		{
			TrainingSet.WORKING_PATH = path;
			soDBPerConfig = new SpeakersIdentDb(path + "/" + "speakers.txt");
			soDBPerSpeaker = new SpeakersIdentDb(path + "/" + "speakers.txt");
			// Since some new API is always introduced...
			//validateVersions();

			/*
			 * Load the speakers database
			 */
			soDBPerConfig.connect();
			soDBPerConfig.query();

			soDBPerSpeaker.connect();
			soDBPerSpeaker.query();

			soDBPerConfig.setFilename("config." + soDBPerConfig.getFilename());
			soDBPerSpeaker.setFilename("speaker." + soDBPerSpeaker.getFilename());

			setDefaultConfig();
			
			// Parse extra arguments
			int iValidOptions = soGetOpt.parse(argv);

			if(iValidOptions == 0)
			{
				throw new Exception("No valid options found: " + soGetOpt);
			}

			setCustomConfig();

			/*
			 * If supplied in the command line, the system when classifying,
			 * will output this ID next to the guessed one.
			 */
			int iExpectedID = -1;

			switch(soGetOpt.getInvalidOptions().size())
			{
				// Unknown
				case 0:
				{
					iExpectedID = -1;
					break;
				}
				
				/*
				 * Extract and make active and valid option out of
				 * a filename and and an expected speaker ID. An
				 * assumption is that the expected speaker ID is
				 * always second on the command line somewhere after
				 * a file or directory name. Presence of the expected
				 * speaker ID always implies presence of the file
				 * or directory argument. 
				 */
				case 2:
				{
					try
					{
						iExpectedID = Integer.parseInt(soGetOpt.getInvalidOptions().elementAt(1).toString());
						soGetOpt.addActiveOption(OPT_EXPECTED_SPEAKER_ID, soGetOpt.getInvalidOptions().elementAt(1).toString());
					}

					/*
					 * May happened when trying to get expected ID,
					 * but the argument doesn't parse as an int.
					 */
					catch(NumberFormatException e)
					{
						iExpectedID = -1;

						System.err.println
						(
							"SpeakerIdentApp: WARNING: could not parse expected speaker ID ("
							+ e.getMessage() + "), ignoring..."
						);
					}

					// No break required as the file or directory
					// must always be present. Also, the clearance
					// of the invalid options need to be postponed.
				}
				
				/*
				 * In the case of a single invalid option
				 * always assume it is either a filename
				 * or a directory name for the --ident
				 * or --train options.
				 */
				case 1:
				{
					soGetOpt.addActiveOption(OPT_DIR_OR_FILE, soGetOpt.getInvalidOptions().firstElement().toString());
					soGetOpt.getInvalidOptions().clear();
					break;
				}

				default:
				{
					throw new Exception("Unrecognized options found: " + soGetOpt.getInvalidOptions());
				}
			}

			// Set misc configuration
			MARF.setDumpSpectrogram(soGetOpt.isActiveOption(OPT_SPECTROGRAM));
			MARF.setDumpWaveGraph(soGetOpt.isActiveOption(OPT_GRAPH));
			Debug.enableDebug(soGetOpt.isActiveOption(OPT_DEBUG));
			Debug.debug("Option set: " + soGetOpt);

			int iMainOption = soGetOpt.getOption(argv[0]);

			switch(iMainOption)
			{
				/*
				 * --------------
				 * Identification
				 * --------------
				 */
				
				// Single case
				case OPT_IDENT:
				{
					ident(getConfigString(argv), soGetOpt.getOption(OPT_DIR_OR_FILE), iExpectedID);
					break;
				}

				// A directory with files for identification
				case OPT_BATCH_IDENT:
				{
					// Store config and error/successes for that config
					String strConfig = getConfigString(argv);

					// Dir contents
					File[] aoSampleFiles = new File(soGetOpt.getOption(OPT_DIR_OR_FILE)).listFiles();

					for(int i = 0; i < aoSampleFiles.length; i++)
					{
						String strFileName = aoSampleFiles[i].getPath();

						if(aoSampleFiles[i].isFile() && strFileName.toLowerCase().endsWith("." + DEFAULT_SAMPLE_FILE_EXTENSION))
						{
							ident(strConfig, strFileName, iExpectedID);
						}
					}

					break;
				}

				/*
				 * --------
				 * Training
				 * --------
				 */

				// Add a single sample to the training set
				case OPT_SINGLE_TRAIN:
				{
					train(soGetOpt.getOption(OPT_DIR_OR_FILE));
					System.out.println("Done training with file \"" + soGetOpt.getOption(OPT_DIR_OR_FILE) + "\".");
					break;
				}
				
				// Train on a directory of files
				case OPT_TRAIN:
				{
					try
					{
						// Dir contents
						File[] aoSampleFiles = new File(soGetOpt.getOption(OPT_DIR_OR_FILE)).listFiles();

						Debug.debug("Files array: " + aoSampleFiles);

						if(Debug.isDebugOn())
						{
							System.getProperties().list(System.err);
						}

						String strFileName = "";

						// XXX: this loop has to be in MARF
						for(int i = 0; i < aoSampleFiles.length; i++)
						{
							strFileName = aoSampleFiles[i].getPath();

							if(aoSampleFiles[i].isFile() && strFileName.toLowerCase().endsWith("." + DEFAULT_SAMPLE_FILE_EXTENSION))
							{
								train(strFileName);
							}
						}
					}
					catch(NullPointerException e)
					{
						System.err.println("Folder \"" + soGetOpt.getOption(OPT_DIR_OR_FILE) + "\" not found.");
						e.printStackTrace(System.err);
						System.exit(-1);
					}

					System.out.println("Done training on folder \"" + soGetOpt.getOption(OPT_DIR_OR_FILE) + "\".");

					break;
				}

				/*
				 * -----
				 * Stats
				 * -----
				 */
				case OPT_STATS:
				{
					soDBPerConfig.restore();
					soDBPerConfig.printStats();
					soDBPerSpeaker.restore();
					soDBPerSpeaker.printStats();
					break;
				}

				/*
				 * Best Result with Stats
				 */
				case OPT_BEST_SCORE:
				{
					soDBPerConfig.restore();
					soDBPerConfig.printStats(true);
					soDBPerSpeaker.restore();
					soDBPerSpeaker.printStats(true);
					break;
				}

				/*
				 * Reset Stats
				 */
				case OPT_RESET:
				{
					soDBPerConfig.resetStats();
					soDBPerSpeaker.resetStats();
					System.out.println("SpeakerIdentApp: Statistics has been reset.");
					break;
				}

				/*
				 * Versioning
				 */
				case OPT_VERSION:
				{
					System.out.println("Text-Independent Speaker Identification Application, v." + getVersion());
					System.out.println("Using MARF, v." + MARF.getVersion());
					validateVersions();
					break;
				}

				/*
				 * Help
				 */
				case OPT_HELP_SHORT:
				case OPT_HELP_LONG:
				{
					//usage();
					break;
				}

				/*
				 * Invalid major option
				 */
				default:
				{
					throw new Exception("Unrecognized option: " + argv[0]);
				}
			} // major option switch
		} // try

		/*
		 * No arguments have been specified
		 */
		catch(ArrayIndexOutOfBoundsException e)
		{
			System.err.println("No arguments have been specified.");

			//if(Debug.isDebugOn())
			{
				System.err.println(e.getMessage());
				e.printStackTrace(System.err);
			}

			//usage();
		}

		/*
		 * MARF-specific errors
		 */
		catch(MARFException e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
		}

		/*
		 * Invalid option and/or option argument
		 */
		catch(Exception e)
		{
			System.err.println(e.getMessage());
			e.printStackTrace(System.err);
			//usage();
		}

		/*
		 * Regardless whatever happens, close the db connection.
		 */
		finally
		{
			try
			{
				Debug.debug("Closing DB connection...");
				soDBPerConfig.close();
				soDBPerSpeaker.close();
			}
			catch(Exception e)
			{
				Debug.debug("Closing DB connection failed: " + e.getMessage());
				e.printStackTrace(System.err);
				System.exit(-1);
			}
		}
	}

	/**
	 * Identifies a speaker using MARF given configuration,
	 * wave filename, and possibly expected speaker ID.
	 *
	 * @param pstrConfig configuration string for stats
	 * @param pstrFilename name of the wave file with voice sample to identify
	 * @param piExpectedID expected speaker ID; if -1 then no stats is kept
	 *
	 * @throws MARFException in case of any error while processing is in MARF
	 * @since 0.3.0.5
	 */
	public static final void ident(String pstrConfig, String pstrFilename, int piExpectedID)
	throws MARFException
	{
		/*
		 * If no expected speaker present on the command line,
		 * attempt to fetch it from the database by filename.
		 */
		if(piExpectedID < 0)
		{
			piExpectedID = soDBPerConfig.getIDByFilename(pstrFilename, false);
		}

		int iIdentifiedID = -1;

		// Second best
		int iSecondClosestID = -1;

		long lTimeStart = System.currentTimeMillis();
		{
			MARF.setSampleFile(pstrFilename);
			MARF.recognize();
	
			// First guess
			iIdentifiedID = MARF.queryResultID();

			//Store the name of the first guess in the variable name
			Variables.name = soDBPerConfig.getName(iIdentifiedID);

			// Second best
			iSecondClosestID = MARF.getResultSet().getSecondClosestID();

		}
		long lTimeFinish = System.currentTimeMillis();

		// XXX: constify
		int iOneSecInMs  = 1000;
		int iOneMinInMs  = 60 * iOneSecInMs;
		int iOneHourInMs = 60 * iOneMinInMs;
		int iOneDayInMs  = 24 * iOneHourInMs;

		long lDuration = lTimeFinish - lTimeStart;

		long lDays = lDuration / iOneDayInMs;
		long lModTimeLeft = lDuration % iOneDayInMs;

		long lHours = lModTimeLeft / iOneHourInMs;
		lModTimeLeft %= iOneHourInMs;

		long lMinutes = lModTimeLeft / iOneMinInMs;
		lModTimeLeft %= iOneMinInMs;

		long lSeconds = lModTimeLeft / iOneSecInMs;
		lModTimeLeft %= iOneSecInMs;

		long lMilliSeconds = lModTimeLeft;

		StringBuffer oDuration = new StringBuffer()
			.append(lDays).append("d:")
			.append(lHours).append("h:")
			.append(lMinutes).append("m:")
			.append(lSeconds).append("s:")
			.append(lMilliSeconds).append("ms:")
			.append(lDuration).append("ms")
			;

		System.out.println("                 File: " + pstrFilename);
		System.out.println("               Config: " + pstrConfig);
		System.out.println("      Processing time: " + oDuration);
		System.out.println("         Speaker's ID: " + iIdentifiedID);
		System.out.println("   Speaker identified: " + soDBPerConfig.getName(iIdentifiedID));

		/*
		 * Only collect stats if we have expected speaker
		 */
		if(piExpectedID > 0)
		{
			System.out.println("Expected Speaker's ID: " + piExpectedID);
			System.out.println("     Expected Speaker: " + soDBPerConfig.getName(piExpectedID));

			soDBPerConfig.restore();
			soDBPerSpeaker.restore();
			{
				// 1st match
				soDBPerConfig.addStats(pstrConfig, (iIdentifiedID == piExpectedID));
				soDBPerSpeaker.addStats(soDBPerConfig.getName(piExpectedID), (iIdentifiedID == piExpectedID));

				// 2nd best: must be true if either 1st true or second true (or both :))
				boolean bSecondBest =
					iIdentifiedID == piExpectedID
					||
					iSecondClosestID == piExpectedID;

				soDBPerConfig.addStats(pstrConfig, bSecondBest, true);
				soDBPerSpeaker.addStats(soDBPerConfig.getName(piExpectedID), bSecondBest, true);
			}
			soDBPerConfig.dump();
			soDBPerSpeaker.dump();
		}

		System.out.println("       Second Best ID: " + iSecondClosestID);
		System.out.println("     Second Best Name: " + soDBPerConfig.getName(iSecondClosestID));
		System.out.println("            Date/time: " + new Date());
		System.out.println("----------------------------8<------------------------------");
	}

	/**
	 * Updates training set with a new sample from a given file.
	 *
	 * @param pstrFilename name of the wave file with voice sample train the system on
	 *
	 * @throws MARFException in case of any error while processing is in MARF
	 * @since 0.3.0.5
	 */
	public static final void train(String pstrFilename)
	throws MARFException
	{
		MARF.setSampleFile(pstrFilename);

		int iID = soDBPerConfig.getIDByFilename(pstrFilename, true);

		if(iID == -1)
		{
			System.out.println("No speaker found for \"" + pstrFilename + "\" for training.");
		}
		else
		{
			MARF.setCurrentSubject(iID);
			MARF.train();
		}
	}
//
//	/**
//	 * Displays application's usage information and exits.
//	 */
//	private static final void usage()
//	{
//		System.out.println
//		(
//			"Usage:\n" +
//			"  java SpeakerIdentApp --train <samples-dir> [options]        -- train mode\n" +
//			"                       --single-train <sample> [options]      -- add a single sample to the training set\n" +
//			"                       --ident <sample> [options]             -- identification mode\n" +
//			"                       --batch-ident <samples-dir> [options]  -- batch identification mode\n" +
//			"                       --gui                                  -- use GUI as a user interface\n" +
//			"                       --stats=[per-config|per-speaker|both]  -- display stats (default is per-config)\n" +
//			"                       --best-score                           -- display best classification result\n" +
//			"                       --reset                                -- reset stats\n" +
//			"                       --version                              -- display version info\n" +
//			"                       --help | -h                            -- display this help and exit\n\n" +
//
//			"Options (one or more of the following):\n\n" +
//
//			"Loaders:\n\n" +
//			"  -wav          - assume WAVE files loading (default)\n" +
//			"  -text         - assume loading of text samples\n" +
//			"\n" +
//
//			"Preprocessing:\n\n" +
//			"  -silence      - remove silence (can be combined with any of the below)\n" +
//			"  -noise        - remove noise (can be combined with any of the below)\n" +
//			"  -raw          - no preprocessing\n" +
//			"  -norm         - use just normalization, no filtering\n" +
//			"  -low          - use low-pass FFT filter\n" +
//			"  -high         - use high-pass FFT filter\n" +
//			"  -boost        - use high-frequency-boost FFT preprocessor\n" +
//			"  -band         - use band-pass FFT filter\n" +
//			"  -bandstop     - use band-stop FFT filter\n" +
//			"  -endp         - use endpointing\n" +
//			"  -lowcfe       - use low-pass CFE filter\n" +
//			"  -highcfe      - use high-pass CFE filter\n" +
//			"  -bandcfe      - use band-pass CFE filter\n" +
//			"  -bandstopcfe  - use band-stop CFE filter\n" +
//			"\n" +
//
//			"Feature Extraction:\n\n" +
//			"  -lpc          - use LPC\n" +
//			"  -fft          - use FFT\n" +
//			"  -minmax       - use Min/Max Amplitudes\n" +
//			"  -randfe       - use random feature extraction\n" +
//			"  -aggr         - use aggregated FFT+LPC feature extraction\n" +
//			"  -f0           - use F0 (pitch, or fundamental frequency; NOT IMPLEMENTED)\n" +
//			"  -segm         - use Segmentation (NOT IMPLEMENTED)\n" +
//			"  -cepstral     - use Cepstral analysis (NOT IMPLEMENTED)\n" +
//			"\n" +
//
//			"Classification:\n\n" +
//			"  -nn           - use Neural Network\n" +
//			"  -cheb         - use Chebyshev Distance\n" +
//			"  -eucl         - use Euclidean Distance\n" +
//			"  -mink         - use Minkowski Distance\n" +
//			"  -diff         - use Diff-Distance\n" +
//			"  -zipf         - use Zipf's Law-based classifier\n" +
//			"  -randcl       - use random classification\n" +
//			"  -markov       - use Hidden Markov Models (NOT IMPLEMENTED)\n" +
//			"  -hamming      - use Hamming Distance\n" +
//			"  -cos          - use Cosine Similarity Measure\n" +
//			"\n" +
//
//			"Misc:\n\n" +
//			"  -debug        - include verbose debug output\n" +
//			"  -spectrogram  - dump spectrogram image after feature extraction\n" +
//			"  -graph        - dump wave graph before preprocessing and after feature extraction\n" +
//			"  <integer>     - expected speaker ID\n" +
//			"\n"
//		);
//
//		System.exit(0);
//	}

	/**
	 * Retrieves String representation of the application's version.
	 * @return version String
	 */
	public static final String getVersion()
	{
		return MAJOR_VERSION + "." + MINOR_VERSION + "." + REVISION;
	}

	/**
	 * Retrieves integer representation of the application's version.
	 * @return integer version
	 */
	public static final int getIntVersion()
	{
		return MAJOR_VERSION * 100 + MINOR_VERSION * 10 + REVISION;
	}

	/**
	 * Makes sure the applications isn't run against older MARF version.
	 * Exits with 1 if the MARF version is too old.
	 */
	public static final void validateVersions()
	{
		if(MARF.getDoubleVersion() < (0 * 100 + 3 * 10 + 0 + .6))
		{
			System.err.println
			(
				"Your MARF version (" + MARF.getVersion() +
				") is too old. This application requires 0.3.0.6 or above."
			);

			System.exit(1);
		}
	}

	/**
	 * Composes the current configuration of in a string form.
	 *
	 * @param pstrArgv set of configuration options passed through the command line;
	 * can be null or empty. If latter is the case, MARF itself is queried for its
	 * numerical set up inside.
	 *
	 * @return the current configuration setup
	 */
	public static final String getConfigString(String[] pstrArgv)
	{
		// Store config and error/successes for that config
		String strConfig = "";

		if(pstrArgv != null && pstrArgv.length > 2)
		{
			// Get config from the command line
			for(int i = 2; i < pstrArgv.length; i++)
			{
				strConfig += pstrArgv[i] + " ";
			}
		}
		else
		{
			// Query MARF for it's current config
			strConfig = MARF.getConfig();
		}
		
		return strConfig;
	}
	
	/**
	 * Sets default MARF configuration parameters as normalization
	 * for preprocessing, FFT for feature extraction, Euclidean
	 * distance for training and classification with no spectrogram
	 * dumps and no debug information, assuming WAVE file format.
	 *
	 * @throws MARFException
	 * @since 0.3.0.5
	 */
	public static final void setDefaultConfig()
	throws MARFException
	{
		/*
		 * Default MARF setup
		 */
		MARF.setPreprocessingMethod(MARF.DUMMY);
		MARF.setFeatureExtractionMethod(MARF.FFT);
		MARF.setClassificationMethod(MARF.EUCLIDEAN_DISTANCE);
		MARF.setDumpSpectrogram(false);
		MARF.setSampleFormat(MARF.WAV);
//		MARF.setSampleFormat(MARF.TEXT);

		Debug.enableDebug(false);
	}
	
	/**
	 * Customizes MARF's configuration based on the options.
	 * @throws MARFException if some options are out of range
	 * @since 0.3.0.5
	 */
	public static final void setCustomConfig()
	throws MARFException
	{
		ModuleParams oParams = new ModuleParams();
		
		for
		(
			int iPreprocessingMethod = MARF.MIN_PREPROCESSING_METHOD;
			iPreprocessingMethod <= MARF.MAX_PREPROCESSING_METHOD;
			iPreprocessingMethod++
		)
		{
			if(soGetOpt.isActiveOption(iPreprocessingMethod))
			{
				MARF.setPreprocessingMethod(iPreprocessingMethod);
				
				switch(iPreprocessingMethod)
				{
					// Endpointing did not respond well to silence removal
					// and the definition of RAW assumes none of that is done.
					// Endpointing also has some extra params of its own.
					case MARF.RAW:
					case MARF.ENDPOINT:

					case MARF.DUMMY:
					case MARF.HIGH_FREQUENCY_BOOST_FFT_FILTER:
					case MARF.HIGH_PASS_FFT_FILTER:
					case MARF.LOW_PASS_FFT_FILTER:
					case MARF.BANDPASS_FFT_FILTER:
//					case MARF.BAND_STOP_FFT_FILTER:
					case MARF.HIGH_PASS_BOOST_FILTER:
//					case MARF.BAND_PASS_CFE_FILTER:
//					case MARF.BAND_STOP_CFE_FILTER:
//					case MARF.LOW_PASS_CFE_FILTER:
//					case MARF.HIGH_PASS_CFE_FILTER:
					{
						// Normalization and filters seem to respond better to silence removal.
						// The setting of the third protocol parameter (silence threshold)
						// is yet to be implemented here.
						oParams.addPreprocessingParam(new Boolean(soGetOpt.isActiveOption(OPT_NOISE_REMOVAL)));
						oParams.addPreprocessingParam(new Boolean(soGetOpt.isActiveOption(OPT_SILENCE_REMOVAL)));
						break;
					}
					
					default:
					{
						assert false : "Not implemented valid preprocessing configuration parameter: " + iPreprocessingMethod;
					}
				} // switch

				break;
			}
		}

		for
		(
			int iFeatureExtractionMethod = MARF.MIN_FEATUREEXTRACTION_METHOD;
			iFeatureExtractionMethod <= MARF.MAX_FEATUREEXTRACTION_METHOD;
			iFeatureExtractionMethod++
		)
		{
			if(soGetOpt.isActiveOption(iFeatureExtractionMethod))
			{
				MARF.setFeatureExtractionMethod(iFeatureExtractionMethod);
				
				switch(iFeatureExtractionMethod)
				{
					case MARF.FFT:
					case MARF.LPC:
					case MARF.RANDOM_FEATURE_EXTRACTION:
					case MARF.MIN_MAX_AMPLITUDES:
					case MARF.F0:
					case MARF.CEPSTRAL:
					case MARF.SEGMENTATION:
						// For now do nothing; customize when these methods
						// become parametrizable.
						break;
					
					case MARF.FEATURE_EXTRACTION_AGGREGATOR:
					{
						// For now aggregate FFT followed by LPC until
						// it becomes customizable
						oParams.addFeatureExtractionParam(new Integer(MARF.FFT));
						oParams.addFeatureExtractionParam(null);
						oParams.addFeatureExtractionParam(new Integer(MARF.LPC));
						oParams.addFeatureExtractionParam(null);
						break;
					}

					default:
						assert false;
				} // switch

				break;
			}
		}

		for
		(
			int iClassificationMethod = MARF.MIN_CLASSIFICATION_METHOD;
			iClassificationMethod <= MARF.MAX_CLASSIFICATION_METHOD;
			iClassificationMethod++
		)
		{
			if(soGetOpt.isActiveOption(iClassificationMethod))
			{
				MARF.setClassificationMethod(iClassificationMethod);
				
				switch(iClassificationMethod)
				{
					case MARF.NEURAL_NETWORK:
					{
						// Dump/Restore Format of the TrainingSet
						oParams.addClassificationParam(new Integer(TrainingSet.DUMP_GZIP_BINARY));
	
						// Training Constant
						oParams.addClassificationParam(new Double(0.5));
	
						// Epoch number
						oParams.addClassificationParam(new Integer(20));
	
						// Min. error
						oParams.addClassificationParam(new Double(0.1));
						
						break;
					}

//					case MARF.HAMMING_DISTANCE:
//					{
//						// Dump/Restore Format
//						oParams.addClassificationParam(new Integer(TrainingSet.DUMP_GZIP_BINARY));
//
//						// Type of hamming comparison
//						oParams.addClassificationParam(new Integer(HammingDistance.STRICT_DOUBLE));
//
//						break;
//					}

					case MARF.MINKOWSKI_DISTANCE:
					{
						// Dump/Restore Format
						oParams.addClassificationParam(new Integer(TrainingSet.DUMP_GZIP_BINARY));
	
						// Minkowski Factor
						oParams.addClassificationParam(new Double(6.0));

						break;
					}

					case MARF.EUCLIDEAN_DISTANCE:
					case MARF.CHEBYSHEV_DISTANCE:
					case MARF.MAHALANOBIS_DISTANCE:
					case MARF.RANDOM_CLASSIFICATION:
					case MARF.DIFF_DISTANCE:
					case MARF.MARKOV:
//					case MARF.ZIPFS_LAW:
//					case MARF.COSINE_SIMILARITY_MEASURE:
//						// For now do nothing; customize when these methods
//						// become parametrizable.
						break;
					
					default:
						assert false : "Unrecognized classification module";
				} // switch
				
				// Method is found, break out of the look up loop
				break;
			}
		}

		// Assign meaningful params only
		if(oParams.size() > 0)
		{
			MARF.setModuleParams(oParams);
		}
	}
}

// EOF
