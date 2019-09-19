package bei7473p5254d69jcuat.tenyu.release1.communication.mutual.processorprovement;

import java.nio.charset.*;
import java.security.*;
import java.util.*;

import bei7473p5254d69jcuat.tenyu.release1.global.*;

public class ProblemSrc {
	private String rndStr;
	private int parallelNumber;
	private int year;
	private int month;
	private int day;
	private int hour;
	private byte[] p2pNodeId;

	public ProblemSrc() {
	}

	/**
	 * 自分が作成者の場合
	 * @param rndStr
	 */
	public ProblemSrc(String rndStr) {
		this.rndStr = rndStr;
		parallelNumber = 0;
		Calendar c = Calendar.getInstance(Locale.JAPAN);
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DATE);
		hour = c.get(Calendar.HOUR);
		p2pNodeId = Glb.getSubje().getMe().getP2PNodeId().getIdentifier();
	}

	public ProblemSrc(String rndStr, int year, int month, int day, int hour,
			int parallelNumber, byte[] p2pNodeId) {
		this.rndStr = rndStr;
		this.year = year;
		this.month = month;
		this.day = day;
		this.hour = hour;
		this.p2pNodeId = p2pNodeId;
		this.parallelNumber = parallelNumber;
	}

	/**
	 * parallelNumberだけ変えて既存のインスタンスから複製する
	 */
	public ProblemSrc(ProblemSrc src, int parallelNumber) {
		this.rndStr = src.getRndStr();
		this.year = src.getYear();
		this.month = src.getMonth();
		this.day = src.getDay();
		this.hour = src.getHour();
		this.p2pNodeId = src.getP2PNodeId();
		this.parallelNumber = parallelNumber;
	}

	private transient byte[] hash;

	/**
	 * @return	createHashのキャッシュ。
	 */
	public byte[] getHash() {
		return hash;
	}

	public byte[] createHash(MessageDigest md) {
		md.reset();
		md.update(rndStr.getBytes(Charset.forName("UTF-8")));
		md.update(Glb.getUtil().toBytes(year));
		md.update(Glb.getUtil().toBytes(month));
		md.update(Glb.getUtil().toBytes(day));
		md.update(Glb.getUtil().toBytes(hour));
		md.update(Glb.getUtil().toBytes(parallelNumber));
		md.update(p2pNodeId);
		hash = md.digest();
		return hash;
	}

	public boolean isNotNullDeep() {
		if (rndStr == null || year == 0 || month == 0 || day == 0)
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + day;
		result = prime * result + hour;
		result = prime * result + month;
		result = prime * result + Arrays.hashCode(p2pNodeId);
		result = prime * result + parallelNumber;
		result = prime * result + ((rndStr == null) ? 0 : rndStr.hashCode());
		result = prime * result + year;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ProblemSrc other = (ProblemSrc) obj;
		if (day != other.day)
			return false;
		if (hour != other.hour)
			return false;
		if (month != other.month)
			return false;
		if (!Arrays.equals(p2pNodeId, other.p2pNodeId))
			return false;
		if (parallelNumber != other.parallelNumber)
			return false;
		if (rndStr == null) {
			if (other.rndStr != null)
				return false;
		} else if (!rndStr.equals(other.rndStr))
			return false;
		if (year != other.year)
			return false;
		return true;
	}

	public String getClassName() {
		return "Problem" + parallelNumber;
	}

	public int getParallelNumber() {
		return parallelNumber;
	}

	public String getRndStr() {
		return rndStr;
	}

	public int getYear() {
		return year;
	}

	public int getMonth() {
		return month;
	}

	public int getDay() {
		return day;
	}

	public int getHour() {
		return hour;
	}

	public byte[] getP2PNodeId() {
		return p2pNodeId;
	}

}
