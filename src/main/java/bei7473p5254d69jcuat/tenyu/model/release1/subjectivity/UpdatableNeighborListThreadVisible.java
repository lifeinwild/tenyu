package bei7473p5254d69jcuat.tenyu.model.release1.subjectivity;

import java.util.*;

import bei7473p5254d69jcuat.tenyu.model.release1.middle.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.P2PNode.*;

/**
 * UpdatableNeighborListはConcurrentHashMapを前提としていて、
 * synchronizedにしていない。
 * ConcurrentHashMapは他スレッドが書き込んだ値が見えるようになるまでに
 * かなり時間がかかる場合がある。
 * そこで、synchronized型を作った。このクラスはHashMapを前提とする。
 * @author exceptiontenyu@gmail.com
 *
 */
public class UpdatableNeighborListThreadVisible extends UpdatableNeighborList {
	public UpdatableNeighborListThreadVisible(int under, int max) {
		super(new HashMap<>(), under, max);
	}

	@Override
	public synchronized P2PEdge getNeighbor(byte[] addr, int p2pPort) {
		return super.getNeighbor(addr, p2pPort);
	}

	@Override
	public synchronized P2PEdge getNeighbor(byte[] p2pNodeId) {
		return super.getNeighbor(p2pNodeId);
	}

	@Override
	public synchronized P2PEdge getNeighbor(long edgeIdFromMe) {
		return super.getNeighbor(edgeIdFromMe);
	}

	@Override
	public synchronized P2PEdge addNeighbor(byte[] pubKey, int nodeNumber,
			AddrInfo addr) {
		return super.addNeighbor(pubKey, nodeNumber, addr);
	}

	@Override
	public synchronized P2PEdge addNeighbor(P2PEdge add) {
		return super.addNeighbor(add);
	}

	@Override
	public synchronized boolean removeNeighbor(byte[] p2pNodeId) {
		return super.removeNeighbor(p2pNodeId);
	}

	@Override
	public synchronized boolean removeNeighbor(long edgeId) {
		return super.removeNeighbor(edgeId);
	}

	@Override
	public synchronized String toString() {
		// TODO 自動生成されたメソッド・スタブ
		return super.toString();
	}

	public synchronized boolean removeNeighbor(
			NodeIdentifierP2PEdge p2pNodeId) {
		return super.removeNeighbor(p2pNodeId);
	}
}
