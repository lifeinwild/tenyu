package bei7473p5254d69jcuat.tenyu.release1.global.subjectivity;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.util.*;

/**
 * sharingシャーディング。分担範囲
 * バイナリツリーをイメージする必要がある。
 * 仮想的に、ツリーの末端ノードはハッシュ値と１：１対応しているとする。
 * 最初分担範囲はルートノードにあり、徐々に末端へと向かっていく。
 * ルートノードは全ハッシュ値を担当する事を意味する。
 * １つ下のノードに降りると、分担範囲を意味するビット列に1ビット加えられ、
 * ハッシュ値の先頭1ビットが分担範囲のビット列と一致する必要があるので、
 * 担当するハッシュ値は半分になる。
 * つまり分担範囲のビット列は担当するハッシュ値の接頭辞である。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class AssignedRange {
	/**
	 * 分担範囲のビット列
	 */
	private Bits bits = new Bits();

	@Override
	public AssignedRange clone() throws CloneNotSupportedException {
		AssignedRange r = new AssignedRange();
		r.setBits(bits.clone());
		return r;
	}

	@Override
	public boolean equals(Object arg0) {
		if (!(arg0 instanceof AssignedRange))
			return false;
		AssignedRange o = (AssignedRange) arg0;
		Bits b = o.getBits();
		if (b == null && bits == null)
			return true;
		if (b == null || bits == null)
			return false;

		return bits.equals(b);
	}

	/**
	 * 分担範囲を2分の1にする
	 */
	public boolean descent() {
		boolean b = Glb.getRnd().nextBoolean();
		descent(b);
		return b;
	}

	/**
	 * 分担範囲を2分の1にする
	 * @param bit
	 */
	public void descent(boolean bit) {
		bits.expand(bit);
	}

	/**
	 * 分担範囲を2倍にする
	 */
	public boolean ascent() {
		return bits.contract();
	}

	/**
	 * oはこの分担範囲に含まれるか？この分担範囲はoの祖先か同等か？
	 */
	public boolean isAncestorOrSame(AssignedRange o) {
		if (o == null)
			return false;
		return bits.isPrefixOf(o.getBits());
	}

	/**
	 * oはこの分担範囲に含まれるか？この分担範囲はoの祖先か？
	 */
	public boolean isAncestor(AssignedRange o) {
		return bits.isPrefixOf(o.getBits())
				&& bits.getLastBitIndex() < o.getBits().getLastBitIndex();
	}

	/**
	 * 祖先または子孫または同じか。傍系ではないか。
	 */
	public boolean isAncestorOrDescendants(AssignedRange o) {
		if (o == null)
			return false;
		return isAncestorOrSame(o) || o.isAncestorOrSame(this);
	}

	/**
	 * hashはこの範囲に含まれるか？
	 */
	public boolean support(byte[] hash) {
		if (bits == null)
			return true;
		return bits.isPrefixOf(hash);
	}

	public Bits getBits() {
		return bits;
	}

	public void setBits(Bits bits) {
		this.bits = bits;
	}

	/**
	 * 欠落範囲を探す
	 */
	public static List<AssignedRange> searchLack(List<AssignedRange> ranges,
			int searchDegree, int max) {
		List<AssignedRange> r = new ArrayList<AssignedRange>();

		int hashSize = Glb.getConst().getHashSize();
		byte[] hash = new byte[hashSize];
		next: for (int i = 0; i < searchDegree && r.size() < max; i++) {
			Glb.getRnd().nextBytes(hash);
			for (AssignedRange range : ranges) {
				if (range.support(hash)) {
					continue next;
				}
			}
			//全範囲がこのハッシュに対応していない
			AssignedRange lack = new AssignedRange();
			Bits bits = new Bits();
			bits.set(hashSize * 8 - 1, hash.clone());
			lack.setBits(bits);
			r.add(lack);
		}

		return r;
	}

	public static int getMaxSize() {
		return Glb.getConst().getHashSize() + 4;
	}
}
