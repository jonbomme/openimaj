package org.openimaj.ml.clustering.spectral;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.openimaj.feature.DoubleFVComparison;
import org.openimaj.io.FileUtils;
import org.openimaj.knn.DoubleNearestNeighbours;
import org.openimaj.knn.DoubleNearestNeighboursExact;
import org.openimaj.logger.LoggerUtils;
import org.openimaj.ml.clustering.SpatialClusterer;
import org.openimaj.ml.clustering.dbscan.ClusterTestDataLoader;
import org.openimaj.ml.clustering.dbscan.ClusterTestDataLoader.TestStats;
import org.openimaj.ml.clustering.dbscan.DBSCANConfiguration;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCAN;
import org.openimaj.ml.clustering.dbscan.DoubleDBSCANClusters;

import ch.akuhn.matrix.SparseMatrix;

/**
 * Test Spectral Clustering implementation using data generated from:
 * http://people.cs.nctu.edu.tw/~rsliang/dbscan/testdatagen.html
 *
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestDoubleNormalisedSpecralClustering {
	private TestStats testStats;
	private double[][] testData;
	private int[][] testClusters;
	/**
	 * @throws IOException
	 */
	@Before
	public void loadTest() throws IOException{
		LoggerUtils.prepareConsoleLogger();
		String[] data = FileUtils.readlines(TestDoubleNormalisedSpecralClustering.class.getResourceAsStream("/org/openimaj/ml/clustering/dbscan/dbscandata"));
		ClusterTestDataLoader loader = new ClusterTestDataLoader();
		this.testStats = loader.readTestStats(data);
		this.testData = loader.readTestData(data);
		this.testClusters = loader.readTestClusters(data);
	}


	/**
	 *
	 */
	@Test
	public void testSimSpatialCluster(){
		DBSCANConfiguration<DoubleNearestNeighbours, double[]> dbsConf = new DBSCANConfiguration<DoubleNearestNeighbours, double[]>(
				0.1, 3,
				new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN)
		);
		SpatialClusterer<DoubleDBSCANClusters,double[]> inner = new DoubleDBSCAN(dbsConf);
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(
			inner
		);
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
		SparseMatrix mat_norm = normalisedSimilarity();
		Clusters res = clust.cluster(mat_norm,true);
		confirmClusters(res);
	}

	/**
	 *
	 */
	@Test
	public void testSimSpatialClusterInverse(){
		DBSCANConfiguration<DoubleNearestNeighbours, double[]> dbsConf = new DBSCANConfiguration<DoubleNearestNeighbours, double[]>( 
				0.1, 3, new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN)
		);
		SpatialClusterer<DoubleDBSCANClusters,double[]> inner = new DoubleDBSCAN(dbsConf);
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(inner, new GraphLaplacian.Normalised());
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
 		SparseMatrix mat_norm = normalisedSimilarity();
		Clusters res = clust.cluster(mat_norm,true);
		confirmClusters(res);
	}
	
	/**
	 *
	 */
	@Test
	public void testSimSpatialClusterInverseHardcoded(){
		DBSCANConfiguration<DoubleNearestNeighbours, double[]> dbsConf = new DBSCANConfiguration<DoubleNearestNeighbours, double[]>( 
				0.2, 2, new DoubleNearestNeighboursExact.Factory(DoubleFVComparison.EUCLIDEAN)
		);
		SpatialClusterer<DoubleDBSCANClusters,double[]> inner = new DoubleDBSCAN(dbsConf);
		SpectralClusteringConf<double[]> conf = new SpectralClusteringConf<double[]>(inner, new GraphLaplacian.Normalised());
//		conf.eigenChooser = new HardCodedEigenChooser(3);
		DoubleSpectralClustering clust = new DoubleSpectralClustering(conf);
		SparseMatrix mat_norm = normalisedSimilarity(Double.MAX_VALUE);
		Clusters res = clust.cluster(mat_norm,true);
		confirmClusters(res);
	}


	private void confirmClusters(Clusters res) {
		for (int i = 0; i < this.testClusters.length; i++) {
			assertTrue(toSet(this.testClusters[i]).equals(toSet(res.clusters()[i])));
		}
	}


	
	private SparseMatrix normalisedSimilarity() {
		return normalisedSimilarity(this.testStats.eps);
	}
	private SparseMatrix normalisedSimilarity(double eps) {
		final SparseMatrix mat = new SparseMatrix(testData.length,testData.length);
		final DoubleFVComparison dist = DoubleFVComparison.EUCLIDEAN;
		double maxD = 0;
		for (int i = 0; i < testData.length; i++) {
			for (int j = i; j < testData.length; j++) {
				double d = dist.compare(testData[i], testData[j]);
				if(d>eps) d = Double.NaN;
				else{
					maxD = Math.max(d, maxD);
				}
				mat.put(i, j, d);
				mat.put(j, i, d);
			}
		}
		SparseMatrix mat_norm = new SparseMatrix(testData.length,testData.length);
		for (int i = 0; i < testData.length; i++) {
			for (int j = i+1; j < testData.length; j++) {
				double d = mat.get(i, j);
				if(Double.isNaN(d)){
					continue;
				}
				else{
					d/=maxD;
				}
				mat_norm.put(i, j, 1-d);
				mat_norm.put(j, i, 1-d);
			}
		}
		return mat_norm;
	}
	private Set<Integer> toSet(int[] is) {
		Set<Integer> set = new HashSet<Integer>();
		for (int i = 0; i < is.length; i++) {
			set.add(is[i]);
		}
		return set;
	}

}
