package bei7473p5254d69jcuat.tenyutalk.file;

import java.nio.*;
import java.util.*;

import glb.*;
import glb.util.Bits;

/**
 * 分散DLの状況を示すデータ。
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class WriteBits {
	/**
	 * 250KBが1bitになる。
	 */
	public static final long unit = 1024 * 250;

	public static WriteBits deserializeStatic(byte[] serialized) {
		WriteBits r = new WriteBits();
		r.deserialize(serialized);
		return r;
	}

	private Bits bits;

	private byte[] hash;

	public WriteBits clone() throws CloneNotSupportedException {
		WriteBits r = new WriteBits();
		r.setBits(bits == null ? null : bits.clone());
		r.setHash(hash == null ? null : hash.clone());
		return r;
	}

	public void deserialize(byte[] serialized) {
		ByteBuffer buf = ByteBuffer.wrap(serialized);
		int lastBitIndex = buf.getInt();
		int bitSize = Bits.size(lastBitIndex);
		int byteSize = Bits.bitSizeToByteSize(bitSize);
		int hashSize = Glb.getConst().getHashSize();
		hash = new byte[hashSize];
		buf.get(hash, 0, hashSize);
		byte[] bitsRaw = new byte[byteSize];
		buf.get(bitsRaw, 0, byteSize);
		Bits bits = new Bits();
		bits.set(lastBitIndex, bitsRaw);
		this.bits = bits;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		WriteBits other = (WriteBits) obj;
		if (bits == null) {
			if (other.bits != null)
				return false;
		} else if (!bits.equals(other.bits))
			return false;
		if (!Arrays.equals(hash, other.hash))
			return false;
		return true;
	}

	public Bits getBits() {
		return bits;
	}

	public byte[] getHash() {
		return hash;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bits == null) ? 0 : bits.hashCode());
		result = prime * result + Arrays.hashCode(hash);
		return result;
	}

	public boolean isWritten(long position, long size) {
		if (position < 0 || size == 0) {
			Glb.getLogger().warn(
					"Invalid size=" + size + " position=" + position,
					new IllegalArgumentException());
			return false;
		}
		int startIndex = (int) (position / unit);
		int indexCount = (int) (size / unit);
		if (size % unit != 0L) {
			indexCount++;
		}

		for (int currentIndex = startIndex; currentIndex < startIndex
				+ indexCount; currentIndex++) {
			if (!bits.isStand(currentIndex)) {
				return false;
			}
		}
		return true;
	}

	public byte[] serialize() {
		ByteBuffer buf = ByteBuffer.allocate(Integer.BYTES
				+ Glb.getConst().getHashSize() + bits.getBinary().length);
		buf.putInt(bits.getLastBitIndex());
		buf.put(hash);
		buf.put(bits.getBinary());
		return buf.array();
	}

	public void setBits(Bits bits) {
		this.bits = bits;
	}

	public void setHash(byte[] hash) {
		this.hash = hash;
	}
}