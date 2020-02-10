package glb.util;

import java.util.*;

public class BitUtil {
	private static final byte mask = (byte) 0xFF;

	/**
	 * @param pos	0-7 posビット目を含まずそれより右のビットをクリア
	 */
	public byte clearRight(byte b, int pos) {
		return (byte) ((b >>> pos) << pos);
	}

	/**
	 * @param bits	文字列化されるビット列
	 * @return	ビット列の文字列
	 */
	public String printBinary(byte[] bits) {
		StringBuilder sb = new StringBuilder();
		for (byte b : bits) {
			String s = String.format("%8s", Integer.toBinaryString(b & 0xFF))
					.replace(' ', '0');
			sb.insert(0, " " + s);
		}
		return sb.toString();
	}

	/**
	 * @param pos	0-7 posビット目を含まずそれより左のビットをクリア
	 */
	public byte clearLeft(byte b, int pos) {
		return (byte) (b & (mask >>> -(pos + 1)));
	}

	public final byte countBits(byte data) {
		byte tmp = data;
		tmp = (byte) ((tmp & 0x55) + (tmp >> 1 & 0x55));
		tmp = (byte) ((tmp & 0x33) + (tmp >> 2 & 0x33));
		return (byte) ((tmp & 0x0f) + (tmp >> 4 & 0x0f));
	}

	public final int countBits(byte[] data) {
		int res = 0;
		for (byte b : data) {
			res += countBits(b);
		}
		return res;
	}

	public final int countBits(int bits) {
		bits = (bits & 0x55555555) + (bits >> 1 & 0x55555555);
		bits = (bits & 0x33333333) + (bits >> 2 & 0x33333333);
		bits = (bits & 0x0f0f0f0f) + (bits >> 4 & 0x0f0f0f0f);
		bits = (bits & 0x00ff00ff) + (bits >> 8 & 0x00ff00ff);
		return (bits & 0x0000ffff) + (bits >> 16 & 0x0000ffff);
	}

	// get first right position of leading bit
	public final int ntz(int bits) {
		return countBits((~bits) & (bits - 1));
		// return countBits((bits & (-bits)) - 1);
	}

	/**
	 * TIntArrayListバージョンもあるので性能を気にするなら
	 * そのライブラリを導入してそちらを使うべき。
	 *
	 * @param bits
	 * @return	ビットの位置一覧
	 */
	public final List<Integer> getPositions(byte[] bits) {
		List<Integer> r = new ArrayList<>();
		for (int j = 0; j < bits.length; j++) {
			byte b = bits[j];
			byte length = countBits(b);
			for (int i = 0; i < length; i++) {
				byte ntz = ntz(b);
				int index = (ntz & 0xFF) + j * 8;
				r.add(index);
				b = flip(b, ntz);
			}
		}
		return r;
	}

	/**
	 * @param bits
	 * @return returns 8 if no standing bit. 00000001 is 0.
	 */
	public final byte ntz(byte bits) {
		return countBits((byte) ((bits & (-bits)) - (byte) 1));
	}

	/**
	 * @param bits
	 * @param index
	 *            00000001 is 0. 10000000 is 7.
	 * @return
	 */
	public final byte flip(byte bits, byte index) {
		return bits ^= ((byte) 1 << index);
	}

	/**
	 * little endian
	 *
	 * @param b
	 * @return
	 */
	public final int ntz(byte[] b) {
		byte pos = -1;
		for (int i = 0; i < b.length; i++) {
			pos = ntz(b[i]);
			if (pos != 8) {
				return pos + i * 8;
			}
		}
		return -1;
	}

	/**
	 * @param bits
	 * @return 10000000 is 0. 01000000 is 1. 00000001 is 7. return 8 if no
	 *         standing bit.
	 */
	public final static byte nlz(byte bits) {
		byte y;
		byte n = 8;
		y = (byte) (bits >> 4);
		if (y != 0) {
			n = (byte) (n - 4);
			bits = y;
		}
		y = (byte) (bits >> 2);
		if (y != 0) {
			n = (byte) (n - 2);
			bits = y;
		}
		y = (byte) (bits >> 1);
		if (y != 0) {
			return (byte) (n - 2);
		}
		return (byte) (n - bits);
	}

	/**
	 * 0を1、1を1に。
	 *
	 * @param bits
	 * @param bitIndex
	 */
	public void stand(byte[] bits, int bitIndex) {
		int wordIndex = byteIndex(bitIndex);
		bits[wordIndex] |= ((byte) 1 << (bitIndex % 8));
	}

	public void down(byte[] bits, int bitIndex) {
		int wordIndex = byteIndex(bitIndex);
		bits[wordIndex] &= ~((byte) 1 << (bitIndex % 8));
	}

	public boolean isStand(byte[] bits, int bitIndex) {
		int wordIndex = byteIndex(bitIndex);
		if (bits.length <= wordIndex) {
			return false;
		}
		return 0 != (bits[wordIndex] & ((byte) 1 << (bitIndex % 8)));
	}

	/**
	 * 8で割ってバイト列の何バイト目かという数値にする
	 */
	private int byteIndex(int bitIndex) {
		return bitIndex >> 3;
	}

	/**
	 * @param bits1
	 * @param bits2
	 * @return
	 */
	public void and(byte[] bits1, byte[] bits2) {
		for (int i = 0; i < bits1.length && i < bits2.length; i++) {
			bits1[i] &= bits2[i];
		}
	}

	/**
	 * 全ビットを反転させる
	 * @param bits
	 */
	public void not(byte[] bits) {
		for (int i = 0; i < bits.length; i++) {
			bits[i] = (byte) ~bits[i];
		}
	}

	/**
	 * バイト配列を伸長する
	 * @param lastBitIndex	末尾のビット位置。０～７まで１バイトで対応、
	 * ８が指定されると２バイト目が必要。
	 */
	public byte[] expandBytes(byte[] src, int lastBitIndex) {
		//現在のビット数 - 必要なビット数
		int lack = src.length * 8 - (lastBitIndex + 1);
		if (lack < 0) {
			byte[] addition = new byte[Math.abs(lack) / 8 + 1];
			Arrays.fill(addition, (byte) 0);
			src = concat(src, addition);
		}
		return src;
	}

	/**
	 * ２つのバイト配列を連結した新たなバイト配列を返す
	 */
	public byte[] concat(byte[] val1, byte[] val2) {
		byte[] res = new byte[val1.length + val2.length];

		System.arraycopy(val1, 0, res, 0, val1.length);
		System.arraycopy(val2, 0, res, val1.length, val2.length);

		return res;
	}

	/**
	 * ビットを立てる。必要ならビット列を伸ばす
	 * @param bits	ビット列
	 * @param bitIndex	ビット位置
	 * @return		伸長されたりビットを立てられたビット列
	 */
	public byte[] standAndExpand(byte[] bits, int bitIndex) {
		bits = expandBytes(bits, bitIndex);
		stand(bits, bitIndex);
		return bits;
	}
}
