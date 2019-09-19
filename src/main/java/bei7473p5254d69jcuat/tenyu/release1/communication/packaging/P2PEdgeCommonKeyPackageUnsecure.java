package bei7473p5254d69jcuat.tenyu.release1.communication.packaging;

import bei7473p5254d69jcuat.tenyu.release1.global.*;
import bei7473p5254d69jcuat.tenyu.release1.global.subjectivity.*;

/**
 * 主観の仮近傍リストからエッジを特定するタイプの共通鍵梱包
 * @author exceptiontenyu@gmail.com
 *
 */
public class P2PEdgeCommonKeyPackageUnsecure extends P2PEdgeCommonKeyPackage {
	public static interface P2PEdgeCommonKeyPackageUnsecureContent {
		default Package createPackage(P2PEdge e) {
			return new P2PEdgeCommonKeyPackageUnsecure(e.getEdgeId(),
					e.getFromOther().getEdgeId());
		}
	}

	public P2PEdgeCommonKeyPackageUnsecure() {
	}

	public P2PEdgeCommonKeyPackageUnsecure(long edgeId, long edgeId2) {
		super(edgeId, edgeId2);
	}

	@Override
	protected boolean isValidType(Object content) {
		return content instanceof P2PEdgeCommonKeyPackageUnsecureContent;
	}

	@Override
	protected UpdatableNeighborList getNeighborList() {
		return Glb.getSubje().getUnsecureNeighborList();
	}

}
