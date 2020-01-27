package nz.wicker.bmad.test.visual;
import nz.wicker.bmad.general.*;
import nz.wicker.bmad.matrix.*;

/** Constructs some fractal pattern using monadic matrix methods,
 *  just to check that map, flatMap, toString etc. works well.
 */

public class MatrixVisualTest {
	public static void main(String... _) {
		RowMajor<Character> m = new RowMajor<Character>(1, 1, '#');
		final RowMajor<Character> pattern = new RowMajor<Character>(2, 2, '#');
		pattern.update(1, 1, ' ');
		final RowMajor<Character> zero = new RowMajor<Character>(2, 2, ' ');
		Function<Character, RowMajor<Character>> f = new Function<Character, RowMajor<Character>>() {
			public RowMajor<Character> apply(Character i) {
				return i == ' ' ? zero : pattern;
			}
		};
		
		System.out.println("\nseed: \n" + m);
		System.out.println("\npattern: \n" + pattern);
		
		for (int i = 0; i < 6; i++) {
			m = m.flatMap(f);
			System.out.println("\niter " + (i + 1) + " " + m.getHeight() + "x" + m.getWidth() + "\n" + m);
		}
	}
}
