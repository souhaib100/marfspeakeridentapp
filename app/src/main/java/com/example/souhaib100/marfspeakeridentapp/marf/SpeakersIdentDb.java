package com.example.souhaib100.marfspeakeridentapp.marf;

import net.windward.android.awt.Point;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DecimalFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import marf.Storage.Database;
import marf.Storage.StorageException;
import marf.util.Arrays;
import marf.util.Debug;


/**
 * <p>Class SpeakersIdentDb manages database of speakers on the application level.</p>
 * <p>XXX: Move stats collection over to MARF.</p>
 *
 * $Id: SpeakersIdentDb.java,v 1.26 2012/07/18 02:45:45 mokhov Exp $
 *
 * @author Serguei Mokhov
 * @version $Revision: 1.26 $
 * @since 0.0.1
 */
public class SpeakersIdentDb
extends Database
{
	/**
	 * Hashes "config string" -&gt; Vector(FirstMatchPoint(XSuccesses, YFailures),
	 * SecondMatchPoint(XSuccesses, YFailures)).
	 */
	private Hashtable oStatsPerConfig = null;

	/**
	 * Array of sorted stats refs.
	 */
	private transient Vector[] aoSortedStatsRefs = null;

	/**
	 * A vector of vectors of speakers info pre-loded on <code>connect()</code>.
	 * @see #connect()
	 */
	private Hashtable oDB = null;

	/**
	 * "Database connection".
	 */
	private BufferedReader oConnection = null;

	/**
	 * For serialization versioning.
	 * @since 0.3.0.5
	 */
	private static final long serialVersionUID = -7185805363856188810L;

	/**
	 * Constructor.
	 * @param pstrFileName filename of a CSV file with IDs and names of speakers
	 */
	public SpeakersIdentDb(final String pstrFileName)
	{
		this.strFilename = pstrFileName;
		this.oDB = new Hashtable();
		this.oStatsPerConfig = new Hashtable();
	}

	/**
	 * Retrieves Speaker's ID by a sample filename.
	 * @param pstrFileName Name of a .wav file for which ID must be returned
	 * @param pbTraining indicates whether the filename is a training (<code>true</code>) sample or testing (<code>false</code>)
	 * @return int ID
	 * @throws StorageException in case of an error in any I/O operation
	 */
	public final int getIDByFilename(final String pstrFileName, final boolean pbTraining) {
		String strFilenameToLookup;

		// Extract actual file name without preceding path (if any)
		if(pstrFileName.lastIndexOf('/') >= 0)
		{
			strFilenameToLookup = pstrFileName.substring(pstrFileName.lastIndexOf('/') + 1, pstrFileName.length());
		}
		else if(pstrFileName.lastIndexOf('\\') >= 0)
		{
			strFilenameToLookup = pstrFileName.substring(pstrFileName.lastIndexOf('\\') + 1, pstrFileName.length());
		}
		else
		{
			strFilenameToLookup = pstrFileName;
		}

		Enumeration<Integer> oIDs = this.oDB.keys();

		// Traverse all the info vectors looking for sample filename
		while(oIDs.hasMoreElements())
		{
			Integer oID = oIDs.nextElement();

			Debug.debug("File: " + pstrFileName + ", id = " + oID.intValue());

			Vector oSpeakerInfo = (Vector)this.oDB.get(oID);
			Vector<String> oFilenames;

			if(pbTraining == true)
			{
				oFilenames = (Vector)oSpeakerInfo.elementAt(1);
			}
			else
			{
				oFilenames = (Vector)oSpeakerInfo.elementAt(2);
			}

			// Start from 1 because 0 is speaker's name
			for(int i = 0; i < oFilenames.size(); i++)
			{
				String strCurrentFilename = oFilenames.elementAt(i);

				if(strCurrentFilename.equals(strFilenameToLookup))
				{
					return oID.intValue();
				}
			}
		}

		return -1;
	}

	/**
	 * Retrieves speaker's name by their ID.
	 * @param piID ID of a person in the DB to return a name for
	 * @return name string
	 * @throws StorageException
	 */
	public final String getName(final int piID) {
		//Debug.debug("getName() - ID = " + piID + ", db size: " + oDB.size());
		String strName;

		Vector oDBEntry = (Vector)this.oDB.get(piID);

		if(oDBEntry == null)
		{
			strName = "Unknown Speaker (" + piID + ")";
		}
		else
		{
			strName = (String)oDBEntry.elementAt(0);
		}

		return strName;
	}

	/**
	 * Connects to the "database" of speakers (opens the text file :-)).
	 * @throws StorageException in case of any I/O error
	 */
	public void connect()
	throws StorageException
	{
		// That's where we should establish file linkage and keep it until closed
		try
		{
			this.oConnection = new BufferedReader(new FileReader(this.strFilename));
			this.bConnected = true;
		}
		catch(IOException e)
		{
			throw new StorageException
			(
				"Error opening speaker DB: \"" + this.strFilename + "\": " +
				e.getMessage() + "."
			);
		}
	}

	/**
	 * Retrieves speaker's data from the text file and populates
	 * internal data structures. Uses StringTokenizer to parse
	 * data read from the file.
	 * @throws StorageException in case of any I/O error
	 */
	public void query()
	throws StorageException
	{
		// That's where we should load db results into internal data structure

		String strLine;
		int iID = -1;

		try
		{
			strLine = this.oConnection.readLine();

			while(strLine != null)
			{
				StringTokenizer oTokenizer = new StringTokenizer(strLine, ",");
				Vector oSpeakerInfo = new Vector();

				// get ID
				if(oTokenizer.hasMoreTokens())
				{
					iID = Integer.parseInt(oTokenizer.nextToken());
				}

				// speaker's name
				if(oTokenizer.hasMoreTokens())
				{
					strLine = oTokenizer.nextToken();
					oSpeakerInfo.add(strLine);
				}

				// training file names
				Vector oTrainingFilenames = new Vector();

				if(oTokenizer.hasMoreTokens())
				{
					StringTokenizer oSTK = new StringTokenizer(oTokenizer.nextToken(), "|");

					while(oSTK.hasMoreTokens())
					{
						strLine = oSTK.nextToken();
						oTrainingFilenames.add(strLine);
					}
				}

				oSpeakerInfo.add(oTrainingFilenames);

				// testing file names
				Vector oTestingFilenames = new Vector();

				if(oTokenizer.hasMoreTokens())
				{
					StringTokenizer oSTK = new StringTokenizer(oTokenizer.nextToken(), "|");

					while(oSTK.hasMoreTokens())
					{
						strLine = oSTK.nextToken();
						oTestingFilenames.add(strLine);
					}
				}

				oSpeakerInfo.add(oTestingFilenames);

				Debug.debug("Putting ID=" + iID + " along with info vector of size " + oSpeakerInfo.size());

				this.oDB.put(new Integer(iID), oSpeakerInfo);

				strLine = this.oConnection.readLine();
			}
		}
		catch(IOException e)
		{
			throw new StorageException
			(
				"Error reading from speaker DB: \"" + this.strFilename +
				"\": " + e.getMessage() + "."
			);
		}
	}

	/**
	 * Closes (file) database connection.
	 * @throws StorageException if not connected or fails to close inner reader
	 */
	public void close()
	throws StorageException
	{
		// Close file
		if(this.bConnected == false)
		{
			throw new StorageException("SpeakersIdentDb.close() - not connected");
		}

		try
		{
			this.oConnection.close();
			this.bConnected = false;
		}
		catch(IOException e)
		{
			throw new StorageException(e.getMessage());
		}
	}

	/**
	 * Adds one more classification statics entry.
	 * @param pstrConfig String representation of the configuration the stats are for
	 * @param pbSuccess <code>true</code> if classification was successful; <code>false</code> otherwise
	 */
	public final void addStats(final String pstrConfig, final boolean pbSuccess)
	{
		addStats(pstrConfig, pbSuccess, false);
	}

	/**
	 * Adds one more classification statics entry and accounts for the second best choice.
	 * @param pstrConfig String representation of the configuration the stats are for
	 * @param pbSuccess <code>true</code> if classification was successful; <code>false</code> otherwise
	 * @param pbSecondBest <code>true</code> if classification was successful; <code>false</code> otherwise
	 */
	public final void addStats(final String pstrConfig, final boolean pbSuccess, final boolean pbSecondBest)
	{
		Vector oMatches = (Vector)this.oStatsPerConfig.get(pstrConfig);
		Point oPoint = null;

		if(oMatches == null)
		{
			oMatches = new Vector(2);
			oMatches.add(new Point());
			oMatches.add(new Point());
			oMatches.add(pstrConfig);
		}
		else
		{
			if(pbSecondBest == false)
			{
				// First match
				oPoint = (Point)oMatches.elementAt(0);
			}
			else
			{
				// Second best match
				oPoint = (Point)oMatches.elementAt(1);
			}
		}

		int iSuccesses = 0; // # of successes
		int iFailures = 0; // # of failures

		if(oPoint == null) // Didn't exist yet; create new
		{
			if(pbSuccess == true)
			{
				iSuccesses = 1;
			}
			else
			{
				iFailures = 1;
			}

			oPoint = new Point(iSuccesses, iFailures);

			if(oMatches.size() == 0)
			{
				System.err.println("SpeakersIdentDb.addStats() - oMatches.size = 0");
				System.exit(-1);
			}

			if(pbSecondBest == false)
			{
				oMatches.setElementAt(oPoint, 0);
			}
			else
			{
				oMatches.setElementAt(oPoint, 1);
			}

			this.oStatsPerConfig.put(pstrConfig, oMatches);
		}

		else // There is an entry for this config; update
		{
			if(pbSuccess == true)
			{
				oPoint.x++;
			}
			else
			{
				oPoint.y++;
			}
		}
	}

	/**
	 * Dumps all collected statistics to STDOUT.
	 * @throws Exception
	 */
	public final void printStats()
	throws Exception
	{
		printStats(false);
	}

	/**
	 * Dumps collected statistics to STDOUT.
	 * @param pbBestOnly <code>true</code> - print out only the best score number; <code>false</code> - all stats
	 * @throws Exception
	 */
	public final void printStats(boolean pbBestOnly) {
		if(this.oStatsPerConfig.size() == 0)
		{
			System.err.println("SpeakerIdentDb: no statistics available. Did you run the recognizer yet?");
			return;
		}

		// First row is for the identified results, 2nd is for 2nd best ones.
		String[][] astrResults = new String[2][this.oStatsPerConfig.size()];

		this.aoSortedStatsRefs = (Vector[])oStatsPerConfig.values().toArray(new Vector[0]);
		Arrays.sort(this.aoSortedStatsRefs, new StatsPercentComparator(StatsPercentComparator.DESCENDING));

		int iResultNum = 0;

		System.out.println("guess,run,config,good,bad,%");

		for(int i = 0; i < this.aoSortedStatsRefs.length; i++)
		{
			String strConfig = (String)(this.aoSortedStatsRefs[i]).elementAt(2);

			for(int j = 0; j < 2; j++)
			{
				Point oGoodBadPoint = (Point)(this.aoSortedStatsRefs[i]).elementAt(j);
				String strGuess = (j == 0) ? "1st" : "2nd";
				String strRun = (iResultNum + 1) + "";
				DecimalFormat oFormat = new DecimalFormat("#,##0.00;(#,##0.00)");
				double dRate = ((double)oGoodBadPoint.x / (double)(oGoodBadPoint.x + oGoodBadPoint.y)) * 100;

				if(pbBestOnly == true)
				{
					System.out.print(oFormat.format(dRate));
					return;
				}

				astrResults[j][iResultNum] =
					strGuess + "," +
					strRun + "," +
					strConfig + "," +
					oGoodBadPoint.x + "," + // Good
					oGoodBadPoint.y + "," + // Bad
					oFormat.format(dRate);
			}

			iResultNum++;
		}

		// Print all of the 1st match
		for(int i = 0; i < astrResults[0].length; i++)
		{
			System.out.println(astrResults[0][i]);
		}

		// Print all of the 2nd match
		for(int i = 0; i < astrResults[1].length; i++)
		{
			System.out.println(astrResults[1][i]);
		}
	}

	/**
	 * Resets in-memory and on-disk statistics.
	 * @throws StorageException
	 */
	public final void resetStats()
	throws StorageException
	{
		this.oStatsPerConfig.clear();
		dump();
	}

	/**
	 * Dumps statistic's Hashtable object as gzipped binary to disk.
	 * @throws StorageException
	 */
	public void dump()
	throws StorageException
	{
		try
		{
			FileOutputStream oFOS = new FileOutputStream(this.strFilename + ".stats");
			GZIPOutputStream oGZOS = new GZIPOutputStream(oFOS);
			ObjectOutputStream oOOS = new ObjectOutputStream(oGZOS);

			oOOS.writeObject(this.oStatsPerConfig);
			oOOS.flush();
			oOOS.close();
		}
		catch(Exception e)
		{
			throw new StorageException(e);
		}
	}

	/**
	 * Reloads statistic's Hashtable object from disk.
	 * If the file did not exist, it creates a new one.
	 * @throws StorageException
	 */
	public void restore()
	throws StorageException 
	{
		try
		{
			FileInputStream oFIS = new FileInputStream(this.strFilename + ".stats");
			GZIPInputStream oGZIS = new GZIPInputStream(oFIS);
			ObjectInputStream oOIS = new ObjectInputStream(oGZIS);

			this.oStatsPerConfig = (Hashtable)oOIS.readObject();
			oOIS.close();
		}
		catch(FileNotFoundException e)
		{
			System.out.println
			(
				"NOTICE: File " + this.strFilename +
				".stats does not seem to exist. Creating a new one...."
			);

			resetStats();
		}
		catch(ClassNotFoundException e)
		{
			throw new StorageException
			(
				"SpeakerIdentDb.retore() - ClassNotFoundException: " +
				e.getMessage()
			);
		}
		catch(Exception e)
		{
			throw new StorageException(e);
		}
	}
}

/**
 * <p>Used in sorting by percentage of the stats entries
 * in either ascending or descending order.</p>
 *
 * <p>TODO: To be moved to Stats.</p>
 *
 * @author Serguei Mokhov
 * @version $Revision: 1.26 $
 * @since 0.0.1 of MARF
 */
class StatsPercentComparator
extends marf.util.SortComparator
{
	/**
	 * For serialization versioning.
	 * @since 0.3.0.5
	 */
	private static final long serialVersionUID = -7185805363856188810L;

	/**
	 * Mimics parent's constructor.
	 */
	public StatsPercentComparator()
	{
		super();
	}

	/**
	 * Mimics parent's constructor.
	 * @param piSortMode either DESCENDING or ASCENDING sort mode 
	 */
	public StatsPercentComparator(final int piSortMode)
	{
		super(piSortMode);
	}

	/**
	 * Implementation of the Comparator interface for the stats objects.
	 * @see java.util.Comparator#compare(Object, Object)
	 */
	public int compare(Object poStats1, Object poStats2)
	{
		Vector oStats1 = (Vector)poStats1;
		Vector oStats2 = (Vector)poStats2;

		Point oGoodBadPoint1 = (Point)(oStats1.elementAt(0));
		Point oGoodBadPoint2 = (Point)(oStats2.elementAt(0));

		double dRate1 = ((double)oGoodBadPoint1.x / (double)(oGoodBadPoint1.x + oGoodBadPoint1.y)) * 100;
		double dRate2 = ((double)oGoodBadPoint2.x / (double)(oGoodBadPoint2.x + oGoodBadPoint2.y)) * 100;

		switch(this.iSortMode)
		{
			case DESCENDING:
			{
				return (int)((dRate2 - dRate1) * 100);
			}

			case ASCENDING:
			default:
			{
				return (int)((dRate1 - dRate2) * 100);
			}
		}
	}
}

// EOF
