package bei7473p5254d69jcuat.tenyu.communication.request.subjectivity;

import bei7473p5254d69jcuat.tenyu.communication.*;
import bei7473p5254d69jcuat.tenyu.communication.packaging.SignedPackage.*;
import bei7473p5254d69jcuat.tenyu.communication.request.*;
import bei7473p5254d69jcuat.tenyu.model.release1.objectivity.*;
import bei7473p5254d69jcuat.tenyu.model.release1.subjectivity.*;
import glb.*;
import io.netty.channel.*;

/**
 * 全体運営者投票への立候補
 *
 * 立候補や全体運営者投票は、P2Pネットワークが大きく障害された時の復興のため、
 * 客観（ユーザーDB含む）成立前の段階で成立する必要がある。
 * しかしここでのユーザー概念への依存は、
 * 大規模障害の前に十分に多くのユーザーを獲得できれば、障害後も誰かユーザー登録済みの
 * 人がネットワーク上で生きているだろうと期待できるので、問題ない。
 *
 * 1.立候補者が署名してネットワークにメッセージを流す
 * 2.拡散段階。各ノードの{@link Subjectivity#getCandidacyList()}に加わる。
 * 3.有効化され立候補者リストに表示される
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public class Candidacy extends Request implements SignedPackageContent {
	/**
	 * ネットワークへの拡散時間
	 * ミリ秒
	 */
	public static final long diffusionTime = 1000 * 60 * 60 * 3;

	/**
	 * メッセージの有効期間
	 */
	public static final long expiredTime = 1000 * 60 * 60 * 24 * 7;

	/**
	 * 立候補した日時
	 */
	private long candidacyDate;

	/**
	 * @return	拡散段階が終わったか
	 */
	public boolean isActivated() {
		long now = Glb.getUtil().getEpochMilli();
		return now - candidacyDate > diffusionTime;
	}

	public long getDate() {
		return candidacyDate;
	}

	public void setCandidacyDate(long candidacyDate) {
		this.candidacyDate = candidacyDate;
	}

	@Override
	public boolean isValid(Response res) {
		return res instanceof StandardResponse;
	}

	@Override
	protected boolean validateRequestConcrete(Message m) {
		Long candidateUserId = m.getUserId();

		if (!IdObject.validateIdStandardNotSpecialId(candidateUserId)) {
			return false;
		}

		if (Glb.getObje().getUser(us -> us.get(candidateUserId) == null)) {
			return false;
		}

		if (isExpired())
			return false;

		return true;
	}

	/**
	 * @return 期限切れか
	 */
	public boolean isExpired() {
		long now = Glb.getUtil().getEpochMilli();
		//期限切れなら受信しない
		//さらにcandidacyDateを不正に極端に昔に設定する行為も防止される
		if (now > candidacyDate + expiredTime) {
			return true;
		}
		return false;
	}

	@Override
	public boolean received(ChannelHandlerContext ctx, Received validated) {
		boolean added = Glb.getSubje().getCandidacyList()
				.add(validated.getMessage());
		if (!added) {
			return false;
		}
		/* 返信無し
		ResultCode code = ResultCode.SUCCESS;
		Glb.debug("StandardResponse code=" + code);
		StandardResponse res = new StandardResponse(code);
		Message resM = Message.build(res).packaging(res.createPackage())
				.finish();
		Glb.getP2p().response(resM, ctx);
		*/
		return true;
	}

}
