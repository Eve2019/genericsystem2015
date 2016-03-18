package org.genericsystem.kernel;

import java.util.AbstractMap.SimpleEntry;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class Diff<E> implements Iterator<Entry<E, Boolean>> {
	private final int m;
	private final int n;
	private final List<E> elements1;
	private final List<E> elements2;
	private int i = 0;
	private int j = 0;
	private Matrix opt;
	private Entry<E, Boolean> next;

	public Diff(List<E> elements1, List<E> elements2) {
		this.elements1 = elements1;
		this.elements2 = elements2;
		m = elements1.size();
		n = elements2.size();
		opt = new Matrix(m + 1, n + 1);
		if (m > 0 && n > 0) {
			for (int i = m - 1; i >= 0; i--) {
				for (int j = n - 1; j >= 0; j--) {
					E x = elements1.get(i);
					E y = elements2.get(j);
					opt.set(i, j, x == y ? (opt.get(i + 1, j + 1) + 1) : Math.max(opt.get(i + 1, j), opt.get(i, j + 1)));
				}
			}
		}
		next = advance();
	}

	@Override
	public boolean hasNext() {
		return next != null;
	}

	private Entry<E, Boolean> advance() {
		if (i < m && j < n) {
			E e1 = elements1.get(i);
			E e2 = elements2.get(j);
			if (e1 == e2) {
				i++;
				j++;
				return new SimpleEntry<>(e1, null);
			} else if (opt.get(i + 1, j) >= opt.get(i, j + 1)) {
				i++;
				return new SimpleEntry<>(e1, false);
			} else {
				j++;
				return new SimpleEntry<>(e2, true);
			}
		} else if (i < m) {
			E e1 = elements1.get(i);
			i++;
			return new SimpleEntry<>(e1, false);
		} else if (j < n) {
			E e2 = elements2.get(j);
			j++;
			return new SimpleEntry<>(e2, true);
		} else {
			return null;
		}
	}

	@Override
	public Entry<E, Boolean> next() {
		Entry<E, Boolean> result = next;
		next = advance();
		return result;
	}

	class Matrix {
		private final int[] state;

		Matrix(Integer width, Integer height) {
			state = new int[width * height];
			Arrays.fill(state, 0);
		}

		public void set(Integer x, Integer y, Integer e) {
			state[x + y * (m + 1)] = e;
		}

		public int get(Integer x, Integer y) {
			return state[x + y * (m + 1)];
		}
	}

}