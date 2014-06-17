package org.kramerlab.bmad.matrix;

import java.util.*;
import static org.kramerlab.bmad.general.Package.*;
import static java.lang.Math.*;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Color;
import java.awt.image.BufferedImage;

import org.kramerlab.bmad.general.*;
import org.kramerlab.bmad.general.layout.*;

public class RowMajor<X> {

	protected ArrayList<ArrayList<X>> entries;
	protected int width;
	protected int height;
	
	// CONSTRUCTORS
	protected RowMajor(int h, int w, ArrayList<ArrayList<X>> entries) {
		this.entries = entries;
		this.width = w;
		this.height = h;
	}
	
	/**
	 * Creates new matrix of the specified size, with entries given by the function <code>entry</code>.
	 * 
	 * @param h
	 * @param w
	 * @param fill
	 */
	public RowMajor(int h, int w, Function<Tuple<Integer, Integer>, X> fill) {
		entries = new ArrayList<ArrayList<X>>();
		for (int r = 0; r < h; r++) {
			ArrayList<X> row = new ArrayList<X>();
			for (int c = 0; c < w; c++) {
				row.add(fill.apply(tuple(r, c)));
			}
			entries.add(row);
		}
		height = h;
		width = w;
	}
	
	/**
	 * Creates new matrix of the specified size, with all entries set to a constant value.
	 * 
	 * @param h
	 * @param w
	 * @param defaultValue
	 */
	public RowMajor(int h, int w, final X defaultValue) {
		this(h, w, org.kramerlab.bmad.general.Package.<Tuple<Integer, Integer>, X>constant(defaultValue));
	}
	
	public static <X> RowMajor<X> asRowVector(ArrayList<X> list) {
		ArrayList<ArrayList<X>> entries = new ArrayList<ArrayList<X>>(list.size());
		entries.add(list);
		return new RowMajor<X>(1, list.size(), entries);
	}
	
	public static <X> RowMajor<X> asColumnVector(ArrayList<X> list) {
		ArrayList<ArrayList<X>> entries = new ArrayList<ArrayList<X>>(list.size());
		for (X x: list) {
			ArrayList<X> row = new ArrayList<X>();
			row.add(x);
			entries.add(row);
		}
		return new RowMajor<X>(list.size(), 1, entries);
	}
	
	// DIMENSIONS, APPLY & UPDATE
	/**
	 * Returns the entry at the specified position.
	 * 
	 * @param row
	 * @param column
	 * @return
	 */
	public X apply(int row, int column) {
		return entries.get(row).get(column);
	}
	
	/**
	 * Gets an entry in the zeroth row of this matrix. 
	 * Should be used only if the matrix is actually a row vector.
	 * 
	 * @param column
	 * @return
	 */
	public X apply(int column) {
		return entries.get(0).get(column);
	}
	
	/**
	 * Overrides the entry at the specified position by a new value.
	 * 
	 * @param row
	 * @param column
	 * @param value
	 */
	public void update(int row, int column, X value) {
		entries.get(row).set(column, value);
	}
	
	/**
	 * Updates an entry in the zeroth row of this matrix.
	 * Should be used only if the matrix is actually a row vertor.
	 * 
	 * @param column
	 * @param value
	 */
	public void update(int column, X value) { 
		entries.get(0).set(column, value);
	}
	
	/**
	 * Returns the width of this matrix
	 * 
	 * @return
	 */
	public int getWidth() {
		return width;
	}
	
	/**
	 * Returns height of this matrix
	 * 
	 * @return
	 */
	public int getHeight() {
		return height;
	}
	
	// FUNCTORIAL AND MONADIC STUFF
	/**
	 * Creates new matrix, where each entry is mapped by the function f
	 * 
	 * @param f
	 * @return
	 */
	public <Y> RowMajor<Y> map(Function<X, Y> f) {
		ArrayList<ArrayList<Y>> e = new ArrayList<ArrayList<Y>>();
		
		for (ArrayList<X> row: entries) {
			ArrayList<Y> newRow = new ArrayList<Y>();
			for (X entry: row) {
				newRow.add(f.apply(entry));
			}
			e.add(newRow);
		}
		
		return new RowMajor<Y>(height, width, e);
	}
	
	/**
	 * Pointwise application of a function that can depend on the value and 
	 * the index. 
	 * 
	 * @param f
	 * @return
	 */
	public <Y> RowMajor<Y> mapWithIndices(Function<Tuple<X, Tuple<Integer, Integer>>, Y> f) {
		ArrayList<ArrayList<Y>> e = new ArrayList<ArrayList<Y>>();
		
		int r = 0;
		for (ArrayList<X> row: entries) {
			ArrayList<Y> newRow = new ArrayList<Y>();
			int c = 0;
			for (X entry: row) {
				newRow.add(f.apply(tuple(entry, tuple(r, c))));
				c++;
			}
			e.add(newRow);
			r++;
		}
		
		return new RowMajor<Y>(height, width, e);
	}
	
	/**
	 * A dense matrix flatMap, that is done about right: it
	 * makes real effort not to calculate anything twice, 
	 * not to allocate more space than necessary, and not to use
	 * dynamic memory allocation of arrayLists, which copies 
	 * entries under the hood.
	 */
	public <Y> RowMajor<Y> flatMap(Function<X, RowMajor<Y>> f) {
		
		if (width == 0 || height == 0) {
			return new RowMajor<Y>(0, 0, new ArrayList<ArrayList<Y>>());
		} else {
		
			// calculate first row and first column, save the submatrices
			ArrayList<RowMajor<Y>> firstRow = new ArrayList<RowMajor<Y>>();
			int totalWidth = 0;
			for (int metacolumn = 0; metacolumn < width; metacolumn ++) {
				RowMajor<Y> m = f.apply(apply(0, metacolumn));
				totalWidth += m.width;
				firstRow.add(m);
			}
			
			int totalHeight = firstRow.get(0).height;
			int[] metarowHeights = new int[height];
			metarowHeights[0] = firstRow.get(0).height;
			ArrayList<RowMajor<Y>> firstColumn = new ArrayList<RowMajor<Y>>();
			firstColumn.add(firstRow.get(0));
			for (int metarow = 1; metarow < height; metarow ++) {
				RowMajor<Y> m = f.apply(apply(metarow, 0));
				metarowHeights[metarow] = m.height;
				totalHeight += m.height;
				firstColumn.add(m);
			}
			
			// allocate enough space: in this case, just empty rows with the right capacity
			ArrayList<ArrayList<Y>> newEntries = new ArrayList<ArrayList<Y>>();
			for (int h: metarowHeights) {
				for (int i = 0; i < h; i++) {
					newEntries.add(new ArrayList<Y>(totalWidth));
				}
			}
			
			
			// rr stands for meta-row index, cc for meta-column
			// r and c index the submatrices
			// totalRow and totalColumn keep track of the actual position in 
			// the big result matrix
			
			// write the first column to the matrix
			// creates something like this:
			//
			// 0 0 0 0 
			// * 0 0 0
			// * 0 0 0
			// * 0 0 0
			//
			int totalRow = metarowHeights[0];
			for (int rr = 1; rr < height; rr++) {
				for (ArrayList<Y> row: firstColumn.get(rr).entries) {
					newEntries.get(totalRow).addAll(row);
					totalRow += 1;
				}
			}
			
			// write the first row into the matrix
			// creates something like this:
			//
			// * * * *
			// * 0 0 0
			// * 0 0 0
			// * 0 0 0
			//
			for (int cc = 0; cc < width; cc++) {
				totalRow = 0;
				for (ArrayList<Y> row: firstRow.get(cc).entries) {
					newEntries.get(totalRow).addAll(row);
					totalRow += 1;
				}
			}
			
			// now fill the remaining space
			// going from left to right from top to bottom
			for (int cc = 1; cc < width; cc ++) {
				totalRow = firstRow.get(0).height;
				for (int rr = 1; rr < height; rr++) {
					for (ArrayList<Y> row: f.apply(apply(rr, cc)).entries) {
						newEntries.get(totalRow).addAll(row);
						totalRow += 1;
					}
				}
			}
			
			return new RowMajor<Y>(totalHeight, totalWidth, newEntries);
		}
	}
	
	/**
	 * Creates string representation of this matrix, indenting the
	 * entries horizontally, but ignoring the fact that some entries might contain
	 * multiple lines: in such cases, the whole layout probably will be pretty botched.
	 * 
	 * @param padding horizontal padding strategy applied to each entry
	 * @param begin prefix of the whole result string
	 * @param end suffix of the whole result string
	 * @param columnSeparator string that is inserted between the padded entries of same row
	 * @param rowSeparator string that separates rows
	 * @return begin + a00 + columnSeparator + ... + columnSeparator + a0(n-1) + rowSeparator + ... + end
	 */
	public String toString(	Padding padding, 
							String begin, 
							String end, 
							String columnSeparator,
							String rowSeparator) {
		
		RowMajor<String> strings = map(new Function<X, String>() {
			public String apply(X x) {
				return x.toString();
			}
		});
		
		int[] maxWidths = new int[width];
		
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				maxWidths[c] = max(maxWidths[c], strings.apply(r, c).length());
			}
		}
		
		StringBuilder b = new StringBuilder();
		
		b.append(begin);
		
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				b.append(padding.padLine(maxWidths[c], ' ', apply(r, c).toString()));
				if (c != width - 1) {
					b.append(columnSeparator);
				}
			}
			if (r != height - 1) {
				b.append(rowSeparator);
			}
		}
		
		b.append(end);
		return b.toString();
	}
	
	protected String toString(final Function<Integer, Padding> columnToHorizontalPadding, final Function<Integer, Padding> rowToVerticalPadding) {
		RowMajor<String> blocks = map(new Function<X, String>() {
			public String apply(X x) {
				return x.toString();
			}
		});
		
		RowMajor<Tuple<Integer, Integer>> blockDims = blocks.map(new Function<String, Tuple<Integer, Integer>>() {
			public Tuple<Integer, Integer> apply(String s) {
				String[] lines = s.split("\n");
				int width = 0;
				for (String line: lines) {
					width = max(width, line.length());
				}
				return tuple(lines.length, width);
			}
		});
		
		final int[] maxWidths = new int[width];
		final int[] maxHeights = new int[height];
		
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				Tuple<Integer, Integer> dim = blockDims.apply(r, c);
				maxWidths[c] = max(maxWidths[c], dim._2);
				maxHeights[r] = max(maxHeights[r], dim._1);
			}
		}
		
		RowMajor<String> paddedBlocks = blocks.mapWithIndices(new Function<Tuple<String, Tuple<Integer, Integer>>, String>() {
			public String apply(Tuple<String, Tuple<Integer, Integer>> t) {
				String str = t._1;
				int r = t._2._1;
				int c = t._2._2;
				return rowToVerticalPadding.apply(r).padBlockVertically(maxHeights[r], ' ',
						columnToHorizontalPadding.apply(c).padBlockHorizontally(maxWidths[c], ' ', str));
			}
		});
		
		return paddedBlocks.toString(new ZeroPadding(), "", "", " ", "\n");
	}
	
	@Override 
	public String toString() {
		/*
		String str = "";
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				str = str + apply(r, c) + " ";
			}
			str = str + "\n";
		}
		return str;
		*/
		
		return toString(org.kramerlab.bmad.general.Package.<Integer, Padding>constant(new Center()), 
						org.kramerlab.bmad.general.Package.<Integer, Padding>constant(new Center()));
	}
	
	/**
	 * Converts this matrix to image.
	 * 
	 * @param toColor function that maps entries to pixel colors.
	 * @return image
	 */
	public Image toImage(Function<X, Color> toColor) {
		BufferedImage img = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics g = img.getGraphics();
		
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				g.setColor(toColor.apply(apply(r, c)));
				g.fillRect(c, r, 1, 1);
			}
		}
		
		return img;
	}
	
	/**
	 * Height and width of the matrix.
	 * 
	 * @return <code>(h, w)</code>
	 */
	public Tuple<Integer, Integer> size() {
		return tuple(height, width);
	}
}