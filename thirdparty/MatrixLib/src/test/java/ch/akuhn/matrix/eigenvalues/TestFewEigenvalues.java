package ch.akuhn.matrix.eigenvalues;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import ch.akuhn.matrix.DenseMatrix;
import ch.akuhn.matrix.Vector;

import com.jmatio.io.MatFileReader;
import com.jmatio.types.MLArray;
import com.jmatio.types.MLDouble;

/**
 * Perform an eigen decomposition on a matrix with a known result, compare
 * @author Sina Samangooei (ss@ecs.soton.ac.uk)
 *
 */
public class TestFewEigenvalues {
	/**
	 * the output folder
	 */
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private DenseMatrix L;
	private DenseMatrix evec;
	private Vector eval;
	
	@Before
	public void before() throws IOException{
		MatFileReader reader = new MatFileReader(TestFewEigenvalues.class.getResourceAsStream("test_eig.mat"));
		Map<String, MLArray> content = reader.getContent();
		L = fromMLArray((MLDouble) content.get("L"));
		evec = fromMLArray((MLDouble) content.get("evec"));
		eval = fromMLArray(((MLDouble) content.get("eval"))).column(0);
	}
	
	private DenseMatrix fromMLArray(MLDouble mlArray) {
		DenseMatrix ret = new DenseMatrix(mlArray.getArray());
		return ret ;
	}

	@Test
	public void testfeweig() throws Exception {
		FewEigenvalues eig = new FewEigenvalues(L.columnCount()) {
			@Override
			protected Vector callback(Vector vector) {
				return L.mult(vector);
			}
		}.greatest(3);
		Eigenvalues ev = eig.run();
		
		System.out.println(Arrays.toString(ev.value));
		double[] evals = new double[eval.size()];
		eval.storeOn(evals, 0);
		Arrays.sort(evals);
		
		System.out.println(Arrays.toString(evals));
	}
}
