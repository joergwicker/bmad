package nz.wicker.bmad.algorithms;

import nz.wicker.bmad.general.*;
import nz.wicker.bmad.matrix.BooleanMatrix;
import static nz.wicker.bmad.matrix.BooleanMatrix.*;

/**
 * Candidate generator, that just takes rows of the original matrix as
 * candidates. All unknowns are replaced by zeros.
 */
public class IdentityGenerator implements CandidateGenerator {

	public BooleanMatrix generateCandidates(BooleanMatrix a, int dimension) {
		return a.mapBoolean(new Function<Byte, Byte>() {
			public Byte apply(Byte b) {
				return b == TRUE ? TRUE : FALSE;
			}
		});
	}

	@Override
	public String toString() {
		return "Id";
	}
}
