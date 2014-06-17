package org.kramerlab.bmad.algorithms;

import org.kramerlab.bmad.general.*;
import org.kramerlab.bmad.matrix.BooleanMatrix;
import static org.kramerlab.bmad.matrix.BooleanMatrix.*;

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
