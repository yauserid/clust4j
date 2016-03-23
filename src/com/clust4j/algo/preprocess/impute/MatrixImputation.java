package com.clust4j.algo.preprocess.impute;

import java.util.Random;

import com.clust4j.Clust4j;
import com.clust4j.algo.AbstractClusterer;
import com.clust4j.algo.preprocess.PreProcessor;
import com.clust4j.except.NaNException;
import com.clust4j.log.Log;
import com.clust4j.log.LogTimer;
import com.clust4j.log.Loggable;
import com.clust4j.utils.MatUtils;
import com.clust4j.utils.Named;

/**
 * {@link AbstractClusterer} algorithms are not capable of robustly handling
 * missing values (or {@link Double#NaN} in clust4j). If an algorithm is invoked
 * on missing data, a {@link NaNException} will be thrown. To rectify these missing
 * values, this class and its children are designed to impute the missing values
 * using different statistical metrics.
 * 
 * @author Taylor G Smith
 */
public abstract class MatrixImputation extends Clust4j implements Loggable, Named, PreProcessor {
	private static final long serialVersionUID = 8816387041123292806L;
	
	final public static boolean DEF_VERBOSE = AbstractClusterer.DEF_VERBOSE;
	protected boolean verbose = DEF_VERBOSE;
	private Random seed = new Random();
	private boolean hasWarnings = false;
	
	
	public static enum CentralTendencyMethod implements java.io.Serializable {
		MEAN, 
		MEDIAN
	}
	
	
	
	public MatrixImputation(final ImputationPlanner planner) {
		this.verbose = planner.getVerbose();
		this.seed = planner.getSeed();
	}
	
	
	
	abstract static public class ImputationPlanner {
		public ImputationPlanner(){}
		abstract public Random getSeed();
		abstract public boolean getVerbose();
		abstract public ImputationPlanner setSeed(Random rand);
		abstract public ImputationPlanner setVerbose(boolean b);
	}
	
	
	
	
	
	/**
	 * Call this prior to every process call!
	 * @param data
	 */
	protected final void checkMat(final double[][] data) {
		MatUtils.checkDims(data);
		String error;
		final int m = data.length, n = data[0].length;
		
		// Now check column NaN level
		boolean seenNaN = false;
		final double[][] dataCopy = MatUtils.copy(data);
		for(int col = 0; col < n; col++) {
			Inner:
			for(int row = 0; row < m; row++) {
				boolean nan = Double.isNaN(dataCopy[row][col]);
				if(nan) {
					seenNaN =true;
					if(row == m - 1) {
						error = "column " + col + " is entirely NaN";
						error(error);
						throw new NaNException(error);
					}
					
				} else break Inner;
			}
		}
		
		if(!seenNaN) warn("no NaNs in matrix; imputation will not have any effect");
		info("initializing matrix imputation method");
	}
	
	public Random getSeed() {
		return seed;
	}
	
	
	/* -- LOGGER METHODS --  */
	@Override public void error(String msg) {
		if(verbose) Log.err(getLoggerTag(), msg);
	}
	
	@Override public void warn(String msg) {
		hasWarnings = true;
		if(verbose) Log.warn(getLoggerTag(), msg);
	}
	
	@Override public void info(String msg) {
		if(verbose) Log.info(getLoggerTag(), msg);
	}
	
	@Override public void trace(String msg) {
		if(verbose) Log.trace(getLoggerTag(), msg);
	}
	
	@Override public void debug(String msg) {
		if(verbose) Log.debug(getLoggerTag(), msg);
	}
	
	@Override public void wallInfo(LogTimer timer, String info) {
		if(verbose) info(timer.wallMsg() + info);
	}
	
	@Override
	public boolean hasWarnings() {
		return hasWarnings;
	}
	
	/**
	 * Write the time the algorithm took to complete
	 * @param timer
	 */
	@Override public void sayBye(final LogTimer timer) {
		info("imputation task completed in " + timer.toString());
	}
}
