package nz.wicker.bmad.general;

/**
 * @param <X> domain 
 * @param <Y> codomain
 */
public interface Function<X, Y> {
	Y apply(X x);
}