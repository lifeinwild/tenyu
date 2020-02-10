package glb.util;

import java.util.*;

import me.lemire.integercompression.differential.*;

public class IDList {
	/**
	 * long値を扱う工夫
	 * 圧縮直前にこれが引かれる
	 * 解凍直後にこれが足される
	 *
	 * idの範囲が広すぎるとintで扱えないので破綻する
	 */
	private long base;
	/**
	 * https://github.com/lemire/JavaFastPFOR
	 * TODO 最大サイズを特定できない
	 */
	private int[] compressed;

	private IDList() {
	}

	/**
	 * @return	圧縮後配列のバイト数
	 */
	public long size() {
		return compressed.length;
	}

	private IDList(long[] sortedIds) {
		if (sortedIds == null || sortedIds.length == 0
				|| sortedIds.length >= Integer.MAX_VALUE)
			throw new IllegalArgumentException();
		base = sortedIds[0];
		int[] basedIds = new int[sortedIds.length];
		for (int i = 0; i < sortedIds.length; i++) {
			long tmp = sortedIds[i] - base;
			if (tmp > Integer.MAX_VALUE || tmp < 0) {
				throw new IllegalArgumentException();
			}
			basedIds[i] = (int) (tmp);
		}
		IntegratedIntCompressor iic = new IntegratedIntCompressor();
		compressed = iic.compress(basedIds);
	}

	private IDList(List<Long> sortedIds) {
		if (sortedIds == null || sortedIds.size() == 0
				|| sortedIds.size() >= Integer.MAX_VALUE)
			throw new IllegalArgumentException();
		base = sortedIds.get(0);
		int[] basedIds = new int[sortedIds.size()];
		for (int i = 0; i < sortedIds.size(); i++) {
			long tmp = sortedIds.get(i) - base;
			if (tmp > Integer.MAX_VALUE || tmp < 0) {
				throw new IllegalArgumentException();
			}
			basedIds[i] = (int) (tmp);
		}
		IntegratedIntCompressor iic = new IntegratedIntCompressor();
		compressed = iic.compress(basedIds);
	}

	public long[] uncompress() {
		IntegratedIntCompressor iic = new IntegratedIntCompressor();
		int[] basedIds = iic.uncompress(compressed);
		long[] sortedIds = new long[basedIds.length];
		for (int i = 0; i < basedIds.length; i++) {
			sortedIds[i] = basedIds[i] + base;
		}
		return sortedIds;
	}

	/**
	 * 内部で新たにHashSetを作るタイプ
	 * @return
	 */
	public HashSet<Long> uncompressToHashSet() {
		return uncompressToHashSet(new HashSet<>());
	}

	/**
	 * 外部から渡されたHashSetに追加するタイプ
	 * @param s
	 * @return		s
	 */
	public HashSet<Long> uncompressToHashSet(HashSet<Long> s) {
		IntegratedIntCompressor iic = new IntegratedIntCompressor();
		int[] basedIds = iic.uncompress(compressed);
		for (int i = 0; i < basedIds.length; i++) {
			s.add(basedIds[i] + base);
		}
		return s;
	}

	public List<Long> uncompressToList() {
		IntegratedIntCompressor iic = new IntegratedIntCompressor();
		int[] basedIds = iic.uncompress(compressed);
		List<Long> r = new ArrayList<>();
		for (int i = 0; i < basedIds.length; i++) {
			r.add(basedIds[i] + base);
		}
		return r;
	}

	public static HashSet<Long> uncompress(List<IDList> ll) {
		HashSet<Long> r = new HashSet<>();
		for (IDList l : ll) {
			l.uncompressToHashSet(r);
		}
		return r;
	}

	/**
	 * 任意の範囲のlong値一覧を圧縮する。
	 * new IDList()では最初の値と最後の値の差がInteger.MAX_VALUE以内でなければならない。
	 * このメソッドはそれを気にしなくていい。
	 *
	 * 注意点として、long値全体に値が分散していると圧縮率が低くなるようだ。
	 * 多くの現実的用途ではせいぜいidは0-10億の範囲で、優れた圧縮率が得られる。
	 *
	 * データ							List<Long>表現に対する圧縮率
	 * long値ランダム100万件			300%
	 * long値上限10億ランダム100万件	5%
	 * long値上限10億ランダム3万件		8%
	 * int値ランダム100万件				6%
	 *
	 * long値上限10億ランダム3万件		27%
	 *
	 * @param sortedIds		昇順ソート済みのlong値一覧
	 * @return				圧縮されたIDListの一覧
	 */
	public static List<IDList> compress(long[] sortedIds) {
		return compress(sortedIds, Integer.MAX_VALUE);
	}

	public static List<IDList> compress(long[] sortedIds, int unitMax) {
		if (sortedIds == null)
			return null;
		List<IDList> r = new ArrayList<>();
		if (sortedIds.length == 0)
			return r;
		Long base = sortedIds[0];
		List<Long> current = new ArrayList<>();
		for (long id : sortedIds) {
			long dif = id - base;
			if (dif > Integer.MAX_VALUE || current.size() >= unitMax) {
				r.add(new IDList(current));
				current.clear();
				base = id;
			}
			current.add(id);
		}
		r.add(new IDList(current));
		return r;
	}

	public static List<IDList> compress(List<Long> sortedIds) {
		return compress(sortedIds, Integer.MAX_VALUE);
	}

	public static List<IDList> compress(List<Long> sortedIds, int unitMax) {
		if (sortedIds == null)
			return null;
		List<IDList> r = new ArrayList<>();
		if (sortedIds.size() == 0)
			return r;
		long base = sortedIds.get(0);
		List<Long> current = new ArrayList<>();
		for (long id : sortedIds) {
			long dif = id - base;
			if (dif > Integer.MAX_VALUE || current.size() >= unitMax) {
				r.add(new IDList(current));
				current.clear();
				base = id;
			}
			current.add(id);
		}
		r.add(new IDList(current));
		return r;
	}

	@Override
	public String toString() {
		return "IDList [base=" + base + ", compressed.size=" + size() + "]";
	}
}
