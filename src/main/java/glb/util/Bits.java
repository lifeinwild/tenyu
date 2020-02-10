package glb.util;

import java.util.*;

import glb.*;

/**
 * bit image of Bits
 * big endian
 * 				byte[2]  byte[1]  byte[0]
 * 	bitIndex=23>00000000 00000000 00000000<bitIndex=0
 *
 * intをbyte[]にする場合等注意しなければならない。
 * ByteBuffer#putInt等でbyte[]に変換すると上位ビットがbyte[]の下位要素になる。
 * bit image of ByteBuffer
 * little endian
 * byte[0]  byte[1]  byte[2]  byte[3]
 * 00000000 00000000 00000000 00000000
 * 数値的には右のビットほど小さく、下位ビットであるが、byte[]の上位要素になる。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Bits {
	private static final byte allStand = (byte) 0b11111111;
	private static final byte allZero = (byte) 0b00000000;

	public static int bitSizeToByteSize(int bitSize) {
		return (bitSize / 8) + (bitSize % 8 == 0 ? 0 : 1);
	}

	public static int size(int lastBitIndex) {
		return lastBitIndex + 1;
	}

	public static int sizeToIndex(int size) {
		return size - 1;
	}

	protected byte[] binary = new byte[1];

	/**
	 * 初期値-1
	 * ビット数1なら0
	 * ビット数2なら1
	 */
	protected int lastBitIndex = -1;

	protected transient final BitUtil u = Glb.getBitUtil();

	@Override
	public Bits clone() throws CloneNotSupportedException {
		Bits r = new Bits();
		r.set(lastBitIndex, binary.clone());
		return r;
	}

	/**
	 * ビット列を縮める
	 * @return	縮めたか
	 */
	public boolean contract() {
		if (lastBitIndex == -1)
			return false;
		lastBitIndex -= 1;
		return true;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Bits other = (Bits) obj;
		if (!Arrays.equals(binary, other.binary))
			return false;
		if (lastBitIndex != other.lastBitIndex)
			return false;
		return true;
	}

	/**
	 * 1ビット増やす
	 */
	public void expand(boolean bit) {
		int nextBitIndex = lastBitIndex + 1;
		binary = u.expandBytes(binary, nextBitIndex);
		lastBitIndex = nextBitIndex;

		//ランダムに降下する
		if (bit) {
			u.stand(binary, lastBitIndex);
		} else {
			u.down(binary, lastBitIndex);
		}
	}

	public List<BitZone> fragmentations() {
		return fragmentations(false, false);
	}

	/**
	 * @param condition	ビットに条件を付けるか
	 * @param bit 0,1どちらのビットの区間を取得するか。condition==falseなら無意味
	 * @return
	 */
	public List<BitZone> fragmentations(boolean condition, boolean bit) {
		List<BitZone> r = new ArrayList<>();
		if (lastBitIndex == -1)
			return r;
		//現在見ているビットが1か
//		boolean oneZone = true;

		BitZone current = null;
		Boolean prev = null;
		for (int i = 0; i <= getLastBitIndex(); i++) {
			Boolean stand = isStand(i);
			//first bit of a zone
			if (prev == null || !prev.equals(stand)) {
				if (current != null) {
					if (!condition || current.isBit() == bit) {
						current.setLastIndex(i - 1);
						r.add(current);
					}
				}
				current = new BitZone();
				current.setBit(stand);
				current.setStartIndex(i);
			}
			prev = stand;
		}

		current.setLastIndex(lastBitIndex);
		if (!condition || current.isBit() == bit) {
			r.add(current);
		}

		return r;
	}

	public byte[] getBinary() {
		return binary;
	}

	/**
	 * @return	ビットが0である最小のbitIndex。ただし0が無い場合-1
	 */
	public int getFirstZero() {
		for (int i = 0; i < binary.length; i++) {
			byte b = binary[i];
			if (b != allStand) {
				int basePos = i * 8;
				for (int pos = basePos; pos < basePos + 8
						&& pos <= lastBitIndex; pos++) {
					if (!isStand(pos)) {
						return pos;
					}
				}
			}
		}
		return -1;
	}

	public int getLastBitIndex() {
		return lastBitIndex;
	}

	/**
	 * @return	ビットが0である最大のbitIndex。ただし0が無い場合-1
	 */
	public int getLastZero() {
		for (int i = binary.length - 1; i >= 0; i--) {
			byte b = binary[i];
			if (b != allStand) {
				int basePos = i * 8;
				int pos = basePos + 8 - 1;
				if (pos > lastBitIndex)
					pos = lastBitIndex;
				for (; pos >= basePos; pos--) {
					if (!isStand(pos)) {
						return pos;
					}
				}
			}
		}
		return -1;
	}

	/**
	 * @return	0のビット数
	 */
	public int getZeroBitCount() {
		if (lastBitIndex == -1)
			return 0;
		return lastBitIndex + 1 - u.countBits(binary);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(binary);
		result = prime * result + lastBitIndex;
		return result;
	}

	/**
	 * @param bit
	 * @return	全ビットがbitか
	 */
	private boolean isFilled(boolean bit) {
		byte val = bit ? allStand : allZero;
		if (lastBitIndex == -1)
			return true;
		for (int i = 0; i < binary.length - 1; i++) {
			byte b = binary[i];
			if (b != val) {
				return false;
			}
		}

		for (int i = binary.length * Byte.BYTES; i <= lastBitIndex; i++) {
			if (isStand(i) != bit) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return	全ビットが1か。ビット数が0の場合true
	 */
	public boolean isFilledOne() {
		return isFilled(true);
	}

	/**
	 * @return	全ビットが0か。ビット数が0の場合true
	 */
	public boolean isFilledZero() {
		return isFilled(false);
	}

	/**
	 * @return	断片化しているか
	 */
	public boolean isFragmentation() {
		if (lastBitIndex == -1)
			return false;
		//現在見ているビットが1か
		boolean oneZone = true;
		//1の後に0が来た回数
		int zeroZoneCount = 0;

		for (int i = 0; i <= getLastBitIndex(); i++) {
			boolean stand = isStand(i);
			if (stand) {
				if (oneZone) {

				} else {
					oneZone = true;
				}
			} else {
				if (oneZone) {
					oneZone = false;
					zeroZoneCount++;
					if (zeroZoneCount >= 2)
						return true;
				} else {

				}
			}
		}

		return false;
	}

	/**
	 * thisはoの接頭辞か？
	 */
	public boolean isPrefixOf(Bits o) {
		return isPrefixOf(o.getBinary());
	}

	public boolean isPrefixOf(byte[] hash) {
		//1ビットも進められていない場合
		if (lastBitIndex == -1)
			return true;

		if (hash == null || hash.length == 0)
			return false;

		//hashの前方部分を切り出す
		byte[] hashPrefix = Arrays.copyOf(hash, binary.length);
		//hashの末尾のバイトについてlastBitIndexを超えるビットを消す
		int hashLastByteIndex = hashPrefix.length - 1;
		//ビット列は右端をbitIndex=0として左に進んでいくイメージ
		hashPrefix[hashLastByteIndex] = u
				.clearLeft(hashPrefix[hashLastByteIndex], lastBitIndex % 8);
		//binaryは進んだ後戻る場合があるので、lastBitIndexの位置までクリアする必要がある。
		byte[] tmpBinary = Arrays.copyOf(binary, hashPrefix.length);
		tmpBinary[hashLastByteIndex] = u.clearLeft(tmpBinary[hashLastByteIndex],
				lastBitIndex % 8);

		return Arrays.equals(tmpBinary, hashPrefix);
	}

	/**
	 * bitIndexの位置のビットが立っているか。
	 * bitIndexがlastBitIndexを超えている場合、返り値は無意味である。
	 */
	public boolean isStand(int bitIndex) {
		return u.isStand(binary, bitIndex);
	}

	public void set(int lastBitIndex) {
		set(lastBitIndex, new byte[bitSizeToByteSize(size(lastBitIndex))]);
	}

	public void set(int lastBitIndex, byte[] binary) {
		this.lastBitIndex = lastBitIndex;
		if (binary != null)
			this.binary = binary;
	}

	/**
	 * @return	0と1両方の全ビット数。lastBitIndex+1
	 */
	public int size() {
		return size(lastBitIndex);
	}

	public void stand(int bitIndex) {
		u.stand(binary, bitIndex);
	}

	public static class BitZone {
		/**
		 * 内容的に連続しているビット区間をまとめる
		 * 可能な全ての連結が行われる保証は無い
		 * @param zones	重複していないビット区間
		 * @return	一度でも連結されたか
		 */
		public static boolean concat(List<BitZone> zones) {
			boolean r = false;
			//ソートする
			zones.sort(Comparator.comparingInt(BitZone::getStartIndex));

			//連結可能な区間は必ずリスト上で連続しているという前提がある
			for (int i = 0; i < zones.size(); i++) {
				BitZone bz1 = zones.get(i);
				for (int j = i + 1; j < zones.size(); j++) {
					BitZone bz2 = zones.get(j);
					if (bz1.tryConcat(bz2)) {
						zones.remove(bz2);
						j--;
						r = true;
					} else {
						break;
					}
				}
			}

			return r;
		}

		private boolean bit;
		private int lastIndex;

		private int startIndex;

		/**
		 * @param drawer	これと重複する部分が抜き取られる
		 * @param zones
		 * @param sizeLimit		サイズ制限。-1は無制限。
		 * @return			抜き取られた部分
		 */
		public BitZone draw(BitZone drawer, int sizeLimit,
				List<BitZone> zones) {
			if (drawer.isBit() != bit)
				return null;
			int start = drawer.getStartIndex();
			int last = drawer.getLastIndex();
			return draw(start, last, sizeLimit, zones);
		}

		/**
		 * このビット区間の一部を抜き出す
		 * @param drawerStart
		 * @param drawerLast
		 * @param zones	前後半の余りの部分が新たにBitZoneとして作られ追加される
		 * @return	抜き取られた部分
		 */
		public BitZone draw(int drawerStart, int drawerLast, int sizeLimit,
				List<BitZone> zones) {
			if (sizeLimit == 0)
				return null;
			int drawerSize = drawerLast - drawerStart + 1;
			if (drawerSize <= 0)
				return null;

			if (drawerStart > lastIndex || drawerLast < startIndex)
				return null;

			int rStart = -1;
			int rLast = -1;

			if (startIndex > drawerStart) {
				rStart = startIndex;
			} else {
				rStart = drawerStart;
			}

			if (lastIndex < drawerLast) {
				rLast = lastIndex;
			} else {
				rLast = drawerLast;
			}

			if (sizeLimit != -1) {
				int rSize = rLast - rStart + 1;
				if (rSize > sizeLimit) {
					//できるだけzonesへの断片化した区間の登録を減らすため
					if (rStart <= startIndex) {
						//rLastを移動させる
						rLast = rStart + sizeLimit - 1;
					} else if (rLast >= lastIndex) {
						//rStartを移動させる
						rStart = rLast - sizeLimit + 1;
					} else {
						//rLastを移動させる
						rLast = rStart + sizeLimit - 1;
					}
					if (rLast < rStart)
						return null;
				}
			}

			BitZone r = new BitZone();
			r.setStartIndex(rStart);
			r.setBit(bit);
			r.setLastIndex(rLast);

			//前後余り部分の追加
			if (zones != null) {
				//前半余り部分
				if (rStart > startIndex) {
					BitZone front = new BitZone();
					front.setBit(bit);
					front.setStartIndex(startIndex);
					front.setLastIndex(rStart - 1);
					if (front.validate())
						zones.add(front);
				}

				//後半余り部分
				if (rLast < lastIndex) {
					BitZone back = new BitZone();
					back.setBit(bit);
					back.setStartIndex(rLast + 1);
					back.setLastIndex(lastIndex);
					if (back.validate())
						zones.add(back);
				}
			}

			return r;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			BitZone other = (BitZone) obj;
			if (bit != other.bit)
				return false;
			if (lastIndex != other.lastIndex)
				return false;
			if (startIndex != other.startIndex)
				return false;
			return true;
		}

		public int getLastIndex() {
			return lastIndex;
		}

		public int getSameCount(BitZone o) {
			if (bit != o.isBit())
				return 0;
			return getSameCountInternal(o);
		}

		private int getSameCountInternal(BitZone o) {
			int start = startIndex > o.getStartIndex() ? startIndex
					: o.getStartIndex();
			int last = lastIndex < o.getLastIndex() ? lastIndex
					: o.getLastIndex();
			int r = last - start + 1;
			if (r < 0)
				return 0;

			return r;
		}

		/**
		 * そのビット区間が意味しているビットを反転させて判定する
		 * @param o
		 * @return	重複しているビット数
		 */
		public int getSameCountInverted(BitZone o) {
			if (bit == o.isBit())
				return 0;
			return getSameCountInternal(o);
		}

		public int getStartIndex() {
			return startIndex;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (bit ? 1231 : 1237);
			result = prime * result + lastIndex;
			result = prime * result + startIndex;
			return result;
		}

		public boolean isBit() {
			return bit;
		}

		/**
		 * @param o
		 * @return	oとthisは連続しているか
		 */
		public boolean isSerial(BitZone o) {
			if (o == null)
				return false;
			if (startIndex == o.getLastIndex() + 1)
				return true;
			if (lastIndex + 1 == o.getStartIndex())
				return true;
			return false;
		}

		public void setBit(boolean bit) {
			this.bit = bit;
		}

		public void setLastIndex(int lastIndex) {
			this.lastIndex = lastIndex;
		}

		public void setStartIndex(int startIndex) {
			this.startIndex = startIndex;
		}

		/**
		 * @return	ビット数
		 */
		public int size() {
			int r = lastIndex - startIndex + 1;
			if (r < 0)
				return 0;
			return r;
		}

		@Override
		public String toString() {
			return "startIndex=" + startIndex + " lastIndex=" + lastIndex;
		}

		/**
		 * 連結を試みる
		 * @param o	連結対象
		 * @return	連結されたか
		 */
		public boolean tryConcat(BitZone o) {
			if (bit != o.isBit())
				return false;
			int type = -1;
			//oが先で連続
			if (startIndex == o.getLastIndex() + 1)
				type = 0;
			//thisが先で連続
			if (lastIndex + 1 == o.getStartIndex())
				type = 1;
			//非連続の場合
			if (type == -1)
				return false;

			if (type == 0) {
				startIndex = o.getStartIndex();
			} else if (type == 1) {
				lastIndex = o.getLastIndex();
			}
			return true;
		}

		/**
		 * このメソッドがtrueを返す事はこのクラスのメソッドは正常に動作するための必要条件
		 * @return
		 */
		public boolean validate() {
			if (startIndex < 0 || startIndex > lastIndex) {
				return false;
			}
			return true;
		}
	}
}
