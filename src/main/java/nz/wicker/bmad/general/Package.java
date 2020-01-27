package nz.wicker.bmad.general;

/**
 * Contains helpful static methods.
 */
public class Package {

	public static <A, B> Tuple<A, B> tuple(A a, B b) {
		return new Tuple<A, B>(a, b); 
	}

	public static <X, Y> Function<X, Y> constant(final Y y) {
		return new Function<X, Y>() {
			public Y apply(X x) {
				return y;
			}
		};
	}
	
	public static <X> Function<X, X> id() {
		return new Function<X, X>() {
			public X apply(X x) {
				return x;
			}
		};
	}
}
